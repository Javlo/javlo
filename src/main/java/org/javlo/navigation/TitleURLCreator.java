package org.javlo.navigation;

import org.apache.commons.lang3.StringEscapeUtils;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.navigation.PageURL;
import org.javlo.context.ContentContext;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.StringHelper;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * create url based on the title of the page.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class TitleURLCreator extends AbstractURLFactory {

	private static Logger logger = Logger.getLogger(TitleURLCreator.class.getName());

	protected boolean isWithParent() {
		return false;
	}
	
	protected boolean isRemoveAccent() {
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

		String title = StringHelper.removeCR(currentPage.getForcedPageTitle(ctx));
		String newTitle = StringHelper.removeCR(currentPage.getFullLabel(freeCtx));
		if (title.length() < 2 && newTitle.length() > 2) {
			title = newTitle;
		} else if (newTitle.length() < title.length()) {
			title = newTitle;
		}
		if (title.isEmpty()) {
			title = StringHelper.removeCR(currentPage.getPageTitle(freeCtx));
		}

		title = title.trim();
		title =  StringEscapeUtils.unescapeHtml4(title);
		
		if (currentPage.getUrlNumber() > 0) {
			title = title + '-' +currentPage.getUrlNumber();
		}
		
		if (isRemoveAccent()) {
			title = StringHelper.createASCIIString(title);
		}
		
		String path = URLEncoder.encode(StringHelper.createI18NURL(StringHelper.removeSpecialChars(title)), ContentContext.CHARACTER_ENCODING);

		String url = path;
		if (isWithParent()) {
			url = ElementaryURLHelper.mergePath(createURLWithoutExt(ctx, currentPage.getParent()), path);
		} else {
			url = '/' + url;
		}
		
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
			logger.warning("url : '"+baseURL+"' found on page : "+this.getExistingURLId(url)+" | final URL="+url);
		}
		return url;
	}

	@Override
	public String createURL(ContentContext ctx, MenuElement currentPage) throws Exception {
		return createURLWithoutExt(ctx, currentPage) + '.' + ctx.getFormat();
	}

}
