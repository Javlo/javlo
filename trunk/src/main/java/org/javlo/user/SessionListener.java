package org.javlo.user;

import java.util.logging.Logger;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(SessionListener.class.getName());

	@Override
	public void sessionCreated(HttpSessionEvent event) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		/*
		 * GlobalContext globalContext = GlobalContext.getInstance(event.getSession());
		 * 
		 * IUserFactory fact = UserFactory.createUserFactory(event.getSession()); Principal logoutUser = fact.getCurrentUser(event.getSession()); if (logoutUser != null) { logger.info("logout : " + logoutUser); globalContext.logout(logoutUser); }
		 */// TODO: check how we can remove logged user on globalContext
	}

}
