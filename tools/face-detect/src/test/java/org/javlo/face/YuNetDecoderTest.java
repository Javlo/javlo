package org.javlo.face;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Tests the pure decoding logic, without loading the ONNX model. The tensors are built by hand for a
 * 64x64 network input, which gives 8x8 anchors on stride 8, 4x4 on stride 16 and 2x2 on stride 32.
 */
public class YuNetDecoderTest {

	private static final int SIZE = 64;

	/** Builds the four output tensors of a 64x64 input, all filled with zeros. */
	private static float[][][] emptyOutputs() {
		float[][] cls = new float[3][];
		float[][] obj = new float[3][];
		float[][] bbox = new float[3][];
		float[][] kps = new float[3][];
		for (int s = 0; s < 3; s++) {
			int anchors = (SIZE / YuNetDecoder.STRIDES[s]) * (SIZE / YuNetDecoder.STRIDES[s]);
			cls[s] = new float[anchors];
			obj[s] = new float[anchors];
			bbox[s] = new float[anchors * 4];
			kps[s] = new float[anchors * 10];
		}
		return new float[][][] { cls, obj, bbox, kps };
	}

	private static List<RawDetection> decode(float[][][] out, float threshold) {
		return YuNetDecoder.decode(out[0], out[1], out[2], out[3], SIZE, SIZE, threshold);
	}

	@Test
	public void decodesAnchorIntoPixelBox() {
		float[][][] out = emptyOutputs();
		int cols = SIZE / 8;
		int anchor = 2 * cols + 3; // row 2, column 3
		out[0][0][anchor] = 1f;
		out[1][0][anchor] = 1f;
		out[2][0][anchor * 4] = 0.5f; // dx
		out[2][0][anchor * 4 + 1] = 0.5f; // dy
		out[2][0][anchor * 4 + 2] = (float) Math.log(2); // width  = exp(ln 2) * 8 = 16
		out[2][0][anchor * 4 + 3] = (float) Math.log(3); // height = exp(ln 3) * 8 = 24

		List<RawDetection> faces = decode(out, 0.5f);

		assertEquals(1, faces.size());
		RawDetection f = faces.get(0);
		assertEquals(16f, f.width, 1e-3);
		assertEquals(24f, f.height, 1e-3);
		// centre = ((3 + 0.5) * 8, (2 + 0.5) * 8) = (28, 20), so the top left corner is (20, 8)
		assertEquals(20f, f.x, 1e-3);
		assertEquals(8f, f.y, 1e-3);
	}

	@Test
	public void scoreIsGeometricMeanOfClassAndObjectness() {
		float[][][] out = emptyOutputs();
		out[0][0][0] = 0.25f;
		out[1][0][0] = 1f;

		List<RawDetection> faces = decode(out, 0.1f);

		assertEquals(1, faces.size());
		assertEquals(0.5f, faces.get(0).score, 1e-4);
	}

	@Test
	public void dropsAnchorsBelowThreshold() {
		float[][][] out = emptyOutputs();
		out[0][0][0] = 0.2f;
		out[1][0][0] = 0.2f; // score = 0.2

		assertTrue(decode(out, 0.3f).isEmpty());
		assertEquals(1, decode(out, 0.1f).size());
	}

	@Test
	public void decodesTheFiveLandmarks() {
		float[][][] out = emptyOutputs();
		int cols = SIZE / 16;
		int anchor = 1 * cols + 1; // row 1, column 1 on stride 16
		out[0][1][anchor] = 1f;
		out[1][1][anchor] = 1f;
		for (int k = 0; k < 5; k++) {
			out[3][1][anchor * 10 + 2 * k] = 0.25f * k;
			out[3][1][anchor * 10 + 2 * k + 1] = 0.5f;
		}

		List<RawDetection> faces = decode(out, 0.5f);

		assertEquals(1, faces.size());
		float[] marks = faces.get(0).landmarks;
		for (int k = 0; k < 5; k++) {
			assertEquals((1 + 0.25f * k) * 16, marks[2 * k], 1e-3);
			assertEquals((1 + 0.5f) * 16, marks[2 * k + 1], 1e-3);
		}
	}

	@Test
	public void decodesEveryStride() {
		float[][][] out = emptyOutputs();
		for (int s = 0; s < 3; s++) {
			out[0][s][0] = 1f;
			out[1][s][0] = 1f;
		}

		assertEquals(3, decode(out, 0.5f).size());
	}

	@Test
	public void suppressesOverlappingBoxes() {
		RawDetection strong = new RawDetection(10, 10, 100, 100, 0.9f, new float[10]);
		RawDetection weak = new RawDetection(12, 12, 100, 100, 0.4f, new float[10]);

		List<RawDetection> kept = YuNetDecoder.nms(Arrays.asList(weak, strong), 0.3f, 100);

		assertEquals(1, kept.size());
		assertEquals(0.9f, kept.get(0).score, 1e-4);
	}

	@Test
	public void keepsBoxesThatDoNotOverlap() {
		RawDetection left = new RawDetection(0, 0, 50, 50, 0.9f, new float[10]);
		RawDetection right = new RawDetection(200, 200, 50, 50, 0.8f, new float[10]);

		assertEquals(2, YuNetDecoder.nms(Arrays.asList(left, right), 0.3f, 100).size());
	}

	@Test
	public void sortsByDescendingScoreAndHonoursTopK() {
		RawDetection a = new RawDetection(0, 0, 10, 10, 0.3f, new float[10]);
		RawDetection b = new RawDetection(100, 0, 10, 10, 0.7f, new float[10]);
		RawDetection c = new RawDetection(200, 0, 10, 10, 0.5f, new float[10]);

		List<RawDetection> kept = YuNetDecoder.nms(Arrays.asList(a, b, c), 0.3f, 2);

		assertEquals(2, kept.size());
		assertEquals(0.7f, kept.get(0).score, 1e-4);
		assertEquals(0.5f, kept.get(1).score, 1e-4);
	}
}
