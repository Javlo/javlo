package org.javlo.face;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

/**
 * Command line front end: prints the faces of an image as JSON and, on request, writes a copy of the
 * image with the boxes drawn on it.
 *
 * <pre>
 * java -jar face-detect.jar photo.jpg
 * java -jar face-detect.jar photo.jpg --annotate marked.jpg --threshold 0.5
 * </pre>
 */
public final class FaceDetectorCLI {

	private FaceDetectorCLI() {
	}

	public static void main(String[] args) throws IOException {
		if (args.length == 0 || args[0].startsWith("--")) {
			usage();
			System.exit(1);
		}
		File source = new File(args[0]);
		File annotated = null;
		float threshold = FaceDetector.DEFAULT_SCORE_THRESHOLD;

		for (int i = 1; i < args.length; i++) {
			switch (args[i]) {
			case "--annotate":
				annotated = new File(requireValue(args, ++i, "--annotate"));
				break;
			case "--threshold":
				threshold = Float.parseFloat(requireValue(args, ++i, "--threshold"));
				break;
			default:
				System.err.println("unknown option: " + args[i]);
				usage();
				System.exit(1);
			}
		}

		BufferedImage image = ImageIO.read(source);
		if (image == null) {
			System.err.println("cannot read image: " + source);
			System.exit(2);
		}

		List<Face> faces;
		long millis;
		try (FaceDetector detector = new FaceDetector()) {
			detector.setScoreThreshold(threshold);
			// the model is already loaded, so this times the detection alone
			long start = System.nanoTime();
			faces = detector.detect(image);
			millis = (System.nanoTime() - start) / 1_000_000;
		}

		System.out.println(toJson(source, image, faces, millis));

		if (annotated != null) {
			write(annotated, draw(image, faces));
			System.err.println("annotated image written to " + annotated);
		}
	}

	private static String requireValue(String[] args, int index, String option) {
		if (index >= args.length) {
			System.err.println(option + " needs a value");
			usage();
			System.exit(1);
		}
		return args[index];
	}

	private static void usage() {
		System.err.println("usage: face-detect <image> [--annotate <output>] [--threshold <0..1>]");
	}

	static String toJson(File source, BufferedImage image, List<Face> faces, long millis) {
		StringBuilder json = new StringBuilder();
		json.append("{\n");
		json.append("  \"image\": \"").append(escape(source.getPath())).append("\",\n");
		json.append("  \"width\": ").append(image.getWidth()).append(",\n");
		json.append("  \"height\": ").append(image.getHeight()).append(",\n");
		json.append("  \"elapsedMs\": ").append(millis).append(",\n");
		json.append("  \"faceCount\": ").append(faces.size()).append(",\n");
		json.append("  \"faces\": [");
		for (int i = 0; i < faces.size(); i++) {
			Face face = faces.get(i);
			Rectangle bounds = face.getBounds();
			json.append(i == 0 ? "\n" : ",\n");
			json.append("    {\"x\": ").append(bounds.x);
			json.append(", \"y\": ").append(bounds.y);
			json.append(", \"width\": ").append(bounds.width);
			json.append(", \"height\": ").append(bounds.height);
			json.append(String.format(Locale.ROOT, ", \"score\": %.4f", face.getScore()));
			json.append(", \"landmarks\": [");
			Point[] landmarks = face.getLandmarks();
			for (int k = 0; k < landmarks.length; k++) {
				json.append(k == 0 ? "" : ", ");
				json.append("[").append(landmarks[k].x).append(", ").append(landmarks[k].y).append("]");
			}
			json.append("]}");
		}
		json.append(faces.isEmpty() ? "]\n" : "\n  ]\n");
		json.append("}");
		return json.toString();
	}

	private static String escape(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private static BufferedImage draw(BufferedImage image, List<Face> faces) {
		BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = copy.createGraphics();
		try {
			graphics.drawImage(image, 0, 0, null);
			graphics.setStroke(new BasicStroke(Math.max(2f, image.getWidth() / 400f)));
			for (Face face : faces) {
				Rectangle bounds = face.getBounds();
				graphics.setColor(Color.GREEN);
				graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
				graphics.setColor(Color.RED);
				for (Point landmark : face.getLandmarks()) {
					graphics.fillOval(landmark.x - 2, landmark.y - 2, 5, 5);
				}
			}
		} finally {
			graphics.dispose();
		}
		return copy;
	}

	private static void write(File target, BufferedImage image) throws IOException {
		String name = target.getName();
		int dot = name.lastIndexOf('.');
		String format = dot < 0 ? "png" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
		if (!ImageIO.write(image, format, target)) {
			throw new IOException("no image writer for format " + format);
		}
	}
}
