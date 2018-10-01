package org.javlo.component.core;

import java.util.List;
import java.util.Map;

import org.javlo.context.ContentContext;

public interface IDataContainer {

	public List<Map<String, String>> getData(ContentContext ctx) throws Exception;
	
	public List<Map<String, String>> getData(ContentContext ctx, String login) throws Exception;
}
