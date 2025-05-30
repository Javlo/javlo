package org.javlo.servlet;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.javlo.bean.InstallBean;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.config.StaticConfig;
import org.javlo.context.*;
import org.javlo.data.EditInfoBean;
import org.javlo.data.InfoBean;
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.filter.CatchAllFilter;
import org.javlo.helper.*;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.ImageEngine;
import org.javlo.io.SessionFolder;
import org.javlo.macro.ClearDataAccessCount;
import org.javlo.mailing.MailService;
import org.javlo.mailing.MailingThread;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.message.PopupMessage;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.portlet.filter.MultiReadRequestWrapper;
import org.javlo.rendering.Device;
import org.javlo.security.password.IPasswordEncryption;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.event.Event;
import org.javlo.service.integrity.IntegrityFactory;
import org.javlo.service.location.LocationService;
import org.javlo.service.log.Log;
import org.javlo.service.pdf.PDFConvertion;
import org.javlo.service.pdf.PDFLayout;
import org.javlo.service.remote.CdnService;
import org.javlo.service.remote.RemoteMessage;
import org.javlo.service.remote.RemoteMessageService;
import org.javlo.service.resource.Resource;
import org.javlo.service.shared.SharedContentService;
import org.javlo.service.syncro.SynchroThread;
import org.javlo.service.visitors.CookiesService;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.thread.ThreadManager;
import org.javlo.user.*;
import org.javlo.utils.DebugListening;
import org.javlo.utils.DoubleOutputStream;
import org.javlo.utils.TimeTracker;
import org.javlo.utils.backup.BackupThread;
import org.javlo.utils.request.IFirstRequestListner;
import org.javlo.ztatic.FileCache;
import org.owasp.encoder.Encode;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.FSImageWriter;

import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.ZipOutputStream;

public class AccessServlet extends HttpServlet implements IVersion {

	private static final Logger botLogger = Logger.getLogger("BotLogger");

	private static final boolean DEBUG_BOT_AGENT = false;

	static {
		try {
			String catalinaBase = System.getProperty("catalina.base");
			File logsDir = new File(catalinaBase, "logs");
			File botLogFile = new File(logsDir, "bot.log");
			FileHandler handler = new FileHandler(botLogFile.getAbsolutePath(), true);
			handler.setFormatter(new SimpleFormatter());
			botLogger.addHandler(handler);
			botLogger.setUseParentHandlers(false); // Avoids logging to console
			botLogger.setLevel(Level.INFO);
		} catch (IOException e) {
			throw new RuntimeException("Cannot initialize bot logger", e);
		}
	}

	public static final String PERSISTENCE_PARAM = "persistence";

	private static final long serialVersionUID = 1L;

	public static long COUNT_ACCESS = 0;

	public static long COUNT_304 = 0;

	public static String VIEW_MSG_PARAM = "__msg";

	private static boolean DEBUG = false;

	private static final DecimalFormat df = new DecimalFormat("#####0.00");

	private static Boolean FIRST_REQUEST = true;

	MailingThread mailingThread = null;

	ThreadManager threadManager = null;

	private static Set<String> FIRST_REQUEST_SET = new HashSet<>();
	/**
	 * create a static logger.
	 */
	// public static Logger logger =
	// Logger.getLogger(AccessServlet.class.getName());

	protected static Logger logger = Logger.getLogger(ContentContext.class.getName());

	@Override
	public void destroy() {
		BackupThread.RUN = false;
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

		System.out.println("");
		System.out.println("");
		System.out.println("		    _  ____  _     _     ____");
		System.out.println("		   / |/  _ \\/ \\ |\\/ \\   /  _ \\");
		System.out.println("		   | || / \\|| | //| |   | / \\|");
		System.out.println("		/\\_| || |-||| \\// | |_/\\| \\_/|");
		System.out.println("		\\____/\\_/ \\|\\__/  \\____/\\____/");
		System.out.println("");
		System.out.println("");
		System.out.println("Init Javlo "+IVersion.VERSION);
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

		MailingThread.SLEEP_BETWEEN_MAILING_SEC = staticConfig.getMailingTimebetweenTwoMailing();

		MailingThread.SLEEP_BETWEEN_MAIL_SEC = staticConfig.getMailingTimebetweenTwoSend();

		ImageEngine.WEBP_CONVERTER = staticConfig.getWebpEncoder();

		// LoggerHelper.changeLogLevel(staticConfig.getAllLogLevel().getName());

		MaxLoginService.getInstance().setMaxErrorLoginByHours(staticConfig.getMaxErrorLoginByHour());
		Integer undoDepth = staticConfig.getUndoDepth();
		if (undoDepth != null) {
			PersistenceService.UNDO_DEPTH = undoDepth;
		}
		TimeTracker.reset(staticConfig);

		try {
			SecurityHelper.passwordEncrypt = (IPasswordEncryption) Class.forName(staticConfig.getPasswordEncrytClass()).newInstance();
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1.getMessage());
		}

		LocalLogger.SPECIAL_LOG_FILE = new File(staticConfig.getSpecialLogFile());

