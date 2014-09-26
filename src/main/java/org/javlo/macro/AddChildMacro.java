package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ContentElementList;
import org.javlo.context.ContentContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class AddChildMacro extends AbstractMacro {
	
	private static final String DEFAULT_PAGE_NAME = "default-child-page";

	@Override
	public String getName() {
		return "add-child";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		
		ContentService content = ContentService.getInstance(ctx.getRequest());
		
		String newPageName = currentPage.getName()+"-1";
		int index = 2;
		while (content.getNavigation(ctx).searchChildFromName(newPageName) != null) {
			newPageName = currentPage.getName()+'-'+index;
			index++;
		}
		MenuElement defaultPage = content.getNavigation(ctx).searchChildFromName(DEFAULT_PAGE_NAME);
		MenuElement newPage = MacroHelper.addPageIfNotExist(ctx, ctx.getCurrentPage(),newPageName, true, true);
		if (defaultPage != null)  {
			ContentElementList comps = defaultPage.getContent(ctx);
			String parentId = "0";
			while (comps.hasNext(ctx)) {
				parentId = content.createContent(ctx, newPage, comps.next(ctx).getComponentBean(), parentId, false);
			}
		}
		
		if (currentPage.isChildrenAssociation()) {			
			String newURL = URLHelper.createURL(ctx, currentPage)+"#page_"+newPage.getId();
			NetHelper.sendRedirectTemporarily(ctx.getResponse(), newURL);
		} else {
			String newURL = URLHelper.createURL(ctx, newPage);
			NetHelper.sendRedirectTemporarily(ctx.getResponse(), newURL);			
		}
		
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
	
	@Override
	public boolean isAdmin() {
		return false;
	}


}
