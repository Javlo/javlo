package org.javlo.module.ticket;

import java.io.Serializable;
import java.util.Date;

import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;

public class Comment implements Serializable {

	private static final long serialVersionUID = 1L;
	private String authors;
	private String message;
	private Date creationDate = new Date();

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
	
	public String getHtmlMessage() {
		return XHTMLHelper.textToXHTML(message);
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return StringHelper.neverNull(authors)+" : "+StringHelper.neverNull(getHtmlMessage());
	}

	public Date getCreationDate() {
		return creationDate;
	}
	
	public String getCreationDateString() {
		return StringHelper.renderTime(getCreationDate());
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
}
