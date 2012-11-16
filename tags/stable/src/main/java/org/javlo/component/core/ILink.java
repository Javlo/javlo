package org.javlo.component.core;

import org.javlo.context.ContentContext;

public interface ILink {
	
	/**
	 * generate a URL or URI link with all elements to click on.
	 * @param ctx
	 * @return
	 */
	public String getURL(ContentContext ctx) throws Exception;

}
