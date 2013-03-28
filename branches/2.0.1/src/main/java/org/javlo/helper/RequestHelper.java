package org.javlo.helper;

import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.mailing.FeedBackMailingBean;
import org.javlo.module.mailing.MailingAction;
import org.javlo.service.DataToIDService;
import org.javlo.service.RequestService;

public class RequestHelper {

	public static final String CRYPTED_PARAM_NAME = "cparam";

	public static final String CLOSE_WINDOW_PARAMETER = "close-window";
	public static final String CLOSE_WINDOW_URL_PARAMETER = "close-window-url";

	public static final void traceMailingFeedBack(ContentContext ctx) {
		ServletContext application = ctx.getRequest().getSession().getServletContext();
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String mfb = requestService.getParameter(MailingAction.MAILING_FEEDBACK_PARAM_NAME, null);
		if (mfb != null) {
			DataToIDService serv = DataToIDService.getInstance(application);
			Map<String, String> params = StringHelper.uriParamToMap(serv.getData(mfb));
			String id = params.get("mailing");
			Enumeration<String> names = ctx.getRequest().getHeaderNames();
			String userAgent = null;
			while (names.hasMoreElements()) {
				String name = names.nextElement();
				if (name.trim().equalsIgnoreCase("user-agent")) {
					userAgent = ctx.getRequest().getHeader(name);
				}
			}
			if (id != null) {
				org.javlo.mailing.Mailing mailing = new org.javlo.mailing.Mailing();
				try {
					if (mailing.isExist(application, id)) {
						mailing.setId(application, id);
						FeedBackMailingBean bean = new FeedBackMailingBean();
						bean.setEmail(params.get("to"));
						bean.setAgent(userAgent);
						bean.setDate(new Date());
						bean.setUrl(ctx.getRequest().getPathInfo());
						mailing.addFeedBack(bean);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

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
		Cookie cokkie = new Cookie(key, value);
		cokkie.setMaxAge(expiry);
		cokkie.setPath(path);
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

}