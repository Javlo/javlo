package org.javlo.module.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.service.ContentService;
import org.javlo.template.TemplateData;
import org.javlo.utils.ListMapValueValue;

//TODO: remove off attribute and replace with call to globalContext
public class GlobalContextBean {
	
	private GlobalContext globalContext;

	public static final class SortOnKey implements Comparator<GlobalContextBean> {
		@Override
		public int compare(GlobalContextBean o1, GlobalContextBean o2) {
			return o1.getKey().compareTo(o2.getKey());
		}
	}

	private String key;
	private String administrator;
	private String aliasOf;
	private boolean aliasActive;
	private List<GlobalContextBean> alias = new LinkedList<GlobalContextBean>();
	private String creationDate;
	private String latestLoginDate;
	private String defaultTemplate;
	private String globalTitle;
	private String defaultLanguage;
	private String defaultLanguages;
	private String languages;
	private String contentLanguages;
	private String size;
	private String folder;
	private String usersAccess;
	private String googleAnalyticsUACCT;
	private String googleApiKey;
	private String tags;
	private String blockPassword;
	private String homepage;
	private String urlFactory;
	private String userRoles;
	private String adminUserRoles;
	private String proxyPathPrefix;
	private boolean autoSwitchToDefaultLanguage;
	private boolean extendMenu;
	private boolean previewMode;
	private boolean openExternalLinkAsPopup = false;
	private boolean openFileAsPopup = false;
	private boolean wizz = false;
	private boolean onlyCreatorModify = false;
	private boolean collaborativeMode = false;
	private boolean portail;
	private boolean componentsFiltered;
	private String noPopupDomain;
	private String URIAlias;
	private boolean master = false;
	private String forcedHost = "";
	private String editTemplateMode = null;
	private String DMZServerInter = "";
	private String DMZServerIntra = "";		
	private String platformType = StaticConfig.WEB_PLATFORM;

	private String shortDateFormat;
	private String mediumDateFormat;
	private String fullDateFormat;

	private String helpURL;
	private String mainHelpURL;
	private String privateHelpURL;

	private int countUser;
	private boolean view;
	private boolean edit;
	private boolean visibility;
	private boolean editability;
	private String userFactoryClassName = "";
	private String adminUserFactoryClassName = "";
	
	private String mailingSenders = "";
	private String mailingSubject = "";
	private String mailingReport = "";
	private String unsubscribeLink = "";
	
	private String pophost;
	private int popport;
	private String popuser;
	private String poppassword;
	private boolean popssl;
	private String smtphost;
	private String smtpport;
	private String smtpuser;
	private String smtppassword;
	private String dkimDomain;
	private String dkimSelector;
	
	private String metaBloc;
	private String headBloc;
	private String footerBloc;		
	
	private boolean forcedHttps = false;
	private boolean cookies = false;
	private String cookiesPolicyUrl = null;
	
	private String specialConfig = "";
	private boolean screenshot = false;
	private String screenshotUrl = null;
	
	private boolean reversedlink;
	private boolean backupThread;
	
	private String ownerName;
	private String ownerContact;
	private String ownerAddress;
	private String ownerPostcode;
	private String ownerCity;
	private String ownerNumber;
	private String ownerPhone;
	private String ownerEmail;
	private String ownerFacebook;
	private String ownerTwitter;
	private String ownerInstagram;
	private String ownerLinkedin;
	
	private String securityCsp;
	
	private TemplateData templateData = null;
	
	private List<String> quietArea = Collections.EMPTY_LIST;

	public GlobalContextBean(ContentContext ctx, GlobalContext globalContext, HttpSession session) throws NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (globalContext == null) {
			return;
		}
		this.globalContext = globalContext;
		setKey(globalContext.getContextKey());
		setFolder(globalContext.getFolder());
		setAdministrator(globalContext.getAdministrator());
		setAliasOf(globalContext.getAliasOf());
		setAliasActive(globalContext.isAliasActive());
		setCountUser(globalContext.getCountUser());
		setCreationDate(StringHelper.renderSortableTime(globalContext.getCreationDate()));
		setLatestLoginDate(StringHelper.renderSortableTime(globalContext.getLatestLoginDate()));
		setView(ContentService.getInstance(globalContext).isViewNav());
		setEdit(ContentService.getInstance(globalContext).isPreviewNav());
		setVisibility(globalContext.isView());
		setWizz(globalContext.isWizz());
		setEditability(globalContext.isEditable());
		setDefaultTemplate(globalContext.getDefaultTemplate());
		setExtendMenu(globalContext.isExtendMenu());
		setPreviewMode(globalContext.isPreviewMode());
		setPlatformType(globalContext.getPlatformType());

