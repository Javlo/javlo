package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class AddChildMacro extends AbstractMacro {

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
		MenuElement newPage = MacroHelper.addPageIfNotExist(ctx, ctx.getCurrentPage(),newPageName, false, true);	
		
		if (currentPage.isChildrenAssociation()) {			
			String newURL = URLHelper.createURL(ctx, currentPage)+"#page_"+newPage.getId();
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
