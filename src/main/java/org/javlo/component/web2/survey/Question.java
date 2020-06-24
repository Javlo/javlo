package org.javlo.component.web2.survey;

import java.util.List;
import java.util.logging.Logger;

public class Question {
	
	private static Logger logger = Logger.getLogger(Question.class.getName());

	private int number;
	private String label;
	private List<Response> responses;
	private Response response;

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

	public String getInputName() {
		return "q" + number;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(String resp) {
		if (resp == null) {
			this.response = null;
		} else {
			boolean notfound = true;
			for (Response r : responses) {
				if (r.isEquals(resp)) {
					this.response = r;
					notfound=false;
				}
			}
			if (notfound) {
				logger.warning("response not found : "+resp);
			}
		}
	}

	@Override
	public String toString() {
		return "" + number + '.' + label + " >>> " + (response!=null?response.getLabel():"NO RESPONSE");
	}

}
