package org.javlo.servlet;

import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.EditInfoBean;
import org.javlo.data.InfoBean;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.social.SocialService;
import org.javlo.utils.DebugListening;

public class WebAction extends HttpServlet {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(WebAction.class.getName());
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			String uri = request.getRequestURI();
			if (StringHelper.isEmpty(uri) || !uri.contains("webaction")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			String action = uri.substring(uri.indexOf("webaction")).split("/")[1];
			
			ContentContext ctx = ContentContext.getContentContext(request, response);
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			RequestService rs = RequestService.getInstance(request);
			request.setAttribute("social", SocialService.getInstance(ctx));
			try {
				// Tracker.trace(request, response);
				if (rs.getParameter(ContentContext.FORCE_MODE_PARAMETER_NAME, null) != null) {
					ctx.setRenderMode(Integer.parseInt(rs.getParameter(ContentContext.FORCE_MODE_PARAMETER_NAME, null)));
				} else {
					ctx.setRenderMode(ContentContext.PREVIEW_MODE);
				}

				if (ctx.getCurrentEditUser() != null) {
					// edit edit info bean
					EditInfoBean.getCurrentInfoBean(ctx);
				}

				EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
				request.setAttribute("editUser", editCtx.getEditUser());
				if (!ctx.isPreviewOnly()) {
					request.setAttribute("editPreview", editCtx.isPreviewEditionMode());
				}

				/*** update module status before action ***/
				ModulesContext moduleContext = ModulesContext.getInstance(request.getSession(), globalContext);
				moduleContext.initContext(request, response);

				InfoBean.updateInfoBean(ctx);
				ServletHelper.execAction(ctx, action);
				ServletHelper.prepareModule(ctx);
			} catch (Throwable t) {
				t.printStackTrace();
				logger.warning(t.getMessage());
				response.setStatus(503);
				DebugListening.getInstance().sendError(ctx, t, "path=" + request.getRequestURI());
			} finally {
				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				String persistenceParam = rs.getParameter(AccessServlet.PERSISTENCE_PARAM, null);
				if (persistenceService.isAskStore() && StringHelper.isTrue(persistenceParam, true)) {
					persistenceService.store(ctx);
				}
				if (ctx.isClearSession()) {
					HttpSession session = ctx.getRequest().getSession();
					session.invalidate();
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
