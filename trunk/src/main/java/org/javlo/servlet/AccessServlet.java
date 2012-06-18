package org.javlo.servlet;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.portlet.WindowState;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.actions.EditActions;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentComponentsList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.portlet.AbstractPortletWrapperComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.UserInterfaceContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.DebugHelper;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.MailingThread;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.message.PopupMessage;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageConfiguration;
import org.javlo.portlet.PortletWindowImpl;
import org.javlo.portlet.filter.MultiReadRequestWrapper;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.service.remote.RemoteMessage;
import org.javlo.service.remote.RemoteMessageService;
import org.javlo.service.syncro.SynchroThread;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.thread.AbstractThread;
import org.javlo.thread.ThreadManager;
import org.javlo.tracking.Tracker;
import org.javlo.user.IUserFactory;
import org.javlo.user.UserFactory;
import org.javlo.utils.DebugListening;

public class AccessServlet extends HttpServlet {

	private static final DecimalFormat df = new DecimalFormat("#####0.00");

	private static Boolean FIRST_REQUEST = true;

	MailingThread mailingThread = null;

	ThreadManager threadManager = null;

	/**
	 * create a static logger.
	 */
	public static Logger logger = Logger.getLogger(AccessServlet.class.getName());

	public static final String VERSION = "2.0.0.1";

	@Override
	public void destroy() {
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
	}

	public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		try {

			StaticConfig staticConfig = StaticConfig.getInstance(getServletContext());

			/** init log **/
			long startTime = System.currentTimeMillis();

			String requestLabel = request.getPathInfo();

			request.setCharacterEncoding(ContentContext.CHARACTER_ENCODING);

			RequestService requestService = RequestService.getInstance(request);

			// content type cannot be set to html if portlet resource - TODO: clean process method (with filter ?)
			if (requestService.getParameter("javlo-portlet-resource", null) == null || requestService.getParameter("javlo-portlet-id", null) == null) {
				response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);
			}

			GlobalContext globalContext = GlobalContext.getInstance(request);

