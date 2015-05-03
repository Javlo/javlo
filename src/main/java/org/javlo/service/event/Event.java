package org.javlo.service.event;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

public class Event implements Serializable {
	
	public static final Event NO_EVENT = new Event(null,null,null,null,null);

	private static final long serialVersionUID = 1L;

	private String id;
	private URL url;
	private Date start;
	private Date end;
	private String summary;
	private String location;
	private String category;
	private String status;
	private String description;
	private int sequence;
	private String user;

	public Event(String id, Date start, Date end, String summary, String description) {
		System.out.println("***** Event.Event : summary = "+summary); //TODO: remove debug trace
		this.id = id;
		this.start = start;
		this.end = end;
		this.summary = summary;
		this.description = description;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	
	public String getProdID() {
		return "-//ImmanenceSPRL//NONSGML Javlo//EN";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

}
