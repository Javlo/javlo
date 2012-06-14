package org.javlo.remote;

import java.beans.XMLDecoder;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class RemoteResourceFactory {

	private GlobalContext globalContext;
	private RemoteResourceList remoteResources = null;
	private RemoteResourceList localeResources = null;

	private static Logger logger = Logger.getLogger(RemoteResourceFactory.class.getName());

	private static final String KEY = RemoteResourceFactory.class.getName();

	public static RemoteResourceFactory getInstance(GlobalContext globalContext) {
		RemoteResourceFactory outFact = (RemoteResourceFactory) globalContext.getAttribute(KEY);
		if (outFact == null) {
			outFact = new RemoteResourceFactory();
			outFact.globalContext = globalContext;
			globalContext.setAttribute(KEY, outFact);
		}
		return outFact;
	}

	public RemoteResourceList getLocalResources(ContentContext ctx) {
		if (remoteResources == null) {
			List<IRemoteResource> list = new ArrayList<IRemoteResource>();
			List<Template> templates;
			try {
				templates = TemplateFactory.getAllDiskTemplates(globalContext.getServletContext());
				for (Template template : templates) {
					list.add(new Template.TemplateBean(ctx, template));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			remoteResources = new RemoteResourceList();
			remoteResources.setList(list);
		}
		return remoteResources;
	}

	public IRemoteResource getLocalResource(ContentContext ctx, String name, String type) {
		List<IRemoteResource> resources = getLocalResources(ctx).getList();
		for (IRemoteResource iRemoteResource : resources) {
			if (iRemoteResource.getName().equals(name) && iRemoteResource.getType().equals(type)) {
				return iRemoteResource;
			}
		}
		return null;
	}

	public RemoteResourceList loadResources() throws IOException {
		if (localeResources == null) {
			StaticConfig staticConfig = StaticConfig.getInstance(globalContext.getServletContext());
			URL url = new URL(staticConfig.getMarketURL());
			logger.info("load remote resources from : " + url);
			URLConnection conn = url.openConnection();
			XMLDecoder decoder = new XMLDecoder(conn.getInputStream());
			localeResources = (RemoteResourceList) decoder.readObject();
			logger.info("resources loaded : " + localeResources.getList().size());
		}
		return localeResources;
	}

	public IRemoteResource loadResource(String id) throws IOException {
		List<IRemoteResource> resources = loadResources().getList();
		for (IRemoteResource iRemoteResource : resources) {
			if (iRemoteResource.getId().equals(id)) {
				return iRemoteResource;
			}
		}
		return null;
	}
	
	/*protected List<IRemoteResource> getRemoteComponents(GlobalContext globalContexdt) {
		List<IRemoteResource> outResources = new ArrayList<IRemoteResource>();
		
		IContentVisualComponent[] components = ComponentFactory.getComponents(globalContext);
		for (IContentVisualComponent comp : components) {
			comp.getClass().getClassLoader().-
		}
		
		return outResources;
	}*/

	public void clear() {
		remoteResources = null;
		localeResources = null;
	}

}
