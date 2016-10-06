/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.layout;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;


/**
 * @author pvandermaesen 
 */
public class PageBreak extends AbstractVisualComponent {
	
	public static final String TYPE = "page-break";

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();	
		finalCode.append("<div class=\"pagebreak\" style=\"page-break-after:always; font-size: 0; height: 0; clear: both; display: block; margin: 0; padding;: 0;\">&nbsp;</div>");
		return finalCode.toString();
	}

	public String getType() {
		return TYPE;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getEditXHTMLCode()
	 */
	@Override
	public String getEditXHTMLCode(ContentContext ctx) {		
		return "<div style=\"border-bottom: 6px #aaaaaa solid;\"><span></span></div>";
	}
	
	public String getCSSClassName(ContentContext ctx) {
		return getStyle(ctx);
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return true;
	}
	
	@Override
	public boolean isEditOnCreate(ContentContext ctx) {
		return false;
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

}
