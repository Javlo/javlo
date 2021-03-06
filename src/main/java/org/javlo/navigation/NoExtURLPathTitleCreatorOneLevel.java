package org.javlo.navigation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.lang3.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class NoExtURLPathTitleCreatorOneLevel extends NoExtURLCreator {

	private String getParentPath(ContentContext ctx, MenuElement page) throws Exception {
		if (page == null || page.getParent() == null) {
			return "";
		} else {
			while (page.getParent().getParent() != null) {
				page = page.getParent();
			}
			ContentContext lgCtx = ctx;
			if (fromDefaultLanguage()) {
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
				lgCtx = ctx.getFreeContentContext();
				lgCtx.setLanguage(globalContext.getDefaultLanguage());
				lgCtx.setRequestContentLanguage(globalContext.getDefaultLanguage());
			}
			if (page.getParent() != null) {
				String label = page.getLocalTitle(lgCtx);
				return StringHelper.createI18NURL(label);
			} else {
				return "";
			}
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
		if (currentPage.getUrlNumber() > 0) {
			label = label + '-' + currentPage.getUrlNumber();
		}
		label = cleanString(label);
		String path = StringHelper.createI18NURL(label);
		return '/'+URLHelper.mergePath(getParentPath(ctx, currentPage.getParent()),ctx.getFormat(),path);
	}

	@Override
	public String getFormat(ContentContext ctx, String url) {
		String[] pathItems = StringUtils.split(url, "/");
		String format = "html";
		if (pathItems.length >= 4) {
			format = pathItems[pathItems.length - 2];
		}
		if (!ctx.getGlobalContext().getStaticConfig().isContentExtensionValid(format)) {
			format = "html";
		}
		return format;
	}

	@Override
	public String createURLKey(String url) {
		try {
			return URLDecoder.decode(url, ContentContext.CHARACTER_ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

}
