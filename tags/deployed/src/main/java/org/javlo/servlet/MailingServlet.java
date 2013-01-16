package org.javlo.servlet;

import java.security.Principal;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.actions.ActionManager;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

public class MailingServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * create a static logger.
	 */
	public static Logger logger = Logger.getLogger(MailingServlet.class.getName());

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	public void destroy() {
		super.destroy();
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
		try {
			GlobalContext globalContext = GlobalContext.getInstance(request);
			Principal logoutUser = null;
			if (request.getParameter("edit-logout") != null) {
				IUserFactory fact = UserFactory.createUserFactory(globalContext, request.getSession());
				logoutUser = fact.getCurrentUser(request.getSession());
				request.getSession().invalidate();
				request.getSession(true);
			}

			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
			editCtx.setMailing(true);

			RequestService requestService = RequestService.getInstance(request);

			response.setContentType("text/html");

			if (logoutUser != null) {
				globalContext.logout(logoutUser);

				if (request.getUserPrincipal() != null) {
					ContentContext ctx = ContentContext.getContentContext(request, response);
					response.sendRedirect(URLHelper.createURL(ctx));
					return;
				}
			}

			if (request.getParameter("edit-login") != null) {
				String login = request.getParameter("j_username");
				String password = request.getParameter("j_password");
				IUserFactory adminFactory = AdminUserFactory.createUserFactory(globalContext, request.getSession());
				User editUser = adminFactory.login(request, login, password);
				if (editUser != null) {
					globalContext.addPrincipal(editUser);
					globalContext.eventLogin(editUser.getLogin());
					logger.info(login + " is logged.");
					if (adminFactory.getCurrentUser(request.getSession()).getUserInfo().getPreferredLanguage() != null && adminFactory.getCurrentUser(request.getSession()).getUserInfo().getPreferredLanguage().length > 0) {
						String lg = adminFactory.getCurrentUser(request.getSession()).getUserInfo().getPreferredLanguage()[0];
						if (globalContext.getLanguages().contains(lg)) {
							ContentContext ctx = ContentContext.getContentContext(request, response);
							ctx.setLanguage(lg);
						} else {
							logger.warning("preferred language found but not defined for : " + adminFactory.getCurrentUser(request.getSession()) + "  lg:" + lg);
						}
					} else {
						logger.warning("preferred language not found for : " + adminFactory.getCurrentUser(request.getSession()));
					}
				}

			}

			if (request.getUserPrincipal() != null && editCtx.getUserPrincipal() == null && logoutUser == null) {				
				IUserFactory adminFactory = AdminUserFactory.createUserFactory(globalContext, request.getSession());
				User user = adminFactory.login(request, request.getUserPrincipal().getName(), "");
				if (user == null) {
					logger.fine("user not found");
					user = editCtx.getEditUser(request.getUserPrincipal().getName());
					editCtx.setEditUser(user);
					/** set user language as default language **/

					if (adminFactory.getCurrentUser(request.getSession()) != null && adminFactory.getCurrentUser(request.getSession()) != null) {
						if (adminFactory.getCurrentUser(request.getSession()).getUserInfo().getPreferredLanguage() != null && adminFactory.getCurrentUser(request.getSession()).getUserInfo().getPreferredLanguage().length > 0) {
							String lg = adminFactory.getCurrentUser(request.getSession()).getUserInfo().getPreferredLanguage()[0];
							if (globalContext.getLanguages().contains(lg)) {
								ContentContext ctx = ContentContext.getContentContext(request, response);
								ctx.setLanguage(lg);
							}
						} else {
							logger.warning("preferred language not found for : " + adminFactory.getCurrentUser(request.getSession()));
						}
					}
				} else {
					logger.fine("user found : "+user.getName());
				}
			}

			if (editCtx.getUserPrincipal() == null) {
				logger.warning("no principal user found.");
				getServletContext().getRequestDispatcher("/jsp/edit/login.jsp").include(request, response);
			} else {
				/* ACTION */
				String action = requestService.getParameter("webaction", null);

				if (action != null) {
					if ((request.getServletPath().equals("/edit") || request.getServletPath().equals("/admin")) && (editCtx.getUserPrincipal() == null)) {
						logger.warning("block action : '" + action + "' because user is not logged.");
					} else {
						String newMessage = null;
						newMessage = ActionManager.perform(action, request, response);
						if (newMessage != null) {
							request.setAttribute("message", newMessage);
						}
					}
				} else {
					action = "undefined";
				}
				getServletContext().getRequestDispatcher("/jsp/mailing/index.jsp").include(request, response);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}