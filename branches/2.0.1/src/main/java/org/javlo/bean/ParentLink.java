package org.javlo.bean;

import java.util.List;

/**
 * represent a link with children.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class ParentLink extends Link {

	protected ParentLink parent;
	protected List<? extends ParentLink> children;

	public ParentLink(String url, String title, String label) {
		super(url, title, label);
	}

	public ParentLink(String url, String title) {
		super(url, title);
	}

	public ParentLink(String url, String title, String label, ParentLink parent, List<? extends ParentLink> children) {
		super(url, title, label);
		this.parent = parent;
		this.children = children;
	}

	public ParentLink getParent() {
		return parent;
	}

	public void setParent(ParentLink parent) {
		this.parent = parent;
	}

	public List<? extends ParentLink> getChildren() {
		return children;
	}

	public void setChildren(List<? extends ParentLink> children) {
		this.children = children;
	}
}
