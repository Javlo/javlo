package org.javlo.navigation;

import org.javlo.context.ContentContext;

public class PageIDURLFactory implements IURLFactory {
	

	@Override
	public  String createURL(ContentContext ctx, MenuElement currentPage) throws Exception {
		if (currentPage.getParent() == null) {
			return "/";
		}
		return '/'+currentPage.getId();
	}
	
}
