package org.javlo.component.core;

import org.javlo.context.ContentContext;

public interface ILink {
	
	/**
	 * return true if the link on the component is enabled
	 * @param ctx
	 * @return
	 */
	public boolean isLinkValid(ContentContext ctx) throws Exception;
	
	/**
	 * generate a URL or URI link with all elements to click on.
	 * @param ctx
	 * @return
	 */
	public String getURL(ContentContext ctx) throws Exception;

}
