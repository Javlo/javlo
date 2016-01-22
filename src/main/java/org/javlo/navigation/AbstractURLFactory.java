package org.javlo.navigation;

import java.util.HashMap;
import java.util.Map;

import org.javlo.helper.StringHelper;

public abstract class AbstractURLFactory implements IURLFactory {
	
	private Map<String, String> urls = null;

	@Override
	public String getFormat(String url) {
		String ext = StringHelper.getFileExtension(url);
		ext = StringHelper.onlyAlphaNumeric(ext, true);
		if (ext.trim().length() == 0) {
			return ext = "html";
		}
		return ext;
	}

	@Override
	public String createURLKey(String url) {
		int pointIndex = url.lastIndexOf('.');
		if (pointIndex >= 0) {
			return url.substring(0, pointIndex);
		} else {
			return url;
		}
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
