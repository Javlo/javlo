package org.javlo.data.source;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.config.StaticConfig;
import org.javlo.context.GlobalContext;

public class DataSourceFactory {

	private static Logger logger = Logger.getLogger(DataSourceFactory.class.getName());

	private static final String KEY = DataSourceFactory.class.getName();

	private StaticConfig staticConfig;

	public DataSourceFactory getInstance(GlobalContext globalContext) {
		DataSourceFactory fact = (DataSourceFactory) globalContext.getAttribute(KEY);
		if (fact == null) {
			fact = new DataSourceFactory();
			StaticConfig staticConfig = globalContext.getStaticConfig();
			fact.staticConfig = staticConfig;
			globalContext.setAttribute(KEY, fact);
		}
		return fact;
	}

	public IDataSource getSource(String name) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		Map<String, String> clazz = staticConfig.getDataSource();
		String className = clazz.get(name);
		if (className != null) {
			return (IDataSource) Class.forName(className).getConstructor(GlobalContext.class).newInstance();
		}
		logger.warning("data source not found : " + name);
		return null;
	}
}
