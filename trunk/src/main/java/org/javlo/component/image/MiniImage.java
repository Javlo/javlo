package org.javlo.component.image;

import org.javlo.context.ContentContext;

public class MiniImage extends StandardImageClickable {
	
	public static final String TYPE = "mini-image";

	@Override
	protected String getFilter(ContentContext ctx) {
		return "mini";
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
