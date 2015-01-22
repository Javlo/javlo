package org.javlo.navigation;

import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;

public class NoExtURLPathTitleCreator extends NoExtURLCreator {

	private String getParentPath(ContentContext ctx, MenuElement page) throws Exception {
		ContentContext lgCtx = ctx;
		if (fromDefaultLanguage()) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			lgCtx = ctx.getFreeContentContext();
			lgCtx.setLanguage(globalContext.getDefaultLanguage());
			lgCtx.setRequestContentLanguage(globalContext.getDefaultLanguage());
		}		
		if (page != null && page.getParent() != null) {
			String label = page.getLocalTitle(lgCtx);
			return getParentPath(ctx, page.getParent()) + '/' + StringHelper.createI18NURL(label);
		} else {
			return "";
		}
	}

	protected boolean fromDefaultLanguage() {
		return false;
	}

	@Override
	public String createURL(ContentContext ctx, MenuElement currentPage) throws Exception {
		ContentContext lgCtx = ctx;
		if (fromDefaultLanguage()) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			lgCtx = ctx.getFreeContentContext();
			lgCtx.setLanguage(globalContext.getDefaultLanguage());
			lgCtx.setRequestContentLanguage(globalContext.getDefaultLanguage());
		}
		String label = currentPage.getLocalTitle(lgCtx);
		String sTitle = currentPage.getSubTitle(lgCtx);
		if (sTitle != null && sTitle.trim().length() > 0) {
			label = label + '-' + sTitle;
		}
		if (currentPage.getUrlNumber() > 0) {
			label = label + '-' + currentPage.getUrlNumber();
		}
		String path = URLEncoder.encode(StringHelper.createI18NURL(label), ContentContext.CHARACTER_ENCODING);

		return getParentPath(ctx, currentPage.getParent()) + '/' + ctx.getFormat() + '/' + path;
	}

	@Override
	public String getFormat(String url) {
		String[] pathItems = StringUtils.split(url, "/");
		String format = "html";
		if (pathItems.length >= 4) {
			format = pathItems[pathItems.length - 2];
		}
		if (format.length() > 4 || (!format.equals("html") && !format.equals("pdf") && !format.equals("png") && !format.equals("jpg") && !format.equals("cxml"))) {
			format = "html";
		}
		return format;
	}

	@Override
	public String createURLKey(String url) {
		if (url.length() < 2) {
			return url;
		}
		String workURL = url.substring(0, url.length() - 1); // remove last char
		int afterExtPos = workURL.lastIndexOf('/');
		if (afterExtPos > 0) {
			int beofreExtPos = workURL.substring(0, afterExtPos - 1).lastIndexOf('/');
			if (afterExtPos < 0 || beofreExtPos < 0) {
				return url;
			} else {
				return url.substring(0, beofreExtPos) + url.substring(afterExtPos, url.length());
			}
		} else {
			return url;
		}
	}

}
