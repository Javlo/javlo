package org.javlo.navigation;

import org.javlo.context.ContentContext;

public class LabelAndSubSubSectionURLCreator extends LabelAndSectionURLCreator {

	/**
	 * return the name of the first level page active. "root" if current page in
	 * root.
	 * 
	 * @return
	 * @throws Exception
	 */
	@Override
	protected MenuElement getSectionPage(MenuElement page) {
		if (page == null) {
			return null;
		}
		if (page.getParent() == null || page.getParent().getParent() == null || page.getParent().getParent().getParent() == null || page.getParent().getParent().getParent().getParent() == null) {
			return null;
		} else {
			while (page.getParent().getParent().getParent().getParent() != null) {
				page = page.getParent();
			}
		}
		return page;
	}

	@Override
	protected String getExtension(ContentContext ctx) {
		if (ctx.getFormat().equalsIgnoreCase("html")) {
			return "";
		} else {
			return super.getExtension(ctx);
		}
	}
}
