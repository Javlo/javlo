package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ContentElementList;
import org.javlo.context.ContentContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.content.Edit;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class AddChildMacro extends AbstractMacro {

	private static final String DEFAULT_PAGE_NAME = "default-child-page";

	@Override
	public String getName() {
		return "add-child";
	}

	public void addChild(ContentContext ctx) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		
		ContentService content = ContentService.getInstance(ctx.getRequest());

		String newPageName = currentPage.getName() + "-1";
		int index = 2;
		while (content.getNavigation(ctx).searchChildFromName(newPageName) != null) {
			newPageName = currentPage.getName() + '-' + index;
			index++;
		}
		MenuElement defaultPage = content.getNavigation(ctx).searchChildFromName(DEFAULT_PAGE_NAME);
		MenuElement newPage = MacroHelper.addPageIfNotExist(ctx, ctx.getCurrentPage(), newPageName, true, true);
		boolean changeNotification = true;
		if (getMacroProperties(ctx) != null) {
			changeNotification = StringHelper.isTrue(getMacroProperties(ctx).getProperty("change-notification", "true"));
		}
		newPage.setChangeNotification(changeNotification);

		if (defaultPage != null) {
			ContentElementList comps = defaultPage.getContent(ctx);
			String parentId = "0";
			while (comps.hasNext(ctx)) {
				parentId = content.createContent(ctx, newPage, comps.next(ctx).getComponentBean(), parentId, false);
			}
		}

		if (currentPage.isChildrenAssociation()) {
			String newURL = URLHelper.createURL(ctx, currentPage) + "#page_" + newPage.getId();
			NetHelper.sendRedirectTemporarily(ctx.getResponse(), newURL);
		} else {
			String newURL = URLHelper.createURL(ctx, newPage);
			NetHelper.sendRedirectTemporarily(ctx.getResponse(), newURL);
		}
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		
		if (!Edit.checkPageSecurity(ctx)) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}
		
		addChild(ctx);
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
	
	@Override
	public boolean isAdd() {
		return true;
	}

}
