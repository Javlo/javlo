/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.title;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
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
	
	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "";
	}
	
	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "";
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
	public boolean isEmpty(ContentContext ctx) {
		return true; /* page with only a title is never pertinent */
	}
	
	@Override
	public boolean isUnique() {
		return true;
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return IContentVisualComponent.COMPLEXITY_ADMIN;
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}


}
