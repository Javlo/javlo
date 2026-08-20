package org.javlo.face;

/** Raised when a detection cannot be carried out, which points at a broken model or runtime. */
public class FaceDetectionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FaceDetectionException(String message) {
		super(message);
	}

	public FaceDetectionException(String message, Throwable cause) {
		super(message, cause);
	}
}
