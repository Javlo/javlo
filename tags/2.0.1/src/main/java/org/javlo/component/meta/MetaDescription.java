/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;



/**
 * @author pvandermaesen 
 */
public class MetaDescription extends AbstractVisualComponent {
	
	public static final String TYPE = "meta-description";
	
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
	public boolean isUnique() {
		return true;
	}
	
	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_HIGH;
	}

}
