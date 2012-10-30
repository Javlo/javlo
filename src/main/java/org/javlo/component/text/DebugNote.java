/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.text;

import org.javlo.context.ContentContext;

/**
 * @author pvandermaesen 
 */
public class DebugNote extends Paragraph {
	
	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			return "<div "+getSpecialPreviewCssClass(ctx, getStyle(ctx)+" "+getType())+getSpecialPreviewCssId(ctx)+" >";
		} else {
			return "";
		}		
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			return "</div>";
		} else {
			return "";
		}
	}
	
	@Override
	public String getType() {
		return "debug-note";
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			return super.getViewXHTMLCode(ctx);
		} else {
			return "";
		}
	}
	
	@Override
	public boolean isEmpty(ContentContext ctx) {
		return ctx.getRenderMode() != ContentContext.PREVIEW_MODE;
	}
	
	@Override
	public String getHexColor() {
		return DEFAULT_COLOR;
	}

}
