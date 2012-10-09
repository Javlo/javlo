/*
 * Created on 20 aout 2003
 */
package org.javlo.helper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.module.core.ModulesContext;
import org.javlo.module.mailing.MailingModuleContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.RequestService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

/**
 * @author pvanderm
 */
public class URLHelper extends ElementaryURLHelper {

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
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		return createURL(ctx, globalContext, ctx.getPath(), true, true, true);
	}

	public static String createAjaxURL(ContentContext ctx, Map params) {
		return createAjaxURL(ctx, ctx.getPath(), params);
	}

	public static String createAjaxURL(ContentContext ctx, String path) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		return createURL(ctx, globalContext, path, true, true, true);
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

	/**
	 * @deprecated use createStaticURL (ContentContext, url), and use editContext for image directory
	 */
	@Deprecated
	public static String createImageURL(HttpServletRequest request, String url) {
		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
		return getPathPrefix(request) + staticConfig.getImageFolder() + '/' + url;
	}

	public static String createOtherLanguageURL(ContentContext ctx, String lg) {
		ContentContext otherContext = new ContentContext(ctx);
		otherContext.setLanguage(lg);
		return createURL(otherContext);
	}

	/**
	 * create a intern link URI to the preview site
	 * 
	 * @param uri
	 *            a standard uri
	 * @param ctx
	 *            the current context of the content.
	 * @return a URL
	 */
	public static String createPreViewURL(String uri, ContentContext ctx) throws Exception {
		ContentContext viewCtx = new ContentContext(ctx);
		viewCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		return createURL(viewCtx, uri);
	}

	public static String createResourceURL(ContentContext ctx, MenuElement currentPage, String url) {
		if (url == null) {
			return null;
		}

		if (StringHelper.isURLFile(url)) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String fullFileName = URLHelper.mergePath(globalContext.getDataFolder(), url);
			try {
				return FileUtils.readFileToString(new File(fullFileName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		url = url.replace('\\', '/');
		if (url.charAt(0) == '/') {
			return createStaticURL(ctx, currentPage, RESOURCE + url);
		} else {
			return createStaticURL(ctx, currentPage, RESOURCE + '/' + url);
		}
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
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
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

		// ContentService.createContent(ctx.getRequest());
		MenuElement elem = ctx.getCurrentPage();

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
		// return createStaticURL(ctx, URLHelper.mergePath(templateFullPath, url));
	}

	public static String createStaticTemplatePluginURL(ContentContext ctx, String url, String pluginFolder) throws Exception {

		if (URLHelper.isAbsoluteURL(url)) {
			return url;
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

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
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (template == null) {
			MenuElement elem = ctx.getCurrentPage();
			template = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(elem.getTemplateId());
			;
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
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (template == null) {
			MenuElement elem = ctx.getCurrentPage();
			template = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(elem.getTemplateId());
			;
		}
		if (template.getFolder(globalContext) == null) {
			template = Template.getApplicationInstance(ctx.getRequest().getSession().getServletContext(), ctx, globalContext.getDefaultTemplate());
		}
		String templateFullPath;
		templateFullPath = URLHelper.mergePath(template.getLocalWorkTemplateFolder(), template.getFolder(globalContext)); // TODO Check why it was null before globalContext

		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');
		return createStaticURL(ctx, null, URLHelper.mergePath(templateFullPath, url), false);
	}

	public static String createTemplateSourceURL(ContentContext ctx, String url) {
		if (url == null) {
			return null;
		}

		if (StringHelper.isURLFile(url)) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
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
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
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

	public static String createURL(ContentContext ctx) {
		return createURL(ctx, ctx.getPath());
	}

	public static String createURL(ContentContext ctx, Map params) {
		return createURL(ctx, ctx.getPath(), params);
	}

	public static String createURL(ContentContext ctx, MenuElement page) {
		if (page.isHttps() && (!ctx.getURLProtocolPrefix().equalsIgnoreCase("https"))) {
			ContentContext httpsCtx = new ContentContext(ctx);
			httpsCtx.setAbsoluteURL(true);
			httpsCtx.setURLProtocolPrefix("https");
			return createURL(httpsCtx, page.getPath());
		} else if (!page.isHttps() && (ctx.getURLProtocolPrefix().equals("https"))) {
			ContentContext httpCtx = new ContentContext(ctx);
			httpCtx.setAbsoluteURL(true);
			httpCtx.setURLProtocolPrefix("http");
			return createURL(httpCtx, page.getPath());
		} else {
			return createURL(ctx, page.getPath());
		}
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
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		return createURL(ctx, globalContext, uri, false, false, true);
	}

	public static String createURL(ContentContext ctx, GlobalContext globalContext, String uri) {
		return createURL(ctx, globalContext, uri, false, false, true);
	}

	public static String createURL(ContentContext ctx, String path, Map params) {
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
		return createURL(ctx, finalURL.toString());
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

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
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
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NavigationService navService = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
		MenuElement page = navService.getPage(ctx, pageName);
		if (page != null) {
			return createURL(ctx, page.getPath());
		} else {
			return createURL(ctx, "/");
		}
	}

	public static String createURLNoForceTemplate(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		return createURL(ctx, globalContext, ctx.getPath(), false, false, true);
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
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		return createURL(ctx, globalContext, uri, false, false, false);
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
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (template == null) {
			template = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(elem.getTemplateId());
		}
		if (template.getFolder(globalContext) == null) {
			template = Template.getApplicationInstance(ctx.getRequest().getSession().getServletContext(), ctx, globalContext.getDefaultTemplate());
		}
		String templateFolder;
		templateFolder = template.getLocalWorkTemplateFolder();

		String templateFullPath = URLHelper.mergePath(templateFolder, template.getFolder(null));

		String url = template.getVisualFile();

		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');
		url = URLHelper.mergePath(TRANSFORM + '/' + filter + '/' + "default" + templateFullPath, url);
		return createStaticURL(ctx, url);
	}

	/**
	 * transform a url to a crypted param url. sampe : http://www.cms.com?id=342&user=patrick --> http://www.cms.com?crypted_param=sl4ZDC74DR5673DT5
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
		// String encodedParam = StringHelper.encodeBase64ToURLParam(StringSecurityUtil.encode('?'+dec[1], staticConfig.getSecretKey()));
		String encodedParam = StringSecurityUtil.encode('?' + dec[1], staticConfig.getSecretKey());
		return URLHelper.addParam(dec[0], RequestHelper.CRYPTED_PARAM_NAME, encodedParam);

	}

	/**
	 * extract a host name from a url
	 * 
	 * @param url
	 *            a url sample : http://www.host.ext/view/fr/test.html (extract name : host)
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

	public static String getMoveChildURL(ContentContext ctx, MenuElement elem) {
		if ((elem != null) && (elem.getParent() != null)) {
			MenuElement newParent = null;
			MenuElement[] elems = elem.getParent().getChildMenuElements();
			for (int i = 0; i < elems.length - 1; i++) {
				if (elems[i].equals(elem)) {
					newParent = elems[i + 1];
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
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ModulesContext moduleContext = ModulesContext.getInstance(ctx.getRequest().getSession(), globalContext);
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

	public static void main(String[] args) {
		// System.out.println("merge path = " + mergePath("/tools/", "poeple", "/genre"));
		System.out.println("merge path = " + mergePath(new String[] { "/tools/", "poeple", "/genre" }));
		System.out.println("*** extract host : " + extractHost("http://www.europarl.europa.eu/president"));
		System.out.println("*** extract host : " + extractHost("http://www.noctis.eu/president"));
		System.out.println("*** extract host : " + extractHost("www.europarl.europa.eu"));
		System.out.println("*** extract host : " + extractHost("noctis.be/test"));
		System.out.println("*** extract host : " + extractHost("noctis.be?test"));
		System.out.println("*** extract host : " + extractHost("noctis.be#test"));
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

	public static String removeTemplateFromRessourceURL(String url) {
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

}