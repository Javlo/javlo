/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.text;

import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.ReverseLinkService;
import org.javlo.utils.SuffixPrefix;

/**
 * @author pvandermaesen
 */
public class Description extends AbstractVisualComponent {

	public static final String TYPE = "description";

	@Override
	public String getHexColor() {
		return TEXT_COLOR;
	}

	@Override
	public List<SuffixPrefix> getMarkerList(ContentContext ctx) {
		return getItalicAndStrongLanguageMarkerList(ctx);
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		if (!isNotDisplayHTML(ctx)) {
			return "<p " + getSpecialPreviewCssClass(ctx, getType() + " " + getStyle(ctx)) + getSpecialPreviewCssId(ctx) + " >";
		} else {
			return "";
		}
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		String visible = "visible";
		String hidden = "hidden";
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			visible = i18n.getText("global.visible");
			hidden = i18n.getText("global.hidden");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[] { visible, hidden };
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "visible", "hidden" };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "position";
	}

	@Override
	public String getSufixViewXHTMLCode(ContentContext ctx) {
		if (!isNotDisplayHTML(ctx)) {
			return "</p>";
		} else {
			return "";
		}
	}

	/*
	 * @Override public boolean isUnique() { return true; }
	 */

	@Override
	public String getType() {
		return TYPE;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		String content = applyReplacement(getValue());
		if (!isNotDisplayHTML(ctx)) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
			content = reverserLinkService.replaceLink(ctx, content);
			content = XHTMLHelper.replaceJSTLData(ctx, content);
			content = XHTMLHelper.autoLink(XHTMLHelper.textToXHTML(content, globalContext));
			finalCode.append(content);
		}
		return finalCode.toString();
	}

	private boolean isNotDisplayHTML(ContentContext ctx) {
		return StringHelper.neverNull(getStyle(ctx)).equals("hidden");
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return !isNotDisplayHTML(ctx) && getValue().length() > 0;
	}

}