		// try {
		// LocationService.init(getServletContext());
		// } catch (Exception e1) {
		// e1.printStackTrace();
		// }

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

		// set SSL protocol (for smtp)
		System.setProperty("https.protocols", "TLSv1.2");

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

		try {
			ResourceHelper.deleteFolder(staticConfig.getWebTempDir());
		} catch (Throwable t) {
			t.printStackTrace();
		}

		try {
			staticConfig.getGeneralLister().onInit(getServletContext());
		} catch (Throwable t) {
			t.printStackTrace();
		}



	}

	public void process(HttpServletRequest request, HttpServletResponse response, boolean post) throws ServletException {

		request.getSession(); // create session

		if (DEBUG_BOT_AGENT) {
			String userAgent = request.getHeader("User-Agent");
			String acceptLanguage = request.getHeader("Accept-Language");
			Locale lg = request.getLocale();
			String ip = request.getRemoteAddr();
			String url = request.getRequestURL().toString();

			if (NetHelper.isBot(userAgent)) {
				// Log bot access
				botLogger.info(String.format(
						"Bot access detected: IP=%s | URL=%s | User-Agent=%s | Accept-Language=%s | Locale=%s",
						ip, url, userAgent, acceptLanguage, lg
				));
			}
		}

		COUNT_ACCESS++;

		// logger.debug("uri : " + request.getRequestURI());

		if (DEBUG) {
			logger.info("AccessServlet : uri : " + request.getRequestURI());
			LocalLogger.log("AccessServlet : uri : " + request.getRequestURI());
		}

		StaticConfig staticConfig = StaticConfig.getInstance(getServletContext());

		/** init log **/
		long startTime = System.currentTimeMillis();
		String requestLabel = request.getPathInfo();

		try {
			request.setCharacterEncoding(ContentContext.CHARACTER_ENCODING);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		RequestService requestService = RequestService.getInstance(request);

		// content type cannot be set to html if portlet resource - TODO:
		// clean process method (with filter ?)
		if (requestService.getParameter("javlo-portlet-resource", null) == null || requestService.getParameter("javlo-portlet-id", null) == null) {
			response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);
		}

		GlobalContext globalContext = GlobalContext.getInstance(request);

		Thread.currentThread().setName("AccessServlet-" + globalContext.getContextKey());
		ContentContext ctx = null;
		try {
			ctx = ContentContext.getContentContext(request, response);

			response.setHeader("Content-Language", ctx.getRequestContentLanguage());

			if (!FIRST_REQUEST_SET.contains(globalContext.getContextKey())) {
				synchronized (FIRST_REQUEST_SET) {
					if (!FIRST_REQUEST_SET.contains(globalContext.getContextKey())) {
						FIRST_REQUEST_SET.add(globalContext.getContextKey());
						IFirstRequestListner firstRequestListner = staticConfig.getFirstRequestLister();
						if (firstRequestListner != null) {
							firstRequestListner.execute(ctx);
						}
					}
				}
			}

			if (request.getParameter(VIEW_MSG_PARAM) != null) {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				String msg = request.getParameter(VIEW_MSG_PARAM);
				msg = Encode.forHtml(msg);
				if (msg.length() > 500) {
					msg = msg.substring(0,500);
				}
				messageRepository.setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
			}

			if (!staticConfig.isFoundFile()) {
				try {
					boolean install = StringHelper.isTrue(request.getParameter("install"));
					if (install) {
						logger.info("install javlo");
						try {
							InstallBean installBean = staticConfig.install(ctx, request.getParameter("config"), request.getParameter("data"), request.getParameter("admin"), request.getParameter("import-template") != null, request.getParameter("import-demo") != null, request.getParameter("email"));
							request.setAttribute("install", installBean);
							if (installBean.getConfigStatus() == InstallBean.ERROR) {
								request.setAttribute("error", "error on install, check log and try again.");
								install = false;
								logger.severe("error on install.");
							}
						} catch (Exception e) {
							e.printStackTrace();
							request.setAttribute("error", e.getMessage());
							install = false;
							logger.severe("exception on install.");
						}
					}
					if (!install) {
						try {
							request.setAttribute("remoteinfo", NetHelper.readPageGet(new URL("http://help.javlo.org/_install?version=" + IVersion.VERSION)));
						} catch (Exception e) {
							e.printStackTrace();
						}
						getServletContext().getRequestDispatcher("/jsp/install.jsp").include(request, response);
					} else {
						getServletContext().getRequestDispatcher("/jsp/install.jsp?done=true").include(request, response);
					}
				} catch (IOException e) {
					throw new ServletException(e);
				}
				return;
			}

			ctx.getDevice().correctWithTemplate(ctx.getCurrentTemplate());
			if (ctx.getDevice().isMobileDevice()) {
				EditContext.getInstance(globalContext, request.getSession()).setPreviewEditionMode(false);
			}
			if (ctx.getDevice().isPdf()) {
				String pdfLayoutRaw = request.getParameter(PDFLayout.REQUEST_KEY);
				if (pdfLayoutRaw != null) {
					PDFLayout pdfLayout = PDFLayout.getInstance(request);
					pdfLayout.setValues(pdfLayoutRaw);
				} else {
					PDFLayout pdfLayout = PDFLayout.getInstance(request);
					pdfLayout.setValues(ctx.getCurrentPage().getPDFLayout(ctx));
				}
			}
			ctx.setPageRequest(true);
			if (ctx.isAsEditMode() || ctx.isAsPreviewMode()) {
				if (staticConfig.isEditIpSecurity()) {
					if (!NetHelper.isIPAccepted(ctx)) {
						logger.warning("refuse access for ip : " + ctx.getRemoteIp());
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
				}
			}

			if (!ctx.isAsViewMode()) {
				SecurityHelper.checkUserAccess(ctx);
				AdminUserSecurity userSec = AdminUserSecurity.getInstance();
				if (ctx.getCurrentEditUser() == null || !userSec.canRole(ctx.getCurrentEditUser(), "content") && !ctx.getCurrentPage().isPublic(ctx)) {
					logger.warning("unauthorized access : " + request.getRequestURL());
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				}
			}

			if (ctx.getGlobalContext().isCookies()) {
				CookiesService.getInstance(ctx); // init cookies service
			}

			TaxonomyService.getInstance(ctx);

			if (requestService.getParameter(ContentContext.FORCE_ABSOLUTE_URL) == null) {
				if (ctx.isAsViewMode() && ctx.isContentFound() && ctx.getCurrentPage() != null && staticConfig.isRedirectSecondaryURL() && !ctx.isPostRequest() && StringHelper.isEmpty(request.getQueryString())) {
					ContentContext lgCtx = new ContentContext(ctx);
					lgCtx.setContentLanguage(ctx.getRequestContentLanguage());
					String pageUrl = URLHelper.createURL(lgCtx, lgCtx.getCurrentPage());
					pageUrl = URLDecoder.decode(pageUrl, ContentContext.CHARACTER_ENCODING);
					String mainURL = (String) request.getAttribute(CatchAllFilter.MAIN_URI_KEY);
					if (mainURL != null && !mainURL.endsWith(pageUrl)) {
						// response.sendRedirect(pageUrl);
						if (ctx.isPageRequest()) {
							globalContext.log(Log.WARNING, "url", "redirect : " + mainURL + " >> " + URLHelper.createURL(lgCtx, lgCtx.getCurrentPage()) + " - [" + pageUrl + "]");
						}
						logger.info("redirect : " + mainURL + " >> " + URLHelper.createURL(lgCtx, lgCtx.getCurrentPage()));
						NetHelper.sendRedirectPermanently(response, URLHelper.createURL(lgCtx, lgCtx.getCurrentPage()));
						return;
					}
				}
			}

			if (!staticConfig.isContentExtensionValid(ctx.getFormat())) {
				logger.warning("extension not found : " + ctx.getFormat() + " url=" + request.getRequestURL());
				ctx.setFormat(staticConfig.getDefaultContentExtension());
				ctx.setContentFound(false);
			}
			if (staticConfig.isXSSHeader()) {
				response.setHeader("X-XSS-Protection", "1; mode=block");
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
							 * if (globalContext.getDMZServerIntra() != null) { SynchroThread synchro =
							 * (SynchroThread) AbstractThread.createInstance(staticConfig.
							 * getThreadFolder(), SynchroThread.class);
							 * synchro.initSynchronisationThread(staticConfig, globalContext,
							 * request.getSession().getServletContext()); synchro.store(); }
							 */

							CdnService.getInstance(ctx.getGlobalContext()).internalTestCdn();
							SessionFolder.clearAllSessionFolder(globalContext);

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
			ContentService content = ContentService.getInstance(ctx.getRequest());
			MenuElement currentPage = content.getNavigation(ctx).getNoErrorFreeCurrentPage(ctx);

			/** redirect **/
			if (ctx.getGlobalContext().getStaticConfig().isAllUrlRedirect()) {
				if (ctx.isAsViewMode() && currentPage != null) {
					String url;
					/*if (currentPage.isLikeRoot(ctx)) {
						url = URLHelper.createURL(ctx, "/");
					} else {*/
						url = URLHelper.createURL(ctx, currentPage);
					//}
					String mainUri = ctx.getMainUri();
					if (mainUri != null && !mainUri.equals(url)) {
						logger.info("redirect to main url : "+request.getRequestURI()+" -> "+url);
						//ctx.getResponse().sendRedirect(url);
						NetHelper.sendRedirectPermanently(response, url);
						return;
					}
				}
			}

			if (ctx.isAsViewMode() && currentPage != null && currentPage.isCacheable(ctx) && globalContext.isPreviewMode() && globalContext.getPublishDate() != null && request.getMethod().equalsIgnoreCase("get") && request.getParameter("webaction") == null) {
				long lastModified = globalContext.getPublishDate().getTime();
				response.setDateHeader(NetHelper.HEADER_LAST_MODIFIED, lastModified);
				response.setHeader("Cache-Control", "max-age=60,must-revalidate");
				if (NetHelper.insertEtag(ctx, currentPage)) {
					return;
				}
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
			RequestHelper.initRequestAttributes(ctx);

			CdnService cdnService = CdnService.getInstance(ctx.getGlobalContext());
			cdnService.testCdn();
			if (cdnService.isReleaseCache()) {
				ContentService.clearCache(ctx, globalContext);
				cdnService.setReleaseCache(false);
			}

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

			if (request.getServletPath().equals("/edit") || (globalContext.getSpecialConfig().isNeedLogForPreview() && (request.getServletPath().equals("/preview") || request.getServletPath().equals("/time")))) {
				EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
				ctx.setArea(editCtx.getCurrentArea());
				if (ctx.getCurrentEditUser() == null) {
					ServletHelper.execAction(ctx, null, true);
					InfoBean.updateInfoBean(ctx);
					response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);
					if (ctx.getGlobalContext().getMainContext() == null) {
						String jspForLogin = editCtx.getLoginRenderer();
						getServletContext().getRequestDispatcher(jspForLogin).include(request, response);
					} else {
						request.getSession().getServletContext().getRequestDispatcher("/jsp/view/error/blocked.jsp?message=use main context for edit : '" + ctx.getGlobalContext().getMainContext().getContextKey() + "'").include(request, response);
					}
					return;
				}
			}

			// RequestHelper.traceMailingFeedBack(ctx);
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
					DebugListening.getInstance().sendError(ctx, msg);
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

			I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
			if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {

				/*** update module status before action ***/
				ModulesContext moduleContext = ModulesContext.getInstance(request.getSession(), globalContext);
				moduleContext.initContext(request, response);

				i18nAccess.setCurrentModule(globalContext, request.getSession(), moduleContext.getCurrentModule());

				EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
				ctx.setArea(editCtx.getCurrentArea());
			}

			String action = ServletHelper.execAction(ctx, null, false);
			if (ctx.isStopRendering()) {
				return;
			}

			if (ctx.getCurrentPage() != null) {
				ctx.getCurrentPage().updateLinkedData(ctx);
			}

			localLogger.stepCount("execute action", "1");

			MenuElement elem = content.getNavigation(ctx).getNoErrorFreeCurrentPage(ctx);
			/** INIT TEMPLATE **/
			if (ctx.getCurrentTemplate() == null || action != null) {
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

			localLogger.stepCount("execute action", "2");

			if (!ctx.isAsViewMode()) {
				IntegrityFactory.getInstance(ctx);
			}
			if (ctx.getRenderMode() != ContentContext.EDIT_MODE) {
				/*** CHECK CONTENT AVAIBILITY ***/
				boolean checkContentAviability = true;
				if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
					EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
					checkContentAviability = !editCtx.isPreviewEditionMode();
				}
				ContentContext newCtx = new ContentContext(ctx);
				if (checkContentAviability) {
					if (!content.contentExistForContext(newCtx)) {
						String currentLg = ctx.getRequestContentLanguage();
						newCtx.setContentLanguage(newCtx.getLanguage());
						if (globalContext.isAutoSwitchToDefaultLanguage()) {
							Iterator<String> defaultLgs = globalContext.getDefaultLanguages().iterator();
							while (!content.contentExistForContext(newCtx) && defaultLgs.hasNext()) {
								String lg = defaultLgs.next();
								newCtx.setAllLanguage(lg);
							}
						}
						if (content.contentExistForContext(newCtx)) {
							I18nAccess.getInstance(ctx.getRequest());
							String msg = i18nAccess.getViewText("global.language-not-found", "Sorry this page does not exist in " + new Locale(currentLg).getDisplayLanguage(Locale.ENGLISH));
							/*InfoBean.getCurrentInfoBean(ctx).setPageNotFoundMessage(msg);
							ctx = newCtx;
							ctx.setMainLanguage(currentLg);
							ctx.storeInRequest(request);*/
							String newURL = URLHelper.createURL(newCtx, newCtx.getCurrentPage());
							if (ctx.getGlobalContext().getSpecialConfig().isMsgOnForward()) {
								newURL = URLHelper.addParam(newURL, VIEW_MSG_PARAM, msg);
							}
							if (!newCtx.getRequestContentLanguage().equals(ctx.getRequestContentLanguage())) {
								NetHelper.sendRedirectPermanently(response, newURL);
							}
							return;
						}
					}
				}
				localLogger.stepCount("execute action", "3");
			}

			localLogger.endCount("execute action", "execute action = " + action);

			if (StringHelper.isTrue(requestService.getParameter(RequestHelper.CLOSE_WINDOW_PARAMETER), false)) {
				String newURL = requestService.getParameter(RequestHelper.CLOSE_WINDOW_URL_PARAMETER, null);

				response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);

				PrintWriter out = new PrintWriter(new OutputStreamWriter(response.getOutputStream()));
				out.println("<script type=\"text/javascript\">");
				if (!ctx.isLikeViewRenderMode()) {
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
				}
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
			// Tracker.trace(request, response);
			localLogger.endCount("tracking", "tracking user");


			InfoBean infoBean = InfoBean.updateInfoBean(ctx);

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
						ctx.setForcePathPrefix(request, globalContext.getContextKey());
						String cssURL = URLHelper.createResourceURL(ctx, URLHelper.mergePath(globalContext.getStaticConfig().getStaticFolder(), "/edit/specific.css"));
						request.setAttribute("specificCSS", cssURL);
						ctx.setForcePathPrefix(request, savePathPrefix);
					}
					if (request.getAttribute("specificCSS") == null) {
						GlobalContext masterContext = GlobalContextFactory.getMasterGlobalContext(request.getSession().getServletContext());
						if (masterContext != null) {
							editCSS = new File(URLHelper.mergePath(masterContext.getStaticFolder(), "/edit/specific.css"));
							if (editCSS.exists()) {
								String savePathPrefix = ctx.getPathPrefix();
								ctx.setForcePathPrefix(request, masterContext.getContextKey());
								String cssURL = URLHelper.createResourceURL(ctx, URLHelper.mergePath(globalContext.getStaticConfig().getStaticFolder(), "/edit/specific.css"));
								request.setAttribute("specificCSS", cssURL);
								ctx.setForcePathPrefix(request, savePathPrefix);
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
				response.addHeader("link", "<" + URLHelper.createURL(robotCtx) + ">; rel=\"canonical\"");

				if (ctx.getRequestCountOnSession() <= 1) {
					// cdn
					if (CdnService.getInstance(ctx.getGlobalContext()).getMainCdnAuto() != null) {
						response.addHeader("link", "<link href='" + StringHelper.extractHostAndProtocol(cdnService.getMainCdn()) + "' rel='preconnect' crossorigin>");
					}
					;
					if (ctx.getCurrentTemplate() != null) {
						for (String host : ctx.getCurrentTemplate().getHostDetected(ctx)) {
							response.addHeader("link", "<link href='" + host + "' rel='preconnect' crossorigin>");
						}
					}
				}

				localLogger.startCount("content");

				if (request.getServletPath().equals("/preview") && !ctx.isPreviewOnly()) {
					EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
					request.setAttribute("editPreview", editCtx.isPreviewEditionMode());
				}

				if (!globalContext.isVisible()) {
					ServletHelper.includeBlocked(request, response);
					return;
				}

				String path = ctx.getPath();
				if (ctx.getFormat().equalsIgnoreCase("zip")) {
					response.setContentType("application/zip; charset=" + ContentContext.CHARACTER_ENCODING);
					ZipOutputStream outZip = new ZipOutputStream(response.getOutputStream());
					outZip.setLevel(9);
					ZipManagement.addFileInZip(outZip, "content.xml", new ByteArrayInputStream(XMLHelper.getPageXML(ctx, elem, requestService.getParameter("lang")).getBytes(ContentContext.CHARSET_DEFAULT)));
					Collection<Resource> resources = ctx.getCurrentPage().getAllResources(ctx);
					for (MenuElement page : ctx.getCurrentPage().getAllChildrenList()) {
						resources.addAll(page.getAllResources(ctx));
					}
					logger.info("prepare zip file with " + resources.size() + " resources.");
					File mainFolder = new File(ctx.getGlobalContext().getDataFolder());
					if (mainFolder.exists()) {
						for (Resource resource : resources) {
							File file = new File(URLHelper.mergePath(mainFolder.getAbsolutePath(), resource.getUri()));
							if (file.exists()) {
								try {
									ZipManagement.zipFile(outZip, file, mainFolder);
								} catch (IOException e) {
									logger.warning("error zip file : " + file + " (" + e.getMessage() + ')');
								}
							} else {
								logger.warning("file not found for create zip : " + file);
							}
						}
					}
					outZip.finish();
					outZip.flush();
					outZip.close();
				} else if (ctx.getFormat().equalsIgnoreCase("xml")) {
					response.setContentType("text/xml; charset=" + ContentContext.CHARACTER_ENCODING);
					Writer out = response.getWriter();
					out.write(XMLHelper.getPageXML(ctx, elem, requestService.getParameter("lang")));
				} else if (ctx.getFormat().equalsIgnoreCase("png") || ctx.getFormat().equalsIgnoreCase("jpg")) {
					if (ctx.getGlobalContext().getStaticConfig().isConvertHTMLToImage()) {
						logger.warning("convert image convertion : " + request.getRequestURI());
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
					} else {
						logger.warning("rejected content image convertion : " + request.getRequestURI());
						response.setStatus(HttpServletResponse.SC_NOT_FOUND);
						return;
					}
				} else if (ctx.getFormat().equalsIgnoreCase("eml")) {
					response.setContentType("message/rfc822");
					OutputStream out = response.getOutputStream();
					Map<String, String> params = new HashMap<String, String>();
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
					params.put(ContentContext.FORCE_ABSOLUTE_URL, "true");
					params.put(ContentContext.NO_DMZ_PARAM_NAME, "true");
					params.put(ContentContext.CLEAR_SESSION_PARAM, "true");
					if (!globalContext.isView() && globalContext.getBlockPassword() != null) {
						params.put("block-password", globalContext.getBlockPassword());
					}
					if (request.getParameter(Template.FORCE_TEMPLATE_PARAM_NAME) != null) {
						params.put(Template.FORCE_TEMPLATE_PARAM_NAME, request.getParameter(Template.FORCE_TEMPLATE_PARAM_NAME));
					}
					// params.put("clean-html", "true");
					String url = URLHelper.createURL(viewCtx, params);
					logger.info("create EML from : " + url);
					String html = NetHelper.readPageForMailing(new URL(url));
					MailService.writeEMLFile(ctx.getCurrentPage().getTitle(viewCtx), html, out);
				} else if (ctx.getFormat().equalsIgnoreCase("pdf")) {
					if (ctx.getGlobalContext().isCollaborativeMode() && !ctx.getCurrentPage().isPublic(ctx)) {
						Set<String> pageRoles = ctx.getCurrentPage().getEditorRolesAndParent();
						if ((pageRoles.size() > 0 || ctx.getCurrentEditUser() == null)) {
							if (ctx.getCurrentEditUser() == null || !ctx.getCurrentEditUser().validForRoles(pageRoles)) {
								MenuElement parent = ctx.getCurrentPage().getParent();
								while (parent != null) {
									parent = parent.getParent();
								}
								logger.warning("no acces to : " + ctx.getCurrentPage().getName() + " (public:" + ctx.getCurrentPage().isPublic(ctx) + ")");
								response.setStatus(HttpServletResponse.SC_FORBIDDEN);
								return;
							}
						}
					}
					String pdfLayoutRaw = request.getParameter(PDFLayout.REQUEST_KEY);
					if (pdfLayoutRaw != null) {
						PDFLayout pdfLayout = PDFLayout.getInstance(request);
						pdfLayout.setValues(pdfLayoutRaw);
					} else {
						PDFLayout pdfLayout = PDFLayout.getInstance(request);
						pdfLayout.setValues(ctx.getCurrentPage().getPDFLayout(ctx));
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

					boolean isParam;
					if (lowDef) {
						isParam = request.getParameterMap().size() > 2; // skip _checkcontext
					} else {
						isParam = request.getParameterMap().size() > 1; // skip _checkcontext
					}

					if (!isParam && staticConfig.isPDFCache() && pdfFileCache.exists() && pdfFileCache.length() > 0) {
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
							// logger.debug("create user token : "+ctx.getCurrentUser().getLogin());
							String userToken;
							if (ctx.getCurrentUser().isEditor()) {
								userToken = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), request.getSession()).getTokenCreateIfNotExist(ctx.getCurrentUser());
							} else {
								userToken = UserFactory.createUserFactory(ctx.getGlobalContext(), request.getSession()).getTokenCreateIfNotExist(ctx.getCurrentUser());
							}
							// logger.debug("userToken : "+userToken);
							String token = globalContext.createOneTimeToken(userToken);
							// logger.debug("token : "+token);
							params.put(IUserFactory.TOKEN_PARAM, token);
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
						url = url.replace("pdfwebaction", "webaction");

						/*
						 * if (staticConfig.getApplicationLogin() != null) { url =
						 * URLHelper.addCredential(url, staticConfig.getApplicationLogin(),
						 * staticConfig.getApplicationPassword()); }
						 * PDFConvertion.getInstance().convertXHTMLToPDF( url, out);
						 */
						FileOutputStream outFile = new FileOutputStream(pdfFileCache);
						try {
							DoubleOutputStream outDbl = new DoubleOutputStream(out, outFile);
							PDFConvertion.getInstance().convertXHTMLToPDF(new URL(url), staticConfig.getApplicationLogin(), staticConfig.getApplicationPassword(), outDbl);
							ctx.setContentFound(true);
						} finally {
							ResourceHelper.closeResource(outFile);
						}
						if (pdfFileCache.length() == 0) {
							pdfFileCache.delete();
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
							MenuElement page = content.getNavigation(ctx).getNoErrorFreeCurrentPage(ctx);
							page.addAccess(ctx);
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
							if (!ctx.getCurrentPage().isReadAccess(ctx, ctx.getCurrentUser())) {
								if (ctx.getCurrentUser() == null) {
									if (ctx.getCurrentTemplate().getLoginFile(ctx) != null) {
										String loginPage = ctx.getCurrentTemplate().getLoginFile(ctx);
										// System.out.println(">>>>>>>>> AccessServlet.process : forward : "+loginPage);
										// //TODO: remove debug trace
										// RequestDispatcher view = request.getRequestDispatcher(loginPage);

										getServletContext().getRequestDispatcher(loginPage).include(request, response);
										return;

										// view.forward(request, response);
									} else {
										ctx.setSpecialContentRenderer("/jsp/view/login.jsp");
									}
								} else {
									if (ctx.getCurrentUser().getPassword() != null && staticConfig.isFirstPasswordMustBeChanged() && ctx.getCurrentUser().getPassword().equals(staticConfig.getFirstPasswordEncryptedIfNeeded())) {
										ctx.setSpecialContentRenderer("/jsp/view/change_password.jsp");
									} else {
										if (!ctx.getCurrentPage().isReadAccess(ctx, ctx.getCurrentUser())) {
											if (ctx.getCurrentTemplate().getLoginFile(ctx) != null) {
												String loginPage = ctx.getCurrentTemplate().getLoginFile(ctx);
												RequestDispatcher view = request.getRequestDispatcher(loginPage);
												view.forward(request, response);
											} else {
												ctx.setSpecialContentRenderer("/jsp/view/login.jsp");
											}
										}
									}
								}
							}
						}

						if (ctx.getGlobalContext().isCollaborativeMode()) {
							Set<String> pageRoles = ctx.getCurrentPage().getEditorRolesAndParent();
							if ((pageRoles.size() > 0 || ctx.getCurrentEditUser() == null) && !ctx.getCurrentPage().isPublic(ctx)) {
								if (ctx.getCurrentEditUser() == null || !ctx.getCurrentEditUser().validForRoles(pageRoles)) {
									MenuElement parent = ctx.getCurrentPage().getParent();
									while (parent != null) {
										parent = parent.getParent();
									}
									logger.info("no access to : " + request.getRequestURL());
									response.setStatus(HttpServletResponse.SC_FORBIDDEN);
									ctx.setSpecialContentRenderer("/jsp/view/no_access.jsp");
								}
							}
						}

						ctx.setCurrentTemplate(template);
						if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && staticConfig.isFixPreview()) {
							ctx.getRequest().setAttribute("components", ComponentFactory.getComponentForDisplay(ctx, true));

							/************************/
							/**** Shared Content ****/
							/************************/
							SharedContentService.prepare(ctx);
						}

						/** check content **/

						if (!ctx.isContentFound()) {
							logger.warning("content not found : "+ctx.getPath());
							globalContext.log(Log.SEVERE, "url", "page not found : " + ctx.getPath());
							globalContext.add404Url(ctx, ContentManager.getPath(request));
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
							logger.warning("page not found (" + globalContext.getContextKey() + ") : " + ctx.getPath());
							ctx.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
							if (ctx.isAsViewMode()) {
								MenuElement page404 = content.getNavigation(ctx).searchChildFromName(staticConfig.get404PageName());
								if (page404 != null) {
									ctx.setCurrentPageCached(page404);
									template = TemplateFactory.getTemplate(ctx, page404);
								} else {
									Template defaultTemplate = TemplateFactory.getDiskTemplates(getServletContext()).get(globalContext.getDefaultTemplate());
									if (defaultTemplate != null) {
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
								logger.warning("page undefined (" + globalContext.getContextKey() + ") : " + ctx.getPath());
								response.setStatus(HttpServletResponse.SC_NOT_FOUND);
							}
						}
					}
				}
				localLogger.endCount("content", "include content");
			}
			i18nAccess.resetRequestMap();

		} catch (Throwable t) {
			if (!response.isCommitted()) {
				try {
					response.setStatus(503);
					Writer out = response.getWriter();
					out.write("<div style=\"margin-top: 50px; margin-left: auto; margin-right: auto; border: 2px #ff0000 solid; width: 500px; padding: 3px;\" id=\"fatal-error\">");
					out.write("<h1 style=\"margin: 0px; padding: 1px; font-size: 120%; text-align: center;\">Techinal error.</h1>");
					out.write("<p style=\"text-align: center;\"><a href=\"mailto:" + staticConfig.getManualErrorEmail() + "?subject=fatal error in javlo : " + globalContext.getContextKey() + "\">Describe your error in a email.</a></p>");
					out.write("<p style=\"padding: 10px 10px 10px 10px; margin-bottom: 10px; color: #000000; border: 1px solid #ff0000; background-color: #ffeaea;\">" + t.getMessage() + "</p>");
					out.write("</div>");
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}

			if (!(t instanceof SocketException)) {
				t.printStackTrace();
				try {
					DebugListening.getInstance().sendError(ctx, t, "path=" + request.getRequestURI());
				} catch (Throwable tmail) {
				}
			} else {
				logger.warning(t.getMessage());
			}
		} finally {
			PersistenceService persistenceService;
			try {
				persistenceService = PersistenceService.getInstance(globalContext);
				String persistenceParam = requestService.getParameter(PERSISTENCE_PARAM, null);
				if (persistenceService.isAskStore() && StringHelper.isTrue(persistenceParam, true)) {
					persistenceService.store(ctx);
				}
				if (ctx != null && ctx.isClearSession()) {
					HttpSession session = ctx.getRequest().getSession();
					session.invalidate();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void writeInfo(PrintStream out) {
		StaticConfig staticConfig = StaticConfig.getInstance(getServletContext());
		Runtime runtime = Runtime.getRuntime();
		ThreadMXBean threads = ManagementFactory.getThreadMXBean();

		/** test email **/

		boolean smtpConnect = false;
		if (!StringHelper.isEmpty(staticConfig.getSMTPHost())) {
			Transport transport = null;
			try {
				if (StringHelper.isDigit(staticConfig.getSMTPPort())) {
					Session mailSession = MailService.getMailSession(null);
					transport = mailSession.getTransport("smtp");
					transport.connect(staticConfig.getSMTPHost(), Integer.parseInt(staticConfig.getSMTPPort()), staticConfig.getSMTPUser(), staticConfig.getSMTPPasswordParam());
					smtpConnect = transport.isConnected();
				} else {
					logger.severe("staticConfig.getSMTPPort() is not DIGIT : "+staticConfig.getSMTPPort());
				}
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				if (transport != null) {
					try {
						transport.close();
					} catch (MessagingException e) {
						e.printStackTrace();
					}
				}
			}
		}

		out.println("****************************************************************");
		out.println("****************************************************************");
		out.println("****");
		out.println("**** Current Time      :  " + StringHelper.renderTime(new Date()));
		out.println("**** Server info       :  " + getServletContext().getServerInfo());
		out.println("**** JAVA_HOME         :  " + System.getenv("JAVA_HOME"));
		out.println("**** JAVLO_HOME        :  " + StringHelper.neverNull(StaticConfig.getJavloHome()));
		out.println("**** System encoding   :  " + System.getProperty("file.encoding"));
		out.println("**** CMS encoding      :  " + ContentContext.CHARACTER_ENCODING);
		out.println("**** VERSION           :  " + VERSION);
		try {
			out.println("**** IP                :  " + InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		out.println("**** ENV               :  " + staticConfig.getEnv());
		out.println("**** Internet Access   :  " + staticConfig.isInternetAccess());
		out.println("**** STATIC CONFIG DIR :  " + staticConfig.getStaticConfigLocalisation());
		out.println("**** SITE LOG          :  " + staticConfig.isSiteLog());
		out.println("**** IP2 FILE          :  " + LocationService.ipDbFile);
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
		out.println("**** ACCESS LOG FOLDER :  " + staticConfig.getAccessLogFolder());
		out.println("**** IMAGE TEMP DIR    :  " + staticConfig.getImageCacheFolder());
		out.println("**** IMAGE AUTO FOCUS  :  " + staticConfig.isAutoFocus());
		out.println("**** SEARCH ENGINE     :  " + staticConfig.getSearchEngineClassName());

		out.println("**** #EXT MACRO        :  " + staticConfig.getSpecialMacros().size());

		try {
			out.println("**** GENERAL LISTNER   :  " + staticConfig.getGeneralLister().getClass());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			out.println("**** FIRST REQ. LIST.  :  " + staticConfig.getFirstRequestLister());
		} catch (Exception e) {
			e.printStackTrace();
		}
		out.println("**** MAIL THREAD       :  " + staticConfig.isMailingThread());
		out.println("**** MAIL HOST         :  " + staticConfig.getSMTPHost() + ':' + staticConfig.getSMTPPort() + " - [connection valid:" + smtpConnect + ']');
		out.println("**** MAILING FB URI    :  " + staticConfig.getMailingFeedBackURI());
		out.println("**** MAILING FB CLASSE :  " + staticConfig.getMailingFeedbackClass());
		out.println("**** MAIL HOST         :  " + staticConfig.getSMTPHost() + ':' + staticConfig.getSMTPPort() + " - [connection valid:" + smtpConnect + ']');
		out.println("**** MAIL SLEEP 2M     :  " + MailingThread.SLEEP_BETWEEN_MAIL_SEC);

		out.println("**** SLF4J COM. LOG    :  " + LoggerFactory.getILoggerFactory().getClass().getCanonicalName());

		out.println("**** ALL LOG LVL       :  " + staticConfig.getAllLogLevel());
		out.println("**** ACCESS LOG LVL    :  " + staticConfig.getAccessLogLevel());
		out.println("**** NAV LOG LVL       :  " + staticConfig.getNavigationLogLevel());
		out.println("**** SYNCHRO LOG LVL   :  " + staticConfig.getSynchroLogLevel());
		out.println("**** ALL COMP LOG LVL  :  " + staticConfig.getAllComponentLogLevel());
		out.println("**** ABST COMP LOG LVL :  " + staticConfig.getAbstractComponentLogLevel());
		out.println("**** BACKUP EXCL. PAT. :  " + staticConfig.getBackupExcludePatterns());
		out.println("**** BACKUP INCL. PAT. :  " + staticConfig.getBackupIncludePatterns());
		out.println("**** HIGH SECURE       :  " + staticConfig.isHighSecure());
		out.println("**** REDIRECT URL      :  " + staticConfig.isRedirectSecondaryURL());
		out.println("**** INTEGRITY CHECKER :  " + staticConfig.isIntegrityCheck());
		out.println("**** HARD USERS        :  " + StringHelper.collectionToString(staticConfig.getEditUsers().keySet(), ","));
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
