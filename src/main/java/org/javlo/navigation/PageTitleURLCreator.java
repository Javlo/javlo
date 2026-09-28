package org.javlo.navigation;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

/**
 * create url based on the page title (content of the html &lt;title&gt; tag) :
 * forced page title first, then title (h1), then label.
 *
 * @author Patrick Vandermaesen
 *
 */
public class PageTitleURLCreator extends TitleURLCreator {

	@Override
	protected String getURLTitle(ContentContext ctx, MenuElement currentPage) throws Exception {
		return StringHelper.removeCR(currentPage.getPageTitle(ctx));
	}

}
