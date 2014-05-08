package org.javlo.servlet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.ContentManager;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.data.InfoBean;
import org.javlo.helper.DebugHelper;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XMLHelper;
import org.javlo.i18n.I18nAccess;
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
import org.javlo.service.remote.RemoteMessage;
import org.javlo.service.remote.RemoteMessageService;
import org.javlo.service.shared.ISharedContentProvider;
import org.javlo.service.shared.SharedContentContext;
import org.javlo.service.shared.SharedContentService;
import org.javlo.service.social.SocialService;
import org.javlo.service.syncro.SynchroThread;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.thread.AbstractThread;
import org.javlo.thread.ThreadManager;
import org.javlo.tracking.Tracker;
import org.javlo.user.AdminUserSecurity;
import org.javlo.utils.DebugListening;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.FSImageWriter;

public class AccessServlet extends HttpServlet implements IVersion {

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
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	@Override
	public void init() throws ServletException {
		super.init();

		/** JSTL Constant **/
		getServletContext().setAttribute("BACK_PARAM_NAME", ElementaryURLHelper.BACK_PARAM_NAME);

		try {
			DebugHelper.updateLoggerLevel(getServletContext());
		} catch (Exception e1) {
			logger.warning("error when update logger level : " + e1.getMessage());
			e1.printStackTrace();
		}

		StaticConfig staticConfig = StaticConfig.getInstance(getServletContext());

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

		File defaultTemplateFolder = new File(getServletContext().getRealPath("/WEB-INF/template/"));
		for (File template : defaultTemplateFolder.listFiles()) {
			if (template.isDirectory()) {
				File templateFolder = new File(URLHelper.mergePath(staticConfig.getTemplateFolder(), template.getName()));
				if (!templateFolder.exists()) {
					templateFolder.getParentFile().mkdirs();
					logger.info("import default template : " + template.getName());
					try {
						FileUtils.copyDirectory(template, templateFolder);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {

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

			globalContext.initExternalService(ctx);

			if (FIRST_REQUEST) {
				synchronized (FIRST_REQUEST) {
					if (FIRST_REQUEST) {
						FIRST_REQUEST = false;
						try {
							GlobalContext.getDefaultContext(request.getSession()); // create
																					// default
																					// context
																					// if
																					// not
																					// exist
							GlobalContext.getMasterContext(request.getSession()); // create
																					// master
																					// context
																					// if
																					// not
																					// exist
							if (globalContext.getDMZServerIntra() != null) {
								SynchroThread synchro = (SynchroThread) AbstractThread.createInstance(staticConfig.getThreadFolder(), SynchroThread.class);
								synchro.initSynchronisationThread(staticConfig, globalContext, request.getSession().getServletContext());
								synchro.store();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}

			/** get info from system to browser **/
			if (ctx.getCurrentEditUser() != null && request != null && request.getPathInfo() != null && request.getPathInfo().endsWith("info.txt")) {
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
			if (ctx.isAsViewMode() && ctx.getCurrentPage().isCacheable(ctx) && globalContext.isPreviewMode() && globalContext.getPublishDate() != null && request.getMethod().equalsIgnoreCase("get") && request.getParameter("webaction") == null) {
				long lastModified = globalContext.getPublishDate().getTime();
				response.setDateHeader(NetHelper.HEADER_LAST_MODIFIED, lastModified);
				long lastModifiedInBrowser = request.getDateHeader(NetHelper.HEADER_IF_MODIFIED_SINCE);
				if (lastModified > 0 && lastModified / 1000 <= lastModifiedInBrowser / 1000) {
					COUNT_304++;
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return;
				}
			}

			ctx.getCurrentTemplate();
			request.setAttribute("frontCache", globalContext.getFrontCache(ctx));

			if (logger.isLoggable(Level.FINE)) {
				logger.fine(requestLabel + " : first ContentContext " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");
				logger.fine("device : " + ctx.getDevice());
			}

			I18nAccess i18nAccess = I18nAccess.getInstance(ctx);

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
				if (editCtx.getUserPrincipal() == null) {
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

			org.javlo.helper.Logger localLogger = new org.javlo.helper.Logger();

			localLogger.startCount("sid");

			/** ************************** */
			/** EXCECUTE CRYPTED ACTION * */
			/** ************************** */

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
								logger.info("content not found in " + ctx.getPath() + " lg:" + ctx.getRequestContentLanguage());
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
				// if (!request.getServletPath().equals("/admin")) {
				Tracker.trace(request, response);
				// }
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

						GlobalContext masterContext = GlobalContextFactory.getMasterGlobalContext(request.getSession().getServletContext());
						if (masterContext != null) {
							File editCSS = new File(URLHelper.mergePath(masterContext.getStaticFolder(), "/edit/specific.css"));
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

						getServletContext().getRequestDispatcher(editCtx.getEditTemplate()).include(request, response);
					}
					localLogger.endCount("edit", "include edit");
				} else { // view

					request.setAttribute("social", SocialService.getInstance(globalContext));

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
						response.setContentType("application/pdf;");
						OutputStream out = response.getOutputStream();
						ContentContext viewCtx = ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE);
						viewCtx.setAbsoluteURL(true);
						viewCtx.setFormat("html");
						viewCtx.resetDMZServerInter();

						Map<String, String> params = new HashMap<String, String>();
						params.put(Device.FORCE_DEVICE_PARAMETER_NAME, "pdf");
						params.put(ContentContext.FORCE_ABSOLUTE_URL, "true");
						if (!globalContext.isView() && globalContext.getBlockPassword() != null) {
							params.put("block-password", globalContext.getBlockPassword());
						}

						if (request.getParameter(Template.FORCE_TEMPLATE_PARAM_NAME) != null) {
							params.put(Template.FORCE_TEMPLATE_PARAM_NAME, request.getParameter(Template.FORCE_TEMPLATE_PARAM_NAME));
						}

						params.put("clean-html", "true");

						String url = URLHelper.createURL(viewCtx, params);

						/*
						 * if (staticConfig.getApplicationLogin() != null) { url
						 * = URLHelper.addCredential(url,
						 * staticConfig.getApplicationLogin(),
						 * staticConfig.getApplicationPassword()); }
						 * PDFConvertion.getInstance().convertXHTMLToPDF(url,
						 * out);
						 */
						PDFConvertion.getInstance().convertXHTMLToPDF(new URL(url), staticConfig.getApplicationLogin(), staticConfig.getApplicationPassword(), out);

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

							/** check page securised **/

							if (globalContext.getActivationKey() != null) {
								String activationKey = requestService.getParameter("activation-key", null);
								if (activationKey != null && activationKey.equals(globalContext.getActivationKey())) {
									globalContext.setActivationKey(null);
								} else {
									ctx.setSpecialContentRenderer("/jsp/view/activation.jsp");
								}
							}

							if (ctx.getCurrentPage().getUserRoles().size() > 0) {
								if (ctx.getCurrentUser() == null) {
									ctx.setSpecialContentRenderer("/jsp/view/login.jsp");
								} else {
									Set<String> roles = new HashSet<String>(ctx.getCurrentUser().getRoles());
									roles.retainAll(ctx.getCurrentPage().getUserRoles());
									if (roles.size() == 0 && !(ctx.getCurrentUser().isEditor() && AdminUserSecurity.getInstance().haveRight(ctx.getCurrentUser(), AdminUserSecurity.CONTENT_ROLE))) {
										ctx.setSpecialContentRenderer("/jsp/view/login.jsp");
									}
								}
							}

							if (ctx.getGlobalContext().isCollaborativeMode()) {
								Set<String> pageRoles = ctx.getCurrentPage().getEditorRoles();
								if ((pageRoles.size() > 0 || ctx.getCurrentEditUser() == null) && !ctx.getCurrentPage().getName().equals("registration")) { // leave
																																							// access
																																							// to
																																							// registration
																																							// page.
									if (ctx.getCurrentEditUser() == null || !ctx.getCurrentEditUser().validForRoles(pageRoles)) {
										ctx.setSpecialContentRenderer("/jsp/view/no_access.jsp");
									}
								}
							}

							ctx.setCurrentTemplate(template);

							if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && staticConfig.isFixPreview()) {
								ctx.getRequest().setAttribute("components", ComponentFactory.getComponentForDisplay(ctx));
								ModulesContext modulesContext = ModulesContext.getInstance(request.getSession(), globalContext);

								/************************/
								/**** Shared Content ****/
								/************************/

								if (modulesContext.searchModule("shared-content") != null) {
									SharedContentService sharedContentService = SharedContentService.getInstance(ctx);
									SharedContentContext sharedContentContext = SharedContentContext.getInstance(request.getSession());
									ctx.getRequest().setAttribute("sharedContentProviders", sharedContentService.getAllActiveProvider(ctx));
									ctx.getRequest().setAttribute("currentCategory", sharedContentContext.getCategory());
									ISharedContentProvider provider = sharedContentService.getProvider(ctx, sharedContentContext.getProvider());
									if (provider != null) {
										// set first category by default
										if ((sharedContentContext.getCategory() == null || !provider.getCategories(ctx).containsKey(sharedContentContext.getCategory())) && provider.getCategories(ctx).size() > 0) {
											sharedContentContext.setCategories(new LinkedList<String>(Arrays.asList(provider.getCategories(ctx).keySet().iterator().next())));
										}
										ctx.getRequest().setAttribute("provider", provider);
										ctx.setContentContextIfNeeded(provider);
										ctx.getRequest().setAttribute("sharedContent", provider.getContent(ctx, sharedContentContext.getCategories()));
										ctx.getRequest().setAttribute("sharedContentCategories", provider.getCategories(ctx).entrySet());
									} else {
										logger.warning("shared content not found = " + sharedContentContext.getProvider());
									}
								}
							}

							/** check content **/
							if (!ctx.isContentFound()) {
								ctx.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND, "page not found : " + ctx.getPath());
								if (ctx.isAsViewMode()) {
									MenuElement page404 = content.getNavigation(ctx).searchChildFromName(staticConfig.get404PageName());
									if (page404 != null) {
										ctx.setCurrentPageCached(page404);
									}
								}
							}

							String area = requestService.getParameter("only-area", null);
							if (area != null) {
								getServletContext().getRequestDispatcher("/jsp/view/content_view.jsp?area=" + area).include(request, response);
							} else {
								String jspPath = template.getRendererFullName(ctx);
								getServletContext().getRequestDispatcher(jspPath).include(request, response);
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

			} catch (Throwable t) {

				t.printStackTrace();

				response.setStatus(503);

				Writer out = response.getWriter();
				out.write("<div style=\"margin-top: 50px; margin-left: auto; margin-right: auto; border: 2px #ff0000 solid; width: 500px; padding: 3px;\" id=\"fatal-error\">");
				out.write("<h1 style=\"margin: 0px; padding: 1px; font-size: 120%; text-align: center;\">Techinal error.</h1>");
				out.write("<p style=\"text-align: center;\"><a href=\"mailto:manual_error@javlo.org?subject=fatal error in javlo : " + globalContext.getContextKey() + "\">Describe your error in a email.</a></p>");
				out.write("<p style=\"padding: 10px 10px 10px 10px; margin-bottom: 10px; color: #000000; border: 1px solid #ff0000; background-color: #ffeaea;\">" + t.getMessage() + "</p>");
				out.write("</div>");

				DebugListening.getInstance().sendError(request, t, "path=" + request.getRequestURI());
			} finally {
				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				if (persistenceService.isAskStore()) {
					persistenceService.store(ctx);
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
