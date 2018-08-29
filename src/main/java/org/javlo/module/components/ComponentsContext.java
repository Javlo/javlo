package org.javlo.module.components;

import org.javlo.context.ContentContext;

public class ComponentsContext {
	
	private static final String KEY = "componentsContext";
	
	private String html;
	
	private String css;
	
	public static ComponentsContext getInstance(ContentContext ctx) {
		ComponentsContext outContext = (ComponentsContext)ctx.getRequest().getSession().getAttribute(KEY);
		if (outContext == null) {
			outContext = new ComponentsContext();
			ctx.getRequest().getSession().setAttribute(KEY, outContext);
		}
		return outContext;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	
}
