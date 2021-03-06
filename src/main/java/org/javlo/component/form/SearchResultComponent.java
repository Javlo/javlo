package org.javlo.component.form;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

public class SearchResultComponent extends AbstractVisualComponent {

	public static final String TYPE = "search-result";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);
		//String renderer = "/jsp/components/search/search_result.jsp";
		String renderer = null;

		if (ctx.getCurrentTemplate() != null && ctx.getCurrentTemplate().getSearchRenderer(ctx) != null) {
			renderer = ctx.getCurrentTemplate().getSearchRenderer(ctx);
		}
		if (renderer != null) {
			org.javlo.search.SearchResult.getInstance(ctx); // update ctx
			return executeJSP(ctx, renderer);
		} else {
			return "";
		}
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_ADMIN);
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	@Override
	public boolean isValueProperties() {
		return true;
	}

	@Override
	public boolean isContentTimeCachable(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}
	
	

}
