package org.javlo.component.users;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.RequestParameterMap;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.SecurityHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;
import org.javlo.service.social.Facebook;
import org.javlo.service.social.SocialService;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.TransientUserInfo;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfo;

public class UserLogin extends AbstractPropertiesComponent implements IAction {

	private static final String ADMIN = "administrators";

	public static final String TYPE = "user-login";

	private static final String EMAIL = "email";
	public static final String MSG = "message";
	public static final String ROLES = "roles";

	public static final String VALIDATION = "validation_msg";
	public static final String OPTIN = "optin";
	public static final String OPTOUT = "optout";

	private static final List<String> FIELDS = new LinkedList<String>(Arrays.asList(new String[] { EMAIL, MSG, ROLES, VALIDATION, OPTIN, OPTOUT }));

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

	@Override
	public void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		if (getValue().trim().length() == 0) {
			setValue(ADMIN); // admin registration by default.
		}
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);

		SocialService.getInstance(ctx).prepare(ctx);
		
		if (ctx.getCurrentUser() != null) {
			ctx.getRequest().setAttribute("user", ctx.getCurrentUser());
			ctx.getRequest().setAttribute("userInfoMap", ctx.getCurrentUser().getUserInfo());			
			Collection<String> imageURL = new LinkedList<String>();
			String userFolderName = ctx.getGlobalContext().getUserFolder(ctx.getCurrentUser());
			if (userFolderName != null) {
				File userFolder = new File(userFolderName);
				if (userFolder != null && userFolder.exists()) {
					for (File file : userFolder.listFiles()) {
						if (StringHelper.isImage(file.getName())) {
							imageURL.add(URLHelper.createTransformURL(ctx, file, "height-2"));
						}
					}
				}
			}
			ctx.getRequest().setAttribute("images", imageURL);
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "NEED RENDERER.";
	}

	@Override
	public String getActionGroupName() {
		return "user-login";
	}

	public static String performLogout(RequestService rs, ContentContext ctx, GlobalContext globalContext, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		if (rs.getParameter("logout", null) != null) {
			IUserFactory userFactory = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
			userFactory.logout(session);
			session.setAttribute("logoutDone", "true");
		}
		return null;
	}
	
	public static String performDeleteimage(RequestService rs, ContentContext ctx, GlobalContext globalContext, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		File imageFile = new File (URLHelper.mergePath(ctx.getGlobalContext().getUserFolder(ctx.getCurrentUser()), StringHelper.getFileNameFromPath(rs.getParameter("file"))));
		if (imageFile.exists()) {
			imageFile.delete();
		}
		return null;
	}

	public static String performFacebookLogin(RequestService rs, ContentContext ctx, HttpSession session, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String token = rs.getParameter("token", null);
		Facebook facebook = SocialService.getInstance(ctx).getFacebook();
		IUserInfo ui = facebook.getInitialUserInfo(token);
		TransientUserInfo.getInstance(session).setToken(token);
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

	public static String performLogin(RequestService rs, GlobalContext globalContext, ContentContext ctx, HttpServletRequest request, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) {
		IUserFactory uf = UserFactory.createUserFactory(globalContext, session);
		if (uf.login(request, rs.getParameter("login", rs.getParameter("email", null)), rs.getParameter("password", "")) == null) {
			return i18nAccess.getViewText("user.error.login");
		}
		return null;
	}

	public static String performRegister(RequestService rs, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String login = rs.getParameter("login", rs.getParameter("email", "").trim()).trim();
		IUserFactory userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), session);
		String password = rs.getParameter("password", "").trim();
		String password2 = rs.getParameter("passwordbis", "").trim();
		ctx.getRequest().setAttribute("userInfoMap", new RequestParameterMap(ctx.getRequest()));
		
		String email = rs.getParameter("email", null);

		if (email != null && !PatternHelper.MAIL_PATTERN.matcher(email).matches()) {
			return i18nAccess.getViewText("registration.error.email", "Please enter a valid email.");
		} else if (userFactory.getUser(login) != null) {
			return i18nAccess.getViewText("registration.error.login_allreadyexist", "user already exists : ") + login;
		} else if (!password.equals(password2)) {
			return i18nAccess.getViewText("registration.error.password_notsame", "2 passwords must be the same.");
		} else if (password.length() < 3) {
			return i18nAccess.getViewText("registration.error.password_size", "password must be at least 3 characters.");
		}

		UserLogin comp = (UserLogin) ComponentHelper.getComponentFromRequest(ctx);
		if (!StringHelper.isEmpty(comp.getFieldValue(UserLogin.VALIDATION)) && !StringHelper.isTrue(rs.getParameter("valid"))) {
			return i18nAccess.getViewText("registration.error.check", "Please check : ")+'"'+comp.getFieldValue(UserLogin.VALIDATION)+'"';
		}
		IUserInfo userInfo = new UserInfo();
		userInfo.setLogin(login);
		if (email != null) {
			userInfo.setEmail(email);
		} else {
			if (PatternHelper.MAIL_PATTERN.matcher(login).matches()) {
				userInfo.setEmail(login);
			}
		}
		userInfo.setPassword(SecurityHelper.encryptPassword(password));
		userInfo.setRoles(StringHelper.stringToSet(comp.getFieldValue(ROLES)));
		userFactory.addUserInfo(userInfo);
		userFactory.store();
		userInfo = userFactory.login(ctx.getRequest(), login, password).getUserInfo();
		if (StringHelper.isMail(comp.getFieldValue(EMAIL))) {
			String subject = i18nAccess.getText("user.mail.create.subjet") + ctx.getGlobalContext().getGlobalTitle();
			Map data = new HashMap();
			data.put("email", login);
			if (!StringHelper.isEmpty(userInfo.getFirstName())) {
				data.put(i18nAccess.getText("user.firstname"), userInfo.getFirstName());
			}
			if (!StringHelper.isEmpty(userInfo.getLastName())) {
				data.put(i18nAccess.getText("user.lastanme"), userInfo.getLastName());
			}
			if (!StringHelper.isEmpty(comp.getFieldValue(ROLES))) {
				data.put("roles", comp.getFieldValue(ROLES));
			}
			Map<String, String> params = new HashMap<String, String>();
			params.put("webaction1", "changemode");
			params.put("webaction2", "edit");
			params.put("module", "users");
			params.put("mode", "view");
			params.put("cuser", userInfo.getEncryptLogin());
			ContentContext absCtx = ctx.getContextForAbsoluteURL();
			absCtx.setRenderMode(ContentContext.EDIT_MODE);
			String adminMailContent = XHTMLHelper.createAdminMail(subject, null, data, URLHelper.createURL(absCtx, params), "go on page >>", null);
			MailService mailService = MailService.getInstance(new MailConfig(ctx.getGlobalContext(), ctx.getGlobalContext().getStaticConfig(), null));
			InternetAddress fromEmail = new InternetAddress(ctx.getGlobalContext().getAdministratorEmail());
			InternetAddress toEmail = new InternetAddress(comp.getFieldValue(EMAIL));
			mailService.sendMail(null, fromEmail, toEmail, null, null, subject, adminMailContent, true, null, ctx.getGlobalContext().getDKIMBean());
		}
		if (!StringHelper.isEmpty(comp.getFieldValue(MSG))) {
			messageRepository.setGlobalMessage(new GenericMessage(comp.getFieldValue(MSG), GenericMessage.INFO));
		}
		return null;
	}

	public static String performUpdate(RequestService rs, GlobalContext globalContext, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		IUserFactory userFactory;
		userFactory = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		
		User user = ctx.getCurrentUser();
		IUserInfo userInfo = user.getUserInfo();

		String email = rs.getParameter("email", "");
		if (!userInfo.getLogin().equals(email)) {
			if (userFactory.getUser(email) != null) {
				return i18nAccess.getViewText("registration.error.login_allreadyexist", "user already exists : ") + email;
			} else {
				user.setLogin(email);
				userInfo.setLogin(email);
			}
		}

		Set<String> newRoles = userInfo.getRoles();
		if (!StringHelper.isEmpty(getFieldName(OPTIN))) {
			if (rs.getParameter("optin") != null) {
				newRoles.add(getFieldName(OPTIN));
			} else {
				newRoles.remove(getFieldName(OPTIN));
			}
		}
		if (!StringHelper.isEmpty(getFieldName(OPTOUT))) {
			if (rs.getParameter("optout") != null) {
				newRoles.add(getFieldName(OPTOUT));
			} else {
				newRoles.remove(getFieldName(OPTOUT));
			}
		}
		
		for (FileItem fileItem : rs.getAllFileItem()) {
			if (StringHelper.isImage(fileItem.getName())) {
				File image = new File(URLHelper.mergePath(globalContext.getUserFolder(ctx.getCurrentUser()), fileItem.getName()));
				if (!image.exists()) {
					image.getParentFile().mkdirs();
					image.createNewFile();
					InputStream in = null;
					try {
						in = fileItem.getInputStream();
						ResourceHelper.writeStreamToFile(in, image);
					} finally {
						ResourceHelper.closeResource(in);
					}
				}
			}
		}
		
		
		userInfo.setRoles(newRoles);
		IContentVisualComponent comp = ComponentHelper.getComponentFromRequest(ctx);
		List<String> fields = comp.extractFieldsFromRenderer(ctx);
		if (fields != null && fields.size() > 0) {
			Map<String,String> allValues = new HashMap<String, String>();
			for (String field : fields) {
				allValues.put(field, StringHelper.neverNull(rs.getParameter(field)));
				BeanHelper.copy(allValues, userInfo);
			}
		} else {
			BeanHelper.copy(new RequestParameterMap(ctx.getRequest()), userInfo);	
		}
		
		userFactory.updateUserInfo(userInfo);
		user.setUserInfo(userInfo);
		messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("registration.message.update", "User info is updated."), GenericMessage.INFO));
		return null;

	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_ADMIN);
	}
	
	@Override
	public String getFontAwesome() {
		return "user-circle";
	}

}
