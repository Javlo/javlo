package org.javlo.servlet.status;

public class CheckBean {
	
	private boolean error = false;
	private String label = null;
	private String message = null;
	
	public CheckBean(String label, String message, boolean error) {
		super();
		this.error = error;
		this.label = label;
		this.message = message;
	}
	public boolean isError() {
		return error;
	}
	public void setError(boolean error) {
		this.error = error;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getTableHtml() {
		String style="background-color: #28a745; color: #ffffff; padding: 8px;";
		if (error) {
			style="background-color: #dc3545; color: #ffffff; padding: 8px;";
		}
		return "<tr data-error=\""+error+"\"><td style=\"background-color: #eeeeee; color: #000000; padding: 8px;\">"+label+"</td>"+"<td style=\""+style+"\">"+message+"</td></tr>";
	}
	
	

}
