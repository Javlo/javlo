package org.javlo.service.syncro.exception;

public class SynchroNonFatalException extends Exception {
	
	private static final long serialVersionUID = 2603047944162614611L;

	public SynchroNonFatalException() {
		super();
	}
	public SynchroNonFatalException(String message, Throwable cause) {
		super(message, cause);
	}
	public SynchroNonFatalException(String message) {
		super(message);
	}
	public SynchroNonFatalException(Throwable cause) {
		super(cause);
	}

}
