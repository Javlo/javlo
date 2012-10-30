/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;

/**
 * @author pvandermaesen
 */
public class Category extends AbstractVisualComponent {

	public static final String TYPE = "category-title";

	protected static final String YES = "yes";

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());
		finalCode.append("<div id=\"" + getValue() + "\"><input style=\"width: 100%;\" type=\"text\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\" value=\"" + getValue() + "\"/></div>");
		return finalCode.toString();
	}

	@Override
	public String getHexColor() {
		return TEXT_COLOR;
	}

	public String getInputDateName() {
		return "__" + getId() + ID_SEPARATOR + "date";
	}

	public String getInputTimeName() {
		return "__" + getId() + ID_SEPARATOR + "time";
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		String style = "";
		if (getStyle(ctx) != null) {
			style = getStyle(ctx) + " ";
		}
		return "<div " + getSpecialPreviewCssClass(ctx, style + getType()) + getSpecialPreviewCssId(ctx) + ">";
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_HIGH;
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return new String[] { i18nAccess.getText("global.no", "no"), i18nAccess.getText("global.yes", "yes") };
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[0];
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "no", YES };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return i18nAccess.getText("content.category.translated", "translated");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "</div>";
	}

	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (YES.equals(getStyle(ctx))) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			ContentContext lgCtx = new ContentContext(ctx);
			lgCtx.setLanguage(ctx.getRequestContentLanguage());
			i18nAccess.changeViewLanguage(lgCtx);
			String txt = i18nAccess.getViewText("category." + getValue().toLowerCase().replaceAll(" ", ""), (String)null);
			i18nAccess.changeViewLanguage(ctx);
			if (txt == null) {
				txt = i18nAccess.getViewText("category." + getValue().toLowerCase().replaceAll(" ", ""), getValue());
			}			
			return txt;
		} else {
			return super.getViewXHTMLCode(ctx);
		}
	}

	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return super.isEmpty(ctx); // this component is never not empty -> use empty parent method
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return true;
	}
}
