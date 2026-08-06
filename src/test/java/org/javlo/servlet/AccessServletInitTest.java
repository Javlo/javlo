package org.javlo.servlet;

import junit.framework.TestCase;

/**
 * L'initialisation statique d'AccessServlet ne doit jamais empêcher le
 * chargement de la classe : hors conteneur, ou sur une instance dont le
 * dossier de logs a disparu, le servlet doit rester chargeable.
 */
public class AccessServletInitTest extends TestCase {

	public void testClassLoadsWithoutCatalinaBase() throws Exception {
		String previous = System.getProperty("catalina.base");
		System.clearProperty("catalina.base");
		try {
			Class.forName("org.javlo.servlet.AccessServlet");
		} finally {
			if (previous != null) {
				System.setProperty("catalina.base", previous);
			}
		}
	}
}
