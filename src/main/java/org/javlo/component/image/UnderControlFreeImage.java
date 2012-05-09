package org.javlo.component.image;

import org.javlo.context.ContentContext;

public class UnderControlFreeImage extends FilterImage {
	
	public static final String TYPE = "under-control-free-image";

	@Override
	protected String getFilter(ContentContext ctx) {
		return "under-control-free";
	}
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	protected boolean isClickable() {
		return false;
	}

}
