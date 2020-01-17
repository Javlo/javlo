package org.javlo.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;

public class JVUrlTag extends TagSupport {

	private static final long serialVersionUID = 1L;

	private String value = null;
	private String webaction = null;
	private String name = null;

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int doStartTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();
			ContentContext ctx;
			ctx = ContentContext.getContentContext((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse());
			String url;
			if (webaction == null) {
				url = URLHelper.createStaticURL(ctx, value);
			} else {
				url = URLHelper.createActionURL(ctx, webaction, value);
			}
			if (getName() == null) {
				out.print(url);
			} else {
				ctx.getRequest().setAttribute(getName(), url);
			}
		} catch (Exception ioe) {
			throw new JspException("Error: " + ioe.getMessage());
		}
		return SKIP_BODY;
	}

	public void setWebaction(String webaction) {
		this.webaction = webaction;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
