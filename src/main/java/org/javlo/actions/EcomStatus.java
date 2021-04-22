package org.javlo.actions;

public class EcomStatus {

	public static EcomStatus VALID = new EcomStatus(false);
	
	private String message;
	private boolean error = false;
	private String invoiceHash;

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

	public String getInvoiceHash() {
		return invoiceHash;
	}

	public void setInvoiceHash(String invoiceHash) {
		this.invoiceHash = invoiceHash;
	}

}
