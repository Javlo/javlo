/*
 * Created on 19-fevr.-2004
 */
package org.javlo.user;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.javlo.context.GlobalContext;

/**
 * @author pvandermaesen
 */
public class RolesFactory {

	private Map<String, Role> roles = new HashMap<String, Role>();

	private static final long serialVersionUID = 1L;

	/**
	 * create a static logger.
	 */
	public static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(RolesFactory.class.getName());

	public static RolesFactory getInstance(GlobalContext globalContext) {
		RolesFactory outFact = (RolesFactory) globalContext.getAttribute(RolesFactory.class.getName());
		if (outFact == null) {
			outFact = new RolesFactory();
			globalContext.setAttribute(RolesFactory.class.getName(), outFact);
		}
		return outFact;
	}

	public Role getRole(GlobalContext globalContext, String name) {
		if (name == null) {
			return null;
		} else {
			Role outRole = roles.get(name);
			if (outRole == null) {
				try {
					outRole = new Role(globalContext, name);
				} catch (IOException e) {
					e.printStackTrace();
				}
				roles.put(name, outRole);
			}
			return outRole;
		}
	}
	
	public void clear() {
		roles.clear();
	}
}