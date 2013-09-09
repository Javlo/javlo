package org.javlo.client.localmodule.model;

import java.io.IOException;

public class HttpException extends IOException {

	private static final long serialVersionUID = 2574626703421423553L;

	public HttpException() {
	}

	public HttpException(String message) {
		super(message);
	}

	public HttpException(Throwable cause) {
		super(cause);
	}

	public HttpException(String message, Throwable cause) {
		super(message, cause);
	}

}
