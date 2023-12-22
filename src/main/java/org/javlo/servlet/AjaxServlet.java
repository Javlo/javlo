package org.javlo.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.EditInfoBean;
import org.javlo.data.InfoBean;
import org.javlo.helper.AjaxHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.NotificationService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserSecurity;
import org.javlo.utils.DebugListening;
import org.javlo.utils.JSONMap;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
		
		//System.out.println(">>>>>>>>> AjaxServlet.process : url = "+request.getRequestURL()); //TODO: remove debug trace
		
		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			RequestService rs = RequestService.getInstance(request);
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

				ctx.setAjax(true);
				EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
				request.setAttribute("editUser", editCtx.getEditUser());
				if (!ctx.isPreviewOnly()) {
					request.setAttribute("editPreview", editCtx.isPreviewEditionMode());
				}

				/*** update module status before action ***/
				ModulesContext moduleContext = ModulesContext.getInstance(request.getSession(), globalContext);
				moduleContext.initContext(request, response);

				InfoBean.updateInfoBean(ctx);

				ServletHelper.execAction(ctx, null, false);

				ServletHelper.prepareModule(ctx);

				boolean onlyData = StringHelper.isTrue(rs.getParameter("data", null));

				Map<String, Object> outMap = new HashMap<String, Object>();
				StringWriter strWriter = new StringWriter();

				if (ctx.getSpecificJson() == null) {
					if (ctx.getAjaxMap() == null) {
						if (MessageRepository.getInstance(ctx).getGlobalMessage().isNeedDisplay()) {
							String msgXhtml;
							String htmlId = "message-container";
							if (ctx.isAsEditMode()) {
								msgXhtml = ServletHelper.executeJSP(ctx, editCtx.getMessageTemplate());
							} else {
								String msgJsp = ctx.getCurrentTemplate().getMessageTemplate(ctx);
								htmlId = ctx.getCurrentTemplate().getMessageContainerId();
								if (msgJsp != null) {
									msgXhtml = ServletHelper.executeJSP(ctx, msgJsp);
								} else {
									msgXhtml = "<div class=\"alert alert-" + MessageRepository.getInstance(ctx).getGlobalMessage().getBootstrapType() + "\" role=\"alert\">" + MessageRepository.getInstance(ctx).getGlobalMessage().getMessage() + "</div>";
								}
							}
							if (rs.getParameter("messageIdHtml") != null) {
								htmlId = rs.getParameter("messageIdHtml");
							}
							ctx.addAjaxInsideZone(htmlId, msgXhtml);
							outMap.put("messageText", MessageRepository.getInstance(ctx).getGlobalMessage().getMessage());
							outMap.put("messageType", MessageRepository.getInstance(ctx).getGlobalMessage().getBootstrapType());
						}
						if (editCtx.getUserPrincipal() != null) {
							AdminUserSecurity userSecurity = AdminUserSecurity.getInstance();
							int unreadNotification = NotificationService.getInstance(globalContext).getUnreadNotificationSize(editCtx.getUserPrincipal().getName(), userSecurity.isAdmin(editCtx.getEditUser()), 99);
							ctx.addAjaxInsideZone("notification-count", "" + unreadNotification);
						}
						AjaxHelper.render(ctx, ctx.getAjaxInsideZone(), ctx.getScheduledAjaxInsideZone());
						if (!onlyData) {
							outMap.put("insideZone", ctx.getAjaxInsideZone());
							outMap.put("zone", ctx.getAjaxZone());
						}
						if (ctx.getAjaxData().size() > 0) {
							outMap.put("data", ctx.getAjaxData());
						}
						JSONMap.JSON.toJson(outMap, strWriter);
					} else {
						for (Object key : ctx.getAjaxMap().keySet()) {
							outMap.put("" + key, ctx.getAjaxMap().get(key));
						}
						JSONMap.JSON.toJson(outMap, strWriter);
					}
					strWriter.flush();
				}

				response.setContentType("application/json");
				String jsonResult = strWriter.toString();
			
				if (ctx.getSpecificJson() != null) {
					response.getWriter().write(ctx.getSpecificJson());
				} else {
					response.getWriter().write(jsonResult);
				}
			
				response.flushBuffer();

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
