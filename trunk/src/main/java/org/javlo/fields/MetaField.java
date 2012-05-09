package org.javlo.fields;

import org.javlo.context.ContentContext;

public abstract class MetaField extends Field {
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}
	
	@Override
	public boolean isWrapped() {
		return false;
	}
	
	public abstract boolean isPublished(ContentContext ctx);
}
