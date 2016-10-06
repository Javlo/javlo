package org.javlo.component.users;

import javax.servlet.http.HttpSession;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
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

public class UserLogin extends AbstractVisualComponent implements IAction {

	private static final String ADMIN = "administrators";

	public static final String TYPE = "user-login";

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
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		
		SocialService.getInstance(ctx).prepare(ctx);

		if (ctx.getCurrentUser() != null) {
			ctx.getRequest().setAttribute("user", ctx.getCurrentUser());
			ctx.getRequest().setAttribute("userInfoMap", ctx.getCurrentUser().getUserInfo());
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
			IUserFactory userFactory= UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());			
			userFactory.logout(session);
			session.setAttribute("logoutDone", "true");			
			ctx.setUser();
		}
		return null;
	}
	
	public static String performFacebookLogin(RequestService rs, ContentContext ctx, HttpSession session, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {		
		String token = rs.getParameter("token", null);		
		Facebook facebook = SocialService.getInstance(ctx).getFacebook();
		IUserInfo ui = facebook.getInitialUserInfo(token);		
		TransientUserInfo.getInstance(session).setToken(token);
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
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_ADMIN);
	}

	
}
