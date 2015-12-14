package org.javlo.data.rest;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.thread.ThreadManager;
import org.javlo.utils.JSONMap;

public class ThreadRunRest implements IRestFactory {

	@Override
	public String getName() {
		return "thread";
	}

	@Override
	public IRestItem search(ContentContext ctx, String query) {
		RestItemBean outItem = new RestItemBean();
		ThreadManager threadManager = ThreadManager.getInstance(ctx.getRequest().getSession().getServletContext());		
		outItem.setQuery("{\"thread\" : \""+query+"\"}");
		Map<String,Object> map = new HashMap<String, Object>();
		if (query.startsWith("/")) {
			query = query.substring(1);
		}
		map.put("running", ""+threadManager.isThreadRunning(query));
		StringWriter strWriter = new StringWriter();
		JSONMap.JSON.toJson(map, strWriter);
		outItem.setMap(map);		
		return outItem;
	}	

}