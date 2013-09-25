package org.javlo.filter;

import java.io.IOException;
import java.net.URL;
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
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.ContentManager;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.UserInterfaceContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.NetHelper;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.StringSecurityUtil;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.module.mailing.MailingAction;
import org.javlo.navigation.MenuElement;
import org.javlo.portlet.filter.MultiReadRequestWrapper;
import org.javlo.service.ContentService;
import org.javlo.service.CountService;
import org.javlo.service.DataToIDService;
import org.javlo.service.RequestService;
import org.javlo.template.Template;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
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

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain next) throws IOException, ServletException {
		logger.fine("start catch all servelt.");

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		ServletContext servletContext = httpRequest.getSession().getServletContext();

		CountService.getInstance(servletContext).touch();

		if (request.getParameter(ContentContext.FORWARD_PATH_REQUEST_KEY) != null) {
			String forwardURL = request.getParameter(ContentContext.FORWARD_PATH_REQUEST_KEY);
			((HttpServletResponse) response).sendRedirect(forwardURL);
			return;
		}

		String uri = RequestService.getURI(httpRequest);

		if (uri.endsWith(Template.GZ_FILE_EXT)) {
			String realFile = uri.substring(0, uri.length() - ('.' + Template.GZ_FILE_EXT).length());
			((HttpServletResponse) response).setHeader("Content-Encoding", "gzip");
			((HttpServletResponse) response).setHeader("Content-Type", ResourceHelper.getFileExtensionToManType(StringHelper.getFileExtension(realFile)));
		}

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
			if (!hostDefineSite) {
				if (StringHelper.isTrue(requestService.getParameter("__check_context", "true"))) {
					String contextURI = ContentManager.getContextName(httpRequest);
					if (GlobalContext.isExist(httpRequest, contextURI)) {
						globalContext = GlobalContext.getInstance(httpRequest.getSession(), contextURI);
						globalContext.setPathPrefix(contextURI);
						String newURI = httpRequest.getServletPath();
						httpRequest.getRequestURI();
						newURI = newURI.replaceFirst('/' + contextURI, "");
						if (httpRequest.getQueryString() != null) {
							newURI = newURI + '?' + httpRequest.getQueryString();
						}
						newURI = URLHelper.addParam(newURI, "__check_context", "false");

						// newURI = ((HttpServletResponse)
						// response).encodeURL(newURI);

						forwardURI = newURI;
					}
				} else {
					globalContext = GlobalContext.getInstance(httpRequest);
				}
			}
			if (globalContext == null) { // if no context found search a host
											// context.
				if (StringHelper.isTrue(requestService.getParameter("__check_context", "false"))) {
					globalContext = GlobalContext.getInstance(httpRequest);
				}
				if (globalContext == null) {
					globalContext = GlobalContext.getInstance(httpRequest.getSession(), host);
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

		boolean newUser = doLoginFilter(request, response);
		User user = UserFactory.createUserFactory(globalContext, httpRequest.getSession()).getCurrentUser(httpRequest.getSession());

		/*****************/
		/**** MODULES ****/
		/*****************/

		String editURI = uri;
		if (editURI.startsWith('/' + globalContext.getContextKey())) {
			editURI = editURI.substring(globalContext.getContextKey().length() + 1);
		}

		if (user != null && AdminUserSecurity.getInstance().haveRole(user, AdminUserSecurity.CONTRIBUTOR_ROLE) && (!ContentContext.isEditPreview(httpRequest))) {
			if (editURI.startsWith("/edit")) {
				httpRequest.getRequestDispatcher(editURI.replaceFirst("/edit", "/preview")).forward(request, response);
				return;
			}
		}

		if (editURI.startsWith("/edit-") || editURI.startsWith("/ajax-") || editURI.startsWith("/preview-edit")) {
			boolean editPreview = false;
			if (editURI.startsWith("/preview-edit")) {
				editPreview = true;
				editURI = editURI.replaceFirst("/preview-", "/");
				if (newUser) {
					try {
						ContentContext ctx = ContentContext.getContentContext(httpRequest, (HttpServletResponse) response);
						ctx.setClosePopup(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			String prefix = editURI.substring(0, 5);
			editURI = editURI.substring("/edit-".length());
			String module = editURI;
			if (editURI.contains("/")) {
				module = module.substring(0, module.indexOf('/'));
				editURI = editURI.substring(editURI.indexOf('/'));
			}
			if (module.length() > 0) {
				editURI = prefix + editURI;
				String query = httpRequest.getQueryString();
				if (query != null) {
					editURI = editURI + '?' + query;
				}
				if (query == null || !query.contains("module=")) {
					try {
						ModulesContext.getInstance(httpRequest.getSession(), GlobalContext.getInstance(httpRequest)).setCurrentModule(module);
					} catch (ModuleException e) {
						e.printStackTrace();
					}
				}

				if (query != null && query.contains("edit-logout")) {
					((HttpServletResponse) response).sendRedirect("" + httpRequest.getRequestURL());
					return;
				} else {
					if (editPreview) {
						editURI = URLHelper.addParam(editURI, "previewEdit", "true");
					}
					httpRequest.getRequestDispatcher(editURI).forward(request, response);
					return;
				}
			}
		}

		/*************/
		/**** URL ****/
		/*************/

		if (request.getParameter(RequestHelper.CRYPTED_PARAM_NAME) != null) {
			try {
				String cryptedData = request.getParameter(RequestHelper.CRYPTED_PARAM_NAME);
				String decryptedData = StringSecurityUtil.decode(cryptedData, staticConfig.getSecretKey());
				request.setAttribute(StringSecurityUtil.REQUEST_ATT_FOR_SECURITY_FORWARD, "true");
				String url = httpRequest.getRequestURL().toString();
				if (request.getAttribute(MailingAction.MAILING_FEEDBACK_PARAM_NAME) != null) {
					url = URLHelper.addParam(url, MailingAction.MAILING_FEEDBACK_PARAM_NAME, "" + request.getAttribute(MailingAction.MAILING_FEEDBACK_PARAM_NAME));
				}

				URL newURL = new URL(url + decryptedData);
				for (Map.Entry<String, String> entry : URLHelper.getParams(newURL).entrySet()) {
					requestService.putParameter(entry.getKey(), entry.getValue());
				}

				// httpRequest.getRequestDispatcher(url +
				// decryptedData).forward(request, response);
				// return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/******************/
		/**** SHORT URL ***/
		/******************/

		String shortURI = uri;
		if (shortURI.startsWith('/' + globalContext.getContextKey())) {
			if (shortURI.length() > globalContext.getContextKey().length() + 2) {
				shortURI = shortURI.substring(globalContext.getContextKey().length() + 2);
			} else {
				shortURI = "";
			}
		}

		if (shortURI.length() == globalContext.getStaticConfig().getShortURLSize() + 1 && shortURI.startsWith("U")) {
			ContentContext ctx = null;
			try {
				ctx = ContentContext.getContentContext((HttpServletRequest) request, (HttpServletResponse) response);
				if (ctx.isAsViewMode()) {
					ContentService content = ContentService.getInstance(globalContext);
					MenuElement page = content.getPageWithShortURL(ctx, shortURI);
					if (page != null) {
						String newURL = URLHelper.createURLWithtoutEncodeURL(ctx, page.getPath());
						NetHelper.sendRedirectPermanently((HttpServletResponse) response, newURL);
						return;
					}

				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}

		globalContext = GlobalContext.getInstance(httpRequest);

		if (!ContentManager.isEdit(httpRequest, !hostDefineSite) && !ContentManager.isPreview(httpRequest, !hostDefineSite)) {
			Map<String, String> uriAlias = globalContext.getURIAlias();
			Collection<Map.Entry<String, String>> entries = uriAlias.entrySet();
			for (Map.Entry<String, String> entry : entries) {
				String cmsURI = uri;
				if (ContentContext.getPathPrefix((HttpServletRequest) request) != null && ContentContext.getPathPrefix((HttpServletRequest) request).length() > 0) {
					cmsURI = cmsURI.replaceFirst("/" + ContentContext.getPathPrefix((HttpServletRequest) request), "");
				}
				if (cmsURI.length() > 1) {
					String pattern1 = entry.getKey();
					String pattern2 = entry.getValue();
					if (!pattern1.contains("*")) {
						if (cmsURI.equals(pattern1)) {
							pattern2 = URLHelper.mergePath("/", ContentContext.getPathPrefix((HttpServletRequest) request), pattern2);
							NetHelper.sendRedirectPermanently((HttpServletResponse) response, pattern2);
							return;
						}
					} else {
						String newURL = StringHelper.convertString(pattern1, pattern2, cmsURI);
						if (!newURL.equals(cmsURI)) {
							newURL = URLHelper.mergePath("/", ContentContext.getPathPrefix((HttpServletRequest) request), newURL);
							NetHelper.sendRedirectPermanently((HttpServletResponse) response, newURL);
							return;
						}
					}
				}
			}
		}

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
				if (sep == '/' || sep == '-' || sep == '?' || viewURI.length() == 3) {
					String lg = viewURI.substring(1, 3).toLowerCase();
					if (globalContext.getContentLanguages().contains(lg)) {
						String newPath = "/view" + viewURI;
						if (httpRequest.getSession().isNew() || StringHelper.isTrue(request.getParameter(InfoBean.NEW_SESSION_PARAM))) {
							newPath = URLHelper.addParam(newPath, InfoBean.NEW_SESSION_PARAM, "true");
						}
						httpRequest.getRequestDispatcher(newPath).forward(httpRequest, response);
						return;
					}
				}
			}
		}

		if (httpRequest.getUserPrincipal() != null) {			
			logger.fine("principal user found : " + httpRequest.getUserPrincipal());
			globalContext.addPrincipal(new UserPrincipal(httpRequest.getUserPrincipal()));
		}

		if (forwardURI != null) {

			if (httpRequest.getSession().isNew() || StringHelper.isTrue(request.getParameter(InfoBean.NEW_SESSION_PARAM))) {
				forwardURI = URLHelper.addParam(forwardURI, InfoBean.NEW_SESSION_PARAM, "true");
			}
			httpRequest.getRequestDispatcher(forwardURI).forward(httpRequest, response);
		} else {
			next.doFilter(httpRequest, response);
		}
	}

	public static boolean doLoginFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {

		logger.fine("start login filter");

		try {

			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			GlobalContext globalContext = GlobalContext.getInstance(httpRequest);

			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, httpRequest.getSession());
			RequestService requestService = RequestService.getInstance(httpRequest);

			Principal logoutUser = null;

			if (request.getParameter("edit-logout") != null) {
				IUserFactory fact = UserFactory.createUserFactory(globalContext, httpRequest.getSession());
				logoutUser = fact.getCurrentUser(httpRequest.getSession());
				if (logoutUser != null) {
					DataToIDService service = DataToIDService.getInstance(httpRequest.getSession().getServletContext());
					service.clearData(logoutUser.getName());
					globalContext.logout(logoutUser);
					if (httpRequest.getUserPrincipal() != null) {
						httpResponse.sendRedirect("" + httpRequest.getRequestURL());
					}
				}
				httpRequest.getSession().invalidate();
				httpRequest.getSession(true);
			}

			/** STANDARD LOGIN **/
			IUserFactory fact = UserFactory.createUserFactory(globalContext, httpRequest.getSession());
			User user = fact.getCurrentUser(((HttpServletRequest) request).getSession());
			EditContext editContext = EditContext.getInstance(GlobalContext.getInstance(((HttpServletRequest) request).getSession(), globalContext.getContextKey()), ((HttpServletRequest) request).getSession());
			if (user != null) {
				if (!user.getContext().equals(globalContext.getContextKey())) {
					if (!AdminUserSecurity.getInstance().isGod(user) && !AdminUserSecurity.getInstance().isMaster(user)) {
						try {
							editContext.setEditUser(null);
							logger.info("remove user '" + user.getLogin() + "' context does'nt match.");
							user = null;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}

			if (user != null && editContext.getEditUser() == null) {
				editContext.setEditUser(user);
			}

			if (fact.getCurrentUser(((HttpServletRequest) request).getSession()) == null) {

				String loginType = requestService.getParameter("login-type", null);

				if ((loginType == null || !loginType.equals("adminlogin")) && logoutUser == null) {

					if (fact.getCurrentUser(((HttpServletRequest) request).getSession()) == null) {
						if (request.getParameter("j_username") != null || httpRequest.getUserPrincipal() != null) {
							String login = request.getParameter("j_username");

							if (request.getParameter("autologin") != null) {
								DataToIDService service = DataToIDService.getInstance(httpRequest.getSession().getServletContext());
								String codeId = service.setData(login, IUserFactory.AUTO_LOGIN_AGE_SEC);
								RequestHelper.setCookieValue(httpResponse, "javlo_login_id", codeId, IUserFactory.AUTO_LOGIN_AGE_SEC, null);
							}

							if (login == null && httpRequest.getUserPrincipal() != null) {
								login = httpRequest.getUserPrincipal().getName();
							}
							if (fact.login(httpRequest, login, request.getParameter("j_password")) == null) {
								String msg = i18nAccess.getText("user.error.msg");
								MessageRepository messageRepository = MessageRepository.getInstance(((HttpServletRequest) request));
								messageRepository.setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
							}
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
				if (autoLoginUser != null) {
					IUserFactory adminFactory = AdminUserFactory.createUserFactory(globalContext, httpRequest.getSession());
					User principalUser = adminFactory.autoLogin(httpRequest, autoLoginUser);
					if (principalUser != null) {
						globalContext.addPrincipal(principalUser);
						newUser = true;
						if (request.getParameter("edit-login") != null) {
							adminFactory.autoLogin(httpRequest, principalUser.getLogin());							
							globalContext.addPrincipal(principalUser);
							globalContext.eventLogin(principalUser.getLogin());
						}
					}
				}
				UserInterfaceContext.getInstance(((HttpServletRequest) request).getSession(), globalContext);
			}

			if (request.getParameter("edit-login") != null || request.getParameter("j_token") != null || (httpRequest.getUserPrincipal() != null && logoutUser == null)) {
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
						RequestHelper.setCookieValue(httpResponse, "javlo_login_id", codeId, IUserFactory.AUTO_LOGIN_AGE_SEC, null);
					}
					globalContext.addPrincipal(editUser);
					globalContext.eventLogin(editUser.getLogin());
					newUser = true;

					logger.info(login + " is logged roles : [" + StringHelper.collectionToString(editUser.getRoles(), ",") + ']');

				} else {
					String token = request.getParameter("j_token");
					if (token != null) {
						user = adminFactory.login(httpRequest, token);
					} else {
						logger.info(login + " fail to login.");
					}
					if (user == null) {
						logger.info(login + " fail to login.");
						String msg = i18nAccess.getText("user.error.msg");
						MessageRepository messageRepository = MessageRepository.getInstance(((HttpServletRequest) request));
						messageRepository.setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
					} else {
						globalContext.addPrincipal(user);
					}
				}
				UserInterfaceContext.getInstance(((HttpServletRequest) request).getSession(), globalContext);
			}

			if (newUser) {
				ModulesContext.getInstance(httpRequest.getSession(), globalContext).loadModule(httpRequest.getSession(), globalContext);
			}

			return newUser;
		} catch (Exception e) {
			throw new ServletException(e);
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