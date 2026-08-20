package org.javlo.face;

import java.awt.Point;
import java.awt.Rectangle;

/** One face found in an image: its bounding box, its confidence and its five landmarks. */
public final class Face {

	private final Rectangle bounds;
	private final float score;
	private final Point[] landmarks;

	Face(Rectangle bounds, float score, Point[] landmarks) {
		this.bounds = bounds;
		this.score = score;
		this.landmarks = landmarks;
	}

	/** The bounding box of the face, in pixels of the source image. */
	public Rectangle getBounds() {
		return new Rectangle(bounds);
	}

	/** Confidence between 0 and 1. */
	public float getScore() {
		return score;
	}

	/** Centre of the bounding box, handy to position a crop. */
	public Point getCenter() {
		return new Point((int) bounds.getCenterX(), (int) bounds.getCenterY());
	}

	/**
	 * The five landmarks, in this order: right eye, left eye, nose, right mouth corner, left mouth
	 * corner. Left and right are those of the subject, so the right eye appears on the left of the
	 * picture.
	 */
	public Point[] getLandmarks() {
		Point[] copy = new Point[landmarks.length];
		for (int i = 0; i < landmarks.length; i++) {
			copy[i] = new Point(landmarks[i]);
		}
		return copy;
	}

	public Point getRightEye() {
		return new Point(landmarks[0]);
	}

	public Point getLeftEye() {
		return new Point(landmarks[1]);
	}

	public Point getNose() {
		return new Point(landmarks[2]);
	}

	public Point getRightMouthCorner() {
		return new Point(landmarks[3]);
	}

	public Point getLeftMouthCorner() {
		return new Point(landmarks[4]);
	}

	@Override
	public String toString() {
		return String.format("Face[x=%d, y=%d, w=%d, h=%d, score=%.3f]", bounds.x, bounds.y, bounds.width,
				bounds.height, score);
	}
}
