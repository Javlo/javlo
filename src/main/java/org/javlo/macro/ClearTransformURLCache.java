package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;

public class ClearTransformURLCache extends AbstractMacro {

	@Override
	public String getName() {
		return "clear-transform-url-cache";
	}
	
	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		ctx.getGlobalContext().clearTransformShortURL();
		return null;
	}

	
	@Override
	public boolean isPreview() {
		return false;
	}
	
	@Override
	public boolean isAdmin() {
		return true;
	}
};

