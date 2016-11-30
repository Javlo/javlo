/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import java.awt.Color;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.image.ExtendedColor;
import org.javlo.image.ImageEngine;

/**
 * @author pvandermaesen
 */
public class ColorComponent extends AbstractVisualComponent {

	public static final String TYPE = "color";

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());
		finalCode.append("<div id=\"" + getValue() + "\"><input class=\"color\" type=\"text\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\" value=\"" + getValue() + "\"/></div>");
		return finalCode.toString();
	}

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_NONE;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return true;
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}
	
	@Override
	protected String getEmptyCode(ContentContext ctx) throws Exception {
		if (getValue() != null && getValue().trim().length()>6) {
			ExtendedColor color = new ExtendedColor(ImageEngine.getTextColorOnBackground(Color.decode(getValue(ctx))));
			return ("<div style=\"background-color:"+getValue(ctx)+"; color: "+color.getHTMLCode()+"; text-align: center; padding: 3px;\">"+getValue(ctx)+"</div>");
		} else {
			return super.getEmptyCode(ctx);
		}
	}

}
