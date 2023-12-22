package org.javlo.tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;


public class JVPageUrlTag extends TagSupport {

	private static final long serialVersionUID = 1L;

	private String name = null;
	
	private String var = null;
	
	private String params = null;
	
	private boolean view = false;

	public void setName(String name) {
		this.name = name;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}
	
	@Override
	public int doStartTag() throws JspException {
		try {
			
			ContentContext ctx;
			ctx = ContentContext.getContentContext((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse());
			if (view) {
				ctx.setRenderMode(ContentContext.VIEW_MODE);
			}
			String url = URLHelper.createURLFromPageName(ctx, name);
			if (params != null) {
				if (url.contains("?")) {
					url = url + '&' + params;
				} else {
					url = url + '?' + params;
				}
			}
			if (var == null) {
				JspWriter out = pageContext.getOut();
				out.print(url);
			} else {
				pageContext.getRequest().setAttribute(var, url);
			}
		} catch (Exception ioe) {
			throw new JspException("Error: " + ioe.getMessage());
		}
		return SKIP_BODY;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public boolean isView() {
		return view;
	}

	public void setView(boolean view) {
		this.view = view;
	}

}
