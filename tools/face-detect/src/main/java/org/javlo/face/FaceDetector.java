package org.javlo.face;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

/**
 * Detects faces in an image with the YuNet model, on CPU only. No GPU and no network call: the 227 kB
 * model ships inside the jar and one detection costs a few tens of milliseconds.
 *
 * <pre>
 * try (FaceDetector detector = new FaceDetector()) {
 * 	Rectangle box = detector.detectMainFace(ImageIO.read(file));
 * }
 * </pre>
 *
 * Building a detector loads the model, so keep one instance and share it: detection is thread safe.
 */
public class FaceDetector implements AutoCloseable {

	/** Below this confidence a candidate is not reported as a face. */
	public static final float DEFAULT_SCORE_THRESHOLD = 0.6f;

	/** Two boxes overlapping by more than this ratio are considered to be the same face. */
	public static final float DEFAULT_NMS_THRESHOLD = 0.3f;

	/** Upper bound on the number of faces returned for one image. */
	public static final int DEFAULT_MAX_FACES = 500;

	private static final String EMBEDDED_MODEL = "/models/face_detection_yunet_2023mar.onnx";

	/** The exported model has a fixed input, so every image is letterboxed into this square. */
	private static final int INPUT_SIZE = 640;

	private static final String INPUT_NAME = "input";

	private final OrtEnvironment environment;
	private final OrtSession session;

	private float scoreThreshold = DEFAULT_SCORE_THRESHOLD;
	private float nmsThreshold = DEFAULT_NMS_THRESHOLD;
	private int maxFaces = DEFAULT_MAX_FACES;

	/** Loads the model embedded in the jar. */
	public FaceDetector() throws IOException {
		this(readEmbeddedModel());
	}

	/** Loads a YuNet model from disk, for instance a newer release of the same network. */
	public FaceDetector(Path modelFile) throws IOException {
		this(Files.readAllBytes(modelFile));
	}

	private FaceDetector(byte[] model) throws IOException {
		try {
			environment = OrtEnvironment.getEnvironment();
			session = environment.createSession(model, new OrtSession.SessionOptions());
		} catch (OrtException e) {
			throw new IOException("cannot initialise the YuNet model", e);
		}
	}

	private static byte[] readEmbeddedModel() throws IOException {
		try (InputStream in = FaceDetector.class.getResourceAsStream(EMBEDDED_MODEL)) {
			if (in == null) {
				throw new IOException("model " + EMBEDDED_MODEL + " missing from the classpath");
			}
			return in.readAllBytes();
		}
	}

	/**
	 * Finds every face of the image, sorted by descending confidence.
	 *
	 * @return the faces found, an empty list if there is none
	 * @throws FaceDetectionException if the inference itself fails
	 */
	public List<Face> detect(BufferedImage image) {
		if (image == null || image.getWidth() == 0 || image.getHeight() == 0) {
			return Collections.emptyList();
		}
		// letterbox: the image keeps its aspect ratio in the top left corner of the square input
		float scale = Math.min((float) INPUT_SIZE / image.getWidth(), (float) INPUT_SIZE / image.getHeight());
		float[] input = toInputTensor(image, scale);

		List<RawDetection> detections = YuNetDecoder.nms(run(input), nmsThreshold, maxFaces);

		List<Face> faces = new ArrayList<>(detections.size());
		for (RawDetection detection : detections) {
			faces.add(toFace(detection, scale, image.getWidth(), image.getHeight()));
		}
		return faces;
	}

	/**
	 * The box of the largest face of the image, which is usually the subject of a portrait. Useful to
	 * centre a crop on somebody.
	 *
	 * @return the bounding box, or null when no face is found
	 */
	public Rectangle detectMainFace(BufferedImage image) {
		Rectangle largest = null;
		for (Face face : detect(image)) {
			Rectangle bounds = face.getBounds();
			if (largest == null
					|| (long) bounds.width * bounds.height > (long) largest.width * largest.height) {
				largest = bounds;
			}
		}
		return largest;
	}

	/**
	 * The smallest box containing every face of the image.
	 *
	 * @return the enclosing box, or null when no face is found
	 */
	public Rectangle detectFacesBounds(BufferedImage image) {
		Rectangle union = null;
		for (Face face : detect(image)) {
			union = union == null ? face.getBounds() : union.union(face.getBounds());
		}
		return union;
	}

