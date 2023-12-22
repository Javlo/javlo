package org.javlo.module.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.javlo.data.taxonomy.ITaxonomyContainer;
import org.javlo.helper.StringHelper;
import org.javlo.service.RequestService;
import org.javlo.utils.ListMapValueValue;

public class SearchFilter implements ITaxonomyContainer {
	
	private static final String KEY = "searchFilterModule";

	private String global;
	private String title;
	private String type;
	private String smartquery;
	private String smartqueryre;
	private Set<String> taxonomy = new HashSet<String>();
	private Set<String> components = new HashSet<String>();

	public static final SearchFilter getInstance(HttpServletRequest request) {
		SearchFilter searchFilter = (SearchFilter) request.getSession().getAttribute(KEY);
		if (searchFilter == null) {
			searchFilter = new SearchFilter();
			request.getSession().setAttribute(KEY, searchFilter);
		}
		return searchFilter;
	}

	public SearchFilter update(HttpServletRequest request) {
		RequestService rs = RequestService.getInstance(request);
		if (!StringHelper.isEmpty(request.getParameter("reset"))) {
			request.getSession().removeAttribute(KEY);
			return getInstance(request);
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
			components.clear();
			if (!StringHelper.isEmpty(request.getParameter("components"))) {
				components.addAll(rs.getParameterListValues("components"));
			}
			if (request.getParameter("smartquery") != null) {
				setSmartquery(rs.getParameter("smartquery"));
			}
			if (request.getParameter("smartqueryre") != null) {
				setSmartqueryre(rs.getParameter("smartqueryre"));
			}
			taxonomy.clear();
			taxonomy.addAll(Arrays.asList(rs.getParameterValues("taxonomy", new String[0])));
			return this;
		}
	}

	public boolean isExtendSearch() {
		if (!StringHelper.isEmpty(getTitle())) {
			return true;
		}
		if (!StringHelper.isEmpty(getType())) {
			return true;
		}
		if (getComponents().size() > 0) {
			return true;
		}
		if (getTaxonomy().size() > 0) {
			return true;
		}
		return false;
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

	public Set<String> getComponents() {
		return components;
	}

	public String getSmartquery() {
		return smartquery;
	}

	public void setSmartquery(String smartquery) {
		this.smartquery = smartquery;
	}

	public String getSmartqueryre() {
		return smartqueryre;
	}

	public void setSmartqueryre(String smartqueryre) {
		this.smartqueryre = smartqueryre;
	}

	public Map<String, String> getComponentAsMap() {
		return new ListMapValueValue<String>(components);
	}

}
