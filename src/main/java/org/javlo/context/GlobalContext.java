package org.javlo.context;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.javlo.cache.ICache;
import org.javlo.cache.MapCache;
import org.javlo.component.form.GenericForm;
import org.javlo.config.StaticConfig;
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.data.taxonomy.TaxonomyServiceAgregation;
import org.javlo.ecom.EcomConfig;
import org.javlo.helper.*;
import org.javlo.helper.ElementaryURLHelper.Code;
import org.javlo.io.AppendableTextFile;
import org.javlo.io.TransactionFile;
import org.javlo.mailing.*;
import org.javlo.module.core.IPrintInfo;
import org.javlo.module.remote.RemoteService;
import org.javlo.module.ticket.TicketAction;
import org.javlo.navigation.IURLFactory;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.NoURLFactory;
import org.javlo.navigation.URLTriggerThread;
import org.javlo.service.*;
import org.javlo.service.exception.ServiceException;
import org.javlo.service.log.Log;
import org.javlo.service.log.LogContainer;
import org.javlo.service.notification.NotificationService;
import org.javlo.servlet.AccessServlet;
import org.javlo.servlet.ImageTransformServlet;
import org.javlo.template.TemplateData;
import org.javlo.tracking.Tracker;
import org.javlo.tracking.TrackerInitThread;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.utils.BooleanBean;
import org.javlo.utils.ConfigurationProperties;
import org.javlo.utils.StructuredProperties;
import org.javlo.utils.TimeMap;
import org.javlo.utils.backup.BackupBean;
import org.javlo.utils.backup.BackupThread;
import org.owasp.encoder.Encode;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class GlobalContext implements Serializable, IPrintInfo {

	public String GLOBAL_ERROR = null;

	private static final Date CREATION_DATE = new Date();

	public static final String PAGE_TOKEN_PARAM = "p_token";

	private final Integer[] countArrayMinute = new Integer[60];

	private long latestTime;

	private static int COUNT_INSTANCE = 0;

	private static final long serialVersionUID = 1L;

	private static final Object LOCK_GLOBAL_CONTEXT_LOAD = new Object();

	private final Object lockDataFile = new Object();

	private final Object lockUrlFile = new Object();

	private final Object lockImportTemplate = new Object();

	// private final Object lockLoadContent = new Object();

	private PrintWriter redirectURLList = null;

	private Properties redirectURLMap = null;

	private Properties url404Map = null;

	private POPThread popThread = null;

	private Object localLock = new Object();

	private Calendar latestTicketNotificaitonTime = Calendar.getInstance();

	private static final IURLFactory NO_URL_FACTORY = new NoURLFactory();

	private final String INSTANCE_ID = StringHelper.getLargeRandomIdBase64();

	public static final String SCREENSHOT_FILE_NAME = "screenshot.png";

	private TimeMap<String, String> tokenToMail = new TimeMap<String, String>(60 * 24);

	private List<String> quietArea = null;

	public static final String POP_HOST_PARAM = "mail.pop.host";
	public static final String POP_PORT_PARAM = "mail.pop.port";
	public static final String POP_USER_PARAM = "mail.pop.user";
	public static final String POP_PASSWORD_PARAM = "mail.pop.password";
	public static final String POP_SSL = "mail.pop.ssl";

	private boolean siteLog = false;
	private boolean siteLogStack = false;

	private LogContainer logList = new LogContainer();
	
	private Boolean host = null;
	private String hostName = null;

	private Set<String> languages = null;
	private Set<String> contentLanguages = null;
	private Set<String> defaultLanguages = null;

	private Properties proxyMappings = null;

	private static class StorePropertyThread extends Thread {

		private boolean stopStoreThread = false;

		private static final int SLEEP_BETWEEN_STORAGE = 15 * 1000; // 15 sec

		private Properties dataProperties = null;

		private Object lockDataFile;

		private String contextKey;

		private File dataFile;

		private File lastBakup;

		private BooleanBean needStoreData = null;

		private Calendar lastBakcup = Calendar.getInstance();

		public StorePropertyThread(GlobalContext globalContext) {
			super(StorePropertyThread.class.getSimpleName() + "-" + globalContext.getContextKey());
			this.contextKey = globalContext.getContextKey();
			this.needStoreData = globalContext.needStoreData;
			this.dataProperties = globalContext.dataProperties;
			this.lockDataFile = globalContext.lockDataFile;
			this.lastBakcup.roll(Calendar.DAY_OF_MONTH, false);
			try {
				this.dataFile = globalContext.getDataFile();
				this.lastBakup = globalContext.getDataBackupFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			logger.info("start store property thread : " + this.getName());
			while (!stopStoreThread) {
				if (needStoreData.isValue()) {
					needStoreData.setValue(false);
					saveData(dataProperties, lockDataFile, contextKey, dataFile);
					Calendar today = Calendar.getInstance();
					if (today.get(Calendar.DAY_OF_YEAR) != lastBakcup.get(Calendar.DAY_OF_YEAR)) {
						synchronized (this) {
							if (today.get(Calendar.DAY_OF_YEAR) != lastBakcup.get(Calendar.DAY_OF_YEAR)) {
								lastBakcup = today;
								try {
									logger.info("store backup data : " + lastBakup);
									ResourceHelper.writeFileToFile(dataFile, lastBakup);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
				try {
					Thread.sleep(SLEEP_BETWEEN_STORAGE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			logger.info("stop store property thread : " + this.getName());
		}

		public void setDataProperties(Properties dataProperties) {
			this.dataProperties = dataProperties;
		}
	}

	public static void main(String[] args) {
		Map<Object, Object> map = new HashMap();
		map.put("test", "coucou");
		map.put("test", "tralala");
		for (Map.Entry<Object, Object> entry : map.entrySet()) {
			System.out.println(entry.getKey() + " = " + entry.getValue());
		}
	}

	private Map<String, ICache> cacheMaps = null;

	private TimeMap<String, Object> sharingMap = new TimeMap<String, Object>(60 * 30);
	private TimeMap<Object, String> reverseSharingMap = new TimeMap<Object, String>(60 * 30);

	private final Map<String, ICache> eternalCacheMaps = new Hashtable<String, ICache>();

	private SpecialConfigBean config = null;

	private IURLFactory urlFactory = null;

	/**
	 * public for urls.jsp
	 */
	private Map<String, MenuElement> viewPages = null;

	private IURLFactory urlFromFactoryImported = NO_URL_FACTORY;

	private Boolean externalServiceInitalized = false;

	private ServletContext application;

	private URLTriggerThread pageChangeNotificationThread;

	private URLTriggerThread dynamicComponentChangeNotificationThread;

	private URLTriggerThread ticketChangeNotificationThread;

	private RemoteService remoteService;

	private Integer firstLoadVersion = null;

	private int stopUndoVersion = 0;

	private Integer latestUndoVersion = null;

	private StorePropertyThread storePropertyThread = null;

	private BackupThread backupThread = null;

	private final Object i18nLock = new Object();

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(GlobalContext.class.getName());

	private static final String REDIRECT_URL_LIST = "redirect_url_list.properties";

	private static final String URL_404_LIST = "404_url_list.properties";

	private static final String KEY = "globalContext";

	public static final String LICENCE_BASE = "base";

	public static final String LICENCE_BASE_PLUS = "base+";

	public static final String LICENCE_CORPORATE = "corporate";

	public static final String USERS_FOLDER = "users_files";

	public String getInstanceId() {
		return INSTANCE_ID;
	}

	public GlobalContext(String contextKey) {
		this.contextKey = contextKey;
		COUNT_INSTANCE++;
		logger.info("create globalContext : " + contextKey + " [total globalContext : " + COUNT_INSTANCE + ']');
	}

	private void startThread() {
		if (getMainContext() == null && (storePropertyThread == null || storePropertyThread.stopStoreThread)) {
			storePropertyThread = new StorePropertyThread(this);
			storePropertyThread.start();
		}
		/** backup **/
		if (isBackupThread() && getMainContext() == null) {
			startBackupThread();
		}
		activePopThread();
	}

	private synchronized void startBackupThread() {
		if (backupThread == null || !backupThread.run) {
			File backupFolder = new File(URLHelper.mergePath(getBackupDirectory(), "thread"));
			backupThread = new BackupThread(backupFolder);
			if (staticConfig.getBackupInterval() > 0) {
				if (staticConfig.getDbBackupCount() > 0) {
					BackupBean backupBean = new BackupBean(getDataBaseFolder(), staticConfig.getDbBackupInterval(), staticConfig.getDbBackupCount());
					backupThread.addBackup(backupBean);
				}
				if (staticConfig.getUsersBackupCount() > 0) {
					BackupBean backupBean = new BackupBean(new File(URLHelper.mergePath(getDataFolder(), staticConfig.getUserFolder())), staticConfig.getUsersBackupInterval(), staticConfig.getUsersBackupCount());
					backupThread.addBackup(backupBean);
				}
				if (staticConfig.getFormsBackupCount() > 0) {
					BackupBean backupBean = new BackupBean(new File(URLHelper.mergePath(getDataFolder(), staticConfig.getStaticFolder(), GenericForm.DYNAMIC_FORM_RESULT_FOLDER)), staticConfig.getFormsBackupInterval(), staticConfig.getFormsBackupCount());
					backupThread.addBackup(backupBean);
				}
				backupThread.start();
			}
		}
	}

	public static GlobalContext getDefaultContext(HttpSession session) throws IOException {
		return getRealInstance(session, StaticConfig.getInstance(session).getDefaultContext(), false);
	}

	public static GlobalContext getMasterContext(HttpSession session) throws IOException {
		GlobalContext masterContext = getRealInstance(session, StaticConfig.getInstance(session).getMasterContext(), true);
		return masterContext;
	}
	
	public GlobalContext getMasterContext(ContentContext ctx) throws IOException {
		final String KEY = "_masterGlobalContext";
		GlobalContext masterContext = (GlobalContext) ctx.getServletContext().getAttribute(KEY);
		if (masterContext == null) {
			masterContext = getRealInstance(ctx.getRequest().getSession(), StaticConfig.getInstance(ctx.getRequest().getSession()).getMasterContext(), true);
			PersistenceService persistenceService;
			try {
				persistenceService = PersistenceService.getInstance(masterContext);
				if (!persistenceService.isLoaded()) {
					Map<String, String> contentAttributeMap = new HashMap<String, String>();
					ContentContext masterContentContext = new ContentContext(ctx);
					masterContentContext.setForceGlobalContext(masterContext);
					persistenceService.load(masterContentContext, ctx.getRenderMode(), contentAttributeMap, null);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			ctx.getServletContext().setAttribute(KEY, masterContext);
		}
		return masterContext;
	}

	public static GlobalContext getSessionInstance(HttpSession session) {
		return (GlobalContext) session.getAttribute(KEY);
	}

	public boolean isDefinedByHost() {
		return properties.getBoolean("define_by_host", true);
	}

	public void setDefinedByHost(boolean define) {
		if (isDefinedByHost() != define) {
			properties.setProperty("define_by_host", define);
			save();
		}
	}

	public static GlobalContext getMainInstance(HttpServletRequest request) {
		GlobalContext globalContext = getInstance(request);
		if (globalContext.getMainContext() != null) {
			return globalContext.getMainContext();
		} else {
			return globalContext;
		}
	}

	public static GlobalContext getInstance(HttpServletRequest request) {
		try {
			String contextURI;
			GlobalContext globalContext = (GlobalContext) request.getAttribute(KEY);
			if (globalContext == null) {
				StaticConfig staticConfig = StaticConfig.getInstance(request.getSession().getServletContext());
				if (staticConfig.isHostDefineSite()) {
					String host = ServletHelper.getSiteKey(request);
					globalContext = GlobalContext.getInstance(request.getSession(), host);
					if (globalContext != null) {
						globalContext.setHost(true, request.getServerName());
					}
					contextURI = host;
				} else {
					RequestService requestService = RequestService.getInstance(request);
					if (StringHelper.isTrue(requestService.getParameter("__check_context", "true"))) {
						contextURI = ContentManager.getContextName(request);
						if (contextURI.trim().length() > 0 && GlobalContext.isExist(request, contextURI)) {
							globalContext = GlobalContext.getInstance(request.getSession(), contextURI);
							if (!globalContext.getContextKey().equals(contextURI)) { // alias
								ContentContext.setForcePathPrefix(request, contextURI);
							}
							globalContext.setHost(false, null);
							ContentContext.setHostDefineSite(request, false);
							globalContext.setDefinedByHost(false);
						} else {
							String host = ServletHelper.getSiteKey(request);
							globalContext = GlobalContext.getInstance(request.getSession(), host);
							contextURI = host;
							if (globalContext == null) {
								logger.severe("error GlobalContext not found : " + request.getRequestURI());
								return null;
							}
							globalContext.setHost(true, request.getServerName());
							if (!globalContext.getContextKey().equals(contextURI)) { // alias
								ContentContext.setForcePathPrefix(request, contextURI);
								globalContext.setDefinedByHost(false);
							}
							ContentContext.setHostDefineSite(request, true);
						}
					} else {
						logger.severe("error GlobalContext undefined : " + request.getRequestURI());
						return null;
					}
				}
				request.getSession().setAttribute(KEY, globalContext); // mark
																		// global
																		// context
																		// in
																		// session.
			} else {
				contextURI = globalContext.getContextKey();
			}
			request.setAttribute(KEY, globalContext);

			return globalContext;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static GlobalContext getSessionContext(HttpSession session) {
		return (GlobalContext) session.getAttribute(KEY);
	}

	public static String getSessionContextKey(HttpSession session) {
		return (String) session.getAttribute(KEY);
	}

	public static GlobalContext getInstance(HttpSession session, String contextKey) throws IOException {
		if (contextKey == null) {
			return null;
		}
		GlobalContext globalContext = (GlobalContext) session.getAttribute(KEY + "_" + contextKey);
		if (globalContext == null) {
			contextKey = contextKey.toLowerCase();
			GlobalContext newInstance = getRealInstance(session, contextKey);
			String alias = newInstance.getAliasOf();
			if (alias.trim().length() > 0) {
				if (newInstance.isAliasActive()) {
					newInstance.setMainContext(getRealInstance(session, alias));
				} else {
					String newContextKey = StringHelper.stringToFileName(alias);
					if (!newContextKey.equals(contextKey)) {
						newInstance = getRealInstance(session, alias);
						newInstance.setSourceContextKey(contextKey);
					}
				}
			}
			session.setAttribute(KEY, newInstance);
			session.setAttribute(KEY + "_" + contextKey, newInstance);
			globalContext = newInstance;
		}

		return globalContext;
	}

	public static GlobalContext getInstance(ServletContext application, StaticConfig staticConfig, File configFile) throws IOException {
		String contextKey = FilenameUtils.getBaseName(configFile.getName());
		GlobalContext newInstance = (GlobalContext) application.getAttribute(contextKey);
		if (newInstance == null) {
			synchronized (LOCK_GLOBAL_CONTEXT_LOAD) {
				newInstance = (GlobalContext) application.getAttribute(contextKey);
				if (newInstance == null) {
					newInstance = new GlobalContext(contextKey);
					newInstance.application = application;
					newInstance.cacheMaps = new TimeMap<String, ICache>(staticConfig.getCacheMaxTime(), staticConfig.getCacheMaxSize());
					synchronized (newInstance.properties) {
						newInstance.staticConfig = staticConfig;
						application.setAttribute(contextKey, newInstance);
						if (configFile.exists()) {
							newInstance.properties.clear();
							newInstance.properties.load(configFile);
						}
						int countAccess = newInstance.properties.getInt("session-count", 0);
						newInstance.properties.setProperty("session-count", countAccess + 1);
						newInstance.properties.setProperty("access-date", StringHelper.renderTime(new Date()));
						newInstance.contextFile = configFile;
						newInstance.save();
						newInstance.loadFiles();
					}
					newInstance.startThread();
				}
			}
		}
		return newInstance;
	}

	private void loadFiles() {
		File tokenFile = getTokenPageFile();
		if (tokenFile.exists()) {
			try {
				pageTimeToken.load(tokenFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static GlobalContext getRealInstance(HttpSession session, String contextKey) throws IOException {
		return getRealInstance(session, contextKey, true);
	}

	private File getRedirectURLListFile() {
		return new File(URLHelper.mergePath(getDataFolder(), REDIRECT_URL_LIST));
	}

	private File get404URLListFile() {
		return new File(URLHelper.mergePath(getDataFolder(), URL_404_LIST));
	}

	public void reset404File() {
		synchronized (lockUrlFile) {
			get404URLListFile().delete();
			url404Map = null;
		}
	}

	private static GlobalContext getRealInstance(HttpSession session, String contextKey, boolean copyDefaultContext) throws IOException {

		contextKey = StringHelper.stringToFileName(contextKey);

		StaticConfig staticConfig = StaticConfig.getInstance(session.getServletContext());

		synchronized (LOCK_GLOBAL_CONTEXT_LOAD) {

			// ServletContextWeakReference gcc =
			// ServletContextWeakReference.getInstance(session.getServletContext());
			GlobalContext newInstance = (GlobalContext) session.getServletContext().getAttribute(contextKey);

			if (newInstance == null) {
				// LocalLogger.log("getRealInstance : "+contextKey+" - CRAETE NEW CONTEXT");
				newInstance = new GlobalContext(contextKey);
				newInstance.staticConfig = staticConfig;
				newInstance.application = session.getServletContext();
				newInstance.cacheMaps = new TimeMap<String, ICache>(staticConfig.getCacheMaxTime(), staticConfig.getCacheMaxSize());
				newInstance.siteLog = staticConfig.isSiteLog();
				newInstance.log(Log.INFO, Log.GROUP_INIT_CONTEXT, "Create context : " + contextKey, false);
			} else {
				newInstance.staticConfig = staticConfig;
				return newInstance;
			}

			String fileName = contextKey + ".properties";
			newInstance.contextFile = new File(ElementaryURLHelper.mergePath(staticConfig.getContextFolder(), fileName));
			if (!newInstance.contextFile.exists()) {
				File archiveFile = new File(ElementaryURLHelper.mergePath(staticConfig.getContextArchiveFolder(), fileName));
				if( archiveFile.exists()) {
					logger.info(("import archive context from : "+archiveFile));
					newInstance.contextFile.getParentFile().mkdirs();
					FileUtils.moveFile(archiveFile, newInstance.contextFile);
				}
			}
			if (!newInstance.contextFile.exists()) {
				if (!newInstance.contextFile.getParentFile().exists()) {
					newInstance.contextFile.getParentFile().mkdirs();
					newInstance.creation = true;
				}
				logger.info("create new context file : " + newInstance.contextFile);
				newInstance.log(Log.INFO, Log.GROUP_INIT_CONTEXT, "create new context file : " + newInstance.contextFile, false);
				newInstance.contextFile.createNewFile();
				synchronized (newInstance.properties) {
					newInstance.properties.load(newInstance.contextFile);
					newInstance.properties.setProperty("creation-date", StringHelper.renderTime(new Date()));
					if (staticConfig.isRandomDataFoder()) {
						newInstance.properties.setProperty("folder", "data-" + StringHelper.getRandomId());
					} else {
						newInstance.properties.setProperty("folder", "data-" + contextKey);
					}
					String password = "" + (1000 + Math.round(Math.random() * 8999));
					newInstance.setPassword(password);
					newInstance.setFirstPassword(password);
					if (StringHelper.isEmpty(newInstance.getFolder())) {
						newInstance.GLOBAL_ERROR = "Error on load : '" + newInstance.contextFile + "' folder in empty.";
					}
					if (copyDefaultContext) {
						GlobalContext defaultContext = getDefaultContext(session);
						if (defaultContext.isValid()) {
							newInstance.setAdministrator(defaultContext.getAdministrator());
							newInstance.setAdminManagement(defaultContext.isAdminManagement());
							newInstance.setChangeLicence(defaultContext.isChangeLicence());
							newInstance.setChangeMenu(defaultContext.isChangeMenu());
							newInstance.setComponents(defaultContext.getComponents());
							newInstance.setCSSInline(defaultContext.isCSSInline());
							newInstance.setDefaultLanguages(StringHelper.collectionToString(defaultContext.getDefaultLanguages(), ";"));
							newInstance.setDefaultTemplate(defaultContext.getDefaultTemplate());
							newInstance.setDownloadContent(defaultContext.isDownloadContent());
							newInstance.setEasy(defaultContext.isEasy());
							newInstance.setExtendMenu(defaultContext.isExtendMenu());
							newInstance.setGlobalTitle(defaultContext.getGlobalTitle());
							newInstance.setHelpLink(defaultContext.isHelpLink());
							newInstance.setHelpURL(defaultContext.getHelpURL());
							newInstance.setMainHelpURL(defaultContext.getMainHelpURL());
							newInstance.setLook(defaultContext.getLook());
							newInstance.setMacros(defaultContext.getMacros());
							newInstance.setMailing(defaultContext.isMailing());
							newInstance.setPageStructure(defaultContext.isPageStructure());
							newInstance.setPortail(defaultContext.isPortail());
							newInstance.setPrivatePage(defaultContext.isPrivatePage());
							newInstance.setRAWLanguages(defaultContext.getRAWLanguages());
							newInstance.setReversedLink(defaultContext.isReversedLink());
							newInstance.setTemplateFilter(defaultContext.isTemplateFilter());
							newInstance.setTemplatesNames(defaultContext.getTemplatesNames());
							newInstance.setUserManagement(defaultContext.isUserManagement());
							newInstance.setViewBar(defaultContext.isViewBar());
							newInstance.setVirtualPaternity(defaultContext.isVirtualPaternity());
							newInstance.setAutoSwitchToDefaultLanguage(defaultContext.isAutoSwitchToDefaultLanguage());
							newInstance.setOpenFileAsPopup(defaultContext.isOpenExternalLinkAsPopup());
							newInstance.setNoPopupDomainRAW(defaultContext.getNoPopupDomainRAW());
							newInstance.setModules(defaultContext.getModules());
							newInstance.setData("shared-content-active", defaultContext.getData("shared-content-active"));
							if (defaultContext.getDMZServerInter() != null) {
								newInstance.setDMZServerInter(defaultContext.getDMZServerInter().toString());
							}
							if (defaultContext.getDMZServerIntra() != null) {
								newInstance.setDMZServerIntra(defaultContext.getDMZServerIntra().toString());
							}
							String defaultContentFolder = defaultContext.getDataFolder();
							String newContentFolder = newInstance.getDataFolder();
							File defaultDir = new File(defaultContentFolder);
							if (defaultDir.exists()) {
								File targetDir = new File(newContentFolder);
								if (!targetDir.exists()) {
									FileUtils.copyDirectory(defaultDir, targetDir);
								}
							}
						}
					}
				}
			} else {
				newInstance.log(Log.INFO, Log.GROUP_INIT_CONTEXT, "load context file : " + newInstance.contextFile, false);
				synchronized (newInstance.properties) {
					newInstance.properties.load(newInstance.contextFile);
					newInstance.log(Log.INFO, Log.GROUP_INIT_CONTEXT, "folder : " + newInstance.getFolder(), false);
					File folder = new File(newInstance.getDataFolder());
					newInstance.log(Log.INFO, Log.GROUP_INIT_CONTEXT, "data file : " + folder + " (exist?" + folder.exists() + ')', false);
				}
			}
			newInstance.startThread();

			session.getServletContext().setAttribute(contextKey, newInstance);
			session.setAttribute(KEY, newInstance);

			synchronized (newInstance.properties) {
				newInstance.properties.setProperty("access-date", StringHelper.renderTime(new Date()));
			}
			newInstance.writeInfo(session, System.out);
			newInstance.cleanDataAccess();
			newInstance.loadFiles();

			if (newInstance.getSiteLogFile().exists()) {
				newInstance.getSiteLogFile().delete();
			} else {
				newInstance.getSiteLogFile().getParentFile().mkdir();
			}

			try {
				(new TrackerInitThread(Tracker.getTracker(newInstance, session))).start();
			} catch (ServiceException e) {
				e.printStackTrace();
			}

			// TODO : init resource Id

			return newInstance;
		}
	}

	public void initExternalService(ContentContext ctx) {
		if (!externalServiceInitalized) {
			synchronized (this) {
				if (!externalServiceInitalized) {
					externalServiceInitalized = true;
					// put here code to initialize external services
					// Start "page changes notifications"
					if (getStaticConfig().isNotificationThread()) {
						int secBetweenCheck = getStaticConfig().getTimeBetweenChangeNotification();
						Map<String, String> params = new HashMap<String, String>();
						params.put("webaction", "view.checkChangesAndNotify");
						params.put(ContentContext.FORWARD_AJAX, "true");
						ContentContext absoluteCtx = ctx.getContextForAbsoluteURL();
						absoluteCtx.setRenderMode(ContentContext.VIEW_MODE);
						String url = URLHelper.createURL(absoluteCtx, "/", params);
						try {
							URL urlToTrigger = new URL(url);
							pageChangeNotificationThread = new URLTriggerThread("PageChangeNotificationThread", secBetweenCheck, urlToTrigger);
							pageChangeNotificationThread.start();
							logger.info(pageChangeNotificationThread.getName() + " started.");
						} catch (MalformedURLException ex) {
							ex.printStackTrace();
						}
					}
					if (getStaticConfig().isNotificationDyanamicComponentThread()) {
						int secBetweenCheck = getStaticConfig().getTimeBetweenChangeNotificationForDynamicComponent();
						Map<String, String> params = new HashMap<String, String>();
						params.put("webaction", "view.checkChangesAndNotifyDynamicComponent");
						params.put(ContentContext.FORWARD_AJAX, "true");
						ContentContext absoluteCtx = ctx.getContextForAbsoluteURL();
						absoluteCtx.setRenderMode(ContentContext.VIEW_MODE);
						String url = URLHelper.createURL(absoluteCtx, "/", params);
						try {
							URL urlToTrigger = new URL(url);
							dynamicComponentChangeNotificationThread = new URLTriggerThread("DynamicComponentChangeNotificationThread", secBetweenCheck, urlToTrigger);
							dynamicComponentChangeNotificationThread.start();
							logger.info(dynamicComponentChangeNotificationThread.getName() + " started.");
						} catch (MalformedURLException ex) {
							ex.printStackTrace();
						}
					}
					// Start "ticket changes notifications"
					if (this.getModules().contains(TicketAction.MODULE_NAME)) {
						int secBetweenCheck = getStaticConfig().getTimeBetweenChangeNotification();
						Map<String, String> params = new HashMap<String, String>();
						params.put("webaction", "view.sendTicketChangeNotifications");
						params.put(ContentContext.FORWARD_AJAX, "true");
						params.put(Tracker.TRACKING_PARAM, "false");
						params.put(AccessServlet.PERSISTENCE_PARAM, "false");
						params.put(ContentContext.CLEAR_SESSION_PARAM, "true");
						ContentContext absoluteCtx = ctx.getContextForAbsoluteURL();
						absoluteCtx.setRenderMode(ContentContext.VIEW_MODE);
						String url = URLHelper.createURL(absoluteCtx, "/", params);
						try {
							URL urlToTrigger = new URL(url);
							ticketChangeNotificationThread = new URLTriggerThread("TicketChangeNotificationThread", secBetweenCheck, urlToTrigger);
							ticketChangeNotificationThread.start();
							logger.info(ticketChangeNotificationThread.getName() + " started.");
						} catch (MalformedURLException ex) {
							ex.printStackTrace();
						}
					}
					// Start remote service
					if (this.getModules().contains(RemoteService.MODULE_NAME)) {
						try {
							remoteService = RemoteService.getInstance(ctx);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public void destroy() {
		// put here code to destroy the global context
		// Stop "page changes notifications"
		if (pageChangeNotificationThread != null) {
			pageChangeNotificationThread.stopThread();
		}
		// Stop "ticket changes notifications"
		if (ticketChangeNotificationThread != null) {
			ticketChangeNotificationThread.stopThread();
		}
		// stop "dynamic component trigger"
		if (dynamicComponentChangeNotificationThread != null) {
			dynamicComponentChangeNotificationThread.stopThread();
		}
		// Stop remote service
		if (remoteService != null) {
			remoteService.stopService();
		}
		if (popThread != null) {
			popThread.setStop(true);
		}
	}

	public synchronized void activePopThread() {
		if (!StringHelper.isEmpty(getPOPHost()) && getPOPPort() > 0) {
			if (popThread != null && !popThread.isStop()) {
				popThread.setStop(true);
			}
			popThread = new POPThread(this);
			popThread.start();
		}
	}

	/**
	 * check if there are at least one context
	 * 
	 * @param request
	 * @return
	 */
	/*public static boolean haveContext(HttpServletRequest request) {
		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
		File dir = new File(staticConfig.getContextFolder());
		if (dir != null && dir.listFiles() != null) {
			return dir.listFiles().length > 0;
		} else {
			return false;
		}
	}*/

	public static boolean isExist(HttpServletRequest request, String contextKey) throws IOException {

		if (request.getSession().getServletContext().getAttribute(contextKey) != null) {
			return true;
		}

		contextKey = StringHelper.stringToFileName(contextKey);
		String fileName = contextKey + ".properties";

		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());

		File contextFile = new File(ElementaryURLHelper.mergePath(staticConfig.getContextFolder(), fileName));
		File archiveFile = new File(ElementaryURLHelper.mergePath(staticConfig.getContextArchiveFolder(), fileName));

		return contextFile.exists() || archiveFile.exists();
	}

	protected final ConfigurationProperties properties = new ConfigurationProperties();

	private String contextKey = null;

	private String sourceContextKey = null;

	private GlobalContext mainContext = null;

	private List<GlobalContext> subContexts = null;

	private boolean creation = false;

	private static final String VOTES = "";

	private Class<IUserFactory> userFactoryClass = null;

	private IUserFactory userFactory = null;

	private String userFactoryClassName = "";

	private Class<IUserFactory> adminUserFactoryClass = null;

	private AdminUserFactory admimUserFactory = null;

	private String adminUserFactoryClassName = "";

	public StaticConfig staticConfig = null;

	private Map<String, String> uriAlias = null;

	private File contextFile = null;

	private Properties dataProperties = null;

	private static final String URI_ALIAS_KEY_PREFIX = "uri-alias.";

	private static final String TRANSFORM_SHORT_KEY_PREFIX = "tsf-short-";

	private static final String TRANSFORM_LONG_KEY_PREFIX = "tsf-long-";

	public static final String LICENCE_FREE = "free";

	public static final String LICENCE_FREE_PLUS = "free+";

	public static final String ACCESS_DATE_FORMAT = "yyyy-MM-dd";

	private final transient Map<String, WeakReference<User>> allUsers = new WeakHashMap<String, WeakReference<User>>();

	private final transient Map<Code, WeakReference<Code>> specialAccessCode = new WeakHashMap<Code, WeakReference<Code>>();

	private final Map<String, String> resourcePathToId = new HashMap<String, String>();

	private final Map<String, String> resourceIdToPath = new HashMap<String, String>();

	private final Map<Object, Object> attributes = new HashMap<Object, Object>();

	private final TimeMap<Object, Object> timeAttributes = new TimeMap<Object, Object>(60 * 5);

	private final TimeMap<String, String> forcedContent = new TimeMap<String, String>(60 * 60);

	private TemplateData templateData = null;

	private TimeTravelerContext timeTravelerContext = new TimeTravelerContext();

	private String pathPrefix;

	private Set<String> noPopupDomain = null;

	private BooleanBean needStoreData = new BooleanBean(false);

	private Long accountSize = null;

	private String dataFolder = null;

	private String sharedDataFolder = null;

	private AppendableTextFile specialLogFile = null;

	private final Map<String, String> oneTimeTokens = Collections.synchronizedMap(new TimeMap<String, String>(60 * 60));

	private final TimeMap<String, String> pageTimeToken = new TimeMap<String, String>(60 * 60 * 24 * 90);

	private final Map<String, String> changePasswordToken = Collections.synchronizedMap(new TimeMap<String, String>(60 * 60));

	public final Object RELEASE_CACHE = new Object();

	private EcomConfig ecomConfig;

	public long getAccountSize() {
		if (accountSize == null) {
			File file = new File(getDataFolder());
			if (getStaticConfig().isAccountSize() && file.exists()) {
				try {
					accountSize = FileUtils.sizeOfDirectory(file);
				} catch (Throwable t) {
					t.printStackTrace();
					return -1;
				}
			} else {
				return -1;
			}
		}
		return accountSize;
	}

	public String getAccountSizeLabel() {
		return StringHelper.renderSize(getAccountSize());
	}

	public void addPrincipal(User principal) {
		synchronized (allUsers) {
			allUsers.put(principal.getName(), new WeakReference<User>(principal));
		}
	}

	public void addSpecialAccessCode(Code code) {
		synchronized (specialAccessCode) {
			specialAccessCode.put(code, new WeakReference<Code>(code));
			LangHelper.clearWeekReferenceMap(specialAccessCode);
		}
	}

	public void addTag(String tag) {
		synchronized (properties) {
			List<String> tags = new ArrayList<String>(getTags());
			tags.add(tag);
			properties.setProperty("tags", StringHelper.collectionToString(tags));
			save();
		}
	}

	public void addTemplate(String templateId) {
		synchronized (properties) {
			List<String> templates;
			templates = getTemplatesNames();
			if (!templates.contains(templateId)) {
				templates.add(templateId);
				setTemplatesNames(templates);
			}
		}
	}

	public void removeTemplate(String templateId) {
		synchronized (properties) {
			List<String> templates;
			templates = getTemplatesNames();
			if (templates.contains(templateId)) {
				templates.remove(templateId);
				setTemplatesNames(templates);
			}
		}
	}

	/*
	 * public boolean administratorLogin(String inLogin, String inPassword) { String
	 * login = properties.getString("admin", "admin"); String password =
	 * properties.getString("admin.password", "1234"); return login.equals(inLogin)
	 * && password.equals(StringHelper.encryptPassword(inPassword)); }
	 */

	public void cleanFolder() {
		properties.setProperty("folder", null);
	}

	public void delete(ServletContext application) {
		File trash = new File(contextFile.getParentFile().getAbsolutePath() + "/.trash");
		trash.mkdirs();
		try {
			FileUtils.copyFileToDirectory(contextFile, trash);
			properties.clear();
			FileUtils.forceDelete(contextFile);
			synchronized (staticConfig.getContextFolder()) {
				application.removeAttribute(contextKey);
			}
			storePropertyThread.stopStoreThread = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void eventLogin(String userName) {
		synchronized (properties) {
			properties.setProperty("login-date", StringHelper.renderTime(new Date()));
			properties.setProperty("latest-user", userName);
			save();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		storePropertyThread.stopStoreThread = true;
		ResourceHelper.closeResource(redirectURLList);
		COUNT_INSTANCE--;
		if (backupThread != null) {
			backupThread.run = false;
		}
		if (popThread != null) {
			popThread.setStop(true);
		}
		super.finalize();
	}

	public String getAdministrator() {
		return properties.getString("admin", "admin@javlo.org");
	}

	public String getAdministratorEmail() {
		return getAdministrator();
	}

	public Set<String> getAdminUserRoles() {
		String roleRaw = properties.getString("admin-roles", null);
		if (roleRaw == null || roleRaw.trim().length() == 0) {
			return Collections.emptySet();
		}
		String[] roles = StringHelper.stringToArray(roleRaw);
		Set<String> outRole = new HashSet<String>(Arrays.asList(roles));
		return outRole;
	}

	public String getAliasOf() {
		return properties.getString("alias", "");
	}

	public boolean isAliasActive() {
		return StringHelper.isTrue(properties.getString("alias.active", null));
	}

	public void setAliasActive(boolean aliasActive) {
		synchronized (properties) {
			properties.setProperty("alias.active", aliasActive);
			save();
		}
	}

	public void setMainContext(GlobalContext mainContext) {
		this.mainContext = mainContext;
		mainContext.addSubContext(this);
	}

	public GlobalContext getMainContextOrContext() {
		if (mainContext != null) {
			return mainContext;
		} else {
			return this;
		}
	}

	public GlobalContext getMainContext() {
		return mainContext;
	}

	public List<GlobalContext> getSubContexts() {
		if (subContexts == null) {
			return Collections.emptyList();
		} else {
			return subContexts;
		}
	}

	public synchronized void addSubContext(GlobalContext globalContext) {
		if (subContexts == null) {
			subContexts = new LinkedList<>();
		}
		subContexts.add(globalContext);
	}

	public List<Principal> getAllPrincipals() { // TODO: check this method, some
		// element of the list is null
		// ???
		synchronized (allUsers) {
			Collection<Map.Entry<String, WeakReference<User>>> entries = allUsers.entrySet();
			Collection<Map.Entry<String, WeakReference<User>>> entriesToDelete = new LinkedList<Map.Entry<String, WeakReference<User>>>();
			List<Principal> outPrincipals = new LinkedList<Principal>();
			for (Map.Entry<String, WeakReference<User>> entry : entries) {
				if (entry.getValue() == null) {
					entriesToDelete.add(entry);
				} else {
					if (entry.getValue().get() != null) {
						outPrincipals.add(entry.getValue().get());
					}
				}
			}
			for (Map.Entry<String, WeakReference<User>> entry : entriesToDelete) {
				allUsers.remove(entry.getKey());
			}
			return outPrincipals;
		}
	}

	public String getAllValue() {
		try {
			return FileUtils.readFileToString(properties.getFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	public Set<Object> getAttributesKeys() {
		return attributes.keySet();
	}

	public ICache getCache(String cacheName) {
		return getMapCache(cacheName);
	}

	public ICache getEternalCache(String cacheName) {
		ICache cache = eternalCacheMaps.get(cacheName);
		if (cache == null) {
			cache = new MapCache(new HashMap(), cacheName);
			eternalCacheMaps.put(cacheName, cache);
		}
		return cache;
	}

	public List<ICache> getAllCache() {
		List<ICache> outCaches = new LinkedList<ICache>();

		for (String cacheName : cacheMaps.keySet()) {
			outCaches.add(getCache(cacheName));
		}

		return outCaches;
	}

	public ICache getMapCache(String cacheName) {
		ICache cache = cacheMaps.get(cacheName);
		if (cache == null) {
			cache = new MapCache(new HashMap(), cacheName);
			cacheMaps.put(cacheName, cache);
		}
		return cache;
	}

	public List<String> getComponents() {
		List<String> components = new LinkedList<String>();
		String componentRaw = properties.getString("components", "");
		components.addAll(StringHelper.stringToCollection(componentRaw));
		components.remove(""); // remove empty string

		return components;
	}

	public boolean hasComponent(String className) {
		return getComponents().contains(className);
	}

	public boolean hasComponent(Class clazz) {
		return getComponents().contains(clazz.getCanonicalName());
	}

	public List<String> getUsersAccess() {
		String usersRaw = properties.getString("users", null);
		if (usersRaw == null) {
			return Collections.EMPTY_LIST;
		}
		List<String> users = new LinkedList<String>();
		users.addAll(StringHelper.stringToCollection(usersRaw));
		users.remove(""); // remove empty string
		return users;
	}

	public List<String> getModules() {
		List<String> modules = new LinkedList<String>();
		String modulesRaw = properties.getString("modules", null);
		if (modulesRaw == null) {
			return staticConfig.getBasicModules();
		}
		modules.addAll(StringHelper.stringToCollection(modulesRaw));
		modules.remove(""); // remove empty string

		return modules;
	}

	private Set<String> getLocalsContentLanguages() {
		if (contentLanguages != null) {
			return contentLanguages;
		}
		String lgRAW = properties.getString("content-languages", getRAWLanguages());
		if (lgRAW == null || lgRAW.trim().length() == 0) {
			return getLanguages();
		}
		Set<String> outLg = new LinkedHashSet<String>();
		outLg = new LinkedHashSet<String>();
		for (String lg : StringHelper.stringToArray(lgRAW, ";")) {
			lg = lg.replace(".", "");
			outLg.add(lg);
		}
		if (outLg.size() == 0) {
			return getLanguages();
		}
		contentLanguages = outLg;
		return outLg;
	}

	public Set<String> getContentLanguages() {
		return getLocalsContentLanguages();
	}

	public String getContextKey() {
		return contextKey;
	}

	public String getContextMainKey() {
		if (getMainContext() != null) {
			return getMainContext().getContextKey();
		} else {
			return getContextKey();
		}
	}

	public String getCountry() {
		return properties.getString("country", "");
	}

	public String getNoPopupDomainRAW() {
		return properties.getString("is-popup.exep-domain", "");
	}

	public void setNoPopupDomainRAW(String domains) {
		properties.setProperty("is-popup.exep-domain", domains);
		noPopupDomain = null;
		save();
	}

	public Set<String> getNoPopupDomain() {
		if (noPopupDomain == null) {
			String noPopUp = properties.getString("is-popup.exep-domain", "");
			String[] domains = noPopUp.toLowerCase().split(",");
			noPopupDomain = new HashSet<String>();
			for (String domain : domains) {
				noPopupDomain.add(domain);
			}
		}
		return noPopupDomain;
	}

	public int getCountUser() {
		return getAllPrincipals().size();
	}

	public Date getCreationDate() {
		try {
			if (properties.getString("creation-date", "").trim().length() == 0) {
				properties.setProperty("creation-date", StringHelper.renderTime(new Date()));
			}
			return StringHelper.parseTime(properties.getString("creation-date"));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getData(String key) {
		Properties prop = dataProperties;
		if (prop == null) {
			synchronized (lockDataFile) {
				prop = dataProperties;
				if (prop == null) {
					prop = initDataFile();
				}
			}
		}
		return prop.getProperty(key);
	}

	public String getData(String key, String defaultValue) {
		Properties prop = dataProperties;
		if (prop == null) {
			synchronized (lockDataFile) {
				prop = dataProperties;
				if (prop == null) {
					prop = initDataFile();
				}
			}
		}
		return prop.getProperty(key, defaultValue);
	}

	public void removeData(String key) {
		synchronized (lockDataFile) {
			if (dataProperties.containsKey(key)) {
				dataProperties.remove(key);
				askStoreData();
			}
		}
	}

	public void removeData(Collection<Object> keys) {
		synchronized (lockDataFile) {
			for (Object key : keys) {
				dataProperties.remove(key);
			}
			if (dataProperties.size() > 0) {
				askStoreData();
			}
		}
	}

	/**
	 * get data with specified prefix
	 * 
	 * @param prefix
	 * @return
	 */
	public Map<String, String> getDataWidthKeyPrefix(String prefix) {
		Map<String, String> outData = new HashMap<String, String>();
		for (Object key : getDataKeys()) {
			if (key.toString().startsWith(prefix)) {
				outData.put(key.toString(), getData(key.toString()));
			}
		}
		return outData;
	}

	public Collection<Object> getDataKeys() {
		Properties prop = dataProperties;
		if (prop == null) {
			prop = initDataFile();
		}
		return prop.keySet();
	}

	public void cleanDataAccess() {
		if (dataProperties == null) {
			initDataFile();
		}
		synchronized (lockDataFile) {
			List<Object> toDeleted = new LinkedList<Object>();
			SimpleDateFormat dateFormat = new SimpleDateFormat(ACCESS_DATE_FORMAT);
			Calendar refCal = Calendar.getInstance();
			refCal.roll(Calendar.YEAR, false);
			Calendar keyCal = Calendar.getInstance();
			for (Object key : dataProperties.keySet()) {
				String strKey = key.toString();
				if (strKey.length() > ACCESS_DATE_FORMAT.length() + 4) {
					String dateStr = strKey.substring(strKey.length() - ACCESS_DATE_FORMAT.length());
					try {
						Date date = dateFormat.parse(dateStr);
						keyCal.setTime(date);
						if (keyCal.before(refCal)) {
							toDeleted.add(key);
						}
					} catch (ParseException e) {
					}
				}
			}
			removeData(toDeleted);
		}
	}

	/**
	 * delete a group of data with the prefix of the key.
	 * 
	 * @param prefix
	 * @return true if one or more items was deleted.
	 */
	public synchronized boolean deletedDateFromKeyPrefix(String prefix) {
		if (dataProperties == null) {
			initDataFile();
		}
		boolean deleted = false;
		Collection<Object> keysToDelete = new LinkedHashSet<Object>();
		for (Object key : dataProperties.keySet()) {
			if (key.toString().startsWith(prefix)) {
				deleted = true;
				keysToDelete.add(key);
			}
		}
		for (Object object : keysToDelete) {
			dataProperties.remove(object);
		}
		if (deleted) {
			askStoreData();
		}
		return deleted;
	}

	/**
	 * rename keys in data map
	 * 
	 * @param oldPrefix
	 * @param newPrefix
	 * @return number of renamed keys
	 */
	public synchronized int renameKeys(String oldPrefix, String newPrefix) {
		int c = 0;
		for (Object key : getDataKeys()) {
			if (key.toString().startsWith(oldPrefix)) {
				String newKey = StringUtils.replaceOnce(key.toString(), oldPrefix, newPrefix);
				String value = getData(key.toString());
				dataProperties.put(newKey, value);
				dataProperties.remove(key);
				c++;
			}
		}
		return c;
	}

	private File getDataFile() throws IOException {
		File file = new File(ElementaryURLHelper.mergePath(getDataFolder(), "context_data.properties"));
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}

	private File getDataBackupFile() throws IOException {
		File file = new File(ElementaryURLHelper.mergePath(getDataFolder(), "context_data_backup.properties"));
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}

	public String getCredentialMainFolder() {
		return URLHelper.mergePath(getDataFolder(), ResourceHelper.PRIVATE_DIR, "credentials");
	}
	
	public String getCredentialPath(String project) {
		File out = new File(getCredentialMainFolder(), project+".json");
		if (out.exists()) {
			return out.getAbsolutePath();
		} else {
			return null;
		}
	}

	public String getDataFolder() {
		GlobalContext realGlobalContext = this;
		if (getMainContext() != null) {
			realGlobalContext = getMainContext();
		}
		if (realGlobalContext.dataFolder == null) {
			log(Log.INFO, Log.GROUP_INIT_CONTEXT, "init data folder with folder : " + getFolder(), false);
			realGlobalContext.dataFolder = staticConfig.getAllDataFolder();
			if (realGlobalContext.getFolder() != null) {
				realGlobalContext.dataFolder = ElementaryURLHelper.mergePath(realGlobalContext.dataFolder, getFolder());
			} else {
				GLOBAL_ERROR = "no folder found in " + contextFile;
			}
			try {
				File folderFile = new File(realGlobalContext.dataFolder);
				realGlobalContext.dataFolder = folderFile.getCanonicalPath();
				if (!folderFile.exists()) {
					/** search in archive folder **/
					File archiveFolder = new File(ElementaryURLHelper.mergePath(staticConfig.getAllDataArchiveFolder(), getFolder()));
					if (archiveFolder.exists()) {
						logger.info("import folder from archive : "+archiveFolder);
						folderFile.getParentFile().mkdirs();
						FileUtils.moveDirectory(archiveFolder, folderFile);
					} else {
						logger.info("archive not found : "+archiveFolder);
						folderFile.mkdirs();
					}
				}
			} catch (IOException e) {
				logger.warning(e.getMessage());
			}
			
			logger.info("init data folder with folder ["+realGlobalContext.getContextKey()+"] : " + realGlobalContext.dataFolder);
		}
		return realGlobalContext.dataFolder;
	}

	public String getBackupDirectory() {
		return URLHelper.mergePath(getDataFolder(), getStaticConfig().getBackupFolder());
	}

	public String getCalendarFolder() {
		return URLHelper.mergePath(getDataFolder(), "_calendar");
	}

	public String getSharedDataFolder(HttpSession session) throws IOException {
		if (sharedDataFolder == null) {
			sharedDataFolder = staticConfig.getLocalShareDataFolder();
			if (getFolder() != null) {
				sharedDataFolder = ElementaryURLHelper.mergePath(getMasterContext(session).getDataFolder(), sharedDataFolder);
			}
			try {
				File folderFile = new File(sharedDataFolder);
				sharedDataFolder = folderFile.getCanonicalPath();
				if (!folderFile.exists()) {
					folderFile.mkdirs();
				}
			} catch (IOException e) {
				logger.warning(e.getMessage());
			}
		}
		return sharedDataFolder;
	}

	public File getDataBaseFolder() {
		File dataBaseFolder = new File(getDataFolder(), "db");
		return dataBaseFolder;
	}

	public String getExternComponentFolder() {
		return URLHelper.mergePath(getDataFolder(), staticConfig.getExternComponentFolder());
	}

	public List<File> getExternComponents() {
		File dir = new File(getExternComponentFolder());
		List<File> outFiles = new LinkedList<File>();
		if (dir.listFiles() != null) {
			for (File child : dir.listFiles()) {
				if (StringHelper.getFileExtension(child.getName()).equalsIgnoreCase("jsp") || StringHelper.getFileExtension(child.getName()).equalsIgnoreCase("html")) {
					outFiles.add(child);
				}
			}
			Collections.sort(outFiles, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		return outFiles;
	}

	public String getStaticFolder() {
		return URLHelper.mergePath(getDataFolder(), staticConfig.getStaticFolder());
	}

	public String getDBDriver() {
		return properties.getString("db.driver", staticConfig.getDBDriver());
	}

	public String getDBLogin() {
		return getSpecialConfig().get("db.login", properties.getString("db.login", staticConfig.getDBLogin()));
	}

	public String getDBPassword() {
		return getSpecialConfig().get("db.password", properties.getString("db.password", staticConfig.getDBPassword()));
	}

	public String getDBResourceName() {
		return properties.getString("db.resource-name", staticConfig.getDBResourceName());
	}

	public String getDBURL() {
		return getSpecialConfig().get("db.url", properties.getString("db.url", staticConfig.getDBURL()));
	}

	public String getDefaultEncoding() {
		return properties.getString("encoding.default", "utf-16");
	}

	public String getDefaultImageFilter() {
		return properties.getString("image-filter.default", "standard");
	}

	public String getDefaultLanguage() {
		return getDefaultLanguages().iterator().next();
	}

	public Set<String> getDefaultLanguages() {
		if (defaultLanguages != null) {
			return defaultLanguages;
		}
		String lgRAW = getDefaultLanguagesRAW();
		if (lgRAW == null) {
			return Collections.emptySet();
		}
		Set<String> outLg = new LinkedHashSet<String>();
		for (String lg : StringHelper.stringToArray(lgRAW, ";")) {
			outLg.add(lg);
		}
		defaultLanguages = outLg;
		return outLg;
	}

	public String getDefaultLanguagesRAW() {
		if (getLanguages().size() == 0) {
			return "en";
		}
		return properties.getString("default.language", getLanguages().iterator().next());
	}

	public String getDefaultTemplate() {
		return properties.getString("template.default", null);
	}

	public URL getDMZServerInter() {
		String urlStr = properties.getString("dmz.url");
		if (urlStr != null && urlStr.trim().length() > 0) {
			try {
				return new URL(urlStr);
			} catch (MalformedURLException e) {
				logger.warning(e.getMessage());
			}
		}
		return null;
	}

	public URL getDMZServerIntra() {
		String urlStr = properties.getString("dmz-intra.url");
		if (urlStr != null && urlStr.trim().length() > 0) {
			try {
				return new URL(urlStr);
			} catch (MalformedURLException e) {
				logger.warning(e.getMessage());
			}
		}
		return getDMZServerInter();
	}

	public String getDefaultEditLanguage() {
		return "en";
	}

	public String getEditLanguage(HttpSession session) {
		String lg = properties.getString("edit.language", getDefaultEditLanguage());
		AdminUserFactory userFact = AdminUserFactory.createAdminUserFactory(this, session);
		User user = userFact.getCurrentUser(session);
		if (user != null && user.getUserInfo().getPreferredLanguage().length > 0) {
			EditContext editContext = EditContext.getInstance(this, session);
			for (String userLg : user.getUserInfo().getPreferredLanguage()) {
				if (editContext.getEditLanguages().contains(userLg)) {
					return userLg;
				}
			}
			return lg;
		}
		return lg;
	}

	public AdminUserFactory getAdminUserFactory(HttpSession session) throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (admimUserFactory == null) {
			Constructor<IUserFactory> construct = getAdminUserFactoryClass().getConstructor();
			admimUserFactory = (AdminUserFactory) construct.newInstance();
			admimUserFactory.init(this, session);
			admimUserFactory.adminUserInfoFile = getStaticConfig().getAdminUserInfoFile();
		}
		return admimUserFactory;
	}

	private Class<IUserFactory> getAdminUserFactoryClass() throws ClassNotFoundException {
		if (adminUserFactoryClass == null) {
			adminUserFactoryClassName = getAdminUserFactoryClassName();
			try {
				adminUserFactoryClass = (Class<IUserFactory>) Class.forName(adminUserFactoryClassName);
			} catch (Exception e) {
				logger.severe(e.getMessage());
				e.printStackTrace();
				adminUserFactoryClass = (Class<IUserFactory>) Class.forName(getDefaultAdminUserFactoryClassName());
			}
		}
		return adminUserFactoryClass;
	}

	public String getAdminUserFactoryClassName() {
		String userFactoryClass = properties.getString("adminuserfactory.class", getDefaultAdminUserFactoryClassName()).trim();
		return userFactoryClass;
	}

	public String getRemoteLoginUrl() {
		return properties.getString("remotelogin.url", "").trim();
	}

	public void setRemoteLoginUrl(String url) {
		properties.setProperty("remotelogin.url", url);
		save();
	}

	private String getDefaultAdminUserFactoryClassName() {
		return "org.javlo.user.AdminUserFactory";
	}

	public Set<String> getEncodings() {
		Set<String> outLg = new TreeSet<String>(Arrays.asList(StringHelper.stringToArray(getRAWEncodings(), ";")));
		return outLg;
	}

	public String getFistPassword() {
		String firstPassWord = properties.getString("admin.first-password", null);
		if (firstPassWord == null) {
			firstPassWord = "" + (1000 + Math.round(Math.random() * 8999));
			setFirstPassword(firstPassWord);
		}
		return firstPassWord;
	}

	public String getPlatformType() {
		return StringHelper.neverEmpty(properties.getString("platform.type", null), staticConfig.getPlatformType());
	}

	public void setPlatformType(String type) {
		properties.setProperty("platform.type", type);
		save();
	}

	public boolean isOpenPlatform() {
		return getPlatformType().equals(StaticConfig.OPEN_PLATFORM);
	}

	public boolean isPageTrash() {
		if (isOpenPlatform()) {
			return false;
		} else {
			return getStaticConfig().isPageTrash();
		}
	}

	public boolean isComponentsFiltered() {
		return properties.getBoolean("components.filtered", staticConfig.isComponentsFiltered());
	}

	public List<String> getCookiesTypes() {
		return StringHelper.stringToCollection(properties.getString("cookies.types", ""), ",");
	}

	public void setCookiesTypes(List<String> types) {
		properties.setProperty("cookies.types", StringHelper.collectionToString(types, ","));
		save();
	}

	public boolean isCookies() {
		return properties.getBoolean("security.cookies", false);
	}

	public void setCookies(boolean cook) {
		properties.setProperty("security.cookies", cook);
		save();
	}

	public String getCookiesPolicyUrl() {
		return properties.getString("security.cookies.url", null);
	}

	public void setCookiesPolicyUrl(String url) {
		properties.setProperty("security.cookies.url", url);
		save();
	}

	public void setComponentsFiltered(boolean filtered) {
		properties.setProperty("components.filtered", filtered);
		save();
	}

	public boolean isMailingPlatform() {
		return getPlatformType().equals(StaticConfig.MAILING_PLATFORM);
	}

	public boolean isWebPlatform() {
		return getPlatformType().equals(StaticConfig.WEB_PLATFORM);
	}

	/**
	 * return the data context folder name.
	 * 
	 * @return
	 */
	public String getFolder() {
		return properties.getString("folder");
	}

	public String getFullDateFormat() {
		return properties.getString("date.full", staticConfig.getDefaultDateFormat());
	}

	public String getGlobalTitle() {
		return properties.getString("global-title", "Javlo");
	}

	public String getOwnerName() {
		return properties.getString("owner.name", "");
	}
	
	public String getOwnerContact() {
		return properties.getString("owner.contact", "");
	}
	
	public void setOwnerContact(String contact) {
		properties.setProperty("owner.contact", contact);
	}

	public void setOwnerName(String name) {
		synchronized (properties) {
			properties.setProperty("owner.name", name);
			save();
		}
	}

	public String getOwnerFacebook() {
		return properties.getString("owner.facebook", "");
	}

	public void setOwnerFacebook(String name) {
		synchronized (properties) {
			properties.setProperty("owner.facebook", name);
			save();
		}
	}

	public String getOwnerTwitter() {
		return properties.getString("owner.twitter", "");
	}

	public void setOwnerTwitter(String name) {
		synchronized (properties) {
			properties.setProperty("owner.twitter", name);
			save();
		}
	}

	public String getOwnerLinkedin() {
		return properties.getString("owner.linkedin", "");
	}

	public void setOwnerLinkedin(String name) {
		synchronized (properties) {
			properties.setProperty("owner.linkedin", name);
			save();
		}
	}

	public String getOwnerInstagram() {
		return properties.getString("owner.instagram", "");
	}

	public void setOwnerInstagram(String name) {
		synchronized (properties) {
			properties.setProperty("owner.instagram", name);
			save();
		}
	}

	public String getOwnerAddress() {
		return properties.getString("owner.address", "");
	}

	public String getOwnerAddressHtml() {
		return XHTMLHelper.textToXHTML(properties.getString("owner.address", ""));
	}

	public void setOwnerAddress(String add) {
		synchronized (properties) {
			properties.setProperty("owner.address", add);
			save();
		}
	}
	
	public void setOwnerPostcode(String postcode) {
		synchronized (properties) {
			properties.setProperty("owner.postcode", postcode);
			save();
		}
	}
	
	public String getOwnerPostcode() {
		return properties.getString("owner.postcode", "");
	}
	
	public void setOwnerCity(String city) {
		synchronized (properties) {
			properties.setProperty("owner.city", city);
			save();
		}
	}
	
	public String getOwnerCity() {
		return properties.getString("owner.city", "");
	}

	public String getOwnerNumber() {
		return properties.getString("owner.number", "");
	}
	
	public void setOwnerNumber(String num) {
		synchronized (properties) {
			properties.setProperty("owner.number", num);
			save();
		}
	}

	public String getOwnerPhone() {
		return properties.getString("owner.phone", "");
	}

	public void setOwnerPhone(String phone) {
		synchronized (properties) {
			properties.setProperty("owner.phone", phone);
			save();
		}
	}

	public String getOwnerEmail() {
		return properties.getString("owner.email", "");
	}

	public void setOwnerEmail(String email) {
		synchronized (properties) {
			properties.setProperty("owner.email", email);
			save();
		}
	}

	public String getGoogleAnalyticsUACCT() {
		return properties.getString("google.uacct", "");
	}

	public String getHelpURL() {
		String url = properties.getString("help-url");
		if (StringHelper.isEmpty(url)) {
			return staticConfig.getHelpURL();
		} else {
			return url;
		}
	}

	public String getMainHelpURL() {
		return properties.getString("main-help-url", "");
	}

	public void setMainHelpURL(String helpURL) {
		properties.setProperty("main-help-url", helpURL);
	}

	/**
	 * return help url with specific information over the site.
	 * 
	 * @return
	 */
	public String getPrivateHelpURL() {
		return properties.getString("private-help-url", null);
	}

	public String getHomePage() {
		return properties.getString("homepage", "");
	}

	public String getHomePageLink(ContentContext ctx) throws Exception {
		if (!StringHelper.isEmpty(getHomePage())) {
			return URLHelper.replacePageReference(ctx, getHomePage());
		} else {
			return null;
		}
	}

	public String getHTMLDefinition(String key, String defaultValue) {
		String value = (String) properties.getProperty("html." + key);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	public List<String> getImageViewFilter() {
		String filters = properties.getString("image.view-filter", "preview;thumb-view");
		return Arrays.asList(filters.split(";"));
	}

	private Set<String> getLocalLanguages() {
		if (languages != null)  {
			return languages;
		}
		String lgRAW = properties.getString("languages", null);
		if (lgRAW == null) {
			return Collections.emptySet();
		}
		Set<String> outLg = new LinkedHashSet<String>();
		for (String lg : StringHelper.stringToArray(lgRAW, ";")) {
			lg = lg.replace(".", "");
			outLg.add(lg);
		}
		languages = outLg;
		return outLg;
	}

	public Set<String> getLanguages() {
		return getLocalLanguages();
	}

	public List<Locale> getLanguagesLocal() {
		List<Locale> out = new LinkedList<>();
		for (String lg : getLanguages()) {
			Locale locale = null;
			if (lg.length()==2) {
				locale = new Locale(lg);
			} else {
				if (lg.contains(""+ContentContext.COUNTRY_LG_SEP)) {
					String[] lgTab = lg.split(""+ContentContext.COUNTRY_LG_SEP);
					locale = new Locale(lgTab[0], lgTab[1]);
				}  else if (lg.contains(""+ContentContext.CONTENT_LG_SEP)) {
					locale = new Locale(lg.substring(1,3));
				}
			}
			out.add(locale);
		}
		return out;
	}
	
	public String getDefaultCurrency() {
		return getSpecialConfig().get("ecom.currency.default", "EUR");
	}
	
	public String getDefaultCurrencySymbol() {
		String currency = getDefaultCurrency();
		if (currency.equalsIgnoreCase("EUR")) {
			return "€";
		}
		if (currency.equalsIgnoreCase("USD")) {
			return "$";
		}
		if (currency.equalsIgnoreCase("JPY")) {
			return "¥";
		}
		return currency;
	}

	public Date getLatestLoginDate() {
		try {
			return StringHelper.parseTime(properties.getString("login-date"));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getLatestUser() {
		return properties.getString("latest-user");
	}

	public String getLicence() {
		return properties.getString("licence", "free");
	}

	public String getContentIntegrity() {
		return properties.getString("integrity", "");
	}

	public IntegrityBean getIntegrityDefinition() {
		return new IntegrityBean(getContentIntegrity());
	}

	public void setContentIntegrity(String value) {
		properties.setProperty("integrity", value);
		save();
	}

	public File getLogo() {
		File logo = new File(ElementaryURLHelper.mergePath(ElementaryURLHelper.mergePath(getDataFolder(), staticConfig.getStaticFolder()), getTemplateData().getLogo()));
		if (logo.exists()) {
			return logo;
		} else {
			return null;
		}
	}

	/*
	 * public String getDefaultLanguage() { String defaultLg = "en"; if
	 * (getLanguages().size() > 0) { defaultLg = getLanguages().iterator().next(); }
	 * return properties.getString("default.language", defaultLg); }
	 */

	public String getLook() {
		return properties.getString("look", "javlo");
	}

	public List<String> getMacros() {
		String macroRaw = properties.getString("macros", null);
		if (macroRaw == null) {
			return Collections.emptyList();
		}
		List<String> outMacro = new LinkedList<String>(Arrays.asList(StringHelper.stringToArray(macroRaw)));
		return outMacro;
	}

	public String getMailingReport() {
		return properties.getString("mailing.report", "");
	}

	public String getMailingSenders() {
		return properties.getString("mailing.senders", "");
	}

	public String getMailFrom() {
		String mailFrom = getSpecialConfig().getMailFrom();
		if (StringHelper.isMail(mailFrom)) {
			return mailFrom;
		}
		mailFrom = getMailingSenders();
		if (StringHelper.isMail(mailFrom)) {
			return mailFrom;
		}
		if (StringHelper.isMail(getStaticConfig().getDefaultSender())) {
			return getStaticConfig().getDefaultSender();
		}
		return getAdministratorEmail();
	}

	public Date getMailingStartTime() {
		String timeStr = properties.getString("mailing.start-time", "9:30:00");
		Date date = null;
		try {
			date = StringHelper.parseTimeOnly(timeStr);
		} catch (ParseException e) {
			logger.warning("bad time format : " + timeStr);
			try {
				date = StringHelper.parseTimeOnly("9:30:00");
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return date;
	}

	public String getMailingSubject() {
		return properties.getString("mailing.subject", "");
	}

	public String getMediumDateFormat() {
		return properties.getString("date.medium", staticConfig.getDefaultDateFormat());
	}

	static int DEBC = 0;

	public void loadNavigationUrls(MenuElement root) throws Exception {

		IURLFactory urlCreator = getURLFactory();
		Map<String, MenuElement> localViewPages = viewPages;
		if (urlFromFactoryImported != urlCreator) {
			synchronized (this.getLockLoadContent()) {
				if (urlFromFactoryImported != urlCreator) {

					localViewPages = new Hashtable<String, MenuElement>();
					ContentContext lgCtx = ContentContext.getFakeContentContext(this);
					Collection<String> mainLgs = getLanguages();
					Collection<String> contentLanguages = getContentLanguages();

					String exportVersion = "1.3";
					Set<String> lines = new LinkedHashSet<>();
					lines.add("V"+exportVersion+" - "+StringHelper.renderDateAndTime(LocalDateTime.now()));
					lines.add("### url creator : "+urlCreator.getClass());
					lines.add("### mainLgs : "+StringHelper.collectionToString(mainLgs,","));
					lines.add("### contentLanguages : "+StringHelper.collectionToString(contentLanguages,","));
					lines.add("");

					for (String mainLg : mainLgs) {
						for (String contentLg : contentLanguages) {
							lgCtx.setLanguage(mainLg);
							lgCtx.setContentLanguage(contentLg);
							lgCtx.setRequestContentLanguage(contentLg);
							lgCtx.setFormat(null);
							for (MenuElement me : root.getAllChildrenList()) {
								lgCtx.setCurrentPageCached(me);
								lgCtx.setPath(me.getPath());
								String pageURL = urlCreator.createURL(lgCtx, me);
								String pageKeyURL = urlCreator.createURLKey(pageURL);
								if (pageKeyURL.contains(".")) {
									pageKeyURL = pageKeyURL.substring(0, pageKeyURL.lastIndexOf("."));
								}
								localViewPages.put(pageKeyURL, me);
								String line = me.getName() + " ["+contentLg+"] [empty:"+me.isEmpty(lgCtx, null, false)+"] ["+me.getTitle(lgCtx)+"] > " + pageURL + " > " + pageKeyURL;
								lines.add(line);
							}
						}
					}

					File navigationFile = new File(URLHelper.mergePath(getDataFolder(), "navigation.txt"));
					try (BufferedWriter writer = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(navigationFile, false), StandardCharsets.UTF_8))) {
						for (String line : lines) {
							writer.write(line);
							writer.newLine();
						}
					} catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    logger.info("[site:"+getContextKey()+"] - url cache initialized with '" + urlCreator.getClass().getName() + "' url created : " + localViewPages.size() + " [lgs=" + contentLanguages + "]");
					log(Log.INFO, "url", "url cache initialized with '" + urlCreator.getClass().getName() + "' url created : " + localViewPages.size() + " [lgs=" + contentLanguages + "]");
					viewPages = localViewPages;
					urlFromFactoryImported = urlCreator;
				} else {
					localViewPages = viewPages;
				}
			}
		}
	}

	public MenuElement getPageIfExist(ContentContext ctx, String url, boolean useURLCreator) throws Exception {
		IURLFactory urlCreator = getURLFactory();
		Map<String, MenuElement> localViewPages = viewPages;
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE && urlCreator != null && useURLCreator) {
			/*if (urlFromFactoryImported != urlCreator) {
				synchronized (this.getLockLoadContent()) {
					if (urlFromFactoryImported != urlCreator) {
						localViewPages = new Hashtable<String, MenuElement>();
						ContentContext lgCtx = new ContentContext(ctx);
						Collection<String> mainLgs = getLanguages();
						Collection<String> contentLanguages = getContentLanguages();

						String exportVersion = "1.2";
						Set<String> lines = new LinkedHashSet<>();
						lines.add("V"+exportVersion+" - "+StringHelper.renderDateAndTime(LocalDateTime.now()));
						lines.add("### url creator : "+urlCreator.getClass());
						lines.add("### mainLgs : "+StringHelper.collectionToString(mainLgs,","));
						lines.add("### contentLanguages : "+StringHelper.collectionToString(contentLanguages,","));
						lines.add("");

						for (String mainLg : mainLgs) {
							for (String contentLg : contentLanguages) {
								lgCtx.setLanguage(mainLg);
								lgCtx.setContentLanguage(contentLg);
								lgCtx.setRequestContentLanguage(contentLg);
								lgCtx.setFormat(null);
								for (MenuElement me : ContentService.getInstance(ctx.getRequest()).getNavigation(lgCtx).getAllChildrenList()) {
									String pageURL = urlCreator.createURL(lgCtx, me);
									String pageKeyURL = urlCreator.createURLKey(pageURL);
									if (pageKeyURL.contains(".")) {
										pageKeyURL = pageKeyURL.substring(0, pageKeyURL.lastIndexOf("."));
									}
									localViewPages.put(pageKeyURL, me);
									String line = me.getName() + " ["+contentLg+"] [empty:"+me.isEmpty(lgCtx, null, false)+"] ["+me.getTitle(lgCtx)+"] > " + pageURL + " > " + pageKeyURL;
									lines.add(line);
								}
							}
						}

						File navigationFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), "navigation.txt"));
						try (BufferedWriter writer = new BufferedWriter(
								new OutputStreamWriter(new FileOutputStream(navigationFile, false), StandardCharsets.UTF_8))) {
							for (String line : lines) {
								writer.write(line);
								writer.newLine();
							}
						}

						logger.info("[site:"+ctx.getGlobalContext().getContextKey()+"] - url cache initialized with '" + urlCreator.getClass().getName() + "' url created : " + localViewPages.size() + " [lgs=" + contentLanguages + "]");
						log(Log.INFO, "url", "url cache initialized with '" + urlCreator.getClass().getName() + "' url created : " + localViewPages.size() + " [lgs=" + contentLanguages + "]");
						viewPages = localViewPages;
						urlFromFactoryImported = urlCreator;
					} else {
						localViewPages = viewPages;
					}
				}
			}*/
		} else {
			if (localViewPages == null) {
				logger.warning("create empty url cache.");
				localViewPages = new Hashtable<String, MenuElement>();
				viewPages = localViewPages;
			}
		}
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			String keyURL = url;
			if (urlCreator != null) {
				String urlRef = url;
				if (urlRef.length() > 1 && urlRef.endsWith("/")) {
					urlRef = url.substring(0, urlRef.length() - 1);
				}
				keyURL = urlCreator.createURLKey(urlRef);
			}

			MenuElement page = localViewPages.get(keyURL);
			if (page != null) {
				if (page == MenuElement.NOT_FOUND_PAGE) {
					logger.fine("page not found : "+keyURL +"  (uri:"+ctx.getRequest().getRequestURI()+")");
					log(Log.SEVERE, "url", "page not found keyURL='" + keyURL + "' url='" + url + "' #localViewPages=" + localViewPages.size());
					return null;
				} else {
					return page;
				}
			}
		}
		MenuElement root = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx);
		if (url.equals("/")) {
			return root;
		} else {
			Collection<MenuElement> pastNode = new LinkedList<MenuElement>();
			// System.out.println("##### GlobalContext.getPageIfExist :
			// "+DebugHelper.getCaller(10)); //TODO: remove debug trace
			MenuElement page = MenuElement.searchChild(root, ctx, url, pastNode);
			if (page != null && ctx.getRenderMode() == ContentContext.VIEW_MODE) {
				localViewPages.put(url, page);
			} else if (page == null && ctx.getRenderMode() == ContentContext.VIEW_MODE) {
				localViewPages.put(url, MenuElement.NOT_FOUND_PAGE);
			}

			return page;
		}
	}

	public String getPathPrefix() {
		return pathPrefix;
	}

	/**
	 * set a special path prefix needed by proxy.
	 * 
	 * @return
	 */
	public void setProxyPathPrefix(String prefix) {
		properties.setProperty("proxy-path-prefix", prefix);
		save();
	}

	public String getProxyPathPrefix() {
		return properties.getString("proxy-path-prefix", "");
	}

	public Date getPublishDate() throws ParseException {
		String dateStr = properties.getString("publish-date", null);
		if (dateStr == null) {
			return CREATION_DATE;
		}
		return StringHelper.parseTime(dateStr);
	}

	public String getPublishDateLabel() {
		try {
			return StringHelper.renderTime(getPublishDate());
		} catch (ParseException e) {
			e.printStackTrace();
			return "?";
		}
	}

	public int getRank() {
		long currentRank;
		int votes;
		synchronized (VOTES) {
			currentRank = properties.getLong("rank", 0);
			votes = properties.getInt("rank.votes", 0);
		}
		if (votes == 0) {
			return 0;
		} else {
			return Math.round((float) currentRank / (float) votes);
		}
	}

	public String getRAWContentLanguages() {
		return properties.getString("content-languages", getRAWLanguages());
	}

	public String getRAWEncodings() {
		return properties.getString("encodings", "utf-8;utf-16;unicode;iso-8859-1;iso-8859-2;iso-8859-3;iso-8859-4;iso-8859-5;iso-8859-6;iso-8859-7;iso-8859-8;iso-8859-9;iso-8859-10;iso-8859-11;iso-8859-12;iso-8859-13;iso-8859-14;iso-8859-15");
	}

	public String getRAWLanguages() {
		return properties.getString("languages", null);
	}

	public String getResourceId(String path) {
		return resourcePathToId.get(path);
	}

	public String getResourcePath(String id) {
		return resourceIdToPath.get(id);

	}

	public ServletContext getServletContext() {
		return application;
	}

	public String getShortDateFormat() {
		return properties.getString("date.short", staticConfig.getDefaultDateFormat());
	}

	public StaticConfig getStaticConfig() {
		return staticConfig;
	}

	public List<String> getTags() {
		synchronized (properties) {
			String tagsRaw = getRAWTags();
			if (tagsRaw == null || tagsRaw.trim().length() == 0) {
				return Collections.emptyList();
			}
			List<String> outTags = new LinkedList<String>(Arrays.asList(StringHelper.stringToArray(tagsRaw, ",")));
			return outTags;
		}
	}

	public String getRAWTags() {
		return properties.getString("tags", "");
	}

	public void setRAWTags(String tags) {
		properties.setProperty("tags", tags);
	}

	public TemplateData getTemplateData() {
		if (templateData != null) {
			return templateData;
		}
		String templateDataRAW = properties.getString("template-data", null);
		if (templateDataRAW == null) {
			templateData = TemplateData.EMPTY;
			return TemplateData.EMPTY;
		}
		templateData = new TemplateData(templateDataRAW);
		return templateData;
	}

	public List<String> getTemplatesNames() {
		List<String> templates = new LinkedList<String>();
		String templatesRaw = properties.getString("templates", "");
		templates.addAll(StringHelper.stringToCollection(templatesRaw));
		return templates;
	}

	/**
	 * time attribute, it's key,value stay in the map a specific time (default 5
	 * mintues)
	 * 
	 * @return unmodifiable map
	 */
	public Map<Object, Object> getTimeAttributes() {
		return Collections.unmodifiableMap(timeAttributes);
	}

	public Object getTimeAttribute(String key) {
		return timeAttributes.get(key);
	}

	public TimeTravelerContext getTimeTravelerContext() {
		return timeTravelerContext;
	}

	public Map<String, String> getURIAlias() {
		synchronized (properties) {
			if (uriAlias == null) {
				uriAlias = new HashMap<String, String>();
				Iterator<String> keys = properties.getKeys();
				while (keys.hasNext()) {
					String key = keys.next();
					if (key.startsWith(URI_ALIAS_KEY_PREFIX)) {
						String alias = key.substring(URI_ALIAS_KEY_PREFIX.length());
						String uri = properties.getString(key, "");
						uriAlias.put(alias, uri);
					}
				}
			}
		}
		return uriAlias;
	}

	public void resetURLFactory() {
		urlFactory = null;
	}

	public IURLFactory getURLFactory() {
		if (urlFactory != null) {
			if (urlFactory == NO_URL_FACTORY) {
				return null;
			} else {
				return urlFactory;
			}
		} else {
			String urlClass = properties.getString("url-factory", null);
			if (urlClass != null && urlClass.trim().length() > 0) {
				try {
					urlFactory = ((Class<IURLFactory>) Class.forName(urlClass)).newInstance();
				} catch (Exception e) {
					e.printStackTrace();
					urlFactory = NO_URL_FACTORY;
					return null;
				}
				return urlFactory;
			} else {
				urlFactory = NO_URL_FACTORY;
			}
		}
		return null;
	}

	public String getURLFactoryClass() {
		if (urlFactory == null || urlFactory == NO_URL_FACTORY) {
			return "";
		} else {
			return urlFactory.getClass().getName();
		}

	}

	public IUserFactory getUserFactory(HttpSession session) throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (userFactory == null) {
			Constructor<IUserFactory> construct = getUserFactoryClass().getConstructor();
			userFactory = construct.newInstance();
			userFactory.init(this, session);
		}
		return userFactory;
	}

	private Class<IUserFactory> getUserFactoryClass() throws ClassNotFoundException {
		if (userFactoryClass == null) {
			userFactoryClassName = getUserFactoryClassName();
			userFactoryClass = (Class<IUserFactory>) Class.forName(userFactoryClassName);
		}
		return userFactoryClass;
	}

	public String getUserFactoryClassName() {
		String userFactoryClass = properties.getString("userfactory.class", "org.javlo.user.UserFactory").trim();
		return userFactoryClass;
	}

	public Set<String> getUserRoles() {
		String roleRaw = properties.getString("roles", null);
		if (roleRaw == null || roleRaw.trim().length() == 0) {
			return Collections.emptySet();
		}
		Set<String> outRole = new HashSet<String>(StringHelper.stringToCollectionTrim(roleRaw));
		return outRole;
	}

	public Set<String> getVisibleContentLanguages() {
		String lgRAW = properties.getString("content-languages", getRAWLanguages());
		if (lgRAW == null) {
			return Collections.emptySet();
		}
		Set<String> outLg = new LinkedHashSet<String>();
		for (String lg : StringHelper.stringToArray(lgRAW, ";")) {
			if (!lg.startsWith(".")) {
				outLg.add(lg);
			}
		}
		return outLg;
	}

	public Set<String> getVisibleLanguages() {
		String lgRAW = properties.getString("languages", "fr;nl;en");
		if (lgRAW == null) {
			return Collections.emptySet();
		}
		Set<String> outLg = new LinkedHashSet<String>();
		for (String lg : StringHelper.stringToArray(lgRAW, ";")) {
			if (!lg.startsWith(".")) {
				outLg.add(lg);
			}
		}
		return outLg;
	}

	public synchronized Properties initDataFile() {
		Properties outProp = dataProperties;
		if (outProp == null) {
			outProp = new StructuredProperties();
		}
		InputStream in = null;
		try {
			in = new FileInputStream(getDataFile());
			outProp.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ResourceHelper.closeResource(in);
		}
		dataProperties = outProp;
		if (storePropertyThread == null) {
			startThread();
		}
		storePropertyThread.setDataProperties(dataProperties);

		new File(getCredentialMainFolder()).mkdirs();

		return outProp;
	}

	public boolean isAdminManagement() {
		return properties.getBoolean("admin-management", true);
	}

	public boolean isAutoSwitchToDefaultLanguage() {
		return properties.getBoolean("autoswitch-to-default-languages", false);
	}

	public boolean isChangeLicence() {
		return properties.getBoolean("change-licence", false);
	}

	public boolean isChangeMenu() {
		return properties.getBoolean("change-menu", true);
	}

	public boolean isCreation() {
		return creation;
	}

	public boolean isCSSInline() {
		return properties.getBoolean("css-inline", false);
	}

	public boolean isDownloadContent() {
		return properties.getBoolean("download-content", true);
	}

	public boolean isDynamic() {
		return properties.getBoolean("dynamic", false);
	}

	public boolean isEasy() {
		return properties.getBoolean("easy", false);
	}

	public boolean isWizz() {
		return properties.getBoolean("wizz", false);
	}

	public boolean isEditable() {
		return properties.getBoolean("editable", true);
	}

	public boolean isExtendMenu() {
		return properties.getBoolean("extend-menu", false);
	}

	public boolean isFirstImage() {
		return properties.getBoolean("first-image", false);
	}

	public boolean isHelpLink() {
		return properties.getBoolean("help-link", true);
	}

	public boolean isImagePreview() {
		return properties.getBoolean("image-preview", false);
	}

	public boolean isImportable() {
		return properties.getBoolean("importable", true);
	}

	public boolean isInstantMessaging() {
		return properties.getBoolean("instant-messaging", false);
	}

	public boolean isLightMenu() {
		return properties.getBoolean("light-menu", false);
	}

	public boolean isMailing() {
		return properties.getBoolean("mailing", false);
	}

	public boolean isNewPageVisible() {
		return properties.getBoolean("new-page.visible", true);
	}

	public boolean isOpenExternalLinkAsPopup() {
		return properties.getBoolean("is-popup", false);
	}

	/**
	 * if return true only the creator of the component can modify it.
	 * 
	 * @return
	 */
	public boolean isOnlyCreatorModify() {
		return properties.getBoolean("is-only-creator-modify", false);
	}

	/**
	 * display the creator of component in the page.
	 * 
	 * @return
	 */
	public boolean isCollaborativeMode() {
		return properties.getBoolean("is-collaborative-mode", false);
	}

	public boolean isOpenExternalLinkAsPopup(String url) {
		if (!isOpenExternalLinkAsPopup()) {
			return false;
		}
		String domain = URLHelper.extractHost(url).toLowerCase();
		if (getNoPopupDomain().contains(domain)) {
			return false;
		}
		return true;
	}

	public boolean isOpenFileAsPopup() {
		return properties.getBoolean("is-popup-file", false);
	}

	public boolean isPageStructure() {
		return properties.getBoolean("page-structure", false);
	}

	public boolean isPortail() {
		return properties.getBoolean("portail", false);
	}

	public boolean isPrivatePage() {
		return properties.getBoolean("private-page", true);
	}

	public boolean isRightOnPage() {
		return properties.getBoolean("right-on-page", false);
	}

	public boolean isSpacialAccessCode(Code code) {
		synchronized (specialAccessCode) {
			WeakReference<Code> ref = specialAccessCode.get(code);
			if (ref != null && ref.get() != null) {
				return true;
			} else {
				specialAccessCode.remove(code);
				return false;
			}
		}
	}

	public boolean isTags() {
		return getTags().size() > 0;
	}

	public boolean isTemplateFilter() {
		return properties.getBoolean("template-filter", true);
	}

	public boolean isURIAlias() {
		return properties.getBoolean("is-uri-alias.use", true);
	}

	public boolean isUserManagement() {
		return properties.getBoolean("user-management", true);
	}

	boolean isValid() {
		return getFolder() != null && getFolder().trim().length() > 0;
	}

	public boolean isView() {
		return properties.getBoolean("view", true);
	}

	public boolean isViewBar() {
		return properties.getBoolean("view-bar", true);
	}

	public boolean isVirtualPaternity() {
		return properties.getBoolean("virtal-paternity", false);
	}

	public boolean isVisible() {
		return properties.getBoolean("visible", true);
	}

	public void logout(Principal principal) {
		if (principal != null) {
			synchronized (allUsers) {
				allUsers.remove(principal.getName());
			}
		}
	}

	protected Collection<String> getAllCacheName() {
		return cacheMaps.keySet();
	}

	public void releaseAllCache() {
		cleanDataAccess();
		synchronized (RELEASE_CACHE) {
			for (String name : getAllCacheName()) {
				logger.info("reset cache : " + name);
				getCache(name).removeAll();
			}
			cacheMaps.clear();
			quietArea = null;
			viewPages = null;
			urlFromFactoryImported = NO_URL_FACTORY;
			try {
				ReverseLinkService.getInstance(this).clearCache();
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			dataFolder = null;
			AppendableTextFile log = specialLogFile;
			specialLogFile = null;
			if (log != null) {
				try {
					log.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void reload() {
		try {
			synchronized (LOCK_GLOBAL_CONTEXT_LOAD) {
				properties.clear();
				properties.load(contextFile);
				config = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadExternalProperties(String raw) throws IOException {
		properties.clear();
		properties.load(new StringReader(raw));
		save();
		reload();
	}

	public void removeTag(String tag) {
		synchronized (properties) {
			List<String> tags = new ArrayList<String>(getTags());
			tags.remove(tag);
			properties.setProperty("tags", StringHelper.collectionToString(tags));
			save();
		}
	}

	public synchronized void renameDataKey(String oldKeyPrefix, String newKeyPrefix) {
		Collection<Object> keys = dataProperties.keySet();
		for (Object keyObj : keys) {
			String key = (String) keyObj;
			if (key.startsWith(oldKeyPrefix)) {
				String newKey = StringUtils.replaceOnce(key, oldKeyPrefix, newKeyPrefix);
				dataProperties.setProperty(newKey, dataProperties.getProperty(key));
				dataProperties.remove(key);
			}
		}
	}

	public void resetAdmimUserFactory() {
		admimUserFactory = null;
	}

	public void resetUserFactory() {
		userFactory = null;
	}

	public void save() {
		TransactionFile tf = null;
		synchronized (properties) {
			if (getFolder() != null && getFolder().trim().length() > 0) {
				try {
					tf = new TransactionFile(contextFile);
					properties.save(tf.getOutputStream());
					tf.commit();
				} catch (IOException e) {
					e.printStackTrace();
					try {
						tf.rollback();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

			} else {
				logger.severe("no folder found for : " + getContextKey() + " context not stored, try to reload.");
				reload();
			}
		}

	}

	public void sendMailToAdministrator(String subjet, String body) throws MessagingException {
		MailService mailService = MailService.getInstance(new MailConfig(null, staticConfig, null));
		mailService.sendMail(new InternetAddress(getAdministratorEmail()), new InternetAddress(getAdministratorEmail()), subjet, body, false);
	}

	public void setAdministrator(String admin) {
		synchronized (properties) {
			properties.setProperty("admin", admin);
			save();
		}
	}

	public void setAdminManagement(boolean adminManagement) {
		synchronized (properties) {
			properties.setProperty("admin-management", adminManagement);
			save();
		}
	}

	public void setAdminUserRoles(Set<String> roles) {
		synchronized (properties) {
			String[] rolesArray = new String[roles.size()];
			roles.toArray(rolesArray);
			properties.setProperty("admin-roles", StringHelper.arrayToString(rolesArray));
			save();
		}
	}

	public void setAliasOf(String alias) {
		synchronized (properties) {
			properties.setProperty("alias", alias);
			save();
		}
	}

	public void addAliasUri(String uri, String alias) {
		synchronized (properties) {
			Properties newAlias = new Properties();
			newAlias.putAll(getURIAlias());
			newAlias.put(uri, alias);
			setAliasURI(newAlias);
		}
	}

	public void setAliasURI(Properties aliasURI) {

		synchronized (properties) {
			Iterator<String> keys = properties.getKeys();
			List<String> toBeDeleted = new LinkedList<String>();
			while (keys.hasNext()) {
				String key = keys.next();
				if (key.startsWith(URI_ALIAS_KEY_PREFIX)) {
					toBeDeleted.add(key);
				}
			}
			for (String key : toBeDeleted) {
				properties.clearProperty(key);
			}

			Collection<Map.Entry<Object, Object>> entries = aliasURI.entrySet();
			for (Map.Entry<Object, Object> entry : entries) {
				String key = URI_ALIAS_KEY_PREFIX + entry.getKey();
				if (properties.getProperty(key) == null) {
					properties.setProperty(key, entry.getValue());
				}
			}
			save();
		}
		uriAlias = null;
	}

	/**
	 * set a properties file content for change all values
	 * 
	 * @param content
	 */
	public void setAllValues(String content) {
		File file = properties.getFile();
		try {
			FileUtils.writeStringToFile(file, content);
		} catch (IOException e) {
			e.printStackTrace();
		}
		reload();
	}

	public void setAttribute(String key, Object att) {
		if (att == null) {
			attributes.remove(key);
		} else {
			attributes.put(key, att);
		}
	}

	public void setAutoSwitchToDefaultLanguage(boolean value) {
		properties.setProperty("autoswitch-to-default-languages", value);
		save();
	}

	public void setAutoSwitchToFirstLanguage(boolean value) {
		properties.setProperty("autoswitch-to-first-languages", value);
		save();
	}

	public void setChangeLicence(boolean changeLicence) {
		synchronized (properties) {
			properties.setProperty("change-licence", changeLicence);
			save();
		}
	}

	public void setChangeMenu(boolean changeMenu) {
		synchronized (properties) {
			properties.setProperty("change-menu", changeMenu);
			save();
		}
	}

	public void setComponents(List<String> components) {
		synchronized (properties) {
			properties.setProperty("components", StringHelper.collectionToString(components));
			save();
		}
	}

	public void setModules(List<String> modules) {
		synchronized (properties) {
			properties.setProperty("modules", StringHelper.collectionToString(modules));
			save();
		}
	}

	public void setUsersAccess(List<String> users) {
		synchronized (properties) {
			properties.setProperty("users", StringHelper.collectionToString(users));
			save();
		}
	}

	public void setContextKey(String contextKey) {
		this.contextKey = contextKey;
	}

	public void setCountry(String country) {
		properties.setProperty("country", country);
		save();
	}

	public void setCSSInline(boolean cssInline) {
		synchronized (properties) {
			properties.setProperty("css-inline", cssInline);
			save();
		}
	}

	public synchronized void setData(String key, String value) {
		if (key == null) {
			return;
		}
		Properties prop = dataProperties;
		if (prop == null) {
			prop = initDataFile();
		}
		synchronized (prop) {
			if (value != null) {
				prop.put(key, value);
			} else {
				prop.remove(key);
			}
			askStoreData();
		}
	}

	private void askStoreData() {
		needStoreData.setValue(true);
	}

	/*
	 * private void saveData() { try { saveData(dataProperties, lockDataFile,
	 * getContextKey(), getDataFile()); } catch (IOException e) {
	 * e.printStackTrace(); } }
	 */

	private static void saveData(Properties dataProperties, Object lockDataFile, String contextKey, File dataFile) {
		if (dataProperties != null) {
			synchronized (lockDataFile) {
				TransactionFile tf = null;
				try {
					long startTime = System.currentTimeMillis();

					logger.finest("start storage data of context " + contextKey);

					tf = new TransactionFile(dataFile);
					dataProperties.store(tf.getOutputStream(), contextKey);
					tf.commit();

					logger.fine("store data for : " + contextKey + " size:" + dataProperties.size() + " time:" + StringHelper.renderTimeInSecond(System.currentTimeMillis() - startTime));
				} catch (Exception e) {
					try {
						if (tf != null) {
							tf.rollback();
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
		}
	}

	public void setDefaultEncoding(String encoding) {
		synchronized (properties) {
			properties.setProperty("encoding.default", encoding);
			save();
		}
	}

	public void setDefaultLanguages(String lg) {
		synchronized (properties) {
			properties.setProperty("default.language", lg);
			save();
		}
		resetCache();
	}

	public void setDefaultTemplate(String template) {
		synchronized (properties) {
			properties.setProperty("template.default", template);
			save();
		}
	}

	public void setDMZServerInter(String dmzServer) {
		properties.setProperty("dmz.url", dmzServer);
		save();
	}

	public void setDMZServerIntra(String dmzServer) {
		properties.setProperty("dmz-intra.url", dmzServer);
		save();
	}

	public void setDownloadContent(boolean downloadContent) {
		synchronized (properties) {
			properties.setProperty("download-content", downloadContent);
			save();
		}
	}

	public void setDynamic(boolean dynamic) {
		synchronized (properties) {
			properties.setProperty("dynamic", dynamic);
			save();
		}
	}

	public void setEasy(boolean easy) {
		properties.setProperty("easy", easy);
		save();
	}

	public void setWizz(boolean wizz) {
		properties.setProperty("wizz", wizz);
		save();
	}

	public void setEditable(boolean editable) {
		synchronized (properties) {
			properties.setProperty("editable", editable);
			save();
		}
	}

	public void setEditLanguage(String language) {
		synchronized (properties) {
			properties.setProperty("edit.language", language);
			save();
		}
	}

	public void setAdminUserFactoryClassName(String userFactoryName) {
		synchronized (properties) {
			properties.setProperty("adminuserfactory.class", userFactoryName);
			admimUserFactory = null;
			adminUserFactoryClass = null;
			adminUserFactoryClassName = null;
			save();
		}
	}

	public void setExtendMenu(boolean extendMenu) {
		synchronized (properties) {
			properties.setProperty("extend-menu", extendMenu);
			save();
		}
	}

	public void setFirstPassword(String password) {
		synchronized (properties) {
			properties.setProperty("admin.first-password", password);
			save();
		}
	}

	public void setFolder(String folder) {
		properties.setProperty("folder", folder);
		dataFolder = null;
		save();
	}

	public void setFullDateFormat(String format) {
		properties.setProperty("date.full", format);
		save();
	}

	public void setGlobalTitle(String password) {
		synchronized (properties) {
			properties.setProperty("global-title", password);
			save();
		}
	}

	public void setGoogleAnalyticsUACCT(String uacct) {
		synchronized (properties) {
			properties.setProperty("google.uacct", uacct);
			save();
		}
	}

	public void setGoogleApiKey(String key) {
		synchronized (properties) {
			properties.setProperty("google.api.key", key);
			save();
		}
	}

	public void setHelpLink(boolean helpLink) {
		synchronized (properties) {
			properties.setProperty("help-link", helpLink);
			save();
		}
	}

	public void setHelpURL(String helpURL) {
		synchronized (properties) {
			properties.setProperty("help-url", helpURL);
			save();
		}
	}

	public void setPrivateHelpURL(String helpURL) {
		synchronized (properties) {
			properties.setProperty("private-help-url", helpURL);
			save();
		}
	}

	public void setHomePage(String homepage) {
		properties.setProperty("homepage", homepage);
		save();
	}

	public void setImagePreview(boolean imagePreview) {
		synchronized (properties) {
			properties.setProperty("image-preview", imagePreview);
			save();
		}
	}

	public void setImageViewFilter(String filters) {
		synchronized (properties) {
			properties.setProperty("image.view-filter", filters);
			save();
		}
	}

	public void setImportable(boolean importable) {
		synchronized (properties) {
			properties.setProperty("importable", importable);
			save();
		}
	}

	public void setInstantMessaging(boolean im) {
		synchronized (properties) {
			properties.setProperty("instant-messaging", im);
			save();
		}
	}

	public void setLicence(String licence) {
		if (licence != null) {
			if (licence.equals("free")) {
				setEasy(true);
				setAdminManagement(false);
				setUserManagement(false);
				setPrivatePage(false);
				setMailing(false);
				setChangeMenu(false);
				setPortail(false);
				setViewBar(true);
			} else if (licence.equals("free+")) {
				setEasy(false);
				setAdminManagement(false);
				setUserManagement(false);
				setPrivatePage(false);
				setMailing(false);
				setChangeMenu(false);
				setPortail(false);
				setViewBar(true);
			}
			properties.setProperty("licence", licence);
		}
	}

	public void setLightMenu(boolean lightMenu) {
		properties.setProperty("light-menu", lightMenu);
	}

	public void setLook(String look) {
		properties.setProperty("look", look);
		save();
	}

	public void setMacros(List<String> macros) {
		synchronized (properties) {
			properties.setProperty("macros", StringHelper.collectionToString(macros));
			save();
		}
	}

	public void setMailing(boolean changeMenu) {
		synchronized (properties) {
			properties.setProperty("mailing", changeMenu);
			save();
		}
	}

	public void setMailingReport(String report) {
		synchronized (properties) {
			properties.setProperty("mailing.report", report);
			save();
		}
	}

	public void setMailingSenders(String sender) {
		synchronized (properties) {
			properties.setProperty("mailing.senders", sender);
			save();
		}
	}

	public void setMailingSubject(String subject) {
		synchronized (properties) {
			properties.setProperty("mailing.subject", subject);
			save();
		}
	}

	public void setMailingTemplates(List<String> components) {
		synchronized (properties) {
			properties.setProperty("mailing-templates", StringHelper.collectionToString(components));
			save();
		}
	}

	public void setMediumDateFormat(String format) {
		properties.setProperty("date.medium", format);
		save();
	}

	public void setNewPageVisible(boolean visible) {
		synchronized (properties) {
			properties.setProperty("new-page.visible", visible);
			save();
		}
	}

	public void setOneVote(int value) {
		synchronized (properties) {
			long currentRank = properties.getLong("rank", 0);
			int votes = properties.getInt("rank.votes", 0);
			currentRank = currentRank + value;
			votes = votes + 1;
			properties.setProperty("rank", currentRank);
			properties.setProperty("rank.votes", votes);
		}
		save();
	}

	public void setOpenExernalLinkAsPopup(boolean ipPopup) {
		properties.setProperty("is-popup", ipPopup);
		save();
	}

	public void setOnlyCreatorModify(boolean creatorModif) {
		properties.setProperty("is-only-creator-modify", creatorModif);
		save();
	}

	public void setCollaborativeMode(boolean display) {
		properties.setProperty("is-collaborative-mode", display);
		save();
	}

	public void setOpenFileAsPopup(boolean ipPopup) {
		properties.setProperty("is-popup-file", ipPopup);
		save();
	}

	public void setPageStructure(boolean pageStructure) {
		synchronized (properties) {
			properties.setProperty("page-structure", pageStructure);
			save();
		}
	}

	public void setPassword(String password) {
		synchronized (properties) {
			if (password != null) {
				properties.setProperty("admin.password", StringHelper.encryptPassword(password));
			}
			save();
		}
	}

	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}

	public void setPortail(boolean changeMenu) {
		synchronized (properties) {
			properties.setProperty("portail", changeMenu);
			save();
		}
	}

	public void setPrivatePage(boolean privatePage) {
		synchronized (properties) {
			properties.setProperty("private-page", privatePage);
			save();
		}
	}

	public void setPublishDate(Date publishDate) {
		synchronized (properties) {
			properties.setProperty("publish-date", StringHelper.renderTime(publishDate));
			save();
		}
	}

	public void setLatestPublisher(String userName) {
		synchronized (properties) {
			properties.setProperty("publish-user", userName);
			save();
		}
	}

	public String getLatestPublisher() {
		synchronized (properties) {
			return properties.getString("publish-user", "?");
		}
	}

	public final void resetCache() {
		languages = null;
		defaultLanguages = null;
		contentLanguages = null;
		proxyMappings = null;
	}

	public void setRAWContentLanguages(String languages) {
		properties.setProperty("content-languages", languages);
		resetCache();
	}

	public void setRAWEncodings(String encodings) {
		properties.setProperty("encodings", encodings);
	}

	public void setRAWLanguages(String languages) {
		properties.setProperty("languages", languages);
		resetCache();
	}

	public void setRightOnPage(boolean rightOnPage) {
		synchronized (properties) {
			properties.setProperty("right-on-page", rightOnPage);
			save();
		}
	}

	public void setShortDateFormat(String format) {
		properties.setProperty("date.short", format);
		save();
	}

	public void setTags(boolean inTags) {
		properties.setProperty("is-tags", inTags);
		save();
	}

	public void setTemplateData(TemplateData inTemplateData) {
		templateData = inTemplateData;
		properties.setProperty("template-data", templateData.toString());
		save();
	}

	public void setTemplateFilter(boolean templateFilter) {
		synchronized (properties) {
			properties.setProperty("template-filter", templateFilter);
			save();
		}
	}

	public void setTemplatesNames(List<String> components) {
		synchronized (properties) {
			properties.setProperty("templates", StringHelper.collectionToString(components));
			save();
		}
	}

	public void setTimeAttribute(String key, Object att) {
		timeAttributes.put(key, att);
	}

	public void setTimeAttribute(String key, Object att, int timeInSecond) {
		timeAttributes.put(key, att, timeInSecond);
	}

	public void setTimeTravelerContext(TimeTravelerContext timeContext) {
		timeTravelerContext = timeContext;
	}

	public void setURLFactory(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		properties.setProperty("url-factory", className);
		if (className != null && className.trim().length() > 0) {
			urlFactory = (IURLFactory) (Class.forName(className).newInstance());
		} else {
			urlFactory = null;
		}
	}

	public void setUserFactoryClassName(String userFactoryName) {
		synchronized (properties) {
			properties.setProperty("userfactory.class", userFactoryName);
			userFactory = null;
			userFactoryClass = null;
			userFactoryClassName = null;
			save();
		}
	}

	public void setUserManagement(boolean userManagement) {
		synchronized (properties) {
			properties.setProperty("user-management", userManagement);
			save();
		}
	}

	public void setUserRoles(Set<String> roles) {
		synchronized (properties) {
			List<String> rolesList = StringHelper.trimList(roles);
			properties.setProperty("roles", StringHelper.collectionToString(rolesList).toLowerCase());
			save();
		}
	}

	public void setView(boolean view) {
		synchronized (properties) {
			properties.setProperty("view", view);
			save();
		}
	}

	public void setViewBar(boolean viewBar) {
		synchronized (properties) {
			properties.setProperty("view-bar", viewBar);
			save();
		}
	}

	public void setVirtualPaternity(boolean changeLicence) {
		synchronized (properties) {
			properties.setProperty("virtal-paternity", changeLicence);
			save();
		}
	}

	public void setVisible(boolean visible) {
		synchronized (properties) {
			properties.setProperty("visible", visible);
			save();
		}
	}

	public List<String> getTemplatePlugin() {
		String tp = properties.getString("template.plugins", null);
		if (tp == null || tp.trim().length() == 0) {
			return Collections.EMPTY_LIST;
		} else {
			return Arrays.asList(tp.split(";"));
		}
	}

	public String getTemplatePluginConfig() {
		return properties.getString("template.plugins.config", null);
	}

	public void setTemplatePlugin(Collection<String> top) {
		synchronized (properties) {
			properties.setProperty("template.plugins", StringHelper.collectionToString(top, ";"));
			save();
		}
	}

	public void setTemplatePluginConfig(String config) {
		synchronized (properties) {
			properties.setProperty("template.plugins.config", config);
			save();
		}
	}

	public String getBlockPassword() {
		return properties.getString("security.block-password", null);
	}

	public String getBlockPreviewPassword() {
		return properties.getString("security.block-preview-password", null);
	}

	public String getForcedHost() {
		return properties.getString("url.forced-host", "");
	}

	public void setForcedHost(String host) {
		synchronized (properties) {
			properties.setProperty("url.forced-host", host);
			save();
		}
	}

	public void setBlockPassword(String pwd) {
		synchronized (properties) {
			properties.setProperty("security.block-password", pwd);
			save();
		}
	}

	/**
	 * if true, there are two version of content : preview and view.
	 * 
	 * @return
	 */
	public boolean isPreviewMode() {
		return properties.getBoolean("mode.preview", true);
	}

	public boolean isMaster() {
		return staticConfig.getMasterContext().equals(getContextKey());
	}

	public void setPreviewMode(boolean preview) {
		properties.setProperty("mode.preview", preview);
		save();
	}

	public static final class FrontCacheBean {
		private long creationTime = -1;
		private String data = null;
		private String renderer = null;

		public FrontCacheBean(String data, String renderer) {
			this.creationTime = System.currentTimeMillis();
			this.data = data;
			this.setRenderer(renderer);
		}

		public long getCreationTime() {
			return creationTime;
		}

		public void setCreationTime(long creationTime) {
			this.creationTime = creationTime;
		}

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}

		@Override
		public String toString() {
			return data;
		}

		public String getRenderer() {
			return renderer;
		}

		public void setRenderer(String renderer) {
			this.renderer = renderer;
		}
	}

	public Object getSessionAttribute(HttpSession session, String key) {
		String sessionKey = getContextKey() + "___" + key;
		return session.getAttribute(sessionKey);
	}

	public void setSessionAttribute(HttpSession session, String key, Object value) {
		String sessionKey = getContextKey() + "___" + key;
		session.setAttribute(sessionKey, value);
	}

	public void writeInstanceInfo(ContentContext ctx, PrintStream out) throws Exception {
		out.println("****************************************************************");
		out.println("****************************************************************");
		out.println("****");
		out.println("**** INSTANCE INFO ");
		out.println("****");
		out.println("**** ContextKey         :  " + getContextKey());
		out.println("**** Alias of           :  " + getAliasOf());
		out.println("**** Data folder        :  " + getDataFolder());

		out.println("****");

		ContentService content = ContentService.getInstance(ctx.getRequest());
		ContentContext localContext = new ContentContext(ctx);
		localContext.setRenderMode(ContentContext.VIEW_MODE);
		MenuElement root = content.getNavigation(ctx);
		out.println("**** #MenuElement View    :  " + (root.getAllChildrenList().size()));
		out.println("**** #Comp bean View      :  " + (ContentHelper.getAllComponentsOfChildren(root).size()));
		out.println("**** #Comp icv View       :  " + content.getAllContent(localContext).size());
		localContext.setRenderMode(ContentContext.PREVIEW_MODE);
		root = content.getNavigation(ctx);
		out.println("**** #MenuElement Preview :  " + (root.getAllChildrenList().size()));
		out.println("**** #Comp bean Preview   :  " + (ContentHelper.getAllComponentsOfChildren(root).size()));
		out.println("**** #Comp icv Preview    :  " + content.getAllContent(localContext).size());
		out.println("****");
		printInfo(ctx, out);
		content.printInfo(ctx, out);
	}

	public void writeInfo(HttpSession session, PrintStream out) {
		out.println("****************************************************************");
		out.println("****************************************************************");
		out.println("****");
		out.println("**** GLOBAL CONTEXT INFO ");
		out.println("****");
		out.println("**** ContextKey         :  " + getContextKey());
		out.println("**** Alias of           :  " + getAliasOf());
		out.println("**** User Factory       :  " + getUserFactoryClassName());
		out.println("**** Admin User Factory :  " + getAdminUserFactoryClassName());
		out.println("**** Language           :  " + getLanguages());
		out.println("**** ContentLanguage    :  " + getContentLanguages());
		out.println("**** Data size          :  " + getDataKeys().size());
		out.println("**** Data folder        :  " + getDataFolder());
		if (viewPages != null) {
			out.println("**** # viewPages        :  " + viewPages.size());
		}
		// if (session != null) {
		// try {
		// out.println("**** Modules : " + StringHelper
		// .collectionToString(ModulesContext.getInstance(session,
		// this).getAllModules(), ", "));
		// } catch (ModuleException e) {
		// out.println("**** Error load Modules : " + e.getMessage());
		// e.printStackTrace();
		// }
		// }
		out.println("**** # attributes       :  " + attributes.size());
		out.println("**** # time attributes  :  " + timeAttributes.size());
		if (viewPages != null) {
			out.println("**** # cached pages     :  " + viewPages.size());
		} else {
			out.println("**** # cached pages     :  not found.");
		}
		out.println("****");
		out.println("****************************************************************");
		out.println("****************************************************************");
		out.println("****");
		out.println("**** CACHE INFO ");
		out.println("****");

		if (ImageTransformServlet.COUNT_ACCESS > 0) {
			out.println("**** Resources 304       : " + ImageTransformServlet.COUNT_304 + " on " + ImageTransformServlet.COUNT_ACCESS + " Access (" + Math.round(ImageTransformServlet.COUNT_304 * 100 / ImageTransformServlet.COUNT_ACCESS) + "%).");
		}
		if (AccessServlet.COUNT_ACCESS > 0) {
			out.println("**** Content 304         : " + AccessServlet.COUNT_304 + " on " + AccessServlet.COUNT_ACCESS + " Access (" + Math.round(AccessServlet.COUNT_304 * 100 / AccessServlet.COUNT_ACCESS) + "%).");
		}

	}

	public String createOneTimeToken(String token) {
		String newToken = StringHelper.getRandomIdBase64();
		oneTimeTokens.put(newToken, token);
		return newToken;
	}

	public String getOneTimeToken(String token) {
		String oneTimeToken = oneTimeTokens.get(token);
		if (oneTimeToken == null) {
			oneTimeToken = createOneTimeToken(token);
		}
		return oneTimeToken;
	}

	private File getTokenPageFile() {
		return new File(URLHelper.mergePath(getDataFolder(), ResourceHelper.PRIVATE_DIR, "token_page.properties"));
	}

	public String getLatestTokenForPage(String pageName) {
		String token = pageTimeToken.get(pageName);
		if (token == null) {
			token = StringHelper.getRandomIdBase64();
			pageTimeToken.put(token, pageName);
			pageTimeToken.put(pageName, token);
			try {
				pageTimeToken.store(getTokenPageFile());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return token;
	}

	public String getNewTokenForPage(String pageName) {
		String token = StringHelper.getRandomIdBase64();
		pageTimeToken.put(token, pageName);
		pageTimeToken.put(pageName, token);
		try {
			pageTimeToken.store(getTokenPageFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return token;
	}

	public String getPageToken(String token) {
		return pageTimeToken.get(token);
	}

	public String convertOneTimeToken(String token) {
		if (token == null) {
			return null;
		}
		return oneTimeTokens.get(token);
	}

	public String getChangePasswordToken(String user) {
		String token = StringHelper.getRandomId() + StringHelper.getRandomString(8, StringHelper.ALPHANUM);
		changePasswordToken.put(token, user);
		return token;
	}

	public String getChangePasswordTokenUser(String token, boolean remove) {
		String user = changePasswordToken.get(token);
		if (user != null && remove) {
			changePasswordToken.remove(token);
		}
		return user;
	}

	public Object getLockImportTemplate() {
		return lockImportTemplate;
	}

	public Object getLockLoadContent() {
		if (!StringHelper.isEmpty(getDMZServerInter()) || !StringHelper.isEmpty(getDMZServerIntra())) {
			return PersistenceThread.LOCK;
		} else {
			if (getMainContext() != null) {
				return getMainContext().localLock;
			}
			return localLock;
		}
	}

	public Map<String, String> getConfig() {
		Map<String, String> outMap = new HashMap<String, String>();
		Iterator<String> keys = properties.getKeys();
		while (keys.hasNext()) {
			String key = keys.next();
			outMap.put(key, "" + properties.getProperty(key));
		}
		return outMap;
	}

	public void setApplication(ServletContext application) {
		this.application = application;
	}

	public void setConfig(Map config) {
		for (Object key : config.keySet()) {
			if (!key.equals("folder")) {
				properties.setProperty("" + key, config.get(key));
			}
		}
		templateData = null; // reset template data
		save();
	}

	/**
	 * mode of the edit template, can be used in template renderer for include
	 * special css or js. preview css is : edit_preview_[mode].css
	 * 
	 * @return
	 */
	public String getEditTemplateMode() {
		return properties.getString("edit-template.mode", staticConfig.getEditTemplateMode());
	}

	public void setEditTemplateMode(String mode) {
		synchronized (properties) {
			properties.setProperty("edit-template.mode", mode);
			save();
		}
	}

	public String getActivationKey() {
		return properties.getString("activation-key");
	}

	public void setActivationKey(String value) {

		String key = "activation-key";
		synchronized (properties) {
			if (value == null) {
				properties.clearProperty(key);
			} else {
				properties.setProperty(key, value);
			}
			save();
		}

	}

	public Integer getFirstLoadVersion() {
		if (firstLoadVersion == null) {
			try {
				int version = PersistenceService.getInstance(this).getVersion();
				if (version >= 0) {
					firstLoadVersion = version;
				}
			} catch (ServiceException e) {
				e.printStackTrace();
			}
		}
		return firstLoadVersion;
	}

	public void setStopUndo(boolean stopUndo) {
		if (stopUndo) {
			try {
				PersistenceService persistenceService = PersistenceService.getInstance(this);
				int version = persistenceService.getVersion();
				if (version >= 0) {
					if (persistenceService.isAskStore()) {
						version++;
					}
					stopUndoVersion = version;
				}
			} catch (ServiceException e) {
				e.printStackTrace();
			}
		} else {
			stopUndoVersion = -1;
		}
	}

	public boolean isStopUndo() {
		int version;
		try {
			version = PersistenceService.getInstance(this).getVersion();
			if (version >= 0) {
				return stopUndoVersion == version;
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return false;
	}

	public Integer getLatestUndoVersion() {
		return latestUndoVersion;
	}

	public void setLatestUndoVersion(Integer latestUndoVersion) {
		this.latestUndoVersion = latestUndoVersion;
	}

	/**
	 * get the main context (same if no alias defined)
	 * 
	 * @return
	 */
	public String getSourceContextKey() {
		if (sourceContextKey != null) {
			return sourceContextKey;
		} else {
			return contextKey;
		}
	}

	public void setSourceContextKey(String sourceContextKey) {
		this.sourceContextKey = sourceContextKey;
	}

	public boolean isReversedLink() {
		return properties.getBoolean("reversedLink", true);
	}

	public void setReversedLink(boolean rl) {
		properties.setProperty("reversedLink", rl);
		save();
	}

	public void addForcedContent(String key, String content) {
		forcedContent.put(key, content);
	}

	public String getForcedContent(String key) {
		return forcedContent.get(key);
	}

	private PrintWriter getRedirectUrlList() {
		if (redirectURLList == null) {
			try {
				File redirectURLListFile = getRedirectURLListFile();
				if (!redirectURLListFile.exists()) {
					redirectURLListFile.getParentFile().mkdirs();
					redirectURLListFile.createNewFile();
					logger.info("create url history file : " + redirectURLListFile);
				}
				redirectURLList = new PrintWriter(new BufferedWriter(new FileWriter(getRedirectURLListFile(), true)));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		return redirectURLList;
	}

	public void storeRedirectUrlList() {
		PrintWriter localWriter = redirectURLList;
		redirectURLList = null;
		ResourceHelper.closeResource(localWriter);
	}

	public Properties getRedirectUrlMap() {
		if (redirectURLMap == null) {
			synchronized (lockUrlFile) {
				if (redirectURLMap == null) {
					Properties prop = new Properties();
					Reader reader = null;
					try {
						File redirectURLListFile = getRedirectURLListFile();
						if (redirectURLListFile.exists()) {
							reader = new InputStreamReader(new FileInputStream(redirectURLListFile), ContentContext.CHARACTER_ENCODING);
							prop.load(reader);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						ResourceHelper.closeResource(reader);
					}
					redirectURLMap = prop;
				}
			}
		}
		return redirectURLMap;
	}

	public Properties get404UrlMap() {
		if (url404Map == null) {
			synchronized (lockUrlFile) {
				if (url404Map == null) {
					Properties prop = new Properties();
					Reader reader = null;
					try {
						File redirectURLListFile = get404URLListFile();
						if (redirectURLListFile.exists()) {
							reader = new InputStreamReader(new FileInputStream(redirectURLListFile), ContentContext.CHARACTER_ENCODING);
							prop.load(reader);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						ResourceHelper.closeResource(reader);
					}
					url404Map = prop;
				}
			}
		}
		return url404Map;
	}

	private void store404UrlMap() {
		synchronized (lockUrlFile) {
			if (url404Map != null) {
				Writer writer = null;
				try {
					File redirectURLListFile = get404URLListFile();
					if (redirectURLListFile.exists()) {
						redirectURLListFile.createNewFile();
					}
					writer = new OutputStreamWriter(new FileOutputStream(redirectURLListFile), ContentContext.CHARACTER_ENCODING);
					url404Map.store(writer, getContextKey());
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					ResourceHelper.closeResource(writer);
				}
			}
		}
	}

	public void resetRedirectUrlMap() {
		synchronized (lockUrlFile) {
			redirectURLMap = null;
		}
	}

	public void reset404UrlMap() {
		synchronized (lockUrlFile) {
			url404Map = null;
		}
	}

	public void add404Url(ContentContext ctx, String url) {
		Properties prop = get404UrlMap();
		if (!prop.containsKey(url)) {
			prop.put(url, StringHelper.neverNull(ctx.getRequest().getHeader("Referer"), "no-referer"));
			store404UrlMap();
		}
	}

	public void hide404Url(ContentContext ctx, String url) {
		Properties prop = get404UrlMap();
		if (prop.containsKey(url)) {
			prop.put(url, "");
			store404UrlMap();
		}
	}

	public void delete404Url(ContentContext ctx, String url) {
		Properties prop = get404UrlMap();
		if (prop.containsKey(url)) {
			prop.remove(url);
			store404UrlMap();
		}
	}

	public void delRedirectUrl(ContentContext ctx, String url) {
		synchronized (lockUrlFile) {
			Properties prop = getRedirectUrlMap();
			redirectURLMap = null;
			if (prop.containsKey(url)) {
				prop.remove(url);
			} else {
				logger.warning("url not found : " + url);
			}
			File redirectURLListFile = getRedirectURLListFile();
			Writer writer = null;
			if (redirectURLListFile.exists()) {
				try {
					writer = new OutputStreamWriter(new FileOutputStream(redirectURLListFile), ContentContext.CHARACTER_ENCODING);
					prop.store(writer, ctx.getGlobalContext().getContextKey());
					redirectURLMap = null;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					ResourceHelper.closeResource(writer);
				}
			}
		}
	}

	private static String encodeURLAsKey(String url) {
		return Encode.forUri(url);
		// return StringHelper.createFileName(url);
	}

	public MenuElement convertOldURL(ContentContext ctx, String url) throws Exception {
		if (ctx.isAsViewMode()) {
			String pageId = getRedirectUrlMap().getProperty(encodeURLAsKey(url));
			if (pageId != null) {
				return NavigationHelper.getPageById(ctx, pageId);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public void storeUrl(ContentContext ctx, String url, String pageId) throws Exception {
		if (ctx.isAsViewMode()) {
			String key = encodeURLAsKey(url);
			if (getRedirectUrlMap().getProperty(key) == null) {
				getRedirectUrlList().println(key + '=' + pageId);
				getRedirectUrlMap().setProperty(key, pageId);
				getRedirectUrlList().flush();
			}
		}
	}

	@Override
	public void printInfo(ContentContext ctx, PrintStream out) {
		out.println("****");
		out.println("**** GlobalContext : " + getContextKey());
		out.println("****");
		out.println("**** #cacheMaps        : " + cacheMaps.size());
		out.println("**** #eternalCacheMaps : " + eternalCacheMaps.size());
		out.println("**** #viewPages        : " + viewPages.size());
		out.println("**** #notification     : " + NotificationService.getInstance(this).size());
		out.println("****");

	}

	public String getUserFolder(User user) {
		if (user == null) {
			return null;
		} else {
			return getUserFolder(user.getUserInfo());
		}
	}

	public String getUserFolder(IUserInfo user) {
		if (user == null) {
			return null;
		} else {
			return URLHelper.mergePath(getStaticFolder(), USERS_FOLDER, user.getUserFolder());
		}
	}

	public Object getI18nLock() {
		return i18nLock;
	}

	public void clearTransformShortURL() {
		int countRemoveURL = 0;
		for (Object keyIt : new LinkedList<Object>(getDataKeys())) {
			String key = (String) keyIt;
			if (key.startsWith(TRANSFORM_LONG_KEY_PREFIX)) {
				removeData(key);
				countRemoveURL++;
			} else if (key.startsWith(TRANSFORM_SHORT_KEY_PREFIX)) {
				removeData(key);
				countRemoveURL++;
			}
		}
		logger.info("short url removed : " + countRemoveURL);
	}

	/**
	 * get the long url of a short url
	 * 
	 * @param shortURL
	 * @return
	 */
	public String getTransformShortURL(String shortURL) {
		shortURL = URLHelper.removeParam(shortURL);
		if (shortURL.startsWith("/")) {
			shortURL = shortURL.substring(1);
		}
		return getData(TRANSFORM_LONG_KEY_PREFIX + shortURL);
	}

	/**
	 * create a short url with a long URL
	 * 
	 * @param longURL
	 * @param newName
	 *            propose a new name for the file
	 * @return
	 */
	public String setTransformShortURL(String longURL, String filter, String newName) {
		String shortURL = getData(TRANSFORM_SHORT_KEY_PREFIX + longURL);
		if (shortURL != null) {
			return shortURL;
		} else {
			String fileName;
			if (newName == null) {
				fileName = StringHelper.getFileNameFromPath(longURL);
			} else {
				newName = StringHelper.stringToFileName(newName);
				if (!newName.contains(".")) {
					newName = newName + '.' + StringHelper.getFileExtension(longURL);
				}
				fileName = newName;
			}

			String folder = StringHelper.getLatestFolderFromPath(longURL);
			if (!folder.isEmpty()) {
				fileName = StringHelper.toMaxSize(folder, 50, "") + '_' + fileName;
			}

			shortURL = URLHelper.mergePath(filter, fileName);
			int i = 1;
			String fileOnly = StringHelper.getFileNameWithoutExtension(fileName);
			String ext = StringHelper.getFileExtension(fileName);
			while (getData(TRANSFORM_LONG_KEY_PREFIX + shortURL) != null) {
				shortURL = URLHelper.mergePath(filter, fileOnly + '_' + i + '.' + ext);
				i++;
			}
			setData(TRANSFORM_SHORT_KEY_PREFIX + longURL, shortURL);
			setData(TRANSFORM_LONG_KEY_PREFIX + shortURL, longURL);
			return shortURL;
		}
	}

	public String getUnsubscribeLink() {
		return properties.getString("unsubscribeLink", null);
	}

	public void setUnsubscribeLink(String link) {
		properties.setProperty("unsubscribeLink", link);
	}

	public String getPOPHost() {
		return properties.getString(POP_HOST_PARAM, null);
	}

	public void setPOPHost(String host) {
		properties.setProperty(POP_HOST_PARAM, host);
	}

	public String getPOPPassword() {
		return properties.getString(POP_PASSWORD_PARAM, null);
	}

	public void setPOPPassword(String pwd) {
		properties.setProperty(POP_PASSWORD_PARAM, pwd);
	}

	public String getPOPUser() {
		return properties.getString(POP_USER_PARAM, null);
	}

	public void setPOPUser(String user) {
		properties.setProperty(POP_USER_PARAM, user);
	}

	public int getPOPPort() {
		if (StringHelper.isEmpty(properties.getString(POP_PORT_PARAM, ""))) {
			return 0;
		} else {
			return Integer.parseInt(properties.getString(POP_PORT_PARAM, "0"));
		}
	}

	public void setPOPPort(String port) {
		properties.setProperty(POP_PORT_PARAM, port);
	}

	public void setPOPSsl(boolean ssl) {
		properties.setProperty(POP_SSL, ssl);
	}

	public boolean isPOPSsl() {
		return properties.getBoolean(POP_SSL);
	}

	public String getSMTPHost() {
		return properties.getString(StaticConfig.SMTP_HOST_PARAM, null);
	}

	public void setSMTPHost(String host) {
		properties.setProperty(StaticConfig.SMTP_HOST_PARAM, host);
	}

	public String getSMTPPassword() {
		return properties.getString(StaticConfig.SMTP_PASSWORD_PARAM, null);
	}

	public void setSMTPPassword(String pwd) {
		properties.setProperty(StaticConfig.SMTP_PASSWORD_PARAM, pwd);
	}

	public String getSMTPPort() {
		return properties.getString(StaticConfig.SMTP_PORT_PARAM, null);
	}

	public void setSMTPPort(String port) {
		properties.setProperty(StaticConfig.SMTP_PORT_PARAM, port);
	}

	public String getSMTPUser() {
		return properties.getString(StaticConfig.SMTP_USER_PARAM, null);
	}

	public void setSMTPUser(String user) {
		properties.setProperty(StaticConfig.SMTP_USER_PARAM, user);
	}

	public String getDKIMDomain() {
		return properties.getString("mail.dkim.domain", "");
	}

	public DKIMBean getDKIMBean() {
		if (!StringHelper.isEmpty(getDKIMDomain())) {
			return new DKIMBean(getDKIMDomain(), getDKIMSelector(), DKIMFactory.getDKIMPrivateKeyFile(this).getAbsolutePath(), null);
		} else {
			return null;
		}
	}

	public void setDKIMDomain(String domain) {
		properties.setProperty("mail.dkim.domain", domain);
		save();
	}

	public String getDKIMSelector() {
		return properties.getString("mail.dkim.selector", "");
	}

	public void setDKIMSelector(String selector) {
		properties.setProperty("mail.dkim.selector", selector);
		save();
	}

	public File getSpecialConfigFile() {
		return new File(URLHelper.mergePath(staticConfig.getContextFolder(), getContextKey() + "_special.properties"));
	}

	public boolean isForcedHttps() {
		return properties.getBoolean("security.forced-https", false);
	}

	public void setForcedHttps(boolean https) {
		properties.setProperty("security.forced-https", https);
		save();
	}

	public boolean isBackupThread() {
		return properties.getBoolean("backup.thread", false);
	}

	public void setBackupThread(boolean backup) {
		if (!backup && backupThread != null) {
			backupThread.run = false;
		} else if (backup) {
			startBackupThread();
		}
		properties.setProperty("backup.thread", backup);
		save();
	}

	public void setMetaBloc(String bloc) {
		properties.setProperty("bloc.meta", bloc);
		save();
	}

	public void setHeaderBloc(String bloc) {
		properties.setProperty("bloc.header", bloc);
		save();
	}

	public void setFooterBloc(String bloc) {
		properties.setProperty("bloc.footer", bloc);
		save();
	}

	public String getMetaBloc() {
		return StringHelper.neverNull(properties.getProperty("bloc.meta"));
	}

	public String getHeaderBloc() {
		return StringHelper.neverNull(properties.getProperty("bloc.header"));
	}

	public String getFooterBloc() {
		return StringHelper.neverNull(properties.getProperty("bloc.footer"));
	}

	public SpecialConfigBean getSpecialConfig() {
		if (config == null) {
			File configFile = getSpecialConfigFile();
			if (!configFile.exists()) {
				config = new SpecialConfigBean(Collections.EMPTY_MAP, getStaticConfig());
			} else {
				Properties p = new Properties();
				InputStreamReader fileReader = null;
				try {
					fileReader = new InputStreamReader(new FileInputStream(configFile), ContentContext.CHARACTER_ENCODING);
					p.load(fileReader);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (fileReader != null) {
						try {
							fileReader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				config = new SpecialConfigBean(p, getStaticConfig());
			}
		}
		return config;
	}

	private int getIndexArrayMinute(long infinityIndex) {
		if (infinityIndex < 0) {
			return countArrayMinute.length - (int) (infinityIndex % countArrayMinute.length);
		} else {
			return (int) infinityIndex % countArrayMinute.length;
		}
	}

	public synchronized void touch() {
		touch(true);
	}

	private synchronized void touch(boolean increment) {
		long time = Calendar.getInstance().getTimeInMillis() / 1000;
		if (time - countArrayMinute.length > latestTime) {
			Arrays.fill(countArrayMinute, 0);
			latestTime = time;
		}
		for (long i = time - 1; i > latestTime; i--) {
			countArrayMinute[getIndexArrayMinute(i)] = 0;
		}
		if (increment) {
			if (time == latestTime) {
				countArrayMinute[getIndexArrayMinute(time)]++;
			} else {
				countArrayMinute[getIndexArrayMinute(time)] = 1;
			}
			latestTime = time;
		}
	}

	public int getCount() {
		touch(false);
		int c = 0;
		for (Integer element : countArrayMinute) {
			c = c + element;
		}
		return c;
	}

	public LogContainer getLogList() {
		return logList;
	}

	public boolean isSiteLog() {
		return siteLog;
	}

	public void setSiteLog(boolean siteLog) {
		this.siteLog = siteLog;
	}

	protected File getSiteLogFile() {
		return new File(URLHelper.mergePath(getStaticFolder(), "site.log"));
	}

	public final void log(String level, String group, String text) {
		if (!siteLog) {
			return;
		}

		logList.add(new Log(level, group, text));

		if (getStaticConfig().getSiteLogGroup() != null && !getStaticConfig().getSiteLogGroup().equals(group)) {
			return;
		}
		try {
			if (specialLogFile == null) {
				File logFile = getSiteLogFile();
				specialLogFile = new AppendableTextFile(logFile);
				specialLogFile.setAutoFlush(true);
			}
			String caller = "";
			if (getStaticConfig().isSiteLogCaller()) {
				caller = DebugHelper.getCaller() + " - ";
			}
			specialLogFile.println(StringHelper.renderTime(new Date()) + " - " + caller + group + " : " + text);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public final void log(String level, String group, String text, boolean store) {
		if (!siteLog) {
			return;
		}

		logList.add(new Log(level, group, text));

		if (store && staticConfig.isSiteLogStorage()) {
			if (getStaticConfig().getSiteLogGroup() != null && !getStaticConfig().getSiteLogGroup().equals(group)) {
				return;
			}
			try {
				if (specialLogFile == null) {
					File logFile = new File(URLHelper.mergePath(getStaticFolder(), "site.log"));
					logFile.getParentFile().mkdirs();
					specialLogFile = new AppendableTextFile(logFile);
					specialLogFile.setAutoFlush(true);
				}
				String caller = "";
				if (getStaticConfig().isSiteLogCaller()) {
					caller = DebugHelper.getCaller() + " - ";
				}
				specialLogFile.println(StringHelper.renderTime(new Date()) + " - " + caller + group + " : " + text);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public File getScreenshotFile(ContentContext ctx) {
		return new File(URLHelper.mergePath(getStaticFolder(), SCREENSHOT_FILE_NAME));
	}

	public boolean isScreenshot(ContentContext ctx) {
		return getScreenshotFile(ctx).exists();
	}

	public String getScreenshortUrl(ContentContext ctx) {
		return URLHelper.addParam(URLHelper.createFileURL(ctx, URLHelper.mergePath(staticConfig.getStaticFolder(), SCREENSHOT_FILE_NAME)), "ts", "" + getScreenshotFile(ctx).lastModified());
	}

	public TaxonomyServiceAgregation getAllTaxonomy(ContentContext ctx) throws IOException {
		TaxonomyServiceAgregation outTaxonomy = new TaxonomyServiceAgregation(TaxonomyService.getInstance(ctx), TaxonomyService.getMasterInstance(ctx));
		return outTaxonomy;
	}
	
	public TaxonomyService getTaxonomy(ContentContext ctx) {
		return TaxonomyService.getInstance(ctx);
	}

	public synchronized String addSharedObject(Object obj) {
		String key = reverseSharingMap.get(obj);
		if (key != null) {
			sharingMap.put(key, obj);
			return key;
		}
		key = StringHelper.getRandomString(64);
		while (sharingMap.containsKey(key)) {
			key = StringHelper.getRandomString(64);
		}
		sharingMap.put(key, obj);
		reverseSharingMap.put(obj, key);
		return key;
	}

	public Object getSharedObject(String key) {
		return sharingMap.get(key);
	}

	public Calendar getLatestTicketNotificaitonTime() {
		return latestTicketNotificaitonTime;
	}

	public void setLatestTicketNotificaitonTime(Calendar latestTicketNotificaitonTime) {
		this.latestTicketNotificaitonTime = latestTicketNotificaitonTime;
	}

	public List<String> getQuietArea() {
		if (quietArea != null) {
			return quietArea;
		}
		String areaRaw = properties.getProperty("area.quiet");
		if (StringHelper.isEmpty(areaRaw)) {
			quietArea = Collections.EMPTY_LIST;
		} else {
			quietArea = StringHelper.stringToCollection(areaRaw, ",");
		}
		return quietArea;
	}

	public void setQuitArea(List<String> quietArea) {
		this.quietArea = quietArea;
		synchronized (properties) {
			properties.setProperty("area.quiet", StringHelper.collectionToString(quietArea, ","));
			save();
		}
	}

	public File getPageScreenshotFile(String pageName) {
		return new File(URLHelper.mergePath(getStaticFolder(), "_screenshots_pages", pageName + ".png"));
	}

	public synchronized String createEmailToken(String email) {
		if (StringHelper.isMail(email) && tokenToMail.get(email) != null) {
			return tokenToMail.get(email);
		}
		String token = StringHelper.getNewToken();
		while (tokenToMail.get(token) != null) {
			token = StringHelper.getNewToken();
		}
		tokenToMail.put(token, email);
		tokenToMail.put(email, token);
		return token;
	}

	public String getEmailFromToken(String token) {
		String email = tokenToMail.get(token);
		if (!StringHelper.isMail(email)) {
			return null;
		} else {
			return email;
		}
	}

	public String getGlobalError() {
		return GLOBAL_ERROR;
	}

	public boolean isSiteLogStack() {
		return siteLogStack;
	}

	public void setSiteLogStack(boolean siteLogStack) {
		this.siteLogStack = siteLogStack;
	}

	public boolean isBusinessTicket() {
		return StringHelper.isTrue(getSpecialConfig().get("ticket.business", null));
	}

	public EcomConfig getEcomConfig() {
		if (ecomConfig == null) {
			ecomConfig = new EcomConfig(this);
		}
		return ecomConfig;
	}

	public String getCanonicalHost() {
		return getSpecialConfig().get("canonical.host", null);
	}

	public String getSecurityCsp() {
		return properties.getString("security.csp");
	}

	public void setSecurityCsp(String csp) {
		synchronized (properties) {
			properties.setProperty("security.csp", csp);
			save();
		}
	}

	public String getLocaleCountry() {
		return getStaticConfig().getLocaleCountry();
	}

	/**
	 * return true, if this globalContext was loaded from host in place of context (uri)
	 * @return
	 */
	public Boolean getHost() {
		return host;
	}

	public void setHost(boolean host, String name) {
		// change only if first call or host is true, host=true is priority
		if (this.host == null || host) {
			this.host = host;
			this.hostName = name;
		}
	}

	public boolean isHost() {
		if (host == null) {
			return true;
		} else {
			return host;
		}
	}

	public boolean isProd() {
		Boolean prod = getSpecialConfig().isProd();
		if (prod == null) {
			return getStaticConfig().isProd();
		} else {
			return prod;
		}
	}

	public Properties getProxyMappings() {
		if (proxyMappings == null) {
			proxyMappings = new Properties();

			File propFile = new File(URLHelper.mergePath(getDataFolder(), ResourceHelper.PRIVATE_DIR, "proxy-mappings.properties"));
			logger.info("load : "+propFile);
			try (InputStream in = new FileInputStream(propFile)) {
				proxyMappings.load(in);
			} catch (FileNotFoundException e) {
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
		return proxyMappings;
	}

	public String getHostName() {
		return hostName;
	}

	public ContentService getContentService() {
		return ContentService.getInstance(this);
	}

}
