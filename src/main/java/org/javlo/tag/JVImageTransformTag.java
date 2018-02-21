package org.javlo.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class JVImageTransformTag extends TagSupport {

	private static final long serialVersionUID = 1L;

	private String var = null;
	
	private String filter = null;
	
	private String src = null;
	
	private static final String RESOURCES_PATH = "/resource/";

	@Override
	public int doStartTag() throws JspException {
		try {			
			ContentContext ctx;
			ctx = ContentContext.getContentContext((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse());
			String src = getSrc();
			boolean resource = false;
			if (StringHelper.isURL(src)) {				
				if (src.contains(RESOURCES_PATH)) {
					resource = true;
					src = src.substring(src.indexOf(RESOURCES_PATH)+RESOURCES_PATH.length());
				} else {
					ctx.getRequest().setAttribute(getVar(), src);
					src = null;
				}
			}
			if (src.contains(RESOURCES_PATH)) {
				resource = true;
				src = src.substring(src.indexOf(RESOURCES_PATH)+RESOURCES_PATH.length());
			} 
			if (src != null) {
				String imageURL;
				if (resource) {
					imageURL = URLHelper.createTransformURL(ctx, src, getFilter());
				} else {
					imageURL = URLHelper.createTransformLocalTemplateURL(ctx, ctx.getCurrentTemplate().getId(), getFilter(), src);
				}
				ctx.getRequest().setAttribute(getVar(), imageURL);			
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
