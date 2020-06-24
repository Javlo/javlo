package org.javlo.component.web2.survey;

public class Response {
	
	private String label;
	private int number;
	
	public Response(String label, int number) {
		super();
		this.label = label;
		this.number = number;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public boolean isEquals(String val) {
		if (val == null) {
			return false;
		} else {
			return val.contentEquals(""+number) || val.equals(label);
		}
	}
	
}