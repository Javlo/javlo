package org.javlo.user;

import javax.servlet.http.HttpSession;

import org.javlo.navigation.PageBean;

public class VisitorContext {
	
	private PageBean previousPage = null;

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

	public PageBean getPreviousPage() {
		return previousPage;
	}

	public void setPreviousPage(PageBean previousPage) {
		this.previousPage = previousPage;
	}
}