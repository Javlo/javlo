package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;

public class RebuitTemplate extends AbstractMacro {

	@Override
	public String getName() {
		return "rebuit-template";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return "file deleted : "+ctx.getCurrentTemplate().rebuildTemplate(ctx, true);
	}

	@Override
	public boolean isPreview() {
		return true;
	}

}
