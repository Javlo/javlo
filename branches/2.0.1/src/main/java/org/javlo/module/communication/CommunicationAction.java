package org.javlo.module.communication;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.ConfigurationException;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.IMService;
import org.javlo.service.IMService.IMItem;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserSecurity;
import org.springframework.util.StringUtils;

public class CommunicationAction extends AbstractModuleAction {

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CommunicationAction.class.getName());

	@Override
	public String getActionGroupName() {
		return "communication";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception {

		HttpServletRequest request = ctx.getRequest();

		ContentContext viewCtx = new ContentContext(ctx);
		Module currentModule = moduleContext.getCurrentModule();
		String msg = "";
		viewCtx.setRenderMode(ContentContext.VIEW_MODE);

		AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();

		Collection<String> sites = new LinkedList<String>();
		Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(request.getSession());
		for (GlobalContext context : allContext) {
			if (ctx.getCurrentEditUser() != null) {
				if (adminUserSecurity.isAdmin(ctx.getCurrentEditUser()) || context.getUsersAccess().contains(ctx.getCurrentEditUser().getLogin())) {
					if (context.getAliasOf() == null || context.getAliasOf().trim().isEmpty()) {
						sites.add(context.getContextKey());
					}
				}
			}
		}

		request.setAttribute("sites", sites);

		// AIM
		IMService imService = IMService.getInstance(request.getSession());

		String currentSite = IMService.ALL_SITES;
		String currentUser = ctx.getCurrentUserId();

		Map<String, Map<String, Map<String, String>>> usersBySite = new HashMap<String, Map<String, Map<String, String>>>();
		for (GlobalContext context : allContext) {
			if (ctx.getCurrentEditUser() != null) {
				if (adminUserSecurity.isAdmin(ctx.getCurrentEditUser()) || context.getUsersAccess().contains(ctx.getCurrentEditUser().getLogin())) {
					Map<String, Map<String, String>> users = new LinkedHashMap<String, Map<String, String>>();
					List<Principal> list = context.getAllPrincipals();
					for (Principal principal : list) {
						Map<String, String> user = new LinkedHashMap<String, String>();
						user.put("site", context.getContextKey());
						user.put("username", principal.getName());
						user.put("color", imService.getUserColor(currentSite, principal.getName()));
						users.put(principal.getName(), user);
					}
					usersBySite.put(context.getContextKey(), users);
				}
			}
		}

		Long aimLastMessageId = null;
		if (ctx.isAjax()) {
			aimLastMessageId = StringHelper.safeParseLong(request.getParameter("lastMessageId"), null);
		}
		
		List<IMItem> messages = new ArrayList<IMItem>();
		aimLastMessageId = imService.fillMessageList(currentSite, currentUser, aimLastMessageId, messages);
		//

		request.setAttribute("aimCurrentUser", currentUser);
		request.setAttribute("aimLastMessageId", aimLastMessageId);
		request.setAttribute("aimMessages", messages);
		request.setAttribute("aimUsersBySite", usersBySite);
		if (ctx.isAjax()) {
			ctx.addAjaxZone("aim-next-messages", ServletHelper.executeJSP(ctx, currentModule.getPath() + "/jsp/aim_messages.jsp"));
		}

		return msg;
	}

	public static String performSendAIM(RequestService rs, ContentContext ctx, HttpServletRequest request, Module currentModule) throws ConfigurationException, IOException {

		IMService imService = IMService.getInstance(request.getSession());

		String currentSite = IMService.ALL_SITES;
		String currentUser = ctx.getCurrentUserId();

		String message = StringHelper.trimAndNullify(request.getParameter("message"));
		String receiver = StringHelper.trimAndNullify(request.getParameter("receiver"));
		if (message != null && receiver != null) {
			String[] receiverParts = StringUtils.split(receiver, "::");
			if (receiverParts.length == 2) {
				String receiverSite = StringHelper.trimAndNullify(receiverParts[0]);
				if (receiverSite == null) {
					receiverSite = IMService.ALL_SITES;
				}
				String receiverUser = StringHelper.trimAndNullify(receiverParts[1]);
				if (receiverUser == null) {
					receiverUser = IMService.ALL_USERS;
				}
				message = XHTMLHelper.autoLink(XHTMLHelper.escapeXHTML(message));
				imService.appendMessage(currentSite, currentUser, receiverSite, receiverUser, message);
			}
		}

		return null;
	}

}
