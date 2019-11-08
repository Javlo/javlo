package org.javlo.helper;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestHelper {
	
	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(RequestHelper.class.getName());

	private static class PathCookie extends Cookie {

		private String path = "/";

		public PathCookie(String arg0, String arg1, String inPath) {
			super(arg0, arg1);
			if (inPath != null) {
				path = inPath;
			}
		}

		@Override
		public String getPath() {
			return path;
		}

	}

	public static final String CRYPTED_PARAM_NAME = "cparam";

	public static final String CLOSE_WINDOW_PARAMETER = "close-window";
	public static final String CLOSE_WINDOW_URL_PARAMETER = "close-window-url";

	public static boolean isCookie(HttpServletRequest request, String key, String value) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(key) && cookie.getValue().equals(value)) {
					return true;
				}
			}
		}
		return false;
	}

	public static String getCookieValue(HttpServletRequest request, String key) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(key)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	public static void setCookieValue(HttpServletResponse response, String key, String value, int expiry, String path) {				
		Cookie cokkie = new PathCookie(key, value, path);		
		cokkie.setMaxAge(expiry);
		response.addCookie(cokkie);
	}

	public static void setCookieValue(HttpServletResponse response, String key, String value, int expiry) {
		Cookie cokkie = new Cookie(key, value);
		cokkie.setMaxAge(expiry);
		response.addCookie(cokkie);
	}

	public static void setCookieValue(HttpServletResponse response, String key, String value) {
		Cookie cokkie = new Cookie(key, value);
		response.addCookie(cokkie);
	}
	
	public static final void setJSONType(HttpServletResponse response) {
		response.setContentType("application/json");
	}

}