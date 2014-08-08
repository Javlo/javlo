package org.javlo.macro;

import java.util.Map;

import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.helper.MacroHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;

public class CreateAlphabeticChildrenHereMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "create-alphabetic-here";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		if (currentPage.getParent() == null) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String msg = i18nAccess.getText("action.add.new-news-badposition");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		} else {
			MenuElement page = ctx.getCurrentPage();
			for (char c : new String("abcdefghijklmnopqrstuvwxyz").toCharArray()) {
				String newPageName = page.getName()+'_'+c;
				MenuElement newPage = MacroHelper.addPageIfNotExist(ctx, page.getName(), newPageName, false);
				for (String lg : ctx.getGlobalContext().getContentLanguages()) {
					MacroHelper.addContent(lg, newPage, "0", Title.TYPE, ""+c, ctx.getCurrentEditUser());
				}
			}
			PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		}
		return null;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

}
