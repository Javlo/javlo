package org.javlo.navigation;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

/**
 * create url based on the page title (content of the html &lt;title&gt; tag) :
 * forced page title first, then title (h1), then page name.
 *
 * @author Patrick Vandermaesen
 *
 */
public class PageTitleURLCreator extends TitleURLCreator {

	@Override
	protected String getURLTitle(ContentContext ctx, MenuElement currentPage) throws Exception {
		String title = StringHelper.removeCR(StringHelper.neverNull(currentPage.getForcedPageTitle(ctx))).trim();
		if (title.isEmpty()) {
			title = StringHelper.removeCR(StringHelper.neverNull(currentPage.getTitle(ctx.getFreeContentContext()))).trim();
		}
		if (title.isEmpty()) {
			title = currentPage.getName();
		}
		return title;
	}

}
