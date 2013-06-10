package org.javlo.mailing;

/**
 * this bean represent a email.
 * @author pvandermaesen
 *
 */
public class Mail {	
	private String subject;
	private String content;
	private boolean html;
	public Mail(String subject, String content) {		
		this.subject = subject;
		this.content = content;
		this.html = false;
		if (content.toLowerCase().contains("<p") || content.toLowerCase().contains("<span") || content.toLowerCase().contains("<a") || content.toLowerCase().contains("<div")  || content.toLowerCase().contains("<br")) {
			this.html = true;
		}
	}
	public Mail(String subject, String content, boolean html) {		
		this.subject = subject;
		this.content = content;
		this.html = html;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public boolean isHtml() {
		return html;
	}
	public void setHtml(boolean html) {
		this.html = html;
	}
}
