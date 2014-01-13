package org.javlo.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.javlo.helper.XHTMLHelper;

public class JVAutoLinkTag extends BodyTagSupport {

	private static final long serialVersionUID = 1L;

	@Override
	public int doStartTag() throws JspException {
		return EVAL_BODY_TAG;
	}

	@Override
	public int doAfterBody() throws JspException {
		try {
			String body = getBodyContent().getString();
			getBodyContent().getEnclosingWriter().print(XHTMLHelper.autoLink(body));
		} catch (Exception ioe) {
			throw new JspException("Error: " + ioe.getMessage());
		}
		return EVAL_PAGE;
	}

}
