package org.javlo.mailing;

import java.text.ParseException;
import java.util.Date;

import org.javlo.helper.StringHelper;


public class FeedBackMailingBean {

	private String email;

	private Date date;

	private String agent;

	private String url;
	
	private String action;
	
	private String ip;

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String[] toArray() {
		String[] outArray = new String[5];
		outArray[0] = getEmail();
		outArray[1] = StringHelper.renderTime(getDate());
		outArray[2] = getUrl();
		outArray[3] = getAgent();
		outArray[4] = getIp();
		return outArray;
	}
	
	public void fromArray(String[] array) {
		setEmail(array[0]);
		try {
			setDate(StringHelper.parseTime(array[1]));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		setUrl(array[2]);		
		setAgent(array[3]);
		if (array.length>4) {
			setIp(array[4]);
		}
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
