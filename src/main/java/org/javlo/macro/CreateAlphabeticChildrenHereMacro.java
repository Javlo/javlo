package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.helper.MacroHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;

public class CreateAlphabeticChildrenHereMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "create-alphabetic-here";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		MenuElement parentPage = ctx.getCurrentPage();
		if (parentPage.getParent() == null) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String msg = i18nAccess.getText("action.add.new-news-badposition");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		} else {
			MacroHelper.createAlphabeticChildren(ctx, parentPage);
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
