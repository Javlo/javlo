package org.javlo.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import net.sf.ehcache.CacheManager;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.source.TestDataSource;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.macro.core.IMacro;
import org.javlo.servlet.AccessServlet;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.ztatic.FileCache;

public class StaticConfig extends Observable {
	
	public static String WEB_PLATFORM = "web";
	public static String MAILING_PLATFORM = "mailing";
	private static final List<String> PLATFORMS = new LinkedList<String>(Arrays.asList(new String[] {WEB_PLATFORM, MAILING_PLATFORM}));

	private static final String EHCACHE_FILE = "/WEB-INF/config/ehcache.xml";

	protected static Logger logger = Logger.getLogger(StaticConfig.class.getName());

	PropertiesConfiguration properties = new PropertiesConfiguration();
	Map<String, User> editUsers = new HashMap<String, User>();

	public static final String WEBAPP_CONFIG_FILE = "/WEB-INF/config/webapp_config.properties";

	static final String EDIT_USERS_KEY = "edit.users";
	static final String DEFAULT_CREDENTIALS = "admin,0DPiKuNIrrVmD8IUCuw1hQxNqZc="; // admin,admin;

	private static final String FILE_NAME = "static-config.properties";
	private static final String DEFAULT_CONFIG_DIR = "/WEB-INF/config";

	private static final String SMTP_HOST_PARAM = "mail.smtp.host";
	private static final String SMTP_PORT_PARAM = "mail.smtp.port";
	private static final String SMTP_USER_PARAM = "mail.smtp.user";
	private static final String SMTP_PASSWORD_PARAM = "mail.smtp.password";

	private static final String STATIC_CONFIG_RELATIVE_KEY = "static-config.relative";
	private static final String STATIC_CONFIG_KEY = "static-config.directory";

	private static final String KEY = StaticConfig.class.getName();

	private static final String HOME = System.getProperty("user.home");
	
	private Set<String> excludeContextDomain = null;

	/**
	 * @Deprecated use getInstance (ServletContext application)
	 */
	public static StaticConfig getInstance(HttpSession session) {
		if (session == null) {
			return null;
		} else {
			return getInstance(session.getServletContext());
		}
	}

	public static StaticConfig getInstance(ServletContext application) {
		StaticConfig outCfg = null;
		if (application != null) {
			outCfg = (StaticConfig) application.getAttribute(KEY);
		}
		if (outCfg == null) {
			outCfg = new StaticConfig(application);
		}
		return outCfg;
	}

	private ServletContext application = null;

	private String defaultProxyHost = null;

	private int defaultProxyPort = -1;

	private String staticConfigLocalisation;

	private Class<IUserFactory> adminUserFactoryClass = null;

	private AdminUserFactory admimUserFactory = null;

	private String adminUserFactoryClassName = "";

	private Map<String, String> devices = null;

	public static final List<String> BASIC_MODULES = Arrays.asList(new String[] { "admin", "content", "file" });

	private StaticConfig(ServletContext application) {
		this.application = application;
		try {
			synchronized (FILE_NAME) {
				Properties webappProps = new Properties();
				if (application != null) {
					InputStream in = application.getResourceAsStream(StaticConfig.WEBAPP_CONFIG_FILE);
					try {
						webappProps.load(in);
					} finally {
						ResourceHelper.closeResource(in);
					}
				}

				/** LOAD GOD USERS * */
				String editUser = webappProps.getProperty(EDIT_USERS_KEY);
				if (editUser != null) {
					if (editUser.startsWith("${")) {
						editUser = DEFAULT_CREDENTIALS;
					}
					String[] userPasswordList = editUser.split(";");
					for (String element : userPasswordList) {
						try {
							String[] userPassword = element.split(",");
							User user = new User(userPassword[0], userPassword[1]);
							logger.info("add edit user : " + user.getName());

							editUsers.put(user.getName(), user);
						} catch (RuntimeException e) {
							logger.severe("the definition of edit users list is not correct.");
						}
					}
				} else {
					logger.severe("no user found for edit.");
				}

				/** LOAD STATIC CONFIG FILE LOCATION * */
				staticConfigLocalisation = webappProps.getProperty(STATIC_CONFIG_KEY);
				if (application != null) {
					if (staticConfigLocalisation == null || staticConfigLocalisation.trim().length() == 0 || staticConfigLocalisation.contains("${")) {
						staticConfigLocalisation = application.getRealPath(DEFAULT_CONFIG_DIR + "/" + FILE_NAME);
					} else {
						staticConfigLocalisation = ElementaryURLHelper.mergePath(staticConfigLocalisation, FILE_NAME);

						boolean staticConfigRelative = Boolean.parseBoolean(webappProps.getProperty(STATIC_CONFIG_RELATIVE_KEY));
						if (staticConfigRelative) {
							staticConfigLocalisation = application.getRealPath(staticConfigLocalisation);
						}
					}
				}

				staticConfigLocalisation = replaceFolderVariable(staticConfigLocalisation);

				if (staticConfigLocalisation != null) {
					File file = new File(staticConfigLocalisation);
					logger.info("load static config : " + file);
					if (!file.exists()) {
						if (!file.getParentFile().exists()) {
							file.getParentFile().mkdirs();
						}
						file.createNewFile();
					}
					properties.setDelimiterParsingDisabled(true);
					properties.setFile(file);
					properties.load();
				}
				
			}
			if (application != null) {
				application.setAttribute(KEY, this);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "static config file location not found (" + staticConfigLocalisation + "), using default location inside webapp", e);
		}

	}

