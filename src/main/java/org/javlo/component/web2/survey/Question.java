package org.javlo.component.web2.survey;

import java.util.List;

public class Question {
	
	private int number;
	private String label;
	private List<Response> responses;
	
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public List<Response> getResponses() {
		return responses;
	}
	public void setResponses(List<Response> response) {
		this.responses = response;
	}

}
