package org.javlo.module.search;

public class SearchResultBean {
	
	private String type;
	
	private String context;
	
	private String title;
	
	private String url;
	
	private String authors;
	
	private String date;
	
	private String language;
	
	private String previewURL; 
	
	private int matching = 0;
	
	public SearchResultBean(String context, String type, String title, String lang, String url, String authors, String date, String previewURL, int matching) {
		super();
		this.context = context;
		this.type = type;
		this.title = title;
		this.url = url;
		this.authors = authors;
		this.date = date;
		this.matching = matching;
		this.language = lang;
		this.previewURL = previewURL;
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

	public String getPreviewURL() {
		return previewURL;
	}

	public void setPreviewURL(String previewURL) {
		this.previewURL = previewURL;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
}