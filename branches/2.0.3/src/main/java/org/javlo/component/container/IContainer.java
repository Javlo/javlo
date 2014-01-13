package org.javlo.component.container;

import org.javlo.context.ContentContext;

public interface IContainer {
	
	public static String OPEN_CONTAINER_CODE = "<div class=\"component-container\">";
	
	public static String CLOSE_CONTAINER_CODE = "</div>";
	
	public String getId();
	
	public boolean isOpen(ContentContext ctx);
	
	/**
	 * get the container code "open code" for open component.
	 * @param ctx
	 * @return html code.
	 */
	public String getOpenCode(ContentContext ctx);
	
	/**
	 * return the close code for the current component. 
	 * @param ctx
	 * @return
	 */
	public String getCloseCode(ContentContext ctx);

}
