package org.javlo.navigation;

import java.net.URLEncoder;
import java.util.Collection;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.navigation.PageURL;
import org.javlo.context.ContentContext;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.StringHelper;

public class TitleURLCreator extends AbstractURLFactory {

	protected boolean isWithParent() {
		return false;
	}

	@Override
	public String createURL(ContentContext ctx, MenuElement currentPage) throws Exception {
		if (currentPage.getParent() == null) {
			return "/";
		}
		Collection<IContentVisualComponent> comps = currentPage.getContentByType(ctx, PageURL.TYPE);
		if (comps.size() > 0) {
			return ((PageURL) comps.iterator().next()).getValue();
		}
		String title = currentPage.getTitle(ctx);
		String path = URLEncoder.encode(StringHelper.createI18NURL(title), ContentContext.CHARACTER_ENCODING);

		String url = path;
		if (isWithParent()) {
			url = ElementaryURLHelper.mergePath(createURL(ctx, currentPage.getParent()), path);
		} else {
			url = '/' + url;
		}

		return url + '.' + ctx.getFormat();
	}

}
