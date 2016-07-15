package org.javlo.navigation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

		ContentContext freeCtx = ctx.getFreeContentContext();		
		ContentContext realContentContext = freeCtx.getContextWithContent(currentPage);
		
		if (realContentContext != null) {
			freeCtx = realContentContext;
		}
		
		Collection<IContentVisualComponent> comps = currentPage.getContentByType(freeCtx, PageURL.TYPE);
		if (comps.size() > 0) {
			return ((PageURL) comps.iterator().next()).getValue();
		}
		
		String label;
		String pageTitle = currentPage.getForcedPageTitle(freeCtx);
		
		if (!StringHelper.isEmpty(pageTitle)) {
			label = pageTitle;
		} else {
			label = currentPage.getLabel(freeCtx);
		}
		
		if (label.startsWith("Agenda Week")) {
			label = label.substring("Agenda Week".length()).trim();
			if (label.contains(" ") && label.length() == 7) {				
				label = label.substring(3, 7)+"-W"+label.substring(0, 2);				
			}
		}
		
		if (currentPage.getUrlNumber() > 0) {
			label = label + '-' +currentPage.getUrlNumber();
		}
		String path = StringHelper.createI18NURL(label);
		//String path = StringHelper.createI18NURL(label);

		String url = path;
		MenuElement sectionPage = getSectionPage(currentPage);
		if (sectionPage != null && !sectionPage.isLikeRoot(freeCtx)) {
			url = URLHelper.mergePath(StringHelper.createI18NURL(sectionPage.getLabel(freeCtx)), url);
		}
		url = '/' + url;
				
		String baseURL = url;
		int i=1;
		while (this.addAndCheckExistURL(currentPage, url)) {
			url = baseURL+'_'+i;
			i++;
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
		if (currentPage.isLikeRoot(ctx)) {
			return "/";
		}		
		return createURLWithoutExt(ctx, currentPage) +getExtension(ctx) ;
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

}
