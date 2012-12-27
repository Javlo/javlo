package org.javlo.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentManager;
import org.javlo.context.GlobalContext;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.DataToIDService;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.RemoteLoginService;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

public class LoginFilter implements Filter {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(LoginFilter.class.getName());

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain next) throws IOException, ServletException {

		logger.fine("start login filter");

		try {

			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			GlobalContext globalContext = GlobalContext.getInstance(httpRequest);

			// ContentContext ctx = ContentContext.getContentContext(httpRequest, httpResponse);

			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, httpRequest.getSession());
			RequestService requestService = RequestService.getInstance(httpRequest);

			String loginId = requestService.getParameter(RemoteLoginService.PARAM_NAME, null);
			/*
			 * if (loginId != null) { logger.fine("try a remote login with id : " + loginId); RemoteLoginService remoteLoginService = RemoteLoginService.getInstance(httpRequest.getSession().getServletContext()); User user = remoteLoginService.login(loginId); if (user != null) { IUserFactory fact = AdminUserFactory.createUserFactory(globalContext, httpRequest.getSession()); fact.setCurrentUser(httpRequest.getSession(), user); EditContext editCtx = EditContext.getInstance(globalContext, httpRequest.getSession()); editCtx.setEditUser(user); next.doFilter(httpRequest, response); return; } }
			 */

			Principal logoutUser = null;

			if (request.getParameter("edit-logout") != null) {
				IUserFactory fact = UserFactory.createUserFactory(globalContext, httpRequest.getSession());
				logoutUser = fact.getCurrentUser(httpRequest.getSession());
				if (logoutUser != null) {
					DataToIDService service = DataToIDService.getInstance(httpRequest.getSession().getServletContext());
					service.clearData(logoutUser.getName());

					globalContext.logout(logoutUser);

					if (httpRequest.getUserPrincipal() != null) {
						httpResponse.sendRedirect("/");
						return;
					}
				}
				httpRequest.getSession().invalidate();
				httpRequest.getSession(true);
				next.doFilter(httpRequest, response);
				return;
			}

			/** STANDARD LOGIN **/

			IUserFactory fact = UserFactory.createUserFactory(globalContext, httpRequest.getSession());
			if (fact.getCurrentUser(((HttpServletRequest) request).getSession()) == null) {

				String loginType = requestService.getParameter("login-type", null);

				if ((loginType == null || !loginType.equals("adminlogin")) && logoutUser == null) {

					if (fact.getCurrentUser(((HttpServletRequest) request).getSession()) == null) {
						if (request.getParameter("j_username") != null || httpRequest.getUserPrincipal() != null) {
							String login = request.getParameter("j_username");

							if (request.getParameter("autologin") != null) {
								DataToIDService service = DataToIDService.getInstance(httpRequest.getSession().getServletContext());
								String codeId = service.setData(login, IUserFactory.AUTO_LOGIN_AGE_SEC * 1000);
								RequestHelper.setCookieValue(httpResponse, "javlo_login_id", codeId, IUserFactory.AUTO_LOGIN_AGE_SEC, "/edit");
								RequestHelper.setCookieValue(httpResponse, "javlo_login_id", codeId, IUserFactory.AUTO_LOGIN_AGE_SEC, "/preview");
							}

							if (login == null && httpRequest.getUserPrincipal() != null) {
								login = httpRequest.getUserPrincipal().getName();
							}

							fact.login(httpRequest, login, request.getParameter("j_password"));
						}
					}
				}
				fact.getCurrentUser(((HttpServletRequest) request).getSession());
			}

			/** EDIT LOGIN **/

			if (fact.getCurrentUser(((HttpServletRequest) request).getSession()) == null) {
				/* AUTO LOGIN */
				String autoLoginId = RequestHelper.getCookieValue(httpRequest, "javlo_login_id");
				String autoLoginUser = null;
				if (autoLoginId != null) {
					DataToIDService service = DataToIDService.getInstance(httpRequest.getSession().getServletContext());
					service.clearTimeData();
					autoLoginUser = service.getData(autoLoginId);

					if (autoLoginUser != null) {
						logger.info("autologin for : " + autoLoginUser);
						String msg = i18nAccess.getText("user.autologin", new String[][] { { "login", autoLoginUser } });
						MessageRepository messageRepository = MessageRepository.getInstance(((HttpServletRequest) request));
						messageRepository.setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
					}
				}
				if (ContentManager.isEdit((HttpServletRequest) request) || ContentManager.isPreview((HttpServletRequest) request)) {
					if (autoLoginUser != null) {
						IUserFactory adminFactory = AdminUserFactory.createUserFactory(globalContext, httpRequest.getSession());
						User principalUser = adminFactory.autoLogin(httpRequest, autoLoginUser);
						if (principalUser != null) {
							globalContext.addPrincipal(principalUser);
						}
					}
				}
			}

			if (request.getParameter("edit-login") != null || (httpRequest.getUserPrincipal() != null && logoutUser == null)) {
				String login = request.getParameter("j_username");

				if (login == null && httpRequest.getUserPrincipal() != null) {
					login = httpRequest.getUserPrincipal().getName();
				}
				IUserFactory adminFactory = AdminUserFactory.createUserFactory(globalContext, httpRequest.getSession());
				User editUser = adminFactory.login(httpRequest, login, request.getParameter("j_password"));
				if (editUser != null) {
					if (request.getParameter("autologin") != null) {
						DataToIDService service = DataToIDService.getInstance(httpRequest.getSession().getServletContext());
						String codeId = service.setData(login, ((long) IUserFactory.AUTO_LOGIN_AGE_SEC) * 1000);
						RequestHelper.setCookieValue(httpResponse, "javlo_login_id", codeId, IUserFactory.AUTO_LOGIN_AGE_SEC, "/edit");
						RequestHelper.setCookieValue(httpResponse, "javlo_login_id", codeId, IUserFactory.AUTO_LOGIN_AGE_SEC, "/preview");
					}
					globalContext.addPrincipal(editUser);

					globalContext.eventLogin(editUser.getLogin());

					logger.info(login + " is logged roles : [" + StringHelper.collectionToString(editUser.getRoles(), ",") + ']');

				} else {
					logger.info(login + " fail to login.");
				}

			}
			next.doFilter(httpRequest, response);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	public void initElements(ServletRequest request, ServletResponse response) {
	}
}