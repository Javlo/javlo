package org.javlo.navigation;

import org.javlo.context.ContentContext;

public class NoURLFactory implements IURLFactory {

	public NoURLFactory() {
	}

	@Override
	public String createURLKey(String url) {
		return null;
	}

	@Override
	public String createURL(ContentContext ctx, MenuElement page) throws Exception {
		return null;
	}

	@Override
	public String getFormat(ContentContext ctx, String url) {
		return null;
	}

}
