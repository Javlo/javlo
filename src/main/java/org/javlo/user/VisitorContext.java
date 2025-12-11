package org.javlo.user;

import jakarta.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageBean;
import org.javlo.service.ContentService;

public class VisitorContext {
	
	private String previousPageId = null;

	private VisitorContext() {
	}
	
	public static VisitorContext getInstance(HttpSession session) {
		String KEY = "visitor";
		VisitorContext outVisitor = (VisitorContext)session.getAttribute(KEY);
		if (outVisitor == null) {
			outVisitor = new VisitorContext();
			session.setAttribute(KEY, outVisitor);
		}
		return outVisitor;
	}

	public PageBean getPreviousPage(ContentContext ctx) {
		MenuElement page = ContentService.getInstance(ctx.getGlobalContext()).getNavigation(ctx).searchChildFromId(previousPageId);
		if (page == null) {
			return null;
		} else {
			return new PageBean(ctx, page);
		}
	}

	public void setPreviousPage(PageBean previousPage) {
		this.previousPageId = previousPage.getId();
	}
}