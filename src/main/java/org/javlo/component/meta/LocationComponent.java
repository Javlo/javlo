/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.XHTMLHelper;


/**
 * @author pvandermaesen
 */
public class LocationComponent extends AbstractVisualComponent {

	public static final String TYPE = "location";
	
	public String getType() {
		return TYPE;
	}
	
	public String getInputDateName() {
		return "__" + getId() + ID_SEPARATOR + "date";
	}
	
	public String getInputTimeName() {
		return "__" + getId() + ID_SEPARATOR + "time";
	}
	
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return XHTMLHelper.textToXHTML(getValue());
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_HIGH;
	}
	
	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return true;
	}
	
	@Override
	public String getHexColor() {
		return META_COLOR;
	}	
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {	
		return true;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
	}
	
}
