package org.javlo.bean;

import java.util.List;

public class Link {
	
	protected String url;
	protected String title;
	protected String label;
	protected Link parent;
	protected List<? extends Link> children;

	public Link(String url, String title, String label, Link parent, List<? extends Link> children) {
		super();
		this.url = url;
		this.title = title;
		this.label = label;
		this.parent = parent;
		this.children = children;
	}
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
	public Link getParent() {
		return parent;
	}
	public void setParent(Link parent) {
		this.parent = parent;
	}	
	public List<? extends Link> getChildren() {
		return children;
	}
	public void setChildren(List<? extends Link> children) {
		this.children = children;
	}	
}
