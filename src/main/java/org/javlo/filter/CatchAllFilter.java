package org.javlo.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.javlo.config.StaticConfig;
import org.javlo.context.*;
import org.javlo.helper.*;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.module.mailing.MailingAction;
import org.javlo.module.user.UserAction;
import org.javlo.navigation.MenuElement;
import org.javlo.portlet.filter.MultiReadRequestWrapper;
import org.javlo.service.ContentService;
import org.javlo.service.CountService;
import org.javlo.service.DataToIDService;
import org.javlo.service.RequestService;
import org.javlo.service.log.Log;
import org.javlo.servlet.FileServlet;
import org.javlo.servlet.security.AccessSecurity;
import org.javlo.template.Template;
import org.javlo.tracking.Tracker;
import org.javlo.user.*;
import org.javlo.utils.DebugListening;
import org.javlo.utils.TimeMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

public class CatchAllFilter implements Filter {

	public static final TimeMap<String, Long> globalIpMap = new TimeMap<String, Long>(60*60*2);
	public static final int MAX_LOGIN_BY_IP = 100;
	
	private static long VALID_IP = 0;
	private static long BLOCK_IP = 0;
	private static long ALL_COUNT = 0;

	private static final Set<String> COMPRESS_EXT = new HashSet<String>(Arrays.asList(new String[] { "js", "jpg", "jpeg", "png", "css", "font", "woff", "gif" }));

	public static final String CHECK_CONTEXT_PARAM = "__check_context";

	public static final String POLICY = "default-src 'self'";
	public static final String MAIN_URI_KEY = "_mainURI";

	private static boolean DEBUG = true;

	/**
	 * create a static logger.
	 */
	public static Logger logger = Logger.getLogger(CatchAllFilter.class.getName());

	@Override
	public void destroy() {
	}

	private static FileWriter logAccessFile = null;
	private static String accessLogFile = "";

