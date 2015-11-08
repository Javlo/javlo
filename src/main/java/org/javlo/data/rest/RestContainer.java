package org.javlo.data.rest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.context.ContentContext;

public class RestContainer implements IRestItem {

	private List<Map<String, Object>> restItemList = new LinkedList<Map<String, Object>>();
	private String name;
	
	public RestContainer(String name) {
		this.name = name;
	}
	
	public void addItem(ContentContext ctx, IRestItem item) throws Exception {
		restItemList.add(item.getContentAsMap(ctx));
	}
	
	@Override
	public Map<String, Object> getContentAsMap(ContentContext ctx) throws Exception {
		Map<String, Object> outMap = new HashMap<String, Object>();
		outMap.put(name, restItemList);
		return outMap;
	}

}
