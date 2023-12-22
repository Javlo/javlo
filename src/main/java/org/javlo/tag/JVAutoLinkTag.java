package org.javlo.tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import org.javlo.context.ContentContext;
import org.javlo.helper.XHTMLHelper;
import org.javlo.service.ReverseLinkService;


public class JVAutoLinkTag extends BodyTagSupport {

	private static final long serialVersionUID = 1L;

	@Override
	public int doStartTag() throws JspTagException {
		return EVAL_BODY_TAG;
	}

	@Override
	public int doAfterBody() throws JspTagException {
		try {
			String body = getBodyContent().getString();			
			String html = body;		
			ContentContext ctx = ContentContext.getContentContext((HttpServletRequest)pageContext.getRequest(), (HttpServletResponse)pageContext.getResponse());
			ReverseLinkService rlx = ReverseLinkService.getInstance(ctx.getGlobalContext());
			html=rlx.replaceLink(ctx, null, html);
			html=XHTMLHelper.autoLink(html);
			getBodyContent().getEnclosingWriter().print(html);
		} catch (Exception ioe) {
			throw new JspTagException("Error: " + ioe.getMessage());
		}
		return EVAL_PAGE;
	}

}