	private static void deleteOldAccessLog(StaticConfig staticConfig) {
		if (staticConfig.getAccessLogFolder() == null) {
			return;
		}
		File directory = new File(staticConfig.getAccessLogFolder());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate now = LocalDate.now();

		File[] files = directory.listFiles((dir, name) -> name.matches("access_\\d{8}\\.log"));

		if (files != null) {
			for (File file : files) {
				String dateString = file.getName().substring(7, 15); // Extraction de la date du nom de fichier
				try {
					LocalDate fileDate = LocalDate.parse(dateString, formatter);
					long daysBetween = ChronoUnit.DAYS.between(fileDate, now);
					if (daysBetween > 30) {
						if (file.delete()) {
							logger.info(file.getName() + "has been removed.");
						} else {
							logger.severe("Unable to delete " + file.getName());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static String getFullURL(HttpServletRequest request) {
		StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
		String queryString = request.getQueryString();
		if (queryString == null) {
			return requestURL.toString();
		} else {
			return requestURL.append('?').append(queryString).toString();
		}
	}

	private static String formatRequestInfo(HttpServletRequest request) {
		StringBuilder builder = new StringBuilder();
		builder.append("Time: ").append(StringHelper.renderDateAndTime(LocalDateTime.now())).append("\n");
		builder.append("Method: ").append(request.getMethod()).append("\n");
		builder.append("Protocol: ").append(request.getProtocol()).append("\n");
		builder.append("Request URL: ").append(getFullURL(request)).append("\n");
		builder.append("Content Type: ").append(request.getContentType()).append("\n");
		builder.append("Content Length: ").append(request.getContentLengthLong()).append("\n");

		// Adding headers
		builder.append("Headers:\n");
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			builder.append("  ").append(headerName).append(": ").append(request.getHeader(headerName)).append("\n");
		}

		// Add other information as needed
		builder.append("Client IP Address: ").append(request.getRemoteAddr()).append("\n");
		builder.append("Request Path: ").append(request.getRequestURI()).append("\n");

		return builder.toString();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain next) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		httpRequest.setAttribute("mainUri", httpRequest.getRequestURI());
		
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		ServletContext servletContext = httpRequest.getSession().getServletContext();
		CountService.getInstance(servletContext).touch();

		// Récupération du chemin de la requête
		String path = httpRequest.getRequestURI();

		// Découpage du chemin en segments
		String[] pathSegments = path.split("/");

		// Vérification de chaque segment
		for (String segment : pathSegments) {
			if (segment.startsWith(".")) {
				if (!segment.equals(".well-known")) {
					// Log de l'erreur de sécurité
					logger.severe("Security error: Attempt to access hidden file or directory.");

					// Renvoi d'une réponse 404
					httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
					return; // Arrête le traitement de la requête
				}
			}
		}

		if (ALL_COUNT%1000 == 0 && ALL_COUNT > 0) {
			logger.info("IP Blocking status : VALID_IP:"+VALID_IP+"  BLOCK_IP="+BLOCK_IP+ "  [%BLK:"+StringHelper.renderDoubleAsPercentage((double)BLOCK_IP/(double)ALL_COUNT)+"]");
		}
		ALL_COUNT++;
		
		/** security : block ip attack **/
		if (AccessSecurity.getInstance(httpRequest).isIpBlock(httpRequest)) {
			httpResponse.reset();
			logger.severe("BLOCK IP # = "+BLOCK_IP);
			httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			BLOCK_IP++;
			return;
		} else {
			VALID_IP++;
		}

		if (request.getParameter(ContentContext.FORWARD_PATH_REQUEST_KEY) != null) {
			String forwardURL = request.getParameter(ContentContext.FORWARD_PATH_REQUEST_KEY);
			((HttpServletResponse) response).sendRedirect(forwardURL);
			return;
		}

		String uri = RequestService.getURI(httpRequest);
		if (uri.endsWith(Template.GZ_FILE_EXT)) {
			String realFile = uri.substring(0, uri.length() - ('.' + Template.GZ_FILE_EXT).length());
			((HttpServletResponse) response).setHeader("Content-Encoding", "gzip");
			((HttpServletResponse) response).setHeader("Content-Type", ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(realFile)));
		}

		String host = ServletHelper.getSiteKey(httpRequest);
		StaticConfig staticConfig = StaticConfig.getInstance(servletContext);


		/** access log file **/
		if (staticConfig.getAccessLogFolder() != null) {
			String accessLogFile = URLHelper.mergePath(staticConfig.getAccessLogFolder(), "access_"+StringHelper.renderSortableDate(new Date())+".log");
			if (!accessLogFile.equals(accessLogFile)) {
				if (logAccessFile != null) {
					logAccessFile.close();
					logAccessFile = null;
					deleteOldAccessLog(staticConfig);
				}
			}
			if (logAccessFile == null) {
				logAccessFile = new FileWriter(accessLogFile, true);
			}
			String requestInfo = formatRequestInfo(httpRequest);
			logAccessFile.write(requestInfo+"\n");
		}


		/*****************************/
		/**** INIT GLOBAL CONTEXT ****/
		/*****************************/

		if (DEBUG) {
			LocalLogger.log("URL = " + ((HttpServletRequest) request).getRequestURL());
		}

		// TODO: check this creation
		GlobalContext globalContext = null;
		String forwardURI = null;
		boolean hostDefineSite = staticConfig.isHostDefineSite();
		RequestService requestService = RequestService.getInstance(httpRequest);

		String hostName = ServletHelper.getSiteKey(httpRequest);
		if (!hostDefineSite && !staticConfig.isExcludeContextDomain(hostName)) {
			if (StringHelper.isTrue(requestService.getParameter(CHECK_CONTEXT_PARAM, "true"))) {
				String contextURI = ContentManager.getContextName(httpRequest);
				if (GlobalContext.isExist(httpRequest, contextURI)) {
					globalContext = GlobalContext.getInstance(httpRequest.getSession(), contextURI);
					globalContext.setPathPrefix(contextURI);
					String newURI = httpRequest.getServletPath();
					newURI = newURI.replaceFirst('/' + contextURI, "");
					if (httpRequest.getQueryString() != null) {
						newURI = newURI + '?' + httpRequest.getQueryString();
					}
					newURI = URLHelper.addParam(newURI, CHECK_CONTEXT_PARAM, "false");
					forwardURI = newURI;
				}
			} else {
				globalContext = GlobalContext.getInstance(httpRequest);
			}
		}
		if (globalContext == null) { // if no context found search a host
										// context.
			if (StringHelper.isTrue(requestService.getParameter(CHECK_CONTEXT_PARAM, "false"))) {
				globalContext = GlobalContext.getInstance(httpRequest);
			}
			if (globalContext == null) {
				globalContext = GlobalContext.getInstance(httpRequest.getSession(), host);
				hostDefineSite = true;
			}
		}
		if (staticConfig.isRequestWrapper()) {
			if (httpRequest.getMethod().trim().equalsIgnoreCase("post")) {
				logger.finest("create request wrapper.");
				MultiReadRequestWrapper rw = new MultiReadRequestWrapper(httpRequest);
				httpRequest = rw;
			}
		}

		if (globalContext == null) {
			logger.warning("context not found : " + httpRequest.getRequestURI());
			if (DEBUG) {
				LocalLogger.log("context not found : " + httpRequest.getRequestURI());
			}
			((HttpServletResponse) response).setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		} else {
			globalContext.touch();
		}

		/** globalcontext not null **/

		if (!StringHelper.isEmpty(globalContext.getSecurityCsp())) {
			/** TODO: NACEUR - set CSP in header */
			((HttpServletResponse) response).setHeader("content-security-policy", CatchAllFilter.POLICY);

		}

		if (globalContext.isForcedHttps()) {
			((HttpServletResponse) response).setHeader("Strict-Transport-Security", "max-age=3628800");
		}

		if (StringHelper.isTrue(request.getParameter(ContentContext.FORWARD_AJAX))) {
			try {
				ContentContext ctx = ContentContext.getContentContext(httpRequest, (HttpServletResponse) response);
				String url = URLHelper.createAjaxURL(ctx);
				String forwardURL = URLHelper.removeSite(ctx, url);
				globalContext.log(Log.WARNING, "url", "forward ajax : " + httpRequest.getRequestURI() + " >> " + forwardURL);
				((HttpServletRequest) request).getRequestDispatcher(forwardURL).forward(httpRequest, response);
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/***************/
		/**** LOGIN ****/
		/***************/

		boolean newUser = doLoginFilter(request, response);
		User user = UserFactory.createUserFactory(globalContext, httpRequest.getSession()).getCurrentUser(globalContext, httpRequest.getSession());

		try {
			Tracker.trace(httpRequest, (HttpServletResponse) response);
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		/*****************/
		/**** MODULES ****/
		/*****************/

		String editURI = uri;

		if (editURI.startsWith('/' + globalContext.getSourceContextKey())) {
			editURI = editURI.substring(globalContext.getSourceContextKey().length() + 1);
		}

		if (user != null && AdminUserSecurity.getInstance().haveRole(user, AdminUserSecurity.CONTRIBUTOR_ROLE) && (!ContentContext.isEditPreview(httpRequest))) {
			if (editURI.startsWith("/edit")) {
				httpRequest.getRequestDispatcher(editURI.replaceFirst("/edit", "/preview")).forward(request, response);
				return;
			}
		}

		if (user != null && AdminUserSecurity.getInstance().isViewOnly(user)) {
			if (editURI.startsWith("/edit")) {
				httpRequest.getRequestDispatcher(editURI.replaceFirst("/edit", "/view")).forward(request, response);
				return;
			}
			if (editURI.startsWith("/preview")) {
				httpRequest.getRequestDispatcher(editURI.replaceFirst("/preview", "/view")).forward(request, response);
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
			if (DEBUG) {
				LocalLogger.log("prefix : " + prefix);
				LocalLogger.log("editURI : " + editURI);
			}
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
						editURI = URLHelper.addParam(editURI, ContentContext.PREVIEW_EDIT_PARAM, "true");
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
		String contextKey = globalContext.getSourceContextKey();
		if (shortURI.startsWith('/' + contextKey)) {
			if (shortURI.length() > globalContext.getContextKey().length() + 2) {
				shortURI = shortURI.substring(globalContext.getContextKey().length() + 2);
			} else {
				shortURI = "";
			}
		} else if (shortURI.startsWith("/")) {
			shortURI = shortURI.substring(1);
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
						if (DEBUG) {
							LocalLogger.log("1.sendRedirectPermanently");
							LocalLogger.log("newURL : " + newURL);
						}
						NetHelper.sendRedirectPermanently((HttpServletResponse) response, newURL);
						return;
					} else {
						logger.warning("bad short url code : " + shortURI);
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} else if (shortURI.length() == globalContext.getStaticConfig().getShortURLSize() + 3 && shortURI.startsWith("L")) {
			String lg = shortURI.substring(1, 3).toLowerCase();
			shortURI = 'U' + shortURI.substring(3);
			ContentContext ctx = null;
			try {
				ctx = ContentContext.getContentContext((HttpServletRequest) request, (HttpServletResponse) response);
				ctx.setAllLanguage(lg);
				if (ctx.isAsViewMode()) {
					ContentService content = ContentService.getInstance(globalContext);
					MenuElement page = content.getPageWithShortURL(ctx, shortURI);
					if (page != null) {
						String newURL = URLHelper.createURLWithtoutEncodeURL(ctx, page.getPath());
						if (DEBUG) {
							LocalLogger.log("2.sendRedirectPermanently");
							LocalLogger.log("newURL : " + newURL);
						}
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
			String cmsURI = uri;
			if (ContentContext.getPathPrefix(globalContext, (HttpServletRequest) request) != null && ContentContext.getPathPrefix(globalContext, (HttpServletRequest) request).length() > 0) {
				cmsURI = cmsURI.replaceFirst("/" + ContentContext.getPathPrefix(globalContext, (HttpServletRequest) request), "");
			}
			try {
				if ((cmsURI.length() < 5 || cmsURI.equals("/")) && globalContext.getHomePage().length() > 1) {
					String homeUrl = globalContext.getHomePageLink(ContentContext.getContentContext(httpRequest, httpResponse));
					logger.info("forward to home >>> " + homeUrl);
					NetHelper.sendRedirectPermanently((HttpServletResponse) response, homeUrl);
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Map<String, String> uriAlias = globalContext.getURIAlias();
			Collection<Map.Entry<String, String>> entries = uriAlias.entrySet();
			if (cmsURI.length() > 1) {
				for (Map.Entry<String, String> entry : entries) {
					String pattern1 = entry.getKey();
					String pattern2 = entry.getValue().trim();
					if (!pattern1.contains("*")) {
						if (cmsURI.equals(pattern1)) {
							if (httpRequest.getQueryString() != null && httpRequest.getQueryString().length() > 0) {

								// String query = httpRequest.getQueryString();
								// Map<String,String> params = URLHelper.getParams(query);
								// String newQuery = "";
								// String sep = "";
								// for (Map.Entry<String, String> entryParam : params) {
								// newQuery += sep+entryParam.getKey()+"="+URLEncoder
								// sep = "?";
								// }

								if (pattern2.contains("?")) {
									pattern2 += '&' + httpRequest.getQueryString();
								} else {
									pattern2 += '?' + httpRequest.getQueryString();
								}
							}

							boolean isPage = false;
							if (pattern2.startsWith("page:")) {
								isPage= true;
								pattern2 = pattern2.replace("page:", "");
								try {
									pattern2 = URLHelper.createURLFromPageName(ContentContext.getContentContext(httpRequest, httpResponse), pattern2);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							logger.info("manual redirect (page:"+isPage+") : " + pattern1 + " --> " + pattern2);
							if (DEBUG) {
								LocalLogger.log("1.sendRedirectPermanently");
								LocalLogger.log("pattern1 : " + pattern1);
								LocalLogger.log("pattern2 : " + pattern2);
							}
							NetHelper.sendRedirectPermanently((HttpServletResponse) response, pattern2);
							return;
						}
					} else {
						String newURL = StringHelper.convertString(pattern1, pattern2, cmsURI);
						if (!newURL.equals(cmsURI)) {
							// newURL = URLHelper.mergePath("/",
							// ContentContext.getPathPrefix((HttpServletRequest)
							// request), newURL);
							logger.info("manual redirect : " + pattern1 + " --> " + newURL);
							if (DEBUG) {
								LocalLogger.log("2.sendRedirectPermanently");
								LocalLogger.log("pattern1 : " + pattern1);
								LocalLogger.log("newURL : " + newURL);
							}
							if (httpRequest.getQueryString() != null && httpRequest.getQueryString().length() > 0) {
								if (newURL.contains("?")) {
									newURL += '&' + httpRequest.getQueryString();
								} else {
									newURL += '?' + httpRequest.getQueryString();
								}
							}
							NetHelper.sendRedirectPermanently((HttpServletResponse) response, newURL);
							return;
						}
					}
				}
			}
			File staticFile = new File(URLHelper.mergePath(globalContext.getDataFolder(), "www", cmsURI));
			if (StringHelper.isEmpty(cmsURI) || cmsURI.equals("/")) {
				staticFile = new File(URLHelper.mergePath(globalContext.getDataFolder(), "www", "index.html"));
			}
			if (staticFile.exists() && staticFile.isFile() && response instanceof HttpServletResponse) {
				HttpServletResponse httpServletResponse = (HttpServletResponse) response;
				if (staticFile.getName().contains("_acao")) {
					httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
					httpServletResponse.setHeader("Access-Control-Allow-Methods", "*");
				}
				httpServletResponse.setContentType(ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(staticFile.getName())));
				httpServletResponse.setHeader("Accept-Ranges", "bytes");
				httpServletResponse.setDateHeader("Last-Modified", staticFile.lastModified());
				httpServletResponse.setDateHeader("Expires", System.currentTimeMillis() + FileServlet.DEFAULT_EXPIRE_TIME);
				ResourceHelper.writeFileToStream(staticFile, response.getOutputStream());
				return;
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
				if (sep == '/' || sep == ContentContext.CONTENT_LG_SEP || sep == ContentContext.COUNTRY_LG_SEP || sep == '?' || viewURI.length() == 3) {
					String lg = viewURI.substring(1, 3).toLowerCase();
					String lgCountry = viewURI.substring(1, 6).toLowerCase();
					if (globalContext.getContentLanguages().contains(lg) || globalContext.getContentLanguages().contains(lgCountry)) {
						String newPath = "/view" + viewURI;
						// if (httpRequest.getSession().isNew()) {
						// httpRequest.getSession().setAttribute(InfoBean.NEW_SESSION_PARAM, true);
						// }
						if (DEBUG) {
							LocalLogger.log("1.forward");
							LocalLogger.log("newPath : " + newPath);
						}
						httpRequest.setAttribute(MAIN_URI_KEY, URLDecoder.decode(httpRequest.getRequestURI(), ContentContext.CHARACTER_ENCODING));
						globalContext.log(Log.WARNING, "url", "forward add view : " + httpRequest.getRequestURI() + " >> " + newPath);
						httpRequest.getRequestDispatcher(newPath).forward(httpRequest, response);
						return;
					}
				}
			}
		}

		String ext = StringHelper.getFileExtension(httpRequest.getRequestURI()).toLowerCase();
		if (COMPRESS_EXT.contains(ext)) {
			String cacheTime = staticConfig.getStaticResourceCacheTime();
			if (cacheTime != null && cacheTime.length() > 0) {
				HttpServletResponse resp = (HttpServletResponse) response;
				resp.setHeader("Cache-Control", "max-age=" + cacheTime);
				resp.setHeader("Content-Type", ResourceHelper.getFileExtensionToMineType(ext));
			}
		}

		if (httpRequest.getUserPrincipal() != null) {
			logger.fine("principal user found : " + httpRequest.getUserPrincipal());
			globalContext.addPrincipal(new UserPrincipal(httpRequest.getUserPrincipal()));
		}

		if (forwardURI != null) {
			// if (httpRequest.getSession().isNew()) {
			// httpRequest.getSession().setAttribute(InfoBean.NEW_SESSION_PARAM, true);
			// }
			globalContext.log(Log.WARNING, "url", "forward : " + httpRequest.getRequestURI() + " >> " + forwardURI);
			if (DEBUG) {
				LocalLogger.log("1.forward");
				LocalLogger.log("newPath : " + forwardURI);
			}
			httpRequest.getRequestDispatcher(forwardURI).forward(httpRequest, response);
		} else {
			// JavloServletResponse javloResponse = new
			// JavloServletResponse((HttpServletResponse)response);

			if (DEBUG) {
				LocalLogger.log("request uri : " + httpRequest.getRequestURI());
				LocalLogger.log("forwardURI  : " + forwardURI);
				LocalLogger.log("next.doFilter");
			}
			next.doFilter(httpRequest, response);
			/*
			 * if (javloResponse.isError()) { String viewURI = uri; String newPath =
			 * "/"+globalContext.getDefaultLanguage() + viewURI; if
			 * (httpRequest.getSession().isNew()) {
			 * httpRequest.getSession().setAttribute(InfoBean.NEW_SESSION_PARAM, true); }
			 * NetHelper.sendRedirectTemporarily((HttpServletResponse) response, newPath); }
			 */
		}
	}

	public static boolean doLoginFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {

		logger.fine("start login filter");

		String logIP = NetHelper.getIp((HttpServletRequest) request);

		Long tryLogin = globalIpMap.get(logIP);
		if (tryLogin == null) {
			tryLogin = 0L;
		}

		if (tryLogin > MAX_LOGIN_BY_IP) {
			logger.severe("too many login for ip : "+logIP);
			//throw new ServletException("too many login, wait before try again.");
		}

		try {

			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			GlobalContext globalContext = GlobalContext.getInstance(httpRequest);

			RequestService requestService = RequestService.getInstance(httpRequest);
			IUserFactory fact = UserFactory.createUserFactory(globalContext, httpRequest.getSession());

			Principal logoutUser = null;

			if (request.getParameter("edit-logout") != null) {
				logoutUser = fact.getCurrentUser(globalContext, httpRequest.getSession());
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
			IUserFactory adminFact = AdminUserFactory.createUserFactory(globalContext, httpRequest.getSession());
			User user = fact.getCurrentUser(globalContext, ((HttpServletRequest) request).getSession());

			if (user != null) {
				EditContext editContext = EditContext.getInstance(GlobalContext.getInstance(((HttpServletRequest) request).getSession(), globalContext.getContextKey()), ((HttpServletRequest) request).getSession());
				/*
				 * if (!user.getContext().equals(globalContext.getContextKey())) { if
				 * (!AdminUserSecurity.getInstance().isGod(user) &&
				 * !AdminUserSecurity.getInstance().isMaster(user)) { try {
				 * editContext.setEditUser(null); logger.info("remove user '" + user.getLogin()
				 * + "' context does'nt match."); user = null; } catch (Exception e) {
				 * e.printStackTrace(); } } }
				 */
				if (editContext != null && user != null && editContext.getEditUser() == null && user.isEditor()) {
					editContext.setEditUser(user);
				}
			}

			String token = request.getParameter(IUserFactory.TOKEN_PARAM);
			if (token != null) {
				String realToken = globalContext.convertOneTimeToken(token);
				if (realToken != null) {
					token = realToken;
				} else {
					logger.warning("bad one time token : " + token);
				}
				logger.info("try log with token #="+token.length());
			}

			if (fact.getCurrentUser(globalContext, ((HttpServletRequest) request).getSession()) == null && adminFact.getCurrentUser(globalContext, ((HttpServletRequest) request).getSession()) == null) {
				String loginType = requestService.getParameter("login-type", null);

				if ((loginType == null || !loginType.equals("adminlogin")) && logoutUser == null) {
					if (globalContext.getStaticConfig().isLoginWithToken() && !StringHelper.isEmpty(token)) {
						user = fact.login(httpRequest, token);
						if (user == null) {
							logger.info("fail to log with token on : "+fact.getClass().getName());
							user = adminFact.login(httpRequest, token);
						} else {
							logger.info("logged with tolen : "+user);
						}
					} else if (fact.getCurrentUser(globalContext, ((HttpServletRequest) request).getSession()) == null) {
						if (request.getParameter("j_username") != null || httpRequest.getUserPrincipal() != null) {
							String login = request.getParameter("j_username");
							if (request.getParameter("autologin") != null) {
								DataToIDService service = DataToIDService.getInstance(httpRequest.getSession().getServletContext());
								String codeId = service.setData(login, IUserFactory.AUTO_LOGIN_AGE_SEC);
								RequestHelper.setCookieValue(httpResponse, UserAction.JAVLO_LOGIN_ID, codeId, IUserFactory.AUTO_LOGIN_AGE_SEC, null);
							}
							if (login == null && httpRequest.getUserPrincipal() != null) {
								login = httpRequest.getUserPrincipal().getName();
							} else if (fact.login(httpRequest, login, request.getParameter("j_password")) == null && adminFact.login(httpRequest, login, request.getParameter("j_password")) == null) {
								I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, httpRequest.getSession());
								String msg = i18nAccess.getViewText("login.error");
								MessageRepository messageRepository = MessageRepository.getInstance(((HttpServletRequest) request));
								messageRepository.setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
							} else {
								// ContentService.getInstance(globalContext).releaseViewNav(globalContext);
								// TODO: check why we need to releaseViewNav on login
							}
							ModulesContext.getInstance(httpRequest.getSession(), globalContext).loadModule(httpRequest.getSession(), globalContext);
							// ContentContext ctx = ContentContext.getContentContext(httpRequest,
							// (HttpServletResponse) response);
							// User loggedUser = fact.getCurrentUser(globalContext, ((HttpServletRequest)
							// request).getSession());
							// if (ctx.isAjax() && loggedUser != null) {
							// ctx.getAjaxData().put("login", login);
							// ctx.getAjaxData().put("firstname", loggedUser.getUserInfo().getFirstName());
							// ctx.getAjaxData().put("lastname", loggedUser.getUserInfo().getLastName());
							// ctx.getAjaxData().put("email", loggedUser.getUserInfo().getEmail());
							// }
						}
					}
				}
			}

			boolean newUser = false;

			if (fact.getCurrentUser(globalContext, ((HttpServletRequest) request).getSession()) == null && adminFact.getCurrentUser(globalContext, ((HttpServletRequest) request).getSession()) == null) {
				/* LOGIN FROM HEADER */
				Collection<String> keys = globalContext.getSpecialConfig().getSecureHeaderLoginKey();
				if (keys != null) {
					IUserFactory userFactory = UserFactory.createUserFactory(globalContext, httpRequest.getSession());
					IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, httpRequest.getSession());
					for (String key : keys) {
						String login = ((HttpServletRequest) request).getHeader(key);
						logger.info("auto login user with request header : "+key+" -> "+login);
						if (!StringHelper.isEmpty(login)) {
							User principalUser = adminUserFactory.autoLogin((HttpServletRequest) request, login);
							if (principalUser == null) {
								principalUser = userFactory.autoLogin((HttpServletRequest) request, login);
							}
							if (principalUser != null) {
								globalContext.addPrincipal(principalUser);
								globalContext.eventLogin(principalUser.getLogin());
							}
						}
					}
				}
			}

			/** EDIT LOGIN **/
			if (fact.getCurrentUser(globalContext, ((HttpServletRequest) request).getSession()) == null && adminFact.getCurrentUser(globalContext, ((HttpServletRequest) request).getSession()) == null) {
				/* AUTO LOGIN */
				String autoLoginId = RequestHelper.getCookieValue(httpRequest, UserAction.JAVLO_LOGIN_ID);
				String autoLoginUser = null;
				if (autoLoginId != null) {
					DataToIDService service = DataToIDService.getInstance(httpRequest.getSession().getServletContext());
					service.clearTimeData();
					autoLoginUser = service.getData(autoLoginId);
				}
				if (autoLoginUser != null) {


					globalIpMap.put(logIP, tryLogin+1);

					logger.info("try autologin for : " + autoLoginUser+ " IP:"+logIP+ " #login:"+tryLogin);
					IUserFactory userFactory = UserFactory.createUserFactory(globalContext, httpRequest.getSession());
					User principalUser = userFactory.autoLogin(httpRequest, autoLoginUser);
					I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, httpRequest.getSession());
					if (principalUser == null) {
						IUserFactory adminFactory = AdminUserFactory.createUserFactory(globalContext, httpRequest.getSession());
						principalUser = adminFactory.autoLogin(httpRequest, autoLoginUser);
						if (principalUser != null) {
							String msg = i18nAccess.getText("user.autologin", new String[][] { { "login", principalUser.getLabel() } });
							MessageRepository messageRepository = MessageRepository.getInstance(((HttpServletRequest) request));
							messageRepository.setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
							newUser = true;
							globalContext.addPrincipal(principalUser);
							newUser = true;
							if (request.getParameter("edit-login") != null) {
								adminFactory.autoLogin(httpRequest, principalUser.getLogin());
								globalContext.addPrincipal(principalUser);
								globalContext.eventLogin(principalUser.getLogin());
							}
						}
					}
				}
				UserInterfaceContext.getInstance(((HttpServletRequest) request).getSession(), globalContext);
			}

			if (request.getParameter("edit-login") != null
					|| (request.getParameter("j_token") != null && (httpRequest.getUserPrincipal() != null && fact.getCurrentUser(globalContext, ((HttpServletRequest) request).getSession()) == null))
					|| (httpRequest.getUserPrincipal() != null && fact.getCurrentUser(globalContext, ((HttpServletRequest) request).getSession()) == null && logoutUser == null)) {
				String login = request.getParameter("j_username");
				if (login == null && httpRequest.getUserPrincipal() != null) {
					login = httpRequest.getUserPrincipal().getName();
				}
				User editUser = adminFact.login(httpRequest, login, request.getParameter("j_password"));
				if (editUser != null) {
					if (request.getParameter("autologin") != null) {
						DataToIDService service = DataToIDService.getInstance(httpRequest.getSession().getServletContext());
						String codeId = service.setData(editUser.getLogin(), ((long) IUserFactory.AUTO_LOGIN_AGE_SEC) * 1000);
						RequestHelper.setCookieValue(httpResponse, UserAction.JAVLO_LOGIN_ID, codeId, IUserFactory.AUTO_LOGIN_AGE_SEC, null);
					}
					globalContext.addPrincipal(editUser);
					globalContext.eventLogin(editUser.getLogin());
					newUser = true;

					logger.fine(login + " is logged roles : [" + StringHelper.collectionToString(editUser.getRoles(), ",") + ']');

				} else {
					if (token != null) {
						user = adminFact.login(httpRequest, token);
					} else {
						logger.info(login + " fail to login.");
					}
					if (user == null) {
						I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, httpRequest.getSession());
						logger.info(login + " fail to login.");
						String msg = i18nAccess.getViewText("login.error");
						MessageRepository messageRepository = MessageRepository.getInstance(((HttpServletRequest) request));
						messageRepository.setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
					} else {
						globalContext.addPrincipal(user);
						newUser = true;
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