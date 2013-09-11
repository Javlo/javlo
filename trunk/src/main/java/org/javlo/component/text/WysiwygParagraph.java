/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.text;

import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.LoremIpsumGenerator;
import org.javlo.service.ReverseLinkService;
import org.javlo.utils.SuffixPrefix;

/**
 * @author pvandermaesen
 */
public class WysiwygParagraph extends AbstractVisualComponent {

	public static final String TYPE = "wysiwyg-paragraph";
	
	private String getEditorComplexity(ContentContext ctx) {
		return getConfig(ctx).getProperty("complexity", "light");
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getDebugHeader(ctx));
		finalCode.append(getSpecialInputTag());
		finalCode.append("<textarea class=\"tinymce-light\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\"");
		finalCode.append(" rows=\"" + 30 + "\">");
		finalCode.append(getValue());
		finalCode.append("</textarea>");
		finalCode.append("<script type=\"text/javascript\">jQuery(document).ready(loadWysiwyg('#" + getContentName() + "','"+getEditorComplexity(ctx)+"'));</script>");
		return finalCode.toString();
	}

	@Override
	public List<SuffixPrefix> getMarkerList(ContentContext ctx) {
		return null;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);		
		return reverserLinkService.replaceLink(ctx, getValue());
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return getValue().trim().length() > 0;
	}

	@Override
	public boolean initContent(ContentContext ctx) {
		setValue(LoremIpsumGenerator.getParagraph(120, true, true));
		setModify();
		return true;
	}
	
	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		if (ctx.isAsViewMode() && getValue().contains("</p>")) {
			return "";
		} else {
			return super.getPrefixViewXHTMLCode(ctx);
		}
	}
	
	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		if (ctx.isAsViewMode() && getValue().contains("</p>")) {
			return "";
		} else {
			return super.getSuffixViewXHTMLCode(ctx);
		}
	}
	
	protected String getTag(ContentContext ctx) {
		String defaultTag = "p";
		if (getValue().contains("</p>")) {
			defaultTag = "div";
		}
		return getConfig(ctx).getProperty("tag", defaultTag);
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {	
		return true;
	}

}
