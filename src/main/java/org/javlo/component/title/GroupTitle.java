/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.title;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;


/**
 * @author pvandermaesen
 */
public class GroupTitle extends AbstractVisualComponent {
	
	public static final String TYPE = "group-title";

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_HIGH;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {		
		String tag = getConfig(ctx).getProperty("tag", "h3");
		return '<'+tag+'>'+getValue()+"</"+tag+'>';
	}

	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	public int getTitleLevel(ContentContext ctx) {
		return 3;
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

}
