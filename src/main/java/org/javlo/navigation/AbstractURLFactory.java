package org.javlo.navigation;

import org.javlo.helper.StringHelper;

public abstract class AbstractURLFactory implements IURLFactory {

	@Override
	public String getFormat(String url) {
		return StringHelper.getFileExtension(url);
	}

}
