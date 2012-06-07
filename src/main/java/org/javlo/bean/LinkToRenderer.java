package org.javlo.bean;

/**
 * user for create a link to a renderer (jsp file).
 * @author Patrick Vandermaesen
 *
 */
public class LinkToRenderer extends Link {
	
	protected String renderer;
	protected String name;
	
	public LinkToRenderer(String label, String name, String renderer) {
		super("?webaction=changeRenderer&page="+name, label, label);
		this.renderer = renderer;
		this.name = name;
	}
	public String getRenderer() {
		return renderer;
	}
	public void setRenderer(String renderer) {
		this.renderer = renderer;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	

}
