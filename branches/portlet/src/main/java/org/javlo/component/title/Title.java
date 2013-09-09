/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.title;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.LoremIpsumGenerator;
import org.javlo.service.ReverseLinkService;

/**
 * @author pvandermaesen
 */
public class Title extends AbstractVisualComponent {

	public static final String TYPE = "title";

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_HIGH;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer res = new StringBuffer();
		res.append("<h1 " + getSpecialPreviewCssClass(ctx, getStyle(ctx)) + getSpecialPreviewCssId(ctx) + "><span>");

		String value = getValue();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
		value = reverserLinkService.replaceLink(ctx, value);

		res.append(value);
		res.append("</span></h1>");
		return res.toString();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isLabel() {
		return true;
	}

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	public String getHelpURI(ContentContext ctx) {
		return "/components/title.html";
	}

	@Override
	public int getTitleLevel(ContentContext ctx) {
		return 1;
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return true; /* page with only a title is never pertinent */
	}

	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return super.isEmpty(ctx); // this component is never not empty -> use empty parent method
	}

	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	public boolean initContent(ContentContext ctx) {
		setValue(LoremIpsumGenerator.getParagraph(4, true, true));
		setModify();
		return true;
	}

}
