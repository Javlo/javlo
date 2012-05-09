package org.javlo.component.image;

import org.javlo.context.ContentContext;

public class StandardImage extends FilterImage {
	
	public static final String TYPE = "standard-image";

	@Override
	protected String getFilter(ContentContext ctx) {
		return "standard";
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
