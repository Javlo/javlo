/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.list;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.service.ReverseLinkService;


/**
 * @author pvandermaesen
 */
public class List extends AbstractVisualComponent {
	
	public static final String TYPE = "list";

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
		String value = textToXHTML(getValue());
		value = reverserLinkService.replaceLink(ctx, value);
		return value;
	}
	
	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "<li "+getSpecialPreviewCssClass(ctx, getStyle(ctx))+getSpecialPreviewCssId(ctx)+">";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "</li>";
	}

	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getFirstPrefix(ContentContext ctx) {
		return "<ul>";
	}
	
	@Override
	public String getLastSufix(ContentContext ctx) {
		return "</ul>";
	}
}
