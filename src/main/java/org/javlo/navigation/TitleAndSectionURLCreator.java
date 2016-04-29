package org.javlo.navigation;

import java.net.URLEncoder;
import java.util.Collection;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.navigation.PageURL;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

/**
 * create url based on the title of the page.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class TitleAndSectionURLCreator extends AbstractURLFactory {

	/**
	 * return the name of the first level page active. "root" if current page in
	 * root.
	 * 
	 * @return
	 * @throws Exception
	 */
	private MenuElement getSectionPage(MenuElement page) {		
		if (page == null) {
			return null;
		}
		if (page.getParent() == null || page.getParent().getParent() == null) {
			return null;
		} else {
			while (page.getParent().getParent() != null) {
				page = page.getParent();
			}
		}
		return page;
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
		String title = currentPage.getLocalTitle(freeCtx);
		if (currentPage.getUrlNumber() > 0) {
			title = title + '-' +currentPage.getUrlNumber();
		}
		String path = URLEncoder.encode(StringHelper.createI18NURL(title), ContentContext.CHARACTER_ENCODING);

		String url = path;
		MenuElement sectionPage = getSectionPage(currentPage);
		if (sectionPage != null) {
			url = URLHelper.mergePath(StringHelper.createI18NURL(sectionPage.getTitle(freeCtx)), url);
		}
		url = '/' + url;
				
		String baseURL = url;
		int i=1;
		while (this.addAndCheckExistURL(currentPage, url)) {
			url = baseURL+'_'+i;
			i++;
		}

		return url;
	}

	@Override
	public String createURL(ContentContext ctx, MenuElement currentPage) throws Exception {
		return createURLWithoutExt(ctx, currentPage) + '.' + ctx.getFormat();
	}

}
