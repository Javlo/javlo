package org.javlo.bean;

import java.io.Serializable;

/**
 * represent a link
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class Link implements Serializable {

	protected String url;
	protected String title;
	protected String label;
	protected String style;

	public Link(String url, String title, String label) {
		super();
		this.url = url;
		this.title = title;
		this.label = label;
	}

	public Link(String url, String label) {
		super();
		this.url = url;
		this.label = label;
		this.title = label;
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
	
	public String getStyle() {
		return style;
	}
	
	public void setStyle(String style) {
		this.style = style;
	}

}
