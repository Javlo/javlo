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
	protected void prepareView(ContentContext ctx) throws Exception {
		System.out.println("***** SearchResult.prepareView : update CTX"); //TODO: remove debug trace
		org.javlo.search.SearchResult sr = org.javlo.search.SearchResult.getInstance(ctx);
		sr.setContentContext(ctx);
		super.prepareView(ctx);
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		String renderer = "/jsp/components/search/search_result.jsp";
		
		if (ctx.getCurrentTemplate() != null && ctx.getCurrentTemplate().getSearchRenderer(ctx) != null) {
			renderer = ctx.getCurrentTemplate().getSearchRenderer(ctx);
		}
		
		org.javlo.search.SearchResult sr = org.javlo.search.SearchResult.getInstance(ctx);
		sr.setContentContext(ctx);

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
	
	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "";
	}
	
	@Override
	public String getSufixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

}
