package org.javlo.module.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.javlo.data.taxonomy.ITaxonomyContainer;
import org.javlo.helper.StringHelper;
import org.javlo.service.RequestService;

public class SearchFilter implements ITaxonomyContainer {

	private String global;
	private String title;
	private String type;
	private Set<String> taxonomy = new HashSet<String>();

	public static final SearchFilter getInstance(HttpServletRequest request) {
		final String KEY = "searchFilterModule";
		SearchFilter searchFilter = (SearchFilter) request.getSession().getAttribute(KEY);
		if (searchFilter == null) {
			searchFilter = new SearchFilter();
			request.getSession().setAttribute(KEY, searchFilter);
		}		
		return searchFilter;
	}
	
	public void update(HttpServletRequest request) {	
		RequestService rs = RequestService.getInstance(request);
		if (!StringHelper.isEmpty(request.getParameter("reset"))) {
			global = "";
			title = "";
			type = "";
		} else {
			if (request.getParameter("query") != null) {
				setGlobal(request.getParameter("query"));
			}
			if (request.getParameter("title") != null) {
				setTitle(request.getParameter("title"));
			}
			if (request.getParameter("type") != null) {
				setType(request.getParameter("type"));
			}			
			taxonomy.clear();
			taxonomy.addAll(Arrays.asList(rs.getParameterValues("taxonomy", new String[0])));			
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public Set<String> getTaxonomy() {
		return taxonomy;
	}
	
	protected boolean isOnlyTaxonomy() {
		return getTaxonomy().size() > 0 && StringHelper.isEmpty(global) && StringHelper.isEmpty(title);
	}


}
