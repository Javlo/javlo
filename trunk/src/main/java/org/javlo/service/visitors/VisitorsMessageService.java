package org.javlo.service.visitors;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpSession;

public class VisitorsMessageService {
	
	private Set<String> alReadyDisplayed = new HashSet<String>();
	
	public static final String SESSION_KEY = "visitorsMessageService";
	
	public static final VisitorsMessageService getInstance(HttpSession session) {
		VisitorsMessageService outService = (VisitorsMessageService)session.getAttribute(SESSION_KEY);
		if (outService == null) {
			outService = new VisitorsMessageService(session);
			session.setAttribute(SESSION_KEY, outService);
		}
		return outService;
	}

	private VisitorsMessageService(HttpSession session) {
	}
	
	/**
	 * mark a message as displayed to the visitors.
	 * @param key
	 */
	public void markAsDisplayed(String key) {
		alReadyDisplayed.add(key);
	}
	
	/**
	 * check if a message was displayed to the visitor.
	 * @param key the key of the message
	 * @return
	 */
	public boolean isAlReadyDisplayed(String key) {
		return alReadyDisplayed.contains(key);
	}

}
