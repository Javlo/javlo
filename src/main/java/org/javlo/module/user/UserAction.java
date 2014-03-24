package org.javlo.module.user;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.JavaHelper;
import org.javlo.helper.LangHelper;
import org.javlo.helper.RequestParameterMap;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserInfo;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfoSorting;
import org.javlo.user.exception.UserAllreadyExistException;
import org.javlo.utils.CSVFactory;

public class UserAction extends AbstractModuleAction {

	private static Logger logger = Logger.getLogger(UserAction.class.getName());

	@Override
	public String getActionGroupName() {
		return "user";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception {

		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory userFactory = userContext.getUserFactory(ctx);
		if (userFactory.getCurrentUser(ctx.getRequest().getSession()) == null) {
			return null;
		}

		if (userContext.getCurrentRole() != null && !userFactory.getAllRoles(globalContext, ctx.getRequest().getSession()).contains(userContext.getCurrentRole())) {
			userContext.setCurrentRole(null);
		}

		if (userContext.getCurrentRole() != null) {
			List<IUserInfo> users = new LinkedList<IUserInfo>();
			for (IUserInfo user : userContext.getUserFactory(ctx).getUserInfoList()) {
				if (user.getRoles().contains(userContext.getCurrentRole())) {
					users.add(user);
				}
			}
			ctx.getRequest().setAttribute("users", users);
		} else {
			ctx.getRequest().setAttribute("users", userContext.getUserFactory(ctx).getUserInfoList());
		}

		if (((requestService.getParameter("user", null) == null && requestService.getParameter("cuser", null) == null) || requestService.getParameter("back", null) != null) && !userContext.getMode().equals(UserModuleContext.VIEW_MY_SELF)) {
			moduleContext.getCurrentModule().restoreAll();
		} else {
			User user = null;
			if (requestService.getParameter("user", null) != null) {
				user = userFactory.getUser(requestService.getParameter("user", null));
			} else if (requestService.getParameter("cuser", null) != null) {
				String cuser = requestService.getParameter("cuser", null);
				for (IUserInfo userInfo : userContext.getUserFactory(ctx).getUserInfoList()) {
					if (userInfo.getEncryptLogin().equals(cuser)) {
						user = userFactory.getUser(userInfo.getLogin());
					}
				}
			}
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
			ctx.getRequest().setAttribute("functions", LangHelper.collectionToMap(StringHelper.stringToCollection(userInfoMap.get("function"), ";")));

			ctx.getRequest().setAttribute("user", user);
			ctx.getRequest().setAttribute("userInfoMap", userInfoMap);
			List<String> keys = new LinkedList<String>(userInfoMap.keySet());
			Collections.sort(keys);
			keys.remove("avatarURL");
			keys.remove("encryptLogin");
			keys.remove("rolesRaw");
			ctx.getRequest().setAttribute("userInfoKeys", keys);

		}

		List<String> roles = new LinkedList<String>(userFactory.getAllRoles(globalContext, ctx.getRequest().getSession()));
		Collections.sort(roles);

		if (userFactory instanceof AdminUserFactory) {
			for (String adminRole : globalContext.getAdminUserRoles()) {
				roles.remove(adminRole);
			}
			List<String> contextRoles = new LinkedList<String>(globalContext.getAdminUserRoles());
			Collections.sort(contextRoles);
			ctx.getRequest().setAttribute("contextRoles", contextRoles);
		}

		ctx.getRequest().setAttribute("roles", roles);

		if (userContext.getMode().equals(UserModuleContext.ADMIN_USERS_LIST)) {
			ctx.getRequest().setAttribute("admin", "true");
		}

		if (!userContext.getMode().equals(UserModuleContext.VIEW_MY_SELF)) {
			String CSVLink = URLHelper.createStaticURL(ctx, "/users-list/" + globalContext.getContextKey() + "-users.csv");
			if (userContext.getMode().equals(UserModuleContext.ADMIN_USERS_LIST)) {
				CSVLink = URLHelper.createStaticURL(ctx, "/users-list/" + globalContext.getContextKey() + "-admin-users.csv");
				CSVLink = CSVLink + "?admin=true";
			}
			ctx.getRequest().setAttribute("CSVLink", CSVLink);
			ctx.getRequest().setAttribute("CSVName", URLHelper.extractFileName(CSVLink));
			ctx.getRequest().setAttribute("ExcelLink", URLHelper.addParam(CSVLink, "excel", "true"));
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

	public String performUpdateCurrent(ContentContext ctx, GlobalContext globalContext, EditContext editContext, RequestService requestService, StaticConfig staticConfig, AdminUserSecurity adminUserSecurity, AdminUserFactory adminUserFactory, HttpSession session, Module currentModule, I18nAccess i18nAccess, MessageRepository messageRepository) {
		if (requestService.getParameter("ok", null) != null || requestService.getParameter("token", null) != null || requestService.getParameter("notoken", null) != null) {
			UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
			IUserFactory userFactory = userContext.getUserFactory(ctx);
			User user = userFactory.getUser(requestService.getParameter("user", null));
			if (user == null) {
				return "user not found : " + requestService.getParameter("user", null);
			}
			IUserInfo userInfo = user.getUserInfo();
			try {

				BeanHelper.copy(new RequestParameterMap(ctx.getRequest()), userInfo);

				List<String> functions = requestService.getParameterListValues("function", Collections.EMPTY_LIST);
				if (functions.size() > 0 && userInfo instanceof AdminUserInfo) {
					((AdminUserInfo) userInfo).setFunction(StringHelper.collectionToString(functions, ";"));
				}

				if (requestService.getParameter("token", null) != null) {
					logger.info("token reset for : " + userInfo.getLogin());
					userInfo.setToken(StringHelper.getNewToken());
				}
				if (requestService.getParameter("notoken", null) != null) {
					logger.info("remove token for : " + userInfo.getLogin());
					userInfo.setToken("");
				}
				userFactory.updateUserInfo(userInfo);
				userFactory.store();
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}

			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("user.message.updated", new String[][] { { "user", user.getLogin() } }), GenericMessage.INFO));
		}

		if (editContext.isEditPreview()) {
			ctx.setClosePopup(true);
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
				
				boolean admin = userFactory instanceof AdminUserFactory;				
				IUserInfo ui = user.getUserInfo();
				if (!admin || adminUserSecurity.haveRight(adminUserFactory.getCurrentUser(ctx.getRequest().getSession()), AdminUserSecurity.ADMIN_USER_ROLE, AdminUserSecurity.GENERAL_ADMIN)) {
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
		if (userContext.getCurrentRole() != null) {
			newUserInfo.setRoles(new HashSet<String>(Arrays.asList(new String[] { userContext.getCurrentRole() })));
		}
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

	public static String performChangePassword(RequestService rs, ContentContext ctx, EditContext editContext, GlobalContext globalContext, HttpSession session, StaticConfig staticConfig, MessageRepository messageRepository, I18nAccess i18nAccess) {
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

					if (editContext.isEditPreview()) {
						ctx.setClosePopup(true);
					}

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

	public static String performToken(HttpServletRequest request, ContentContext ctx, GlobalContext globalContext, HttpSession session) {
		UserFactory factory = AdminUserFactory.createAdminUserFactory(globalContext, session);
		User user = factory.getCurrentUser(session);
		if (user == null) {
			return "user not found.";
		} else {
			if (user.getUserInfo().getToken() == null || user.getUserInfo().getToken().trim().length() == 0) {
				user.getUserInfo().setToken(StringHelper.getRandomIdBase64());
			}
			ctx.addAjaxData("token", user.getUserInfo().getToken());
		}
		return null;
	}

	public static String performUpload(RequestService rs, HttpSession session, User user, ContentContext ctx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {

		if (!AdminUserSecurity.getInstance().canRole(user, AdminUserSecurity.USER_ROLE)) {
			return "security error.";
		}

		boolean admin = StringHelper.isTrue(rs.getParameter("admin", null));
		IUserFactory userFact;
		if (admin) {
			userFact = AdminUserFactory.createUserFactory(globalContext, session);
		} else {
			userFact = UserFactory.createUserFactory(globalContext, session);
		}

		Collection<FileItem> fileItems = rs.getAllFileItem();
		String msg = null;
		for (FileItem item : fileItems) {
			InputStream in = item.getInputStream();

			if (item.getFieldName().trim().length() > 1 && item.getSize() > 0 && in != null) {

				Charset charset = Charset.forName(ContentContext.CHARACTER_ENCODING);
				if (StringHelper.getFileExtension(item.getName()).equals("txt")) { // hack
					charset = Charset.forName("utf-16");
				}

				CSVFactory csvFact;
				try {
					csvFact = new CSVFactory(in, null, charset);
				} finally {
					ResourceHelper.closeResource(in);
				}
				String[][] usersArrays = csvFact.getArray();
				if (usersArrays == null || usersArrays.length < 1) {
					msg = i18nAccess.getText("global.message.file-format-error");
					MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
					return null;
				} else {
					if (usersArrays[0].length < 5) {
						msg = i18nAccess.getText("global.message.file-format-error");
						MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
						return null;
					}
				}

				Collection<IUserInfo> userInfoList = new LinkedList<IUserInfo>();
				for (int i = 1; i < usersArrays.length; i++) {
					IUserInfo userInfo = userFact.createUserInfos();
					String[] labels = usersArrays[0];
					try {
						BeanHelper.copy(JavaHelper.createMap(labels, usersArrays[i]), userInfo);
						userInfoList.add(userInfo);
					} catch (Exception e) {
						logger.warning("error on : " + userInfo.getLogin() + " : " + e.getMessage());
					}
				}

				// if (userInfoList.size() > 0) {
				userFact.clearUserInfoList();
				for (Object element2 : userInfoList) {
					IUserInfo element = (IUserInfo) element2;
					try {
						userFact.addUserInfo(element);
					} catch (UserAllreadyExistException e) {
						logger.warning("error on : " + element.getLogin() + " : " + e.getMessage());
					}
				}
				userFact.store();
				// }

			}
		}

		/** vrac import **/
		String vrac = rs.getParameter("vrac", "");
		int countUserInsered = 0;
		if (vrac.trim().length() > 0) {
			String role = rs.getParameter("role", "");
			Collection<String> emails = StringHelper.searchEmail(vrac);
			for (String email : emails) {
				IUserInfo userInfo = userFact.createUserInfos();
				userInfo.setLogin(email);
				userInfo.setEmail(email);
				if (role.trim().length() > 0) {
					userInfo.setRoles(new HashSet(Arrays.asList(new String[] { role })));
				}
				try {
					userFact.addUserInfo(userInfo);
					countUserInsered++;
				} catch (UserAllreadyExistException e) {
				}
			}
			userFact.store();

			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(i18nAccess.getText("global.message.imported") + countUserInsered, GenericMessage.INFO));

			logger.info("vrac user imported : " + countUserInsered);
		}

		return msg;
	}

	public static String performSelectRole(HttpServletRequest request, ContentContext ctx, GlobalContext globalContext, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) {
		String role = request.getParameter("role");
		if (role == null) {
			return "bad request structure : need 'role' as parameter.";
		}
		UserModuleContext context = UserModuleContext.getInstance(request);
		context.setCurrentRole(role);
		messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("user.message.ok-change-role") + ' ' + role, GenericMessage.INFO));
		return null;

	}

	public static String performAjaxUserList(RequestService rs, HttpSession session, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory userFactory = userContext.getUserFactory(ctx);

		if (userFactory.getCurrentUser(ctx.getRequest().getSession()) == null || !AdminUserSecurity.getInstance().canRole(userFactory.getCurrentUser(ctx.getRequest().getSession()), AdminUserSecurity.USER_ROLE)) {
			return "no access";
		}

		if (userContext.getCurrentRole() != null && !userFactory.getAllRoles(globalContext, ctx.getRequest().getSession()).contains(userContext.getCurrentRole())) {
			userContext.setCurrentRole(null);
		}

		List<IUserInfo> users = null;
		if (userContext.getCurrentRole() != null) {
			users = new LinkedList<IUserInfo>();
			for (IUserInfo user : userContext.getUserFactory(ctx).getUserInfoList()) {
				if (user.getRoles().contains(userContext.getCurrentRole())) {
					users.add(user);
				}
			}
		} else {
			users = userContext.getUserFactory(ctx).getUserInfoList();
		}

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		int pageSize = Integer.parseInt(rs.getParameter("iDisplayLength", "10"));
		int displayStart = Integer.parseInt(rs.getParameter("iDisplayStart", "0"));
		int sortingCol = Integer.parseInt(rs.getParameter("iSortingCols", "0"));
		boolean ascSorting = rs.getParameter("sSortDir_0",  "asc").equals("asc");
		
		switch (sortingCol) {
		case 1:
			Collections.sort(users, new UserInfoSorting(UserInfoSorting.LOGIN, ascSorting));
			break;
		case 2:
			Collections.sort(users, new UserInfoSorting(UserInfoSorting.FIRSTNAME, ascSorting));
			break;
		case 3:
			Collections.sort(users, new UserInfoSorting(UserInfoSorting.LASTNAME, ascSorting));
			break;
		case 4:
			Collections.sort(users, new UserInfoSorting(UserInfoSorting.EMAIL, ascSorting));
			break;
		case 5:
			Collections.sort(users, new UserInfoSorting(UserInfoSorting.CREATION_DATE, ascSorting));
			break;
		default:
			break;
		}
		
		String query = rs.getParameter("sSearch", null);
		if (query != null && query.trim().length() == 0) {
			query = null;
		}

		out.println("{");
		out.print("\"aaData\": [");
		String sep = "";
		int record = 0;

		for (IUserInfo userInfo : users) {
			if (query == null || StringHelper.arrayToString(userInfo.getAllValues()).contains(query)) {				
				if (record >= displayStart && record < displayStart + pageSize) {
					out.print(sep + '[' + '"' + "<input type=\\\"checkbox\\\" name=\\\""+userInfo.getLogin()+"\\\" />" + '"' + ',');
					Map<String,String> params = new HashMap<String, String>();
					params.put("webaction", "edit");
					params.put("cuser", userInfo.getEncryptLogin());
					String editURL = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE), params);
					out.print('"' + "<a href=\\\""+editURL+"\\\">" + userInfo.getLogin() + "</a>" + '"' + ',');
					out.print('"' + userInfo.getFirstName() + '"' + ',');
					out.print('"' + userInfo.getLastName() + '"' + ',');
					out.print('"' + userInfo.getEmail() + '"' + ',');					
					out.print('"' + StringHelper.renderSortableTime(userInfo.getCreationDate()) + '"' + ']');
					sep = ",";
				}
				record++;
			}
		}
		out.println("],");
		Integer sEcho = (Integer) session.getAttribute("sEcho");
		if (sEcho == null) {
			sEcho = 0;
		}
		sEcho++;
		session.setAttribute("sEcho", sEcho);
		out.println("\"sEcho\": " + sEcho + ",");
		out.println("\"iTotalRecords\": \"" + users.size() + "\",");
		out.println("\"iTotalDisplayRecords\": \"" + record + "\"}");

		out.close();
		String json = new String(outStream.toByteArray());
		ctx.setSpecificJson(json);

		return null;
	}
}