		setShortDateFormat(globalContext.getShortDateFormat());
		setMediumDateFormat(globalContext.getMediumDateFormat());
		setFullDateFormat(globalContext.getFullDateFormat());

		setHelpURL(globalContext.getHelpURL());
		setMainHelpURL(globalContext.getMainHelpURL());
		setPrivateHelpURL(globalContext.getPrivateHelpURL());

		setForcedHost(globalContext.getForcedHost());

		setEditTemplateMode(globalContext.getEditTemplateMode());

		setSize(StringHelper.renderSize(globalContext.getAccountSize()));
		setGlobalTitle(globalContext.getGlobalTitle());
		setDefaultLanguage(globalContext.getDefaultLanguage());
		setDefaultLanguages(globalContext.getDefaultLanguagesRAW());
		setLanguages(StringHelper.collectionToString(globalContext.getLanguages(), ";"));
		setAutoSwitchToDefaultLanguage(globalContext.isAutoSwitchToDefaultLanguage());
		setContentLanguages(StringHelper.collectionToString(globalContext.getContentLanguages(), ";"));
		setHomepage(globalContext.getHomePage());
		setUrlFactory(globalContext.getURLFactoryClass());

		setUserRoles(StringHelper.collectionToString(globalContext.getUserRoles(), ","));
		setAdminUserRoles(StringHelper.collectionToString(globalContext.getAdminUserRoles(), ","));

		setGoogleAnalyticsUACCT(globalContext.getGoogleAnalyticsUACCT());
		setGoogleApiKey(globalContext.getSpecialConfig().getTranslatorGoogleApiKey());
		setTags(globalContext.getRAWTags());
		setBlockPassword(globalContext.getBlockPassword());

		setMaster(globalContext.isMaster());

		setOnlyCreatorModify(globalContext.isOnlyCreatorModify());
		setCollaborativeMode(globalContext.isCollaborativeMode());
		
		setPortail(globalContext.isPortail());
		
		setComponentsFiltered(globalContext.isComponentsFiltered());

		setTemplateData(globalContext.getTemplateData());

		setProxyPathPrefix(globalContext.getProxyPathPrefix());
		
		setReversedlink(globalContext.isReversedLink());
		
		setMailingSenders(globalContext.getMailingSenders());
		setMailingSubject(globalContext.getMailingSubject());
		setMailingReport(globalContext.getMailingReport());
		setUnsubscribeLink(globalContext.getUnsubscribeLink());
		setDkimDomain(globalContext.getDKIMDomain());
		setDkimSelector(globalContext.getDKIMSelector());
		
		setPophost(globalContext.getPOPHost());
		setPopport(globalContext.getPOPPort());
		setPoppassword(globalContext.getPOPPassword());
		setPopuser(globalContext.getPOPUser());
		setPoppassword(globalContext.getPOPPassword());
		setPopssl(globalContext.isPOPSsl());
		
		setSmtphost(globalContext.getSMTPHost());
		setSmtpport(globalContext.getSMTPPort());
		setSmtpuser(globalContext.getSMTPUser());
		setSmtppassword(globalContext.getSMTPPassword());
		
		setMetaBloc(globalContext.getMetaBloc());
		setHeaderBloc(globalContext.getHeaderBloc());
		setFooterBloc(globalContext.getFooterBloc());
		
		setForcedHttps(globalContext.isForcedHttps());
		setCookies(globalContext.isCookies());
		setCookiesPolicyUrl(globalContext.getCookiesPolicyUrl());
		
		setScreenshot(globalContext.isScreenshot(ctx));
		setScreenshotUrl(globalContext.getScreenshortUrl(ctx));
		
		setQuietArea(globalContext.getQuietArea());
		
		setBackupThread(globalContext.isBackupThread());
		