	public Level getAbstractComponentLogLevel() {
		try {
			return Level.parse(properties.getString("log.component.abstract.level", "INFO"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return Level.INFO;
		}
	}

	public Level getAccessLogLevel() {
		try {
			return Level.parse(properties.getString("log.access.level", "INFO"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return Level.INFO;
		}

	}

	public AdminUserFactory getAdminUserFactory(GlobalContext globalContext, HttpSession session) throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (admimUserFactory == null) {
			Constructor<IUserFactory> construct = getAdminUserFactoryClass().getConstructor();
			admimUserFactory = (AdminUserFactory) construct.newInstance();
			logger.info("create admin user info : " + admimUserFactory.getClass());
			admimUserFactory.init(globalContext, session);
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

	public Level getAllComponentLogLevel() {
		try {
			return Level.parse(properties.getString("log.component.all.level", "INFO"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return Level.INFO;
		}
	}

	/**
	 * return the folder of data context.
	 * 
	 * @return the folder of data context.
	 */
	public String getAllDataFolder() {
		String folder = properties.getString("data-folder", "/WEB-INF/data-ctx/");
		folder = replaceFolderVariable(folder);
		if (isDataFolderRelative() && application != null) {
			folder = application.getRealPath(folder);
		}
		return folder;
	}

	public String getExternalComponentFolder() {
		return URLHelper.mergePath(getAllDataFolder(), properties.getString("external-components-folder", "external-components"));
	}

	public Level getAllLogLevel() {
		try {
			return Level.parse(properties.getString("log.all.level", "INFO"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return Level.INFO;
		}
	}

	public String getAllProperties() throws IOException {
		synchronized (FILE_NAME) {
			File file = new File(properties.getFile().getAbsolutePath());

			logger.info("load all properties from : " + properties.getFile().getAbsolutePath());

			return FileUtils.readFileToString(file, ContentContext.CHARACTER_ENCODING);
		}
	}

	public String getAttribute(String key, String defaultValue) {
		return properties.getString(key, defaultValue);
	}

	public Set<String> getBackupExcludePatterns() {
		String value = properties.getString("backup.exclude-patterns", "");
		return new HashSet<String>(Arrays.asList(value.split(";")));
	}

	public String getBackupFolder() {
		return properties.getString("backup-folder", "backup");
	}

	public Set<String> getBackupIncludePatterns() {
		String value = properties.getString("backup.include-patterns", "/persitence/content_2.xml");
		return new HashSet<String>(Arrays.asList(value.split(";")));
	}

	public String getCacheFolder() {
		return properties.getString("cache-folder", "_cache");
	}

	/**
	 * cache between two update for linked page (in second)
	 * 
	 * @return a time in second.
	 */
	public int getCacheLinkedPage() {
		return Integer.parseInt(properties.getString("cache.linked-page", "30"));
	}

	public String getContextFolder() {
		String path = properties.getString("context-folder", "/WEB-INF/context");

		path = replaceFolderVariable(path);

		if (isDataFolderRelative()) {
			path = application.getRealPath(path);
		}
		return path;
	}

	public String getCSVFolder() {
		return ElementaryURLHelper.mergePath(getStaticFolder(), properties.getString("csv-folder", "csv"));
	}

	public Map<String, String> getDataSource() {
		Iterator keys = properties.getKeys();
		Map<String, String> outDataSource = new HashMap<String, String>();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (key.startsWith("data.")) {
				String dataKey = key.replaceFirst("data.", "");
				String value = properties.getString(key);
				outDataSource.put(dataKey, value);
			}
		}
		if (outDataSource.size() == 0) {
			outDataSource.put("test", TestDataSource.class.getName());
		}
		return outDataSource;
	}

	/*
	 * public boolean isAccessLogger() { return
	 * properties.getBoolean("logger.access", true); }
	 */

	public String getDBDriver() {
		return properties.getString("db.driver", null);
	}

	public String getDBLogin() {
		return properties.getString("db.login", null);
	}

	public String getDBPassword() {
		return properties.getString("db.password", null);
	}

	public String getDBResourceName() {
		return properties.getString("db.resource-name", null);
	}

	public String getDBURL() {
		return properties.getString("db.url", null);
	}

	public String getDefaultContext() {
		return properties.getString("default-context", "default.javlo.org");
	}

	public String getMasterContext() {
		return properties.getString("master-context", "admin");
	}

	public String getDefaultImage() {
		return properties.getString("image.default", "default.png");
	}

	public String getDefaultProxyHost() {
		if (defaultProxyPort < 0) {
			initDefaultProxy();
		}
		return defaultProxyHost;
	}

	public int getDefaultProxyPort() {
		if (defaultProxyPort < 0) {
			initDefaultProxy();
		}
		return defaultProxyPort;
	}

	public String getDefaultReport() {
		String defaultReport = properties.getString("mail.default.report");

		if ((defaultReport != null)) {
			if (!PatternHelper.MAIL_PATTERN.matcher(defaultReport).matches()) {
				logger.warning("default report : '" + defaultReport + "' is not a valid email.");
				defaultReport = "";
			}
		} else {
			defaultReport = "";
		}
		return defaultReport;
	}

	public String getDefaultSender() {
		String defaultSender = properties.getString("mail.default.sender");

		if ((defaultSender != null)) {
			if (!PatternHelper.MAIL_PATTERN.matcher(defaultSender).matches()) {
				logger.warning("default sender : '" + defaultSender + "' is not a valid email.");
				defaultSender = "";
			}
		} else {
			defaultSender = "";
		}
		return defaultSender;
	}

	public String getDefaultSubject() {
		String defaultSubject = properties.getString("mail.default.subject");

		if ((defaultSubject != null)) {
			if (defaultSubject.trim().startsWith("@")) {
				logger.warning("default subject : '" + defaultSubject + "' is not a valid subject.");
				defaultSubject = "";
			}
		} else {
			defaultSubject = "";
		}
		return defaultSubject;
	}

	/**
	 * config the device. device config strucure : device.[device code].[config]
	 * sample : device.phone = iphone device.phone = htc
	 * device.phone.pointer-device = false
	 * 
	 * @return
	 */
	public Map<String, String> getDevices() {
		if (devices == null) {
			devices = new HashMap<String, String>();
			Iterator keys = properties.getKeys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				if (key.startsWith("device.")) {
					String patternString = properties.getString(key);
					key = key.replaceFirst("device.", "");
					devices.put(key, patternString);
				}
			}
		}
		return devices;
	}

	public String getDynamicContentPage() {
		return properties.getString("mailing.dynamic-content-path", "/mailing/dynamic");
	}

	public String[] getEditTemplate() {
		String templateRaw = properties.getString("admin.edit-template", "javlo");
		String[] templates = StringHelper.split(templateRaw, ",");
		return templates;
	}

	public Map<String, User> getEditUsers() {
		/*
		 * System.out.println("*** edit user : "); for (User user :
		 * editUsers.values()) { System.out.println("* user : "+user); }
		 */
		return editUsers;
	}

	public String getEHCacheConfigFile() {
		String path = properties.getString("ehcacheconfig-file", null);
		if (path == null) {
			return null;
		}

		path = replaceFolderVariable(path);

		if (isDataFolderRelative()) {
			path = application.getRealPath(path);
		}
		return path;
	}

	public String getEnv() {
		return properties.getString("deploy.env", "prod");
	}
	
	public boolean isProd() {
		return getEnv().equalsIgnoreCase("prod");
	}
	
	public boolean testInstance() {
		return getEnv().equals("dev") || getEnv().equals("local");
	}
	
	/* mailing */

	public String getErrorMailReport() {
		return properties.getString("error.email", null);
	}

	public String getFileFolder() {
		return ElementaryURLHelper.mergePath(getStaticFolder(), properties.getString("file-folder", "files"));
	}

	public String getImageCacheFolder() {
		return properties.getString("image-cache-folder", FileCache.BASE_DIR);
	}

	public String getFlashFolder() {
		return ElementaryURLHelper.mergePath(getStaticFolder(), properties.getString("flash-folder", "flash"));
	}

	public String getFooterMessage(String lang) {
		String msg = properties.getString("message.footer-" + lang, null);
		if (msg != null) {
			return msg;
		}
		return properties.getString("message.footer", "");
	}

	public String getGalleryFolder() {
		return ElementaryURLHelper.mergePath(getStaticFolder(), properties.getString("gallery-folder", "galleries"));
	}

	public String getHeaderMessage(String lang) {
		String msg = properties.getString("message.header-" + lang, null);
		if (msg != null) {
			return msg;
		}
		return properties.getString("message.header", "");
	}

	public String getI18nEditFile() {
		String file = replaceFolderVariable(properties.getString("i18n.file.edit", "/WEB-INF/i18n/edit_"));
		if (isI18nFileRelative() && application != null) {
			file = application.getRealPath(file);
		}
		return file;
	}

	/* config values */

	public String getI18nSpecificEditFile() {
		String file = replaceFolderVariable(properties.getString("i18n.file.specific-edit", "/WEB-INF/i18n/specific_edit_"));
		if (isI18nFileRelative() && application != null) {
			file = application.getRealPath(file);
		}
		return file;
	}

	public String getI18nSpecificViewFile() {
		String file = replaceFolderVariable(properties.getString("i18n.file.specific-view", "/WEB-INF/i18n/specific_view_"));
		if (isI18nFileRelative() && application != null) {
			file = application.getRealPath(file);
		}
		return file;
	}

	public String getI18nViewFile() {
		String file = replaceFolderVariable(properties.getString("i18n.file.view", "/WEB-INF/i18n/view_"));
		if (isI18nFileRelative() && application != null) {
			file = application.getRealPath(file);
		}
		return file;
	}

	public String getImageFolder() {
		return ElementaryURLHelper.mergePath(getStaticFolder(), getImageFolderName());
	}

	public String getImageFolderName() {
		return properties.getString("image-folder", "images");
	}

	public String getVideoFolder() {
		return ElementaryURLHelper.mergePath(getStaticFolder(), properties.getString("video-folder", "videos"));
	}

	public String getAvatarFolder() {
		return ElementaryURLHelper.mergePath(getStaticFolder(), getAvatarFolderName());
	}

	public String getAvatarFolderName() {
		return properties.getString("avatar-folder", "avatars");
	}

	public String getIMHelpURI(String lang) {
		String uri = properties.getString("help.uri.im", null);
		if (uri != null) {
			uri = uri.replaceAll("\\[lg\\]", lang);
		}
		return uri;
	}

	public String getInstanceId() {
		return properties.getString("debug.id", "not debug id defined");
	}

	public int getLastAccessPage() {
		return properties.getInt("last-access.page", 60);
	}

	public int getLastAccessStatic() {
		return properties.getInt("last-access.static", 60);
	}

	public String getLDAPInitalContextFactory() {
		return properties.getString("ldap.initial-context-factory", "com.sun.jndi.ldap.LdapCtxFactory");
	}

	public String getLDAPProviderURL() {
		return properties.getString("ldap.provider-url", "ldap://ldappedvds.ep.parl.union.eu:6565/");
	}

	public String getLDAPSecurityAuthentification() {
		return properties.getString("ldap.security.authentification", "simple");
	}

	public String getLDAPSecurityCredentials() {
		return properties.getString("ldap.security.credentials", "RO-eCARD2010");
	}

	public String getLDAPSecurityPrincipal() {
		return properties.getString("ldap.security.principal", "uid=RO-eCARD, ou=Applications, dc=parl, dc=union, dc=eu");
	}

	public String getLocalMailingFolder() {
		String outMailingFolder = properties.getString("mailing.folder", "/mailing/todo");
		outMailingFolder = replaceFolderVariable(outMailingFolder);
		return outMailingFolder;
	}

	public String getLocalMailingHistoryFolder() {
		String outMailingFolder = properties.getString("mailing-history.folder", "/mailing/old");
		outMailingFolder = replaceFolderVariable(outMailingFolder);
		return outMailingFolder;
	}

	public String getLocalMailingTemplateFolder() {
		String path = properties.getString("mailing-template-folder", "/mailing-template");
		path = replaceFolderVariable(path);
		return path;
	}

	public String getLocalShareDataFolder() {
		String path = properties.getString("share-folder", "/WEB-INF/share-files");
		path = replaceFolderVariable(path);
		return path;
	}
	
	public boolean isCreateContentOnImportImage() {
		return properties.getBoolean("import.image.content", false);
	}

	public String getLocalTempDir() {
		String path = properties.getString("temp-folder", null);
		if (path != null) {
			path = replaceFolderVariable(path);
		}
		return path;
	}

	public String getLocalTemplateFolder() {
		String path = properties.getString("template-folder", "/template");
		return path;
	}

	public String getLocalTemplatePluginFolder() {
		String path = properties.getString("template-plugin-folder", "/template-plugin");
		path = replaceFolderVariable(path);
		return path;
	}

	public String getLocalThreadFolder() {
		String path = properties.getString("thread-folder", "/WEB-INF/thread");
		path = replaceFolderVariable(path);
		return path;
	}

	public Level getLoginLogLevel() {
		try {
			return Level.parse(properties.getString("log.login.level", "INFO"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return Level.INFO;
		}
	}

	public String getMacroHelpURI(String lang) {
		String uri = properties.getString("help.uri.macro", null);
		if (uri != null) {
			uri = uri.replaceAll("\\[lg\\]", lang);
		}
		return uri;
	}

	public String getMailingFolder() {
		String outMailingFolder = getLocalMailingFolder();
		if (isDataFolderRelative()) {
			outMailingFolder = application.getRealPath(outMailingFolder);
		}
		return outMailingFolder;
	}

	public String getMailingHistoryFolder() {
		String outMailingFolder = getLocalMailingHistoryFolder();
		if (isDataFolderRelative()) {
			outMailingFolder = application.getRealPath(outMailingFolder);
		}
		return outMailingFolder;
	}

	public String getMailingTemplateFolder() {
		if (isDataFolderRelative()) {
			return application.getRealPath(getLocalMailingTemplateFolder());
		} else {
			return getLocalMailingTemplateFolder();
		}
	}

	public int getMaxMenuTitleSize() {
		return properties.getInt("menu.title-size", 30);
	}

	public String getMenuEditHelpURI(String lang) {
		String uri = properties.getString("help.uri.menu-edit", null);
		if (uri != null) {
			uri = uri.replaceAll("\\[lg\\]", lang);
		}
		return uri;
	}

	public long getMinFreeSpaceOnDataFolder() {
		return properties.getLong("system.min-free-space.data", 1024L * 1024L * 10L); // 1
																						// Giga
																						// minimum
																						// size
																						// on
																						// the
																						// system
	}

	public Level getNavigationLogLevel() {
		try {
			return Level.parse(properties.getString("log.navigation.level", "INFO"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return Level.INFO;
		}

	}

	public String getPagePropertiesHelpURI(String lang) {
		String uri = properties.getString("help.uri.page-properties", null);
		if (uri != null) {
			uri = uri.replaceAll("\\[lg\\]", lang);
		}
		return uri;
	}

	public String getPersistanceHelpURI(String lang) {
		String uri = properties.getString("help.uri.persitance", null);
		if (uri != null) {
			uri = uri.replaceAll("\\[lg\\]", lang);
		}
		return uri;
	}

	public String getProductVersion() {
		return AccessServlet.VERSION;
	}

	/**
	 * @return a newly created Properties object
	 */
	@SuppressWarnings("unchecked")
	public Properties getProperties() {
		Properties result = new Properties();
		Iterator<String> iter = properties.getKeys();
		while (iter.hasNext()) {
			String key = iter.next();
			result.put(key, properties.getProperty(key));
		}
		return result;
	}

	public String getProxyHost() {
		return properties.getString("proxy.host", getDefaultProxyHost());
	}

	public int getProxyPort() {
		return properties.getInt("proxy.port", getDefaultProxyPort());
	}

	public int getPublishLoadingDepth() {
		return properties.getInt("publish.loading.depth", 2);
	}

	public int getCaptchaSize() {
		return properties.getInt("security.captcha.size", 4);
	}

	public String getRealPath(String path) {
		return application.getRealPath(path);
	}

	public String getSecretKey() {
		return properties.getString("security.secret-key", "fju43l7m");
	}

	public String getShareDataFolder() {
		String folder = getLocalShareDataFolder();
		if (isDataFolderRelative()) {
			folder = application.getRealPath(folder);
		}
		File file = new File(folder);
		if (!file.exists()) {
			file.mkdirs();
		}
		return folder;
	}

	public String getShareDataFolderKey() {
		return properties.getString("share-folder-key", "___share-files___");
	}

	public String getShareImageFolder() {
		return properties.getString("share-image-folder", "images");
	}

	public String getSMTPHost() {
		return properties.getString(SMTP_HOST_PARAM, null);
	}

	public String getSMTPPasswordParam() {
		return properties.getString(SMTP_PASSWORD_PARAM, null);
	}

	public String getSMTPPort() {
		return properties.getString(SMTP_PORT_PARAM, null);
	}

	public String getSMTPUser() {
		return properties.getString(SMTP_USER_PARAM, null);
	}

	public List<IMacro> getSpecialMacros() {
		List<IMacro> specialMacro = new LinkedList<IMacro>();
		String macroRaw = properties.getString("class.macro");
		if (macroRaw != null) {
			String[] macros = StringHelper.split(macroRaw, ",");
			for (String macro2 : macros) {
				try {
					IMacro macro = (IMacro) Class.forName(macro2).newInstance();
					specialMacro.add(macro);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return specialMacro;
	}

	public String getStaticConfigLocalisation() {
		return staticConfigLocalisation;
	}

	public String getStaticFolder() {
		return properties.getString("static-folder", "static");
	}

	public String getSynchroCode() {
		return properties.getString("synchro-code", "120857013478039430485203984");
	}

	public Level getSynchroLogLevel() {
		try {
			return Level.parse(properties.getString("log.synchro", "INFO"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return Level.INFO;
		}
	}

	public String getTeaserFolder() {
		return ElementaryURLHelper.mergePath(getStaticFolder(), properties.getString("teaser-folder", "teasers"));
	}

	public String getTempDir() {
		if (getLocalTempDir() == null) {
			return null;
		}
		if (isDataFolderRelative()) {
			return application.getRealPath(getLocalTempDir());
		} else {
			return getLocalTempDir();
		}
	}

	public String getTemplateFolder() {
		if (isDataFolderRelative()) {
			return application.getRealPath(getLocalTemplateFolder());
		} else {
			return replaceFolderVariable(getLocalTemplateFolder());
		}
	}

	public String getDefaultTemplateFolder() {
		String path = properties.getString("template-default", "/WEB-INF/config/default-template");
		return application.getRealPath(path);
	}

	public String getTemplatePluginFolder() {
		if (isDataFolderRelative()) {
			return application.getRealPath(getLocalTemplatePluginFolder());
		} else {
			return getLocalTemplatePluginFolder();
		}
	}

	public String getThreadFolder() {
		String threadFolder;
		if (isDataFolderRelative()) {
			threadFolder = application.getRealPath(getLocalThreadFolder());
		} else {
			threadFolder = getLocalThreadFolder();
		}
		File theadFolderFile = new File(threadFolder);
		if (!theadFolderFile.exists()) {
			theadFolderFile.mkdirs();
		}
		return threadFolder;
	}

	public String getTrashContextFolder() {
		String path = properties.getString("trash-context-folder", "/trash-context-folder");
		path = replaceFolderVariable(path);
		if (isDataFolderRelative()) {
			path = application.getRealPath(path);
		}
		return path;
	}

	public String getTrashFolder() {
		String path = properties.getString("trash-folder", "/WEB-INF/.trash");

		path = replaceFolderVariable(path);

		if (isDataFolderRelative()) {
			path = application.getRealPath(path);
		}
		return path;
	}

	public String getUserInfoFile() {
		return properties.getString("userinfo-file", "/users/view/users-list.csv");
	}

	public String getVFSFolder() {
		return ElementaryURLHelper.mergePath(getStaticFolder(), properties.getString("vfs", "vfs"));
	}

	public String getWelcomeMessage(String lang) {
		String msg = properties.getString("message.welcome-" + lang, null);
		if (msg != null) {
			return msg;
		}
		return properties.getString("message.welcome", "");
	}

	public String getWelcomePopupURL(String lg) {
		String url = properties.getString("default-welcome-popup", "");
		url = StringUtils.replace(url, "#lang#", lg);
		return url;
	}

	/**
	 * get the default proxy in the configuration of tomcat
	 */
	private void initDefaultProxy() {
		ProxySelector proxySelector = ProxySelector.getDefault();
		try {
			Proxy proxy = proxySelector.select(new URI("http://www.google.com")).iterator().next();
			if ((proxy != null) && (proxy.address() != null)) {
				String[] proxyArray = proxy.address().toString().split(":");
				if (proxyArray.length == 1) {
					defaultProxyHost = proxyArray[0];
					defaultProxyPort = 80;
				} else {
					defaultProxyHost = proxyArray[0];
					defaultProxyPort = Integer.parseInt(proxyArray[1]);
				}
			} else {
				defaultProxyPort = 0; // no more search default proxy host
			}
		} catch (Throwable e) {
			defaultProxyPort = 0; // no more search default proxy host
			e.printStackTrace();
		}
	}

	public boolean isAutoCreation() {
		if (isHostDefineSite()) { // if host don't define site we can create it
									// automaticely.
			return properties.getBoolean("auto-creation", true);
		} else {
			return false;
		}
	}

	public boolean isCorporate() {
		return properties.getBoolean("admin.corporate", true);
	}

	/**
	 * cancul account size of true. Account size is always -1 if false.
	 * 
	 * @return
	 */
	public boolean isAccountSize() {
		return properties.getBoolean("account.size", true);
	}

	public boolean isDataFolderRelative() {
		return properties.getBoolean("data-folder-relative", true);
	}

	public boolean isDownloadCleanDataFolder() {
		return properties.getBoolean("download.clean-data-folder", false);
	}

	public boolean isDownloadIncludeTracking() {
		return properties.getBoolean("download.include-tracking", false);
	}

	public boolean isHostDefineSite() {
		return properties.getBoolean("url.host-define-site", false);
	}

	public boolean isRandomDataFoder() {
		return properties.getBoolean("data-folder-random", false);
	}

	public boolean isHTMLEditor() {
		return properties.getBoolean("admin.html-editor", true);
	}

	public boolean isI18nFileRelative() {
		return properties.getBoolean("i18n.file.relative", true);
	}

	public boolean isMailingAsContent() {
		return properties.getBoolean("mailing.content", false);
	}

	public boolean isMailingThread() {
		return StringHelper.isTrue(properties.getString("mailing.thread", "true"));
	}
	
	public boolean isNotificationThread() {
		return StringHelper.isTrue(properties.getString("noctification.thread", "true"));
	}

	public boolean isPasswordEncryt() {
		return properties.getBoolean("security.encrypt-password", true);
	}

	public boolean isRequestWrapper() {
		return properties.getBoolean("request-wrapper", true);
	}

	public boolean isTemplateJSP() {
		return properties.getBoolean("security.template-jsp", true);
	}

	public boolean isTracking() {
		return properties.getBoolean("tracking", true);
	}

	public boolean isViewPrefix() {
		return StringHelper.isTrue(properties.getString("url.view"));
	}

	public boolean isURIWithContext() {
		return properties.getBoolean("url.context", true);
	}

	public void reload() {
		synchronized (FILE_NAME) {
			properties.clear();
			try {
				properties.load();
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
			adminUserFactoryClass = null;
			admimUserFactory = null;
			adminUserFactoryClassName = "";
			devices = null;
			excludeContextDomain = null;
		}
	}

	public String replaceFolderVariable(String folder) {
		if (folder != null) {
			folder = folder.replace("$HOME", HOME);
		}
		return folder;
	}

	public void storeAllProperties(String content) throws IOException {
		synchronized (FILE_NAME) {
			File file = new File(properties.getFile().getAbsolutePath());
			FileUtils.writeStringToFile(file, content, ContentContext.CHARACTER_ENCODING);
			properties.clear();
			try {
				properties.load();
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}

			devices = null;

			setChanged();
			notifyObservers();

			logger.info("store and refresh all properties in : " + properties.getFile().getAbsolutePath());
		}
	}

	public boolean useHttps() {
		return properties.getBoolean("security.use-https", false);
	}

	public boolean useLocalFolder() {
		return properties.getBoolean("local-folder.use", false);
	}

	public String getDefaultDateFormat() {
		return properties.getString("default-date-format", "dd/MM/yyyy");
	}

	public String getDefaultJSDateFormat() {
		return properties.getString("default-date-format", "d/M/yy");
	}

	public String getMarketURL() {
		return properties.getString("market.url", "http://market.javlo.org/market/");
	}

	public ServletContext getServletContext() {
		return application;
	}

	public String getMarketServerName() {
		return properties.getString("market.server.name", "javlo.org");
	}

	public String getLogFile() {
		return properties.getString("log.file", "/../../logs/catalina.out");
	}

	public String getSiteEmail() {
		return properties.getString("site.email", "webmaster@javlo.org");
	}
	
	public String getManualErrorEmail() {
		return properties.getString("manual-error.email", getSiteEmail());		
	}


	public String getHelpURL() {
		return properties.getString("site.help-url", "http://www.javlo.org/help/");
	}

	public boolean isDefaultTemplateImported() {
		return properties.getBoolean("template.default-imported", true);
	}

	public boolean useEhCache() {
		return properties.getBoolean("ehcache-active", false);
	}

	public int getShortURLSize() {
		return properties.getInt("url.short.size", 3);
	}

	public List<String> getBasicModules() {
		String basicModules = properties.getString("modules", null);
		if (basicModules == null) {
			return BASIC_MODULES;
		} else {
			return StringHelper.stringToCollection(basicModules, ",");
		}
	}

	public long getTransformingSize() {
		return properties.getLong("transforming.size", 2);
	}

	CacheManager cacheManager = null;

	public CacheManager getEhCacheManager() throws IOException {
		if (useEhCache() && cacheManager == null) {
			File ehCacheFile = null;
			if (getEHCacheConfigFile() == null || !(new File(getEHCacheConfigFile()).exists())) {
				logger.info("load default ehcache config from : " + EHCACHE_FILE);
				ehCacheFile = new File(application.getRealPath(EHCACHE_FILE));
			} else {
				ehCacheFile = new File(getEHCacheConfigFile());
				logger.info("load ehcache config from : " + ehCacheFile);
			}

			if (ehCacheFile != null && ehCacheFile.exists()) {
				cacheManager = CacheManager.newInstance(ehCacheFile.getAbsolutePath());
			} else {
				cacheManager = CacheManager.getInstance();
			}

			for (String name : cacheManager.getCacheNames()) {
				System.out.println("'" + name + "' max item in memory = " + cacheManager.getCache(name).getCacheConfiguration().getMaxElementsInMemory());
			}
			System.out.println("");

		}

		return cacheManager;
	}

	public void shutdown() {
		if (cacheManager != null) {
			cacheManager.shutdown();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		shutdown();
	}

	public String getCmsName() {
		return properties.getString("default-cms-name", "javlo");
	}

	public boolean isFixPreview() {
		return properties.getBoolean("fix-preview", true);
	}
	
	public List<String> getPlatformTypes() {
		return PLATFORMS;
	}
	
	public String getPlatformType() {
		return properties.getString("platform.type", WEB_PLATFORM);
	}
	
	public String getPlatformTitle() {
		return properties.getString("platform.title", "Javlo");
	}
	
	public String get404PageName() {
		return properties.getString("404-name", "404");
	}
	
	/**
	 * if page not found, search a page with the same name and redirect to it.
	 * @return
	 */
	public boolean isRedirectWidthName() {
		return properties.getBoolean("url.redirect-width-name", true);
	}
	
	public String getEditTemplateFolder() {
		return properties.getString("edit-template.folder", "/jsp/edit/template/mandy-lane-premium-flat");
	}
	
	/**
	 * default mode of the edit template (mode is defined in GlobalContext), can be used in template renderer for include special css or js.
	 * preview css is : edit_preview_[mode].css
	 * @return
	 */
	public String getEditTemplateMode() {
		return properties.getString("edit-template.mode", "");
	}

	public String getEditDefaultMimeTypeImage() {
		return properties.getString("edit-template.default-icon", "mimetypes/default.png");
	}

	/**
	 * return time between 2 modification check in second.
	 * default : 5 minutes (300 sec.)
	 * @return
	 */
	public int getTimeBetweenChangeNotification() {
		return Integer.parseInt(properties.getString("time-between-change-notification", ""+5*60)); 
	}
	
	public String getApplicationLogin() {
		return properties.getString("security.application-login", null);
	}
	
	public String getApplicationPassword() {
		return properties.getString("security.application-password", null);
	}
	
	/**
	 * all image uploaded was resize under this max-size
	 * @return
	 */
	public int getImageMaxWidth() {
		return properties.getInt("image.max-width", 0);
	}
	
	public boolean isUndo() {
		return properties.getBoolean("function.undo", false);
	}
	
	public boolean isExcludeContextDomain(String domain) {
		if (excludeContextDomain == null) {
			excludeContextDomain = new HashSet<String>();
			String hostNames = properties.getString("url.domain-no-context", null);
			if (hostNames != null && hostNames.trim().length() > 0) {
				hostNames = hostNames.trim();
				excludeContextDomain.addAll(StringHelper.stringToCollection(hostNames, ","));
			}			
		}
		return excludeContextDomain.contains(domain);
	}

	public String getSearchEngineClassName() {
		return properties.getString("searchengine.class", "org.javlo.search.DefaultSearchEngine").trim();
	}

	public String getSearchEngineLucenePattern() {
		return properties.getString("searchengine.lucene.pattern", "level3:{QUERY}^3 level2:{QUERY}^2 level1:{QUERY}^1").trim();
	}
	
	public String getDropboxAppKey() {
		return properties.getString("dropbox.app-key", null);		
	}

	public String getDropboxAppSecret() {		
		return properties.getString("dropbox.app-secret", null);
	}
	
	public String getPreviewCommandFilePath() {		
		return properties.getString("preview.command-file", "/jsp/preview/command.jsp");
	}
	
	public String getTimeTravelerFilePath() {		
		return properties.getString("preview.timetraveler-file", "/jsp/time-traveler/command.jsp");
	}
	
	public String getCssPreview() {
		return properties.getString("preview.css", "/css/preview/edit_preview.css");
	}
	
	public String getJSPreview() {
		return properties.getString("preview.js", "/js/preview/edit_preview.js");
	}
	
	public String getJSLibPreview() {
		return properties.getString("preview.lib.js", null);		
	}
	
	public Boolean isTracked() {
		return StringHelper.isTrue(properties.getString("tracked", null), true);		
	}

}
