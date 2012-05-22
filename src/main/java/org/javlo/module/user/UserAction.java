package org.javlo.module.user;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.RequestParameterMap;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.Module;
import org.javlo.module.ModuleContext;
import org.javlo.service.RequestService;
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
	public String prepare(ContentContext ctx, ModuleContext moduleContext) throws Exception {

		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest().getSession());
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		ctx.getRequest().setAttribute("users", userContext.getUserFactory(ctx).getUserInfoList());

		if (requestService.getParameter("user", null) == null || requestService.getParameter("back", null) != null) {
			moduleContext.getCurrentModule().restoreAll();
		} else {
			IUserFactory userFactory = userContext.getUserFactory(ctx);
			User user = userFactory.getUser(requestService.getParameter("user", null));
			if (user == null) {
				return "user not found : " + requestService.getParameter("user", null);
			}

			Map<String, String> userInfoMap = BeanHelper.bean2Map(user.getUserInfo());

			ctx.getRequest().setAttribute("user", user);
			ctx.getRequest().setAttribute("userInfoMap", userInfoMap);
			List<String> keys = new LinkedList<String>(userInfoMap.keySet());
			Collections.sort(keys);
			ctx.getRequest().setAttribute("userInfoKeys", keys);			
		}

		return super.prepare(ctx, moduleContext);
	}

	public String performChangeMode(ContentContext ctx, RequestService requestService, HttpSession session) {
		UserModuleContext userContext = UserModuleContext.getInstance(session);
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

	public String performUpdate(ContentContext ctx, RequestService requestService, StaticConfig staticConfig, HttpSession session, Module currentModule, I18nAccess i18nAccess, MessageRepository messageRepository) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (requestService.getParameter("ok", null) != null) {
			UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest().getSession());
			IUserFactory userFactory = userContext.getUserFactory(ctx);
			User user = userFactory.getUser(requestService.getParameter("user", null));
			if (user == null) {
				return "user not found : " + requestService.getParameter("user", null);
			}
			
			IUserInfo userInfo = user.getUserInfo();
			String pwd = user.getPassword();
			
			BeanHelper.copy(new RequestParameterMap( ctx.getRequest() ), userInfo);
			
			if (staticConfig.isPasswordEncryt()) {
				if (!userInfo.getPassword().equals(pwd)) {
					userInfo.setPassword(StringHelper.encryptPassword(userInfo.getPassword()));
				}
			}
			
			userFactory.updateUserInfo(userInfo);
			userFactory.store();
			
			messageRepository.setGlobalMessageAndNotification(ctx,new GenericMessage(i18nAccess.getText("user.message.updated", new String[][] {{"user", user.getLogin() }}), GenericMessage.INFO));
		}

		return null;
	}
	
	public String performCreateUser(ContentContext ctx, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository) {
		String newUser = requestService.getParameter("user", null);
		if (newUser == null) {
			return "bad request structure : need 'user' as parameter for create a new user.";
		}
		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest().getSession());
		IUserFactory userFactory = userContext.getUserFactory(ctx);
		IUserInfo newUserInfo = userFactory.createUserInfos();
		newUserInfo.setId(newUser);
		newUserInfo.setLogin(newUser);
		try {
			userFactory.addUserInfo(newUserInfo);
			userFactory.store();
		} catch (UserAllreadyExistException e) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("user.message.user-exist"), GenericMessage.ERROR));			
		}
		
		messageRepository.setGlobalMessageAndNotification(ctx,new GenericMessage(i18nAccess.getText("user.message.create", new String[][] {{"user", newUser }}), GenericMessage.INFO));
		
		return null;	
	}
	
	public String performDeleteUser(ContentContext ctx, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository) throws UserAllreadyExistException {
		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest().getSession());
		IUserFactory userFactory = userContext.getUserFactory(ctx);
		
		Collection<IUserInfo> users = new LinkedList<IUserInfo>(userFactory.getUserInfoList());
		int deletedUser = 0;
		for (IUserInfo ui : users) {
			if (requestService.getParameter(ui.getLogin(), null)  != null) {
				userFactory.deleteUser(ui.getLogin());
				deletedUser++;
			}
		}
		userFactory.store();

		if (deletedUser > 0) {
			messageRepository.setGlobalMessageAndNotification(ctx,new GenericMessage(i18nAccess.getText("user.message.delete", new String[][] {{"deletedUser", ""+deletedUser }}), GenericMessage.INFO));
		} else {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("user.message.no-delete"), GenericMessage.ALERT));
		}
		
		return null;
		
	}

}
