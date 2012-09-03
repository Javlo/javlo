package org.javlo.helper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.image.ImageHelper;
import org.javlo.navigation.IURLFactory;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageConfiguration;
import org.javlo.service.ContentService;
import org.javlo.template.Template;
import org.javlo.ztatic.FileCache;

/**
 * countain the method with efficient body for URLHelper.
 */
public abstract class ElementaryURLHelper {

	public static class Code {
		private String code = null;

		public Code(String inCode) {
			code = inCode;
		}

		@Override
		public boolean equals(Object obj) {
			return getCode().equals(((Code) (obj)).getCode());
		}

		public String getCode() {
			return code;
		}

		@Override
		public int hashCode() {
			return code.hashCode();
		}

		public void setCode(String code) {
			this.code = code;
		}

		@Override
		public String toString() {
			return getCode();
		}

	}

	static final int HTTPS_PORT = 8443;

	static final String TRANSFORM = "/transform";

	static final String RESOURCE = "/resource";

	static final String TEMPLATE = "/template";

	static final String REQUEST_MANAGER = "/wrapper";

	static final String FEEDBACK = "/feedback";

	public static final String URL_SUFFIX = ".html";

	public static final String ROOT_FILE_NAME = "index";

	public static final String SPACIAL_RIGHT_CODE_KEY = "special-right-code";

	/**
	 * add get param to a url
	 */
	public static String addParam(String url, String name, String value) {
		if (url.contains("?")) {
			return url = url + "&" + name + '=' + value;
		} else {
			return url = url + "?" + name + '=' + value;
		}
	}

	/**
	 * add get attribute to a url.
	 * 
	 * @param params
	 *            a list of string represent param and value. (sp: name=patrick).
	 * @return a url with new params
	 */
	public static String addAllParams(String url, String... params) {
		char sep = '?';
		if (url.contains("?")) {
			sep = '&';
		}
		String allParam = "";
		for (String param : params) {
			allParam = allParam + sep + param;
			sep = '&';
		}
		return url + allParam;
	}

	public static final String addSpecialRightCode(GlobalContext globalContext, HttpSession session, String url) {

		Code specialRightCode = (Code) session.getAttribute(SPACIAL_RIGHT_CODE_KEY);
		if (specialRightCode == null) {
			specialRightCode = new Code(StringHelper.getRandomId().replace('=', '*'));
			session.setAttribute(SPACIAL_RIGHT_CODE_KEY, specialRightCode);
			globalContext.addSpecialAccessCode(specialRightCode);
		}

		url = addParam(url, SPACIAL_RIGHT_CODE_KEY, specialRightCode.getCode());

		return url;
	}

	public static String createAbsoluteURL(ContentContext ctx, String uri) {
		String url = uri;
		if (ctx.getDMZServerInter() == null) {
			String port = "";
			if (ctx.getHostPort() != 80) {
				port = ":" + ctx.getHostPort();
			}
			url = ctx.getURLProtocolPrefix() + "://" + ctx.getHostName() + port + url;
		} else {
			url = mergePath(ctx.getDMZServerInter().toString(), url);
		}
		return url;
	}

	/*
	 * public static String createExternalURL(ServletContext application, String uri) { StaticConfig staticConfig = StaticConfig.getInstance(application); String prefix = staticConfig.getPathPrefix(); return prefix + uri; }
	 */

	public static String createJSPComponentURL(HttpServletRequest request, String url, String componentType) {
		String workingURL = "/jsp/components/" + componentType + "/" + url;
		return workingURL;
	}

