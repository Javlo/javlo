package org.javlo.component.links;

import java.util.LinkedList;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageBean;
import org.javlo.user.VisitorContext;

public class Breadcrumb extends AbstractVisualComponent {

	public static final String TYPE = "breadcrumb";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "structure", "visitor" };
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
		VisitorContext visitorContext = VisitorContext.getInstance(ctx.getRequest().getSession());
		if (getStyle().equals("visitor") && visitorContext.getPreviousPage() != null) {			
			if (page != null) {
				pages.add(0, page.getPageBean(ctx));
			}
			
			PageBean pageBean = visitorContext.getPreviousPage();
			pageBean.setContentContext(ctx);			
			while (pageBean != null) {
				if (!pageBean.getName().equals(ctx.getCurrentPage().getName())) {
					pages.add(0, pageBean);					
				}
				pageBean = pageBean.getParent();
			}
		} else {
			while (page != null) {
				pages.add(0, page.getPageBean(ctx));
				page = page.getParent();
			}
		}
		ctx.getRequest().setAttribute("pages", pages);
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
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
