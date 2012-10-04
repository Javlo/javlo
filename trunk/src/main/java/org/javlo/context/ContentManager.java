package org.javlo.context;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.javlo.config.StaticConfig;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 * 
 *         some static method for manage the content database.
 * 
 */
public class ContentManager {

	protected static Logger logger = Logger.getLogger(ContentManager.class.getName());

	public static final char MULTI_PARAM_SEP = '?';

	public static String getContentLanguage(ContentContext ctx) {
		String lg = getLanguage(ctx.getRequest(), 1);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (!globalContext.getContentLanguages().contains(lg) && lg != null) {
			lg = globalContext.getDefaultLanguages().iterator().next();
			if (lg.trim().length() == 0) {
				lg = globalContext.getContentLanguages().iterator().next();
			}
		} else {
			try {
				I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
				i18n.changeViewLanguage(ctx);
			} catch (Exception e) {
				logger.log(Level.INFO, "impossible to change content view language in I18NAccess", e);
			}
		}
		return lg;
	}

	public static String getContextName(HttpServletRequest request) {

		String realPath = RequestService.getURI(request);

		if (realPath.startsWith("/")) {
			realPath = realPath.substring(1);
		}

		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession().getServletContext());
		String[] splitedPath = StringHelper.split(realPath, "/");

		if (!staticConfig.isHostDefineSite() && splitedPath.length > 0) {
			return splitedPath[0];
		}

