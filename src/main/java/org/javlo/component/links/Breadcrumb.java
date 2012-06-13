package org.javlo.component.links;

import java.util.LinkedList;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

public class Breadcrumb extends AbstractVisualComponent {
	
	public static final String TYPE = "breadcrumb";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}	
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		LinkedList<MenuElement.PageBean> pages = new LinkedList<MenuElement.PageBean>();
		MenuElement page = ctx.getCurrentPage();
		while (page != null) {
			pages.add(0, page.getPageBean(ctx));			
			page = page.getParent();
		}
		ctx.getRequest().setAttribute("pages", pages);
	}
	
	@Override
	public String getDefaultRenderer(ContentContext ctx) {
		return "default.jsp";
	}

}
