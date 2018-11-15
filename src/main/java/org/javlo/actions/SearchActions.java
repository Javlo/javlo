/** 
 * Created on Aug 13, 2003
 */
package org.javlo.actions;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.component.form.SearchResultComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.PaginationContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLNavigationHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.search.ISearchEngine;
import org.javlo.search.SearchEngineFactory;
import org.javlo.search.SearchFilter;
import org.javlo.search.SearchResult.SearchElement;
import org.javlo.service.RequestService;
import org.javlo.template.TemplateSearchContext;
import org.javlo.user.AdminUserSecurity;
import org.owasp.encoder.Encode;

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
					ISearchEngine search = SearchEngineFactory.getEngine(ctx);
					List<SearchElement> result = search.search(ctx, groupId, searchStr, sort, componentList);
					if (!ctx.isAjax()) {
						if (ctx.getCurrentPage().getContentByType(ctx.getContextWithoutArea(), SearchResultComponent.TYPE).size() == 0) {
							ctx.setSpecialContentRenderer("/jsp/view/search/search_result.jsp");
							if (ctx.getCurrentTemplate() != null && ctx.getCurrentTemplate().getSearchRenderer(ctx) != null) {
								ctx.setSpecialContentRenderer(ctx.getCurrentTemplate().getSearchRenderer(ctx));
							}
						}
						ctx.getRequest().getSession().setAttribute("searchList", result);
						PaginationContext.getInstance(ctx.getRequest(), "searchPagination", result.size(), 10);
					} else {
						String maxStr = requestService.getParameter("max");
						int max = Integer.MAX_VALUE;
						if (StringHelper.isDigit(maxStr)) {
							max = Integer.parseInt(maxStr);
							if (result.size()>max) {
								List<SearchElement> maximuzedResult  = new LinkedList<>();
								for (int i=0; i<max; i++) {
									maximuzedResult.add(result.get(i));
								}
								result = maximuzedResult;
							}
						}
						ctx.getAjaxData().put("searchResult", result);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			msg = e.getMessage();
		}

		return msg;
	}
	
	public synchronized static String performSearchresulthtml(ContentContext ctx) throws Exception {
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		performSearch(ctx.getRequest(), ctx.getResponse());
		List<SearchElement> result = (List<SearchElement>)ctx.getAjaxData().get("searchResult");
		if (result != null) {
			ctx.getAjaxData().remove("searchResult");
			synchronized (result) {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				PrintStream out = new PrintStream(outStream);
				if (result.size() == 0) {
					I18nAccess i18nAccess = I18nAccess.getInstance(ctx);					
					String searchStr = rs.getParameter("keywords", rs.getParameter("q", null));
					out.println("<div class=\"alert alert-warning\">"+i18nAccess.getText("search.title.no-result")+Encode.forHtml(searchStr)+"</div>");
				}
				for (SearchElement page : result) {
					String method = rs.getParameter("method");
					if (method != null) {
						if (!AdminUserSecurity.getInstance().canRole(ctx.getCurrentEditUser(), AdminUserSecurity.CONTENT_ROLE)) {
							method = null;
						}
					}
					out.println(XHTMLNavigationHelper.renderPageResult(ctx, page, method));
				}
				out.close();
				ctx.getAjaxInsideZone().put(rs.getParameter("id"), new String(outStream.toByteArray()));
			}
		}
		return null;
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

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return true;
	}

}
