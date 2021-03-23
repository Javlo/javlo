package org.javlo.actions;

public class EcomStatus {

	public static EcomStatus VALID = new EcomStatus(false);

	public EcomStatus() {
	}

	public EcomStatus(boolean error) {
		this.error = error;
	}

	public EcomStatus(String message, boolean error) {
		super();
		this.message = message;
		this.error = error;
	}

	private String message;
	private boolean error = false;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

}
