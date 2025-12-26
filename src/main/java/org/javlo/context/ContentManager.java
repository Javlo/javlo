package org.javlo.context;

import jakarta.servlet.http.HttpServletRequest;
import org.javlo.config.StaticConfig;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.service.GeoService;
import org.javlo.service.RequestService;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

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

			String lg = getLanguage(ctx.getRequest(), 0);
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			if (globalContext.getSpecialConfig().isSwitchCountry()) {
				if (!globalContext.getContentLanguages().contains(lg)) {
					String lgClient = ctx.getRequest().getLocale().getLanguage();
					String clientCode = lgClient + '-' + ctx.getCountry();
					for (String lgChoice : globalContext.getLanguages()) {
						if (lgChoice.equalsIgnoreCase(clientCode)) {
							lg = lgChoice;
							break;
						}
					}
					logger.info("switch content language to : "+lg+" uri: "+ctx.getRequest().getRequestURI());
				}
			}

			if (!globalContext.getContentLanguages().contains(lg)) {
				lg = globalContext.getDefaultLanguages().iterator().next();
				if (lg.trim().isEmpty()) {
					lg = globalContext.getLanguages().iterator().next();
				}
			}

			return lg;

	}

	public static String getContentCountry(ContentContext ctx) {
		String ct = "";
		String realPath = RequestService.getURI(ctx.getRequest());
		GlobalContext.getInstance(ctx.getRequest());
		if (realPath != null) {
			String[] splitedPath = StringHelper.split(realPath, "/");
			int splitedPathMinSize = 3;
			if (splitedPath.length >= splitedPathMinSize) {
				ct = splitedPath[2].toLowerCase();
			}
		}
		String[] lgs = StringHelper.split(ct, ""+ContentContext.COUNTRY_LG_SEP);
		if (lgs.length > 1) {
			ct = lgs[1];
		} else {
			ct = ctx.getCountry();
		}
		return ct;
	}
	
	public static String getContextName(HttpServletRequest request) {

		String realPath = RequestService.getURI(request);

		if (realPath.startsWith("/")) {
			realPath = realPath.substring(1);
		}

		StaticConfig staticConfig = StaticConfig.getInstance(request.getServletContext());
		String[] splitedPath = StringHelper.split(realPath, "/");

		if (!staticConfig.isHostDefineSite() && splitedPath.length > 0) {
			return splitedPath[0];
		}

		return null;
	}

	public static String getErrorLg(String lg) {
		String res = "undefined language";
		if (lg.equals("fr")) {
			res = "Cette page n'est pas encore disponible en francais.";
		} else if (lg.equals("en")) {
			res = "Sorry, this page does not yet exist in english.";
		} else if (lg.equals("nl")) {
			res = "Sorry, dit pagina bestaat nog niet in het nederlands.";
		}
		return res;
	}

	public static String getCountry(HttpServletRequest request) {
		String localCountry = request.getLocale().getCountry();
		if (StringHelper.isEmpty(localCountry)) {
			String ip = NetHelper.getClientIp(request);
			localCountry = GeoService.getIpInfoBean(ip).getCountryCode();
			if (StringHelper.isEmpty(localCountry)) {
				localCountry = StaticConfig.getInstance(request.getSession()).getLocaleCountry();
			}
		}
		return localCountry;
	}

	public static String getLanguage(HttpServletRequest request) {
		String lg = getLanguage(request, 0);
		GlobalContext globalContext = GlobalContext.getInstance(request);

		if (globalContext.getSpecialConfig().isSwitchCountry()) {
			if (!globalContext.getLanguages().contains(lg)) {
				String lgClient = request.getLocale().getLanguage();
				String clientCode = lgClient + '-' + getCountry(request);
				for (String lgChoice : globalContext.getLanguages()) {
					if (lgChoice.equalsIgnoreCase(clientCode)) {
						lg = lgChoice;
						break;
					}
				}
				logger.info("switch language to : "+lg);
			}
		}

		if (!globalContext.getLanguages().contains(lg)) {
			lg = globalContext.getDefaultLanguages().iterator().next();
			if (lg.trim().length() == 0) {
				lg = globalContext.getLanguages().iterator().next();
			}
		}

		return lg;
	}

	public static String getLanguage(HttpServletRequest request, int index) {
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

		String[] lgs = StringHelper.split(lg, ""+ContentContext.CONTENT_LG_SEP);
		if (index < lgs.length) {
			return lgs[index];
		} else {
			if (index == 1) {
				return null;
			} else {
				lgs = StringHelper.split(lg, ""+ContentContext.COUNTRY_LG_SEP);
				if (index < lgs.length) {
					return lgs[index];
				} else {
					return null;
				}
			}
		}
	}

	public static String getPath(HttpServletRequest request) {
		String path = null;
		String realPath = RequestService.getURI(request);
		if (realPath != null) {
			GlobalContext.getInstance(request);
			path = getPath(realPath);
		}
		return path;
	}

	public static String getPath(String realPath) {
		return getPath(realPath, true);
	}
	
	public static String getPath(String realPath, boolean renderMode) {
		String path = null;
		if (realPath != null) {
			String[] splitedPath = StringHelper.split(realPath, "/");
			int splitedPathMinSize = 3;
			if (!renderMode) {
				splitedPathMinSize = 2;
			}
			path = "";
			for (int i = splitedPathMinSize; i < splitedPath.length; i++) {
				path = path + '/' + splitedPath[i];
			}
		}
		if (path == null || path.trim().length() == 0) {
			path = "/";
		}
		// remove extension if exist (.html)
		if (path.indexOf('.') >= 0) {
			path = path.substring(0, path.lastIndexOf('.'));
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

	public static boolean isAjax(HttpServletRequest request) {
		return isAjax(request, false);
	}

	public static boolean isAjax(HttpServletRequest request, boolean uriPrefixed) {
		boolean res = false;
		String realPath = request.getServletPath();
		if (realPath != null) {
			StringTokenizer pathTokens = new StringTokenizer(realPath, "/");
			if (uriPrefixed) {
				pathTokens.nextToken();
			}
			if (pathTokens.hasMoreTokens()) {
				String nextToken = pathTokens.nextToken();
				if (nextToken.equals("ajax")) {
					res = true;
				}
			}
		}
		return res;
	}

	public static boolean isEdit(HttpServletRequest request, boolean uriPrefixed) {
		boolean res = false;
		String realPath = request.getServletPath();
		if (realPath != null) {
			StringTokenizer pathTokens = new StringTokenizer(realPath, "/");
			if (uriPrefixed) {
				pathTokens.nextToken();
			}
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
				String nextToken = pathTokens.nextToken();
				if (nextToken.equals("preview")) {
					res = true;
				} else if (nextToken.equals("ajax")) {
					res = true;
				}
			}
		}
		return res;
	}
	
	public static boolean isView(HttpServletRequest request) {
		return isView(request, false);
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
		System.out.println("** path = " + getPath("/fr/primes-chaudieres-2024"));
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