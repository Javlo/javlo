package org.javlo.module.search;

public class SearchResultBean {
	
	private String type;
	
	private String title;
	
	private String url;
	
	private String authors;
	
	private String date;
	
	private String language;
	
	private int matching = 0;
	
	public SearchResultBean(String type, String title, String lang, String url, String authors, String date, int matching) {
		super();
		this.type = type;
		this.title = title;
		this.url = url;
		this.authors = authors;
		this.date = date;
		this.matching = matching;
		this.language = lang;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getMatching() {
		return matching;
	}

	public void setMatching(int matching) {
		this.matching = matching;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	


}
