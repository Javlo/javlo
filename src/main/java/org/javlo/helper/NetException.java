package org.javlo.helper;

import java.io.IOException;

public class NetException extends IOException {

	private static final long serialVersionUID = -799020972739591748L;

	public NetException() {
		super();
	}

	public NetException(String message, Throwable cause) {
		super(message, cause);
	}

	public NetException(String message) {
		super(message);
	}

	public NetException(Throwable cause) {
		super(cause);
	}

}
