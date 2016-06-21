package org.javlo.navigation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public abstract class AbstractURLFactory implements IURLFactory {
	
	private Map<String, String> urls = null;

	@Override
	public String getFormat(ContentContext ctx, String url) {
		String ext = StringHelper.getFileExtension(url);
		ext = StringHelper.onlyAlphaNumeric(ext, true);
		if (!ctx.getGlobalContext().getStaticConfig().isContentExtensionValid(ext)) {
			return ext = "html";
		}
		return ext;
	}

	@Override
	public String createURLKey(String url) {
		int pointIndex = url.lastIndexOf('.');
		if (pointIndex >= 0) {
			url = url.substring(0, pointIndex);
		}
		try {
			url = URLDecoder.decode(url, ContentContext.CHARACTER_ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;
	}
	
	/**
	 * check if url allready exist, and add in the set if not.
	 * @param url
	 * @return true if url allready exist on other page, false otherwise
	 */
	protected boolean addAndCheckExistURL(MenuElement page, String url) {
		if (urls == null) {
			urls = new HashMap<String, String>();
		}
		if (urls.keySet().contains(url)) {
			if (urls.get(url).equals(page.getId())) {
				return false;
			} else {
				return true;
			}
		} else {
			urls.put(url, page.getId());
			return false;
		}
	}

}
