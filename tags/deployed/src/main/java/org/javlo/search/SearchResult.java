/*
 * Created on 8 sept. 2003
 */
package org.javlo.search;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.search.SearchResult.SearchElement.Component;
import org.javlo.service.ContentService;

/**
 * @author pvandermaesen
 */
public class SearchResult {

	private static Logger logger = Logger.getLogger(SearchResult.class.getName());

	private static final String SEARCH_SESSION_KEY = "searchResult";

	List<SearchElement> result = new ArrayList<SearchElement>();

	String currentLg = "";

	String searchText;

	int maxPriority = 0;

	private String query;

	private String sort = "relevance";

	private final String SORT_RELEVANCE = "relevance";
	private final String SORT_DATE = "date";

	private ContentContext contentContext = null;

	public class DateComporator implements Comparator<SearchElement> {

		@Override
		public int compare(SearchElement o1, SearchElement o2) {
			return -o1.getDate().compareTo(o2.getDate());
		}

	}

	public class PriorityComporator implements Comparator<SearchElement> {
		@Override
		public int compare(SearchElement o1, SearchElement o2) {
			return -(o1.getPriority() - o2.getPriority());
		}
	}

	public static class SearchElement implements Serializable {

		public static class Component {
			private final IContentVisualComponent comp;
			private final SearchResult searchResult;

			public Component(SearchResult searchResult, IContentVisualComponent comp) {
				this.comp = comp;
				this.searchResult = searchResult;
			}

			public String getType() {
				return comp.getType();
			}

			public String getXhtml() {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				PrintStream out = new PrintStream(outStream);
				out.println(comp.getPrefixViewXHTMLCode(searchResult.getContentContext()));
				try {
					out.println(comp.getXHTMLCode(searchResult.getContentContext()));
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				out.println(comp.getSuffixViewXHTMLCode(searchResult.getContentContext()));
				out.close();
				return new String(outStream.toByteArray());
			}
		}

		String name;
		String title;
		String url;
		String description;
		String searchRequest;
		Date date = null;
		String dateString = "";
		String shortDate = "";
		String mediumDate = "";
		String fullDate = "";
		String path = null;
		int priority = 0;
		int maxPriority = 0;
		private List<Component> components;

		// MenuElement page;

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public String getDateString() {
			return dateString;
		}

		public void setDateString(String dateString) {
			this.dateString = dateString;
		}

		/**
		 * @return
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * @param description
		 */
		public void setDescription(String description) {
			this.description = description;
		}

		/**
		 * @return
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * @param url
		 */
		public void setUrl(String url) {
			this.url = url;
		}

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		public int getMaxPriority() {
			return maxPriority;
		}

		public void setMaxPriority(int maxPriority) {
			this.maxPriority = maxPriority;
		}

