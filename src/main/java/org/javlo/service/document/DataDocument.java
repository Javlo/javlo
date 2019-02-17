package org.javlo.service.document;

import java.util.Map;

import org.javlo.helper.StringHelper;

public class DataDocument {
	
	private String category;
	private long id=0;
	private String token = null;
	private Map<String,String> data;
	
	public DataDocument(String category, long id, String token, Map<String, String> data) {
		super();
		this.category = category;
		this.id = id;
		this.token = token;
		setData(data);
	}
	
	public DataDocument(String category, long id, Map<String, String> data) {
		super();
		this.category = category;
		this.id = id;
		setData(data);
	}
	
	public DataDocument(String category, Map<String, String> data) {
		super();
		this.category = category;
		setData(data);
	}

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getToken() {
		return token;
	}
	public void resetToken() {
		token = StringHelper.getRandomString(10);
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Map<String,String> getData() {
		return data;
	}
	public void setData(Map<String,String> data) {
		final String KEY = "__TOKEN";
		String dataToken = data.get(KEY);
		if (dataToken != null) {
			token = dataToken;
		} else if (token != null) {
			data.put(KEY, token);
		}
		this.data = data;
	}

}
