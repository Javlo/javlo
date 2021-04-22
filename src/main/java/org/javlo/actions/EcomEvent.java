package org.javlo.actions;

public class EcomEvent {
	
	private String paymentIntent;
	private boolean valid;
	private String errorMessage;
	
	public EcomEvent(String paymentIntent, boolean valid, String errorMessage) {
		super();
		this.paymentIntent = paymentIntent;
		this.valid = valid;
		this.errorMessage = errorMessage;
	}
	public String getPaymentIntent() {
		return paymentIntent;
	}
	public void setPaymentIntent(String paymentIntent) {
		this.paymentIntent = paymentIntent;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	

}
