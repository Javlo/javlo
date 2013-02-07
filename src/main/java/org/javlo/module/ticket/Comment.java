package org.javlo.module.ticket;

import java.io.Serializable;

public class Comment implements Serializable {

	private static final long serialVersionUID = 1L;
	private String authors;
	private String message;

	public Comment() {
	}

	public Comment(String authors, String message) {
		this.authors = authors;
		this.message = message;
	}

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
