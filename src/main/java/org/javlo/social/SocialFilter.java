package org.javlo.social;

import javax.servlet.http.HttpSession;

public class SocialFilter {
	
	private String query;
	private boolean onlyMine = false;
	
	private static final String KEY = "socialFilter";
	
	public static final SocialFilter getInstance(HttpSession session) {
		SocialFilter instance = (SocialFilter)session.getAttribute(KEY);
		if (instance == null) {
			instance = new SocialFilter();
			session.setAttribute(KEY, instance);
		}
		return instance;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isOnlyMine() {
		return onlyMine;
	}

	public void setOnlyMine(boolean onlyMine) {
		this.onlyMine = onlyMine;
	}

}
