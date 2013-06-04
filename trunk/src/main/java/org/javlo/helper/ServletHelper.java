package org.javlo.helper;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.actions.ActionManager;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ElementaryURLHelper.Code;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.NotificationService;
import org.javlo.service.RequestService;

public class ServletHelper {

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ServletHelper.class.getName());

	/**
	 * exec current action define in request
	 * 
	 * @param ctx
	 * @return the name of the action.
	 * @throws Exception
	 */
	public static final String execAction(ContentContext ctx) throws Exception {

		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		/** INIT TEMPLATE **/

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		boolean specialRightON = false;
		if (globalContext.isSpacialAccessCode(new Code(requestService.getParameter(URLHelper.SPACIAL_RIGHT_CODE_KEY, "no-code")))) {
			specialRightON = true;
		}

		List<String> actions = new LinkedList<String>();
		List<String> actionsKey = new LinkedList<String>();
		Map<String, String[]> params = requestService.getParametersMap();

		Collection<String> keys = params.keySet();
		for (String key : keys) {
			if (key.startsWith("webaction")) {
				actionsKey.add(key);
			}
		}
		if (actionsKey.size() > 1) {
			Collections.sort(actionsKey);
			for (String key : actionsKey) {
				actions.addAll(requestService.getParameterListValues(key, Collections.EMPTY_LIST));
			}
		} else {
			actions.addAll(requestService.getParameterListValues("webaction", Collections.EMPTY_LIST));
		}

		// String[] actions = requestService.getParameterValues("webaction", null);

		if (actions.size() == 0) {
			return null;
		}

		for (String action : actions) {
			EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
			if ((ctx.getRequest().getServletPath().equals("/edit") || ctx.getRequest().getServletPath().equals("/admin")) && (editCtx.getUserPrincipal() == null && !specialRightON)) {
				logger.warning("block action : '" + action + "' because user is not logged.");
			} else {
				String newMessage = ActionManager.perform(action, ctx.getRequest(), ctx.getResponse());
				if (newMessage != null) {
					ctx.getRequest().setAttribute("message", new GenericMessage(newMessage, GenericMessage.ERROR));
				}
			}
		}

		return StringHelper.collectionToString(actions, ",");
	}

	public static final void prepareModule(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ModulesContext moduleContext = ModulesContext.getInstance(ctx.getRequest().getSession(), globalContext);
		ctx.getRequest().setAttribute("modules", moduleContext.getModules());
		ctx.getRequest().setAttribute("currentModule", moduleContext.getCurrentModule());
		if (moduleContext.getFromModule() != null) {
			ctx.getRequest().setAttribute("fromModule", moduleContext.getFromModule());
		}
		GenericMessage msg = MessageRepository.getInstance(ctx).getGlobalMessage();
		if (msg != GenericMessage.EMPTY_MESSAGE) {
			ctx.getRequest().setAttribute("message", msg);
		}

		NotificationService notifService = NotificationService.getInstance(globalContext);
		ctx.getRequest().setAttribute("notificationSize", notifService.getUnreadNotificationSize(ctx.getCurrentUserId(), 99));

		String errorMsg = moduleContext.getCurrentModule().getAction().prepare(ctx, moduleContext);
		if (errorMsg != null) {
			GenericMessage genericMessage = new GenericMessage(errorMsg, GenericMessage.ERROR);
			MessageRepository.getInstance(ctx).setGlobalMessage(genericMessage);
		}
	}

	public static String getSiteKey(HttpServletRequest request) {
		return request.getServerName().toLowerCase();
	}

	public static final String executeJSP(ContentContext ctx, String url) throws ServletException, IOException {
		try {
			Jsp2String jsp2String = new Jsp2String(ctx.getResponse());
			ctx.getRequest().getRequestDispatcher(url).include(ctx.getRequest(), jsp2String);
			return jsp2String.toString();
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
			return null;
		}
	}

	public static void includeBlocked(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(503);
		request.getSession().getServletContext().getRequestDispatcher("/jsp/view/error/blocked.jsp").include(request, response);
		return;
	}

}
