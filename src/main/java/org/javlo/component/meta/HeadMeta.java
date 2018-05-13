/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

/**
 * @author pvandermaesen
 */
public class HeadMeta extends AbstractVisualComponent {

	public static final String TYPE = "meta-head";
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
	}

	@Override
	public String getFontAwesome() {
		return "info";
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

}
