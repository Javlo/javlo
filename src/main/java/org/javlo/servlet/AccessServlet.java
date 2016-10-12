package org.javlo.servlet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.ContentManager;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.data.EditInfoBean;
import org.javlo.data.InfoBean;
import org.javlo.filter.CatchAllFilter;
import org.javlo.helper.DebugHelper;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.LocalLogger;
import org.javlo.helper.NetHelper;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.ClearDataAccessCount;
import org.javlo.mailing.MailingThread;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.message.PopupMessage;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.portlet.filter.MultiReadRequestWrapper;
import org.javlo.rendering.Device;
import org.javlo.service.ContentService;
import org.javlo.service.ListService;
import org.javlo.service.PDFConvertion;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.event.Event;
import org.javlo.service.integrity.IntegrityFactory;
import org.javlo.service.remote.RemoteMessage;
import org.javlo.service.remote.RemoteMessageService;
import org.javlo.service.shared.SharedContentService;
import org.javlo.service.social.SocialService;
import org.javlo.service.syncro.SynchroThread;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.thread.ThreadManager;
import org.javlo.tracking.Tracker;
import org.javlo.user.UserFactory;
import org.javlo.user.VisitorContext;
import org.javlo.utils.DebugListening;
import org.javlo.utils.DoubleOutputStream;
import org.javlo.utils.TimeTracker;
import org.javlo.ztatic.FileCache;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.FSImageWriter;

public class AccessServlet extends HttpServlet implements IVersion {

	public static final String PERSISTENCE_PARAM = "persistence";

	private static final long serialVersionUID = 1L;

	public static long COUNT_ACCESS = 0;

	public static long COUNT_304 = 0;

	private static final DecimalFormat df = new DecimalFormat("#####0.00");

	private static Boolean FIRST_REQUEST = true;

	MailingThread mailingThread = null;

	ThreadManager threadManager = null;

	/**
	 * create a static logger.
	 */
	public static Logger logger = Logger.getLogger(AccessServlet.class.getName());

