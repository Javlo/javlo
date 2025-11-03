package org.javlo.navigation;

import org.javlo.context.ContentContext;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * create url based on the title of the page.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class LabelAndSectionURLCreator extends AbstractURLFactory {

	/**
	 * return the name of the first level page active. "root" if current page in
	 * root.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected MenuElement getSectionPage(MenuElement page) {
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
	
	protected boolean isWithId() {
		return false;
	}

	protected String createURLWithoutExt(ContentContext ctx, MenuElement currentPage) throws Exception {
		
		if (currentPage == null) {
			return "/";
		}

		if (!currentPage.isRealContent(ctx)) {
			MenuElement contentPage = NavigationHelper.getChildWithContent(ctx, currentPage);
			if (contentPage != null) {
				currentPage = contentPage; 
			}
		}

		ContentContext freeCtx = ctx.getFreeContentContext();
		ContentContext contextWidthTitle = freeCtx.getContextWidthTitle(currentPage);

		if (contextWidthTitle != null) {
			freeCtx = contextWidthTitle;
		}

		if (currentPage.getForcedUrl(ctx) != null) {
			return currentPage.getForcedUrl(ctx);
		}

		String label;
		String pageTitle = currentPage.getForcedPageTitle(freeCtx);

		if (!StringHelper.isEmpty(pageTitle)) {
			label = pageTitle;
		} else {
			label = currentPage.getLabel(freeCtx);
		}

		if (currentPage.getUrlNumber() > 0) {
			label = label + '-' +currentPage.getUrlNumber();
		}
		String path = StringHelper.removeSpecialChars(label.trim(), "-");
		path = StringHelper.createI18NURL(path);

		String url = path;
		MenuElement sectionPage = getSectionPage(currentPage);		
		
		if (sectionPage != null && !sectionPage.isLikeRoot(freeCtx)) {
			contextWidthTitle = freeCtx.getContextWidthTitle(sectionPage);
			if (contextWidthTitle != null) {
				freeCtx = contextWidthTitle;			
			}		
			url = URLHelper.mergePath(StringHelper.createI18NURL(StringHelper.removeSpecialChars(sectionPage.getLabel(freeCtx).trim())), url);
		}
		url = '/' + url;

		String baseURL = url;
		if (this.addAndCheckExistURL(currentPage, url)) {
			url = baseURL+'-'+currentPage.getName();
			if (this.addAndCheckExistURL(currentPage, url)) {
				url = baseURL+'-'+currentPage.getId();
				int i=1;
				while (this.addAndCheckExistURL(currentPage, url)) {
					url = baseURL+'-'+i;
					i++;
				}
			}
		}
		
		if (isWithId()) {
			url = URLHelper.mergePath(url, currentPage.getId());
		}
		
		return url;
	}
	
	protected String getExtension(ContentContext ctx) {
		return '.' + ctx.getFormat();
	}

	@Override
	public String createURL(ContentContext ctx, MenuElement currentPage) throws Exception {		
		return createURLWithoutExt(ctx, currentPage) + getExtension(ctx) ;
	}
	
	@Override
	public String createURLKey(String url) {	
		try {
			return URLDecoder.decode(url, ContentContext.CHARACTER_ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return url;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(" >>>> StringHelper.removeSpecialChars(label) " + StringHelper.removeSpecialChars("été"));
	}

}