		setOwnerName(globalContext.getOwnerName());
		setOwnerContact(globalContext.getOwnerContact());
		setOwnerAddress(globalContext.getOwnerAddress());
		setOwnerPostcode(globalContext.getOwnerPostcode());
		setOwnerCity(globalContext.getOwnerCity());
		setOwnerNumber(globalContext.getOwnerNumber());
		setOwnerEmail(globalContext.getOwnerEmail());
		setOwnerPhone(globalContext.getOwnerPhone());		
		setOwnerTwitter(globalContext.getOwnerTwitter());
		setOwnerFacebook(globalContext.getOwnerFacebook());
		setOwnerLinkedin(globalContext.getOwnerLinkedin());
		setOwnerInstagram(globalContext.getOwnerInstagram());
		
		try {
			setSpecialConfig(ResourceHelper.loadStringFromFile(globalContext.getSpecialConfigFile()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		Properties properties = new Properties();
		properties.putAll(globalContext.getURIAlias());
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		try {
			properties.store(outStream, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		setURIAlias(new String(outStream.toByteArray()));

		setOpenFileAsPopup(globalContext.isOpenFileAsPopup());
		setOpenExternalLinkAsPopup(globalContext.isOpenExternalLinkAsPopup());
		setNoPopupDomain(globalContext.getNoPopupDomainRAW());

		outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		List<String> usersAccess = globalContext.getUsersAccess();
		for (String userName : usersAccess) {
			out.println(userName);
		}
		out.close();
		setUsersAccess(new String(outStream.toByteArray()));

		/** user engine **/
		setUserFactoryClassName(globalContext.getUserFactoryClassName());
		setAdminUserFactoryClassName(globalContext.getAdminUserFactory(session).getClass().getName());

		/** remote **/
		if (globalContext.getDMZServerInter() != null) {
			setDMZServerInter(globalContext.getDMZServerInter().toString());
		}
		if (globalContext.getDMZServerIntra() != null) {
			setDMZServerIntra(globalContext.getDMZServerIntra().toString());
		}
		
		setSecurityCsp(globalContext.getSecurityCsp());

	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getAdministrator() {
		return administrator;
	}

	public void setAdministrator(String administrator) {
		this.administrator = administrator;
	}
	
	public boolean isAliasActive() {
		return aliasActive;
	}
	
	public void setAliasActive(boolean aliasActive) {
		this.aliasActive = aliasActive;
	}

	public String getAliasOf() {
		return aliasOf;
	}

	public void setAliasOf(String aliasOf) {
		this.aliasOf = aliasOf;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String createDate) {
		this.creationDate = createDate;
	}

	public String getLatestLoginDate() {
		return latestLoginDate;
	}

	public void setLatestLoginDate(String latestLoginDate) {
		this.latestLoginDate = latestLoginDate;
	}

	public int getCountUser() {
		return countUser;
	}

	public void setCountUser(int countUser) {
		this.countUser = countUser;
	}

	public boolean isView() {
		return view;
	}

	public void setView(boolean view) {
		this.view = view;
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	public boolean isVisibility() {
		return visibility;
	}

	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
	}

	public boolean isEditability() {
		return editability;
	}

	public void setEditability(boolean editability) {
		this.editability = editability;
	}

	public String getDefaultTemplate() {
		return defaultTemplate;
	}

	public void setDefaultTemplate(String defaultTemplate) {
		this.defaultTemplate = defaultTemplate;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getGlobalTitle() {
		return globalTitle;
	}

	public void setGlobalTitle(String globalTitle) {
		this.globalTitle = globalTitle;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public String getLanguages() {
		return languages;
	}

	public void setLanguages(String languages) {
		this.languages = languages;
	}

	public String getContentLanguages() {
		return contentLanguages;
	}

	public void setContentLanguages(String contentLanguages) {
		this.contentLanguages = contentLanguages;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getUserFactoryClassName() {
		return userFactoryClassName;
	}

	public void setUserFactoryClassName(String userFactoryClassName) {
		this.userFactoryClassName = userFactoryClassName;
	}

	public String getAdminUserFactoryClassName() {
		return adminUserFactoryClassName;
	}

	public void setAdminUserFactoryClassName(String adminUserFactoryClassName) {
		this.adminUserFactoryClassName = adminUserFactoryClassName;
	}

	public String getUsersAccess() {
		return usersAccess;
	}

	public void setUsersAccess(String usersAccess) {
		this.usersAccess = usersAccess;
	}

	public String getGoogleAnalyticsUACCT() {
		return googleAnalyticsUACCT;
	}

	public void setGoogleAnalyticsUACCT(String googleAnalyticsUACCT) {
		this.googleAnalyticsUACCT = googleAnalyticsUACCT;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getBlockPassword() {
		return blockPassword;
	}

	public void setBlockPassword(String blockPassword) {
		this.blockPassword = blockPassword;
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public boolean isAutoSwitchToDefaultLanguage() {
		return autoSwitchToDefaultLanguage;
	}

	public void setAutoSwitchToDefaultLanguage(boolean autoSwitchToDefaultLanguage) {
		this.autoSwitchToDefaultLanguage = autoSwitchToDefaultLanguage;
	}

	public String getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(String userRoles) {
		this.userRoles = userRoles;
	}

	public String getShortDateFormat() {
		return shortDateFormat;
	}

	public void setShortDateFormat(String shortDateFormat) {
		this.shortDateFormat = shortDateFormat;
	}

	public String getMediumDateFormat() {
		return mediumDateFormat;
	}

	public void setMediumDateFormat(String mediumDateFormat) {
		this.mediumDateFormat = mediumDateFormat;
	}

	public String getFullDateFormat() {
		return fullDateFormat;
	}

	public void setFullDateFormat(String fullDateFormat) {
		this.fullDateFormat = fullDateFormat;
	}

	public String getHelpURL() {
		return helpURL;
	}

	public void setHelpURL(String helpURL) {
		this.helpURL = helpURL;
	}

	public String getPrivateHelpURL() {
		return privateHelpURL;
	}

	public void setPrivateHelpURL(String privateHelpURL) {
		this.privateHelpURL = privateHelpURL;
	}

	public String getUrlFactory() {
		return urlFactory;
	}

	public void setUrlFactory(String urlFactory) {
		this.urlFactory = urlFactory;
	}

	public String getDefaultLanguages() {
		return defaultLanguages;
	}

	public void setDefaultLanguages(String defaultLanguages) {
		this.defaultLanguages = defaultLanguages;
	}

	public boolean isExtendMenu() {
		return extendMenu;
	}

	public void setExtendMenu(boolean extendMenu) {
		this.extendMenu = extendMenu;
	}

	public String getAdminUserRoles() {
		return adminUserRoles;
	}

	public void setAdminUserRoles(String adminUserRoles) {
		this.adminUserRoles = adminUserRoles;
	}

	public boolean isOpenExternalLinkAsPopup() {
		return openExternalLinkAsPopup;
	}

	public void setOpenExternalLinkAsPopup(boolean openLinkAsPopup) {
		this.openExternalLinkAsPopup = openLinkAsPopup;
	}

	public boolean isOpenFileAsPopup() {
		return openFileAsPopup;
	}

	public void setOpenFileAsPopup(boolean openFileAsPopup) {
		this.openFileAsPopup = openFileAsPopup;
	}

	public String getNoPopupDomain() {
		return noPopupDomain;
	}

	public void setNoPopupDomain(String noPopupDomain) {
		this.noPopupDomain = noPopupDomain;
	}

	public boolean isPreviewMode() {
		return previewMode;
	}

	public void setPreviewMode(boolean previewMode) {
		this.previewMode = previewMode;
	}

	public String getURIAlias() {
		return URIAlias;
	}

	public void setURIAlias(String uRIAlias) {
		URIAlias = uRIAlias;
	}

	public boolean isMaster() {
		return master;
	}

	public void setMaster(boolean master) {
		this.master = master;
	}

	public boolean isWizz() {
		return wizz;
	}

	public void setWizz(boolean wizz) {
		this.wizz = wizz;
	}

	public boolean isOnlyCreatorModify() {
		return onlyCreatorModify;
	}

	public void setOnlyCreatorModify(boolean onlyCreatorModify) {
		this.onlyCreatorModify = onlyCreatorModify;
	}

	public boolean isCollaborativeMode() {
		return collaborativeMode;
	}

	public void setCollaborativeMode(boolean displayCreator) {
		this.collaborativeMode = displayCreator;
	}

	public String getForcedHost() {
		return forcedHost;
	}

	public void setForcedHost(String forcedHost) {
		this.forcedHost = forcedHost;
	}

	public List<GlobalContextBean> getAlias() {
		return alias;
	}

	public void addAlias(GlobalContextBean context) {
		alias.add(context);
	}

	public String getEditTemplateMode() {
		return editTemplateMode;
	}

	public void setEditTemplateMode(String editTemplateMode) {
		this.editTemplateMode = editTemplateMode;
	}

	public String getDMZServerInter() {
		return DMZServerInter;
	}

	public void setDMZServerInter(String dMZServerInter) {
		DMZServerInter = dMZServerInter;
	}

	public String getDMZServerIntra() {
		return DMZServerIntra;
	}

	public void setDMZServerIntra(String dMZServerIntra) {
		DMZServerIntra = dMZServerIntra;
	}

	public TemplateData getTemplateData() {
		return templateData;
	}

	public void setTemplateData(TemplateData templateData) {
		this.templateData = templateData;
	}

	public String getProxyPathPrefix() {
		return proxyPathPrefix;
	}

	public void setProxyPathPrefix(String proxyPathPrefix) {
		this.proxyPathPrefix = proxyPathPrefix;
	}

	public String getPlatformType() {
		return platformType;
	}

	public void setPlatformType(String platform) {
		this.platformType = platform;
	}

	public boolean isReversedlink() {
		return reversedlink;
	}

	public void setReversedlink(boolean reversedlink) {
		this.reversedlink = reversedlink;
	}

	public String getMailingSenders() {
		return mailingSenders;
	}

	public void setMailingSenders(String mailingSenders) {
		this.mailingSenders = mailingSenders;
	}

	public String getMailingSubject() {
		return mailingSubject;
	}

	public void setMailingSubject(String mailingSubject) {
		this.mailingSubject = mailingSubject;
	}

	public String getMailingReport() {
		return mailingReport;
	}

	public void setMailingReport(String mailingReport) {
		this.mailingReport = mailingReport;
	}

	public String getSmtphost() {
		return smtphost;
	}

	public void setSmtphost(String smtphost) {
		this.smtphost = smtphost;
	}

	public String getSmtpport() {
		return smtpport;
	}

	public void setSmtpport(String smtpport) {
		this.smtpport = smtpport;
	}

	public String getSmtpuser() {
		return smtpuser;
	}

	public void setSmtpuser(String smtpuser) {
		this.smtpuser = smtpuser;
	}

	public String getSmtppassword() {
		return smtppassword;
	}

	public void setSmtppassword(String smtppassword) {
		this.smtppassword = smtppassword;
	}

	public String getUnsubscribeLink() {
		return unsubscribeLink;
	}

	public void setUnsubscribeLink(String unsubscribeLink) {
		this.unsubscribeLink = unsubscribeLink;
	}

	public String getDkimDomain() {
		return dkimDomain;
	}

	public void setDkimDomain(String dkimDomain) {
		this.dkimDomain = dkimDomain;
	}

	public String getDkimSelector() {
		return dkimSelector;
	}

	public void setDkimSelector(String dkimSelector) {
		this.dkimSelector = dkimSelector;
	}

	public String getSpecialConfig() {
		return specialConfig;
	}

	public void setSpecialConfig(String specialConfig) {
		this.specialConfig = specialConfig;
	}

	public boolean isForcedHttps() {
		return forcedHttps;
	}

	public void setForcedHttps(boolean forcedHttps) {
		this.forcedHttps = forcedHttps;
	}

	public String getMainHelpURL() {
		return mainHelpURL;
	}

	public void setMainHelpURL(String mainHelpURL) {
		this.mainHelpURL = mainHelpURL;
	}

	public boolean isPortail() {
		return portail;
	}

	public void setPortail(boolean portail) {
		this.portail = portail;
	}

	public boolean isComponentsFiltered() {
		return componentsFiltered;
	}

	public void setComponentsFiltered(boolean componentsFiltered) {
		this.componentsFiltered = componentsFiltered;
	}

	public String getMetaBloc() {
		return metaBloc;
	}

	public void setMetaBloc(String metaBloc) {
		this.metaBloc = metaBloc;
	}

	public String getHeaderBloc() {
		return headBloc;
	}

	public void setHeaderBloc(String headBloc) {
		this.headBloc = headBloc;
	}

	public String getFooterBloc() {
		return footerBloc;
	}

	public void setFooterBloc(String footerBloc) {
		this.footerBloc = footerBloc;
	}

	public String getPophost() {
		return pophost;
	}

	public void setPophost(String pophost) {
		this.pophost = pophost;
	}

	public int getPopport() {
		return popport;
	}

	public void setPopport(int popport) {
		this.popport = popport;
	}

	public String getPopuser() {
		return popuser;
	}

	public void setPopuser(String popuser) {
		this.popuser = popuser;
	}

	public String getPoppassword() {
		return poppassword;
	}

	public void setPoppassword(String poppassword) {
		this.poppassword = poppassword;
	}

	public boolean isPopssl() {
		return popssl;
	}

	public void setPopssl(boolean popssl) {
		this.popssl = popssl;
	}

	public boolean isCookies() {
		return cookies;
	}
	
	public String getCookiesPolicyUrl() {
		return cookiesPolicyUrl;
	}
	
	public void setCookiesPolicyUrl(String cookiesUrl) {
		this.cookiesPolicyUrl = cookiesUrl;
	}

	public void setCookies(boolean cookies) {
		this.cookies = cookies;
	}

	public String getGoogleApiKey() {
		return googleApiKey;
	}

	public void setGoogleApiKey(String googleApiKey) {
		this.googleApiKey = googleApiKey;
	}

	public boolean isScreenshot() {
		return screenshot;
	}

	public void setScreenshot(boolean screenshot) {
		this.screenshot = screenshot;
	}

	public String getScreenshotUrl() {
		return screenshotUrl;
	}

	public void setScreenshotUrl(String screenshotUrl) {
		this.screenshotUrl = screenshotUrl;
	}

	public List<String> getQuietArea() {
		return quietArea;
	}
	
	public Map getQuietAreaMap() {
		return new ListMapValueValue(quietArea);
	}

	public void setQuietArea(List<String> quietArea) {
		this.quietArea = quietArea;
	}

	public boolean isBackupThread() {
		return backupThread;
	}

	public void setBackupThread(boolean backupThread) {
		this.backupThread = backupThread;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getOwnerAddress() {
		return ownerAddress;
	}

	public void setOwnerAddress(String ownerAddress) {
		this.ownerAddress = ownerAddress;
	}
	
	public String getOwnerPostcode() {
		return ownerPostcode;
	}

	public void setOwnerPostcode(String ownerPostcode) {
		this.ownerPostcode = ownerPostcode;
	}

	public String getOwnerCity() {
		return ownerCity;
	}

	public void setOwnerCity(String ownerCity) {
		this.ownerCity = ownerCity;
	}

	public String getOwnerNumber() {
		return ownerNumber;
	}
	
	public String getOwnerNumberNoCountry() {
		String number = getOwnerNumber();
		if (number == null) {
			return null;
		}
		if (number.length()<=1) {
			return number;
		}
		if (!StringHelper.isDigit(number.substring(0,2))) {
			return number.substring(2);
		} else {
			return number;
		}
	}

	public void setOwnerNumber(String ownerNumber) {
		this.ownerNumber = ownerNumber;
	}

	public String getOwnerPhone() {
		return ownerPhone;
	}

	public void setOwnerPhone(String ownerPhone) {
		this.ownerPhone = ownerPhone;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}
	
	public boolean isCookiesType(String type) {
		if (type == null) {
			return false;
		}
		return this.globalContext.getCookiesTypes().contains(type.trim());
	}
	
	public static void main(String[] args) {
		List<String> test = Arrays.asList(new String[] {"test1", "technics"});
		System.out.println(test.contains("technics"));
	}
	
	public List<String> getCookiesTypes() {
		return globalContext.getCookiesTypes();
	}

	public String getOwnerFacebook() {
		return ownerFacebook;
	}

	public void setOwnerFacebook(String ownerFacebook) {
		this.ownerFacebook = ownerFacebook;
	}

	public String getOwnerTwitter() {
		return ownerTwitter;
	}

	public void setOwnerTwitter(String ownerTwitter) {
		this.ownerTwitter = ownerTwitter;
	}

	public String getOwnerInstagram() {
		return ownerInstagram;
	}

	public void setOwnerInstagram(String ownerInstagram) {
		this.ownerInstagram = ownerInstagram;
	}

	public String getOwnerLinkedin() {
		return ownerLinkedin;
	}

	public void setOwnerLinkedin(String ownerLinkedin) {
		this.ownerLinkedin = ownerLinkedin;
	}

	public String getSecurityCsp() {
		return securityCsp;
	}

	public void setSecurityCsp(String securityCsp) {
		this.securityCsp = securityCsp;
	}

	public String getOwnerContact() {
		return ownerContact;
	}

	public void setOwnerContact(String ownerContact) {
		this.ownerContact = ownerContact;
	}
	
	

}
