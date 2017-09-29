package org.javlo.service.visitors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;

public class CookiesService {

	public static final int ALWAYS_STATUS = 9;
	public static final int NOCHOICE_STATUS = 2;
	public static final int ACCEPTED_STATUS = 1;
	public static final int REFUSED_STATUS = 0;

	private Boolean accepted = null;

	private boolean cookiesHidden = false;

	public static final String SESSION_KEY = "cookiesService";

	public static final CookiesService getInstance(ContentContext ctx) throws Exception {
		HttpSession session = ctx.getRequest().getSession();
		CookiesService outService = (CookiesService) session.getAttribute(SESSION_KEY);
		if (outService == null) {
			outService = new CookiesService();
			session.setAttribute(SESSION_KEY, outService);
		}
		if (ctx.getRequest().getAttribute("SESSION_KEY") == null) {
			outService.refresh(ctx);
		}

		return outService;
	}
	
	public void refresh(ContentContext ctx) {
		try {
			if (ctx.getCurrentTemplate() != null) {
				ctx.getRequest().setAttribute("SESSION_KEY",this);
				if (ctx.getGlobalContext().isCookies()) {
					Cookie cookie = NetHelper.getCookie(ctx.getRequest(), ctx.getCurrentTemplate().getCookiesMessageName());
					if (cookie != null) {
						if (cookie.getValue().equals("" + ALWAYS_STATUS)) {
							setAccepted(false);
						} else if (cookie.getValue().equals("" + ACCEPTED_STATUS)) {
							setAccepted(true);
						}
						cookiesHidden = true;
					}
				} else {
					cookiesHidden = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private CookiesService() {
	}

	public Boolean getAccepted() {
		return accepted;
	}

	public void setAccepted(Boolean accepted) {
		this.accepted = accepted;
	}

	public boolean isDisplayMessage() {
		if (cookiesHidden) {
			return false;
		}
		return accepted == null;
	}

}
