package org.javlo.service.document;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.javlo.helper.StringHelper;

public class DataDocument {
	
	private String category;
	private long id=0;
	private String token = null;
	private Date date = new Date();
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
		setData(data); // store token in data
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Map<String,String> getData() {
		return data;
	}
	public Date getDate() {
		return date;
	}
	
	public void setData(Map<String,String> data) {
		/** date **/
		final String DATE_KEY = "__DATE";		
		String dateToken = data.get(DATE_KEY);
		if (dateToken != null) {
			try {
				date = StringHelper.parseSortableTime(dateToken);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			data.put(DATE_KEY, StringHelper.renderSortableTime(getDate()));
		}
		
		/** token **/		
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