		public String getRelevance() {
			if (getMaxPriority() == 0) {
				return "100&nbsp;%";
			}
			String outRelevance = Math.round((getPriority() * 100) / getMaxPriority()) + "&nbsp;%";
			return StringUtils.leftPad(outRelevance, 5, '0');
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getSearchRequest() {
			return searchRequest;
		}

		public void setSearchRequest(String searchRequest) {
			this.searchRequest = searchRequest;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public void setComponent(List<Component> componentsRendered) {
			this.components = componentsRendered;
		}

		public List<Component> getComponents() {
			return components;
		}

		public String getShortDate() {
			return shortDate;
		}

		public void setShortDate(String shortDate) {
			this.shortDate = shortDate;
		}

		public String getMediumDate() {
			return mediumDate;
		}

		public void setMediumDate(String mediumDate) {
			this.mediumDate = mediumDate;
		}

		public String getFullDate() {
			return fullDate;
		}

		public void setFullDate(String longDate) {
			this.fullDate = longDate;
		}

	}

	private SearchResult() {
	}; /* instance only the class here */

	public static SearchResult getInstance(ContentContext ctx) {
		SearchResult res = (SearchResult) ctx.getRequest().getAttribute(SEARCH_SESSION_KEY);
		if (res == null) {
			res = (SearchResult) ctx.getRequest().getSession().getAttribute(SearchResult.class.getName());
			if (res == null) {
				res = new SearchResult();
				ctx.getRequest().getSession().setAttribute(SearchResult.class.getName(), res);
			}
			ctx.getRequest().setAttribute(SEARCH_SESSION_KEY, res);
		}
		res.setContentContext(ctx);
		return res;
	}

	public void cleanResult() {
		result.clear();
	}

	private void addResult(ContentContext ctx, MenuElement page, String searchElement, String name, String title, String url, String description, int priority, List<Component> componentsRendered) {
		SearchElement rst = new SearchElement();
		rst.setName(name);
		rst.setUrl(url);
		rst.setTitle(title);
		rst.setSearchRequest(searchElement);
		rst.setDescription(description);
		rst.setPriority(priority);
		rst.setPath(page.getPath());
		rst.setComponent(componentsRendered);
		try {
			if (page.getContentDateNeverNull(ctx) != null) {
				Date date = page.getContentDateNeverNull(ctx);
				rst.setDate(date);
				rst.setDateString(StringHelper.renderSortableDate(date));
				rst.setShortDate(StringHelper.renderShortDate(ctx, date));
				rst.setFullDate(StringHelper.renderFullDate(ctx, date));
				rst.setMediumDate(StringHelper.renderMediumDate(ctx, date));
			} else {
				logger.warning("date not found on : " + page.getPath());
			}
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		if (priority > maxPriority) {
			maxPriority = priority;
		}
		result.add(rst);
	}

	/**
	 * @Deprecated use getSearchResult
	 * @return
	 */
	public SearchElement[] getResult() {
		SearchElement[] res = new SearchElement[result.size()];
		result.toArray(res);
		return res;
	}

	public List<SearchElement> getSearchResult() {
		if (SORT_DATE.equals(getSort())) {
			Collections.sort(result, new DateComporator());
		} else if (SORT_RELEVANCE.equals(getSort())) {
			Collections.sort(result, new PriorityComporator());
		}
		return result;
	}

	private void searchInPage(MenuElement page, ContentContext ctx, String groupId, String inSearchText, Collection<String> componentType, List<MenuElement> rootPage) throws Exception {

		SearchFilter searchFilter = SearchFilter.getInstance(ctx);
		boolean tagOK = true;
		if (searchFilter.getTag() != null && !page.getTags(ctx).contains(searchFilter.getTag())) {
			tagOK = false;
		}

		if ((!page.notInSearch(ctx) || (componentType != null && componentType.size() > 0)) && (rootPage == null || NavigationHelper.isParent(page, rootPage)) && tagOK && searchFilter.isInside(page.getContentDateNeverNull(ctx))) {

			if (groupId == null || groupId.trim().length() == 0 || page.getGroupID(ctx).contains(groupId)) {

				ContentContext ctxWithContent = ctx.getContextWithContent(page);

				if (ctxWithContent != null) {
					ContentElementList elemList = page.getLocalContentCopy(ctxWithContent);

					searchText = inSearchText;
					int searchLevel = 0;
					List<Component> componentsRenderer = new LinkedList<Component>();

					while (elemList.hasNext(ctxWithContent)) {
						IContentVisualComponent cpt = elemList.next(ctxWithContent);

						if (componentType == null || componentType.contains(cpt.getType())) {
							if (cpt.getSearchLevel() > 0) {
								int cptSearchLevel = StringUtils.countMatches(cpt.getTextForSearch().toLowerCase(), inSearchText.toLowerCase()) * cpt.getSearchLevel();
								searchLevel = searchLevel + cptSearchLevel;
								if (cptSearchLevel > 0 && componentType != null) {
									componentsRenderer.add(new Component(this, cpt));
								}
							}
						}
					}
					if (searchLevel != 0) {
						addResult(ctx, page, inSearchText, page.getName(), page.getFullLabel(ctxWithContent), URLHelper.createURL(ctxWithContent, page.getPath()), page.getDescription(ctxWithContent), searchLevel, componentsRenderer);
					}
				}
			}
		}
		Collection<MenuElement> children = page.getChildMenuElements();
		for (MenuElement element : children) {
			searchInPage(element, ctx, groupId, inSearchText, componentType, rootPage);
		}
	}

	public void searchComponentInPage(ContentContext ctx, String componentType) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService content = ContentService.getInstance(globalContext);
		searchComponentInPage(content.getNavigation(ctx), ctx, componentType);
	}

	private void searchComponentInPage(MenuElement page, ContentContext ctx, String componentType) throws Exception {

		if (!page.notInSearch(ctx)) {

			ContentContext ctxWithContent = ctx.getContextWithContent(page);

			if (ctxWithContent != null) {
				ctxWithContent.setArea(null);
				int searchLevel = page.getContentByType(ctxWithContent, componentType).size();
				if (searchLevel > 0) {
					addResult(ctx, page, null, page.getName(), page.getFullLabel(ctxWithContent), URLHelper.createURL(ctxWithContent, page.getPath()), page.getDescription(ctxWithContent), searchLevel, null);
				}
			}
		}
		Collection<MenuElement> children = page.getChildMenuElements();
		for (MenuElement element : children) {
			searchComponentInPage(element, ctx, componentType);
		}
	}

	public void search(ContentContext ctx, String groupId, String searchText, String sort, List<String> comps) throws Exception {
		setQuery(searchText);
		if (sort != null) {
			setSort(sort);
		}

		SearchFilter searchFilter = SearchFilter.getInstance(ctx);
		List<MenuElement> rootsSearch = new LinkedList<MenuElement>();
		if (searchFilter.getRootPageName() != null) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			for (String pageName : searchFilter.getRootPageName().keySet()) {
				MenuElement rootSearch = ContentService.getInstance(globalContext).getNavigation(ctx).searchChildFromName(pageName);
				if (rootSearch != null) {
					rootsSearch.add(rootSearch);
				}
			}
		}

		synchronized (result) { // if two browser with the same session
			cleanResult();
			ContentService content = ContentService.getInstance(ctx.getRequest());
			MenuElement nav = content.getNavigation(ctx);

			searchInPage(nav, ctx, groupId, searchText, comps, rootsSearch);

			Iterator<SearchElement> results = result.iterator();
			while (results.hasNext()) {
				SearchElement element = results.next();
				element.setMaxPriority(maxPriority);
			}
			if (result.size() == 0) {
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("search.title.no-result") + ' ' + searchText, GenericMessage.ALERT));
			}
		}
	}

