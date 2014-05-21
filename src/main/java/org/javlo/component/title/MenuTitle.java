/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.title;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;


/**
 * @author pvandermaesen
 */
public class MenuTitle extends AbstractVisualComponent {
	
	public static final String TYPE = "menu-title";

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
	public String getTextLabel() {
		return getValue();
	}
	
	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	/*public String getHelpURL(String lang) {
		return getBaseHelpURL()+"/view/"+lang+"/components/title.html";
	}*/
	
	@Override
	public boolean isEmpty(ContentContext ctx) {
		return true; /* page with only a title is never pertinent */
	}
	
	@Override
	public String getEmptyXHTMLCode(ContentContext ctx) throws Exception {
		String emptyCode = super.getEmptyXHTMLCode(ctx);
		return emptyCode.replace("]", ':'+getValue()+']');
	}

}
