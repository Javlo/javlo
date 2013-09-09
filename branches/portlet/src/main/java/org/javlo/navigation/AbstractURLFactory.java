package org.javlo.navigation;

import org.javlo.helper.StringHelper;

public abstract class AbstractURLFactory implements IURLFactory {

	@Override
	public String getFormat(String url) {
		String ext = StringHelper.getFileExtension(url);
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

}
