package org.javlo.module.user;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.RequestParameterMap;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.user.exception.UserAllreadyExistException;

public class UserAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "user";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception {

		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		ctx.getRequest().setAttribute("users", userContext.getUserFactory(ctx).getUserInfoList());

		if ((requestService.getParameter("user", null) == null || requestService.getParameter("back", null) != null) && !userContext.getMode().equals(UserModuleContext.VIEW_MY_SELF)) {
			moduleContext.getCurrentModule().restoreAll();
		} else {
			IUserFactory userFactory = userContext.getUserFactory(ctx);
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			User user = userFactory.getUser(requestService.getParameter("user", null));

			if (userContext.getMode().equals(UserModuleContext.VIEW_MY_SELF)) {
				Module currentModule = moduleContext.getCurrentModule();
				currentModule.setToolsRenderer(null);
				currentModule.setRenderer("/jsp/edit_current.jsp");
				user = userFactory.getUser(userFactory.getCurrentUser(ctx.getRequest().getSession()).getLogin());
			}

			if (user == null) {
				return "user not found : " + requestService.getParameter("user", null);
			}

			Map<String, String> userInfoMap = BeanHelper.bean2Map(user.getUserInfo());

			ctx.getRequest().setAttribute("user", user);
			ctx.getRequest().setAttribute("userInfoMap", userInfoMap);
			List<String> keys = new LinkedList<String>(userInfoMap.keySet());
			Collections.sort(keys);
			ctx.getRequest().setAttribute("userInfoKeys", keys);
			List<String> roles = new LinkedList<String>(userFactory.getAllRoles(globalContext, ctx.getRequest().getSession()));
			Collections.sort(roles);
			ctx.getRequest().setAttribute("roles", roles);

		}

