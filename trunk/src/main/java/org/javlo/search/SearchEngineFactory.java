package org.javlo.search;

import javax.servlet.http.HttpSession;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;

public class SearchEngineFactory {

	private static final String SESSION_ATTRIBUTE = ISearchEngine.class.getName();

	public static ISearchEngine getEngine(ContentContext ctx) {
		HttpSession session = ctx.getRequest().getSession();
		ISearchEngine out = (ISearchEngine) session.getAttribute(SESSION_ATTRIBUTE);
		if (out == null) {
			out = createEngine(ctx);
			session.setAttribute(SESSION_ATTRIBUTE, out);
		}
		return out;
	}

	private static ISearchEngine createEngine(ContentContext ctx) {
		try {
			StaticConfig staticConfig = ctx.getGlobalContext().getStaticConfig();
			String className = staticConfig.getSearchEngineClassName();
			Class<?> clazz = Class.forName(className);
			return (ISearchEngine) clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Exception in SearchEngine creation: " + e.getMessage(), e);
		}
	}

}
