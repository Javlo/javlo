package org.javlo.service.visitors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.utils.KeyMap;
import org.javlo.utils.ListMapValueValue;

public class CookiesService {

	public static final String COOKIES_TYPE_TECH = "technic";
	public static final String COOKIES_TYPE_ANAL = "analytics";
	public static final String COOKIES_TYPE_SOCIAL = "social";
	public static final String COOKIES_TYPE_MEDIA = "media";
	public static final String COOKIES_TYPE_PERSO = "personalization";
	public static final String COOKIES_TYPE_PUB = "pub";
	
	public static final List<String> COOKIES_TYPES = Arrays.asList(new String[] {COOKIES_TYPE_ANAL, COOKIES_TYPE_SOCIAL, COOKIES_TYPE_MEDIA, COOKIES_TYPE_PERSO, COOKIES_TYPE_PUB});

	public static final int ALWAYS_STATUS = 9;
	public static final int NOCHOICE_STATUS = 2;
	public static final int ACCEPTED_STATUS = 1;
	public static final int NOT_ACCEPTED_STATUS = 5;
	public static final int REFUSED_STATUS = 0;

	private Boolean accepted = null;
	
	private List<String> acceptedTypes = new LinkedList<>();

	private boolean cookiesHidden = false;

	public static final String SESSION_KEY = "cookiesService";

	public static final CookiesService getInstance(ContentContext ctx) throws Exception {
		HttpSession session = ctx.getRequest().getSession();
		CookiesService outService = (CookiesService) session.getAttribute(SESSION_KEY);
		if (outService == null) {
			outService = new CookiesService();
			session.setAttribute(SESSION_KEY, outService);
		}
		if (ctx.getRequest().getAttribute(SESSION_KEY) == null) {
			outService.refresh(ctx);
		}

		return outService;
	}
	
	public void refresh(ContentContext ctx) {
		try {
			if (ctx.getCurrentTemplate() != null) {
				ctx.getRequest().setAttribute(SESSION_KEY,this);
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
					Cookie acceptedTypesCookie = NetHelper.getCookie(ctx.getRequest(), ctx.getCurrentTemplate().getCookiesTypeName());
					if (acceptedTypesCookie != null && !StringHelper.isEmpty(acceptedTypesCookie.getValue())) {
						setAcceptedTypes(StringHelper.stringToCollection(acceptedTypesCookie.getValue(), ","));
					}
					
					Cookie cookiesTypeAccepted = NetHelper.getCookie(ctx.getRequest(), ctx.getCurrentTemplate().getCookiesTypeName());
					if (cookiesTypeAccepted != null) {
						acceptedTypes = StringHelper.stringToCollection(cookiesTypeAccepted.getValue(), "-");
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
	
	public List<String> getCookiesTypes() {
		return COOKIES_TYPES;
	}

	public Map<String, String> getAcceptedTypes() {
		if (accepted == null) {			
			return new ListMapValueValue<String>(acceptedTypes);
		} else if (accepted) {
			return KeyMap.stringInstance;
		} else {
			return Collections.EMPTY_MAP;
		}
	}

	public void setAcceptedTypes(List<String> acceptedTypes) {
		if (acceptedTypes != null) {
			cookiesHidden = true;
		}
		this.acceptedTypes = acceptedTypes;
	}
	
	public void reset(ContentContext ctx) {
		ctx.getSession().removeAttribute(SESSION_KEY);
	}

}
