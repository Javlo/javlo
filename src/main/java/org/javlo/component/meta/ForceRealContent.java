package org.javlo.component.meta;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

public class ForceRealContent extends AbstractVisualComponent {

	public static final String TYPE = "force-real-content";

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return AbstractVisualComponent.COMPLEXITY_ADMIN;
	}

}
