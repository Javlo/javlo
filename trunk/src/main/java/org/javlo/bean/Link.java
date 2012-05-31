package org.javlo.bean;

public class Link {
	
	private String url;
	private String title;
	private String label;
	
	public Link(String url, String title, String label) {
		super();
		this.url = url;
		this.title = title;
		this.label = label;
	}
	public Link(String url, String label) {
		super();
		this.url = url;
		this.title = label;
		this.label = label;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

}
