package org.javlo.module.search;

import javax.servlet.http.HttpServletRequest;

import org.javlo.helper.StringHelper;

public class SearchFilter {

	private String global;
	private String title;

	public static final SearchFilter getInstance(HttpServletRequest request) {
		final String KEY = "searchFilter";
		SearchFilter searchFilter = (SearchFilter) request.getSession().getAttribute(KEY);
		if (searchFilter == null) {
			searchFilter = new SearchFilter();
			request.getSession().setAttribute(KEY, searchFilter);
		}		
		return searchFilter;
	}
	
	public void update(HttpServletRequest request) {		
		if (!StringHelper.isEmpty(request.getParameter("reset"))) {
			global = "";
			title = "";
		} else {
			if (!StringHelper.isEmpty(request.getParameter("query"))) {
				setGlobal(request.getParameter("query"));
			}
			if (!StringHelper.isEmpty(request.getParameter("title"))) {
				setTitle(request.getParameter("title"));
			}
		}
	}

	public String getGlobal() {
		return global;
	}

	public void setGlobal(String global) {
		this.global = global;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
