/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.css.CssColor;
import org.javlo.image.ExtendedColor;
import org.javlo.image.ImageEngine;

import java.awt.*;

/**
 * @author pvandermaesen
 */
public class ColorComponent extends AbstractVisualComponent {

	public static final String TYPE = "color";

	public static final String BACKGROUND_COLOR = "background";

	private String[] TYPES = new String[]  {"font", BACKGROUND_COLOR};

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());

		if ( ctx.getGlobalContext().getTemplateData().getColorList() != null &&  ctx.getGlobalContext().getTemplateData().getColorList().length > 0) {
			finalCode.append("<div class=\"color-list d-flex\">");
			for (CssColor c : ctx.getGlobalContext().getTemplateData().getColorList()) {
				String js = "document.getElementById('"+getContentName()+"').value='"+c+"';";
				if (c!=null) {
					finalCode.append("<button class=\"btn btn-primary mb-3\" style=\"background-color: " + c + "\" onclick=\"" + js + "\">" + c + "</button>");
				}
			}
			finalCode.append("</div>");
		}

		finalCode.append("<div id=\"" + getValue() + "\"><input class=\"color\" type=\"text\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\" value=\"" + getValue() + "\"/></div>");
		return finalCode.toString();
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return TYPES;
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
	public boolean isDispayEmptyXHTMLCode(ContentContext ctx) throws Exception {
		return true;
	}

	@Override
	protected String renderViewXHTMLCode(ContentContext ctx) throws Exception {
		if (getValue() != null && getValue().trim().length()>6 && ctx.getRenderMode() == ContentContext.PREVIEW_MODE && EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession()).isPreviewEditionMode() && !ctx.isPreviewOnly()) {
			ExtendedColor color = new ExtendedColor(ImageEngine.getTextColorOnBackground(Color.decode(getValue(ctx))));
			return ("<div style=\"background-color:"+getValue(ctx)+"; color: "+color.getHTMLCode()+"; text-align: center; padding: 3px; width: 180px; margin: 0 auto;\">"+getType()+" : "+getValue(ctx)+"</div>");
		} else {
			return super.renderViewXHTMLCode(ctx);
		}
	}
	
	@Override
	public String getFontAwesome() {
		return "paint-brush";
	}

}