		return null;
	}

	public static String getErrorLg(String lg) {
		String res = "undefined language";
		if (lg.equals("fr")) {
			res = "Désolé, cette page n'est pas encore disponible en français.";
		} else if (lg.equals("en")) {
			res = "Sorry, this page does not yet exist in english.";
		} else if (lg.equals("nl")) {
			res = "Sorry, dit pagina bestaat nog niet in het nederlands.";
		}
		return res;
	}

	public static String getLanguage(ContentContext ctx) {
		String lg = getLanguage(ctx.getRequest(), 0);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (!globalContext.getLanguages().contains(lg)) {
			lg = globalContext.getDefaultLanguages().iterator().next();
			if (lg.trim().length() == 0) {
				lg = globalContext.getLanguages().iterator().next();
			}
		} else {
			try {
				I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
				ctx.setPageRequest(true);
				i18n.changeViewLanguage(ctx);
			} catch (Exception e) {
				logger.log(Level.INFO, "impossible to change view language in I18NAccess", e);
			}
		}
		return lg;
	}

	private static String getLanguage(HttpServletRequest request, int index) {

		String lg = "";

		String realPath = RequestService.getURI(request);

		GlobalContext.getInstance(request);

		if (realPath != null) {
			String[] splitedPath = StringHelper.split(realPath, "/");
			int splitedPathMinSize = 3;

			if (splitedPath.length >= splitedPathMinSize) { // TODO: check regression and test with ">" in place of ">="
				lg = splitedPath[2].toLowerCase();
			}
		}

		String[] lgs = StringHelper.split(lg, "-");
		if (index < lgs.length) {
			return lgs[index];
		} else {
			return null;
		}
	}

	public static String getLanguageFromRequest(HttpServletRequest request) {
		String lg = getLanguage(request, 0);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		if (!globalContext.getLanguages().contains(lg)) {
			lg = globalContext.getDefaultLanguages().iterator().next();
			if (lg.trim().length() == 0) {
				lg = globalContext.getLanguages().iterator().next();
			}
		} else {
			try {
				I18nAccess.getInstance(globalContext, request.getSession());
			} catch (Exception e) {
				logger.log(Level.INFO, "impossible to change view language in I18NAccess", e);
			}
		}
		return lg;
	}

	/**
	 * @deprecated use RequestService
	 * @param request
	 * @param paramName
	 * @param defaultValue
	 * @return
	 */
	@Deprecated
	public static String getParameterValue(HttpServletRequest request, String paramName, String defaultValue) {

		RequestService service = RequestService.getInstance(request);
		return service.getParameter(paramName, defaultValue);

	}

	static String getPath(HttpServletRequest request) {
		String path = null;
		String realPath = RequestService.getURI(request);

		if (realPath != null) {
			String[] splitedPath = StringHelper.split(realPath, "/");
			int splitedPathMinSize = 3;
			GlobalContext.getInstance(request);
			/*
			 * if (globalContext.getPathPrefix().trim().length() > 0) { splitedPathMinSize = 4; }
			 */
			path = "";
			for (int i = splitedPathMinSize; i < splitedPath.length; i++) {
				path = path + '/' + splitedPath[i];
			}
		}

		if (path == null || path.trim().length() == 0) {
			path = "/";
		} else {
			path = path.split("\\?|\\#|\\;")[0];
		}
		// remove extension if exist (.html)
		if (path.indexOf('.') >= 0) {
			path = path.substring(0, path.indexOf('.'));
		}

		return path;
	}

	/**
	 * get the depth of the path
	 */
	public static int getPathDepth(String path) {
		int res = 0;
		if (path != null) {
			StringTokenizer strTkn = new StringTokenizer(path, "/");
			while (strTkn.hasMoreTokens()) {
				res++;
				strTkn.nextToken();
			}
		}
		return res;
	}

	public static String getPathElement(String path, int depth) {
		String res = "";
		String[] splitPath = splitPath(path);
		if (depth > splitPath.length) {
			res = splitPath[depth];
		}
		return res;
	}

	public static boolean getRewrite(HttpServletRequest request) {
		boolean rewrite = false;
		/*
		 * if (URLHelper.getPathPrefix(request).length() == 0) { rewrite = true; }
		 */
		return rewrite;
	}

	/**
	 * construct a valid path if the path is not valid.
	 * 
	 * @param path
	 *            a path, valid or not
	 * @return a valid path.
	 */
	public static String getValidPath(ContentContext ctx, String path) throws Exception {
		String res = path;

		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement elem = content.getNavigation(ctx);
		if (elem.searchChild(ctx) == null) {
			res = "/";
			String lastName = path;
			if (path.indexOf('/') >= 0) {
				if (path.lastIndexOf('/') + 1 < path.length()) {
					lastName = path.substring(path.lastIndexOf('/') + 1, path.length());
				}
			}
			elem = elem.searchChildFromName(lastName);
			if (elem != null) {
				res = elem.getPath();
			}
		}
		ctx.setPath(res);

		return res;
	}

	public static boolean isAdmin(HttpServletRequest request) {
		return isAdmin(request, false);
	}

	public static boolean isAdmin(HttpServletRequest request, boolean uriPrefixed) {
		boolean res = false;
		String realPath = request.getServletPath();
		if (realPath != null) {
			StringTokenizer pathTokens = new StringTokenizer(realPath, "/");
			if (uriPrefixed) {
				pathTokens.nextToken();
			}
			/*
			 * if (URLHelper.getPathPrefix(request).length() > 0) { pathTokens.nextToken(); } GlobalContext globalContext = GlobalContext.getInstance(request); if (!StringHelper.isEmpty(globalContext.getPathPrefix())) { pathTokens.nextToken(); }
			 */
			if (pathTokens.hasMoreTokens()) {
				String nextToken = pathTokens.nextToken();
				if (nextToken.equals("admin")) {
					res = true;
				}
			}
		}
		return res;
	}

	public static boolean isAdmin(String path) {
		boolean res = false;
		if (path != null) {
			StringTokenizer pathTokens = new StringTokenizer(path, "/");
			if (pathTokens.hasMoreTokens()) {
				String next = pathTokens.nextToken();
				if (next.equals("edit") || next.equals("preview") || next.equals("ajax") || next.equals("view")) {
					return false;
				} else if (next.equals("admin")) {
					return true;
				} else if (pathTokens.hasMoreTokens()) {
					if (pathTokens.nextToken().equals("admin")) {
						return true;
					}
				}
			}
		}
		return res;
	}

	public static boolean isEdit(HttpServletRequest request) {
		return isEdit(request, false);
	}

	public static boolean isEdit(HttpServletRequest request, boolean uriPrefixed) {
		boolean res = false;
		String realPath = request.getServletPath();
		if (realPath != null) {
			StringTokenizer pathTokens = new StringTokenizer(realPath, "/");
			if (uriPrefixed) {
				pathTokens.nextToken();
			}
			/*
			 * if (!StringHelper.isEmpty(URLHelper.getPathPrefix(request))) { pathTokens.nextToken(); } GlobalContext globalContext = GlobalContext.getInstance(request); if (!StringHelper.isEmpty(globalContext.getPathPrefix())) { pathTokens.nextToken(); }
			 */
			if (pathTokens.hasMoreTokens()) {
				String nextToken = pathTokens.nextToken();
				if (nextToken.equals("edit") || nextToken.equals("ajax")) {
					res = true;
				}
			}
		}
		return res;
	}

	public static boolean isMailing(HttpServletRequest request) {
		return isMailing(request, false);
	}

	public static boolean isMailing(HttpServletRequest request, boolean uriPrefixed) {
		boolean res = false;
		String realPath = request.getServletPath();
		if (realPath != null) {
			StringTokenizer pathTokens = new StringTokenizer(realPath, "/");
			if (uriPrefixed) {
				pathTokens.nextToken();
			}
			/*
			 * if (URLHelper.getPathPrefix(request).length() > 0) { pathTokens.nextToken(); } GlobalContext globalContext = GlobalContext.getInstance(request); if (!StringHelper.isEmpty(globalContext.getPathPrefix())) { pathTokens.nextToken(); }
			 */
			if (pathTokens.hasMoreTokens()) {
				String nextToken = pathTokens.nextToken();
				if (nextToken.equals("mailing")) {
					res = true;
				}
			}
		}
		return res;
	}

	public static boolean isPreview(HttpServletRequest request) {
		return isPreview(request, false);
	}

	public static boolean isPreview(HttpServletRequest request, boolean uriPrefixed) {
		boolean res = false;
		String realPath = request.getServletPath();

		if (realPath != null) {
			StringTokenizer pathTokens = new StringTokenizer(realPath, "/");
			if (uriPrefixed) {
				pathTokens.nextToken();
			}
			/*
			 * if (URLHelper.getPathPrefix(request).length() > 0) { pathTokens.nextToken(); } GlobalContext globalContext = GlobalContext.getInstance(request); if (!StringHelper.isEmpty(globalContext.getPathPrefix())) { pathTokens.nextToken(); }
			 */
			if (pathTokens.hasMoreTokens()) {
				if (pathTokens.nextToken().equals("preview")) {
					res = true;
				}
			}
		}
		return res;
	}

	public static boolean isTime(HttpServletRequest request) {
		return isTime(request, false);
	}

	public static boolean isTime(HttpServletRequest request, boolean uriPrefixed) {
		boolean res = false;
		String realPath = request.getServletPath();
		if (realPath != null) {
			StringTokenizer pathTokens = new StringTokenizer(realPath, "/");
			if (uriPrefixed) {
				pathTokens.nextToken();
			}
			/*
			 * if (URLHelper.getPathPrefix(request).length() > 0) { pathTokens.nextToken(); } GlobalContext globalContext = GlobalContext.getInstance(request); if (!StringHelper.isEmpty(globalContext.getPathPrefix())) { pathTokens.nextToken(); }
			 */
			if (pathTokens.hasMoreTokens()) {
				String nextToken = pathTokens.nextToken();
				if (nextToken.equals("time")) {
					res = true;
				}
			}
		}
		return res;
	}

	public static boolean isView(HttpServletRequest request, boolean uriPrefixed) {
		boolean res = false;
		String realPath = request.getServletPath();
		if (realPath != null) {
			StringTokenizer pathTokens = new StringTokenizer(realPath, "/");
			if (uriPrefixed) {
				pathTokens.nextToken();
			}
			/*
			 * if (URLHelper.getPathPrefix(request).length() > 0) { pathTokens.nextToken(); }
			 */
			/*
			 * GlobalContext globalContext = GlobalContext.getInstance(request); if (!StringHelper.isEmpty(globalContext.getPathPrefix())) { pathTokens.nextToken(); }
			 */
			if (pathTokens.hasMoreTokens()) {
				String nextToken = pathTokens.nextToken();
				if (nextToken.equals("view")) {
					res = true;
				}
			}
		}
		return res;
	}

	public static boolean isView(String path) {
		return isView(path, false);
	}

	public static boolean isView(String path, boolean uriPrefixed) {
		boolean res = false;
		if (path != null) {
			StringTokenizer pathTokens = new StringTokenizer(path, "/");
			if (pathTokens.hasMoreTokens()) {
				if (uriPrefixed) {
					pathTokens.nextToken();
				}
				String next = pathTokens.nextToken();
				if (next.equals("edit") || next.equals("preview") || next.equals("ajax") || next.equals("admin")) {
					return false;
				} else if (next.equals("view")) {
					return true;
				} else if (pathTokens.hasMoreTokens()) {
					if (pathTokens.nextToken().equals("view")) {
						return true;
					}
				}
			}
		}
		return res;
	}

	// public static MenuElement[] getLastMenuElement(ContentContext ctx) throws
	// Exception {
	// NavigationDAO dao = NavigationDAO.createDAO(ctx.getRequest());
	// MenuElement menu = dao.getFullMenu(ctx);
	// MenuElement childs = menu.searchChild(ctx.getPath());
	// MenuElement[] res = new MenuElement[0];
	// if (childs != null) {
	// res = childs.getChildMenuElements();
	// }
	// return res;
	// }

	public static void main(String[] args) {

		String lg = "";

		String realPath = "/view/fr";

		if (realPath != null) {
			String[] splitedPath = StringHelper.split(realPath, "/");

			System.out.println("**** splitedPath.length = " + splitedPath.length);

			int splitedPathMinSize = 3;

			if (splitedPath.length >= splitedPathMinSize) {
				lg = splitedPath[2].toLowerCase();
			}
		}

		System.out.println("** lg = " + lg);
	}

	public static String[] splitPath(String path) {
		ArrayList<String> res = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(path, "/");
		while (tokenizer.hasMoreTokens()) {
			res.add(tokenizer.nextToken());
		}
		String[] finalRes = new String[res.size()];
		res.toArray(finalRes);
		return finalRes;
	}

}