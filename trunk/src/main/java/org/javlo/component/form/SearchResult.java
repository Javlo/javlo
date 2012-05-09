package org.javlo.component.form;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

public class SearchResult extends AbstractVisualComponent {
	
	public static final String TYPE = "search-result";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		String renderer = "/jsp/search/search_result.jsp";
		
		if (ctx.getCurrentTemplate() != null && ctx.getCurrentTemplate().getSearchRenderer(ctx) != null) {
			renderer = ctx.getCurrentTemplate().getSearchRenderer(ctx);
		}
		
		return executeJSP(ctx, renderer);
	}
	
	@Override
	public int getComplexityLevel() {
		return COMPLEXITY_ADMIN;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}
	
	@Override
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}

}
