package org.javlo.data.rest;

import org.javlo.context.ContentContext;

import java.util.HashMap;
import java.util.Map;

public class RestItemBean implements IRestItem {

	private String query = "";

	private Map<String, Object> map = null;

	public RestItemBean() {
	}

	public RestItemBean(Map<String, Object> map) {
		this.map = map;
	}

	@Override
	public Map<String, Object> getContentAsMap(ContentContext ctx) throws Exception {
		if (map != null) {
			return map;
		} else {
			Map<String, Object> outMap = new HashMap<String, Object>();
			outMap.put("query", query);
			return outMap;
		}
	}

	public void setQuery(String json) {
		this.query = json;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}

}
