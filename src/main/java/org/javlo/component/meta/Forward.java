/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

/**
 * @author pvandermaesen
 */
public class Forward extends AbstractVisualComponent {

	public static final String TYPE = "forward";

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_NONE;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (ctx.isAsViewMode()) {
			return "";
		} else {
			return TYPE+" >> "+getValue();
		}
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_ADMIN);
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {	
		return true; // true for lauch the forward
	}
}
