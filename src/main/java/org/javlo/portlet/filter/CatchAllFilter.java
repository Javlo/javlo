package org.javlo.portlet.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.javlo.actions.MailingActions;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.ContentManager;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.StringSecurityUtil;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.DataToIDService;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.user.UserPrincipal;
import org.javlo.utils.DebugListening;

public class CatchAllFilter implements Filter {

	/**
	 * create a static logger.
	 */
	public static Logger logger = Logger.getLogger(CatchAllFilter.class.getName());

	public static void doLoginFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {

		logger.fine("start login filter");

		try {

			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			GlobalContext globalContext = GlobalContext.getInstance(httpRequest);

			// ContentContext ctx = ContentContext.getContentContext(httpRequest, httpResponse);

			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, httpRequest.getSession());
			RequestService requestService = RequestService.getInstance(httpRequest);

			/*String loginId = requestService.getParameter(RemoteLoginService.PARAM_NAME, null);
			if (loginId != null) {
				logger.fine("try a remote login with id : " + loginId);
				RemoteLoginService remoteLoginService = RemoteLoginService.getInstance(httpRequest.getSession().getServletContext());
				User user = remoteLoginService.login(loginId);
				if (user != null) {
					IUserFactory fact = AdminUserFactory.createUserFactory(globalContext, httpRequest.getSession());
					fact.setCurrentUser(httpRequest.getSession(), user);
					EditContext editCtx = EditContext.getInstance(globalContext, httpRequest.getSession());
					editCtx.setEditUser(user);
					// next.doFilter(httpRequest, response);
					return;
				}
			}*/

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
				// next.doFilter(httpRequest, response);
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
								//RequestHelper.setCookieValue(httpResponse, "javlo_login_id", codeId, IUserFactory.AUTO_LOGIN_AGE_SEC);								
								String pathPrefix = URLHelper.getPathPrefix((HttpServletRequest)request);
								RequestHelper.setCookieValue(httpResponse, "javlo_login_id", codeId, IUserFactory.AUTO_LOGIN_AGE_SEC, URLHelper.mergePath(pathPrefix,"/edit"));
								RequestHelper.setCookieValue(httpResponse, "javlo_login_id", codeId, IUserFactory.AUTO_LOGIN_AGE_SEC, URLHelper.mergePath(pathPrefix,"/preview"));
							}

