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

}
