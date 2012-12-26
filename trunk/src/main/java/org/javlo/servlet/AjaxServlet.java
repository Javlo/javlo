package org.javlo.servlet;

import java.io.StringWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.AjaxHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.NotificationService;
import org.javlo.tracking.Tracker;
import org.json.JSONObject;

public class AjaxServlet extends HttpServlet {

	private static final long serialVersionUID = -2086338711912267925L;

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(AjaxServlet.class.getName());

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		System.out.println("***** AjaxServlet.process : request = " + request); // TODO: remove debug trace
		System.out.println("***** AjaxServlet.process : response = " + response); // TODO: remove debug trace
		try {

			ContentContext ctx = ContentContext.getContentContext(request, response);

			Tracker.trace(request, response);

			ctx.setAjax(true);
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());

			/*** update module status before action ***/
			ModulesContext moduleContext = ModulesContext.getInstance(request.getSession(), globalContext);
			moduleContext.initContext(request, response);

			InfoBean.updateInfoBean(ctx);

			String action = ServletHelper.execAction(ctx);
			if (action != null) {
				logger.info("exec action : " + action);
			}

			ServletHelper.prepareModule(ctx);

			JSONObject outMap = new JSONObject();
			StringWriter strWriter = new StringWriter();

			if (ctx.getAjaxMap() == null) {
				String msgXhtml = ServletHelper.executeJSP(ctx, editCtx.getMessageTemplate());
				ctx.addAjaxInsideZone("message-container", msgXhtml);
				if (editCtx.getUserPrincipal() != null) {
					int unreadNotification = NotificationService.getInstance(globalContext).getUnreadNotificationSize(editCtx.getUserPrincipal().getName(), 99);
					ctx.addAjaxInsideZone("notification-count", "" + unreadNotification);
				}
				AjaxHelper.render(ctx, ctx.getAjaxInsideZone(), ctx.getScheduledAjaxInsideZone());
				outMap.put("insideZone", ctx.getAjaxInsideZone());
				outMap.put("zone", ctx.getAjaxZone());
				outMap.write(strWriter);
			} else {
				for (Object key : ctx.getAjaxMap().keySet()) {
					outMap.put("" + key, ctx.getAjaxMap().get(key));
				}
				outMap.write(strWriter);
			}
			strWriter.flush();
			response.setContentType("application/json");
			String jsonResult = strWriter.toString();
			response.getWriter().write(jsonResult);
			response.flushBuffer();

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
