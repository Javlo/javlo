package org.javlo.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;

public class JVUrlTag extends TagSupport {

	private String value = null;

	public void setValue(String value) {
		this.value = value;
	}

	public int doStartTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();

			ContentContext ctx;

			ctx = ContentContext.getContentContext((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse());
			out.print(URLHelper.createStaticURL(ctx, value));

		} catch (Exception ioe) {
			throw new JspException("Error: " + ioe.getMessage());
		}
		return SKIP_BODY;
	}

}
