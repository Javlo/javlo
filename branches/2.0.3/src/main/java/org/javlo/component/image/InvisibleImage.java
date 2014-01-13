/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.image;

import org.javlo.context.ContentContext;

/**
 * @author pvandermaesen
 */
public class InvisibleImage extends Image {
	
	@Override
	public String getType() {
		return "invisible-image";
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}
}
