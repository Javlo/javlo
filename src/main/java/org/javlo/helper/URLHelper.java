/*
 * Created on 20 aout 2003
 */
package org.javlo.helper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.module.core.ModulesContext;
import org.javlo.module.mailing.MailingAction;
import org.javlo.module.mailing.MailingModuleContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.RequestService;
import org.javlo.servlet.ImageTransformServlet;
import org.javlo.servlet.ProxyServlet;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserInfo;
import org.javlo.user.IUserInfo;
import org.javlo.ztatic.StaticInfo;

/**
 * @author pvanderm
 */
public class URLHelper extends ElementaryURLHelper {

	public static final String MINETYPE_FOLDER = "/images/mimetypes/";

	public static String REQUEST_MANAGER_PARAMATER_KEY = "req_man";

	public static String VFS_SERVLET_NAME = "vfs";

	public static String TEMPLATE_RESOURCE_PREFIX = "__tpl__";

	public static String cleanPath(String path, boolean trimStartSeparator) {
		path = path.replaceAll("[/\\\\]+", "/");
		if (trimStartSeparator) {
			path = path.replaceFirst("^/+", "");
		}
		return path;
	}

	public static boolean contains(Set<String> pathPatterns, String path, boolean trimStartSeparator) {
		path = URLHelper.cleanPath(path, trimStartSeparator);
		for (String pattern : pathPatterns) {
			pattern = URLHelper.cleanPath(pattern, trimStartSeparator);
			if (StringHelper.matchStarPattern(path, pattern)) {
				return true;
			}

		}
		return false;
	}

	public static String createAbsoluteViewURL(ContentContext ctx, String uri) throws Exception {
		ContentContext viewCtx = new ContentContext(ctx);
		viewCtx.setRenderMode(ContentContext.VIEW_MODE);
		viewCtx.setAbsoluteURL(true);
		return createURL(viewCtx, uri);
	}

	public static String createAjaxURL(ContentContext ctx) {
		GlobalContext globalContext = ctx.getGlobalContext();
		return createURL(ctx, globalContext, ctx.getPath(), true, true, true, true);
	}

	public static String createAjaxURL(ContentContext ctx, Map params) {
		return createAjaxURL(ctx, ctx.getPath(), params);
	}

	public static String createAjaxURL(ContentContext ctx, String path) {
		GlobalContext globalContext = ctx.getGlobalContext();
		return createURL(ctx, globalContext, path, true, true, true, true);
	}

