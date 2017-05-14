package org.javlo.component.form;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.exception.UserAllreadyExistException;

public class CreateContext extends AbstractVisualComponent implements IAction {
	
	private static Set<String> DEFAULT_ROLES = new HashSet<String>(Arrays.asList(new String[] {"content", "light-interface", "contributor"}));
	
	public static final String TYPE  = "create-context";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getActionGroupName() {
		return "createContext";
	}
	
	public static String performCreate(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException, SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, UserAllreadyExistException {
		String name = rs.getParameter("name", null);
		String url = rs.getParameter("url", null);
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
		GlobalContext newContext = GlobalContext.getInstance(ctx.getRequest().getSession(), url);
		newContext.setAdministrator(email);
		newContext.setGlobalTitle(name);
		IUserFactory userFactory = newContext.getAdminUserFactory(ctx.getRequest().getSession());
		IUserInfo newUser = userFactory.createUserInfos();
		newUser.setLogin(email);
		newUser.setPassword(true, pwd);
		newUser.addRoles(DEFAULT_ROLES);
		userFactory.addUserInfo(newUser);	
		userFactory.store();
		return null;
	}
	
	@Override
	public boolean isContentCachableByQuery(ContentContext ctx) {
		return false;
	}

}

