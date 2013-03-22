/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.meta;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;

/**
 * @author pvandermaesen
 */
public class Keywords extends AbstractVisualComponent {

	public static final String NOT_VISIBLE = "not-visible";

	public static final String BOLD_IN_CONTENT = "bold-in-content";

	public static final String TYPE = "keywords";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { NOT_VISIBLE, BOLD_IN_CONTENT };
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			return new String[] { i18n.getText("content.keywords.not-visible"), i18n.getText("content.keywords.bold-in-content") };
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return getStyleList(ctx);
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "";
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());
		finalCode.append("<div id=\"" + getValue() + "\"><input style=\"width: 100%;\" type=\"text\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\" value=\"" + getValue() + "\"/></div>");
		return finalCode.toString();
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
		// return "<meta name=\"keywords\" content=\"" + getValue() + "\" />";
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_HIGH;
	}

	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return super.isEmpty(ctx); // this component is never not empty -> use empty parent method
	}

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

}
