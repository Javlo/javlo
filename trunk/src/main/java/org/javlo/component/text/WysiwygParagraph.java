/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.text;

import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.utils.SuffixPrefix;

/**
 * @author pvandermaesen
 */
public class WysiwygParagraph extends Paragraph {

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getDebugHeader(ctx));
		finalCode.append(getSpecialInputTag());
		finalCode.append("<textarea class=\"tinymce-light\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\"");
		finalCode.append(" rows=\"" + 30 + "\">");
		finalCode.append(getValue());
		finalCode.append("</textarea>");
		finalCode.append("<script type=\"text/javascript\">loadWysiwyg('#"+getContentName()+"');</script>");
		return finalCode.toString();
	}

	@Override
	public List<SuffixPrefix> getMarkerList(ContentContext ctx) {
		return null;
	}

	@Override
	public String getType() {
		return "wysiwyg-paragraph";
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
		return reverserLinkService.replaceLink(ctx, getContent(ctx));
	}

	@Override
	public boolean isExtractable() {
		return false;
	}
	
	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String content = requestService.getParameter(getContentName(), "");		
		super.performEdit(ctx);
	}

}
