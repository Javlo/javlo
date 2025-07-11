package org.javlo.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.javlo.actions.DefaultEcomLister;
import org.javlo.actions.DefaultGeneralLister;
import org.javlo.actions.IEcomListner;
import org.javlo.actions.IGeneralListner;
import org.javlo.bean.InstallBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.source.TestDataSource;
import org.javlo.helper.*;
import org.javlo.macro.core.IMacro;
import org.javlo.mailing.feedback.DefaultMailingFeedback;
import org.javlo.module.mailing.MailingAction;
import org.javlo.service.ContentService;
import org.javlo.service.pdf.PDFLayout;
import org.javlo.servlet.AccessServlet;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserInfo;
import org.javlo.utils.ConfigurationProperties;
import org.javlo.utils.request.IFirstRequestListner;
import org.javlo.ztatic.FileCache;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaticConfig extends Observable {
	
	public static String ZONE_DATE_TIME = "Europe/Brussels";
	public static String WEB_PLATFORM = "web";
	public static String MAILING_PLATFORM = "mailing";
	public static String OPEN_PLATFORM = "open";
	private static final List<String> PLATFORMS = new LinkedList<String>(Arrays.asList(new String[] { WEB_PLATFORM, MAILING_PLATFORM }));

	protected static Logger logger = Logger.getLogger(StaticConfig.class.getName());

	ConfigurationProperties properties = new ConfigurationProperties();
	private final Map<String, User> editUsers = new HashMap<String, User>();

	public static final String WEBAPP_CONFIG_FILE = "/WEB-INF/config/webapp_config.properties";

	public static final String WEBAPP_CONFIG_INSTALL = "/WEB-INF/config/static-config-install.properties";

	static final String EDIT_USERS_KEY = "edit.users";
	static final String DEFAULT_CREDENTIALS = "admin,0DPiKuNIrrVmD8IUCuw1hQxNqZc="; // admin,admin;

	private static final String FILE_NAME = "static-config.properties";
	private static final String DEFAULT_CONFIG_DIR = "/WEB-INF/config";

	public static final String SMTP_HOST_PARAM = "mail.smtp.host";
	public static final String SMTP_PORT_PARAM = "mail.smtp.port";
	public static final String SMTP_USER_PARAM = "mail.smtp.user";
	public static final String SMTP_PASSWORD_PARAM = "mail.smtp.password";

	private static final String STATIC_CONFIG_RELATIVE_KEY = "static-config.relative";
	private static final String STATIC_CONFIG_KEY = "static-config.directory";

	private static final String KEY = StaticConfig.class.getName();

	private static final String HOME = System.getProperty("user.home");

	public static final String DEMO_SITE = "demo";

	private static String javloHome = null;

	private Set<String> excludeContextDomain = null;

	private Set<String> contentExtension = null;

	private Boolean redirectSecondaryURL = null;

	private List<String> ipMaskList = null;

	private boolean foundFile = false;
	
	private IGeneralListner generalListner;
	
	private IEcomListner ecomListner;

	private static class FolderBean {
		String thread = null;
		String data = null;

		String archiveData = null;
		String context = null;

		String archiveContext = null;
		String share = null;
		String template = null;
		String mailing = null;
		String mailingHistory = null;
		String temp;
	}

	FolderBean folderBean = new FolderBean();

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

	private String sourceRevision;

	private String buildTime;
	
	private Boolean prod = null;

	private Boolean back = null;

	private Class<IUserFactory> adminUserFactoryClass = null;

	private AdminUserFactory admimUserFactory = null;

	private String adminUserFactoryClassName = "";

	private Map<String, String> devices = null;

	private String encryptedFirstPassword = null;

	public static final List<String> BASIC_MODULES = Arrays.asList(new String[] { "admin", "content", "file" });
	private static final String DEFAULT_PDF_LAYOUT = (new PDFLayout()).store();

	public Boolean internetAccess = null;

	public static String getJavloHome() {
		if (javloHome == null) {
			Map<String, String> env = System.getenv();
			for (String envName : env.keySet()) {
				if (envName.equals("JAVLO_HOME")) {
					String folder = env.get(envName);
					if (folder != null) {
						folder = folder.replace("$HOME", HOME);
						folder = folder.replace("~", HOME);
					}
					File file = new File(folder);
					if (!file.exists()) {
						file.mkdirs();
					}
					try {
						javloHome = file.getCanonicalPath();
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}
			if (javloHome == null) {
				javloHome = "";
			}
		}
		if (javloHome.length() == 0) {
			return null;
		} else {
			return javloHome;
		}
	}
	
	private StaticConfig(ServletContext application) {
		init(application);
	}
	
	private void init(ServletContext application) {
		this.application = application;
		try {
			synchronized (FILE_NAME) {
				Properties webappProps = new Properties();
				if (application != null) {
					InputStream in = application.getResourceAsStream(StaticConfig.WEBAPP_CONFIG_FILE);
					try {
						if (in != null) {
							webappProps.load(in);
						} else {
							logger.severe(StaticConfig.WEBAPP_CONFIG_FILE+" not found.");
						}
					} catch (Exception e) {
						logger.severe("error on read : " + StaticConfig.WEBAPP_CONFIG_FILE);
						throw e;
					} finally {
						ResourceHelper.closeResource(in);
					}
				}

				/** LOAD STATIC CONFIG FILE LOCATION * */
				String JAVLO_HOME = getJavloHome();
				if (JAVLO_HOME != null) {
					logger.info("JAVLO_HOME = "+JAVLO_HOME);
					staticConfigLocalisation = JAVLO_HOME;
				} else {
					staticConfigLocalisation = webappProps.getProperty(STATIC_CONFIG_KEY);
				}
				if (application != null) {
					if (staticConfigLocalisation == null || staticConfigLocalisation.trim().length() == 0 || staticConfigLocalisation.contains("${")) {
						staticConfigLocalisation = ResourceHelper.getRealPath(application, DEFAULT_CONFIG_DIR + "/" + FILE_NAME);
					} else {
						staticConfigLocalisation = ElementaryURLHelper.mergePath(staticConfigLocalisation, FILE_NAME);
						boolean staticConfigRelative = Boolean.parseBoolean(webappProps.getProperty(STATIC_CONFIG_RELATIVE_KEY));
						if (staticConfigRelative && !staticConfigLocalisation.contains("$")) {
							staticConfigLocalisation = ResourceHelper.getRealPath(application, staticConfigLocalisation);
						}
					}
				}

				staticConfigLocalisation = replaceFolderVariable(staticConfigLocalisation);
				
				if (staticConfigLocalisation != null) {
					File file = new File(staticConfigLocalisation);
					logger.info("load static config : " + file +" (exist? "+file.exists()+")");
					if (!file.exists()) {
						// if (!file.getParentFile().exists()) {
						// file.getParentFile().mkdirs();
						// }
						// file.createNewFile();
						foundFile = false;
					} else {
						foundFile = true;
						properties.setFile(file);
					}

				}

				/** LOAD GOD USERS * */
				String editUser = webappProps.getProperty(EDIT_USERS_KEY);
				if (StringHelper.isEmpty(editUser) || editUser.startsWith("${")) {
					editUser = properties.getProperty(EDIT_USERS_KEY);
				}
				if (editUser != null) {
					if (editUser.startsWith("${")) {
						editUser = DEFAULT_CREDENTIALS;
					}
					String[] userPasswordList = editUser.split(";");
					for (String element : userPasswordList) {
						try {
							String[] userPassword = element.split(",");
							User user = new User(userPassword[0], userPassword[1]);
							UserInfo ui = new UserInfo();
							ui.setLogin(user.getLogin());
							ui.setPassword(user.getPassword());
							String token = StringHelper.getRandomIdBase64() + StringHelper.getRandomIdBase64();
							ui.setToken(token);
							ui.setEmail(getSiteEmail());
							user.setUserInfo(ui);

							logger.info("add edit user : " + user.getName()+" email:"+ui.getEmail());

							editUsers.put(user.getName(), user);
						} catch (RuntimeException e) {
							logger.severe("the definition of edit users list is not correct.");
						}
					}
				} else {
					logger.severe("no user found for edit.");
				}

				{ // Load product version
					String productVersion = webappProps.getProperty("product.version");
					if (productVersion != null) {
						Pattern parser = Pattern.compile("Rev=(.*) BuildTime=(.*)");
						Matcher m = parser.matcher(productVersion);
						if (m.find()) {
							sourceRevision = m.group(1);
							buildTime = m.group(2);
						} else {
							sourceRevision = productVersion;
							buildTime = null;
						}
					} else {
						productVersion = "? not found  ?";
					}
				}

			}
			if (application != null) {
				application.setAttribute(KEY, this);
			}
			
			if (getZoneDateTime() != null) {
				ZONE_DATE_TIME = getZoneDateTime();
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.WARNING, "static config file location not found (" + staticConfigLocalisation + "), using default location inside webapp", e);
		}

	}

	public String getSourceRevision() {
		return sourceRevision;
	}

	public String getBuildTime() {
		return buildTime;
	}

	public Level getAbstractComponentLogLevel() {
		try {
			return Level.parse(properties.getString("log.component.abstract.level", "INFO"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return Level.INFO;
		}
	}

	public boolean isTimeTracker() {
		return properties.getBoolean("time-tracker", false);
	}
	
	public boolean isPageTrash() {
		return properties.getBoolean("page.trash", true);
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
		if (folderBean.data == null) {
			synchronized (folderBean) {
				folderBean.data = properties.getString("data-folder", "/WEB-INF/data-ctx/");
				folderBean.data = replaceFolderVariable(folderBean.data);
				if (isDataFolderRelative() && application != null) {
					folderBean.data = ResourceHelper.getRealPath(application, folderBean.data);
				}
			}
		}
		return folderBean.data;
	}

	public String getAllDataArchiveFolder() {
		if (folderBean.archiveData == null) {
			synchronized (folderBean) {
				folderBean.archiveData = properties.getString("data-archive-folder", "/WEB-INF/data-ctx.bk/");
				folderBean.archiveData = replaceFolderVariable(folderBean.archiveData);
				if (isDataFolderRelative() && application != null) {
					folderBean.archiveData = ResourceHelper.getRealPath(application, folderBean.archiveData);
				}
			}
		}
		return folderBean.archiveData;
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

	/**
	 * define the max size of the main cache
	 * 
	 * @return
	 */
	public int getCacheMaxSize() {
		return Integer.parseInt(properties.getString("cache.max-size", "10000"));
	}

	/**
	 * return the max time in cache for item
	 * 
	 * @return
	 */
	public int getCacheMaxTime() {
		return Integer.parseInt(properties.getString("cache.max-time", "" + (60 * 60 * 24))); // 1 day default value
	}

	public boolean isPDFCache() {
		return properties.getBoolean("cache.pdf", false);
	}

	public String getContextFolder() {
		if (folderBean.context == null) {
			synchronized (folderBean) {
				folderBean.context = properties.getString("context-folder", "/WEB-INF/context");
				folderBean.context = replaceFolderVariable(folderBean.context);
				if (isDataFolderRelative()) {
					folderBean.context = ResourceHelper.getRealPath(application, folderBean.context);
				}
			}
		}
		return folderBean.context;
	}

	public String getContextArchiveFolder() {
		if (folderBean.archiveContext == null) {
			synchronized (folderBean) {
				folderBean.archiveContext = properties.getString("context-archive-folder", "/WEB-INF/context.bk");
				folderBean.archiveContext = replaceFolderVariable(folderBean.archiveContext);
				if (isDataFolderRelative()) {
					folderBean.archiveContext = ResourceHelper.getRealPath(application, folderBean.archiveContext);
				}
			}
		}
		return folderBean.archiveContext;
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

	public String getDBInternalLogin() {
		return properties.getString("db.internal.login", "sa");
	}

	public String getDBInternalPassword() {
		return properties.getString("db.internal.password", "");
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

	public String getInstanceName() {
		return properties.getString("instance-name", System.getProperty("user.name"));
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
	
	public String getMailTemplateParent() {
		return properties.getString("mail.template.parent", "mail-template");
	}

	/**
	 * config the device. device config strucure : device.[device code].[config]
	 * sample : device.phone = iphone device.phone = htc device.phone.pointer-device
	 * = false
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

	public String[] getEditTemplate() {
		String templateRaw = properties.getString("admin.edit-template", "javlo");
		String[] templates = StringHelper.split(templateRaw, ",");
		return templates;
	}

	public Map<String, User> getEditUsers() {
		/*
		 * System.out.println("*** edit user : "); for (User user : editUsers.values())
		 * { System.out.println("* user : "+user); }
		 */
		return editUsers;
	}

	public String getEnv() {
		return properties.getString("deploy.env", "dev");
	}

	public String getInfra() {
		return properties.getString("deploy.infra", "back");
	}

	public boolean isProd() {
		if (prod == null) {
			prod = getEnv().equalsIgnoreCase("prod");
		}
		return prod;
	}

	public boolean isBack() {
		if (back == null) {
			back = getInfra().equalsIgnoreCase("back");
		}
		return back;
	}

	public boolean isFront() {
		if (prod == null) {
			prod = getEnv().equalsIgnoreCase("front");
		}
		return prod;
	}

	public boolean isImageShortURL() {
		return properties.getBoolean("image.short-url", false);
	}
	
	public boolean isImageAsService() {
		return properties.getBoolean("image.as-service", false);
	}

	public boolean isImageMetaEdition() {
		return properties.getBoolean("image.meta", true);
	}

	public boolean isEditRepeatComponent() {
		return properties.getBoolean("content.edit-repeat", true);
	}

	public boolean isResourceShortURL() {
		return properties.getBoolean("resource.short-url", false);
	}

	public boolean testInstance() {
		return getEnv().equals("dev") || getEnv().equals("local");
	}

	/* mailing */

	public String getErrorMailReport() {
		return properties.getString("error.email", "error@javlo.org");
	}

	public String getFileFolder() {
		return ElementaryURLHelper.mergePath(getStaticFolder(), getFileFolderName());
	}

	public String getFileFolderName() {
		return properties.getString("file-folder", "files");
	}

	public String getVFSFolderName() {
		return properties.getString("file-folder", "VFS");
	}
	
	public long getMailingTimebetweenTwoMailing() {
		return properties.getLong("mailing.time-between-two-mailing.second", 360);
	}

	public long getMailingTimebetweenTwoSend() {
		return properties.getLong("mailing.time-between-two-send.second", 70);
	}

	public String getImageCacheFolder() {
		return replaceFolderVariable(properties.getString("image-cache-folder", FileCache.BASE_DIR));
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
		return ElementaryURLHelper.mergePath(getStaticFolder(), getGalleryFolderName());
	}

	public String getGalleryFolderName() {
		return properties.getString("gallery-folder", "galleries");
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
			file = ResourceHelper.getRealPath(application, file);
		}
		return file;
	}

	/* config values */

	public String getI18nSpecificEditFile() {
		String file = replaceFolderVariable(properties.getString("i18n.file.specific-edit", "/WEB-INF/i18n/specific_edit_"));
		if (isI18nFileRelative() && application != null) {
			file = ResourceHelper.getRealPath(application, file);
		}
		return file;
	}

	public String getI18nSpecificViewFile() {
		String file = replaceFolderVariable(properties.getString("i18n.file.specific-view", "/WEB-INF/i18n/specific_view_"));
		if (isI18nFileRelative() && application != null) {
			file = ResourceHelper.getRealPath(application, file);
		}
		return file;
	}

	public String getI18nViewFile() {
		String file = replaceFolderVariable(properties.getString("i18n.file.view", "/WEB-INF/i18n/view_"));
		if (isI18nFileRelative() && application != null) {
			file = ResourceHelper.getRealPath(application, file);
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
		return properties.getString("ldap.security.credentials", null);
	}

	public String getLDAPSecurityPrincipal() {
		return properties.getString("ldap.security.principal", null);
	}

	/**
	 * use #login# in place of login
	 * 
	 * @return
	 */
	public String getLDAPSecurityLogin(String login) {
		if (StringHelper.isEmpty(login)) {
			return null;
		}
		return properties.getString("ldap.security.login", "").replace("#login#", login);
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

	public String getLocalMailingTrashFolder() {
		String outMailingFolder = properties.getString("mailing-trash.folder", "/mailing/trash");
		outMailingFolder = replaceFolderVariable(outMailingFolder);
		return outMailingFolder;
	}

	public String getLocalMailingTemplateFolder() {
		String path = properties.getString("mailing-template-folder", "/mailing-template");
		path = replaceFolderVariable(path);
		return path;
	}

	public String getLocalShareDataFolder() {
		if (folderBean.share == null) {
			synchronized (folderBean) {
				folderBean.share = properties.getString("local-share-folder", "/static/share-files");
				folderBean.share = replaceFolderVariable(folderBean.share);
			}
		}
		return folderBean.share;
	}
	
	public boolean isRemoveImportOnDeletePage() {
		return properties.getBoolean("import.clear-on-delete", false);
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
		path = replaceFolderVariable(path);
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
		if (folderBean.mailing == null) {
			synchronized (folderBean) {
				folderBean.mailing = getLocalMailingFolder();
				if (isDataFolderRelative()) {
					folderBean.mailing = ResourceHelper.getRealPath(application, folderBean.mailing);
				}
			}
		}
		return folderBean.mailing;
	}

	public String getMailingHistoryFolder() {
		if (folderBean.mailingHistory == null) {
			synchronized (folderBean) {
				folderBean.mailingHistory = getLocalMailingHistoryFolder();
				if (isDataFolderRelative()) {
					folderBean.mailingHistory = ResourceHelper.getRealPath(application, folderBean.mailingHistory);
				}
			}
		}
		return folderBean.mailingHistory;
	}

	public MailingStaticConfig getMailingStaticConfig() {
		return new MailingStaticConfig(this);
	}

	public String getMailingTrashFolder() {
		String outMailingFolder = getLocalMailingTrashFolder();
		if (isDataFolderRelative()) {
			outMailingFolder = ResourceHelper.getRealPath(application, outMailingFolder);
		}
		return outMailingFolder;
	}

	public String getMailingTemplateFolder() {
		if (isDataFolderRelative()) {
			return ResourceHelper.getRealPath(application, getLocalMailingTemplateFolder());
		} else {
			return getLocalMailingTemplateFolder();
		}
	}

	public int getMaxMenuTitleSize() {
		return properties.getInt("menu.title-size", 30);
	}

	public int getMaxURLSize() {
		return properties.getInt("navigation.url.max-size", 140);
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
		return properties.getString("product.version",AccessServlet.VERSION);		
	}
	
	public String getProductName() {
		return properties.getString("product.name",getCmsName());		
	}
	
	public String getProductUrl() {
		return properties.getString("product.url",null);		
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
	
	public String getProperty(String key, String defaultValue) {
		return properties.getString(key, defaultValue);
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

	public boolean isXSSHeader() {
		return properties.getBoolean("security.header.xss", true);
	}

	public boolean isRemoteLogin() {
		return properties.getBoolean("security.remote-login", false);
	}
	
	/**
	 * if true, test the roles of the resource, if current user has'nt right role --> return 403
	 * @return
	 */
	public boolean isResourcesSecured() {
		return properties.getBoolean("security.resources", true);
	}

	public boolean isLoginWithToken() {
		return properties.getBoolean("security.login.token", false);
	}

	public int getMaxErrorLoginByHour() {
		return properties.getInt("security.login.max-error-hour", isHighSecure() ? 10 : 100);
	}

	public String getRealPath(String path) {
		return ResourceHelper.getRealPath(application, path);
	}

	public String getSecretKey() {
		return properties.getString("security.secret-key", "???");
	}

	public String getDefaultPassword() {
		return properties.getString("security.default-password", "changeme");
	}

	/*
	 * public String getShareDataFolder() { String folder =
	 * getLocalShareDataFolder(); if (isDataFolderRelative()) { folder =
	 * application.getRealPath(folder); } File file = new File(folder); if
	 * (!file.exists()) { file.mkdirs(); } return folder; }
	 */

	public String getShareDataFolderKey() {
		return properties.getString("share-folder-key", "___share-files___");
	}

	public String getShareImageFolder() {
		return properties.getString("share-image-folder", "images");
	}

	public String getSharedPixaBayAPIKey() {
		return properties.getString("shared.pixabay.key", null);
	}

	public String getSharedFreepikAPIKey() {
		return properties.getString("shared.freepik.key", null);
	}

	public String getSharedStockvaultAPIKey() {
		return properties.getString("shared.stockvault.key", null);
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
					IMacro macro = (IMacro) Class.forName(macro2.trim()).newInstance();
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

	public String getExternComponentFolder() {
		return properties.getString("exter-component-folder", "components");
	}

	public String getStaticFolder() {
		return properties.getString("static-folder", "static");
	}

	public String getSynchroCode() {
		return properties.getString("synchro-code", null);
	}

	public int getSynchroTokenValidityMinutes() {
		return properties.getInt("synchro-token.validity-minutes", 5);
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

	public File getWebTempDir() {
		String path = properties.getString("temp-web-folder", "/web-tmp");
		return new File(ResourceHelper.getRealPath(application, path));
	}

	public String getTempDir() {
		if (getLocalTempDir() == null) {
			return null;
		}
		if (isDataFolderRelative()) {
			return ResourceHelper.getRealPath(application, getLocalTempDir());
		} else {
			return getLocalTempDir();
		}
	}

	public String getTemplateFolder() {
		if (folderBean.template == null) {
			if (isDataFolderRelative()) {
				folderBean.template = ResourceHelper.getRealPath(application, getLocalTemplateFolder());
			} else {
				folderBean.template = replaceFolderVariable(getLocalTemplateFolder());
			}
		}
		return folderBean.template;
	}

	public String getDefaultTemplateFolder() {
		String path = properties.getString("template-default", "/WEB-INF/config/default-template");
		return ResourceHelper.getRealPath(application, path);
	}

	public String getTemplatePluginFolder() {
		if (isDataFolderRelative()) {
			return ResourceHelper.getRealPath(application, getLocalTemplatePluginFolder());
		} else {
			return getLocalTemplatePluginFolder();
		}
	}

	public String getThreadFolder() {
		if (folderBean.thread == null) {
			synchronized (folderBean) {
				if (isDataFolderRelative()) {
					folderBean.thread = ResourceHelper.getRealPath(application, getLocalThreadFolder());
				} else {
					folderBean.thread = getLocalThreadFolder();
				}
				File theadFolderFile = new File(folderBean.thread);
				if (!theadFolderFile.exists()) {
					theadFolderFile.mkdirs();
				}
			}
		}
		return folderBean.thread;
	}

	public String getTrashContextFolder() {
		String path = properties.getString("trash-context-folder", "/trash-context-folder");
		path = replaceFolderVariable(path);
		if (isDataFolderRelative()) {
			path = ResourceHelper.getRealPath(application, path);
		}
		return path;
	}

	public String getTrashFolder() {
		String path = properties.getString("trash-folder", "/WEB-INF/.trash");

		path = replaceFolderVariable(path);

		if (isDataFolderRelative()) {
			path = ResourceHelper.getRealPath(application, path);
		}
		return path;
	}

	public String getUserFolder() {
		return "users";
	}

	public String getUserInfoFile() {
		return properties.getString("userinfo-file", "/" + getUserFolder() + "/view/users-list.csv");
	}

	public String getAdminUserInfoFile() {
		return properties.getString("adminuserinfo-file", "/" + getUserFolder() + "/admin/edit-users-list.csv");
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

	public String getDefaultTestURL() {
		return properties.getString("url.test", "http://www.wikipedia.org/");
	}

	public boolean isInternetAccess() {
		if (internetAccess == null) {
			try {
				internetAccess = NetHelper.isURLValid(new URL(getDefaultTestURL()));
			} catch (MalformedURLException e) {
				logger.severe("bad url format : " + getDefaultTestURL());
			}
		}
		return internetAccess;
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
		if (isHostDefineSite()) {
			return properties.getBoolean("auto-creation", !isHighSecure());
		} else {
			return properties.getBoolean("auto-creation", false);
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
		return properties.getBoolean("account.size", false);
	}

	public boolean isAllUrlRedirect() {
		return properties.getBoolean("url.all-redirect", false);
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

	public boolean isStorageZipped() {
		return properties.getBoolean("persistence.zip", false);
	}
	
	public boolean isSecureEncrypt() {
		return properties.getBoolean("security.encrypt", false);
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

	public boolean isMailingWidthUserInfo() {
		return properties.getBoolean("mailing.users", true);
	}

	public boolean isMailingUserTracking() {
		return properties.getBoolean("mailing.tracking", true);
	}

	public boolean isMailingThread() {
		return StringHelper.isTrue(properties.getString("mailing.thread"), true);
	}

	public boolean isStaticInfoDescription() {
		return StringHelper.isTrue(properties.getString("static-info.description"), true);
	}

	public boolean isNotificationThread() {
		return StringHelper.isTrue(properties.getString("noctification.thread"), false);
	}

	public int getNotificationHours() {
		return properties.getInt("noctification.hours", 9);
	}

	public boolean isNotificationDyanamicComponentThread() {
		return StringHelper.isTrue(properties.getString("noctification-dynamic-component.thread"), false);
	}

	public String getPasswordEncrytClass() {
		return properties.getString("security.encrypt-password.class", "org.javlo.security.password.SHAEncryt");
	}

	public boolean isFirstPasswordMustBeChanged() {
		return properties.getBoolean("security.change-password", true);
	}
	
	public boolean isOauthView() {
		return properties.getBoolean("security.oauth.view", false);
	}

	public String getOauthGoogleIdClient() {
		return properties.getString("security.oauth.google-id-client", null);
	}

	public String getOauthGoogleSecret() {
		return properties.getString("security.oauth.google-secret", null);
	}

	public String getFirstPasswordEncryptedIfNeeded() {
		if (encryptedFirstPassword == null) {
			encryptedFirstPassword = SecurityHelper.encryptPassword(encryptedFirstPassword);
		}
		return encryptedFirstPassword;
	}

	public String getFirstPassword() {
		return properties.getString("security.first-password", "changeme");
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
			foundFile = properties.getFile().exists();
			folderBean = new FolderBean();
			try {
				properties.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
			clearCache();
		}
	}

	public void clearCache() {
		redirectSecondaryURL = null;
		adminUserFactoryClass = null;
		admimUserFactory = null;
		adminUserFactoryClassName = "";
		devices = null;
		excludeContextDomain = null;
		encryptedFirstPassword = null;
		contentExtension = null;
		ipMaskList = null;
		internetAccess = null;
		prod = null;
	}

	public static String replaceFolderVariable(String folder) {
		if (folder != null) {
			folder = folder.replace("$HOME", HOME);
			folder = folder.replace("~", HOME);
		}
		String JAVLO_HOME = getJavloHome();
		if (JAVLO_HOME != null) {
			folder = folder.replace("$JAVLO_HOME", JAVLO_HOME);
		}
		String CATALINA_HOME = System.getenv("CATALINA_HOME");
		if (CATALINA_HOME != null) {
			folder = folder.replace("$CATALINA_HOME", CATALINA_HOME);
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
			} catch (IOException e) {
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

	public ServletContext getServletContext() {
		return application;
	}

	public String getMarketServerName() {
		return properties.getString("market.server.name", "javlo.org");
	}

	/**
	 * log file, if retrun null, choose the latest modified file of log dir
	 * @return
	 */
	public String getLogFile() {
		return properties.getString("log.file", null);
	}
	
	public String getLogDir() {
		return properties.getString("log.dir", "/../../logs/");
	}

	public String getSiteEmail() {
		String email = properties.getString("site.email", "webmaster@javlo.org");
		if (!StringHelper.isMail(email)) {
			logger.warning("bad email in static config : " + email);
		}
		return email;
	}

	public String getManualErrorEmail() {
		return properties.getString("manual-error.email", getSiteEmail());
	}

	public String getHelpURL() {
		return properties.getString("site.help-url", "/help/${language}");
	}

	public boolean isDefaultTemplateImported() {
		return properties.getBoolean("template.default-imported", true);
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
		return properties.getLong("transforming.size", 8);
	}

	public void shutdown() {
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
	 * 
	 * @return
	 */
	public boolean isRedirectWidthName() {
		return properties.getBoolean("url.redirect-width-name", false);
	}

	public String getEditTemplateFolder() {
		return properties.getString("edit-template.folder", "/jsp/edit/template/mandy-lane-premium-material");
	}

	/**
	 * default mode of the edit template (mode is defined in GlobalContext), can be
	 * used in template renderer for include special css or js. preview css is :
	 * edit_preview_[mode].css
	 * 
	 * @return
	 */
	public String getEditTemplateMode() {
		return properties.getString("edit-template.mode", "");
	}

	public String getEditDefaultMimeTypeImage() {
		return properties.getString("edit-template.default-icon", "mimetypes/default.png");
	}

	/**
	 * return time between 2 modification check in second. default : 3 minutes
	 * 
	 * @return
	 */
	public int getTimeBetweenChangeNotification() {
		return Integer.parseInt(properties.getString("time-between-change-notification", "" + 3 * 60));
	}

	/**
	 * return time between 2 modification check of dynamic component in second.
	 * default : 10 minutes
	 * 
	 * @return
	 */
	public int getTimeBetweenChangeNotificationForDynamicComponent() {
		return Integer.parseInt(properties.getString("time-between-change-notification", "" + 10 * 60));
	}

	public String getApplicationLogin() {
		return properties.getString("security.application-login", null);
	}

	public String getApplicationPassword() {
		return properties.getString("security.application-password", null);
	}

	public String getPasswordRegularExpression() {
		return properties.getString("security.password.regular-expression", "...+");
	}

	/**
	 * all image uploaded was resize under this max-size
	 * 
	 * @return
	 */
	public int getImageMaxWidth() {
		return properties.getInt("image.max-width", 0);
	}
	
	public boolean isUndo() {
		return properties.getBoolean("function.undo", true);
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
		return properties.getString("searchengine.class", "org.javlo.search.LuceneSearchEngine").trim();
	}

	public String getSearchEngineLucenePattern() {
		return properties.getString("searchengine.lucene.pattern", "level3:{QUERY}^3 level2:{QUERY}^2 level1:{QUERY}^1").trim();
	}

	public String getDropboxAppKey() {
		return properties.getString("dropbox.app-key", null);
	}

	public String getHtmlHead() {
		if (isHighSecure()) {
			return null;
		}
		return properties.getString("html.head", null);
	}

	public String getDropboxAppSecret() {
		return properties.getString("dropbox.app-secret", null);
	}

	public String getPreviewCommandFilePath() {
		return properties.getString("preview.command-file", "/jsp/preview/command_bootstrap.jsp");
	}

	public String getTimeTravelerFilePath() {
		return properties.getString("preview.timetraveler-file", "/jsp/time-traveler/bootstrap/command.jsp");
	}

	public String getCssPreview() {
		return properties.getString("preview.css", "/jsp/preview/css/bootstrap/main.css");
	}

	public String getJSPreview() {
		return properties.getString("preview.js", "/jsp/preview/js/bootstrap/preview.js");
	}

	public String getHTML2Canvas() {
		return properties.getString("html2Canvas.js", "/jsp/preview/js/lib/html2canvas.min.js");
	}

	public String getJSBootStrap() {
		return properties.getString("preview.bootstrap.js", "/jsp/preview/js/bootstrap/bootstrap.all.js");
	}

	public String getStaticResourceCacheTime() {
		return properties.getString("resources.cache-time", "" + (60 * 60 * 24));
	}

	public boolean isContentExtensionValid(String ext) {
		if (ext == null) {
			return false;
		} else {
			if (contentExtension == null) {
				String rawExtension = properties.getString("content.extension", "html,pdf,png,jpg,xml,cxml,zip,eml");
				if (rawExtension.trim().length() == 0 || rawExtension.trim().equals("*")) {
					contentExtension = Collections.EMPTY_SET;
				} else {
					contentExtension = new HashSet<String>(StringHelper.stringToCollection(rawExtension, ","));
				}
			}
			if (contentExtension.size() == 0) {
				return true;
			} else {
				return contentExtension.contains(ext.toLowerCase());
			}
		}
	}

	public String getDefaultContentExtension() {
		return properties.getString("content.default-extension", "html");
	}

	public boolean isCheckContentIntegrity() {
		return properties.getBoolean("content.check-integrity", false);
	}

	public boolean isDisplayIntegrity() {
		return properties.getBoolean("content.display-integrity", false);
	}

	public String getJSLibPreview() {
		return properties.getString("preview.lib.js", "/jsp/preview/js/preview_jquery-1.11.2.min.js");
	}

	public Boolean isTracked() {
		return StringHelper.isTrue(properties.getString("tracked", null), true);
	}

	public String getDefaultParentEditableTemplate() {
		return properties.getString("template.editable.parent", "editable");
	}

	public boolean isSharedImportDocument() {
		return StringHelper.isTrue(properties.getString("shared.import-document", null), true);
	}

	public boolean isIntegrityCheck() {
		return StringHelper.isTrue(properties.getString("content.integrity-checker", null), true);
	}

	public boolean isEditOnCreate() {
		return StringHelper.isTrue(properties.getString("content.edit-on-create", null), false);
	}

	public boolean isRestServlet() {
		return StringHelper.isTrue(properties.getString("security.rest-servlet", null), !isHighSecure());
	}

	public boolean isHighSecure() {
		return StringHelper.isTrue(properties.getString("security.high", null), false);
	}

	public String getSpecialLogFile() {
		return properties.getString("debug.special-file", "/tmp/javlo.log");
	}

	public Integer getUndoDepth() {
		return properties.getInteger("content.undo-depth");
	}

	public String getImportFolder() {
		return properties.getString("import", "/import");
	}

	public String getImportImageFolder() {
		return properties.getString("import.image", "/images" + getImportFolder());
	}

	public String getImportGalleryFolder() {
		return properties.getString("import.gallery", getGalleryFolderName() + getImportFolder());
	}

	public String getImportResourceFolder() {
		return properties.getString("import.resource", "/files" + getImportFolder());
	}

	public String getImportVFSFolder() {
		return properties.getString("import.vfs", "/vfs" + getImportFolder());
	}

	public long getSiteMapSizeLimit() {
		return properties.getLong("sitemap.maxsite", 1024 * 1024 * 10);
	}

	public boolean isAutoFocus() {
		return StringHelper.isTrue(properties.getProperty("image.auto-focus"), true);
	}

	public long getSiteMapNewsLimit() {
		return properties.getLong("sitemap.news-days", 2);
	}

	public String getImageFormat() {
		return properties.getString("content.image-format", "png,jpg,jpeg,gif,svg,webp");
	}

	public String getVideoFormat() {
		return properties.getString("content.video-format", "mp4");
	}

	/**
	 * return true if javlo must be forward to the canonical URL. WARNING : always
	 * false if isJsessionID return false
	 * 
	 * @return
	 */
	public boolean isRedirectSecondaryURL() {
		if (redirectSecondaryURL == null) {
			redirectSecondaryURL = properties.getBoolean("url.redirect-secondary-url", true);
		}
		return redirectSecondaryURL && !isJsessionID();
	}

	public boolean isJsessionID() {
		return properties.getBoolean("url.jsessionid", false);
	}

	public boolean isComponentsFiltered() {
		return properties.getBoolean("components.filtered", true);
	}

	public List<String> getDocumentExtension() {
		return StringHelper.stringToCollection(properties.getString("content.document-format", getImageFormat() + ',' + properties.getString("content.document-format", "mp3,wav,m4a,aif,aiff,aifc") + ",doc,docx,svg,odf,xls,xlsx,pdf,xml,zip,ppt,pptx,pub,eml,osd,odt,vcard,ppsx,sdw,mp4,mp3,avi,wpt,odm,mov,url,ept,stw,sdd,sds,odc,fax,vdx,wpa,ppv,sgf,wp5,xtd,psd,rar,html,htm"), ",");
	}

	public List<String> getSoundExtension() {
		return StringHelper.stringToCollection(properties.getString("content.sound-format", "mp3,wav,m4a,aif,aiff,aifc"), ",");
	}

	public boolean isEditIpSecurity() {
		return StringHelper.isTrue(properties.getString("security.ip.edit"), false);
	}

	public List<String> getIPMasks() {
		if (ipMaskList == null) {
			String accepIP = properties.getString("security.ip.ranges");
			if (!StringHelper.isEmpty(accepIP)) {
				ipMaskList = StringHelper.stringToCollection(accepIP, ";");
			} else {
				ipMaskList = Collections.EMPTY_LIST;
			}
		}
		return ipMaskList;
	}

	public boolean isConvertHTMLToImage() {
		return StringHelper.isTrue(properties.getString("content.image-from-html"), false);
	}

	public boolean isIM() {
		return StringHelper.isTrue(properties.getString("interface.im"), false);
	}

	public boolean isSiteLog() {
		return StringHelper.isTrue(properties.getString("site.log.active"), false);
	}
	
	public boolean isSiteLogStorage() {
		return StringHelper.isTrue(properties.getString("site.log.store"), false);
	}

	public boolean isSiteLogCaller() {
		return StringHelper.isTrue(properties.getString("site.log.caller"), false);
	}

	public String getSiteLogGroup() {
		return properties.getString("site.log.group");
	}

	public boolean isAddButton() {
		return properties.getBoolean("preview.add-button", true);
	}

	public boolean isRestServer() {
		return properties.getBoolean("security.rest-server", false);
	}

	public boolean isAnonymisedTracking() {
		return properties.getBoolean("security.tracking.anonymised", false);
	}

	public boolean isCompressJsp() {
		return properties.getBoolean("deploy.compress-jsp", false);
	}

	public boolean isMobilePreview() {
		return properties.getBoolean("mobile.preview", true);
	}

	public List<String> getPreviewLayout() {
		return StringHelper.stringToCollection(properties.getString("preview.layout", "light,dark,pink,sky,sea"), ",");
	}

	public int getBackupInterval() {
		String interval = properties.getProperty("backup.interval");
		if (!StringHelper.isDigit(interval)) {
			return 0;
		} else {
			return Integer.parseInt(interval);
		}
	}

	public int getDbBackupCount() {
		String backupCount = properties.getProperty("backup.count.db");
		if (!StringHelper.isDigit(backupCount)) {
			return 7;
		} else {
			return Integer.parseInt(backupCount);
		}
	}

	public int getDbBackupInterval() {
		String backupCount = properties.getProperty("backup.interval.db");
		if (!StringHelper.isDigit(backupCount)) {
			return getBackupInterval();
		} else {
			return Integer.parseInt(backupCount);
		}
	}

	public int getUsersBackupCount() {
		String backupCount = properties.getProperty("backup.count.users");
		if (!StringHelper.isDigit(backupCount)) {
			return 120;
		} else {
			return Integer.parseInt(backupCount);
		}
	}

	public int getUsersBackupInterval() {
		String backupCount = properties.getProperty("backup.interval.users");
		if (!StringHelper.isDigit(backupCount)) {
			return 60 * 60 * 2;
		} else {
			return Integer.parseInt(backupCount);
		}
	}
	
	public String getMailingFeedBackURI() {
		String classRaw = properties.getProperty("mailing.feedback.uri");
		if (StringHelper.isEmpty(classRaw)) {
			return "/mfb/mfb.png?"+MailingAction.MAILING_FEEDBACK_PARAM_NAME+'='+MailingAction.MAILING_FEEDBACK_VALUE_NAME;
		} else {
			return classRaw;
		}
	}
	
	public List<String> getMailingFeedbackClass() {
		String classRaw = properties.getProperty("mailing.feedback.class");
		if (StringHelper.isEmpty(classRaw)) {
			return Arrays.asList( new String[] {DefaultMailingFeedback.class.getCanonicalName()});
		} else {
			return StringHelper.stringToCollection(classRaw, ",");
		}
	}

	public int getFormsBackupCount() {
		String backupCount = properties.getProperty("backup.count.forms");
		if (!StringHelper.isDigit(backupCount)) {
			return 120;
		} else {
			return Integer.parseInt(backupCount);
		}
	}

	public int getFormsBackupInterval() {
		String backupCount = properties.getProperty("backup.interval.forms");
		if (!StringHelper.isDigit(backupCount)) {
			return 60 * 60 * 24;
		} else {
			return Integer.parseInt(backupCount);
		}
	}

	public boolean isFoundFile() {
		return foundFile;
	}

	public void setFoundFile(boolean foundFile) {
		this.foundFile = foundFile;
	}

	/**
	 * download data form javlo.org
	 * 
	 * @param ctx
	 * @param importTemplate
	 * @param importDemo
	 * @return a ctx to new demo site, null if no demo site imported
	 * @throws Exception
	 */
	public static ContentContext download(ContentContext ctx, InstallBean installBean, boolean importTemplate, boolean importDemo, String email) {
		Properties p = new Properties();
		try {
		InputStream in = new URL("https://javlo.org/resource/static/install/install_info.properties").openStream();
		try {
			p.load(in);
		} finally {
			ResourceHelper.closeResource(in);
		}
		
		if (StringHelper.isMail(email)) {
			try {
				String url = p.getProperty("register.url");
				url = url.replace("#EMAIL#", email);
				NetHelper.readPage(new URL(url));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		} catch (Exception e) {
			e.printStackTrace();
			installBean.setDemoStatus(InstallBean.ERROR);
			installBean.setTemplateStatus(InstallBean.ERROR);
			return null;
		}
		if (importTemplate) {
			try {
				String prefix = p.getProperty("template.url.prefix", null);
				String suffix = p.getProperty("template.url.suffix", null);
				for (String template : StringHelper.stringToCollection(p.getProperty("templates"), ",")) {
					String templateURL = prefix + template + suffix;
					InputStream tplIn = new URL(templateURL).openStream();
					try {
						ZipManagement.uploadZipTemplate(ctx.getGlobalContext().getStaticConfig().getTemplateFolder(), tplIn, template);
					} finally {
						ResourceHelper.closeResource(tplIn);
					}
				}
				installBean.setTemplateStatus(InstallBean.SUCCESS);
			} catch (Exception e) {
				e.printStackTrace();
				installBean.setTemplateStatus(InstallBean.ERROR);
			}
		}
		if (importDemo) {
			try {
				if (!GlobalContext.isExist(ctx.getRequest(), DEMO_SITE)) {
					String contentUrl = p.getProperty("content.url", null);
					String contextUrl = p.getProperty("content.context", null);
					String propertiesRaw = null;
					try {
						propertiesRaw = NetHelper.readPageGet(new URL(contextUrl));
					} catch (Exception e) {
						logger.warning(e.getMessage());
					}
					GlobalContext demoContext = GlobalContext.getInstance(ctx.getRequest().getSession(), DEMO_SITE);
					if (propertiesRaw != null) {
						demoContext.loadExternalProperties(propertiesRaw);
					} else {
						demoContext.setDefaultLanguages("en");
						demoContext.setRAWLanguages("en");
						demoContext.setRAWContentLanguages("en");
					}
					ContentContext demoCtx = new ContentContext(ctx);
					demoCtx.setForceGlobalContext(demoContext);
					InputStream inContent = new URL(contentUrl).openStream();
					try {
						ZipManagement.uploadZipFile(demoCtx, inContent);
					} finally {
						ResourceHelper.closeResource(inContent);
					}
					installBean.setDemoStatus(InstallBean.SUCCESS);
					return demoCtx;
				}
			} catch (Exception e) {
				e.printStackTrace();
				installBean.setDemoStatus(InstallBean.ERROR);
			}
		}
		return null;
	}

	public InstallBean install(ContentContext ctx, String inConfigFolder, String idDataFolder, String adminPassword, boolean importTemplate, boolean importDemo, String email) {
		
		InstallBean outBean = new InstallBean();
		try {
			outBean = new InstallBean();
			idDataFolder = URLHelper.cleanPath(idDataFolder, true);
			
			Properties webappProps = ResourceHelper.loadProperties(new File(application.getRealPath(WEBAPP_CONFIG_FILE)));
			webappProps.setProperty(STATIC_CONFIG_KEY, inConfigFolder);
			webappProps.setProperty("static-config.relative", "false");
			webappProps.setProperty("edit.users", "admin,"+SecurityHelper.encryptPassword(adminPassword));
			inConfigFolder = replaceFolderVariable(inConfigFolder);
			File configFile = new File(URLHelper.mergePath(inConfigFolder, FILE_NAME));
			ResourceHelper.writePropertiesToFile(webappProps, new File(application.getRealPath(WEBAPP_CONFIG_FILE)), "config (install done)");
			if (!configFile.exists()) {
				configFile.getParentFile().mkdirs();
				String configBase = ResourceHelper.loadStringFromFile(new File(application.getRealPath(WEBAPP_CONFIG_INSTALL)));
				configBase = configBase.replace("#DATA#", idDataFolder);
				configBase = configBase.replace("#PASSWORD#", SecurityHelper.encryptPassword(adminPassword));
				configBase = configBase.replace("#SYNCHRO#", StringHelper.getRandomString(32));
				ResourceHelper.writeStringToFile(configFile, configBase);
			}
			properties.setFile(configFile);
			reload();
			TemplateFactory.copyDefaultTemplate(ctx.getRequest().getSession().getServletContext());
			ContentService.clearAllContextCache(ctx);
			if (!GlobalContext.isExist(ctx.getRequest(), getMasterContext())) {
				GlobalContext.getInstance(ctx.getRequest().getSession(), getMasterContext());
			}
			download(ctx, outBean, importTemplate || importDemo, importDemo, email);
		} catch (Exception e) {
			e.printStackTrace();
			outBean.setConfigStatus(InstallBean.ERROR);
		}
		init(ctx.getRequest().getSession().getServletContext());
		return outBean;
	}
	
	public String getSearchPageName() {
		return properties.getString("page.search", "search");
	}
	
	public String getRegisterPageName() {
		return properties.getString("page.register", "register");
	}
	
	public String getLoginPageName() {
		return properties.getString("page.login", "register");
	}
	
	public String getIP2LocationURL() {
		return properties.getString("tracking.url.ip2", null);
	}
	
	public String getDefaultPDFLayout() {
		return properties.getString("pdf.layout", DEFAULT_PDF_LAYOUT);
	}
	
	public String getNewsPageName() {
		return properties.getString("page.news", "news");
	}
	
	public IEcomListner getEcomLister() throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (ecomListner == null) {
			String generalListerClass = properties.getString("ecomlister.class", null);
			if (generalListerClass == null) {
				ecomListner = new DefaultEcomLister();
			} else {
				ecomListner = (IEcomListner) Class.forName(generalListerClass.trim()).newInstance();
			}
		}
		return ecomListner;
	}
	
	public IGeneralListner getGeneralLister() throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (generalListner == null) {
			String generalListerClass = properties.getString("generallister.class", null);
			if (generalListerClass == null) {
				generalListner = new DefaultGeneralLister();
			} else {
				generalListner = (IGeneralListner) Class.forName(generalListerClass.trim()).newInstance();
			}
		}
		return generalListner;
	}

	private Boolean accessLogFolderExist = null;
	private String accessLogFolder = null;

	public String getAccessLogFolder() {
		if (accessLogFolderExist != null && !accessLogFolderExist) {
			return null;
		}
		if (accessLogFolder == null) {
			accessLogFolder = properties.getString("access.log.folder", null);
			accessLogFolderExist = accessLogFolder != null;
			if (accessLogFolderExist) {
				new File(accessLogFolder).mkdirs();
			}
		}
		return accessLogFolder;
	}
	
	public String getWebpEncoder() {
		return properties.getString("image.webp.encoder", null);
	}
	
	public String getZoneDateTime() {
		return properties.getString("calendar.zone", null);
	}
	
	public String getLocaleCountry() {
		return properties.getString("locale.country", "be");
	}
	
	public int getLayerCacheMaxSize() {
		return properties.getInt("image.layer.cache.size", 6);
	}
	
	public IFirstRequestListner getFirstRequestLister() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String firstRequestClass = properties.getString("listner.first-request", null);
		logger.info("first request lister : "+firstRequestClass);
		if (firstRequestClass == null) {
			return null;
		} else {
			Class c = Class.forName(firstRequestClass);
			return (IFirstRequestListner)c.newInstance();
		}
	}
	
}
