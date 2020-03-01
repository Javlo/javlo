package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;

public class RebuitTemplateHtml extends AbstractMacro {

	@Override
	public String getName() {
		return "rebuit-template-html";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return "file deleted : "+ctx.getCurrentTemplate().rebuildTemplate(ctx, false);
	}

	@Override
	public boolean isPreview() {
		return true;
	}

}
