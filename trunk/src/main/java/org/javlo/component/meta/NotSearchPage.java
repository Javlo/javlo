package org.javlo.component.meta;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

public class NotSearchPage extends AbstractVisualComponent {
	
	public static final String TYPE = "not-search";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getEditXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}
	
	@Override
	public boolean isUnique() {
		return true;
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}
}
