package org.javlo.social;

import javax.servlet.http.HttpSession;

import org.javlo.helper.StringHelper;

public class SocialFilter {
	
	private String query;
	private String author;
	private String title;
	private boolean onlyMine = false;
	private boolean notValided = false;
	private boolean noResponse = false;
	
	private static final String KEY = "socialFilter";
	
	public static final SocialFilter getInstance(HttpSession session) {
		SocialFilter instance = (SocialFilter)session.getAttribute(KEY);
		if (instance == null) {
			instance = new SocialFilter();
			session.setAttribute(KEY, instance);
		}
		return instance;
	}
	
	public boolean isFilterEmpty() {
		return StringHelper.isEmpty(title) && StringHelper.isEmpty(author) && StringHelper.isEmpty(query) && !onlyMine;
	}

	public String getQuery() {
		if (query != null) {
			return query.trim();
		} else {
			return null;
		}
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

	public void reset() {
		query = null;
		title = null;
		author = null;
		onlyMine = false;
		notValided = false;
	}

	public boolean isNotValided() {
		return notValided;
	}

	public void setNotValided(boolean notValided) {
		this.notValided = notValided;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public boolean isNoResponse() {
		return noResponse;
	}

	public void setNoResponse(boolean noResponse) {
		this.noResponse = noResponse;
	}

}
