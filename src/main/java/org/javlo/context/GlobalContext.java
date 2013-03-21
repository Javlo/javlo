package org.javlo.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.javlo.cache.ICache;
import org.javlo.cache.MapCache;
import org.javlo.cache.ehCache.EHCacheWrapper;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.ElementaryURLHelper.Code;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.mailing.MailService;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.IURLFactory;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.service.exception.ServiceException;
import org.javlo.template.Template;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.utils.SmartMap;
import org.javlo.utils.StructuredProperties;
import org.javlo.utils.TimeMap;
import org.javlo.ztatic.StaticInfo;

public class GlobalContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Object LOCK_GLOBAL_CONTEXT_LOAD = new Object();

	private class StorePropertyThread extends Thread {

		private static final int SLEEP_BETWEEN_STORAGE = 10 * 1000; // 10 sec

		GlobalContext globalContext = null;

		public StorePropertyThread(GlobalContext globalContext) {
			this.globalContext = globalContext;
		}

		@Override
		public void run() {
			while (!globalContext.stopStoreThread) {
				if (globalContext.needStoreData) {
					globalContext.needStoreData = false;
					globalContext.saveData();
				}
				try {
					Thread.sleep(SLEEP_BETWEEN_STORAGE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

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

	private CacheManager cacheManager;

	private final Map<String, ICache> cacheMaps = new HashMap<String, ICache>();

	private final Map<String, ICache> eternalCacheMaps = new HashMap<String, ICache>();

	private IURLFactory urlFactory = null;

	private final Map<String, MenuElement> viewPages = new HashMap<String, MenuElement>();

	private final SmartMap frontCache = new SmartMap();

	private ServletContext application;

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(GlobalContext.class.getName());

	private static final String KEY = "globalContext";

	public static final String LICENCE_BASE = "base";

	public static final String LICENCE_BASE_PLUS = "base+";

	public static final String LICENCE_CORPORATE = "corporate";

	public static final String LOGO_FILE_NAME = "dynamic_template/logo.png";

	public GlobalContext() {
		properties.setDelimiterParsingDisabled(true);
		StorePropertyThread storePropertyThread = new StorePropertyThread(this);
		storePropertyThread.start();

	}

	private static void addResources(ContentContext ctx, File dir, Collection<StaticInfo> resources) throws Exception {
		File[] childrenFiles = dir.listFiles();
		for (File file : childrenFiles) {
			if (file.isFile()) {
				resources.add(StaticInfo.getInstance(ctx, file));
			} else {
				addResources(ctx, file, resources);
			}
		}
	}

	public static GlobalContext getDefaultContext(HttpSession session) throws IOException, ConfigurationException {
		return getRealInstance(session, StaticConfig.getInstance(session).getDefaultContext(), false);
	}

	public static GlobalContext getMasterContext(HttpSession session) throws IOException, ConfigurationException {
		return getRealInstance(session, StaticConfig.getInstance(session).getMasterContext(), true);
	}

	public static GlobalContext getSessionInstance(HttpSession session) {
		String contextKey = (String) session.getAttribute(KEY);
		return (GlobalContext) session.getServletContext().getAttribute(contextKey);
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
							ContentContext.setHostDefineSite(request, false);
						} else {
							String host = ServletHelper.getSiteKey(request);
							globalContext = GlobalContext.getInstance(request.getSession(), host);
							contextURI = host;
							if (globalContext == null) {
								logger.severe("error GlobalContext not found : " + request.getRequestURI());
								return null;
							}
							if (!globalContext.getContextKey().equals(contextURI)) { // alias
								ContentContext.setForcePathPrefix(request, contextURI);
							}
							ContentContext.setHostDefineSite(request, true);
						}
					} else {
						logger.severe("error GlobalContext undefined : " + request.getRequestURI());
						return null;
					}
				}
				request.getSession().setAttribute(KEY, contextURI); // mark global context in session.
			} else {
				contextURI = globalContext.getContextKey();
			}
			request.setAttribute(KEY, globalContext);

			return globalContext;
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static GlobalContext getSessionContext(HttpSession session) {
		String contextKey = (String) session.getAttribute(KEY);
		try {
			return GlobalContext.getInstance(session, contextKey);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getSessionContextKey(HttpSession session) {
		return (String) session.getAttribute(KEY);
	}

	public static GlobalContext getInstance(HttpSession session, String contextKey) throws IOException, ConfigurationException {
		if (contextKey == null) {
			return null;
		}
		contextKey = contextKey.toLowerCase();
		GlobalContext newInstance = getRealInstance(session, contextKey);
		String alias = newInstance.getAliasOf();
		if (alias.trim().length() > 0) {
			String newContextKey = StringHelper.stringToFileName(alias);
			if (!newContextKey.equals(contextKey)) {
				newInstance = getRealInstance(session, alias);
			}
		}
		session.setAttribute(KEY, newInstance.getContextKey());
		return newInstance;
	}

	public static GlobalContext getInstance(ServletContext application, StaticConfig staticConfig, File configFile) throws IOException, ConfigurationException {
		String contextKey = FilenameUtils.getBaseName(configFile.getName());
		GlobalContext newInstance = (GlobalContext) application.getAttribute(contextKey);
		if (newInstance == null) {
			synchronized (LOCK_GLOBAL_CONTEXT_LOAD) {
				newInstance = new GlobalContext();
				newInstance.application = application;
				synchronized (newInstance.properties) {
					newInstance.staticConfig = staticConfig;
					application.setAttribute(contextKey, newInstance);
					newInstance.setContextKey(contextKey);
					if (configFile.exists()) {
						newInstance.properties.clear();
						newInstance.properties.load(configFile);
					}
					int countAccess = newInstance.properties.getInt("session-count", 0);
					newInstance.properties.setProperty("session-count", countAccess + 1);
					newInstance.properties.setProperty("access-date", StringHelper.renderTime(new Date()));
					newInstance.contextFile = configFile;
					newInstance.save();

					newInstance.initCacheManager();
				}
			}
		}
		return newInstance;
	}

	private void initCacheManager() throws IOException {
		cacheManager = staticConfig.getEhCacheManager();
	}

	public static GlobalContext getRealInstance(HttpSession session, String contextKey) throws IOException, ConfigurationException {
		return getRealInstance(session, contextKey, true);
	}

	private static GlobalContext getRealInstance(HttpSession session, String contextKey, boolean copyDefaultContext) throws IOException, ConfigurationException {
		contextKey = StringHelper.stringToFileName(contextKey);

		StaticConfig staticConfig = StaticConfig.getInstance(session.getServletContext());

		synchronized (LOCK_GLOBAL_CONTEXT_LOAD) {
			// ServletContextWeakReference gcc = ServletContextWeakReference.getInstance(session.getServletContext());
			GlobalContext newInstance = (GlobalContext) session.getServletContext().getAttribute(contextKey);
			if (newInstance == null) {
				newInstance = new GlobalContext();
				newInstance.staticConfig = staticConfig;
				newInstance.application = session.getServletContext();
			} else {
				newInstance.staticConfig = staticConfig;
				return newInstance;
			}

			newInstance.setContextKey(contextKey);
			String fileName = contextKey + ".properties";
			newInstance.contextFile = new File(ElementaryURLHelper.mergePath(staticConfig.getContextFolder(), fileName));
			if (!newInstance.contextFile.exists()) {
				if (!newInstance.contextFile.getParentFile().exists()) {
					newInstance.contextFile.getParentFile().mkdirs();
					newInstance.creation = true;
				}
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
				synchronized (newInstance.properties) {
					newInstance.properties.load(newInstance.contextFile);
				}
			}

			session.getServletContext().setAttribute(contextKey, newInstance);
			session.setAttribute(KEY, newInstance);

			synchronized (newInstance.properties) {
				newInstance.properties.setProperty("access-date", StringHelper.renderTime(new Date()));
			}

			newInstance.initCacheManager();

			newInstance.writeInfo(session, System.out);

			// TODO : init resource Id

			return newInstance;
		}
	}

	/**
	 * check if there are at least one context
	 * 
	 * @param request
	 * @return
	 */
	public static boolean haveContext(HttpServletRequest request) {
		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
		File dir = new File(staticConfig.getContextFolder());
		if (dir != null && dir.listFiles() != null) {
			return dir.listFiles().length > 0;
		} else {
			return false;
		}
	}

	public static boolean isExist(HttpServletRequest request, String contextKey) throws IOException, ConfigurationException {
		contextKey = StringHelper.stringToFileName(contextKey);
		String fileName = contextKey + ".properties";

		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());

		File contextFile = new File(ElementaryURLHelper.mergePath(staticConfig.getContextFolder(), fileName));

		return contextFile.exists();
	}

	private final PropertiesConfiguration properties = new PropertiesConfiguration();

	private String contextKey = null;

	private boolean creation = false;

	private static final String VOTES = "";

	private Class<IUserFactory> userFactoryClass = null;

	private IUserFactory userFactory = null;

	private String userFactoryClassName = "";

	private Class<IUserFactory> adminUserFactoryClass = null;

	private AdminUserFactory admimUserFactory = null;

	private String adminUserFactoryClassName = "";

	private StaticConfig staticConfig = null;

	private Map<String, String> uriAlias = null;

	private File contextFile = null;

	private Properties dataProperties = null;

	private static final String URI_ALIAS_KEY_PREFIX = "uri-alias.";

	public static final String LICENCE_FREE = "free";

	public static final String LICENCE_FREE_PLUS = "free+";

	private final transient Map<String, WeakReference<User>> allUsers = new WeakHashMap<String, WeakReference<User>>();

	private final transient Map<Code, WeakReference<Code>> specialAccessCode = new WeakHashMap<Code, WeakReference<Code>>();

	private final Map<String, String> resourcePathToId = new HashMap<String, String>();

	private final Map<String, String> resourceIdToPath = new HashMap<String, String>();

	private final Map<Object, Object> attributes = new HashMap<Object, Object>();

	private final TimeMap<Object, Object> timeAttributes = new TimeMap<Object, Object>(60 * 5);

	private Template.TemplateData templateData = null;

	private TimeTravelerContext timeTravelerContext = new TimeTravelerContext();

	private String pathPrefix;

	private Set<String> noPopupDomain = null;

	private boolean needStoreData = false;

	private boolean stopStoreThread = false;

	private Long accountSize = null;

	private String dataFolder = null;

	private final TimeMap<String, String> oneTimeTokens = new TimeMap<String, String>(60 * 60); // one time tolen live 1u

	public long getAccountSize() {
		if (accountSize == null) {
			File file = new File(getDataFolder());
			if (file.exists()) {
				accountSize = FileUtils.sizeOfDirectory(file);
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

	public void addTemplate(String templateId, boolean mailing) {
		synchronized (properties) {
			List<String> templates;
			if (mailing) {
				templates = getMailingTemplates();
			} else {
				templates = getTemplatesNames();
			}
			if (!templates.contains(templateId)) {
				templates.add(templateId);
				if (mailing) {
					setMailingTemplates(templates);
				} else {
					setTemplatesNames(templates);
				}
			}
		}
	}

	public void removeTemplate(String templateId, boolean mailing) {
		synchronized (properties) {
			List<String> templates;
			if (mailing) {
				templates = getMailingTemplates();
			} else {
				templates = getTemplatesNames();
			}
			if (templates.contains(templateId)) {
				templates.remove(templateId);
				if (mailing) {
					setMailingTemplates(templates);
				} else {
					setTemplatesNames(templates);
				}
			}
		}
	}

	public boolean administratorLogin(String inLogin, String inPassword) {
		String login = properties.getString("admin", "admin");
		String password = properties.getString("admin.password", "1234");
		return login.equals(inLogin) && password.equals(StringHelper.encryptPassword(inPassword));
	}

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
			stopStoreThread = true;
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
		stopStoreThread = true;
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

	public ICache getCache(String cacheName) {
		if (staticConfig.useEhCache()) {
			return getEhCacheCache(cacheName);
		} else {
			return getMapCache(cacheName);
		}
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
		if (cacheManager != null) {
			for (String cacheName : cacheManager.getCacheNames()) {
				outCaches.add(getCache(cacheName));
			}
		} else {
			for (String cacheName : cacheMaps.keySet()) {
				outCaches.add(getCache(cacheName));
			}
		}
		return outCaches;
	}

	public synchronized ICache getMapCache(String cacheName) {
		ICache cache = cacheMaps.get(cacheName);
		if (cache == null) {
			cache = new MapCache(new HashMap(), cacheName);
			cacheMaps.put(cacheName, cache);
		}
		return cache;
	}

	private synchronized ICache getEhCacheCache(String cacheName) {
		ICache outCache = cacheMaps.get(cacheName);
		if (outCache == null) {
			Cache cache = cacheManager.getCache(cacheName);
			if (cache == null) {
				synchronized (cacheManager) {
					cache = cacheManager.getCache(cacheName);
					if (cache == null) {
						if (cacheName.equals(AbstractVisualComponent.TIME_CACHE_NAME)) {
							cache = new Cache(cacheName, 0, true, false, 60, 60 * 60); // time cache config
						} else {
							cache = new Cache(cacheName, 0, true, false, 60 * 60 * 24, 60 * 60 * 24); // default cache config
						}
						cacheManager.addCache(cache);
					}
				}
			}
			outCache = new EHCacheWrapper(getContextKey(), cacheName, cache);
			cacheMaps.put(cacheName, outCache);
		}
		return outCache;
	}

	public List<String> getComponents() {
		List<String> components = new LinkedList<String>();
		String componentRaw = properties.getString("components", "");
		components.addAll(StringHelper.stringToCollection(componentRaw));
		components.remove(""); // remove empty string

		return components;
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

	public Set<String> getContentLanguages() {
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
		return outLg;
	}

	public String getContextKey() {
		return contextKey;
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

	public synchronized String getData(String key) {
		if (dataProperties == null) {
			initDataFile();
		}
		return dataProperties.getProperty(key);
	}

	public synchronized Collection<Object> getDataKeys() {
		if (dataProperties == null) {
			initDataFile();
		}
		return dataProperties.keySet();
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

	public String getDataFolder() {
		if (dataFolder == null) {
			dataFolder = staticConfig.getAllDataFolder();
			if (getFolder() != null) {
				dataFolder = ElementaryURLHelper.mergePath(dataFolder, getFolder());
			}
			try {
				File folderFile = new File(dataFolder);
				dataFolder = folderFile.getCanonicalPath();
				if (!folderFile.exists()) {
					folderFile.mkdirs();
				}
			} catch (IOException e) {
				logger.warning(e.getMessage());
			}
		}
		return dataFolder;
	}

	public String getStaticFolder() {
		return URLHelper.mergePath(getDataFolder(), staticConfig.getStaticFolder());
	}

	public String getDBDriver() {
		return properties.getString("db.driver", staticConfig.getDBDriver());
	}

	public String getDBLogin() {
		return properties.getString("db.login", staticConfig.getDBLogin());
	}

	public String getDBPassword() {
		return properties.getString("db.password", staticConfig.getDBPassword());
	}

	public String getDBResourceName() {
		return properties.getString("db.resource-name", staticConfig.getDBResourceName());
	}

	public String getDBURL() {
		return properties.getString("db.url", staticConfig.getDBURL());
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
		String lgRAW = getDefaultLanguagesRAW();
		if (lgRAW == null) {
			return Collections.emptySet();
		}
		Set<String> outLg = new LinkedHashSet<String>();
		for (String lg : StringHelper.stringToArray(lgRAW, ";")) {
			outLg.add(lg);
		}
		return outLg;
	}

	public String getDefaultLanguagesRAW() {
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
		if (urlStr != null) {
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
		}
		return admimUserFactory;
	}

	private Class<IUserFactory> getAdminUserFactoryClass() throws ClassNotFoundException {
		if (adminUserFactoryClass == null) {
			adminUserFactoryClassName = getAdminUserFactoryClassName();
			adminUserFactoryClass = (Class<IUserFactory>) Class.forName(adminUserFactoryClassName);
		}
		return adminUserFactoryClass;
	}

	public String getAdminUserFactoryClassName() {
		String userFactoryClass = properties.getString("adminuserfactory.class", "org.javlo.user.AdminUserFactory").trim();
		return userFactoryClass;
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

	public String getGoogleAnalyticsUACCT() {
		return properties.getString("google.uacct", "");
	}

	public String getHelpURL() {
		return properties.getString("help-url", staticConfig.getHelpURL());
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

	public Set<String> getLanguages() {
		String lgRAW = properties.getString("languages", "fr;nl;en");
		if (lgRAW == null) {
			return Collections.emptySet();
		}
		Set<String> outLg = new LinkedHashSet<String>();
		for (String lg : StringHelper.stringToArray(lgRAW, ";")) {
			lg = lg.replace(".", "");
			outLg.add(lg);
		}
		return outLg;
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

	public File getLogo() {
		File logo = new File(ElementaryURLHelper.mergePath(ElementaryURLHelper.mergePath(getDataFolder(), staticConfig.getStaticFolder()), LOGO_FILE_NAME));
		if (logo.exists()) {
			return logo;
		} else {
			return null;
		}
	}

	/*
	 * public String getDefaultLanguage() { String defaultLg = "en"; if (getLanguages().size() > 0) { defaultLg = getLanguages().iterator().next(); } return properties.getString("default.language", defaultLg); }
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

	public String getMailingSender() {
		return properties.getString("mailing.sender", "");
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

	public List<String> getMailingTemplates() {
		List<String> templates = new LinkedList<String>();
		String templatesRaw = properties.getString("mailing-templates", "");
		templates.addAll(StringHelper.stringToCollection(templatesRaw));
		return templates;
	}

	public String getMediumDateFormat() {
		return properties.getString("date.medium", staticConfig.getDefaultDateFormat());
	}

	public MenuElement getPage(ContentContext ctx, String url) throws Exception {
		MenuElement elem = getPageIfExist(ctx, url);
		if (elem == null) {
			if (ctx.isPageRequest() && ctx.getPath().equals(url)) {
				logger.info("page not found : " + url + " (ctx=" + ctx + ')');
				ctx.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND, "page not found : " + url);
			}
			MenuElement root = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx);
			elem = root.searchChildFromName("404");
			if (elem == null) {
				elem = root;
			}
		}
		return elem;
	}

	public MenuElement getPageIfExist(ContentContext ctx, String url) throws Exception {
		IURLFactory urlCreator = getURLFactory(ctx);
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE && urlCreator != null) {
			synchronized (viewPages) {
				if (viewPages.size() == 0) {
					ContentContext lgCtx = new ContentContext(ctx);
					Collection<String> lgs = getContentLanguages();
					for (String lg : lgs) {
						lgCtx.setRequestContentLanguage(lg);
						MenuElement[] children = ContentService.getInstance(ctx.getRequest()).getNavigation(lgCtx).getAllChildren();
						for (MenuElement menuElement : children) {
							String pageURL = urlCreator.createURL(lgCtx, menuElement);
							String pageKeyURL = urlCreator.createURLKey(pageURL);
							viewPages.put(pageKeyURL, menuElement);
						}
					}
				}
			}
		}
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			String keyURL = url;
			if (urlCreator != null) {
				keyURL = urlCreator.createURLKey(url);
			}

			MenuElement page = viewPages.get(keyURL);
			if (page != null) {
				return page;
			}
		}
		MenuElement root = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx);
		if (url.equals("/")) {
			return root;
		} else {
			Collection<MenuElement> pastNode = new LinkedList<MenuElement>();
			MenuElement page = MenuElement.searchChild(root, ctx, url, pastNode);

			if (page != null && ctx.getRenderMode() == ContentContext.VIEW_MODE) {
				viewPages.put(url, page);
			}

			return page;
		}
	}

	public String getPathPrefix() {
		return pathPrefix;
	}

	public Date getPublishDate() throws ParseException {
		String dateStr = properties.getString("publish-date", null);
		if (dateStr == null) {
			return null;
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
		return properties.getString("languages", "fr;nl;en");
	}

	public Collection<StaticInfo> getResources(ContentContext ctx) throws Exception {
		Collection<StaticInfo> resources = new LinkedList<StaticInfo>();
		File staticDir = new File(ElementaryURLHelper.mergePath(getDataFolder(), staticConfig.getStaticFolder()));
		addResources(ctx, staticDir, resources);
		return resources;
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

	public Template.TemplateData getTemplateData() {
		if (templateData != null) {
			return templateData;
		}
		String templateDataRAW = properties.getString("template-data", null);
		if (templateDataRAW == null) {
			templateData = Template.TemplateData.EMPTY;
			return Template.TemplateData.EMPTY;
		}
		templateData = new Template.TemplateData(templateDataRAW);
		return templateData;
	}

	public List<String> getTemplatesNames() {
		List<String> templates = new LinkedList<String>();
		String templatesRaw = properties.getString("templates", "");
		templates.addAll(StringHelper.stringToCollection(templatesRaw));
		return templates;
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

	public IURLFactory getURLFactory(ContentContext ctx) {
		if (urlFactory != null) {
			return urlFactory;
		} else {
			String urlClass = properties.getString("url-factory", null);
			if (urlClass != null && urlClass.trim().length() > 0) {
				try {
					urlFactory = ((Class<IURLFactory>) Class.forName(urlClass)).newInstance();
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				return urlFactory;
			}
		}
		return null;
	}

	public String getURLFactoryClass() {
		if (urlFactory == null) {
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
		Set<String> outRole = new HashSet<String>(Arrays.asList(StringHelper.stringToArray(roleRaw)));
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

	public synchronized void initDataFile() {
		if (dataProperties == null) {
			dataProperties = new StructuredProperties();
		}
		InputStream in = null;
		try {
			in = new FileInputStream(getDataFile());
			dataProperties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ResourceHelper.closeResource(in);
		}
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

	public boolean isOpenExernalLinkAsPopup(String url) {

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

	public boolean isReversedLink() {
		return properties.getBoolean("reversed-link", false);
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
		for (String name : getAllCacheName()) {
			getCache(name).removeAll();
		}
		cacheMaps.clear();

		viewPages.clear();
		frontCache.clear();
		try {
			ReverseLinkService.getInstance(this).clearCache();
		} catch (ServiceException e) {
			e.printStackTrace();
		}

		dataFolder = null;
	}

	public void reload() {
		try {
			synchronized (LOCK_GLOBAL_CONTEXT_LOAD) {
				properties.clear();
				properties.load(contextFile);
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
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
		try {
			synchronized (properties) {
				if (getFolder() != null && getFolder().trim().length() > 0) {
					properties.save(contextFile);
				} else {
					logger.severe("no folder found for : " + getContextKey() + " context not stored, try to reload.");
					reload();
				}
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void sendMailToAdministrator(String subjet, String body) throws MessagingException {
		MailService mailService = MailService.getInstance(staticConfig);
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
		attributes.put(key, att);
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
		if (dataProperties == null) {
			initDataFile();
		}
		synchronized (dataProperties) {
			dataProperties.put(key, value);
			askStoreData();
		}

	}

	private void askStoreData() {
		needStoreData = true;
	}

	private void saveData() {
		if (dataProperties != null) {
			synchronized (dataProperties) {
				logger.fine("store data");
				OutputStream out = null;
				try {
					out = new FileOutputStream(getDataFile());
					long startTime = System.currentTimeMillis();
					logger.info("start storage data of context " + getContextKey());
					dataProperties.store(out, getContextKey());
					logger.info("store data for : " + getContextKey() + " size:" + dataProperties.size() + " time:" + StringHelper.renderTimeInSecond(System.currentTimeMillis() - startTime));
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						out.close();
					} catch (Throwable e) {
						e.printStackTrace();
					}
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

	public void setMailingSender(String sender) {
		synchronized (properties) {
			properties.setProperty("mailing.sender", sender);
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

	public void setRAWContentLanguages(String languages) {
		properties.setProperty("content-languages", languages);
	}

	public void setRAWEncodings(String encodings) {
		properties.setProperty("encodings", encodings);
	}

	public void setRAWLanguages(String languages) {
		properties.setProperty("languages", languages);
	}

	public void setReversedLink(boolean extendMenu) {
		synchronized (properties) {
			properties.setProperty("reversed-link", extendMenu);
			save();
		}
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

	public void setTemplateData(Template.TemplateData inTemplateData) {
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
			String[] rolesArray = new String[roles.size()];
			roles.toArray(rolesArray);
			properties.setProperty("roles", StringHelper.arrayToString(rolesArray));
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

	/**
	 * store the logo of the website.
	 * 
	 * @param ctx
	 *            context
	 * @param in
	 *            inputStream, if null old logo is deleted.
	 * @throws IOException
	 */
	public void storeLogo(ContentContext ctx, InputStream in) throws IOException {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		File logo = new File(ElementaryURLHelper.mergePath(ElementaryURLHelper.mergePath(getDataFolder(), staticConfig.getStaticFolder()), LOGO_FILE_NAME));
		if (in != null) {
			if (!logo.exists()) {
				logo.getParentFile().mkdirs();
			}
			ResourceHelper.writeStreamToFile(in, logo);
		} else {
			if (logo.exists()) {
				logo.delete();
			}
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

	public Map getFrontCache(ContentContext ctx) {
		return new SmartMap(ctx, frontCache);
	}

	/**
	 * put item in front cache.
	 * 
	 * @param key
	 *            the key of item
	 * @param value
	 *            the value of item. If null item is removed.
	 */
	public void putItemInFrontCache(ContentContext ctx, String key, String value, String rendererKey) {
		if (value != null) {
			try {
				String renderer = ctx.getCurrentTemplate().getRenderer(ctx, rendererKey);
				frontCache.put(key, new SmartMap.JspSmartValue(renderer, value));
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			frontCache.remove(key);
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
		try {
			out.println("**** Modules            :  " + StringHelper.collectionToString(ModulesContext.getInstance(session, this).getAllModules(), ", "));
		} catch (ModuleException e) {
			out.println("**** Error load Modules :  " + e.getMessage());
			e.printStackTrace();
		}
		out.println("**** # attributes       :  " + attributes.size());
		out.println("**** # time attributes  :  " + timeAttributes.size());
		out.println("**** # cached pages     :  " + viewPages.size());
		out.println("****");
		out.println("****************************************************************");
		out.println("****************************************************************");
		if (cacheManager != null) {
			out.println("****");
			out.println("**** CACHE INFO ");
			out.println("****");
			String[] cachesName = cacheManager.getCacheNames();
			for (String cacheName : cachesName) {
				Cache cache = cacheManager.getCache(cacheName);
				if (cache != null) {
					out.println("**** cache name : " + cacheName);
					out.println("**** mem. store size : " + cache.getMemoryStoreSize());
					out.println("**** disk store size : " + cache.getDiskStoreSize());
					out.println("**** cache size      : " + cache.getSize());
					out.println("**** CONFIG ****");
					out.println("**** MaxElementsInMemory : " + cache.getCacheConfiguration().getMaxElementsInMemory());
					out.println("**** MaxElementsOnDisk   : " + cache.getCacheConfiguration().getMaxElementsOnDisk());
					out.println("**** TimeToLiveSeconds   : " + cache.getCacheConfiguration().getTimeToLiveSeconds());
					out.println("**** TimeToIdleSeconds   : " + cache.getCacheConfiguration().getTimeToIdleSeconds());
					out.println("**** Eternal ?           : " + cache.getCacheConfiguration().isEternal());
					out.println("**** Disk Persistent ?   : " + cache.getCacheConfiguration().isDiskPersistent());
					out.println("**** Over flow to disk ? : " + cache.getCacheConfiguration().isOverflowToDisk());

					/*
					 * out.println("**** stat cache hits : " + stat.getCacheHits()); out.println("           memory    : " + stat.getInMemoryHits()); out.println("           disk      : " + stat.getDiskStoreObjectCount()); out.println("**** cache Misses    : " + stat.getCacheMisses()); out.println("**** cache count     : " + stat.getObjectCount());
					 */
					out.println("****");
				}
			}
			out.println("****************************************************************");
			out.println("****************************************************************");
			out.println("attributes : ");
			for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
				out.println(entry.getKey() + " = " + entry.getValue());
			}
			out.println("****************************************************************");
			out.println("****************************************************************");

		}
	}

	public String createOneTimeToken(String token) {
		String newToken = StringHelper.getRandomIdBase64();
		oneTimeTokens.put(newToken, token);
		return newToken;
	}

	public String convertOneTimeToken(String token) {
		String realToken = oneTimeTokens.get(token);
		if (realToken != null) {
			oneTimeTokens.remove(token);
		}
		return realToken;
	}

	public boolean isEhCache() {
		return cacheManager != null;
	}
}