	public static String createAjaxURL(ContentContext ctx, String path, Map params) {
		StringBuffer finalURL = new StringBuffer();
		finalURL.append(path);
		char sep = '?';
		if (path.indexOf('?') >= 0) {
			sep = '&';
		}
		Iterator keys = params.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			String value = (String) params.get(key);

			finalURL.append(sep);
			finalURL.append(key);
			finalURL.append('=');
			finalURL.append(value);
			sep = '&';
		}
		return createAjaxURL(ctx, finalURL.toString());
	}

	public static String createEditURL(ContentContext ctx) {
		ContentContext editCtx = new ContentContext(ctx);
		editCtx.setRenderMode(ContentContext.EDIT_MODE);
		return createURL(editCtx, ctx.getPath());
	}

	/**
	 * create a intern link URI to the page for edition
	 * 
	 * @param uri
	 *            a standard uri
	 * @param ctx
	 *            the current context of the content.
	 * @return a URL
	 */
	public static String createEditURL(String uri, ContentContext ctx) throws Exception {
		ContentContext editCtx = new ContentContext(ctx);
		editCtx.setRenderMode(ContentContext.EDIT_MODE);
		return createURL(editCtx, uri);
	}

	public static String createGeneralResourceURL(ContentContext ctx, String url) {
		return createStaticResourceURL(ctx, url);
	}

	public static String createOtherLanguageURL(ContentContext ctx, String lg) {
		ContentContext otherContext = new ContentContext(ctx);
		otherContext.setLanguage(lg);
		return createURL(otherContext);
	}

	public static String createResourceURL(ContentContext ctx, MenuElement currentPage, String url) {
		if (url == null) {
			return null;
		}
		
		GlobalContext globalContext = ctx.getGlobalContext();
		String fullFileName = URLHelper.mergePath(globalContext.getDataFolder(), url);

		if (StringHelper.isURLFile(url)) {						
			try {
				return FileUtils.readFileToString(new File(fullFileName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		url = url.replace('\\', '/');
		
		
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE && ctx.getGlobalContext().getStaticConfig().isResourceShortURL()) {
			File file = new File(fullFileName);
			StaticInfo staticInfo;
			try {
				staticInfo = StaticInfo.getInstance(ctx, file);
				String fileName = null;
				if (staticInfo != null && !StringHelper.isEmpty(staticInfo.getTitle(ctx))) {
					fileName = staticInfo.getTitle(ctx);
					url = URLHelper.mergePath(RESOURCE_SERVLET_PATH, ctx.getGlobalContext().setTransformShortURL(url, fileName));
					url = createStaticURL(ctx,url);
				} else {
					if (url.charAt(0) == '/') {
						url = createStaticURL(ctx, currentPage, RESOURCE + url);
					} else {
						url = createStaticURL(ctx, currentPage, RESOURCE + '/' + url);
					}
				}				
			} catch (Exception e) { 
				e.printStackTrace();
			}
		} else {
			if (url.charAt(0) == '/') {
				url = createStaticURL(ctx, currentPage, RESOURCE + url);
			} else {
				url = createStaticURL(ctx, currentPage, RESOURCE + '/' + url);
			}
		}
		
		return url;
	}

	public static String createAvatarUrl(ContentContext ctx, IUserInfo userInfo) {
		if (userInfo.getAvatarURL() != null && userInfo.getAvatarURL().trim().length() > 0) {
			return userInfo.getAvatarURL();
		}

		String url = mergePath(ctx.getGlobalContext().getStaticConfig().getAvatarFolder(), userInfo.getLogin().toLowerCase() + ".png");
		File avatarFile = new File(mergePath(ctx.getGlobalContext().getDataFolder(), url));
		if (avatarFile.exists()) {
			try {
				return createTransformURL(ctx, url, "avatar");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			url = mergePath(ctx.getGlobalContext().getStaticConfig().getAvatarFolder(), userInfo.getLogin().toLowerCase() + ".jpg");
			avatarFile = new File(mergePath(ctx.getGlobalContext().getDataFolder(), url));
			if (avatarFile.exists()) {
				try {
					return createTransformURL(ctx, url, "avatar");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else if (userInfo instanceof AdminUserInfo) {
				AdminUserInfo adminUserInfo = (AdminUserInfo) userInfo;
				if (adminUserInfo.getFacebook() != null && adminUserInfo.getFacebook().trim().length() > 0) {
					return adminUserInfo.getFacebook().replace("//www.", "//graph.") + "/picture?type=small";
				} else if (adminUserInfo.getTwitter() != null && adminUserInfo.getTwitter().trim().length() > 0) {
					return "https://api.twitter.com/1/users/profile_image?screen_name=" + adminUserInfo.getTwitter().replaceAll("https://twitter.com/", "") + "&size=normal";
				}
				if (adminUserInfo.getEmail() != null) {
					try {
						url = URLHelper.getGravatarURL(adminUserInfo.getEmail(), null).toString();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
				return url;
			}
		}
		return null;
	}

	public static String createQRCodeLink(ContentContext ctx, IContentVisualComponent comp) {
		String command = "link";
		if (ctx.isAsPreviewMode()) {
			command = "link_preview";
		}
		if (comp == null) {
			return createStaticURL(ctx, "/qrcode/" + command + "/");
		}
		return createStaticURL(ctx, "/qrcode/" + command + "/" + comp.getId() + ".png");

	}

	public static String createExpCompLink(ContentContext ctx, String compId) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(ContentContext.FORCE_MODE_PARAMETER_NAME, "" + ctx.getRenderMode());
		return createStaticURL(ctx, "/expcomp/" + compId + ".html", params);
	}

	public static String createQRCodeLink(ContentContext ctx, String data) {
		String command = "data";
		String code = StringHelper.getRandomIdBase64();
		ctx.getGlobalContext().setTimeAttribute(code, data);
		return createStaticURL(ctx, "/qrcode/" + command + "/" + code + ".png");
	}

	public static String createResourceURL(ContentContext ctx, File file) {
		GlobalContext globalContext = ctx.getGlobalContext();
		String url = file.getAbsolutePath();
		url = StringUtils.removeStart(url, globalContext.getDataFolder());
		return createResourceURL(ctx, url);
	}

	public static String createLocalURI(ContentContext ctx, File file) {
		GlobalContext globalContext = ctx.getGlobalContext();
		String url = file.getAbsolutePath();
		return StringUtils.removeStart(url, globalContext.getDataFolder());
	}

	public static String createResourceURL(ContentContext ctx, String url) {
		return createResourceURL(ctx, null, url);
	}

	public static String createResourceURLWithoutAccessCount(ContentContext ctx, String url) {
		url = URLHelper.addParam(url, "no-access", "true");
		return createResourceURL(ctx, url);
	}

	public static String createRSSURL(ContentContext ctx, String channel) throws Exception {

		if (channel.trim().length() == 0) {
			channel = "all";
		}

		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement elem = content.getNavigation(ctx).getNoErrorFreeCurrentPage(ctx);
		Template template = null;
		if (elem != null) {
			template = ctx.getCurrentTemplate().getFinalTemplate(ctx);
			ctx.setCurrentTemplate(template);
			String uri = "/xml/" + ctx.getContentLanguage() + '/' + template.getName() + "/rss/" + channel + ".xml";
			return createStaticURL(ctx, uri);
		}
		return null;
	}

	public static String createServletWrapperURL(ContentContext ctx, String url, String paramName) {
		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');
		url = addParam(url, URLHelper.REQUEST_MANAGER_PARAMATER_KEY, paramName);
		if (url.charAt(0) == '/') {
			return createStaticURL(ctx, REQUEST_MANAGER + url);
		} else {
			return createStaticURL(ctx, REQUEST_MANAGER + '/' + url);
		}
	}

	public static final String createSSLURL(ContentContext ctx) {
		return createSSLURL(ctx.getPath(), ctx);
	}

	public static String createStaticFeedbackURL(ContentContext ctx, String data) {

		if (data == null) {
			return null;
		}
		String url = URLHelper.mergePath(FEEDBACK, "pic_" + data + ".gif");
		return createStaticURL(ctx, url);
	}

	public static String createStaticResourceURL(ContentContext ctx, String url) {
		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');
		URLHelper.mergePath(RESOURCE, url);
		return createStaticURL(ctx, URLHelper.mergePath(RESOURCE, url));
	}

	public static String createStaticSharedResourceURL(ContentContext ctx, String url) {

		StaticConfig config = StaticConfig.getInstance(ctx.getRequest().getSession());

		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');
		url = URLHelper.mergePath(config.getShareDataFolderKey(), url);
		url = URLHelper.mergePath(RESOURCE, url);
		return createStaticURL(ctx, url);
	}

	public static String createTemplateResourceURL(ContentContext ctx, String url) {

		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');
		url = URLHelper.mergePath(TEMPLATE_RESOURCE_PREFIX, url);
		return createResourceURL(ctx, url);
	}

	public static String createStaticTemplateURL(ContentContext ctx, String url) throws Exception {
		return createStaticTemplateURL(ctx, url, null);
	}

	public static String createStaticTemplatePath(ContentContext ctx, String url, String templateVersion) throws Exception {
		return createStaticTemplateURL(ctx, url, templateVersion, false);
	}

	public static String createStaticTemplateURL(ContentContext ctx, String url, String templateVersion) throws Exception {
		return createStaticTemplateURL(ctx, url, templateVersion, true);
	}

	protected static String createStaticTemplateURL(ContentContext ctx, String url, String templateVersion, boolean widthPath) throws Exception {
		Template template = null;
		GlobalContext globalContext = ctx.getGlobalContext();
		if (ctx.getRenderMode() == ContentContext.PAGE_MODE) {
			MailingModuleContext mailingCtx = MailingModuleContext.getInstance(ctx.getRequest());
			RequestService requestService = RequestService.getInstance(ctx.getRequest());
			String templateID = requestService.getParameter("template", null);
			if (templateID == null) {
				templateID = mailingCtx.getCurrentTemplate();
				if (templateID == null) {
					if (ctx.getCurrentTemplate() != null) {
						templateID = ctx.getCurrentTemplate().getId();
					} else {
						for (Template mtemplate : ctx.getCurrentTemplates()) {
							if (mtemplate.isMailing()) {
								templateID = mtemplate.getId();
							}
						}
					}
				}
			}
			template = Template.getApplicationInstance(ctx.getRequest().getSession().getServletContext(), ctx, templateID);
		}

		if (template == null) {
			template = ctx.getCurrentTemplate();
		}
		String templateFolder;

		template = template.getFinalTemplate(ctx);
		templateFolder = template.getLocalWorkTemplateFolder();

		String templateFullPath = URLHelper.mergePath(templateFolder, template.getFolder(globalContext));

		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');

		if (ctx.isResourceGZip()) {
			if (url.toLowerCase().endsWith(".css") || url.toLowerCase().endsWith(".js")) {
				url = url + '.' + Template.GZ_FILE_EXT;
			}
		}

		if (templateVersion != null) {
			url = URLHelper.addParam(url, "template-id", templateVersion);
		}
		return createStaticURL(ctx, null, URLHelper.mergePath(templateFullPath, url), widthPath);
		// return createStaticURL(ctx, URLHelper.mergePath(templateFullPath,
		// url));
	}

	public static String createStaticTemplatePluginURL(ContentContext ctx, String url, String pluginFolder) throws Exception {

		if (URLHelper.isAbsoluteURL(url)) {
			return url;
		}

		GlobalContext globalContext = ctx.getGlobalContext();

		String templateFolder;

		Template template = ctx.getCurrentTemplate();

		templateFolder = template.getLocalWorkTemplateFolder();
		String templateFullPath = URLHelper.mergePath(templateFolder, template.getFolder(globalContext), pluginFolder);

		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');

		return createStaticURL(ctx, null, URLHelper.mergePath(templateFullPath, url), true);
	}

	public static String createStaticURL(ContentContext ctx, MenuElement referencePage, String inUrl) {
		return createStaticURL(ctx, referencePage, inUrl, true);
	}

	public static String createStaticTemplateURL(ContentContext ctx, Template template, String url) throws Exception {
		GlobalContext globalContext = ctx.getGlobalContext();
		if (template == null) {
			MenuElement elem = ctx.getCurrentPage();
			template = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(elem.getTemplateId());
		}
		if (template.getFolder(globalContext) == null) {
			template = Template.getApplicationInstance(ctx.getRequest().getSession().getServletContext(), ctx, globalContext.getDefaultTemplate());
		}
		String templateFullPath;
		templateFullPath = URLHelper.mergePath(template.getLocalWorkTemplateFolder(), template.getFolder(globalContext));

		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');
		return createStaticURL(ctx, URLHelper.mergePath(templateFullPath, url));
	}

	public static String createStaticTemplateURLWithoutContext(ContentContext ctx, Template template, String url) throws Exception {
		if (url == null || template == null) {
			return null;
		}
		GlobalContext globalContext = ctx.getGlobalContext();
		if (template == null) {
			MenuElement elem = ctx.getCurrentPage();
			template = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(elem.getTemplateId());
		}

		if (url.startsWith(template.getLocalWorkTemplateFolder())) {
			return url;
		}

		if (template.getFolder(globalContext) == null) {
			template = Template.getApplicationInstance(ctx.getRequest().getSession().getServletContext(), ctx, globalContext.getDefaultTemplate());
		}
		String templateFullPath;
		templateFullPath = URLHelper.mergePath(template.getLocalWorkTemplateFolder(), template.getFolder(globalContext)); // TODO
																															// Check
																															// why
																															// it
																															// was
																															// null
																															// before
																															// globalContext

		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');
		return createStaticURL(ctx.getContextWithInternalURL(), null, URLHelper.mergePath(templateFullPath, url), false);
	}

	public static String createTemplateSourceURL(ContentContext ctx, String url) {
		if (url == null) {
			return null;
		}

		if (StringHelper.isURLFile(url)) {
			GlobalContext globalContext = ctx.getGlobalContext();
			String fullFileName = URLHelper.mergePath(globalContext.getDataFolder(), url);
			try {
				return FileUtils.readFileToString(new File(fullFileName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		url = url.replace('\\', '/');
		if (url.charAt(0) == '/') {
			return createStaticURL(ctx, TEMPLATE + url);
		} else {
			return createStaticURL(ctx, TEMPLATE + '/' + url);
		}
	}

	public static String createTransformStaticTemplateURL(ContentContext ctx, Template template, String filter, String url) throws Exception {
		GlobalContext globalContext = ctx.getGlobalContext();
		if (template == null) {
			MenuElement elem = ctx.getCurrentPage();
			template = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(elem.getTemplateId());
		}
		if (template.getFolder(globalContext) == null) {
			template = Template.getApplicationInstance(ctx.getRequest().getSession().getServletContext(), ctx, globalContext.getDefaultTemplate());
		}
		String templateFolder;
		templateFolder = template.getLocalWorkTemplateFolder();

		String templateFullPath = URLHelper.mergePath(templateFolder, template.getFolder(globalContext));

		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');
		url = URLHelper.mergePath(TRANSFORM + '/' + filter + '/' + templateFullPath, url);
		return createStaticURL(ctx, url);
	}

	public static String createTransformLocalTemplateURL(ContentContext ctx, String template, String filter, String url) throws Exception {
		if (template == null) {
			template = ctx.getCurrentTemplate().getId();
		}
		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');
		url = URLHelper.mergePath(TRANSFORM, filter, template + ImageTransformServlet.LOCAL_TEMPLATE_SUFFIX, ctx.getGlobalContext().getContextKey(), url);		
		return createStaticURL(ctx, url);
	}

	public static String createURL(ContentContext ctx) {
		return createURL(ctx, ctx.getPath());
	}

	/**
	 * create URL without context.
	 * 
	 * @param ctx
	 * @return
	 */
	public static String createLocalURL(ContentContext ctx) {
		String pathPrefix = ctx.getPathPrefix(ctx.getRequest());
		ctx.setForcePathPrefix("");
		String url = createURL(ctx, ctx.getPath());
		ctx.setForcePathPrefix(pathPrefix);
		return url;
	}

	public static String createURL(ContentContext ctx, Map params) {
		return createURL(ctx, ctx.getPath(), params);
	}
	
	public static String createURL(ContentContext ctx, MenuElement page, Map params) {
		if (page == null) {
			return null;
		}
		String path = page.getPath();
		try {			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return createURL(ctx, path, params);
	}

	public static String createURL(ContentContext ctx, MenuElement page) {
		String path = page.getPath();		
		if (page.isHttps() && (!ctx.getURLProtocolPrefix().equalsIgnoreCase("https"))) {
			ContentContext httpsCtx = new ContentContext(ctx);
			httpsCtx.setAbsoluteURL(true);
			httpsCtx.setURLProtocolPrefix("https");
			return createURL(httpsCtx, page.getPath());
		} else if (!page.isHttps() && (ctx.getURLProtocolPrefix().equals("https"))) {
			ContentContext httpCtx = new ContentContext(ctx);
			httpCtx.setAbsoluteURL(true);
			httpCtx.setURLProtocolPrefix("http");
			return createURL(httpCtx, path);
		} else {
			return createURL(ctx, path);
		}
	}

	/**
	 * add suffix to the URL. sample (suffix: #suffix) : www.javlo.org >>
	 * www.javlo.org#suffix sample 2 (suffix: #suffix) : www.javlo.org?test=test
	 * >> www.javlo.org#suffix?test=test
	 * 
	 * @param URL
	 * @param suffix
	 * @return
	 */
	public static String addSuffix(String URL, String suffix) {
		if (URL.contains("?")) {
			URL = URL.replace("?", suffix + '?');
		} else {
			URL = URL + suffix;
		}
		return URL;
	}

	/**
	 * create a intern link URL
	 * 
	 * @param uri
	 *            the target uri
	 * @param ctx
	 *            the current context of the content.
	 * @return a URL
	 */
	public static String createURL(ContentContext ctx, String uri) {
		GlobalContext globalContext = ctx.getGlobalContext();
		return createURL(ctx, globalContext, uri, false, false, true, true);
	}

	/**
	 * create url without context and without encoreURL (remove jsessionid)
	 * 
	 * @param ctx
	 * @param uri
	 * @return
	 */
	public static String createLocalURLWithtoutEncodeURL(ContentContext ctx, String uri) {
		String pathPrefix = ctx.getPathPrefix(ctx.getRequest());
		ctx.setForcePathPrefix("");
		String url = createURLWithtoutEncodeURL(ctx, uri);
		ctx.setForcePathPrefix(pathPrefix);
		return url;
	}

	public static String createURLWithtoutEncodeURL(ContentContext ctx, String uri) {
		GlobalContext globalContext = ctx.getGlobalContext();
		return createURL(ctx, globalContext, uri, false, false, true, false);
	}

	public static String createURLWithtoutContext(ContentContext ctx, String uri) {
		GlobalContext globalContext = ctx.getGlobalContext();
		return createURL(ctx, globalContext, uri, false, false, false, false);
	}

	public static String createURL(ContentContext ctx, GlobalContext globalContext, String uri) {
		return createURL(ctx, globalContext, uri, false, false, true, true);
	}

	public static String createURL(ContentContext ctx, String path, Map params) {
		StringBuffer finalURL = new StringBuffer();
		finalURL.append(path);
		if (params != null) {
			char sep = '?';
			if (path.indexOf('?') >= 0) {
				sep = '&';
			}
			Iterator keys = params.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				String value = (String) params.get(key);
				finalURL.append(sep);
				finalURL.append(key);
				finalURL.append('=');
				finalURL.append(URLEncoder.encode(value));
				sep = '&';
			}
		}
		return createURL(ctx, finalURL.toString());
	}

	public static String createStaticURL(ContentContext ctx, String path, Map params) {
		StringBuffer finalURL = new StringBuffer();
		finalURL.append(path);
		char sep = '?';
		if (path.indexOf('?') >= 0) {
			sep = '&';
		}
		Iterator keys = params.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			String value = (String) params.get(key);

			finalURL.append(sep);
			finalURL.append(key);
			finalURL.append('=');
			finalURL.append(value);
			sep = '&';
		}
		return createStaticURL(ctx, finalURL.toString());
	}

	/**
	 * create a url but change the lg
	 * 
	 * @param ctx
	 * @param uri
	 * @param lg
	 * @return
	 */
	public static String createURL(ContentContext ctx, String uri, String lg) {
		ContentContext otherContext = new ContentContext(ctx);
		if (lg != null) {
			otherContext.setAllLanguage(lg);
		}
		return createURL(otherContext, uri);
	}

	public static String createURL(ContentContext ctx, String[] recupParam, Map params) {
		if (params == null) {
			params = new HashMap<String, String>();
		}
		for (String param : recupParam) {
			params.put(param, ctx.getRequest().getParameter(param));
		}
		return createURL(ctx, ctx.getPath(), params);
	}

	/**
	 * @deprecated switch parameters
	 */
	@Deprecated
	public static String createURL(String uri, ContentContext ctx) {
		return createURL(ctx, uri);
	}

	/**
	 * create a url but check if url start with a lg ref.
	 * 
	 * @param ctx
	 * @param uri
	 *            the uri (can start with lg ref.)
	 * @return
	 */
	public static String createURLCheckLg(ContentContext ctx, String uri) {
		GlobalContext globalContext = ctx.getGlobalContext();
		Set<String> viewLg = globalContext.getLanguages();
		String lgFound = null;
		for (String lg : viewLg) {
			if (uri.startsWith('/' + lg + '/') || uri.equals('/' + lg)) {
				lgFound = lg;
				uri = uri.substring(3);
			}
		}
		return createURL(ctx, uri, lgFound);

	}

	public static String createURLFromPageName(ContentContext ctx, String pageName) throws Exception {
		GlobalContext globalContext = ctx.getGlobalContext();
		NavigationService navService = NavigationService.getInstance(globalContext);
		MenuElement page = navService.getPage(ctx, pageName);
		if (page != null) {
			return createURL(ctx, page.getPath());
		} else {
			return createURL(ctx, "/");
		}
	}

	public static String createURLNoForceTemplate(ContentContext ctx) {
		GlobalContext globalContext = ctx.getGlobalContext();
		return createURL(ctx, globalContext, ctx.getPath(), false, false, true, true);
	}

	/**
	 * create a intern link URL without context path
	 * 
	 * @param uri
	 *            the target uri
	 * @param ctx
	 *            the current context of the content.
	 * @return a URL
	 */
	public static String createURLNoPathPrefix(ContentContext ctx, String uri) {
		return createURL(ctx, ctx.getGlobalContext(), uri, false, false, false, true);
	}

	/**
	 * create the url to a file in to a virtual file system (sp. zip file)
	 * 
	 * @param dataFile
	 *            the date file (sp. zip file)
	 * @param uri
	 *            the uri in virtual file
	 * @return a full url to uri
	 */
	public static String createVFSURL(ContentContext ctx, String dataFile, String uri) {
		if (uri == null) {
			return null;
		}
		String url = uri;
		url = url.replace('\\', '/');
		String fullDataFile = URLHelper.mergePath('/' + VFS_SERVLET_NAME, dataFile);
		url = URLHelper.mergePath(fullDataFile, url);
		return createStaticURL(ctx, url);
	}

	/**
	 * create a intern link URI to the view site
	 * 
	 * @param uri
	 *            a standard uri
	 * @param ctx
	 *            the current context of the content.
	 * @return a URL
	 */
	public static String createViewURL(String uri, ContentContext ctx) throws Exception {
		ContentContext viewCtx = new ContentContext(ctx);
		viewCtx.setRenderMode(ContentContext.VIEW_MODE);
		return createURL(viewCtx, uri);
	}

	public static String createVisualTemplateURL(ContentContext ctx, Template template, String filter) throws Exception {
		MenuElement elem = ctx.getCurrentPage();
		GlobalContext globalContext = ctx.getGlobalContext();
		if (template == null) {
			template = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(elem.getTemplateId());
		}
		if (template.getFolder(globalContext) == null) {
			template = Template.getApplicationInstance(ctx.getRequest().getSession().getServletContext(), ctx, globalContext.getDefaultTemplate());
		}
		String templateFolder;
		templateFolder = template.getLocalWorkTemplateFolder();

		String templateFullPath = URLHelper.mergePath(templateFolder, template.getFolder((String) null));

		String url = template.getVisualFile();

		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');
		url = URLHelper.mergePath(TRANSFORM + '/' + filter + '/' + "default" + templateFullPath, url);
		return createStaticURL(ctx, url);
	}

	/**
	 * transform a url to a crypted param url. sampe :
	 * http://www.cms.com?id=342&user=patrick -->
	 * http://www.cms.com?crypted_param=sl4ZDC74DR5673DT5
	 * 
	 * @param ctx
	 *            current content context.
	 * @param url
	 *            a url (with parameters)
	 * @return
	 * @throws Exception
	 */
	public static String cryptURL(ContentContext ctx, String url) throws Exception {
		if (url == null || url.trim().length() == 0 || !url.contains("?")) {
			return url;
		}
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		String[] dec = url.split("\\?");
		// String encodedParam =
		// StringHelper.encodeBase64ToURLParam(StringSecurityUtil.encode('?'+dec[1],
		// staticConfig.getSecretKey()));
		String encodedParam = StringSecurityUtil.encode('?' + dec[1], staticConfig.getSecretKey());
		return URLHelper.addParam(dec[0], RequestHelper.CRYPTED_PARAM_NAME, encodedParam);

	}

	/**
	 * extract a host name from a url
	 * 
	 * @param url
	 *            a url sample : http://www.host.ext/view/fr/test.html (extract
	 *            name : host)
	 * @return return a name (hostname without last extension)
	 */
	public static String extractName(String url) {
		if (url == null) {
			return "";
		}
		String outName = url;
		int indDblSlash = url.indexOf("//");
		if (indDblSlash >= 0) {
			outName = url.substring(indDblSlash + 2);
		}
		int indSplSlash = outName.indexOf("/");
		if (indSplSlash >= 0) {
			outName = outName.substring(0, indSplSlash);
		}

		int indPoint = outName.indexOf('.');
		if (indPoint >= 0) {
			String saveOutName = outName;
			outName = outName.substring(indPoint + 1);
			if (outName.indexOf('.') < 0) { // if only one '.'
				outName = saveOutName;
			}
		} else {
			return "";
		}
		indPoint = outName.indexOf(".");
		if (indPoint >= 0) {
			outName = outName.substring(0, indPoint);
		}

		return outName;
	}

	public static String extractHost(String url) {
		if (url == null) {
			return "";
		}
		String outName = url;
		int indDblSlash = url.indexOf("//");
		if (indDblSlash >= 0) {
			outName = url.substring(indDblSlash + 2);
		}
		int indSplSlash = outName.indexOf("/");
		if (indSplSlash >= 0) {
			outName = outName.substring(0, indSplSlash);
		}

		int indPoint = outName.indexOf("/");
		if (indPoint >= 0) {
			outName = outName.substring(0, indPoint);
		} else {
			indPoint = outName.indexOf("?");
			if (indPoint >= 0) {
				outName = outName.substring(0, indPoint);
			} else {
				indPoint = outName.indexOf("#");
				if (indPoint >= 0) {
					outName = outName.substring(0, indPoint);
				}
			}
		}

		int indPort = outName.lastIndexOf(':');
		if (indPort >= 0) {
			outName = outName.substring(0, indPort);
		}

		return outName;
	}

	public static String extractPath(String url) {
		if (url == null) {
			return null;
		}
		int slashIndex = url.lastIndexOf("/");
		if (slashIndex >= 0) {
			return url.substring(0, slashIndex);
		}
		return "";
	}

	public static String extractFileName(String path) {
		File file = new File(path);
		String outName = file.getName();
		int endNameIndex = outName.indexOf('?');
		if (endNameIndex >= 0) {
			outName = outName.substring(0, endNameIndex);
		}
		return outName;
	}

	public static String getMoveChildURL(ContentContext ctx, MenuElement elem) {
		if ((elem != null) && (elem.getParent() != null)) {
			MenuElement newParent = null;
			List<MenuElement> elems = elem.getParent().getChildMenuElements();
			for (int i = 0; i < elems.size() - 1; i++) {
				if (elems.get(i).equals(elem)) {
					newParent = elems.get(i + 1);
				}
			}
			if (newParent != null) {
				return createURL(ctx, newParent.getPath() + '/' + elem.getName());
			}
		}
		return "";
	}

	public static String getMoveParentURL(ContentContext ctx, MenuElement elem) {
		if ((elem.getParent() != null) && (elem.getParent().getParent() != null)) {
			return createURL(ctx, elem.getParent().getParent().getPath() + '/' + elem.getName());
		} else {
			return "";
		}
	}

	public static String getRelativePath(String parentPath, String childPath) {
		parentPath = cleanPath(parentPath, true);
		childPath = cleanPath(childPath, true);
		if (childPath.startsWith(parentPath)) {
			return childPath.substring(parentPath.length());
		} else {
			// TODO Do better?
			return null;
		}
	}

	/**
	 * create a special URL to call a other module.
	 * 
	 * @param url
	 * @param moduleName
	 * @return
	 * @throws Exception
	 */
	public static String createInterModuleURL(ContentContext ctx, String url, String moduleName) throws Exception {
		return createInterModuleURL(ctx, url, moduleName, null);
	}

	/**
	 * create a special URL to call a other module.
	 * 
	 * @param url
	 * @param moduleName
	 * @return
	 * @throws Exception
	 */
	public static String createInterModuleURL(ContentContext ctx, String url, String moduleName, Map<String, String> inParams) throws Exception {		
		ModulesContext moduleContext = ModulesContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext());
		return createInterModuleURL(ctx, url, moduleName, moduleContext.getCurrentModule().getName(), inParams);
	}

	public static String createInterModuleURL(ContentContext ctx, String url, String moduleName, String fromModule, Map<String, String> inParams) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (inParams != null) {
			params.putAll(inParams);
		}
		params.put("from-module", fromModule);
		params.put("module", moduleName);
		return createURL(ctx, url, params);
	}

	/**
	 * create a special URL to activate a module.
	 */
	public static String createModuleURL(ContentContext ctx, String url, String moduleName) {
		return createModuleURL(ctx, url, moduleName, null);
	}

	/**
	 * create a special URL to activate a module.
	 */
	public static String createModuleURL(ContentContext ctx, String url, String moduleName, Map<String, String> inParams) {
		Map<String, String> params = new HashMap<String, String>();
		if (inParams != null) {
			params.putAll(inParams);
		}
		params.put("module", moduleName);
		return createURL(ctx, url, params);
	}

	public static String httpDecryptData(String data) {
		if (data == null) {
			return null;
		}
		return StringHelper.decodeNoChar(data, StringHelper.specialChar);
		// return StringSecurityUtil.decode(decodeParam, "");
	}

	public static String httpEncryptData(String data) {
		// String cryptDate = StringSecurityUtil.encode(data, "");
		return StringHelper.encodeNoChar(data, StringHelper.specialChar);
	}

	public static boolean isAbsoluteURL(String url) {
		String urlLC = url.toLowerCase().trim();
		return urlLC.startsWith("http://") || urlLC.startsWith("https://") || urlLC.startsWith("ftp://") || urlLC.startsWith("file://");
	}

	public static String mergePath(String... paths) {
		String outPath = "";
		for (String path : paths) {
			outPath = mergePath(outPath, path);
		}
		return outPath.toString();
	}

	public static final String path2URL(String path) {
		return path.replace(" ", "%20");
	}

	public static String removeTemplateFromResourceURL(String url) {
		String[] splitURL = url.split("/");
		if (splitURL.length > 3) {
			StringBuffer outURL = new StringBuffer();
			for (int i = 0; i < splitURL.length; i++) {
				if (i != 3 && splitURL[i].trim().length() > 0) {
					outURL.append("/");
					outURL.append(splitURL[i]);
				}
			}
			return outURL.toString();
		} else {
			return url;
		}

	}
	
	/**
	 * remove first / on a path if exist
	 * @param path
	 * @return the same string if not start width / and the string without first / (all first /)
	 */
	public static String removeFirstSlash(String path) {		
		if (path == null || !path.startsWith("/")) {
			return path;
		} else {
			return removeFirstSlash(path.substring(1));
		}
	}

	public static final String _changeMode(String inURL, String mode) throws MalformedURLException {
		URL url = new URL(inURL);

		String path = url.getPath();
		String[] pathItems = StringUtils.splitByWholeSeparator(path, "/");
		if (pathItems.length > 1 && pathItems[0].length() == 2) {
			path = URLHelper.mergePath(mode, path);
		} else {
			path = mode;
			for (int i = 0; i < pathItems.length - 1; i++) {
				path = URLHelper.mergePath(path, pathItems[i + 1]);
			}
		}
		if (url.getQuery() != null) {
			path = path + '?' + url.getQuery();
		}
		String port = "";
		if (url.getPort() >= 0 && url.getPort() != 80) {
			port = ":" + url.getPort();
		}

		return url.getProtocol() + "://" + url.getHost() + port + '/' + path;

	}

	public static final String changeMode(String inURL, String mode) throws MalformedURLException {
		URL url = new URL(inURL);
		String path = url.getPath();
		String[] pathItems = StringUtils.splitByWholeSeparator(path, "/");
		for (int i = 0; i < pathItems.length; i++) {
			if (pathItems[i].startsWith("edit-") || pathItems[i].equals("edit") || pathItems[i].equals("view") || pathItems[i].startsWith("ajax-") || pathItems[i].equals("ajax") || pathItems[i].equals("preview")) {
				pathItems[i] = mode;
			}
		}
		String port = "";
		if (url.getPort() >= 0 && url.getPort() != 80) {
			port = ":" + url.getPort();
		}

		String query = "";
		if (url.getQuery() != null) {
			query = '?' + url.getQuery();
		}

		return url.getProtocol() + "://" + url.getHost() + port + '/' + URLHelper.mergePath(pathItems) + query;
	}

	/**
	 * get the parent url of a url. sample:
	 * http://www.javlo.org/static/images/visual.png >>
	 * http://www.javlo.org/static/images
	 * 
	 * @param url
	 * @return a url to a folder.
	 */
	public static String getParentURL(String url) {
		if (url.contains("?")) {
			url = url.substring(0, url.lastIndexOf('?'));
		}
		if (!url.contains("/")) {
			return "";
		} else {
			return url.substring(0, url.lastIndexOf('/'));
		}
	}

	public static String replaceFolderVariable(ContentContext ctx, String url) {
		String outURL = url;
		GlobalContext globalContext = ctx.getGlobalContext();
		outURL = globalContext.getStaticConfig().replaceFolderVariable(outURL);
		outURL = outURL.replace("$DATA", globalContext.getDataFolder());
		return outURL;
	}

	/**
	 * transform text to url. if absolute url >> return same link, if contains
	 * '/' >> search on path, don't contains '/' >> search as page name.
	 * 
	 * @param ctx
	 * @param link
	 * @return
	 * @throws Exception
	 */
	public static String smartLink(ContentContext ctx, String link) throws Exception {
		if (link == null) {
			return null;
		}
		if (URLHelper.isAbsoluteURL(link)) {
			return link;
		}
		if (link.contains("/")) {
			String url = URLHelper.createURL(ctx, link);
			if (url != null) {
				return url;
			} else {
				return link;
			}
		} else {
			String url = URLHelper.createURLFromPageName(ctx, link);
			if (url != null) {
				return url;
			} else {
				return link;
			}
		}
	}

	public static String addMailingFeedback(ContentContext ctx, String url) {
		if (ctx.getRenderMode() == ContentContext.PAGE_MODE && url != null) {
			return addParam(url, MailingAction.MAILING_FEEDBACK_PARAM_NAME, MailingAction.MAILING_FEEDBACK_VALUE_NAME);
		} else {
			return url;
		}
	}

	/**
	 * transform url with credential. semple : http://www.javlo.be >>
	 * http://login:password@www.javlo.be
	 * 
	 * @param url
	 * @param login
	 * @param password
	 * @return
	 */
	public static String addCredential(String url, String login, String password) {
		if (url != null && StringHelper.isURL(url)) {
			return url.replace("http://", "http://" + login + ':' + password + "@");
		} else {
			return url;
		}
	}

	public static String getFileURL(ContentContext ctx, File file) {
		File rootStatic = new File(ctx.getGlobalContext().getDataFolder());
		String relativePath = file.getAbsolutePath().replace(rootStatic.getAbsolutePath(), "");
		return URLHelper.createResourceURL(ctx, relativePath);
	}

	public static String createProxyURL(ContentContext ctx, String inURL) throws MalformedURLException {
		URL url = new URL(inURL);
		String id = ProxyServlet.getURLCode(url);
		String proxyURL = "/proxy/" + id + '/' + StringHelper.getFileNameFromPath(url.getPath());
		return createStaticURL(ctx, proxyURL);
	}

	public static String createProxyURL(String rootURL, String inURL) throws MalformedURLException {
		URL url = new URL(inURL);
		String id = ProxyServlet.getURLCode(url);
		String proxyURL = "/proxy/" + id + '/' + StringHelper.getFileNameFromPath(url.getPath());
		return URLHelper.mergePath(rootURL, proxyURL);
	}

	public static String encodePathForAttribute(String path) {
		String outPath = path.replace(" ", "_BLK_");
		outPath = outPath.replace("/", "_SLA_");
		outPath = outPath.replace("\\", "_BSL_");
		return outPath;
	}

	public static String decodePathForAttribute(String path) {
		if (path == null) {
			return null;
		}
		String outPath = path.replace("_BLK_", " ");
		outPath = outPath.replace("_SLA_", "/");
		outPath = outPath.replace("_BSL_", "\\");
		return outPath;
	}
	
	private static String removeJsessionid (String url) {
		String outURL = url;
		if (url.contains(";jsessionid=")) {
			outURL = outURL.substring(0, url.indexOf(";jsessionid="));
		} 
		if (url.contains("&")) {
			outURL = outURL + url.substring(url.indexOf("?"));
		}
		return outURL;
	}

	public static String getFileTypeURL(ContentContext ctx, File file) {
		String fileType = StringHelper.getFileExtension(file.getName());
		boolean folder = file.isDirectory();
		String imageFolder = MINETYPE_FOLDER;
		if (folder) {
			if (file.listFiles().length==0) {
				return createStaticURL(ctx, imageFolder + "folder-empty.svg");
			} else {
				if (file.getName().contains("import")) {					
					return createStaticURL(ctx, imageFolder + "folder-download.svg");
				} else if (StringHelper.isImage(file.listFiles()[0].getName())) {
					return createStaticURL(ctx, imageFolder + "folder-pictures.svg");
				} else {
					return createStaticURL(ctx, imageFolder + "folder-documents.svg");
				}
			}
		} else {
			String mineType = ResourceHelper.getFileExtensionToMineType(fileType).replace('/', '-').toLowerCase();
			File imageFile = new File(URLHelper.mergePath(ctx.getRequest().getSession().getServletContext().getRealPath(imageFolder), mineType + ".svg"));
			if (imageFile.exists()) {
				return createStaticURL(ctx, imageFolder + mineType + ".svg");
			} else {
				return createStaticURL(ctx, "/mimetype/"+fileType+".svg");
			}
		}
	}
	
	/**
	 * replace page:pagename with the correct link to the page.
	 * @param text
	 * @return
	 * @throws Exception 
	 */
	public static String replacePageReference(ContentContext ctx, String text) throws Exception {
		Pattern pattern = Pattern.compile("(page:)(.+?)(>| |,|;|:|\\?)");
		Matcher matcher = pattern.matcher(text);		
		String outText = text;
		while (matcher.find()) {
			String group = matcher.group();
			group = group.substring(0, group.length()-1);	
			String pageName = group.replaceFirst("page:", "");
			outText = outText.replaceAll(group, URLHelper.createURLFromPageName(ctx, pageName));
		}
		return outText;
	}

}