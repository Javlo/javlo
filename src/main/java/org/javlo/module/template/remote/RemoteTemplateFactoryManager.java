package org.javlo.module.template.remote;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jakarta.servlet.ServletContext;

import org.javlo.cache.ICache;
import org.javlo.context.GlobalContext;
import org.javlo.module.template.remote.freecsstemplates.FreeCSSTemplateFactory;
import org.javlo.ztatic.FileCache;

public class RemoteTemplateFactoryManager {

	private static Logger logger = Logger.getLogger(RemoteTemplateFactoryManager.class.getName());

	public static final String KEY = RemoteTemplateFactoryManager.class.getName();

	private static final List<String> ALL_FACTORIES = Arrays.asList(FreeCSSTemplateFactory.NAME);

	public static final RemoteTemplateFactoryManager getInstance(ServletContext application) {
		RemoteTemplateFactoryManager instance = (RemoteTemplateFactoryManager) application.getAttribute(KEY);
		if (instance == null) {
			instance = new RemoteTemplateFactoryManager();
			application.setAttribute(KEY, instance);
		}
		return instance;
	}

	public List<IRemoteResourcesFactory> getAllFactories(GlobalContext globalContext) throws Exception {
		List<IRemoteResourcesFactory> outFacotries = new LinkedList<IRemoteResourcesFactory>();
		for (String name : ALL_FACTORIES) {
			outFacotries.add(getRemoteTemplateFactory(globalContext, name));
		}
		return outFacotries;
	}

	public IRemoteResourcesFactory getRemoteTemplateFactory(GlobalContext globalContext, String name) throws Exception {
		ICache cache = globalContext.getCache("remote-template");
		if (cache == null) {
			logger.severe("cache 'remote-template' not found.");
			return null;
		}
		IRemoteResourcesFactory outFactory = null;
		if (cache.get(name) == null) {
			FileCache fileCache = FileCache.getInstance(globalContext.getServletContext());
			outFactory = (IRemoteResourcesFactory) fileCache.loadBean(name);
			if (outFactory == null) {
				if (FreeCSSTemplateFactory.NAME.equals(name)) {
					outFactory = new FreeCSSTemplateFactory();
					outFactory.refresh();
					cache.put(outFactory.getName(), outFactory);
					fileCache.storeBean(outFactory.getName(), outFactory);
				}
			}
		} else {
			outFactory = (IRemoteResourcesFactory) cache.get(name);
		}

		return outFactory;
	}

}
