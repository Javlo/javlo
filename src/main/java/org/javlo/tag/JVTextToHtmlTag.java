package org.javlo.tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import org.javlo.context.ContentContext;
import org.javlo.helper.XHTMLHelper;
import org.javlo.service.ReverseLinkService;

public class JVTextToHtmlTag extends BodyTagSupport {

	private static final long serialVersionUID = 1L;

	@Override
	public int doStartTag() throws JspException {
		return EVAL_BODY_TAG;
	}

	@Override
	public int doAfterBody() throws JspException {
		try {
			String body = getBodyContent().getString();			
			String html = body;		
			ContentContext ctx = ContentContext.getContentContext((HttpServletRequest)pageContext.getRequest(), (HttpServletResponse)pageContext.getResponse());
			ReverseLinkService rlx = ReverseLinkService.getInstance(ctx.getGlobalContext());
			html=rlx.replaceLink(ctx, null, html);
			html=XHTMLHelper.autoLink(html);
			html=XHTMLHelper.textToXHTMLWidthParagraph(html, ctx.getGlobalContext());
			html=XHTMLHelper.replaceJSTLData(ctx, html );
			getBodyContent().getEnclosingWriter().print(html);
		} catch (Exception ioe) {
			throw new JspException("Error: " + ioe.getMessage());
		}
		return EVAL_PAGE;
	}

}
