package org.javlo.data.rest;

import java.util.Map;

import org.javlo.context.ContentContext;

public interface IRestItem {

	public Map<String, Object> getContentAsMap(ContentContext ctx) throws Exception;
	
}
