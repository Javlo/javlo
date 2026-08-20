package org.javlo.face;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Turns the twelve raw YuNet output tensors into detections. YuNet is anchor free: it predicts one
 * candidate per cell of three feature maps, downscaled by 8, 16 and 32. This class holds no state and
 * never touches the ONNX runtime, so it can be tested on hand written tensors.
 */
final class YuNetDecoder {

	/** Downscaling factors of the three feature maps, in the order the tensors are passed in. */
	static final int[] STRIDES = { 8, 16, 32 };

	private YuNetDecoder() {
	}

	/**
	 * Decodes the network outputs into boxes expressed in network input pixels.
	 *
	 * @param cls  per stride, one face probability per cell
	 * @param obj  per stride, one objectness score per cell
	 * @param bbox per stride, four values per cell: centre offsets then log scaled width and height
	 * @param kps  per stride, ten values per cell: five landmark offsets as x/y pairs
	 */
	static List<RawDetection> decode(float[][] cls, float[][] obj, float[][] bbox, float[][] kps,
			int inputWidth, int inputHeight, float scoreThreshold) {
		List<RawDetection> detections = new ArrayList<>();
		for (int s = 0; s < STRIDES.length; s++) {
			int stride = STRIDES[s];
			int cols = inputWidth / stride;
			int rows = inputHeight / stride;
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < cols; col++) {
					int cell = row * cols + col;
					float score = (float) Math.sqrt(clamp01(cls[s][cell]) * clamp01(obj[s][cell]));
					if (score < scoreThreshold) {
						continue;
					}
					float centerX = (col + bbox[s][cell * 4]) * stride;
					float centerY = (row + bbox[s][cell * 4 + 1]) * stride;
					float width = (float) Math.exp(bbox[s][cell * 4 + 2]) * stride;
					float height = (float) Math.exp(bbox[s][cell * 4 + 3]) * stride;

					float[] landmarks = new float[10];
					for (int k = 0; k < 5; k++) {
						landmarks[2 * k] = (col + kps[s][cell * 10 + 2 * k]) * stride;
						landmarks[2 * k + 1] = (row + kps[s][cell * 10 + 2 * k + 1]) * stride;
					}
					detections.add(new RawDetection(centerX - width / 2, centerY - height / 2, width, height,
							score, landmarks));
				}
			}
		}
		return detections;
	}

	/**
	 * Greedy non maximum suppression: keeps the best scoring box, drops every box overlapping it by more
	 * than the given ratio, and repeats. The result is sorted by descending score.
	 */
	static List<RawDetection> nms(List<RawDetection> detections, float iouThreshold, int topK) {
		List<RawDetection> candidates = new ArrayList<>(detections);
		candidates.sort(Comparator.comparingDouble((RawDetection d) -> d.score).reversed());

		List<RawDetection> kept = new ArrayList<>();
		for (RawDetection candidate : candidates) {
			if (kept.size() >= topK) {
				break;
			}
			boolean overlaps = false;
			for (RawDetection alreadyKept : kept) {
				if (candidate.intersectionOverUnion(alreadyKept) > iouThreshold) {
					overlaps = true;
					break;
				}
			}
			if (!overlaps) {
				kept.add(candidate);
			}
		}
		return kept;
	}

	private static float clamp01(float value) {
		return value < 0f ? 0f : (value > 1f ? 1f : value);
	}
}
