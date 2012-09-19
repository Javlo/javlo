package org.javlo.navigation;

import java.net.URLEncoder;
import java.util.Collection;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.navigation.PageURL;
import org.javlo.context.ContentContext;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.StringHelper;

/**
 * create url based on the title of the page.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class TitleURLCreator extends AbstractURLFactory {

	protected boolean isWithParent() {
		return false;
	}

	protected String createURLWithoutExt(ContentContext ctx, MenuElement currentPage) throws Exception {

		if (currentPage == null) {
			return "/";
		}

		ContentContext freeCtx = ctx.getFreeContentContext();

		Collection<IContentVisualComponent> comps = currentPage.getContentByType(freeCtx, PageURL.TYPE);
		if (comps.size() > 0) {
			return ((PageURL) comps.iterator().next()).getValue();
		}
		String title = currentPage.getTitle(freeCtx);
		String path = URLEncoder.encode(StringHelper.createI18NURL(title), ContentContext.CHARACTER_ENCODING);

		String url = path;
		if (isWithParent()) {
			url = ElementaryURLHelper.mergePath(createURLWithoutExt(ctx, currentPage.getParent()), path);
		} else {
			url = '/' + url;
		}

		return url;
	}

	@Override
	public String createURL(ContentContext ctx, MenuElement currentPage) throws Exception {
		return createURLWithoutExt(ctx, currentPage) + '.' + ctx.getFormat();
	}

}
