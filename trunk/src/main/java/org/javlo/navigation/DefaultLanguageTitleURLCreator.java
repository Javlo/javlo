package org.javlo.navigation;

import java.net.URLEncoder;
import java.util.Collection;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.navigation.PageURL;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.StringHelper;

public class DefaultLanguageTitleURLCreator extends AbstractURLFactory {

	protected boolean isWithParent() {
		return true;
	}

	@Override
	public String createURL(ContentContext ctx, MenuElement currentPage) throws Exception {
		if (currentPage.getParent() == null) {
			return "/";
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentContext defaultLgCtx = new ContentContext(ctx);
		defaultLgCtx.setLanguage(globalContext.getDefaultLanguage());
		defaultLgCtx.setRequestContentLanguage(globalContext.getDefaultLanguage());

		Collection<IContentVisualComponent> comps = currentPage.getContentByType(defaultLgCtx, PageURL.TYPE);
		if (comps.size() > 0) {
			return ((PageURL) comps.iterator().next()).getValue();
		}
		String title = currentPage.getTitle(defaultLgCtx);
		String path = URLEncoder.encode(StringHelper.createI18NURL(title), ContentContext.CHARACTER_ENCODING);

		String url = path;
		if (isWithParent()) {
			url = ElementaryURLHelper.mergePath(createURL(defaultLgCtx, currentPage.getParent()), path);
		} else {
			url = '/' + url;
		}

		return url + '.' + ctx.getFormat();
	}

}