							if (login == null && httpRequest.getUserPrincipal() != null) {
								login = httpRequest.getUserPrincipal().getName();
							}							
							fact.login(httpRequest, login, request.getParameter("j_password"));
							ModulesContext.getInstance(httpRequest.getSession(), globalContext).loadModule(httpRequest.getSession(), globalContext);
						}
					}
				}
				fact.getCurrentUser(((HttpServletRequest) request).getSession());
			}
			
			boolean newUser = false;

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
						newUser = true;
					}
				}
				if (ContentManager.isEdit((HttpServletRequest) request) || ContentManager.isPreview((HttpServletRequest) request)) {
					if (autoLoginUser != null) {
						IUserFactory adminFactory = AdminUserFactory.createUserFactory(globalContext, httpRequest.getSession());
						User principalUser = adminFactory.autoLogin(httpRequest, autoLoginUser);
						if (principalUser != null) {
							globalContext.addPrincipal(principalUser);
							newUser = true;
						}
					}
				}
			}

			if (request.getParameter("edit-login") != null || (httpRequest.getUserPrincipal() != null && logoutUser == null)) {
				String login = request.getParameter("j_username");
				
				if (login == null && httpRequest.getUserPrincipal() != null) {
					login = httpRequest.getUserPrincipal().getName();
				}
				AdminUserFactory adminFactory = AdminUserFactory.createUserFactory(globalContext, httpRequest.getSession());
				User editUser = adminFactory.login(httpRequest, login, request.getParameter("j_password"));
				
				if (editUser != null) {
					if (request.getParameter("autologin") != null) {
						DataToIDService service = DataToIDService.getInstance(httpRequest.getSession().getServletContext());
						String codeId = service.setData(login, ((long) IUserFactory.AUTO_LOGIN_AGE_SEC) * 1000);
						//RequestHelper.setCookieValue(httpResponse, "javlo_login_id", codeId, IUserFactory.AUTO_LOGIN_AGE_SEC);
						String pathPrefix = URLHelper.getPathPrefix((HttpServletRequest)request);
						RequestHelper.setCookieValue(httpResponse, "javlo_login_id", codeId, IUserFactory.AUTO_LOGIN_AGE_SEC, URLHelper.mergePath(pathPrefix,"/edit"));
						RequestHelper.setCookieValue(httpResponse, "javlo_login_id", codeId, IUserFactory.AUTO_LOGIN_AGE_SEC, URLHelper.mergePath(pathPrefix,"/preview"));
					}
					globalContext.addPrincipal(editUser);					
					globalContext.eventLogin(editUser.getLogin());
					newUser = true;

					logger.info(login + " is logged roles : [" + StringHelper.collectionToString(editUser.getRoles(), ",") + ']');

				} else {
					logger.info(login + " fail to login.");
				}

			}
			
			if (newUser) {				
				ModulesContext.getInstance(httpRequest.getSession(), globalContext).loadModule(httpRequest.getSession(), globalContext);
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain next) throws IOException, ServletException {

		logger.fine("start catch all servelt.");

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		ServletContext servletContext = httpRequest.getSession().getServletContext();

		if (request.getParameter(ContentContext.FORWARD_PATH_REQUEST_KEY) != null) {
			String forwardURL = request.getParameter(ContentContext.FORWARD_PATH_REQUEST_KEY);
			((HttpServletResponse) response).sendRedirect(forwardURL);
			return;
		}

		String uri = RequestService.getURI(httpRequest);

		String host = ServletHelper.getSiteKey(httpRequest);
		StaticConfig staticConfig = StaticConfig.getInstance(servletContext);

		/*****************************/
		/**** INIT GLOBAL CONTEXT ****/
		/*****************************/

		// TODO: check this creation
		GlobalContext globalContext = null;
		String forwardURI = null;
		boolean hostDefineSite = staticConfig.isHostDefineSite();
		RequestService requestService = RequestService.getInstance(httpRequest);
		try {
			if (!staticConfig.isHostDefineSite()) {
				if (StringHelper.isTrue(requestService.getParameter("__check_context", "true"))) {
					String contextURI = ContentManager.getContextName(httpRequest);
					if (GlobalContext.isExist(httpRequest, contextURI)) {
						globalContext = GlobalContext.getInstance(httpRequest, contextURI);
						globalContext.setPathPrefix(contextURI);
						String newURI = httpRequest.getServletPath();
						httpRequest.getRequestURI();
						newURI = newURI.replaceFirst('/' + contextURI, "");
						if (httpRequest.getQueryString() != null) {
							newURI = newURI + '?' + httpRequest.getQueryString();
						}
						newURI = URLHelper.addParam(newURI, "__check_context", "false");

						newURI = ((HttpServletResponse) response).encodeURL(newURI);

						forwardURI = newURI;
					}
				} else {
					globalContext = GlobalContext.getInstance(httpRequest);
				}
			}
			if (globalContext == null) { // if no context found search a host context.
				if (StringHelper.isTrue(requestService.getParameter("__check_context", "false"))) {
					globalContext = GlobalContext.getInstance(httpRequest);
				}
				if (globalContext == null) {
					globalContext = GlobalContext.getInstance(httpRequest, host);
					hostDefineSite = true;
				}
			}
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		}

		if (staticConfig.isRequestWrapper()) {
			if (httpRequest.getMethod().trim().equalsIgnoreCase("post")) {
				logger.finest("create request wrapper.");
				MultiReadRequestWrapper rw = new MultiReadRequestWrapper(httpRequest);
				httpRequest = rw;
			}
		}

		if (globalContext == null) {
			((HttpServletResponse) response).setStatus(HttpServletResponse.SC_NOT_FOUND, "context not found.");
			return;
		}

		/***************/
		/**** LOGIN ****/
		/***************/

		doLoginFilter(request, response);

		/*************/
		/**** URL ****/
		/*************/

		if (request.getParameter(RequestHelper.CRYPTED_PARAM_NAME) != null) {
			try {
				String cryptedData = request.getParameter(RequestHelper.CRYPTED_PARAM_NAME);
				String decryptedData = StringSecurityUtil.decode(cryptedData, staticConfig.getSecretKey());
				request.setAttribute(StringSecurityUtil.REQUEST_ATT_FOR_SECURITY_FORWARD, "true");
				String url = ((HttpServletRequest) request).getRequestURI();
				if (request.getAttribute(MailingActions.MAILING_FEEDBACK_PARAM_NAME) != null) {
					url = URLHelper.addParam(url, MailingActions.MAILING_FEEDBACK_PARAM_NAME, "" + request.getAttribute(MailingActions.MAILING_FEEDBACK_PARAM_NAME));
				}
				httpRequest.getRequestDispatcher(url + decryptedData).forward(httpRequest, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

			/******************/
			/**** ADD VIEW ****/
			/******************/

			if (!staticConfig.isViewPrefix()) {
				String viewURI = uri;
				if (forwardURI != null) {
					viewURI = forwardURI;
				}
				if (viewURI.length() > 2) {
					char sep = '/';
					if (viewURI.length() > 3) {
						sep = viewURI.charAt(3);
					}
					if (sep == '/' || sep == '-') {
						String lg = viewURI.substring(1, 3).toLowerCase();
						if (globalContext.getContentLanguages().contains(lg)) {
							String newPath = "/view" + viewURI;							
							httpRequest.getRequestDispatcher(newPath).forward(httpRequest, response);
							return;
						}
					}
				}
			}
			
			 if (request.getParameter("webaction") != null && request.getParameter("webaction").equals("view.language")) {
					try {
						ContentContext ctx = ContentContext.getContentContext((HttpServletRequest) request, (HttpServletResponse) response);
						String lang = request.getParameter("lg");
						if (lang != null) {
							if (globalContext.getLanguages().contains(lang)) {
								ctx.setLanguage(lang);
								ctx.setContentLanguage(lang);
								ctx.setRequestContentLanguage(null);
								ctx.setCookieLanguage(lang);
							}
							String newURL = URLHelper.createURL(ctx);
							NetHelper.sendRedirectPermanently((HttpServletResponse) response, newURL);
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			 }

			initElements(request, response);

			globalContext = GlobalContext.getInstance(httpRequest);

			if (!ContentManager.isEdit(httpRequest, !hostDefineSite) && !ContentManager.isAdmin(httpRequest, !hostDefineSite) && !ContentManager.isPreview(httpRequest, !hostDefineSite)) {
				Map<String, String> uriAlias = globalContext.getURIAlias();
				Collection<Map.Entry<String, String>> entries = uriAlias.entrySet();
				for (Map.Entry<String, String> entry : entries) {
					if (uri.length() > 0) {
						String pattern1 = entry.getKey();
						String pattern2 = entry.getValue();
						if (!pattern1.contains("*")) {
							if (uri.equals(pattern1)) {
								// ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
								// ((HttpServletResponse) response).sendRedirect(pattern2);
								NetHelper.sendRedirectPermanently((HttpServletResponse) response, pattern2);
								return;
							}
						} else {
							String newURL = StringHelper.convertString(pattern1, pattern2, uri);
							if (!newURL.equals(uri)) {
								// ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
								// ((HttpServletResponse) response).sendRedirect(newURL);
								NetHelper.sendRedirectPermanently((HttpServletResponse) response, newURL);
								return;
							}
						}
					}
				}
			}

			if (httpRequest.getUserPrincipal() != null) {
				logger.fine("principal user found : " + httpRequest.getUserPrincipal());
				globalContext.addPrincipal(new UserPrincipal(httpRequest.getUserPrincipal()));
			}

			if (forwardURI != null) {
				httpRequest.getRequestDispatcher(forwardURI).forward(httpRequest, response);
			} else {
				next.doFilter(httpRequest, response);
			}

		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	public void initElements(ServletRequest request, ServletResponse response) {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		DebugListening.getInstance().setAppplication(httpRequest.getSession().getServletContext());
	}
}