package org.javlo.navigation;

import org.apache.commons.lang3.StringEscapeUtils;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
		url = StringHelper.trim(url, '/');
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
	
	protected static String staticCleanString(String text) {
		if (text==null) {
			return null;
		} else {
			return StringEscapeUtils.unescapeHtml4(text).trim();
		}
	}
	
	protected String cleanString(String text) {
		return AbstractURLFactory.staticCleanString(text);
	}
	
	/**
	 * check if url allready exist, and add in the set if not.
	 * @param url
	 * @return true if url allready exist on other page, false otherwise
	 */
	protected boolean addAndCheckExistURL(MenuElement page, String url) {
		if (urls == null) {
			urls = Collections.synchronizedMap(new HashMap<String, String>());
		}
		if (urls.containsKey(url)) {
            return !urls.get(url).equals(page.getId());
		} else {
			urls.put(url, page.getId());
			return false;
		}
	}

	public String getExistingURLId(String url) {
		if (urls != null) {
			return urls.get(url);
		} else {
			return null;
		}
	}

}
