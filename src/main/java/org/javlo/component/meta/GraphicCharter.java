/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import java.util.Collections;
import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;

/**
 * @author pvandermaesen
 */
public class GraphicCharter extends AbstractVisualComponent {

	public static final String TYPE = "graphic-charter";

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_NONE;
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return new String[] { i18nAccess.getText("global.no", "no"), i18nAccess.getText("global.yes", "yes"), "hidden" };
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[0];
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		List<String> fonts = ctx.getCurrentTemplate().getWebFonts(ctx.getGlobalContext());
		Collections.sort(fonts);
		ctx.getRequest().setAttribute("currentContext", ctx.getGlobalContext());
		ctx.getRequest().setAttribute("fonts", fonts);
		ctx.getRequest().setAttribute("fontsMap", ctx.getCurrentTemplate().getFontReference(ctx.getGlobalContext()));
		String jsp = "/modules/admin/jsp/graphic_charter.jsp";
		return "<p>" + XHTMLHelper.autoLink(getValue()) + "</p>" + ServletHelper.executeJSP(ctx, jsp);
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_ADMIN);
	}

	@Override
	public String getFontAwesome() {
		return "adjust";
	}

}
