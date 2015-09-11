package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ContentElementList;
import org.javlo.context.ContentContext;
import org.javlo.helper.MacroHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class DuplicatePage extends AbstractMacro {

	@Override
	public String getName() {
		return "duplicate-page";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		if (currentPage.getParent() == null) {
			return "you can't duplicate the root page.";
		}

		ContentService content = ContentService.getInstance(ctx.getRequest());

		String baseName = "child";
		if (currentPage.getName().contains("-")) {
			baseName = currentPage.getName().substring(0, currentPage.getName().lastIndexOf('-'));
		}

		String newPageName = baseName + "-1";
		int index = 2;
		while (content.getNavigation(ctx).searchChildFromName(newPageName) != null) {
			newPageName = currentPage.getName() + '-' + index;
			index++;
		}
		
		MenuElement newPage = MacroHelper.addPageIfNotExist(ctx, ctx.getCurrentPage().getParent(), newPageName, false, false);
		newPage.setTemplateId(currentPage.getTemplateId());
		ContentContext noAreaCtx = ctx.getContextWithoutArea();
		ContentElementList comps = currentPage.getContent(noAreaCtx);
		
		String parentId = "0";
		while (comps.hasNext(noAreaCtx)) {
			parentId = content.createContent(ctx, newPage, comps.next(noAreaCtx).getComponentBean(), parentId, false);
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
