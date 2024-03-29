/*
 * Created on Aug 13, 2003
 */
package org.javlo.actions;

import java.lang.reflect.Method;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author pvandermaesen
 * manage the actions for wcms
 */
public class SearchActionManager {

	static final String METHOD_PREFIX = "perform";
	
	static final String formatActionName(String name) {
		String start = name.substring(0, 1);
		String end = "";
		if (name.length() > 1) {
			end = name.substring(1, name.length());
		}
		return start.toUpperCase() + end.toLowerCase();
	}

	static public final String perform(String actionName, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String methodName = METHOD_PREFIX + formatActionName(actionName);
		Class[] params = { HttpServletRequest.class, HttpServletResponse.class };
		Method method = SearchActions.class.getMethod(methodName, params);
		Object[] objects = { request, response };
		return (String) method.invoke(new SearchActions(), objects);
	}
}
