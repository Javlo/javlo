package org.javlo.component.users;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpSession;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.LangHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.RequestParameterMap;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
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

public class UserRegistration extends AbstractVisualComponent implements IAction {

	private static final String ADMIN = "administrators";

	public static final String TYPE = "user-registration";

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
		
		DeliveryPrice deliveryPrice = DeliveryPrice.getInstance(ctx);
		if (deliveryPrice != null) {
			ListService.getInstance(ctx).addList("countries", deliveryPrice.getZone());			
		}

		if (ctx.getCurrentUser() != null) {
			ctx.getRequest().setAttribute("user", ctx.getCurrentUser());
			ctx.getRequest().setAttribute("userInfoMap", ctx.getCurrentUser().getUserInfo());
		}

	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		if (ctx.getRequest().getAttribute("registration-message") == null) {
			Module userModule = ModulesContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext()).searchModule("users");
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

		IUserInfo userInfo = userFactory.getCurrentUser(session).getUserInfo();
		BeanHelper.copy(new RequestParameterMap(ctx.getRequest()), userInfo);
		userFactory.updateUserInfo(userInfo);
		userFactory.store();

		messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("registration.message.update", "User info is updated."), GenericMessage.INFO));

		return null;

	}

	public static String performRegister(RequestService rs, GlobalContext globalContext, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		UserRegistration comp = (UserRegistration) ComponentHelper.getComponentFromRequest(ctx);

		IUserFactory userFactory;
		UserInfo userInfo;

		if (comp == null || comp.isAdminRegistration()) {
			userFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
			userInfo = new AdminUserInfo();
		} else {
			userFactory = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
			userInfo = new UserInfo();
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
			return i18nAccess.getViewText("registration.error.login_allreadyexist", "user allready exist : ");
		} else if (!password.equals(password2)) {
			return i18nAccess.getViewText("registration.error.password_notsame", "2 passwords must be the same.");
		} else if (password.length() < 3) {
			return i18nAccess.getViewText("registration.error.password_size", "password must be at least 3 characters.");
		} else if (!PatternHelper.MAIL_PATTERN.matcher(email).matches()) {
			return i18nAccess.getViewText("registration.error.password_size", "Please enter a valid email.");
		}

		List<String> functions = rs.getParameterListValues("function", Collections.EMPTY_LIST);
		if (functions.size() > 0 && userInfo instanceof AdminUserInfo) {
			((AdminUserInfo) userInfo).setFunction(StringHelper.collectionToString(functions, ";"));
		}
		try {
			BeanHelper.copy(new RequestParameterMap(ctx.getRequest()), userInfo);
			if (emailLogin != null) {
				userInfo.setLogin(emailLogin);
				userInfo.setEmail(emailLogin);
			}
			if (globalContext.getStaticConfig().isPasswordEncryt()) {
				userInfo.setPassword(StringHelper.encryptPassword(userInfo.getPassword()));
			}
			userFactory.addUserInfo(userInfo);
			userFactory.store();

			ctx.getRequest().setAttribute("registration-message", i18nAccess.getViewText("registration.message.registred", "Thanks for you registration.")); // depreciate
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("registration.message.registred", "Thanks for you registration."), GenericMessage.INFO));

			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("Registration on : " + globalContext.getGlobalTitle());
			out.println("");

			out.println(i18nAccess.getViewText("form.login") + " : " + userInfo.getLogin());
			out.println(i18nAccess.getViewText("form.firstName") + " : " + userInfo.getFirstName());
			out.println(i18nAccess.getViewText("form.lastName") + " : " + userInfo.getLastName());
			out.println(i18nAccess.getViewText("form.email") + " : " + userInfo.getEmail());
			out.println(i18nAccess.getViewText("form.address.country") + " : " + userInfo.getCountry());
			if (userInfo.getOrganization().trim().length() > 0) {
				out.println(i18nAccess.getViewText("form.organization") + " : " + userInfo.getOrganization());
			}
			if (rs.getParameter("message", "").trim().length() > 0) {
				out.println("");
				out.println(i18nAccess.getViewText("form.comment") + " : ");
				out.println(rs.getParameter("message", ""));
				out.println("");
			}
			out.println("");
			if (globalContext.isCollaborativeMode()) {
				out.println(URLHelper.createURL(ctx.getContextForAbsoluteURL().getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), "/"));
			} else {
				out.println(URLHelper.createURL(ctx.getContextForAbsoluteURL().getContextWithOtherRenderMode(ContentContext.VIEW_MODE), "/"));
			}
			out.println("");
			out.close();

			MailService mailService = MailService.getInstance(globalContext.getStaticConfig());
			InternetAddress newUser = new InternetAddress(userInfo.getEmail());
			InternetAddress admin = new InternetAddress(globalContext.getAdministratorEmail());

			mailService.sendMail(newUser, admin, "new user : " + userInfo.getLogin(), new String(outStream.toByteArray()), false);
			mailService.sendMail(admin, newUser, i18nAccess.getViewText("user.new-account") + globalContext.getGlobalTitle(), new String(outStream.toByteArray()), false);

			ctx.getRequest().setAttribute("noform", "true");
		} catch (Exception e) {
			logger.severe("error on " + ctx.getGlobalContext().getContextKey()+" login:"+login);
			e.printStackTrace();
			return i18nAccess.getViewText("global.technical-error");
		}

		return null;
	}

	public static String performChangePassword(RequestService rs, ContentContext ctx, GlobalContext globalContext, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		UserRegistration comp = (UserRegistration) ComponentHelper.getComponentFromRequest(ctx);

		UserFactory userFactory;
		if (comp == null || comp.isAdminRegistration()) {
			userFactory = (UserFactory)AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		} else {
			userFactory = (UserFactory)UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
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
				if (!ctx.getCurrentUser().isRightPassword(rs.getParameter("password", null), globalContext.getStaticConfig().isPasswordEncryt())) {
					return i18nAccess.getViewText("user.message.bad-password");				
				}  
				userInfo = ctx.getCurrentUser().getUserInfo();
			}
			if (globalContext.getStaticConfig().isPasswordEncryt()) {
				password = StringHelper.encryptPassword(password2);
			}
			userInfo.setPassword(password);
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
			ctx.setUser();
		}
		return null;
	}
	
	public static String performResetPasswordWithEmail(RequestService rs, GlobalContext globalContext, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws AddressException {		
		UserFactory userFactory = (UserFactory)UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		String email = rs.getParameter("email", "").trim();		
		String passwordRetrieveKey = null;
		if (StringHelper.isMail(email)) {
			for(IUserInfo user : userFactory.getUserInfoList()) {
				if (user.getEmail().equals(email)) {
					passwordRetrieveKey = userFactory.createPasswordChangeKey(user);
					Map<String,String> params = new HashMap<String, String>();
					params.put("pwkey", passwordRetrieveKey);					
					String url = URLHelper.createURL(ctx.getContextForAbsoluteURL(), params);					
					String subject = i18nAccess.getViewText("user.mail.reset-password-subject");					
					InternetAddress from = new InternetAddress(globalContext.getAdministratorEmail());
					InternetAddress to = new InternetAddress(email);
					NetHelper.sendMail(globalContext, from, to, null, null, subject+' '+globalContext.getGlobalTitle(), url);
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
		Facebook facebook = SocialService.getInstance(globalContext).getFacebook();
		IUserInfo ui = facebook.getInitialUserInfo(token);
		if (!StringHelper.isMail(ui.getEmail())) {
			return "technical error : facebook have not returned a valid email ("+ui.getEmail()+')';
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
		ctx.setCurrentUser(userFactory.autoLogin(ctx.getRequest(), ui.getLogin()));
		return null;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	protected boolean isAdminRegistration() {
		return getValue().equals(ADMIN);
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getContentName() + "\">");
		out.println(i18nAccess.getText("user.registration.select", "Select user type"));
		out.println("</label>");
		Map<String, String> selection = new HashMap<String, String>();
		selection.put(ADMIN, i18nAccess.getText("user.registration.admin", "Administrator"));
		selection.put("visitors", i18nAccess.getText("user.registration.visotors", "Visitors"));
		out.println(XHTMLHelper.getInputOneSelect(getContentName(), selection, getValue()));

		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

}
