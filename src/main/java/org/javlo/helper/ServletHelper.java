package org.javlo.helper;

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
import org.javlo.user.AdminUserSecurity;
import org.javlo.utilThymeleaf.TemplateEngineUtil;
import org.owasp.encoder.Encode;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ServletHelper {

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ServletHelper.class.getName());

	/**
	 * exec current action define in request
	 * 
	 * @param ctx
	 * @return the name of the action.
	 * @throws Exception
	 */
	public static final String execAction(ContentContext ctx, String forceAction) throws Exception {

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

		if (forceAction != null) {
			actions.add(forceAction);
		}

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
		AdminUserSecurity userSecurity = AdminUserSecurity.getInstance();
		ctx.getRequest().setAttribute("notificationSize", notifService.getUnreadNotificationSize(ctx.getCurrentUserId(), userSecurity.isAdmin(ctx.getCurrentEditUser()), 99));

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
			String prefix = "";
			if (!ctx.getGlobalContext().getStaticConfig().isProd()) {
				prefix = "<!-- execute jsp : " + Encode.forHtmlContent(url) + " -->\r\n";
			}
			return prefix + jsp2String.toString();
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
			return null;
		}
	}

	public static final String executeThymeleaf(HttpServletRequest request, HttpServletResponse response, String url) throws ServletException, IOException {
		System.out.println("******* Thymeleafffff *******");







		try {



			//System.out.println(url+" ******* Engine break 1 *******");

			TemplateEngine engine = TemplateEngineUtil.getTemplateEngine(request.getServletContext());


			//System.out.println( engine + " ******* Engine break 2 *******");

			WebContext context = new WebContext(request, response, request.getServletContext());
			//System.out.println( context + " ******* Engine break 4 *******");
			context.setVariable("recipient", "World");

			// engine.process(option, context, response.getWriter());
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			PrintWriter writer = new PrintWriter(stream);
			//System.out.println( writer+ " ******* Engine break 3 *******");

			engine.process(url,context, writer);




			return new String(stream.toByteArray());


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

	public static final String getContextKey(URL url) {
		String realPath = url.getPath();
		if (realPath != null && realPath.startsWith("/")) {
			realPath = realPath.substring(1);
		}
		if (realPath != null && realPath.trim().length() > 0) {
			return StringHelper.split(realPath, "/")[0].toLowerCase();
		} else {
			return StringHelper.stringToFileName(url.getHost().toLowerCase());
		}
	}

	public static void main(String[] args) {
		try {
			System.out.println("***** ServletHelper.main : 1." + getContextKey(new URL("http://localhost:8080/test")));
			System.out.println("***** ServletHelper.main : 2." + getContextKey(new URL("http://localhost:8080/")));
			System.out.println("***** ServletHelper.main : 3." + getContextKey(new URL("http://www.javlo.org/")));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // TODO: remove debug trace
	}

}
