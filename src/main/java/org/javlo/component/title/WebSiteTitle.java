/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.title;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;


/**
 * @author pvandermaesen
 */
public class WebSiteTitle extends AbstractVisualComponent {
	
	public static final String TYPE = "web-site-title";

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_HIGH;
	}
	
	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getHexColor() {
		return META_COLOR;
	}
	
	@Override
	public boolean isUnique() {
		return true;
	}
	
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_ADMIN);
	}



}