		return super.prepare(ctx, moduleContext);
	}

	public String performChangeMode(ContentContext ctx, RequestService requestService) {
		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
		String mode = requestService.getParameter("mode", "");
		userContext.setMode(mode);
		if (userContext.getUserFactory(ctx) == null) {
			userContext.setMode(UserModuleContext.ADMIN_USERS_LIST);
			return "bad user mode : " + mode;
		}

		return null;
	}

	public String performEdit(Module currentModule) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		currentModule.setToolsRenderer(null);
		currentModule.setRenderer("/jsp/edit.jsp");
		return null;
	}

	public String performUpdateCurrent(ContentContext ctx, GlobalContext globalContext, RequestService requestService, StaticConfig staticConfig, AdminUserSecurity adminUserSecurity, AdminUserFactory adminUserFactory, HttpSession session, Module currentModule, I18nAccess i18nAccess, MessageRepository messageRepository) {
		if (requestService.getParameter("ok", null) != null) {
			UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
			IUserFactory userFactory = userContext.getUserFactory(ctx);
			User user = userFactory.getUser(requestService.getParameter("user", null));
			if (user == null) {
				return "user not found : " + requestService.getParameter("user", null);
			}
			IUserInfo userInfo = user.getUserInfo();
			try {
				BeanHelper.copy(new RequestParameterMap(ctx.getRequest()), userInfo);
				userFactory.updateUserInfo(userInfo);
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}

			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("user.message.updated", new String[][] { { "user", user.getLogin() } }), GenericMessage.INFO));
		}

		return null;
	}

	public String performUpdate(ContentContext ctx, GlobalContext globalContext, RequestService requestService, StaticConfig staticConfig, AdminUserSecurity adminUserSecurity, AdminUserFactory adminUserFactory, HttpSession session, Module currentModule, I18nAccess i18nAccess, MessageRepository messageRepository) {
		if (requestService.getParameter("ok", null) != null) {
			UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
			IUserFactory userFactory = userContext.getUserFactory(ctx);
			User user = userFactory.getUser(requestService.getParameter("user", null));
			if (user == null) {
				return "user not found : " + requestService.getParameter("user", null);
			}

			IUserInfo userInfo = user.getUserInfo();
			String pwd = user.getPassword();

			try {
				BeanHelper.copy(new RequestParameterMap(ctx.getRequest()), userInfo);

				if (staticConfig.isPasswordEncryt()) {
					if (!userInfo.getPassword().equals(pwd)) {
						userInfo.setPassword(StringHelper.encryptPassword(userInfo.getPassword()));
					}
				}

				userFactory.updateUserInfo(userInfo);

				Set<String> newRoles = new HashSet<String>();
				Set<String> allRoles = userFactory.getAllRoles(globalContext, session);
				for (String role : allRoles) {
					if (requestService.getParameter("role-" + role, null) != null) {
						newRoles.add(role);
					}
				}
				IUserInfo ui = user.getUserInfo();
				if (adminUserSecurity.haveRight(adminUserFactory.getCurrentUser(ctx.getRequest().getSession()), AdminUserSecurity.ADMIN_USER_ROLE, AdminUserSecurity.GENERAL_ADMIN)) {
					ui.setRoles(newRoles);
					userFactory.updateUserInfo(ui);
				} else {
					newRoles.removeAll(ui.getRoles());
					if (newRoles.size() > 0) {
						messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("global.message.noright"), GenericMessage.ERROR));
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}

			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("user.message.updated", new String[][] { { "user", user.getLogin() } }), GenericMessage.INFO));
		}

		return null;
	}

	public String performCreateUser(ContentContext ctx, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository) {
		String newUser = requestService.getParameter("user", null);
		if (newUser == null) {
			return "bad request structure : need 'user' as parameter for create a new user.";
		}
		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
		IUserFactory userFactory = userContext.getUserFactory(ctx);
		IUserInfo newUserInfo = userFactory.createUserInfos();
		newUserInfo.setId(newUser);
		newUserInfo.setLogin(newUser);
		try {
			userFactory.addUserInfo(newUserInfo);
			try {
				userFactory.store();
			} catch (IOException e) {
				e.printStackTrace();
				return e.getMessage();
			}
		} catch (UserAllreadyExistException e) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("user.message.user-exist"), GenericMessage.ERROR));
		}

		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("user.message.create", new String[][] { { "user", newUser } }), GenericMessage.INFO));

		return null;
	}

	public String performDeleteUser(ContentContext ctx, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository) throws UserAllreadyExistException {
		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
		IUserFactory userFactory = userContext.getUserFactory(ctx);

		Collection<IUserInfo> users = new LinkedList<IUserInfo>(userFactory.getUserInfoList());
		int deletedUser = 0;
		for (IUserInfo ui : users) {
			if (requestService.getParameter(ui.getLogin(), null) != null) {
				userFactory.deleteUser(ui.getLogin());
				deletedUser++;
			}
		}
		try {
			userFactory.store();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}

		if (deletedUser > 0) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("user.message.delete", new String[][] { { "deletedUser", "" + deletedUser } }), GenericMessage.INFO));
		} else {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("user.message.no-delete"), GenericMessage.ALERT));
		}

		return null;

	}

	public static String performChangePassword(RequestService rs, ContentContext ctx, GlobalContext globalContext, HttpSession session, StaticConfig staticConfig, MessageRepository messageRepository, I18nAccess i18nAccess) {
		String pwd = rs.getParameter("password", null);
		String newPwd = rs.getParameter("newpassword", null);

		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
		IUserFactory userFactory = userContext.getUserFactory(ctx);
		User user = userFactory.getCurrentUser(session);

		if (staticConfig.isPasswordEncryt()) {
			pwd = StringHelper.encryptPassword(pwd);
		}

		if (user.getPassword().equals(pwd)) {
			if (newPwd == null || newPwd.length() < 4) {
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("user.message.password-to-short"), GenericMessage.ERROR));
			} else {
				IUserInfo ui = user.getUserInfo();
				if (staticConfig.isPasswordEncryt()) {
					newPwd = StringHelper.encryptPassword(newPwd);
				}
				ui.setPassword(newPwd);
				try {
					userFactory.updateUserInfo(ui);
					userFactory.store();
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("user.message.ok-change-password"), GenericMessage.INFO));
				} catch (IOException e) {
					e.printStackTrace();
					return e.getMessage();
				}
			}
		} else {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("user.message.bad-password"), GenericMessage.ERROR));
		}

		return null;
	}
}
