package org.javlo.data.rest;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

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
	public IRestItem search(ContentContext ctx, String path, String query) throws Exception {		
		ContentService content = ContentService.getInstance(ctx.getRequest());		
		MenuElement root = content.getNavigation(ctx).searchChild(ctx, '/'+path);		
		if (root != null && root.isReadAccess(ctx, ctx.getCurrentUser())) {
			root.setRestWidthChildren(children);
		}
		return root;
	}	

}