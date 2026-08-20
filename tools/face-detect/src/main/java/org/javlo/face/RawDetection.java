package org.javlo.face;

/**
 * A detection expressed in the coordinate system of the network input (640x640 pixels), before it is
 * mapped back onto the source image.
 */
final class RawDetection {

	final float x;
	final float y;
	final float width;
	final float height;
	final float score;

	/** Five landmarks as x/y pairs: right eye, left eye, nose, right mouth corner, left mouth corner. */
	final float[] landmarks;

	RawDetection(float x, float y, float width, float height, float score, float[] landmarks) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.score = score;
		this.landmarks = landmarks;
	}

	float area() {
		return width * height;
	}

	float intersectionOverUnion(RawDetection other) {
		float left = Math.max(x, other.x);
		float top = Math.max(y, other.y);
		float right = Math.min(x + width, other.x + other.width);
		float bottom = Math.min(y + height, other.y + other.height);
		if (right <= left || bottom <= top) {
			return 0f;
		}
		float intersection = (right - left) * (bottom - top);
		return intersection / (area() + other.area() - intersection);
	}
}
