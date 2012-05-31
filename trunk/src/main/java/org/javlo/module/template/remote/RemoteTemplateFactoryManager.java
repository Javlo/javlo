package org.javlo.module.template.remote;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

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

	public List<IRemoteTemplateFactory> getAllFactories(GlobalContext globalContext) throws Exception {
		List<IRemoteTemplateFactory> outFacotries = new LinkedList<IRemoteTemplateFactory>();
		for (String name : ALL_FACTORIES) {
			outFacotries.add(getRemoteTemplateFactory(globalContext, name));
		}
		return outFacotries;
	}

	public IRemoteTemplateFactory getRemoteTemplateFactory(GlobalContext globalContext, String name) throws Exception {
		Cache cache = globalContext.getCache("remote-template");
		if (cache == null) {
			logger.severe("cache 'remote-template' not found.");
			return null;
		}
		IRemoteTemplateFactory outFactory = null;
		if (cache.get(name) == null) {
			FileCache fileCache = FileCache.getInstance(globalContext.getServletContext());
			outFactory = (IRemoteTemplateFactory) fileCache.loadBean(name);
			if (outFactory == null) {
				if (FreeCSSTemplateFactory.NAME.equals(name)) {					
					outFactory = new FreeCSSTemplateFactory();
					outFactory.refresh();
					cache.put(new Element(outFactory.getName(), outFactory));					
					fileCache.storeBean(outFactory.getName(), outFactory);
				}
			}
		} else {			
			outFactory = (IRemoteTemplateFactory) cache.get(name).getValue();
		}

		return outFactory;
	}

}
