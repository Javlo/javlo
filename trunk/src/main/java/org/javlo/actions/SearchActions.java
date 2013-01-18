/** 
 * Created on Aug 13, 2003
 */
package org.javlo.actions;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.component.form.SearchResultComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.PaginationContext;
import org.javlo.helper.StringHelper;
import org.javlo.search.SearchFilter;
import org.javlo.search.SearchResult;
import org.javlo.search.SearchResult.SearchElement;
import org.javlo.service.RequestService;
import org.javlo.template.TemplateSearchContext;

/**
 * @author pvandermaesen list of actions for search in cms.
 */
public class SearchActions implements IAction {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(SearchActions.class.getName());

	public static String performSearch(HttpServletRequest request, HttpServletResponse response) {
		String msg = null;

		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			RequestService requestService = RequestService.getInstance(request);

			String searchStr = requestService.getParameter("keywords", requestService.getParameter("q", null));

			logger.info("search on : " + searchStr);

			List<String> componentList = requestService.getParameterListValues("comps", null);
			String groupId = requestService.getParameter("search-group", null);
			String sort = requestService.getParameter("sort", null);

			SearchFilter searchFilter = SearchFilter.getInstance(ctx);

			Collection<String> keys = requestService.getParametersMap().keySet();
			for (String key : keys) { // reset only if at least one element is selected, it can be "all"
				if (key.startsWith("root")) {
					searchFilter.clearRootPages();
				}
			}
			for (String key : keys) {
				if (key.startsWith("root-")) {
					searchFilter.addRootPageName(requestService.getParameter(key, null));
				}
			}
			if (requestService.getParameter("root", null) != null) { // all selected
				searchFilter.clearRootPages();
			}

			if (requestService.getParameter("reset", null) != null) { // all selected
				searchFilter.reset(ctx);
			}

			searchFilter.setStartDate(StringHelper.smartParseDate(requestService.getParameter("startdate", null)));
			searchFilter.setEndDate(StringHelper.smartParseDate(requestService.getParameter("enddate", null)));

			searchFilter.setTag(requestService.getParameter("tag", null));

			logger.info("search action : " + searchStr);

			if (searchStr != null) {
				if (searchStr.length() > 0) {
					if (ctx.getCurrentPage().getContentByType(ctx.getContextWithoutArea(), SearchResultComponent.TYPE).size() == 0) {
						ctx.setSpecialContentRenderer("/jsp/view/search/search_result.jsp");
						if (ctx.getCurrentTemplate() != null && ctx.getCurrentTemplate().getSearchRenderer(ctx) != null) {
							ctx.setSpecialContentRenderer(ctx.getCurrentTemplate().getSearchRenderer(ctx));
						}
					}
					SearchResult search = SearchResult.getInstance(ctx);
					search.search(ctx, groupId, searchStr, sort, componentList);
					List<SearchElement> result = search.getSearchResult();
					ctx.getRequest().getSession().setAttribute("searchList", result);
					PaginationContext.getInstance(ctx.getRequest().getSession(), "pagination", result.size(), 10);
				} else {
					msg = "error search strign not defined";
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			msg = e.getMessage();
		}

		return msg;
	}

	public static String performTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);

		TemplateSearchContext tempCtx = TemplateSearchContext.getInstance(request.getSession());
		tempCtx.setAuthors(requestService.getParameter("authors", ""));
		tempCtx.setSource(requestService.getParameter("source", ""));
		tempCtx.setDominantColor(requestService.getParameter("dominant_color", ""));
		try {
			String dateStr = requestService.getParameter("date", "");
			if (dateStr.trim().length() == 0) {
				tempCtx.setDate(null);
			} else {
				Date date = StringHelper.parseDate(dateStr);
				tempCtx.setDate(date);
			}
		} catch (ParseException e) {
			tempCtx.setDate(null);
		}
		try {
			tempCtx.setDepth(Integer.parseInt(requestService.getParameter("depth", "")));
		} catch (NumberFormatException e) {
		}
		return null;
	}

	@Override
	public String getActionGroupName() {
		return "search";
	}

}