	/** Draws the scaled image into the square input and lays the pixels out as BGR planes. */
	private float[] toInputTensor(BufferedImage image, float scale) {
		BufferedImage canvas = new BufferedImage(INPUT_SIZE, INPUT_SIZE, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = canvas.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics.setColor(Color.BLACK);
			graphics.fillRect(0, 0, INPUT_SIZE, INPUT_SIZE);
			graphics.drawImage(image, 0, 0, Math.round(image.getWidth() * scale),
					Math.round(image.getHeight() * scale), null);
		} finally {
			graphics.dispose();
		}

		int pixelCount = INPUT_SIZE * INPUT_SIZE;
		int[] pixels = canvas.getRGB(0, 0, INPUT_SIZE, INPUT_SIZE, null, 0, INPUT_SIZE);
		// YuNet was trained on OpenCV images, so the channels are ordered blue, green, red
		float[] tensor = new float[3 * pixelCount];
		for (int i = 0; i < pixelCount; i++) {
			int rgb = pixels[i];
			tensor[i] = rgb & 0xFF;
			tensor[pixelCount + i] = (rgb >> 8) & 0xFF;
			tensor[2 * pixelCount + i] = (rgb >> 16) & 0xFF;
		}
		return tensor;
	}

	private List<RawDetection> run(float[] input) {
		long[] shape = { 1, 3, INPUT_SIZE, INPUT_SIZE };
		synchronized (session) {
			try (OnnxTensor tensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(input), shape);
					OrtSession.Result result = session.run(Map.of(INPUT_NAME, tensor))) {
				Map<String, float[]> outputs = new HashMap<>();
				for (Map.Entry<String, OnnxValue> entry : result) {
					FloatBuffer buffer = ((OnnxTensor) entry.getValue()).getFloatBuffer();
					float[] values = new float[buffer.remaining()];
					buffer.get(values);
					outputs.put(entry.getKey(), values);
				}
				return YuNetDecoder.decode(byStride(outputs, "cls"), byStride(outputs, "obj"),
						byStride(outputs, "bbox"), byStride(outputs, "kps"), INPUT_SIZE, INPUT_SIZE,
						scoreThreshold);
			} catch (OrtException e) {
				throw new FaceDetectionException("face detection failed", e);
			}
		}
	}

	/** Gathers the three tensors of one family, ordered like {@link YuNetDecoder#STRIDES}. */
	private static float[][] byStride(Map<String, float[]> outputs, String family) {
		float[][] tensors = new float[YuNetDecoder.STRIDES.length][];
		for (int i = 0; i < YuNetDecoder.STRIDES.length; i++) {
			String name = family + "_" + YuNetDecoder.STRIDES[i];
			tensors[i] = outputs.get(name);
			if (tensors[i] == null) {
				throw new FaceDetectionException("output " + name + " missing from the model");
			}
		}
		return tensors;
	}

	/** Maps a detection back onto the source image and clips it to the image bounds. */
	private static Face toFace(RawDetection detection, float scale, int imageWidth, int imageHeight) {
		int x = Math.round(detection.x / scale);
		int y = Math.round(detection.y / scale);
		int width = Math.round(detection.width / scale);
		int height = Math.round(detection.height / scale);

		Rectangle bounds = new Rectangle(x, y, width, height)
				.intersection(new Rectangle(0, 0, imageWidth, imageHeight));

		Point[] landmarks = new Point[5];
		for (int k = 0; k < 5; k++) {
			landmarks[k] = new Point(Math.round(detection.landmarks[2 * k] / scale),
					Math.round(detection.landmarks[2 * k + 1] / scale));
		}
		return new Face(bounds, detection.score, landmarks);
	}

	public float getScoreThreshold() {
		return scoreThreshold;
	}

	/** Lower it to catch more faces at the cost of false positives, raise it to be stricter. */
	public void setScoreThreshold(float scoreThreshold) {
		this.scoreThreshold = scoreThreshold;
	}

	public float getNmsThreshold() {
		return nmsThreshold;
	}

	public void setNmsThreshold(float nmsThreshold) {
		this.nmsThreshold = nmsThreshold;
	}

	public int getMaxFaces() {
		return maxFaces;
	}

	public void setMaxFaces(int maxFaces) {
		this.maxFaces = maxFaces;
	}

	@Override
	public void close() {
		try {
			session.close();
		} catch (OrtException e) {
			throw new FaceDetectionException("cannot release the model", e);
		}
	}
}
