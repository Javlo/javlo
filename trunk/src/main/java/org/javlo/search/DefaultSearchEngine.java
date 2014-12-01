package org.javlo.search;

import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.search.SearchResult.SearchElement;

public class DefaultSearchEngine implements ISearchEngine {

	private SearchResult wrapped;

	@Override
	public List<SearchElement> search(ContentContext ctx, String groupId, String searchStr, String sort, List<String> componentList) throws Exception {
		wrapped = SearchResult.getInstance(ctx);
		wrapped.search(ctx, groupId, searchStr, sort, componentList);
		return wrapped.getSearchResult();
	}

}
