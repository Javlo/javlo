/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.title;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;

/**
 * @author pvandermaesen
 */
public class LinkLabel extends AbstractVisualComponent {

	public static final String TYPE = "link-label";

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_HIGH;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && EditContext.getInstance(GlobalContext.getInstance(ctx.getRequest()), ctx.getRequest().getSession()).isEditPreview()) {
			return super.getPrefixViewXHTMLCode(ctx);
		} else {
			return "";
		}
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && EditContext.getInstance(GlobalContext.getInstance(ctx.getRequest()), ctx.getRequest().getSession()).isEditPreview()) {
			return super.getSuffixViewXHTMLCode(ctx);
		} else {
			return "";
		}
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return true;
	}

	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

}
