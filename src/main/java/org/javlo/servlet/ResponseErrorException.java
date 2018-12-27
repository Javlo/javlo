package org.javlo.servlet;

import javax.servlet.ServletException;

public class ResponseErrorException extends ServletException {
	
	private int responseStatus = -1;
	
	public ResponseErrorException(int errorCode) {
		super();
		this.responseStatus = errorCode;
	}

	public int getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(int errorCode) {
		this.responseStatus = errorCode;
	}

}
