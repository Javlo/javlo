package org.javlo.component.form;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

public class CreateContext extends AbstractVisualComponent implements IAction {
	
	public static final String TYPE  = "create-context";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getActionGroupName() {
		return "createContext";
	}
	
	public static String performCreate(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws ConfigurationException, IOException {
		
		String name = rs.getParameter("name", null);
		if (GlobalContext.isExist(ctx.getRequest(), name)) {
			return i18nAccess.getViewText("create-context.msg.error.exist");
		}
		String email = rs.getParameter("email", null);
		if (!StringHelper.isMail(email)) {
			return i18nAccess.getViewText("create-context.msg.error.email");
		}
		String pwd = rs.getParameter("pwd", "");
		String pwd2 = rs.getParameter("pwd2", "");
		if (pwd.length() < 5) {
			return i18nAccess.getViewText("create-context.msg.error.pwd-size");
		}
		if (!pwd.equals(pwd2)) {
			return i18nAccess.getViewText("create-context.msg.error.pwd-same");
		}
		
		GlobalContext newContext = GlobalContext.getInstance(ctx.getRequest().getSession(), name);
		
		return null;
	}
	
	@Override
	public boolean isContentCachableByQuery(ContentContext ctx) {
		return false;
	}

}