			if (FIRST_REQUEST) {
				synchronized (FIRST_REQUEST) {
					if (FIRST_REQUEST) {
						FIRST_REQUEST = false;
						try {
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

			ContentContext ctx = ContentContext.getContentContext(request, response);

			logger.fine(requestLabel + " : first ContentContext " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");
			logger.fine("device : " + ctx.getDevice());

			/** get info from system to browser **/
			if (request != null && request.getPathInfo() != null && request.getPathInfo().endsWith("info.txt")) {
				response.setContentType("text/plain");
				PrintStream out = new PrintStream(response.getOutputStream());
				writeInfo(out);
				out.println("");
				globalContext.writeInfo(out);
				out.println("");
				ContentService content = ContentService.getInstance(globalContext);
				out.println("latest update by : " + content.getAttribute(ctx, "user.update"));
				out.close();
				return;
			}
			
			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());

			i18nAccess.requestInit(ctx);

			/* **************** */
			/* LOGIN EDIT/ADMIN */
			/* **************** */

			if (request.getServletPath().equals("/edit") || request.getServletPath().equals("/preview")) {
				if (!globalContext.isEditable()) {
					InfoBean.updateInfoBean(ctx);
					response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);					
					ServletHelper.includeBlocked(request, response);					
					return;
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

			logger.fine(requestLabel + " : i18nAccess.requestInit(ctx) " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");

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
			 * localLogger.startCount("mailing"); MailingContext mCtx = MailingContext.getInstance(request.getSession()); mCtx.setUnsubscribeHost(request.getServerName()); mCtx.setUnsubscribePort(request.getServerPort()); localLogger.endCount("mailing", "mailing lauch");
			 */

			localLogger.startCount("execute action");

			/*** update module status before action ***/
			ModulesContext moduleContext = ModulesContext.getInstance(request.getSession(), globalContext);
			if (requestService.getParameter("module", null) != null) {
				UserInterfaceContext uic = UserInterfaceContext.getInstance(request.getSession(), globalContext);
				uic.setCurrentModule(requestService.getParameter("module", null));
				moduleContext.setCurrentModule(requestService.getParameter("module", null));
				i18nAccess.setCurrentModule(globalContext, moduleContext.getCurrentModule());
			}
			if (requestService.getParameter("module", null) != null && requestService.getParameter("from-module", null) == null) {
				moduleContext.setFromModule((Module)null);
			}
			if (requestService.getParameter("from-module", null) != null) {
				Module fromModule = moduleContext.searchModule(requestService.getParameter("from-module", null));
				if (fromModule != null) {
					moduleContext.setFromModule(fromModule);
				}
			}

			if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {
				EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
				ctx.setArea(editCtx.getCurrentArea());
			}

			try {
				String action = ServletHelper.execAction(ctx);
				logger.fine(requestLabel + " : action " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");

				ContentService content = ContentService.getInstance(globalContext);
				ctx.getCurrentPage().updateLinkedData(ctx);
				MenuElement elem = content.getNavigation(ctx).getNoErrorFreeCurrentPage(ctx);
				/** INIT TEMPLATE **/
				if (ctx.getCurrentTemplate() == null || action != null) { // action can change the template
					Template template = null;
					if (elem != null) {
						PageConfiguration pageConfig = PageConfiguration.getInstance(globalContext);
						template = pageConfig.getCurrentTemplate(ctx, elem);
						if (template != null) {
							template = template.getFinalTemplate(ctx);
						}
						ctx.setCurrentTemplate(template);
					}
				}
				Template template = ctx.getCurrentTemplate();

				if ((ctx.getRenderMode() != ContentContext.ADMIN_MODE) && (ctx.getRenderMode() != ContentContext.EDIT_MODE)) {
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
									// newCtx.setContentLanguage(lg);
									newCtx.setRequestContentLanguage(lg);
								}
							}
							if (!content.contentExistForContext(newCtx)) {
								logger.info("content not found in " + ctx.getPath() + " lg:" + ctx.getRequestContentLanguage());
								ctx.setSpecialContentRenderer("/jsp/view/content_not_found.jsp");
							} else {
								ctx = newCtx;
								ctx.storeInRequest(request);
							}
						}
					}
				}

				logger.fine(requestLabel + " : content integrity " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");

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

				logger.fine(requestLabel + " : tracking " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");

				InfoBean infoBean = InfoBean.updateInfoBean(ctx);

				logger.fine(requestLabel + " : InfoBean " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");

				/***********/
				/* PORTLET */
				/**********/

				String portletId = requestService.getParameter("javlo-portlet-id", null);
				if (portletId != null) {
					MenuElement currentPage = ctx.getCurrentPage();
					IContentComponentsList contentList = currentPage.getAllContent(ctx);
					while (contentList.hasNext(ctx)) {
						IContentVisualComponent comp = contentList.next(ctx);
						if (comp instanceof AbstractPortletWrapperComponent) {
							AbstractPortletWrapperComponent portlet = (AbstractPortletWrapperComponent) comp;
							if (portletId.equals(portlet.getId())) {

								// serves a static resource within a portlet, whatever the mode
								if (requestService.getParameter("javlo-portlet-resource", null) != null) {
									portlet.renderPortletResource(ctx);
									return;
								} else if (request.getServletPath().equals("/edit")) {

									// render a specific portlet maximized (edit mode only) - plm
									// TODO: maximized in view
									PortletWindowImpl pw = portlet.getPortletWindow(ctx);

									// TODO: maximized in edit ?
									if (WindowState.MAXIMIZED.equals(pw.getWindowState())) {
										request.setAttribute("portlet", portlet);
										response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);
										getServletContext().getRequestDispatcher("/jsp/edit/template/portlet_max_edit.jsp").include(request, response);

										return;
									}
								}
							}
						}
					}
				}

				/** *** */
				/* EDIT */
				/** *** */

				if (request.getServletPath().equals("/edit")) {
					EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
					ctx.setArea(editCtx.getCurrentArea());
					if (editCtx.getAjaxRenderer() != null) {
						response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);

						getServletContext().getRequestDispatcher(editCtx.getAjaxRenderer()).include(request, response);
						editCtx.setAjaxRenderer(null);
					} else {
						if ((template == null) || (!template.exist())) {
							EditActions.performChoosetemplate(request, response);
						} else {
							if (!template.getAreas().contains(ctx.getArea())) {
								editCtx.setCurrentArea(ComponentBean.DEFAULT_AREA);
								ctx.setArea(ComponentBean.DEFAULT_AREA);
							}
						}
						ctx.setCurrentTemplate(template);

						response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);

						/***********************/
						/******* MODULES *******/
						/***********************/
						ServletHelper.prepareModule(ctx);
						getServletContext().getRequestDispatcher(editCtx.getEditTemplate()).include(request, response);
					}
					localLogger.endCount("edit", "include edit");
				} else {
					localLogger.startCount("content");

					if (request.getServletPath().equals("/preview")) {
						EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
						request.setAttribute("editPreview", editCtx.isEditPreview());
					}

					boolean renderXML = false;
					if (request.getPathInfo() != null) {
						if (StringHelper.getFileExtension(request.getPathInfo()).equalsIgnoreCase("xml")) {
							renderXML = true;
						}
					}

					/* user management - old login.jsp still used ? (plm) */
					if (requestService.getParameter("__logout", null) != null) {
						IUserFactory userFactory = UserFactory.createUserFactory(globalContext, request.getSession());
						userFactory.logout(ctx.getRequest().getSession());
						if (globalContext.getAllPrincipals().size() == 0) {
							content.releasePreviewNav(ctx);
						}
					}

					if (!globalContext.isVisible()) {
						ServletHelper.includeBlocked(request, response);
						return;
					}

					String path = ctx.getPath();

					if (!renderXML) {
						if (elem == null) {
							logger.warning("bad path : " + path);
						}
						if ((template == null) || (!template.exist()) || (template.getRendererFullName(ctx) == null)) {
							String msg = i18nAccess.getText("command.admin.block.no-template");
							infoBean.setContentLanguage(ctx.getContentLanguage());
							infoBean.setLanguage(ctx.getLanguage());
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

							ctx.setCurrentTemplate(template);

							String jspPath = template.getRendererFullName(ctx);
							getServletContext().getRequestDispatcher(jspPath).include(request, response);
						}
					} else {
						response.setContentType("text/xml; charset=" + ContentContext.CHARACTER_ENCODING);
						Writer out = response.getWriter();
						out.write(XMLHelper.getPageXML(ctx, elem));
					}
					localLogger.endCount("content", "include content");

					logger.fine(requestLabel + " : render " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");
				}

				// if (staticConfig.isAccessLogger()) {
				logger.fine(requestLabel + " : all process method " + df.format((double) (System.currentTimeMillis() - startTime) / (double) 1000) + " sec.");
				// }

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
		out.println("**** System encoding   :  " + System.getProperty("file.encoding"));
		out.println("**** CMS encoding      :  " + ContentContext.CHARACTER_ENCODING);
		/*
		 * out.println(""); out.println("**** ENV           :  "); Map<String,String> env = System.getenv(); for (Map.Entry<String, String> entry : env.entrySet()) { out.println(entry.getKey()+" : " + entry.getValue()); } out.println("");
		 */
		out.println("**** VERSION           :  " + VERSION);
		out.println("**** ENV               :  " + staticConfig.getEnv());
		out.println("**** STATIC CONFIG DIR :  " + staticConfig.getStaticConfigLocalisation());
		out.println("**** PROXY HOST        :  " + staticConfig.getProxyHost());
		out.println("**** PROXY PORT        :  " + staticConfig.getProxyPort());
		out.println("**** DIR RELATIVE      :  " + staticConfig.isDataFolderRelative());
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
		out.println("**** HARD USERS        :  " + StringHelper.collectionToString(staticConfig.getEditUsers().keySet(),","));
		out.println("**** TOTAL MEMORY      :  " + runtime.totalMemory() + " (" + runtime.totalMemory() / 1024 + " KB)" + " (" + runtime.totalMemory() / 1024 / 1024 + " MB)");
		out.println("**** FREE MEMORY       :  " + runtime.freeMemory() + " (" + runtime.freeMemory() / 1024 + " KB)" + " (" + runtime.freeMemory() / 1024 / 1024 + " MB)");
		out.println("**** THREAD ****");
		out.println("**** THREAD COUNT      :  " + threads.getThreadCount());
		out.println("**** THREAD STR COUNT  :  " + threads.getTotalStartedThreadCount());
		out.println("**** THREAD DMN COUNT  :  " + threads.getDaemonThreadCount());
		out.println("****");
		out.println("****************************************************************");
		out.println("****************************************************************");
	}
}
