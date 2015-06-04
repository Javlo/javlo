package org.javlo.component.links;

import java.util.LinkedList;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageBean;

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
		LinkedList<PageBean> pages = new LinkedList<PageBean>();
		MenuElement page = ctx.getCurrentPage();
		while (page != null) {
			pages.add(0, page.getPageBean(ctx));
			page = page.getParent();
		}
		ctx.getRequest().setAttribute("pages", pages);
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return AbstractVisualComponent.COMPLEXITY_STANDARD;
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}
	
	@Override
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}

}
