package org.javlo.macro.interactive;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

public class LogAsUser implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(LogAsUser.class.getName());

	@Override
	public String getName() {
		return "log-as-user";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}

	@Override
	public String getActionGroupName() {
		return getName();
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/log-as-user.jsp";
	}

	@Override
	public String getInfo(ContentContext ctx) {
		return null;
	}

	@Override
	public String prepare(ContentContext ctx) {
		if (haveRight(ctx, "login")) {
			List<IUserInfo> users = UserFactory.createUserFactory(ctx.getRequest()).getUserInfoList();
			Collections.sort(users, new Comparator<IUserInfo>() {
				@Override
				public int compare(IUserInfo o1, IUserInfo o2) {
					return o1.getLogin().compareTo(o2.getLogin());
				}
			});
			ctx.getRequest().setAttribute("users", users);
		}
		return null;
	}

	public static String performLogin(RequestService rs, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		IUserFactory userFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		if (!AdminUserSecurity.getInstance().canRole(userFactory.getCurrentUser(ctx.getGlobalContext(), ctx.getRequest().getSession()), AdminUserSecurity.USER_ROLE)) {
			logger.severe("user : " + ctx.getCurrentEditUser() + " try to execute unauthorized macro.");
			return "security exception !";
		}
		String login = rs.getParameter("login");
		User user = UserFactory.createUserFactory(ctx.getRequest()).adminFakeLogin(ctx.getRequest(), login);
		messageRepository.clearGlobalMessage(); 
		if (user != null) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("macro.log-as-user.login", "you are 'fake' logged as : ")+login, GenericMessage.INFO));
		} else {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("macro.log-as-user.login", "user not found : ")+login, GenericMessage.ERROR));
		}
		ctx.setParentURL(URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE)));
		ctx.setClosePopup(true);
		return null;
	}
	
	public static String performLogout(RequestService rs, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		UserFactory.createUserFactory(ctx.getRequest()).logout(ctx.getRequest().getSession());
		ctx.setClosePopup(true);
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

	@Override
	public boolean isAdd() {
		return true;
	}

	@Override
	public boolean isInterative() {
		return true;
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return AdminUserSecurity.getInstance().canRole(ctx.getCurrentEditUser(), AdminUserSecurity.USER_ROLE);
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void init(ContentContext ctx) {
	}

	@Override
	public String getModalSize() {
		return SMALL_MODAL_SIZE;
	}

	@Override
	public String getIcon() {
		return "fa fa-user-circle";
	}
	
	@Override
	public String getUrl() {
		return null;
	}
	
	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}

}
