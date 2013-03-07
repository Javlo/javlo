package org.javlo.navigation;

import java.net.URLEncoder;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.navigation.PageURL;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;

public class NoExtURLCreator implements IURLFactory {

	private static String getParentPath(MenuElement page) {
		if (page != null && page.getParent() != null) {
			return getParentPath(page.getParent()) + '/' + page.getName();
		} else {
			return "";
		}
	}

	protected boolean fromDefaultLanguage() {
		return false;
	}

	@Override
	public String createURL(ContentContext ctx, MenuElement currentPage) throws Exception {
		Collection<IContentVisualComponent> comps = currentPage.getContentByType(ctx, PageURL.TYPE);
		if (comps.size() > 0) {
			return ((PageURL) comps.iterator().next()).getValue();
		}
		ContentContext lgCtx = ctx;
		if (fromDefaultLanguage()) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			lgCtx = ctx.getFreeContentContext();
			lgCtx.setLanguage(globalContext.getDefaultLanguage());
			lgCtx.setRequestContentLanguage(globalContext.getDefaultLanguage());
		}
		String label = currentPage.getLocalTitle(lgCtx);
		String path = URLEncoder.encode(StringHelper.createI18NURL(label), ContentContext.CHARACTER_ENCODING);

		return getParentPath(currentPage.getParent()) + '/' + ctx.getFormat() + '/' + path;
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

	public static void main(String[] args) {
		String val = "/item1/item2/item3/";
		System.out.println(val + " : " + val.split("/").length);
		System.out.println(val + " : " + StringUtils.split(val, "/").length);
		System.out.println(val + " : " + StringUtils.splitByWholeSeparator(val, "/").length);
	}
}
