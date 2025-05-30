package org.javlo.navigation;

import org.apache.commons.lang3.StringEscapeUtils;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.navigation.PageURL;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

import java.util.Collection;

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
	
	protected boolean isTransliteration() {
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
		if (title.equals(currentPage.getName())) {
			Collection<String> subtitles = currentPage.getSubTitles(freeCtx,2);
			if (subtitles.size()>0) {
				title = StringHelper.collectionToString(currentPage.getSubTitles(freeCtx,2), " / ");
			}
		}
		title =  StringEscapeUtils.unescapeHtml4(title);
		if (currentPage.getUrlNumber() > 0) {			
			title = title + '-' +currentPage.getUrlNumber();
		}
		if (StringHelper.isEmpty(title)) {
			title = currentPage.getName();
		}

		int maxSize = ctx.getGlobalContext().getStaticConfig().getMaxURLSize()+2; // +2 = indice of the page (sp. _1)
		if  (maxSize > 2 && title.length() >  maxSize ) {
			title = title.substring(0, maxSize);			
		}
		
		if (isTransliteration()) {
			title = StringHelper.removeSpecialChars(title);
		}
		title = cleanString(title);

		String path = StringHelper.createI18NURL(title);

		String url = path;
		MenuElement sectionPage = getSectionPage(currentPage);
		if (sectionPage != null) {
			String subtitle = sectionPage.getTitle(freeCtx);
			subtitle =  StringEscapeUtils.unescapeHtml4(subtitle);
			subtitle = StringHelper.createI18NURL(subtitle);
			url = URLHelper.mergePath(subtitle, url);
		}
		url = '/' + url;
				
		String baseURL = url;
		
		if (this.addAndCheckExistURL(currentPage, url)) {
			url = baseURL+'_'+currentPage.getName();
			if (this.addAndCheckExistURL(currentPage, url)) {
				url = baseURL+'_'+currentPage.getId();
				int i=1;
				while (this.addAndCheckExistURL(currentPage, url)) {
					url = baseURL+'_'+i;
					i++;
				}
			}
		}

		return url;
	}

	@Override
	public String createURL(ContentContext ctx, MenuElement currentPage) throws Exception {		
		if (currentPage.isLikeRoot(ctx)) {
			return "/";
		}
		
		return createURLWithoutExt(ctx, currentPage) + '.' + ctx.getFormat();
	}

}
