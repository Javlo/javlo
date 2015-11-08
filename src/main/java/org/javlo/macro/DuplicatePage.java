package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ContentElementList;
import org.javlo.context.ContentContext;
import org.javlo.helper.MacroHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.content.Edit;
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
		if (ctx.getCurrentPage().getChildMenuElements().size() > 0) {
			return "you can't duplicate a page without children.";
		}
		
		if (!Edit.checkPageSecurity(ctx, currentPage.getParent())) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
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
