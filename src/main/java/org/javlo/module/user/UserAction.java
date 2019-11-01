package org.javlo.module.user;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.files.Cell;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.filter.CatchAllFilter;
import org.javlo.helper.ArrayHelper;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.JavaHelper;
import org.javlo.helper.LangHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.RequestParameterMap;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.SecurityHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.ImageEngine;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.DataToIDService;
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
import org.javlo.utils.JSONMap;
import org.javlo.ztatic.FileCache;
import org.javlo.ztatic.StaticInfo;
import org.javlo.ztatic.StaticInfoBean;
import org.owasp.encoder.Encode;

public class UserAction extends AbstractModuleAction {

	public static final String JAVLO_LOGIN_ID = "javlo_login_id";

	public static final String INIT_TOKEN = "initToken";
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
//		if (userFactory.getCurrentUser(globalContext, ctx.getRequest().getSession()) == null) {
//			logger.warning("security error no user found.");
//			return null;
//		}

		if (ctx.getCurrentEditUser() == null) {
			logger.fine("security error no user found.");
			return null;
		}

		if (userContext.getCurrentRole() != null && !userFactory
				.getAllRoles(globalContext, ctx.getRequest().getSession()).contains(userContext.getCurrentRole())) {
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
		if (((requestService.getParameter("user", null) == null && requestService.getParameter("cuser", null) == null)
				|| requestService.getParameter("back", null) != null)
				&& !userContext.getMode().equals(UserModuleContext.VIEW_MY_SELF)) {
			moduleContext.getCurrentModule().restoreAll();
		} else {
			User user = null;
			boolean userSet = false;
			String userName = requestService.getParameter("user", null);
			if (userName != null) {
				userSet = true;
				if (userName.contains("<")) {
					try {
						InternetAddress internetAddress = new InternetAddress(userName);
						userName = internetAddress.getAddress();
					} catch (AddressException e) {
					}
				}
				user = userFactory.getUser(userName);
			} else if (requestService.getParameter("cuser", null) != null) {
				userSet = true;
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
				user = userFactory
						.getUser(userFactory.getCurrentUser(globalContext, ctx.getRequest().getSession()).getLogin());
			}

			if (user == null) {
				if (userSet) {
					return "user not found (prepare) : "
							+ requestService.getParameter("user", requestService.getParameter("cuser", null));
				} else {
					return null;
				}
			}

			Map<String, String> userInfoMap = BeanHelper.bean2Map(user.getUserInfo());
			ctx.getRequest().setAttribute("functions",
					LangHelper.collectionToMap(StringHelper.stringToCollection(userInfoMap.get("function"), ";")));

			ctx.getRequest().setAttribute("user", user);
			ctx.getRequest().setAttribute("userInfoMap", userInfoMap);
			List<String> keys = new LinkedList<String>(userInfoMap.keySet());
			Collections.sort(keys);
			keys.remove("avatarURL");
			keys.remove("encryptLogin");
			keys.remove("rolesRaw");
			ctx.getRequest().setAttribute("userInfoKeys", keys);

			List<StaticInfoBean> files = new LinkedList<StaticInfoBean>();
			File userFolder = new File(ctx.getGlobalContext().getUserFolder(user));
			if (userFolder.isDirectory()) {
				for (File file : userFolder.listFiles()) {
					files.add(new StaticInfoBean(ctx, StaticInfo.getInstance(ctx, file)));
				}
			}
			ctx.getRequest().setAttribute("files", files);

		}

		List<String> roles = new LinkedList<String>(
				userFactory.getAllRoles(globalContext, ctx.getRequest().getSession()));
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
			String CSVLink = URLHelper.createStaticURL(ctx,
					"/users-list/" + globalContext.getContextKey() + "-users.csv");
			if (userContext.getMode().equals(UserModuleContext.ADMIN_USERS_LIST)) {
				CSVLink = URLHelper.createStaticURL(ctx,
						"/users-list/" + globalContext.getContextKey() + "-admin-users.csv");
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

	public String performEdit(Module currentModule)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		currentModule.setToolsRenderer(null);
		currentModule.setRenderer("/jsp/edit.jsp");
		return null;
	}

	public String performUpdateCurrent(ContentContext ctx, GlobalContext globalContext, EditContext editContext,
			RequestService requestService, StaticConfig staticConfig, AdminUserSecurity adminUserSecurity,
			AdminUserFactory adminUserFactory, HttpSession session, Module currentModule, I18nAccess i18nAccess,
			MessageRepository messageRepository) {
		if (requestService.getParameter("ok", null) != null || requestService.getParameter("token", null) != null
				|| requestService.getParameter("notoken", null) != null) {
			UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
			IUserFactory userFactory = userContext.getUserFactory(ctx);
			if (requestService.getParameter("type", "").equals("visitors")) {
				userFactory = UserFactory.createUserFactory(ctx.getRequest());
			}
			User user = userFactory.getUser(requestService.getParameter("user", null));
			if (user == null) {
				return "user not found (update current) : " + requestService.getParameter("user", null);
			}
			IUserInfo userInfo = user.getUserInfo();
			try {

				BeanHelper.copy(new RequestParameterMap(ctx.getRequest()), userInfo,
						StringHelper.isTrue(ctx.getRequestService().getParameter("reset-boolean"), true));

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

				FileItem userFile = requestService.getFileItem("userFile");
				if (userFile != null && userFile.getSize() > 0) {
					InputStream in = null;
					try {
						in = userFile.getInputStream();
						File newFile = new File(
								URLHelper.mergePath(ctx.getGlobalContext().getUserFolder(user), userFile.getName()));
						ResourceHelper.writeStreamToFile(in, newFile);
					} finally {
						ResourceHelper.safeClose(in);
					}
				}
				String avatarFileName = userInfo.getLogin() + ".png";
				File avatarFile = new File(URLHelper.mergePath(globalContext.getDataFolder(),
						staticConfig.getAvatarFolder(), avatarFileName));
				if (StringHelper.isTrue(requestService.getParameter("deleteAvatar", null))) {
					avatarFile.delete();
				}
				FileItem newAvatar = requestService.getFileItem("avatar");
				if (newAvatar != null && newAvatar.getSize() > 0) {
					InputStream in = null;
					try {
						in = newAvatar.getInputStream();
						BufferedImage img = ImageIO.read(in);
						img = ImageEngine.resizeWidth(img, 255, true);
						avatarFile.getParentFile().mkdirs();
						ImageIO.write(img, "png", avatarFile);
					} finally {
						ResourceHelper.safeClose(in);
					}
					FileCache.getInstance(ctx.getRequest().getSession().getServletContext())
							.deleteAllFile(globalContext.getContextKey(), avatarFileName);
				}

				userFactory.updateUserInfo(userInfo);
				userFactory.store();
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}

			messageRepository.setGlobalMessageAndNotification(ctx,
					new GenericMessage(
							i18nAccess.getText("user.message.updated", new String[][] { { "user", user.getLogin() } }),
							GenericMessage.INFO));
		}

		if (editContext.isPreviewEditionMode()) {
			ctx.setClosePopup(true);
		}

		return null;
	}

	public String performUpdate(ContentContext ctx, GlobalContext globalContext, RequestService requestService,
			StaticConfig staticConfig, AdminUserSecurity adminUserSecurity, AdminUserFactory adminUserFactory,
			HttpSession session, Module currentModule, I18nAccess i18nAccess, MessageRepository messageRepository) {
		if (requestService.getParameter("back", null) == null) {
			UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
			IUserFactory userFactory = userContext.getUserFactory(ctx);
			String userName = requestService.getParameter("user", null);
			User user = userFactory.getUser(userName);
			if (user == null) {
				return "user not found (update)  : " + userName;
			}
			IUserInfo userInfo = user.getUserInfo();
			String pwd = user.getPassword();
			try {
				BeanHelper.copy(new RequestParameterMap(ctx.getRequest()), userInfo,
						StringHelper.isTrue(ctx.getRequestService().getParameter("reset-boolean"), true));
				if (!userInfo.getPassword().equals(pwd)) {
					userInfo.setPassword(SecurityHelper.encryptPassword(userInfo.getPassword()));
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
				if (!admin
						|| adminUserSecurity.haveRight(adminUserFactory.getCurrentUser(ctx.getRequest().getSession()),
								AdminUserSecurity.ADMIN_USER_ROLE, AdminUserSecurity.GENERAL_ADMIN)) {
					ui.setRoles(newRoles);
					userFactory.updateUserInfo(ui);
				} else {
					newRoles.removeAll(ui.getRoles());
					if (newRoles.size() > 0) {
						messageRepository.setGlobalMessageAndNotification(ctx,
								new GenericMessage(i18nAccess.getText("global.message.noright"), GenericMessage.ERROR));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}
			messageRepository.setGlobalMessageAndNotification(ctx,
					new GenericMessage(
							i18nAccess.getText("user.message.updated", new String[][] { { "user", user.getLogin() } }),
							GenericMessage.INFO));
		}
		return null;
	}

	public String performCreateuserwidthemail(ContentContext ctx, StaticConfig staticConfig,
			RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository)
			throws IOException, Exception {
		String newUser = requestService.getParameter("email", null);
		if (!StringHelper.isMail(newUser)) {
			return i18nAccess.getViewText("mailing.error.email");
		}
		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
		IUserFactory userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		if (userFactory.getUserByEmail(newUser) != null) {
			return i18nAccess.getViewText("registration.error.email_alreadyexist");
		} else {
			GlobalContext gc = ctx.getGlobalContext();
			String title = i18nAccess.getViewText("registration.mail.confirm.title");
			String subject = i18nAccess.getViewText("registration.mail.confirm.subject") + ctx.getCurrentPage().getGlobalTitle(ctx);
			String text = i18nAccess.getViewText("registration.mail.confirm.text");
			String actionLabel = i18nAccess.getViewText("registration.mail.confirm.action");
			ContentService contentService = ContentService.getInstance(ctx.getGlobalContext());
			MenuElement regPage = contentService.getRegistrationPage(ctx);
			String actionUrl = URLHelper.createURL(ctx.getContextForAbsoluteURL());
			if (regPage != null) {
				actionUrl = URLHelper.createURL(ctx.getContextForAbsoluteURL(), regPage);
			}
			actionUrl = URLHelper.addParam(actionUrl, INIT_TOKEN, ctx.getGlobalContext().createEmailToken(newUser));
			String mail = XHTMLHelper.createUserMail(ctx, "/images/font/user-plus.png", title, text, actionUrl,
					actionLabel, "");
			NetHelper.sendMail(gc, new InternetAddress(gc.getAdministratorEmail()), new InternetAddress(newUser), null,
					null, subject, mail, null, true);
			messageRepository.setGlobalMessageAndNotification(ctx,
					new GenericMessage(i18nAccess.getViewText("registration.message.mail-send"), GenericMessage.INFO));
		}
		return null;
	}

	public String performCreateUser(ContentContext ctx, StaticConfig staticConfig, RequestService requestService,
			I18nAccess i18nAccess, MessageRepository messageRepository) {
		String newUser = requestService.getParameter("user", null);
		if (StringHelper.isEmpty(newUser)) {
			newUser = requestService.getParameter("login", null);
			if (newUser == null) {
				return "bad request structure : need 'user' as parameter for create a new user.";
			}
		}
		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
		String pwd1 = requestService.getParameter("password", null);
		String pwd2 = requestService.getParameter("password2", null);
		if (pwd1 != null) {
			if (!pwd1.equals(pwd2)) {
				return i18nAccess.getViewText("create-context.msg.error.pwd-same");
			} else if (pwd1.length() < 4) {
				return i18nAccess.getViewText("create-context.msg.error.pwd-size");
			}
		}
		IUserFactory userFactory = userContext.getUserFactory(ctx);
		if (requestService.getParameter("type", "").equals("visitors")) {
			userFactory = UserFactory.createUserFactory(ctx.getRequest());
		} else if (requestService.getParameter("type", "").equals("edit")) {
			userFactory = AdminUserFactory.createUserFactory(ctx.getRequest());
		}

		IUserInfo newUserInfo = userFactory.createUserInfos();

		if (newUser.contains("<")) {
			try {
				InternetAddress internetAddress = new InternetAddress(newUser);
				newUser = internetAddress.getAddress();
				String personal = StringHelper.neverNull(internetAddress.getPersonal()).trim();
				if (personal.contains(" ")) {
					newUserInfo.setLastName(personal.substring(personal.indexOf(' ') + 1));
					personal = personal.substring(0, personal.indexOf(' '));
				}
				newUserInfo.setFirstName(personal);
			} catch (AddressException e) {
			}
		}
		newUserInfo.setId(newUser);
		newUserInfo.setLogin(newUser);
		newUserInfo.setSite(ctx.getGlobalContext().getContextKey());
		if (StringHelper.isMail(newUser)) {
			newUserInfo.setEmail(newUser);
		}

		for (String label : newUserInfo.getAllLabels()) {
			String val = requestService.getParameter(label, null);
			if (val != null) {
				BeanHelper.setProperty(newUserInfo, label, val);
			}
		}

		if (pwd1 != null) {
			newUserInfo.setPassword(SecurityHelper.encryptPassword(pwd1));
		}

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
			messageRepository.setGlobalMessage(
					new GenericMessage(i18nAccess.getViewText("user.error.allready-exist"), GenericMessage.ERROR));
			return null;
		}

		messageRepository.setGlobalMessageAndNotification(ctx,
				new GenericMessage(i18nAccess.getViewText("registration.message.registred"), GenericMessage.INFO));
		ctx.getRequest().setAttribute("hideForm", true);

		return null;
	}

	public String performDeleteUser(ContentContext ctx, RequestService requestService, I18nAccess i18nAccess,
			MessageRepository messageRepository) throws UserAllreadyExistException {
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
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(
					i18nAccess.getText("user.message.delete", new String[][] { { "deletedUser", "" + deletedUser } }),
					GenericMessage.INFO));
		} else {
			messageRepository.setGlobalMessage(
					new GenericMessage(i18nAccess.getText("user.message.no-delete"), GenericMessage.ALERT));
		}

		return null;

	}

	public static String performAskChangePassword(RequestService rs, ContentContext ctx, EditContext editContext,
			GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String email = rs.getParameter("email", null);
		if (email == null && rs.getParameter("j_username", null) != null) {
			email = rs.getParameter("j_username", null);
			rs.removeParameter("j_username");
			messageRepository.clearGlobalMessage();
		}
		if (!StringHelper.isMail(email)) {
			return i18nAccess.getText("mailing.error.email");
		} else {
			IUserFactory userFactory = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
			User user = userFactory.getUser(email);
			if (user == null) {
				user = userFactory.getUserByEmail(email);
			}
			if (user == null) {
				userFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
				user = userFactory.getUser(email);
			}
			if (user == null) {
				userFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
				user = userFactory.getUserByEmail(email);
			}
			if (user == null) {
				return i18nAccess.getViewText("user.message.error.change-mail-not-found");
			} else {
				String subject = i18nAccess.getViewText("user.message.change-password.email-subject") + ' '	+ globalContext.getGlobalTitle();
				String body = i18nAccess.getViewText("user.message.change-password.email-body");
				Map<String, String> params = new HashMap<String, String>();
				params.put("pwtoken", globalContext.getChangePasswordToken(user.getLogin()));
				params.put("hideform", "true");
				ContentService contentService = ContentService.getInstance(globalContext);
				MenuElement regPage = contentService.getRegistrationPage(ctx);
				String link = URLHelper.createURL(ctx.getContextForAbsoluteURL(), regPage, params);
				// String mailBody = XHTMLHelper.createUserMail(globalContext.getTemplateData(),
				// body, null, null, link, i18nAccess.getViewText("user.change-password"),
				// null);
				String mailBody = XHTMLHelper.createUserMail(ctx, "/images/font/lock.png", body, "", link,
						i18nAccess.getViewText("user.change-password"), "");

				MailService mailService = MailService
						.getInstance(new MailConfig(globalContext, globalContext.getStaticConfig(), null));
				mailService.sendMail(globalContext, new InternetAddress(globalContext.getAdministratorEmail()),
						new InternetAddress(email), subject, mailBody, true);
				messageRepository.setGlobalMessage(new GenericMessage(
						i18nAccess.getViewText("user.message.change-password-link"), GenericMessage.INFO));
			}
		}

		return null;
	}

	public static String performChangePasswordWithToken(RequestService rs, ContentContext ctx, EditContext editContext,
			GlobalContext globalContext, HttpSession session, StaticConfig staticConfig,
			MessageRepository messageRepository, I18nAccess i18nAccess) {
		String token = rs.getParameter("token", null);
		String pwd1 = rs.getParameter("password", null);
		String pwd2 = rs.getParameter("password2", null);
		String userName = globalContext.getChangePasswordTokenUser(token);

		if (userName == null) {
			return i18nAccess.getViewText("user.message.password-bad-token");
		} else {
			if (pwd1 == null || pwd1.length() < 4) {
				messageRepository.setGlobalMessage(
						new GenericMessage(i18nAccess.getText("user.message.password-to-short"), GenericMessage.ERROR));
			} else {
				if (!pwd1.equals(pwd2)) {
					return i18nAccess.getViewText("login.message.password-not-same");
				}
				try {
					IUserFactory userFactory = UserFactory.createUserFactory(globalContext, session);
					User user = userFactory.getUser(userName);
					if (user == null) {
						user = userFactory.getUserByEmail(userName);
					}
					if (user == null) {
						user = userFactory.getUser(userName);
					}
					if (user == null) {
						userFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
						user = userFactory.getUser(userName);
					}
					if (user == null) {
						userFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
						user = userFactory.getUserByEmail(userName);
					}
					if (user == null) {
						return "user not found : " + userName;
					}
					logger.info("change password of : " + user.getLogin());
					IUserInfo ui = user.getUserInfo();
					ui.setPassword(SecurityHelper.encryptPassword(pwd1));
					userFactory.updateUserInfo(ui);
					userFactory.store();
					userFactory.reload(globalContext, session);
					messageRepository.setGlobalMessage(new GenericMessage(
							i18nAccess.getViewText("user.message.ok-change-password"), GenericMessage.INFO));
					if (editContext.isPreviewEditionMode()) {
						ctx.setClosePopup(true);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return e.getMessage();
				}
			}
		}
		return null;
	}

	public static String performChangePassword(RequestService rs, ContentContext ctx, EditContext editContext,
			GlobalContext globalContext, HttpSession session, StaticConfig staticConfig,
			MessageRepository messageRepository, I18nAccess i18nAccess) {
		String pwd = rs.getParameter("password", null);
		String newPwd = rs.getParameter("newpassword", null);

		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
		IUserFactory userFactory = userContext.getUserFactory(ctx);
		User user = userFactory.getCurrentUser(globalContext, session);

		pwd = StringHelper.encryptPassword(pwd);

		if (user.getPassword().equals(pwd)) {
			if (newPwd == null || newPwd.length() < 4) {
				messageRepository.setGlobalMessage(
						new GenericMessage(i18nAccess.getText("user.message.password-to-short"), GenericMessage.ERROR));
			} else {
				IUserInfo ui = user.getUserInfo();
				ui.setPassword(SecurityHelper.encryptPassword(newPwd));
				try {
					userFactory.updateUserInfo(ui);
					userFactory.store();
					userFactory.reload(globalContext, session);
					messageRepository.setGlobalMessage(new GenericMessage(
							i18nAccess.getText("user.message.ok-change-password"), GenericMessage.INFO));

					if (editContext.isPreviewEditionMode()) {
						ctx.setClosePopup(true);
					}

				} catch (IOException e) {
					e.printStackTrace();
					return e.getMessage();
				}
			}
		} else {
			messageRepository.setGlobalMessage(
					new GenericMessage(i18nAccess.getText("user.message.bad-password"), GenericMessage.ERROR));
		}

		return null;
	}

	public static String performChangePassword2Check(RequestService rs, GlobalContext globalContext,
			HttpSession session, ContentContext ctx, StaticConfig staticConfig, MessageRepository messageRepository,
			I18nAccess i18nAccess) throws IOException {
		String newPwd = rs.getParameter("newpassword", "");
		String newPwd2 = rs.getParameter("newpassword2", null);

		Pattern validPassword = Pattern.compile(staticConfig.getPasswordRegularExpression());

		if (!validPassword.matcher(newPwd).matches()) {
			return i18nAccess.getViewText("login.password.error");
		} else {
			if (!newPwd.equals(newPwd2)) {
				return i18nAccess.getViewText("login.message.password-not-same");
			} else {
				IUserFactory userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), session);
				User user = userFactory.getCurrentUser(globalContext, session);
				IUserInfo ui = user.getUserInfo();
				newPwd = SecurityHelper.encryptPassword(newPwd);
				ui.setPassword(SecurityHelper.encryptPassword(newPwd));
				userFactory.updateUserInfo(ui);
				userFactory.store();
				userFactory.reload(ctx.getGlobalContext(), session);
				messageRepository.setGlobalMessage(
						new GenericMessage(i18nAccess.getText("user.message.ok-change-password"), GenericMessage.INFO));
				ctx.getRequest().setAttribute("passwordChanged", true);
			}
		}
		return null;
	}

