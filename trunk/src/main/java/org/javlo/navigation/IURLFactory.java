package org.javlo.navigation;

import org.javlo.context.ContentContext;

public interface IURLFactory {
	
	public String createURL(ContentContext ctx, MenuElement page) throws Exception;

}
