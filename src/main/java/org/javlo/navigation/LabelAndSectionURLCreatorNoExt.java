package org.javlo.navigation;

import org.javlo.context.ContentContext;

/**
 * create url based on the title of the page.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class LabelAndSectionURLCreatorNoExt extends LabelAndSectionURLCreator {

	@Override
	public String createURL(ContentContext ctx, MenuElement currentPage) throws Exception {		
		if (currentPage.isLikeRoot(ctx)) {
			return "/";
		}
		
		String ext = ctx.getFormat();
		if (ext.toLowerCase().equals("html")) {
			ext = "";
		} else {
			ext = '.'+ext;
		}
		
		return createURLWithoutExt(ctx, currentPage) + ext;
	}

}
