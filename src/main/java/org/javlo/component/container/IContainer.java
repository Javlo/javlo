package org.javlo.component.container;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;

public interface IContainer extends IContentVisualComponent {
	
	public static String OPEN_CONTAINER_CODE = "<div class=\"component-container\">";
	
	public static String CLOSE_CONTAINER_CODE = "</div>";
	
	public static final String CLOSE_BOX_ATTRIBUTE = "closeBox";
	
	public String getId();
	
	public boolean isOpen(ContentContext ctx);
	
	public void setOpen(ContentContext ctx, boolean open);
	
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
	
	public IContainer getCloseComponent(ContentContext ctx);
	
	public IContainer getOpenComponent(ContentContext ctx);
	
	public String getXHTMLCode(ContentContext ctx);

}
