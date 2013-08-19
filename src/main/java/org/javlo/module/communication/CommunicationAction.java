package org.javlo.module.communication;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.IMService;
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
	public AbstractModuleContext getModuleContext(HttpSession session, Module module) throws Exception {
		return CommunicationModuleContext.getInstance(session, GlobalContext.getSessionInstance(session), module, CommunicationModuleContext.class);
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		HttpServletRequest request = ctx.getRequest();
		HttpSession session = request.getSession();

		CommunicationModuleContext communicationModuleContext = CommunicationModuleContext.getInstance(request);

		communicationModuleContext.loadNavigation(ctx);

		super.prepare(ctx, modulesContext);

		ContentContext viewCtx = new ContentContext(ctx);
		Module currentModule = modulesContext.getCurrentModule();
		String msg = "";
		viewCtx.setRenderMode(ContentContext.VIEW_MODE);

		AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();
		IMService imService = IMService.getInstance(request.getSession());

		CommunicationBean comm = new CommunicationBean();

		request.setAttribute("comm", comm);

		comm.setCurrentSite(communicationModuleContext.getCurrentSite());
		comm.setCurrentUser(ctx.getCurrentUserId());

		if (comm.isAllSites()) {
			Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(request.getSession());
			for (GlobalContext context : allContext) {
				if (context.getAliasOf() == null || context.getAliasOf().trim().isEmpty()) {
					prepareSite(context, comm, adminUserSecurity, imService);
				}
			}
		} else {
			prepareSite(GlobalContext.getInstance(session, comm.getCurrentSite()), comm, adminUserSecurity, imService);
		}

		Long lastMessageId = null;
		if (ctx.isAjax()) {
			lastMessageId = StringHelper.safeParseLong(request.getParameter("lastMessageId"), null);
		}

		comm.setLastMessageId(imService.fillMessageList(
				comm.getCurrentSite(),
				comm.getCurrentUser(),
				lastMessageId,
				comm.getMessages()));

		if (ctx.isAjax()) {
			ctx.addAjaxZone("cim-next-messages", ServletHelper.executeJSP(ctx, currentModule.getPath() + "/jsp/cim_messages.jsp"));
		}

		return msg;
	}

	private void prepareSite(GlobalContext context, CommunicationBean comm, AdminUserSecurity adminUserSecurity, IMService imService) {
		CommunicationSiteBean site = new CommunicationSiteBean();
		site.setKey(context.getContextKey());
		List<Principal> list = context.getAllPrincipals();		
		for (Principal principal : list) {
			CommunicationUserBean user = new CommunicationUserBean();
			user.setSite(context.getContextKey());
			user.setUsername(principal.getName());
			user.setColor(imService.getUserColor(context.getContextKey(), principal.getName()));
			site.addUser(user);
		}
		comm.addSite(site);
	}

	public static String performSendIM(RequestService rs, ContentContext ctx, HttpServletRequest request, Module currentModule, CommunicationModuleContext communicationModuleContext) throws ConfigurationException, IOException {
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

				if (!communicationModuleContext.getCanSpeakSites().contains(receiverSite)) {
					throw new SecurityException("You can not send message to the given site.");
				}

				String currentSite = communicationModuleContext.getCurrentSite();
				if (IMService.ALL_SITES.equals(currentSite)) {
					currentSite = receiverSite;
				}
				String currentUser = ctx.getCurrentUserId();

				message = XHTMLHelper.autoLink(XHTMLHelper.escapeXHTML(message));

				IMService imService = IMService.getInstance(request.getSession());
				imService.appendMessage(currentSite, currentUser, receiverSite, receiverUser, message);
			}
		}
		return null;
	}

}
