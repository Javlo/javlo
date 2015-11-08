package org.javlo.data.rest;

import java.util.HashMap;
import java.util.Map;

import org.javlo.context.ContentContext;

public class RestItemBean implements IRestItem {

	private String query = "";

	@Override
	public Map<String, Object> getContentAsMap(ContentContext ctx) throws Exception {		
		Map<String, Object> outMap = new HashMap<String, Object>();
		outMap.put("query", query);
		return outMap;
	}

	public void setQuery(String json) {
		this.query = json;
	}

}
