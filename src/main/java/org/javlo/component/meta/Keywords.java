/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.meta;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

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
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_ADMIN);
	}

}
