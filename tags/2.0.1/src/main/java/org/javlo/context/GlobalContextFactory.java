package org.javlo.context;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.javlo.config.StaticConfig;
import org.javlo.filter.PropertiesFilter;

public class GlobalContextFactory {
	
	private static final String MASTER_CONTEXT_KEY = "masterContext";

	public static final Collection<GlobalContext> getAllGlobalContext(HttpSession session) throws ConfigurationException, IOException {
		StaticConfig staticConfig = StaticConfig.getInstance(session);
		Collection<GlobalContext> result = new LinkedList<GlobalContext>();
		File contextDir = new File(staticConfig.getContextFolder());
		File[] childs = contextDir.listFiles(new PropertiesFilter());
		if (childs != null) {
			for (int i = 0; i < childs.length; i++) {
				GlobalContext globalContext = GlobalContext.getInstance(session.getServletContext(), staticConfig, childs[i]);
				result.add(globalContext);
			}
		}
		return result;
	}
	
	public static GlobalContext getMasterGlobalContext(HttpSession session) throws ConfigurationException, IOException {
		GlobalContext masterContext = (GlobalContext) session.getServletContext().getAttribute(MASTER_CONTEXT_KEY);
		StaticConfig staticConfig = StaticConfig.getInstance(session.getServletContext());
		if (masterContext == null || !masterContext.getContextKey().equals(staticConfig.getMasterContext())) {
			for (GlobalContext globalContext : getAllGlobalContext(session)) {
				if (staticConfig.getMasterContext().equals(globalContext.getContextKey())) {
					masterContext = globalContext;
					break;
				}
			}
			session.getServletContext().setAttribute(MASTER_CONTEXT_KEY, masterContext);
		}
		return masterContext;
	}

}
