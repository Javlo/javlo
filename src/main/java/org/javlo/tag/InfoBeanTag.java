package org.javlo.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.javlo.context.ContentContext;
import org.javlo.data.InfoBean;

public class InfoBeanTag extends TagSupport {

	private static final long serialVersionUID = 1L;

	private String value = null;

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int doStartTag() throws JspException {
		try {			
			ContentContext ctx = ContentContext.getContentContext((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse());
			InfoBean.updateInfoBean(ctx);
		} catch (Exception ioe) {
			throw new JspException("Error: " + ioe.getMessage());
		}
		return SKIP_BODY;
	}

}
