package org.javlo.mailing;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class FeedBackMailingDisplayBean {

	private String email;

	private Date date;

	private String agent;

	private List<String> url = new LinkedList<String>();

	private List<String> action = new LinkedList<String>();;

	public String getAgent() {
		if (agent == null) {
			return "";
		}
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date inDate) {
		if ((date!=null)&&(inDate!=null)&&(inDate.before(date))) {
			date = inDate;
		}
	}

	public String getEmail() {
		if (email == null) {
			return "";
		}
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<String> getUrl() {
		return url;
	}

	public void addUrl(String inURL) {
		if ((inURL != null) && (inURL.trim().length() > 0)) {
			if (!url.contains(inURL)) {
				url.add(inURL);
			}
		}
	}

	public List<String> getAction() {
		return action;
	}

	public void addAction(String inAction) {
		if ((inAction != null) && (inAction.trim().length() > 0)) {
			if (!action.contains(inAction)) {
				action.add(inAction);
			}
		}
	}
}
