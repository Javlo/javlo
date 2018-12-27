package org.javlo.data.rest;

import org.javlo.context.ContentContext;

public interface IRestFactory {

	public String getName();
	
	public IRestItem search(ContentContext ctx, String path, String query, int max) throws Exception;
	
}
