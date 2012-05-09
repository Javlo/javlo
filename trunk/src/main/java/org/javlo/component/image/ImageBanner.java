package org.javlo.component.image;

import org.javlo.context.ContentContext;

public class ImageBanner extends ImageLink {

	@Override
	public String getType() {
		return "banner";
	}
	
	@Override
	protected String getFilter(ContentContext ctx) {	
		return "banner";
	}
	
	@Override
	public String[] getStyleList(ContentContext ctx) {
		return getConfig(ctx).getStyleList();
	}
	
	@Override
	/**
	 * banner never get as page image
	 */
	public boolean isImageValid(ContentContext ctx) {
		return false;
	}
	
}