	protected static final String createNoProtocolURL(ContentContext ctx, GlobalContext globalContext, String uri, boolean ajax, boolean withPathPrefix) {
		String newUri;
		HttpServletRequest request = ctx.getRequest();

		if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			IURLFactory urlCreator = ctx.getURLFactory();
			if (urlCreator != null) {
				try {
					uri = uri.substring(0, uri.lastIndexOf("."));
					// MenuElement page = globalContext.getPageByPath(ctx, uri);
					MenuElement page = globalContext.getPage(ctx, uri);
					uri = urlCreator.createURL(ctx, page) + URL_SUFFIX;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (uri.length() < 1) {
			uri = "/";
		} else if (uri.charAt(0) != '/') {
			uri = '/' + uri;
		}

		String mode;
		if (ctx.isViewPrefix()) {
			mode = "/view/";
		} else {
			mode = "/";
		}

		// String mode = "/view/";
		if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {
			mode = "/edit/";
		} else if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			mode = "/preview/";
		} else if (ctx.getRenderMode() == ContentContext.PAGE_MODE) {
			mode = "/page/";
		} else if (ctx.getRenderMode() == ContentContext.ADMIN_MODE) {
			mode = "/admin/";
		} else if (ctx.getRenderMode() == ContentContext.MAILING_MODE) {
			mode = "/mailing/";
		} else if (ctx.getRenderMode() == ContentContext.TIME_MODE) {
			mode = "/time/";
		}
		if (ajax) {
			mode = "/ajax/";
		}

		if (ctx.getContentLanguage().equals(ctx.getLanguage())) {
			if (withPathPrefix) {
				newUri = getPathPrefix(globalContext, request) + mode + ctx.getLanguage() + uri;
			} else {
				newUri = mode + ctx.getLanguage() + uri;
			}
		} else {
			if (withPathPrefix) {
				newUri = getPathPrefix(globalContext, request) + mode + ctx.getLanguage() + '-' + ctx.getContentLanguage() + uri;
			} else {
				newUri = mode + ctx.getLanguage() + '-' + ctx.getContentLanguage() + uri;
			}
		}

		String url = newUri;

		if (!ctx.isAbsoluteURL()) {
			url = ctx.getResponse().encodeURL(newUri);
		}

		// force mode for ajax request
		if (ajax) {
			url = URLHelper.addParam(url, ContentContext.FORCE_MODE_PARAMETER_NAME, "" + ctx.getRenderMode());
		}

		return url;
	}

	public static final String createSSLURL(String uri, ContentContext ctx) {

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String sslURL = createNoProtocolURL(ctx, globalContext, uri, false, true);
		if (ctx.getRequest().getProtocol().toLowerCase().indexOf("http:") != -1) {
			String host = ctx.getRequest().getServerName();
			sslURL = "https://" + host + ':' + HTTPS_PORT + sslURL;
		}

		return sslURL;
	}

	public static String createStaticComponentURL(HttpServletRequest request, String url, String componentType) {
		String workingURL = "/static/components/" + componentType + "/" + url;
		return workingURL;
	}

	protected static String createStaticURL(ContentContext ctx, MenuElement referencePage, String inUrl, boolean withPathPrefix) {
		ContentContext newCtx = ctx;
		if (referencePage != null && referencePage.isRemote()) {
			String linkedURLStr = NavigationHelper.getLinkedURL(referencePage);
			if (linkedURLStr.trim().length() > 0) {
				newCtx = new ContentContext(ctx);
				newCtx.setAbsoluteURL(true);
				try {
					URL linkedURL = new URL(linkedURLStr);
					newCtx.setHostName(linkedURL.getHost());
					if (linkedURL.getPort() > 0) {
						newCtx.setHostPort(linkedURL.getPort());
					} else {
						newCtx.setHostPort(80);
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}

		String url = inUrl;
		if (withPathPrefix) {
			String pathPrefix = getPathPrefix(ctx.getRequest());
			url = ElementaryURLHelper.mergePath(pathPrefix, inUrl);
		}

		if (newCtx.isAbsoluteURL()) {
			if (!url.startsWith("/")) {
				url = url + '/';
			}
			String port = "";
			if (newCtx.getHostPort() != 80) {
				port = ":" + newCtx.getHostPort();
			}
			url = newCtx.getURLProtocolPrefix() + "://" + newCtx.getHostName() + port + url;
		}
		url = url.replace('\\', '/');
		return url;
	}

	public static String createStaticURL(ContentContext ctx, String inUrl) {
		return createStaticURL(ctx, null, inUrl, true);
	}

	/**
	 * @deprecated use createStaticURL with the ContentContext
	 * @param request
	 * @param url
	 * @return
	 */
	@Deprecated
	public static String createStaticURL(HttpServletRequest request, String url) {
		return getPathPrefix(request) + url;
	}

	public static String createTeaserURL(HttpServletRequest request, String url) {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		return getPathPrefix(request) + editCtx.getTeasersDirectory() + '/' + url;
	}

	public static String createThumbURL(ContentContext ctx, String url, int width, boolean ts) {
		url = url.replace('\\', '/');
		if (url.charAt(0) == '/') {
			return createStaticURL(ctx, TRANSFORM + "/thumbnails" + url);
		} else {
			return createStaticURL(ctx, TRANSFORM + "/thumbnails/" + url);
		}
	}

	public static String createTransformLongDescURL(ContentContext ctx, String url) throws Exception {
		if (url == null) {
			return null;
		}
		url = url + ".html";
		url = url.replace('\\', '/');

		ContentService.createContent(ctx.getRequest());
		MenuElement elem = ctx.getCurrentPage();

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		PageConfiguration pageConfig = PageConfiguration.getInstance(globalContext);
		Template template = pageConfig.getCurrentTemplate(ctx, elem);

		if (template != null) {
			String templateName;
			if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {
				templateName = Template.EDIT_TEMPLATE_CODE;
			} else {
				templateName = template.getId();
			}
			url = ElementaryURLHelper.mergePath(TRANSFORM + '/' + ctx.getRequestContentLanguage() + '/' + templateName, url);
		} else {
			url = ElementaryURLHelper.mergePath(TRANSFORM + '/' + ctx.getRequestContentLanguage(), url);
		}

		return createStaticURL(ctx, url);
	}

	public static String createTransformURL(ContentContext ctx, MenuElement referencePage, String url, String filter) throws Exception {
		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');
		
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		String deviceCode = "no-device";
		if (ctx.getDevice() != null) {
			deviceCode = ctx.getDevice().getCode();
		}
		String key = ImageHelper.createSpecialDirectory(filter, ctx.getArea(), deviceCode, ctx.getCurrentTemplate());
		FileCache fc = FileCache.getInstance(ctx.getRequest().getSession().getServletContext());		
		if (!globalContext.getImageViewFilter().contains(filter) && fc.getFileName(key, url).exists() ) {
			return URLHelper.createStaticURL(ctx, fc.getRelativeFilePath(key, url));
		}

		ContentService.createContent(ctx.getRequest());
		MenuElement elem = ctx.getCurrentPage();

		
		PageConfiguration pageConfig = PageConfiguration.getInstance(globalContext);
		Template template = pageConfig.getCurrentTemplate(ctx, elem);

		if (template != null) {
			String templateName;
			if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {
				templateName = Template.EDIT_TEMPLATE_CODE;
			} else {
				templateName = template.getId();
			}
			url = createTransformURL(ctx, referencePage, url, filter, templateName);
		} else {			
			if (filter.equals("template")) {
				url = ElementaryURLHelper.mergePath(TRANSFORM + '/' + filter, url);
			} else {
				url = ElementaryURLHelper.mergePath(TRANSFORM + '/' + filter + '/' + ctx.getArea(), url);
			}
			url = createStaticURL(ctx, referencePage, url, true);
		}
		return url;
	}

	public static String createTransformURL(ContentContext ctx, MenuElement referencePage, String url, String filter, String templateName) throws Exception {
		if (url == null) {
			return null;
		}
		url = url.replace('\\', '/');
		if (templateName != null) {			
			url = ElementaryURLHelper.mergePath(TRANSFORM + '/' + filter  + '/' + templateName + '/' + ctx.getArea(), url);
		} else {
			url = ElementaryURLHelper.mergePath(TRANSFORM + '/' + filter, url);
		}

		return createStaticURL(ctx, referencePage, url, true);
	}

	public static String createTransformURL(ContentContext ctx, String url, String filter) throws Exception {
		return createTransformURL(ctx, null, url, filter);
	}

	public static String createTransformURLWithoutCountAccess(ContentContext ctx, String url, String filter) throws Exception {
		url = ElementaryURLHelper.addParam(url, "no-access", "true");
		return createTransformURL(ctx, url, filter);

	}

	protected static String createURL(ContentContext ctx, GlobalContext globalContext, String uri, boolean ajax, boolean forceTemplate, boolean withPathPrefix) {

		if (uri == null) {
			return "";
		}

		if (uri.length() < 1) {
			uri = "/";
		} else if (uri.charAt(0) != '/') {
			uri = "/" + uri;
		}

		String totalURI = uri;
		String[] splitURI = totalURI.split("\\?|\\#|\\;");
		uri = splitURI[0];
		String params = totalURI.substring(uri.length());

		if (!uri.endsWith(URL_SUFFIX)) {
			if (uri.endsWith("/")) {
				uri = uri + ROOT_FILE_NAME + URL_SUFFIX;
			} else {
				uri = uri + URL_SUFFIX;
			}
		}
		String url = createNoProtocolURL(ctx, globalContext, uri, ajax, withPathPrefix);

		if (ctx.isAbsoluteURL()) {
			if (ctx.getDMZServerInter() == null) {
				String port = "";
				if (ctx.getHostPort() != 80) {
					port = ":" + ctx.getHostPort();
				}
				url = ctx.getURLProtocolPrefix() + "://" + ctx.getHostName() + port + url;
			} else {
				url = mergePath(ctx.getDMZServerInter().toString(), url);
			}
		}

		url = url + params;

		if (forceTemplate) {
			if (ctx.getRequest().getParameter(PageConfiguration.FORCE_TEMPLATE_PARAM_NAME) != null) {
				if ((ctx.getRenderMode() != ContentContext.EDIT_MODE) && (ctx.getRenderMode() != ContentContext.ADMIN_MODE)) {
					if (!url.contains(PageConfiguration.FORCE_TEMPLATE_PARAM_NAME)) {
						url = addParam(url, PageConfiguration.FORCE_TEMPLATE_PARAM_NAME, ctx.getRequest().getParameter(PageConfiguration.FORCE_TEMPLATE_PARAM_NAME));
					}
				}
			}
		}

		return url;
	}

	public static Map<String, String> extractParameterFromURL(String url) {
		if (url.indexOf('?') >= 0) {
			url = url.split("\\?")[1]; // remove host and path
		}

		Map<String, String> outParam = new HashMap<String, String>();

		String[] params = url.split("&");
		for (String param : params) {
			String[] paramSplited = param.split("=");
			String key = paramSplited[0];
			String value = "";
			if (paramSplited.length > 1) {
				value = paramSplited[1];
			}
			outParam.put(key, value);
		}

		return outParam;
	}

	public static String getIconeURL(ContentContext ctx, String icone) {
		return ElementaryURLHelper.createStaticURL(ctx, "/images/icones/" + icone);
	}

	public static String getPathPrefix(HttpServletRequest request) {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		return getPathPrefix(globalContext, request);
	}

	/**
	 * return the path path prefix defined in ServletContext
	 * 
	 * @param request
	 * @return
	 */
	public static String getPathPrefix(GlobalContext globalContext, HttpServletRequest request) {
		String CACHE_KEY = "javlo-path-prefix-" + globalContext.getContextKey();
		String res = (String) request.getAttribute(CACHE_KEY);
		if (res == null) {
			String requestPrefix = request.getContextPath();
			res = globalContext.getPathPrefix();
			if (res == null) {
				res = requestPrefix;
			} else {
				StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
				if (staticConfig.isURIWithContext()) {
					res = URLHelper.mergePath("/", requestPrefix, res);
				} else {
					res = URLHelper.mergePath("/", res);
				}
			}
			request.setAttribute(CACHE_KEY, res);
		}
		return res;
	}

	/**
	 * return the path path prefix defined in ServletContext
	 * 
	 * @param request
	 * @return
	 */
	/*
	 * public static String getPathPrefix(ServletContext application) { StaticConfig staticConfig = StaticConfig.getInstance(application); return staticConfig.getPathPrefix(); }
	 */

	public static void main(String[] args) {
		/*
		 * String totalURI = "http://localhost:8080/perso/view/fr/test#anchor;jsessionid=C7B2998A8806C17C05CD673F1E09B890?image__142=0&page__142=0" ; String[] splitURI = totalURI.split("\\?|\\#|\\;"); String uri = splitURI[0]; String params = ""; for (int i = 1; i < splitURI.length; i++) { params = params + totalURI.substring(uri.length()); } System.out.println("uri = "+uri); System.out.println("param = "+params);
		 */

		System.out.println("*** 1. " + mergePath("/test/dc", "/path3"));
		System.out.println("*** 2. " + mergePath("/test/dc?ceci=test", "/path3"));
		System.out.println("*** 3. " + mergePath("/test/dc", "/path3?lavie=moi"));
		System.out.println("*** 4. " + mergePath("/test/dc?ceci=test", "/path3?moi=patrick"));

	}

	/**
	 * merge the path. sample mergePath ("/cat", "element" ) -> /cat/element, mergePath ("/test/", "/google) -> /test/google
	 * 
	 * @param path1
	 * @param path2
	 * @return
	 */
	public static String mergePath(String path1, String path2) {
		if (path1 == null) {
			return StringHelper.neverNull(path2);
		} else if (path2 == null) {
			return path1;
		}
		path1 = path1.replace('\\', '/');
		path2 = path2.replace('\\', '/');
		if ((path1 == null) || (path1.trim().length() == 0)) {
			return path2;
		} else if ((path2 == null) || (path2.trim().length() == 0)) {
			return path1;
		} else {
			String[] pathSep = StringUtils.split(path1, "?");
			String paramPath1 = "";
			if (pathSep.length > 1) {
				path1 = pathSep[0];
				paramPath1 = pathSep[1];
			}
			pathSep = StringUtils.split(path2, "?");
			String paramPath2 = "";
			if (pathSep.length > 1) {
				path2 = pathSep[0];
				paramPath2 = pathSep[1];
			}

			if (paramPath1.length() > 0 && paramPath2.length() > 0) {
				paramPath1 = '?' + paramPath1 + '&' + paramPath2;
			} else {
				paramPath1 = paramPath1 + paramPath2;
				if (paramPath1.length() > 0) {
					paramPath1 = "?" + paramPath1;
				}
			}

			if (path1.endsWith("/")) {
				if (path2.startsWith("/")) {
					path2 = path2.replaceFirst("/", "");
					return path1 + path2 + paramPath1;
				} else {
					return path1 + path2 + paramPath1;
				}
			} else {
				if (path2.startsWith("/")) {
					return path1 + path2 + paramPath1;
				} else {
					return path1 + '/' + path2 + paramPath1;
				}
			}
		}
	}

}
