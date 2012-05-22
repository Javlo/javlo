package org.javlo.module.user;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.UserFactory;

public class UserModuleContext {
	
	private static final String KEY = "userContext";

	public static final String VIEW_USERS_LIST = "view";
	public static final String ADMIN_USERS_LIST = "admin";
	
	private List<String> allModes = new LinkedList<String>(Arrays.asList(new String[] {VIEW_USERS_LIST, ADMIN_USERS_LIST}));

	private String mode = ADMIN_USERS_LIST;
	
	public static UserModuleContext getInstance(HttpSession session) {
		UserModuleContext userContext = (UserModuleContext)session.getAttribute(KEY);
		if (userContext == null) {
			userContext = new UserModuleContext();
			session.setAttribute(KEY, userContext);
		}
		return userContext;
	}
		
	public IUserFactory getUserFactory(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (mode.equals(VIEW_USERS_LIST)) {
			return UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		} else if (mode.equals(ADMIN_USERS_LIST)) {
			return AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		} else {
			return null;
		}
	}
	
	public Collection<String> getAllModes() {
		return allModes;
	}
	
	public String getMode() {
		return mode;
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}
}