	@Override
	public void destroy() {
		Collection<GlobalContext> allGlobalContext = null;
		try {
			allGlobalContext = GlobalContextFactory.getAllGlobalContext(getServletContext());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (allGlobalContext != null) {
			for (GlobalContext globalContext : allGlobalContext) {
				globalContext.destroy();
			}
		}
		if (mailingThread != null) {
			mailingThread.stop = true;
			synchronized (mailingThread.stop) {
				try {
					mailingThread.stop.wait();
					logger.info("mailing thread stopped");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		threadManager.stop = true;
		synchronized (threadManager.stop) {
			try {
				threadManager.stop.wait();
				logger.info("thread manager stopped");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		super.destroy();
		StaticConfig.getInstance(getServletContext()).shutdown();
		try {
			org.javlo.helper.LocalLogger.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response, false);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response, true);
	}

	@Override
	public void init() throws ServletException {
		super.init();

		LocalLogger.init(getServletContext());

		System.out.println("");
		System.out.println("");
		System.out.println("		    _  ____  _     _     ____");
		System.out.println("		   / |/  _ \\/ \\ |\\/ \\   /  _ \\");
		System.out.println("		   | || / \\|| | //| |   | / \\|");
		System.out.println("		/\\_| || |-||| \\// | |_/\\| \\_/|");
		System.out.println("		\\____/\\_/ \\|\\__/  \\____/\\____/");
		System.out.println("");
		System.out.println("");

		/** JSTL Constant **/
		getServletContext().setAttribute("BACK_PARAM_NAME", ElementaryURLHelper.BACK_PARAM_NAME);

		try {
			DebugHelper.updateLoggerLevel(getServletContext());
		} catch (Exception e1) {
			logger.warning("error when update logger level : " + e1.getMessage());
			e1.printStackTrace();
		}

		StaticConfig staticConfig = StaticConfig.getInstance(getServletContext());
		Integer undoDepth = staticConfig.getUndoDepth();
		if (undoDepth != null) {
			PersistenceService.UNDO_DEPTH = undoDepth;
		}
		TimeTracker.reset(staticConfig);

		LocalLogger.SPECIAL_LOG_FILE = new File(staticConfig.getSpecialLogFile());

		writeInfo(System.out);

		DebugListening.staticConfig = staticConfig;

		if (staticConfig.isMailingThread()) {
			logger.info("start mailing thread.");
			mailingThread = new MailingThread(getServletContext());
			mailingThread.start();
		} else {
			logger.info("not start mailing thread for this instance");
		}

		threadManager = ThreadManager.getInstance(getServletContext());
		threadManager.setThreadDir(new File(staticConfig.getThreadFolder()));
		threadManager.start();

		try {
			SynchroThread.createInstance(staticConfig.getThreadFolder(), SynchroThread.class);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		if (staticConfig.getTempDir() != null) {
			System.setProperty("java.io.tmpdir", staticConfig.getTempDir());
		}

		MultiReadRequestWrapper.clearTempDir(getServletContext());
		TemplateFactory.copyDefaultTemplate(getServletContext());

	}
	
	public void process(HttpServletRequest request, HttpServletResponse response, boolean post) throws ServletException {

		try {
			COUNT_ACCESS++;

			logger.fine("uri : " + request.getRequestURI()); // TODO: remove
																// debug trace

			StaticConfig staticConfig = StaticConfig.getInstance(getServletContext());

			/** init log **/
			long startTime = System.currentTimeMillis();

			String requestLabel = request.getPathInfo();

			request.setCharacterEncoding(ContentContext.CHARACTER_ENCODING);

			RequestService requestService = RequestService.getInstance(request);

			// content type cannot be set to html if portlet resource - TODO:
			// clean process method (with filter ?)
			if (requestService.getParameter("javlo-portlet-resource", null) == null || requestService.getParameter("javlo-portlet-id", null) == null) {
				response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);
			}

			GlobalContext globalContext = GlobalContext.getInstance(request);
			Thread.currentThread().setName("AccessServlet-" + globalContext.getContextKey());

			ContentContext ctx = ContentContext.getContentContext(request, response);
			if (ctx.isAsEditMode() || ctx.isAsPreviewMode()) {
				if (!staticConfig.acceptIP(request.getRemoteAddr())) {
					logger.warning("refuse access for ip : "+request.getRemoteAddr());
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					return;
				}
			}
			
			if (ctx.isAsViewMode() && ctx.isContentFound() && ctx.getCurrentPage() != null && staticConfig.isRedirectSecondaryURL() && !ctx.isPostRequest() && StringHelper.isEmpty(request.getQueryString())) {
				ContentContext lgCtx = new ContentContext(ctx);
				lgCtx.setContentLanguage(ctx.getRequestContentLanguage());
				String pageUrl = URLHelper.createURL(lgCtx, lgCtx.getCurrentPage());
				pageUrl = URLDecoder.decode(pageUrl, ContentContext.CHARACTER_ENCODING);
				String mainURL = (String)request.getAttribute(CatchAllFilter.MAIN_URI_KEY);				
				
				if (mainURL != null && !mainURL.endsWith(pageUrl)) {
					// response.sendRedirect(pageUrl);
					NetHelper.sendRedirectPermanently(response, URLHelper.createURL(lgCtx, lgCtx.getCurrentPage()));
					return;
				}
			}
			
			if (!staticConfig.isContentExtensionValid(ctx.getFormat())) {
				ctx.setFormat(staticConfig.getDefaultContentExtension());
				ctx.setContentFound(false);
			}
			if (staticConfig.isXSSHeader()) {
				response.setHeader("X-XSS-Protection", "1");
			}

			if (ctx.getCurrentEditUser() != null) {
				// edit edit info bean
				EditInfoBean.getCurrentInfoBean(ctx);
			}

			ctx.setPostRequest(post);

			globalContext.initExternalService(ctx);

			if (FIRST_REQUEST) {
				synchronized (FIRST_REQUEST) {
					if (FIRST_REQUEST) {
						FIRST_REQUEST = false;
						try {
							GlobalContext.getDefaultContext(request.getSession());
							GlobalContext.getMasterContext(request.getSession());
							if (!globalContext.getSpecialConfig().isTrackingAccess()) {
								ClearDataAccessCount.clearDataAccess(ctx);
							}
							/*
							 * if (globalContext.getDMZServerIntra() != null) {
							 * SynchroThread synchro = (SynchroThread)
							 * AbstractThread.createInstance(staticConfig.
							 * getThreadFolder(), SynchroThread.class);
							 * synchro.initSynchronisationThread(staticConfig,
							 * globalContext,
							 * request.getSession().getServletContext());
							 * synchro.store(); }
							 */
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}

			/** get info from system to browser **/
			if (ctx.getCurrentEditUser() != null && request.getPathInfo() != null && request.getPathInfo().endsWith("info.txt")) {
				response.setContentType("text/plain");
				PrintStream out = new PrintStream(response.getOutputStream());
				writeInfo(out);
				out.println("");
				out.println("User-Agent : " + request.getHeader("User-Agent"));
				out.println("");
				globalContext.writeInfo(request.getSession(), out);
				out.println("");
				globalContext.writeInstanceInfo(ctx, out);
				ContentService content = ContentService.getInstance(globalContext);
				out.println("");
				ctx.getCurrentPage().printInfo(ctx, out);
				out.println("");
				out.println("latest update by : " + content.getAttribute(ctx, "user.update"));
				out.close();
				return;
			}

			/** CACHE **/
			if (ctx.isAsViewMode() && ctx.getCurrentPage() != null && ctx.getCurrentPage().isCacheable(ctx) && globalContext.isPreviewMode() && globalContext.getPublishDate() != null && request.getMethod().equalsIgnoreCase("get") && request.getParameter("webaction") == null) {
				long lastModified = globalContext.getPublishDate().getTime();
				response.setDateHeader(NetHelper.HEADER_LAST_MODIFIED, lastModified);
				response.setHeader("Cache-Control", "max-age=60,must-revalidate");
				long lastModifiedInBrowser = request.getDateHeader(NetHelper.HEADER_IF_MODIFIED_SINCE);
				if (lastModified > 0 && lastModified / 1000 <= lastModifiedInBrowser / 1000) {
					COUNT_304++;
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return;
				}
			} else {
				response.setHeader("Cache-Control", "max-age=0,no-cache");
			}

			ctx.getCurrentTemplate();
			request.setAttribute("frontCache", globalContext.getFrontCache(ctx));

			if (logger.isLoggable(Level.FINE)) {
				logger.fine(requestLabel + " : first ContentContext " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");
				logger.fine("device : " + ctx.getDevice());
			}

			I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
			if (ctx.getCurrentPage() != null) {
				i18nAccess.setRequestMap(ctx.getCurrentPage().getI18n(ctx));
			}

			i18nAccess.requestInit(ctx);
			ctx.getRequest().setAttribute("list", ListService.getInstance(ctx).getAllList(ctx));

			/* ******** */
			/* SECURITY */
			/* ******** */

			if (request.getServletPath().equals("/edit") || request.getServletPath().equals("/preview")) {
				if (!globalContext.isEditable()) {
					InfoBean.updateInfoBean(ctx);
					response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);
					request.setAttribute("edit", "true");
					ServletHelper.includeBlocked(request, response);
					return;
				}
			}

			if (ctx.getRenderMode() == ContentContext.VIEW_MODE || ctx.getRenderMode() == ContentContext.PREVIEW_MODE || ctx.getRenderMode() == ContentContext.TIME_MODE || ctx.getRenderMode() == ContentContext.PAGE_MODE || ctx.getRenderMode() == ContentContext.MAILING_MODE) {
				if (!globalContext.isView()) {
					String sessionKey = "__unlock_content__";
					String pwd = request.getParameter("block-password");
					if (pwd != null && pwd.trim().length() > 0) { // unlock with
																	// special
																	// pwd
						if (pwd.equals(globalContext.getBlockPassword())) {
							request.getSession().setAttribute(sessionKey, "true");
						} else {
							Thread.sleep(5000); // if bad password wait 5 sec
						}
					}
					if (request.getSession().getAttribute(sessionKey) == null) {
						InfoBean.updateInfoBean(ctx);
						response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);
						ServletHelper.includeBlocked(request, response);
						return;
					}
				}
			}

			if (request.getServletPath().equals("/edit")) {
				EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
				ctx.setArea(editCtx.getCurrentArea());
				if (ctx.getCurrentEditUser() == null) {
					InfoBean.updateInfoBean(ctx);
					response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);
					String jspForLogin = editCtx.getLoginRenderer();
					getServletContext().getRequestDispatcher(jspForLogin).include(request, response);
					return;
				}
			}

			RequestHelper.traceMailingFeedBack(ctx);

			if (logger.isLoggable(Level.FINE)) {
				logger.fine(requestLabel + " : i18nAccess.requestInit(ctx) " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");
			}
			
			/* ********************** */
			/* CHECK SYSTEM INTEGRITY */
			/* ********************** */

			if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {
				long minSize = staticConfig.getMinFreeSpaceOnDataFolder();
				if (new File(staticConfig.getAllDataFolder()).getFreeSpace() < minSize) {
					logger.warning("not enough space on data disk.");
					MessageRepository messageRepository = MessageRepository.getInstance(ctx);
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage("WARNING : Space left on device becoming to low.  Please log off and contact the administrator.", GenericMessage.ERROR));
					String msg = "no enough free space on data device : " + new File(staticConfig.getAllDataFolder()).getFreeSpace() + " bytes free.";
					DebugListening.getInstance().sendError(request, msg);
					RemoteMessageService remoteMessageService = RemoteMessageService.getInstance(globalContext);
					remoteMessageService.addMessage(RemoteMessage.ADMIN_LEVEL, RemoteMessage.WARNING, msg);
				}
			}

			/* welcome popup */
			if (requestService.getParameter("popup", "").equals("welcome")) {
				String url = staticConfig.getWelcomePopupURL(ctx.getLanguage());
				if ((url != null) && (url.length() > 0)) {
					PopupMessage.setPopupMessage(request.getSession(), new URL(url));
				}
			}

			org.javlo.helper.LocalLogger localLogger = new org.javlo.helper.LocalLogger();

			localLogger.startCount("sid");

			/** *********************** **/
			/** EXCECUTE CRYPTED ACTION **/
			/** *********************** **/

			String encodedParam = requestService.getParameter("_sid_", null);

			if ((encodedParam != null) && (request.getAttribute("_sid_") == null)) {
				request.setAttribute("_sid_", "true");
				String decodeParam = URLHelper.httpDecryptData(encodedParam);
				String url = request.getServletPath() + request.getPathInfo() + decodeParam;

				response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);

				getServletContext().getRequestDispatcher(url).include(request, response);
				return;
			}

			localLogger.endCount("sid", "sid traitement");

			/** ************************************* **/
			/** INIT MAILING SERVER FOR UNSUBSCRIBE * **/
			/** ************************************* **/

			/*
			 * localLogger.startCount("mailing"); MailingContext mCtx =
			 * MailingContext.getInstance(request.getSession());
			 * mCtx.setUnsubscribeHost(request.getServerName());
			 * mCtx.setUnsubscribePort(request.getServerPort());
			 * localLogger.endCount("mailing", "mailing lauch");
			 */

			localLogger.startCount("execute action");

			if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {

				/*** update module status before action ***/
				ModulesContext moduleContext = ModulesContext.getInstance(request.getSession(), globalContext);
				moduleContext.initContext(request, response);

				i18nAccess.setCurrentModule(globalContext, request.getSession(), moduleContext.getCurrentModule());

				EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
				ctx.setArea(editCtx.getCurrentArea());
			}

			try {
				String action = ServletHelper.execAction(ctx);
				if (logger.isLoggable(Level.FINE)) {
					logger.fine(requestLabel + " : action " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");
				}

				ContentService content = ContentService.getInstance(globalContext);
				if (ctx.getCurrentPage() != null) {
					ctx.getCurrentPage().updateLinkedData(ctx);
				}
				MenuElement elem = content.getNavigation(ctx).getNoErrorFreeCurrentPage(ctx);
				/** INIT TEMPLATE **/
				if (ctx.getCurrentTemplate() == null || action != null) { // action
																			// can
																			// change
																			// the
																			// template
					Template template = null;
					if (elem != null) {
						template = ctx.getCurrentTemplate();
						if (template != null) {
							template = template.getFinalTemplate(ctx);
						}
						ctx.setCurrentTemplate(template);
					}
				}
				Template template = ctx.getCurrentTemplate();

				if (!ctx.isAsViewMode()) {
					IntegrityFactory.getInstance(ctx);
				}
				if (ctx.getRenderMode() != ContentContext.EDIT_MODE) {
					/*** CHECK CONTENT AVAIBILITY ***/
					boolean checkContentAviability = true;
					if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
						EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
						checkContentAviability = !editCtx.isEditPreview();
					}
					ContentContext newCtx = new ContentContext(ctx);
					if (checkContentAviability) {
						if (!content.contentExistForContext(newCtx)) {
							newCtx.setContentLanguage(newCtx.getLanguage());
							if (globalContext.isAutoSwitchToDefaultLanguage()) {
								Iterator<String> defaultLgs = globalContext.getDefaultLanguages().iterator();
								while (!content.contentExistForContext(newCtx) && defaultLgs.hasNext()) {
									String lg = defaultLgs.next();
									newCtx.setRequestContentLanguage(lg);
								}
							}
							if (!content.contentExistForContext(newCtx)) {
								logger.fine("content not found in " + ctx.getPath() + " lg:" + ctx.getRequestContentLanguage());
								// ctx.setSpecialContentRenderer("/jsp/view/content_not_found.jsp");
							} else {
								ctx = newCtx;
								ctx.storeInRequest(request);
							}
						}
					}
				}

				if (logger.isLoggable(Level.FINE)) {
					logger.fine(requestLabel + " : content integrity " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");
				}

				localLogger.endCount("execute action", "execute action = " + action);

				if (StringHelper.isTrue(requestService.getParameter(RequestHelper.CLOSE_WINDOW_PARAMETER, "false"))) {
					String newURL = requestService.getParameter(RequestHelper.CLOSE_WINDOW_URL_PARAMETER, null);

					response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);

					PrintWriter out = new PrintWriter(new OutputStreamWriter(response.getOutputStream()));
					out.println("<script type=\"text/javascript\">");
					out.println("if (window.opener != null) {");
					if (newURL == null) {
						out.println("	window.opener.location.href = window.opener.location.href;");
					} else {
						out.println("	window.opener.location.href = '" + newURL + "';");
					}
					out.println("	if (window.opener.progressWindow) {");
					out.println("		window.opener.progressWindow.close();");
					out.println("	}");
					out.println("}");
					out.println("if (window.parent != null) {");
					if (newURL == null) {
						out.println("	window.parent.location.href = window.parent.location.href;");
					} else {
						out.println("	window.parent.location.href = '" + newURL + "';");
					}
					out.println("}");
					out.println("window.close();");
					out.println("</script>");
					out.close();
					return;
				}

				localLogger.startCount("tracking");

				/** ******* */
				/* TRACKING */
				/** ******* */

				localLogger.startCount("tracking1");
				Tracker.trace(request, response);
				localLogger.endCount("tracking", "tracking user");

				if (logger.isLoggable(Level.FINE)) {
					logger.fine(requestLabel + " : tracking " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");
				}

				InfoBean infoBean = InfoBean.updateInfoBean(ctx);

				if (logger.isLoggable(Level.FINE)) {
					logger.fine(requestLabel + " : InfoBean " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");
				}
				
				/* **** */
				/* EDIT */
				/* **** */

				if (request.getServletPath().equals("/edit")) {

					/* ********************* */
					/* ****** MODULES ****** */
					/* ********************* */
					ServletHelper.prepareModule(ctx);

					EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
					ctx.setArea(editCtx.getCurrentArea());
					if (editCtx.getAjaxRenderer() != null) {
						response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);

						getServletContext().getRequestDispatcher(editCtx.getAjaxRenderer()).include(request, response);
						editCtx.setAjaxRenderer(null);
					} else {
						if (template != null) {
							if (!template.getAreas().contains(ctx.getArea())) {
								editCtx.setCurrentArea(ComponentBean.DEFAULT_AREA);
								ctx.setArea(ComponentBean.DEFAULT_AREA);
							}
							ctx.setCurrentTemplate(template);
						}

						response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);

						File editCSS = new File(URLHelper.mergePath(globalContext.getStaticFolder(), "/edit/specific.css"));
						if (editCSS.exists()) {
							String savePathPrefix = ctx.getPathPrefix();
							ContentContext.setForcePathPrefix(request, globalContext.getContextKey());
							String cssURL = URLHelper.createResourceURL(ctx, URLHelper.mergePath(globalContext.getStaticConfig().getStaticFolder(), "/edit/specific.css"));
							request.setAttribute("specificCSS", cssURL);
							ContentContext.setForcePathPrefix(request, savePathPrefix);
						}
						if (request.getAttribute("specificCSS") == null) {
							GlobalContext masterContext = GlobalContextFactory.getMasterGlobalContext(request.getSession().getServletContext());
							if (masterContext != null) {
								editCSS = new File(URLHelper.mergePath(masterContext.getStaticFolder(), "/edit/specific.css"));
								if (editCSS.exists()) {
									String savePathPrefix = ctx.getPathPrefix();
									ContentContext.setForcePathPrefix(request, masterContext.getContextKey());
									String cssURL = URLHelper.createResourceURL(ctx, URLHelper.mergePath(globalContext.getStaticConfig().getStaticFolder(), "/edit/specific.css"));
									request.setAttribute("specificCSS", cssURL);
									ContentContext.setForcePathPrefix(request, savePathPrefix);
								}
							} else {
								logger.severe("master context not found.");
							}
						}

						getServletContext().getRequestDispatcher(editCtx.getEditTemplate()).include(request, response);
					}
					localLogger.endCount("edit", "include edit");
				} else { // view

					ContentContext robotCtx = new ContentContext(ctx);
					robotCtx.setDevice(Device.getFakeDevice("robot"));
					robotCtx.setAbsoluteURL(true);
					response.setHeader("link", "<"+URLHelper.createURL(robotCtx)+">; rel=\"canonical\"");
					
					request.setAttribute("social", SocialService.getInstance(ctx));

					localLogger.startCount("content");

					if (request.getServletPath().equals("/preview")) {
						EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
						request.setAttribute("editPreview", editCtx.isEditPreview());
					}

					if (!globalContext.isVisible()) {
						ServletHelper.includeBlocked(request, response);
						return;
					}

					String path = ctx.getPath();

					if (ctx.getFormat().equalsIgnoreCase("xml")) {

						response.setContentType("text/xml; charset=" + ContentContext.CHARACTER_ENCODING);
						Writer out = response.getWriter();
						out.write(XMLHelper.getPageXML(ctx, elem));

					} else if (ctx.getFormat().equalsIgnoreCase("png") || ctx.getFormat().equalsIgnoreCase("jpg")) {

						String fileFormat = ctx.getFormat().toLowerCase();
						response.setContentType("image/" + fileFormat + ";");
						OutputStream out = response.getOutputStream();
						ContentContext viewCtx = ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE);
						viewCtx.setAbsoluteURL(true);
						viewCtx.setFormat("html");
						String url;
						Map<String, String> params = new HashMap<String, String>();
						params.put(Device.FORCE_DEVICE_PARAMETER_NAME, "image");
						if (request.getParameter(Template.FORCE_TEMPLATE_PARAM_NAME) != null) {
							params.put(Template.FORCE_TEMPLATE_PARAM_NAME, request.getParameter(Template.FORCE_TEMPLATE_PARAM_NAME));
						}
						params.put("clean-html", "true");
						url = URLHelper.createURL(viewCtx, params);

						int width = 1280;
						String widthParam = request.getParameter("width");
						if (widthParam != null) {
							width = Integer.parseInt(widthParam);
						}

						Java2DRenderer renderer = new Java2DRenderer(url, width);
						BufferedImage img = renderer.getImage();
						FSImageWriter imageWriter = new FSImageWriter();
						imageWriter.write(img, out);

					} else if (ctx.getFormat().equalsIgnoreCase("pdf")) {						
						if (ctx.getGlobalContext().isCollaborativeMode()) {
							Set<String> pageRoles = ctx.getCurrentPage().getEditorRolesAndParent();
							if ((pageRoles.size() > 0 || ctx.getCurrentEditUser() == null)) {
								if (ctx.getCurrentEditUser() == null || !ctx.getCurrentEditUser().validForRoles(pageRoles)) {
									MenuElement parent = ctx.getCurrentPage().getParent();
									while (parent != null) {
										parent = parent.getParent();
									}
									response.setStatus(HttpServletResponse.SC_FORBIDDEN);
									return;
								}
							}
						}
						
						response.setContentType("application/pdf;");
						OutputStream out = response.getOutputStream();
						
						Map<String, String> params = new HashMap<String, String>();
						boolean lowDef = false;
						if (request.getParameter("lowdef") != null) {
							params.put("lowdef", request.getParameter("lowdef"));
							lowDef = true;
						}

						FileCache fileCache = FileCache.getInstance(getServletContext());
						File pdfFileCache = fileCache.getPDFPage(ctx, ctx.getCurrentPage(), lowDef);
						if (pdfFileCache.exists()) {
							synchronized (FileCache.PDF_LOCK) {
								logger.info("pdf file found in cache : " + pdfFileCache);
								if (pdfFileCache.exists()) {
									ResourceHelper.writeFileToStream(pdfFileCache, out);
								}
							}
						} else {
							ContentContext viewCtx = ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE);
							viewCtx.setAbsoluteURL(true);
							viewCtx.setFormat("html");
							viewCtx.resetDMZServerInter();

							for (Object key : ctx.getRequest().getParameterMap().keySet()) {
								if (!key.equals("__check_context")) {
									params.put(key.toString(), ctx.getRequest().getParameter(key.toString()));
								}
							}

							if (ctx.getCurrentUser() != null) {
								String userToken = UserFactory.createUserFactory(ctx.getGlobalContext(), request.getSession()).getTokenCreateIfNotExist(ctx.getCurrentUser());
								String token = globalContext.createOneTimeToken(userToken);
								params.put("j_token", token);
							}

							params.put(Device.FORCE_DEVICE_PARAMETER_NAME, "pdf");
							params.put(ContentContext.FORCE_ABSOLUTE_URL, "true");
							params.put(ContentContext.NO_DMZ_PARAM_NAME, "true");
							params.put(ContentContext.CLEAR_SESSION_PARAM, "true");
							if (!globalContext.isView() && globalContext.getBlockPassword() != null) {
								params.put("block-password", globalContext.getBlockPassword());
							}

							if (request.getParameter(Template.FORCE_TEMPLATE_PARAM_NAME) != null) {
								params.put(Template.FORCE_TEMPLATE_PARAM_NAME, request.getParameter(Template.FORCE_TEMPLATE_PARAM_NAME));
							}

							params.put("clean-html", "true");

							String url = URLHelper.createURL(viewCtx, params);

							// LocalLogger.log(url);

							/*
							 * if (staticConfig.getApplicationLogin() != null) {
							 * url = URLHelper.addCredential(url,
							 * staticConfig.getApplicationLogin(),
							 * staticConfig.getApplicationPassword()); }
							 * PDFConvertion.getInstance().convertXHTMLToPDF(
							 * url, out);
							 */
							FileOutputStream outFile = new FileOutputStream(pdfFileCache);
							try {
								DoubleOutputStream outDbl = new DoubleOutputStream(out, outFile);
								PDFConvertion.getInstance().convertXHTMLToPDF(new URL(url), staticConfig.getApplicationLogin(), staticConfig.getApplicationPassword(), outDbl);
							} finally {
								ResourceHelper.closeResource(outFile);
							}

						}

					} else if (ctx.getFormat().equalsIgnoreCase("ics") || ctx.getFormat().equalsIgnoreCase("ical") || ctx.getFormat().equalsIgnoreCase("icalendar")) {
						OutputStream out = response.getOutputStream();
						Event event = ctx.getCurrentPage().getEvent(ctx);
						if (event == null) {
							logger.warning("event not found on page : " + ctx.getPath() + "  context:" + ctx.getGlobalContext().getContextKey());
							response.setStatus(HttpServletResponse.SC_NOT_FOUND);
							return;
						} else {
							DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
							response.setContentType("text/calendar;");
							PrintWriter outPrint = new PrintWriter(out);
							outPrint.println("BEGIN:VCALENDAR");
							outPrint.println("VERSION:2.0");
							if (event.getProdID() != null) {
								outPrint.println("PRODID:" + event.getProdID());
							}
							outPrint.println("BEGIN:VEVENT");
							if (event.getUser() != null) {
								;
								outPrint.println("UID:" + event.getUser());
							}
							outPrint.println("DTSTART:" + dateFormat.format(event.getStart()));
							outPrint.println("DTEND:" + dateFormat.format(event.getEnd()));
							outPrint.println("CATEGORIES:" + event.getCategory());
							outPrint.println("SUMMARY:" + event.getSummary());
							if (StringHelper.neverNull(event.getLocation()).trim().length() > 0) {
								outPrint.println("LOCATION:" + event.getLocation());
							}
							outPrint.println("DESCRIPTION:" + event.getDescription());
							outPrint.println("END:VEVENT");
							outPrint.println("END:VCALENDAR");
							outPrint.close();
						}
					} else if (ctx.getFormat().equalsIgnoreCase("cxml")) {

						String realPath = ContentManager.getPath(request);
						MenuElement agendaPage = ContentService.getInstance(globalContext).getNavigation(ctx).searchChild(ctx, realPath);

						response.setContentType("text/xml; charset=" + ContentContext.CHARACTER_ENCODING);
						Date startDate = StringHelper.parseSortableDate(requestService.getParameter("start-date", "1900-01-01"));
						Date endDate = StringHelper.parseSortableDate(requestService.getParameter("end-date", "2100-01-01"));
						String agendaXML = TimeHelper.exportAgenda(ctx, agendaPage, startDate, endDate);
						response.getWriter().write(agendaXML);
					} else {
						if (elem == null) {
							logger.warning("bad path : " + path);
						}
						if ((template == null) || (!template.exist()) || (template.getRendererFullName(ctx) == null)) {
							ServletHelper.includeBlocked(request, response);
						} else {
							if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
								content.getNavigation(ctx).getNoErrorFreeCurrentPage(ctx).addAccess(ctx);
							}

							if (ctx.getSpecialContentRenderer() != null) {
								Template specialTemplate = TemplateFactory.getTemplates(getServletContext()).get(template.getSpecialRendererTemplate());
								if (specialTemplate != null) {
									template = specialTemplate;
								}
							}

							/** check page security **/

							if (globalContext.getActivationKey() != null) {
								String activationKey = requestService.getParameter("activation-key", null);
								if (activationKey != null && activationKey.equals(globalContext.getActivationKey())) {
									globalContext.setActivationKey(null);
								} else {
									ctx.setSpecialContentRenderer("/jsp/view/activation.jsp");
								}
							}

							if (ctx.getCurrentPage() != null && ctx.getCurrentPage().getUserRoles().size() > 0) {
								if (ctx.getCurrentUser() == null) {
									ctx.setSpecialContentRenderer("/jsp/view/login.jsp");
								} else {
									if (ctx.getCurrentUser().getPassword() != null && staticConfig.isFirstPasswordMustBeChanged() && ctx.getCurrentUser().getPassword().equals(staticConfig.getFirstPasswordEncryptedIfNeeded())) {
										ctx.setSpecialContentRenderer("/jsp/view/change_password.jsp");
									} else {
										if (!ctx.getCurrentPage().isReadAccess(ctx, ctx.getCurrentUser())) {
											ctx.setSpecialContentRenderer("/jsp/view/login.jsp");
										}
									}
								}
							}

							if (ctx.getGlobalContext().isCollaborativeMode()) {
								Set<String> pageRoles = ctx.getCurrentPage().getEditorRolesAndParent();
								if ((pageRoles.size() > 0 || ctx.getCurrentEditUser() == null) && !ctx.getCurrentPage().getName().equals("registration")) { // leave
																																							// access
																																							// to
																																							// registration
																																							// page.
									if (ctx.getCurrentEditUser() == null || !ctx.getCurrentEditUser().validForRoles(pageRoles)) {
										MenuElement parent = ctx.getCurrentPage().getParent();
										while (parent != null) {
											parent = parent.getParent();
										}

										response.setStatus(HttpServletResponse.SC_FORBIDDEN);
										ctx.setSpecialContentRenderer("/jsp/view/no_access.jsp");
									}
								}
							}

							ctx.setCurrentTemplate(template);

							if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && staticConfig.isFixPreview()) {
								ctx.getRequest().setAttribute("components", ComponentFactory.getComponentForDisplay(ctx));

								/************************/
								/**** Shared Content ****/
								/************************/
								SharedContentService.prepare(ctx);

							}
							
							/** check content **/
							if (!ctx.isContentFound()) {
								if (staticConfig.isRedirectWidthName()) {
									String pageName = StringHelper.getFileNameWithoutExtension(StringHelper.getFileNameFromPath(request.getRequestURI()));
									MenuElement newPage = content.getNavigation(ctx).searchChildFromName(pageName);
									if (newPage != null) {
										String forwardURL = URLHelper.createURL(ctx, newPage);
										NetHelper.sendRedirectPermanently(response, forwardURL);
										logger.info("redirect permanently : " + pageName + " to " + forwardURL);
										return;
									}
								}

								ctx.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND, "page not found : " + ctx.getPath());
								if (ctx.isAsViewMode()) {
									MenuElement page404 = content.getNavigation(ctx).searchChildFromName(staticConfig.get404PageName());
									if (page404 != null) {
										ctx.setCurrentPageCached(page404);
										template = TemplateFactory.getTemplate(ctx, page404);
										ctx.setCurrentTemplate(template);
									} else {
										Template defaultTemplate = TemplateFactory.getDiskTemplates(getServletContext()).get(globalContext.getDefaultTemplate());
										defaultTemplate.importTemplateInWebapp(staticConfig, ctx);
										File file404 = new File(URLHelper.mergePath(defaultTemplate.getWorkTemplateRealPath(globalContext), defaultTemplate.get404File()));
										if (file404.exists()) {
											response.setStatus(HttpServletResponse.SC_NOT_FOUND);
											ResourceHelper.writeFileToStream(file404, response.getOutputStream());
											return;
										}
									}
								}
							}

							String area = requestService.getParameter("only-area", null);
							if (area != null) {
								getServletContext().getRequestDispatcher("/jsp/view/content_view.jsp?area=" + area).include(request, response);								
							} else {
								if (ctx.getCurrentPage() != null) {
									String jspPath = template.getRendererFullName(ctx);
									int timeTrackerNumber = TimeTracker.start(globalContext.getContextKey(), "render");
									getServletContext().getRequestDispatcher(jspPath).include(request, response);
									TimeTracker.end(globalContext.getContextKey(), "render", timeTrackerNumber);
									VisitorContext.getInstance(request.getSession()).setPreviousPage(ctx.getCurrentPage().getPageBean(ctx));
								} else {
									response.setStatus(HttpServletResponse.SC_NOT_FOUND);
								}
							}
						}
					}
					localLogger.endCount("content", "include content");

					if (logger.isLoggable(Level.FINE)) {
						logger.fine(requestLabel + " : render " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");
					}
				}

				if (logger.isLoggable(Level.FINE)) {
					logger.fine(requestLabel + " : all process method " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");
				}

				if (StringHelper.isTrue(request.getSession().getAttribute(InfoBean.NEW_SESSION_PARAM))) {
					request.getSession().removeAttribute(InfoBean.NEW_SESSION_PARAM);
				}

				i18nAccess.resetRequestMap();

			} catch (Throwable t) {

				t.printStackTrace();

				response.setStatus(503);

				Writer out = response.getWriter();
				out.write("<div style=\"margin-top: 50px; margin-left: auto; margin-right: auto; border: 2px #ff0000 solid; width: 500px; padding: 3px;\" id=\"fatal-error\">");
				out.write("<h1 style=\"margin: 0px; padding: 1px; font-size: 120%; text-align: center;\">Techinal error.</h1>");
				out.write("<p style=\"text-align: center;\"><a href=\"mailto:" + staticConfig.getManualErrorEmail() + "?subject=fatal error in javlo : " + globalContext.getContextKey() + "\">Describe your error in a email.</a></p>");
				out.write("<p style=\"padding: 10px 10px 10px 10px; margin-bottom: 10px; color: #000000; border: 1px solid #ff0000; background-color: #ffeaea;\">" + t.getMessage() + "</p>");
				out.write("</div>");

				if (!(t instanceof SocketException)) {
					DebugListening.getInstance().sendError(request, t, "path=" + request.getRequestURI());
				}
			} finally {
				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				String persistenceParam = requestService.getParameter(PERSISTENCE_PARAM, null);
				if (persistenceService.isAskStore() && StringHelper.isTrue(persistenceParam, true)) {
					persistenceService.store(ctx);
				}
				if (ctx.isClearSession()) {
					HttpSession session = ctx.getRequest().getSession();
					session.invalidate();
				}
			}
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

	private void writeInfo(PrintStream out) {
		StaticConfig staticConfig = StaticConfig.getInstance(getServletContext());
		Runtime runtime = Runtime.getRuntime();
		ThreadMXBean threads = ManagementFactory.getThreadMXBean();
		out.println("****************************************************************");
		out.println("****************************************************************");
		out.println("****");
		out.println("**** Current Time      :  " + StringHelper.renderTime(new Date()));
		out.println("**** Server info       :  " + getServletContext().getServerInfo());
		out.println("**** JAVA_HOME         :  " + System.getenv("JAVA_HOME"));
		out.println("**** System encoding   :  " + System.getProperty("file.encoding"));		
		out.println("**** CMS encoding      :  " + ContentContext.CHARACTER_ENCODING);
		out.println("**** VERSION           :  " + VERSION);
		out.println("**** Platform type     :  " + staticConfig.getPlatformType());
		out.println("**** ENV               :  " + staticConfig.getEnv());
		out.println("**** Internet Access   :  " + staticConfig.isInternetAccess());
		out.println("**** STATIC CONFIG DIR :  " + staticConfig.getStaticConfigLocalisation());
		out.println("**** PROXY HOST        :  " + staticConfig.getProxyHost());
		out.println("**** PROXY PORT        :  " + staticConfig.getProxyPort());
		out.println("**** DIR RELATIVE      :  " + staticConfig.isDataFolderRelative());
		out.println("**** AUTO CREATION     :  " + staticConfig.isAutoCreation());
		out.println("**** CONTEXT DIR       :  " + staticConfig.getContextFolder());
		out.println("**** DATA DIR          :  " + staticConfig.getAllDataFolder());
		out.println("**** MAILING DIR       :  " + staticConfig.getMailingFolder());
		out.println("**** MAIL HI DIR       :  " + staticConfig.getMailingHistoryFolder());
		out.println("**** TPLT DIR          :  " + staticConfig.getTemplateFolder());
		out.println("**** MAIL TPLT DIR     :  " + staticConfig.getMailingTemplateFolder());
		out.println("**** THREAD DIR        :  " + staticConfig.getThreadFolder());
		out.println("**** TEMP DIR          :  " + staticConfig.getTempDir());
		out.println("**** IMAGE TEMP DIR    :  " + staticConfig.getImageCacheFolder());
		out.println("**** IMAGE AUTO FOCUS  :  " + staticConfig.isAutoFocus());		
		out.println("**** EHCACHE FILE      :  " + staticConfig.getEHCacheConfigFile());
		out.println("**** MAIL THREAD       :  " + staticConfig.isMailingThread());
		out.println("**** ALL LOG LVL       :  " + staticConfig.getAllLogLevel());
		out.println("**** ACCESS LOG LVL    :  " + staticConfig.getAccessLogLevel());
		out.println("**** NAV LOG LVL       :  " + staticConfig.getNavigationLogLevel());
		out.println("**** SYNCHRO LOG LVL   :  " + staticConfig.getSynchroLogLevel());
		out.println("**** ALL COMP LOG LVL  :  " + staticConfig.getAllComponentLogLevel());
		out.println("**** ABST COMP LOG LVL :  " + staticConfig.getAbstractComponentLogLevel());
		out.println("**** BACKUP EXCL. PAT. :  " + staticConfig.getBackupExcludePatterns());
		out.println("**** BACKUP INCL. PAT. :  " + staticConfig.getBackupIncludePatterns());
		out.println("**** HIGH SECURE      	:  " + staticConfig.isHighSecure());
		out.println("**** REDIRECT URL      :  " + staticConfig.isRedirectSecondaryURL());
		out.println("**** INTEGRITY CHECKER :  " + staticConfig.isIntegrityCheck());
		out.println("**** HARD USERS        :  " + StringHelper.collectionToString(staticConfig.getEditUsers().keySet(), ","));
		out.println("**** USE EHCACHE       :  " + staticConfig.useEhCache());
		out.println("**** TOTAL MEMORY      :  " + runtime.totalMemory() + " (" + runtime.totalMemory() / 1024 + " KB)" + " (" + runtime.totalMemory() / 1024 / 1024 + " MB)");
		out.println("**** FREE MEMORY       :  " + runtime.freeMemory() + " (" + runtime.freeMemory() / 1024 + " KB)" + " (" + runtime.freeMemory() / 1024 / 1024 + " MB)");
		out.println("**** THREAD ****");
		out.println("**** MODIF. THREAD     :  " + staticConfig.isNotificationThread());
		out.println("**** MAILING THREAD    :  " + staticConfig.isMailingThread());
		out.println("**** THREAD COUNT      :  " + threads.getThreadCount());
		out.println("**** THREAD STR COUNT  :  " + threads.getTotalStartedThreadCount());
		out.println("**** THREAD DMN COUNT  :  " + threads.getDaemonThreadCount());
		out.println("****");
		out.println("****************************************************************");
		out.println("****************************************************************");
	}
}
