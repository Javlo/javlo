package org.javlo.service.shared;

import java.util.Collection;
import java.util.LinkedList;

import javax.servlet.http.HttpSession;

public class SharedContentContext {
	
	private static final String KEY = "sharedContentContext";
	
	private String provider = null;
	private String searchQuery = null;
	private Collection<String> categories = new LinkedList<String>();
	
	public static final SharedContentContext getInstance(HttpSession session) {
		SharedContentContext outContext = (SharedContentContext)session.getAttribute(KEY);
		if (outContext == null) {
			outContext = new SharedContentContext();
			session.setAttribute(KEY, outContext);
		}
		return outContext;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		if (provider != null || !this.provider.equals(provider)) {
			setSearchQuery(null);
		}
		this.provider = provider;
	}

	public Collection<String> getCategories() {
		return categories;
	}

	public void setCategories(Collection<String> categories) {
		this.categories = categories;
	}
	
	public String getCategory() {
		if (categories.size() > 0) {
			return categories.iterator().next();
		} else {
			return null;
		}
	}

	public String getSearchQuery() {
		return searchQuery;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}
	

}
