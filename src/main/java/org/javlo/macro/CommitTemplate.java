package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;

public class CommitTemplate extends AbstractMacro {

	@Override
	public String getName() {
		return "commit-template";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		ctx.getCurrentTemplate().clearRenderer(ctx);
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

};
