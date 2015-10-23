package org.javlo.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;

public class JVImageTransformTag extends TagSupport {

	private static final long serialVersionUID = 1L;

	private String var = null;
	
	private String filter = null;
	
	private String src = null;

	@Override
	public int doStartTag() throws JspException {
		try {			
			ContentContext ctx;
			ctx = ContentContext.getContentContext((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse());
			String imageURL = URLHelper.createTransformLocalTemplateURL(ctx, ctx.getCurrentTemplate().getId(), getFilter(), getSrc());
			ctx.getRequest().setAttribute(getVar(), imageURL);			
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

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

}
