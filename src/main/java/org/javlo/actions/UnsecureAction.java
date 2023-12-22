package org.javlo.actions;

import jakarta.servlet.http.HttpSession;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.user.UserAction;
import org.javlo.service.RequestService;

public class UnsecureAction implements IAction {

	public static final String TYPE = "unsecure";

	@Override
	public String getActionGroupName() {
		return TYPE;
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return true;
	}

	public static String performAskChangePassword(RequestService rs, ContentContext ctx, EditContext editContext, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		return UserAction.performAskChangePassword(rs, ctx, editContext, globalContext, messageRepository, i18nAccess);
	}

	public static String performChangePasswordWithToken(RequestService rs, ContentContext ctx, EditContext editContext, GlobalContext globalContext, HttpSession session, StaticConfig staticConfig, MessageRepository messageRepository, I18nAccess i18nAccess) {
		return UserAction.performChangePasswordWithToken(rs, ctx, editContext, globalContext, session, staticConfig, messageRepository, i18nAccess);
	}

}
