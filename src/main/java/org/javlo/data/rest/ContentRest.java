package org.javlo.data.rest;

import jakarta.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.servlet.ResponseErrorException;

public class ContentRest implements IRestFactory {
	
	private boolean children = true;
	
	public ContentRest(boolean children) {
		this.children = children;
	}

	@Override
	public String getName() {
		if (children) {
			return "content-children";
		} else {
			return "content";
		}
	}

	@Override
	public IRestItem search(ContentContext ctx, String path, String query, int max) throws Exception, ResponseErrorException {		
		ContentService content = ContentService.getInstance(ctx.getRequest());
		if (path.toLowerCase().endsWith(".html")) {
			path = path.substring(0, path.length()-5);
		}
		MenuElement root = content.getNavigation(ctx).searchChild(ctx, '/'+path);
		if (root != null && !root.isReadAccess(ctx, ctx.getCurrentUser())) {
			throw new ResponseErrorException(HttpServletResponse.SC_FORBIDDEN);
		}
		if (root != null) {
			root.setRestWidthChildren(children);
		}
		return root;
	}

}