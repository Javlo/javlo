package org.javlo.component.users;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.LangHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.RequestParameterMap;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.MailService;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserInfo;
import org.javlo.user.IUserFactory;
import org.javlo.user.UserFactory;

public class UserRegistration extends AbstractVisualComponent implements IAction {
	
	private static final String ADMIN = "administrators";

	public static final String TYPE = "admin-user-registration";

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
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		if (ctx.getRequest().getAttribute("registration-message") == null) {
			Module userModule = ModulesContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext()).searchModule("users");
			i18nAccess.setCurrentModule(ctx.getGlobalContext(), ctx.getRequest().getSession(), userModule);		
			ctx.getRequest().setAttribute("webaction", "user-registration.register");
			
			AdminUserInfo userInfo = new AdminUserInfo();
			RequestService rs = RequestService.getInstance(ctx.getRequest());
			List<String> functions = rs.getParameterListValues("function", Collections.EMPTY_LIST);
			if (functions.size() > 0 && userInfo instanceof AdminUserInfo) {
				((AdminUserInfo)userInfo).setFunction(StringHelper.collectionToString(functions, ";"));
			}
			ctx.getRequest().setAttribute("functions", LangHelper.collectionToMap(functions));
			
			String jsp = "/modules/users/jsp/edit_current.jsp";
			return ServletHelper.executeJSP(ctx, jsp);
		} else {
			return "<div class=\"message info\">"+ctx.getRequest().getAttribute("registration-message")+"</div>";
		}

	}

	@Override
	public String getActionGroupName() {
		return "user-registration";
	}

	public static String performRegister(RequestService rs, GlobalContext globalContext, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		
		UserRegistration comp = (UserRegistration)ComponentHelper.getComponentFromRequest(ctx);
		
		IUserFactory userFactory;
		if (comp == null || comp.isAdminRegistration()) {
			userFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		} else {
			userFactory = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		}

		String login = rs.getParameter("login", "").trim();
		String password = rs.getParameter("password", "").trim();
		String password2 = rs.getParameter("password2", "").trim();
		String email = rs.getParameter("email", "").trim();
		ctx.getRequest().setAttribute("userInfoMap", new RequestParameterMap(ctx.getRequest()));
		if (login.length() < 3) {
			return i18nAccess.getViewText("registration.error.login_size", "login must be at least 3 characters.");
		} else if (userFactory.getUser(login) != null) {
			return i18nAccess.getViewText("registration.error.login_allreadyexist", "user allready exist.");
		} else if (!password.equals(password2)) {
			return i18nAccess.getViewText("registration.error.password_notsame", "2 passwords must be the same.");
		} else if (password.length() < 3) {
			return i18nAccess.getViewText("registration.error.password_size", "password must be at least 3 characters.");
		} else if (!PatternHelper.MAIL_PATTERN.matcher(email).matches()) {
			return i18nAccess.getViewText("registration.error.password_size", "Please enter a valid email.");
		}
		AdminUserInfo userInfo = new AdminUserInfo();
		List<String> functions = rs.getParameterListValues("function", Collections.EMPTY_LIST);
		if (functions.size() > 0 && userInfo instanceof AdminUserInfo) {
			((AdminUserInfo)userInfo).setFunction(StringHelper.collectionToString(functions, ";"));
		}
		try {
			BeanHelper.copy(new RequestParameterMap(ctx.getRequest()), userInfo);
			if (globalContext.getStaticConfig().isPasswordEncryt()) {
				userInfo.setPassword(StringHelper.encryptPassword(userInfo.getPassword()));
			}
			userFactory.addUserInfo(userInfo);
			userFactory.store();
			ctx.getRequest().setAttribute("registration-message", i18nAccess.getViewText("registration.message.registred", "Thanks for you registration."));
			
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("Registration on : "+globalContext.getGlobalTitle());
			out.println("");
			out.println("login           : "+userInfo.getLogin());
			out.println("firstname       : "+userInfo.getFirstName());
			out.println("lastname        : "+userInfo.getLastName());
			out.println("email           : "+userInfo.getEmail());
			out.println("country         : "+userInfo.getCountry());
			out.println("orgnization     : "+userInfo.getOrganization());			
			out.println("");
			out.println("message         : ");
			out.println(rs.getParameter("message","" ));
			out.println("");
			out.println("");
			out.println("Access link     : "+URLHelper.createURL(ctx.getContextForAbsoluteURL().getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), "/"));
			out.println("");
			out.close();
			
			MailService mailService = MailService.getInstance(globalContext.getStaticConfig());
			InternetAddress newUser = new InternetAddress(userInfo.getEmail());
			InternetAddress admin = new InternetAddress(globalContext.getAdministratorEmail());
			
			mailService.sendMail(newUser, admin, "new user : "+userInfo.getLogin(), new String(outStream.toByteArray()), false);
			mailService.sendMail(admin, newUser, "you new account on : "+globalContext.getGlobalTitle(), new String(outStream.toByteArray()), false);
			
		} catch (Exception e) {
			e.printStackTrace();
			return "technical error.";
		}
		

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
		out.println("<label for=\""+getContentName()+"\">");
		out.println(i18nAccess.getText("user.registration.select", "Select user type"));
		out.println("</label>");
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(ADMIN, i18nAccess.getText("user.registration.admin", "Administrator"));
		selection.put("visitors", i18nAccess.getText("user.registration.visotors", "Visitors"));		
		out.println(XHTMLHelper.getInputOneSelect(getContentName(),selection,getValue()));
	
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}
	
}
