package org.javlo.module.user;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.UserFactory;

public class UserModuleContext {

	private static final String KEY = "userContext";

	public static final String VIEW_MY_SELF = "myself";
	public static final String VIEW_USERS_LIST = "view";
	public static final String ADMIN_USERS_LIST = "admin";

	private static final List<String> allModes = new LinkedList<String>(Arrays.asList(new String[] { VIEW_MY_SELF, VIEW_USERS_LIST, ADMIN_USERS_LIST }));

	private List<String> modes;

	private String mode = VIEW_MY_SELF;

	public static UserModuleContext getInstance(HttpServletRequest request) {
		HttpSession session = request.getSession();

		UserModuleContext userContext = (UserModuleContext) session.getAttribute(KEY);
		if (userContext == null) {
			userContext = new UserModuleContext();
			session.setAttribute(KEY, userContext);
		}

		AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();
		GlobalContext globalContext = GlobalContext.getInstance(request);
		AdminUserFactory adminUserFactory = AdminUserFactory.createAdminUserFactory(globalContext, session);

		userContext.modes = new LinkedList<String>(allModes);

		if (!adminUserSecurity.haveRight(adminUserFactory.getCurrentUser(session), AdminUserSecurity.ADMIN_USER_ROLE, AdminUserSecurity.GENERAL_ADMIN)) {
			userContext.modes.remove(ADMIN_USERS_LIST);
		}

		if (!adminUserSecurity.haveRight(adminUserFactory.getCurrentUser(session), AdminUserSecurity.USER_ROLE, AdminUserSecurity.GENERAL_ADMIN)) {
			userContext.modes.remove(VIEW_USERS_LIST);
		}

		if (adminUserSecurity.isGod(adminUserFactory.getCurrentUser(session))) {
			userContext.modes.remove(VIEW_MY_SELF);
			if (userContext.mode.equals(VIEW_MY_SELF)) {
				userContext.mode = ADMIN_USERS_LIST;
			}
		} else if (!globalContext.isMaster()) {
			if (adminUserSecurity.isMaster(adminUserFactory.getCurrentUser(session))) {
				userContext.modes.remove(VIEW_MY_SELF);
				if (userContext.mode.equals(VIEW_MY_SELF)) {
					userContext.mode = ADMIN_USERS_LIST;
				}
			}
		}

		return userContext;
	}

	public IUserFactory getUserFactory(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (mode.equals(VIEW_USERS_LIST)) {
			return UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		} else if (mode.equals(ADMIN_USERS_LIST) || mode.equals(VIEW_MY_SELF)) {
			return AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		} else {
			return null;
		}
	}

	public Collection<String> getAllModes() {
		return modes;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
}