	public static String performToken(HttpServletRequest request, ContentContext ctx, GlobalContext globalContext,
			HttpSession session) {
		UserFactory factory = AdminUserFactory.createAdminUserFactory(globalContext, session);
		User user = factory.getCurrentUser(globalContext, session);
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

	private static Integer getFieldPos(Cell[] cells, String field) {
		if (field != null) {
			int p = 0;
			int maxRowSpan = 1;
			for (Cell title : cells) {
				if (maxRowSpan < title.getRowSpan()) {
					maxRowSpan = title.getRowSpan();
				}

			}
			for (Cell title : cells) {
				Cell finalTitle = title;

				if (title != null && title.getValue() != null
						&& title.getValue().toLowerCase().contains(field.toLowerCase())) {
					return p;
				}
				p++;
			}
		}
		return null;
	}

	public static String performUpload(RequestService rs, HttpSession session, User user, ContentContext ctx,
			GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		if (!AdminUserSecurity.getInstance().canRole(user, AdminUserSecurity.USER_ROLE)) {
			return "security error.";
		}

		boolean admin = StringHelper.isTrue(rs.getParameter("admin", null));
		boolean merge = StringHelper.isTrue(rs.getParameter("merge", null));

		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());

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
				List<IUserInfo> newUserInfo = new LinkedList<IUserInfo>();
				if (StringHelper.getFileExtension(item.getName()).equals("xlsx")) {
					XSSFWorkbook excel = ArrayHelper.loadWorkBook(in);
					ResourceHelper.closeResource(in);
					Cell[][] cells = ArrayHelper.getXLSXArray(ctx, excel, excel.getActiveSheetIndex());
					for (int i = 1; i < cells.length; i++) {
						IUserInfo userInfo = userFact.createUserInfos();
						for (String label : userInfo.getAllLabels()) {
							Integer labelPos = getFieldPos(cells[0], label);
							if (labelPos != null && cells[i][labelPos] != null) {
								userInfo.setValue(label, cells[i][labelPos].getValue());
							}
						}
						if (userInfo.getEmail() != null && !StringHelper.isMail(userInfo.getEmail())) {
							Collection<String> emails = StringHelper.searchEmail(userInfo.getEmail());
							if (emails.size() > 0) {
								userInfo.setEmail(emails.iterator().next());
							} else {
								userInfo.setEmail("");
							}
						}
						if (StringHelper.isEmpty(userInfo.getLogin()) && StringHelper.isMail(userInfo.getEmail())) {
							userInfo.setLogin(userInfo.getEmail());
						}
						if (!StringHelper.isEmpty(userInfo.getLogin())) {
							Collection<String> emails = StringHelper.searchEmail(userInfo.getLogin());
							if (emails.size() > 0) {
								userInfo.setLogin(emails.iterator().next());
							}
							if (!StringHelper.isEmpty(userContext.getCurrentRole())) {
								userInfo.addRoles(
										new HashSet(Arrays.asList(new String[] { userContext.getCurrentRole() })));
							}
							if (!StringHelper.isEmpty(userInfo.getPassword())) {
								userInfo.setPassword(StringHelper.encryptPassword(userInfo.getPassword()));
							}
							newUserInfo.add(userInfo);
						}
					}
					if (newUserInfo.size() == 0) {
						return "error : no valid users found in excel file.";
					} else {
						if (!merge) {
							userFact.releaseUserInfoList();
						}
						for (IUserInfo userInfo : newUserInfo) {
							userFact.mergeUserInfo(userInfo);
						}
					}
				} else {

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
						MessageRepository.getInstance(ctx)
								.setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
						return null;
					} else {
						if (usersArrays[0].length < 5) {
							msg = i18nAccess.getText("global.message.file-format-error");
							MessageRepository.getInstance(ctx)
									.setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
							return null;
						}
					}

					Collection<IUserInfo> userInfoList = new LinkedList<IUserInfo>();
					for (int i = 1; i < usersArrays.length; i++) {
						IUserInfo userInfo = userFact.createUserInfos();
						String[] labels = usersArrays[0];
						try {
							BeanHelper.copy(JavaHelper.createMap(labels, usersArrays[i]), userInfo, false);
							userInfoList.add(userInfo);
						} catch (Exception e) {
							logger.warning("error on : " + userInfo.getLogin() + " : " + e.getMessage());
						}
					}

					// if (userInfoList.size() > 0) {
					if (!merge) {
						userFact.clearUserInfoList();
					}
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
		}
		userFact.store();
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
					userFact.addOrModifyUserInfo(userInfo);
					countUserInsered++;
				} catch (UserAllreadyExistException e) {
				}
			}
			userFact.store();

			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(
					i18nAccess.getText("global.message.imported") + countUserInsered, GenericMessage.INFO));

			logger.info("vrac user imported : " + countUserInsered);
		}

		return msg;
	}

	public static String performSelectRole(HttpServletRequest request, ContentContext ctx, GlobalContext globalContext,
			HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		String role = request.getParameter("role");

		boolean admin = StringHelper.isTrue(request.getParameter("admin"));

		IUserFactory userFact;
		if (admin) {
			userFact = AdminUserFactory.createUserFactory(globalContext, session);
		} else {
			userFact = UserFactory.createUserFactory(globalContext, session);
		}

		Set<String> roleSet = new HashSet<String>();
		roleSet.add(role);
		if (!StringHelper.isEmpty(request.getParameter("remove"))) {
			for (IUserInfo user : userFact.getUserInfoList()) {
				user.removeRoles(roleSet);
				userFact.updateUserInfo(user);
				messageRepository.setGlobalMessage(new GenericMessage(
						i18nAccess.getText("user.message.ok-remove-role", "Role removed from all users : ") + ' '
								+ role,
						GenericMessage.INFO));
			}
		} else if (!StringHelper.isEmpty(request.getParameter("add"))) {
			for (IUserInfo user : userFact.getUserInfoList()) {
				user.addRoles(roleSet);
				userFact.updateUserInfo(user);
				messageRepository.setGlobalMessage(new GenericMessage(
						i18nAccess.getText("user.message.ok-add-role", "Role added to all users : ") + ' ' + role,
						GenericMessage.INFO));
			}
		} else {

			if (role == null) {
				return "bad request structure : need 'role' as parameter.";
			}
			UserModuleContext context = UserModuleContext.getInstance(request);
			context.setCurrentRole(role);
			messageRepository.setGlobalMessage(new GenericMessage(
					i18nAccess.getText("user.message.ok-change-role") + ' ' + role, GenericMessage.INFO));

		}
		return null;

	}

	public static String performAjaxUserList(RequestService rs, HttpSession session, ContentContext ctx,
			MessageRepository messageRepository, I18nAccess i18nAccess) {
		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (!AdminUserSecurity.getInstance().canRole(ctx.getCurrentUser(), AdminUserSecurity.USER_ROLE)) {
			return "no access";
		}

//		IUserFactory userFactory = AdminUserFactory.createUserFactory(ctx.getRequest());
//		if (userContext.getCurrentRole() != null && !userFactory.getAllRoles(globalContext, ctx.getRequest().getSession()).contains(userContext.getCurrentRole())) {
//			userContext.setCurrentRole(null);
//		}

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
		int sortingCol = Integer.parseInt(rs.getParameter("iSortCol_0", "0"));
		boolean ascSorting = rs.getParameter("sSortDir_0", "asc").equals("asc");

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
					out.print(sep + '[' + '"' + "<input type=\\\"checkbox\\\" name=\\\""
							+ Encode.forJavaScriptAttribute(userInfo.getLogin()) + "\\\" />" + '"' + ',');
					Map<String, String> params = new HashMap<String, String>();
					params.put("webaction", "edit");
					params.put("cuser", userInfo.getEncryptLogin());
					String editURL = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE),
							params);
					out.print('"' + "<a href=\\\"" + editURL + "\\\">"
							+ Encode.forJavaScriptAttribute(userInfo.getLogin()) + "</a>" + '"' + ',');
					out.print('"' + Encode.forJavaScriptAttribute(userInfo.getFirstName()) + '"' + ',');
					out.print('"' + Encode.forJavaScriptAttribute(userInfo.getLastName()) + '"' + ',');
					out.print('"' + Encode.forJavaScriptAttribute(userInfo.getEmail()) + '"' + ',');
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

	public static String performDeleteFile(RequestService rs, User user, ContentContext ctx,
			MessageRepository messageRepository, I18nAccess i18nAccess) {
		File userFolder = new File(ctx.getGlobalContext().getUserFolder(user));
		String fileName = rs.getParameter("name", null);
		if (userFolder.isDirectory()) {
			for (File file : userFolder.listFiles()) {
				if (file.getName().equals(fileName)) {
					file.delete();
				}
			}
		}
		messageRepository.setGlobalMessage(
				new GenericMessage(i18nAccess.getText("global.delete-file", "file deleted."), GenericMessage.INFO));
		return null;
	}

	public static String performLogin(ContentContext ctx, HttpServletRequest request, RequestService rs,
			GlobalContext globalContext, I18nAccess i18nAccess) throws Exception {
	IUserFactory fact = UserFactory.createUserFactory(request);
		User loggedUser = ctx.getCurrentUser(); // for return current user information withtout login
		String login = request.getParameter("login");
		if (fact.getCurrentUser(globalContext, request.getSession()) == null) {			
			if (login != null || request.getUserPrincipal() != null) {
				if (request.getParameter("autologin") != null) {
					DataToIDService service = DataToIDService.getInstance(request.getSession().getServletContext());
					String codeId = service.setData(login, IUserFactory.AUTO_LOGIN_AGE_SEC);
					RequestHelper.setCookieValue(ctx.getResponse(), JAVLO_LOGIN_ID, codeId,
							IUserFactory.AUTO_LOGIN_AGE_SEC, null);
				}
				if (login == null && request.getUserPrincipal() != null) {
					login = request.getUserPrincipal().getName();
				} else if (fact.login(request, login, request.getParameter("password")) == null) {
					String msg = i18nAccess.getText("user.error.msg");
					MessageRepository messageRepository = MessageRepository.getInstance(((HttpServletRequest) request));
					messageRepository.setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
				} else {
					ContentService.getInstance(globalContext).releaseViewNav(globalContext);
				}
				ModulesContext.getInstance(request.getSession(), globalContext).loadModule(request.getSession(),
						globalContext);
				loggedUser = fact.getCurrentUser(globalContext, ((HttpServletRequest) request).getSession());
			}
		}
		Map<String, Object> data = new HashMap<>();
		if (loggedUser != null) {
			globalContext.addPrincipal(loggedUser);
			globalContext.eventLogin(loggedUser.getLogin());
			data.put("logged", true);
			data.put("login", loggedUser.getUserInfo().getLogin());
			data.put("firstname", loggedUser.getUserInfo().getFirstName());
			data.put("lastname", loggedUser.getUserInfo().getLastName());
			data.put("email", loggedUser.getUserInfo().getEmail());
			data.put("message", i18nAccess.getViewText("login.done"));
		} else {
			data.put("logged", false);
			if (login != null) {
				data.put("error", i18nAccess.getViewText("login.error"));
			}
		}
		RequestHelper.setJSONType(ctx.getResponse());
		JSONMap.JSON.toJson(data, ctx.getResponse().getWriter());	
		return null;
	}
	
	public static String performLogout(ContentContext ctx, HttpServletRequest request, RequestService rs,
			GlobalContext globalContext, I18nAccess i18nAccess) throws Exception {
		IUserFactory fact = UserFactory.createUserFactory(request);
		if (fact.getCurrentUser(globalContext, ctx.getRequest().getSession()) != null) {
			fact.logout(ctx.getRequest().getSession());
		} else {
			fact = AdminUserFactory.createUserFactory(request);
			if (fact.getCurrentUser(globalContext, ctx.getRequest().getSession()) != null) {
				fact.logout(ctx.getRequest().getSession());
			}
		}		
		return null;
	}

	public static void main(String[] args) throws Exception {
		List<IUserInfo> newUserInfo = new LinkedList<IUserInfo>();
		InputStream in = new FileInputStream(new File("c:/trans/bdf.xlsx"));
		XSSFWorkbook excel = ArrayHelper.loadWorkBook(in);
		in.close();
		for (int sheet = 0; sheet < excel.getNumberOfSheets(); sheet++) {
			System.out.println(">>>>>>>>> UserAction.performUpload : EXCEL FILE - " + sheet); // TODO: remove debug
																								// trace
			Cell[][] cells = ArrayHelper.getXLSXArray(null, excel, sheet);

			System.out.println("titles : " + ArrayHelper.getTitles(cells));

			System.out.println(">>>>>>>>> UserAction.performUpload : #cells : " + cells.length); // TODO: remove debug
																									// trace
			System.out.println(">>>>>>>>> UserAction.performUpload : #cells[0] : " + cells[0].length); // TODO: remove
																										// debug trace
			for (int i = 1; i < cells.length; i++) {
				String label = "rolesRaw";
				Integer labelPos = getFieldPos(cells[0], label);
				System.out.println(">>>>>>>>> UserAction.main : labelPos = " + labelPos); // TODO: remove debug trace
				if (labelPos != null && cells[i][labelPos] != null) {
					System.out.println(">>>>>>>>> UserAction.performUpload : add : label=" + label + "   value="
							+ cells[i][labelPos].getValue()); // TODO: remove debug trace
				}
			}
			if (newUserInfo.size() == 0) {
				System.out.println("error : no valid users found in excel file.");
			}
		}
	}
}
