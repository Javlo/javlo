package org.javlo.data.rest;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.utils.JSONMap;

public class TestRest implements IRestFactory {

	@Override
	public String getName() {
		return "test";
	}

	@Override
	public IRestItem search(ContentContext ctx, String path, String query, int max) {
		RestItemBean outItem = new RestItemBean();
		outItem.setQuery("{\"path\" : \""+path+"\"}");
		Map<String,String> map = new HashMap<String, String>();
		map.put("path", path);
		StringWriter strWriter = new StringWriter();
		JSONMap.JSON.toJson(map, strWriter);
		outItem.setQuery(strWriter.toString());
		return outItem;
	}	

}