	public void searchComponent(ContentContext ctx, String componentType) throws Exception {
		setQuery(searchText);
		if (sort != null) {
			setSort(sort);
		}
		synchronized (result) { // if two browser with the same session
			cleanResult();
			ContentService content = ContentService.getInstance(ctx.getRequest());
			MenuElement nav = content.getNavigation(ctx);

			searchComponentInPage(nav, ctx, componentType);

			Iterator<SearchElement> results = result.iterator();
			while (results.hasNext()) {
				SearchElement element = results.next();
				element.setMaxPriority(maxPriority);
			}
			if (result.size() == 0) {
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("search.title.no-result") + ' ' + searchText, GenericMessage.ALERT));
			}
		}
	}

	public Collection<SearchElement> getSearchResultCollection() {
		return result;
	}

	public String getSearchText() {
		return searchText;
	}

	public String getUrlSearchText() {
		return StringHelper.txt2html(searchText);
	}

	public String getQuery() {
		if (query != null) {
			return StringHelper.removeTag(query);
		} else {
			return null;
		}
	}

	public String getUrlQuery() {
		return StringHelper.txt2html(query);
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public ContentContext getContentContext() {
		return contentContext;
	}

	public void setContentContext(ContentContext contentContext) {
		this.contentContext = contentContext;
	}

}
