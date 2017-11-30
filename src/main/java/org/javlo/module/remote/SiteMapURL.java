package org.javlo.module.remote;

import org.javlo.xml.NodeXML;

public class SiteMapURL {
	
	private static int decompteId=0;

	private String link;
	private String date;
	private String priority;
	private String changeFreq;
	private String id = ""+Math.abs(decompteId++);
	private int responseCode = -1;
	private long responseTime = -1;
	
	public SiteMapURL() {}

	public SiteMapURL(NodeXML node) {
		if (node.getChild("loc") != null) {
			link = node.getChild("loc").getContent();
			if (node.getChild("lastmod") != null) {
				date = node.getChild("lastmod").getContent();
			}
			if (node.getChild("changefreq") != null) {
				changeFreq = node.getChild("changefreq").getContent();
			}
			if (node.getChild("priority") != null) {
				priority = node.getChild("priority").getContent();
			}
		}
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getChangeFreq() {
		return changeFreq;
	}

	public void setChangeFreq(String changeFreq) {
		this.changeFreq = changeFreq;
	}

	public long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(long reponseTime) {
		this.responseTime = reponseTime;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

}
