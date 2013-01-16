package org.javlo.service.syncro.exception;

public class SynchroFatalException extends Exception {
	
	private static final long serialVersionUID = -8573439489932572775L;

	public SynchroFatalException() {
		super();
	}
	public SynchroFatalException(String message, Throwable cause) {
		super(message, cause);
	}
	public SynchroFatalException(String message) {
		super(message);
	}
	public SynchroFatalException(Throwable cause) {
		super(cause);
	}

}
