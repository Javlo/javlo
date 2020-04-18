package org.javlo.component.users;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.MapComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.LangHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.RequestParameterMap;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.SecurityHelper;
import org.javlo.helper.ServletHelper;
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
import org.javlo.module.ecom.DeliveryPrice;
import org.javlo.service.ListService;
import org.javlo.service.RequestService;
import org.javlo.service.social.Facebook;
import org.javlo.service.social.SocialService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserInfo;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfo;
import org.javlo.utils.CollectionAsMap;
import org.javlo.ztatic.FileCache;
import org.javlo.ztatic.StaticInfo;
import org.javlo.ztatic.StaticInfoBean;

public class UserRegistration extends MapComponent implements IAction {

	private static final String ADMIN = "administrators";

	public static final String TYPE = "user-registration";

	public static final String FIELD_SCOPE = "scope";

	public static final String FIELD_SELECTED_ROLES = "roles";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	protected void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		if (getValue().trim().length() == 0) {
			setValue(ADMIN); // admin registration by default.
		}
	}

	@Override
	public String getRenderer(ContentContext ctx) {
		if (getValue().equals(ADMIN)) {
			return null;
		} else {
			return super.getRenderer(ctx);
		}
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);

		SocialService.getInstance(ctx).prepare(ctx);

		DeliveryPrice deliveryPrice = DeliveryPrice.getInstance(ctx);
		if (deliveryPrice != null) {
			ListService.getInstance(ctx).addList("countries", deliveryPrice.getZone());
		}

		if (ctx.getCurrentUser() != null) {
			ctx.getRequest().setAttribute("user", ctx.getCurrentUser());
			ctx.getRequest().setAttribute("userInfoMap", ctx.getCurrentUser().getUserInfo());
			ctx.getRequest().setAttribute("functions", new CollectionAsMap(StringHelper.stringToCollection(((UserInfo) ctx.getCurrentUser().getUserInfo()).getFunction(), ",")));
			List<StaticInfoBean> files = new LinkedList<StaticInfoBean>();
			if (ctx.getCurrentUser() != null) {
				String fileDir = ctx.getGlobalContext().getUserFolder(ctx.getCurrentUser().getUserInfo());
				if (fileDir != null && new File(fileDir).listFiles() != null) {
					for (File file : new File(fileDir).listFiles()) {
						files.add(new StaticInfoBean(ctx, StaticInfo.getInstance(ctx, file)));
					}
				}
			}
			ctx.getRequest().setAttribute("files", files);
		} else {
			RequestService requestService = RequestService.getInstance(ctx.getRequest());
			ctx.getRequest().setAttribute("userInfoMap", requestService.getParameterMap());
		}

	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		if (ctx.getRequest().getAttribute("registration-message") == null) {
			Module userModule = ModulesContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext()).searchModule("users");
			if (userModule == null) {
				return "<div class=\"error\">error : user module not found.</div>";
			}
			i18nAccess.setCurrentModule(ctx.getGlobalContext(), ctx.getRequest().getSession(), userModule);
			ctx.getRequest().setAttribute("webaction", "user-registration.register");

			AdminUserInfo userInfo = new AdminUserInfo();
			RequestService rs = RequestService.getInstance(ctx.getRequest());
			List<String> functions = rs.getParameterListValues("function", Collections.EMPTY_LIST);
			if (functions.size() > 0 && userInfo instanceof AdminUserInfo) {
				((AdminUserInfo) userInfo).setFunction(StringHelper.collectionToString(functions, ";"));
			}
			ctx.getRequest().setAttribute("functions", LangHelper.collectionToMap(functions));

			String jsp = "/modules/users/jsp/edit_current.jsp";

			return ServletHelper.executeJSP(ctx, jsp);
		} else {
			return "<div class=\"message info\">" + ctx.getRequest().getAttribute("registration-message") + "</div>";
		}

	}

	@Override
	public String getActionGroupName() {
		return "user-registration";
	}

	public static String performUpdate(RequestService rs, GlobalContext globalContext, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		UserRegistration comp = (UserRegistration) ComponentHelper.getComponentFromRequest(ctx);

		IUserFactory userFactory;
		if (comp == null || comp.isAdminRegistration()) {
			userFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		} else {
			userFactory = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		}
		IUserInfo userInfo = userFactory.getCurrentUser(globalContext, session).getUserInfo();
		BeanHelper.copy(new RequestParameterMap(ctx.getRequest()), userInfo, StringHelper.isTrue(rs.getParameter("reset-boolean"), true));
		userFactory.updateUserInfo(userInfo);
		userFactory.store();

		uploadFile(ctx);

		messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("registration.message.update", "User info is updated."), GenericMessage.INFO));

		return null;

	}

	protected static void uploadFile(ContentContext ctx) throws IOException {
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		FileItem userFile = rs.getFileItem("userFile");
		if (userFile != null && userFile.getSize() > 0) {
			InputStream in = null;
			try {
				in = userFile.getInputStream();
				File newFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getUserFolder(ctx.getCurrentUser().getUserInfo()), userFile.getName()));
				newFile.getParentFile().mkdirs();
				ResourceHelper.writeStreamToFile(in, newFile);
			} finally {
				ResourceHelper.safeClose(in);
			}
		}
		String avatarFileName = ctx.getCurrentUserId() + ".png";
		File avatarFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getAvatarFolder(), avatarFileName));
		if (StringHelper.isTrue(rs.getParameter("deleteAvatar", null))) {
			avatarFile.delete();
		}
		FileItem newAvatar = rs.getFileItem("avatar");
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
			FileCache.getInstance(ctx.getRequest().getSession().getServletContext()).deleteAllFile(ctx.getGlobalContext().getContextKey(), avatarFileName);
		}
	}

	public static String performRegister(RequestService rs, GlobalContext globalContext, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		UserRegistration comp = (UserRegistration) ComponentHelper.getComponentFromRequest(ctx);

		IUserFactory userFactory;
		IUserInfo userInfo;

		if (comp == null || comp.isAdminRegistration()) {
			userFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
			userInfo = new AdminUserInfo();
		} else {
			userFactory = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
			userInfo = userFactory.createUserInfos();
		}

		String login = rs.getParameter("login", "").trim();
		String email = rs.getParameter("email", "").trim();

		String emailLogin = rs.getParameter("email-login", null);
		if (emailLogin != null) {
			login = emailLogin;
			email = emailLogin;
		}

		String password = rs.getParameter("password", "").trim();
		String password2 = rs.getParameter("password2", "").trim();
		ctx.getRequest().setAttribute("userInfoMap", new RequestParameterMap(ctx.getRequest()));

		if (login.length() < 3) {
			return i18nAccess.getViewText("registration.error.login_size", "login must be at least 3 characters.");
		} else if (userFactory.getUser(login) != null) {
			return i18nAccess.getViewText("registration.error.login_allreadyexist", "user already exists : ");
		} else if (!password.equals(password2)) {
			return i18nAccess.getViewText("registration.error.password_notsame", "2 passwords must be the same.");
		} else if (password.length() < 3) {
			return i18nAccess.getViewText("registration.error.password_size", "password must be at least 3 characters.");
		} else if (!PatternHelper.MAIL_PATTERN.matcher(email).matches()) {
			if (!comp.getRenderer(ctx).contains("anonym") && email.length()>0) {
				return i18nAccess.getViewText("registration.error.password_size", "Please enter a valid email.");
			}
		}

		List<String> functions = rs.getParameterListValues("function", Collections.EMPTY_LIST);
		if (functions.size() > 0 && userInfo instanceof AdminUserInfo) {
			((AdminUserInfo) userInfo).setFunction(StringHelper.collectionToString(functions, ";"));
		}
		try {
			BeanHelper.copy(new RequestParameterMap(ctx.getRequest()), userInfo, StringHelper.isTrue(rs.getParameter("reset-boolean"), true));
			if (emailLogin != null) {
				userInfo.setLogin(emailLogin);
				userInfo.setEmail(emailLogin);				
			}

			userInfo.setSite(ctx.getGlobalContext().getContextKey());
			userInfo.setPassword(SecurityHelper.encryptPassword(userInfo.getPassword()));

			userInfo.addRoles(new HashSet<String>(comp.getFieldList(FIELD_SELECTED_ROLES)));
			userFactory.addUserInfo(userInfo);
			userFactory.store();

			ctx.getRequest().setAttribute("registration-message", i18nAccess.getViewText("registration.message.registred", "Thanks for you registration.")); // depreciate
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("registration.message.registred", "Thanks for you registration."), GenericMessage.INFO));

			uploadFile(ctx);

			MailService mailService = MailService.getInstance(new MailConfig(globalContext, globalContext.getStaticConfig(), null));
			InternetAddress newUser = new InternetAddress(userInfo.getEmail());
			InternetAddress admin = new InternetAddress(globalContext.getAdministratorEmail());

			Map<String, String> mapMailData = new HashMap<String, String>();

			mapMailData.put(i18nAccess.getViewText("form.login"), userInfo.getLogin());
			mapMailData.put(i18nAccess.getViewText("form.firstName"), userInfo.getFirstName());
			mapMailData.put(i18nAccess.getViewText("form.lastName"), userInfo.getLastName());
			mapMailData.put(i18nAccess.getViewText("form.email"), userInfo.getEmail());
			mapMailData.put(i18nAccess.getViewText("form.address.country"), userInfo.getCountry());
			if (userInfo.getOrganization().trim().length() > 0) {
				mapMailData.put(i18nAccess.getViewText("form.organization"), userInfo.getOrganization());
			}

			String mailAdminContent = XHTMLHelper.createAdminMail(ctx.getCurrentPage().getTitle(ctx), "Registration on : " + globalContext.getGlobalTitle(), mapMailData, URLHelper.createURL(ctx.getContextForAbsoluteURL().getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE)), "go on page >>", null);

			mailService.sendMail(admin, admin, "new user : " + userInfo.getLogin(), mailAdminContent, true);
			mailService.sendMail(admin, newUser, i18nAccess.getViewText("user.new-account") + globalContext.getGlobalTitle(), mailAdminContent, true);

			ctx.getRequest().setAttribute("noform", "true");
		} catch (Exception e) {
			logger.severe("error on " + ctx.getGlobalContext().getContextKey() + " login:" + login);
			e.printStackTrace();
			return i18nAccess.getViewText("global.technical-error");
		}

		return null;
	}

	public static String performChangePassword(RequestService rs, ContentContext ctx, GlobalContext globalContext, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		UserRegistration comp = (UserRegistration) ComponentHelper.getComponentFromRequest(ctx);

		UserFactory userFactory;
		if (comp == null || comp.isAdminRegistration()) {
			userFactory = (UserFactory) AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		} else {
			userFactory = (UserFactory) UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		}

		if (rs.getParameter("logout", null) != null) {
			userFactory.logout(session);
		} else {
			String password = rs.getParameter("newpassword1", "").trim();
			String password2 = rs.getParameter("newpassword2", "").trim();
			if (!password.equals(password2)) {
				return i18nAccess.getViewText("registration.error.password_notsame", "2 passwords must be the same.");
			} else if (password.length() < 3) {
				return i18nAccess.getViewText("registration.error.password_size", "password must be at least 3 characters.");
			}
			IUserInfo userInfo;
			if (rs.getParameter("pwkey", "").trim().length() > 2) {
				userInfo = userFactory.getPasswordChangeWidthKey(rs.getParameter("pwkey", ""));
				if (userInfo == null) {
					return i18nAccess.getViewText("user.message.bad-password-key");
				}
			} else {
				if (!ctx.getCurrentUser().isRightPassword(rs.getParameter("password", null))) {
					return i18nAccess.getViewText("user.message.bad-password");
				}
				userInfo = ctx.getCurrentUser().getUserInfo();
			}
			//password = SecurityHelper.encryptPassword(password2);

			userInfo.setPassword(SecurityHelper.encryptPassword(password));
			userFactory.store();
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("registration.message.password_changed", "Password changed."), GenericMessage.INFO));
		}
		return null;
	}

	public static String performLogout(RequestService rs, ContentContext ctx, GlobalContext globalContext, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		if (rs.getParameter("logout", null) != null) {
			UserRegistration comp = (UserRegistration) ComponentHelper.getComponentFromRequest(ctx);

			IUserFactory userFactory;
			if (comp == null || comp.isAdminRegistration()) {
				userFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
			} else {
				userFactory = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
			}
			userFactory.logout(session);
			session.setAttribute("logoutDone", "true");
		}
		return null;
	}

	public static String performResetPasswordWithEmail(RequestService rs, GlobalContext globalContext, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws AddressException {
		UserFactory userFactory = (UserFactory) UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		String email = rs.getParameter("email", "").trim();
		String passwordRetrieveKey = null;
		if (StringHelper.isMail(email)) {
			for (IUserInfo user : userFactory.getUserInfoList()) {
				if (user.getEmail().equals(email)) {
					passwordRetrieveKey = userFactory.createPasswordChangeKey(user);
					Map<String, String> params = new HashMap<String, String>();
					params.put("pwkey", passwordRetrieveKey);
					String url = URLHelper.createURL(ctx.getContextForAbsoluteURL(), params);
					String subject = i18nAccess.getViewText("user.mail.reset-password-subject");
					InternetAddress from = new InternetAddress(globalContext.getAdministratorEmail());
					InternetAddress to = new InternetAddress(email);
					NetHelper.sendMail(globalContext, from, to, null, null, subject + ' ' + globalContext.getGlobalTitle(), url);
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("user.message.change-password-link"), GenericMessage.INFO));
					return null;
				}
			}
			return i18nAccess.getViewText("user.message.error.change-mail-not-found");
		} else {
			return i18nAccess.getViewText("form.error.email");
		}
	}

	public static String performFacebookLogin(RequestService rs, ContentContext ctx, HttpSession session, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String token = rs.getParameter("token", null);
		Facebook facebook = SocialService.getInstance(ctx).getFacebook();
		IUserInfo ui = facebook.getInitialUserInfo(token);
		if (!StringHelper.isMail(ui.getEmail())) {
			return "technical error : facebook have not returned a valid email (" + ui.getEmail() + ')';
		}
		IUserFactory userFactory = UserFactory.createUserFactory(globalContext, session);
		User user = userFactory.getUser(ui.getLogin());
		if (user == null) {
			ui.setExternalLoginUser();
			userFactory.addUserInfo(ui);
			userFactory.store();
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("user.message.facebook-login"), GenericMessage.INFO));
		} else {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("user.message.facebook-login"), GenericMessage.INFO));
		}
		return null;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	protected boolean isAdminRegistration() {
		return getField(FIELD_SCOPE, ADMIN).equals(ADMIN);
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		out.println("<div class=\"form-group\">");
		out.println("<label for=\"" + getContentName() + "\">");
		out.println(i18nAccess.getText("user.registration.select", "Select user type"));
		out.println("</label>");
		Map<String, String> selection = new HashMap<String, String>();
		selection.put(ADMIN, i18nAccess.getText("user.registration.admin", "Administrator"));
		selection.put("visitors", i18nAccess.getText("user.registration.visotors", "Visitors"));
		out.println(XHTMLHelper.getInputOneSelect(getContentName(), selection, getField(FIELD_SCOPE, ADMIN), "form-control"));
		out.println("</div>");
		out.println("<div class=\"radio\">");

		Collection<String> roles = getFieldList(FIELD_SELECTED_ROLES);
		EditContext editContext = EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession());
		if (getField(FIELD_SCOPE, ADMIN).equals(ADMIN)) {
			out.println("<h3>" + i18nAccess.getText("user.roles.default", "default roles") + "</h3>");
			for (String role : editContext.getDefaultAdminUserRoles()) {
				out.println("<label class=\"checkbox-inline\"><input type=\"checkbox\" name=\"" + getInputName(role) + "\" " + (roles.contains(role) ? "checked=\"checked\"" : "") + ">" + role + "</label>");
			}
			out.println("<h3>" + i18nAccess.getText("user.roles.specific", "specific roles") + "</h3>");
			for (String role : ctx.getGlobalContext().getAdminUserRoles()) {
				out.println("<label class=\"checkbox-inline\"><input type=\"checkbox\" name=\"" + getInputName(role) + "\" " + (roles.contains(role) ? "checked=\"checked\"" : "") + ">" + role + "</label>");
			}
		} else {
			out.println("<h3>" + i18nAccess.getText("user.roles.default", "default roles") + "</h3>");
			for (String role : ctx.getGlobalContext().getUserRoles()) {
				out.println("<label class=\"checkbox-inline\"><input type=\"checkbox\" name=\"" + getInputName(role) + "\" " + (roles.contains(role) ? "checked=\"checked\"" : "") + ">" + role + "</label>");
			}
		}
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		String previousValue = getValue();
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		setField(FIELD_SCOPE, rs.getParameter(getContentName()));
		Collection<String> roles = new LinkedList<String>();
		Collection<String> possibleRoles;
		if (getField(FIELD_SCOPE, ADMIN).equals(ADMIN)) {
			EditContext editContext = EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession());
			possibleRoles = editContext.getAdminUserRoles();
		} else {
			possibleRoles = ctx.getGlobalContext().getUserRoles();
		}
		for (String role : possibleRoles) {
			if (StringHelper.isTrue(rs.getParameter(getInputName(role)))) {
				roles.add(role);
			}
		}
		setField(FIELD_SELECTED_ROLES, roles);
		if (!previousValue.equals(getValue())) {
			setModify();
		}
		return null;
	}
}
