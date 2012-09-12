package org.javlo.navigation;

import java.util.Collection;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.navigation.PageURL;
import org.javlo.context.ContentContext;

public class NameURLCreator extends AbstractURLFactory {

	public static final NameURLCreator instance = new NameURLCreator();

	@Override
	public String createURL(ContentContext ctx, MenuElement currentPage) throws Exception {
		if (currentPage.getParent() == null) {
			return "/";
		}
		Collection<IContentVisualComponent> comps = currentPage.getContentByType(ctx, PageURL.TYPE);
		if (comps.size() > 0) {
			return ((PageURL) comps.iterator().next()).getValue();
		}

		return '/' + currentPage.getParent().getName() + '/' + currentPage.getName();
	}
}
