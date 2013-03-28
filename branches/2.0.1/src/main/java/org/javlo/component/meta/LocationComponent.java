/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;


/**
 * @author pvandermaesen
 */
public class LocationComponent extends AbstractVisualComponent {

	public static final String TYPE = "location";
	
	public String getType() {
		return TYPE;
	}
	
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());
		finalCode.append("<div id=\"" + getValue() + "\"><input style=\"width: 100%;\" type=\"text\" id=\"" + getContentName() + "\" name=\""
				+ getContentName() + "\" value=\"" + getValue() + "\"/></div>");
		return finalCode.toString();
	}
	
	public String getInputDateName() {
		return "__" + getId() + ID_SEPARATOR + "date";
	}
	
	public String getInputTimeName() {
		return "__" + getId() + ID_SEPARATOR + "time";
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "<div " + getSpecialPreviewCssClass(ctx, getStyle(ctx) + " " + getType()) + getSpecialPreviewCssId(ctx) + ">";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "<span class=\"separator\"> - </span></div>";
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_HIGH;
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
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
