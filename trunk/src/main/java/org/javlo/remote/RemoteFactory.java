package org.javlo.remote;

import java.beans.XMLDecoder;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class RemoteFactory {

	private GlobalContext globalContext;
	private RemoteResourceList remoteResources = null;
	private RemoteResourceList localeResources = null;

	private static Logger logger = Logger.getLogger(RemoteFactory.class.getName());

	private static final String KEY = RemoteFactory.class.getName();

	public static RemoteFactory getInstance(GlobalContext globalContext) {
		RemoteFactory outFact = (RemoteFactory) globalContext.getAttribute(KEY);
		if (outFact == null) {
			outFact = new RemoteFactory();
			outFact.globalContext = globalContext;
			globalContext.setAttribute(KEY, outFact);
		}
		return outFact;
	}

	public RemoteResourceList getAllResources(ContentContext ctx) {
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

	public RemoteResourceList loadResource(ServletContext application) throws IOException {
		if (localeResources == null) {
			StaticConfig staticConfig = StaticConfig.getInstance(application);
			URL url = new URL(staticConfig.getMarketURL());
			logger.info("load remote resources from : "+url);
			URLConnection conn = url.openConnection();
			XMLDecoder decoder = new XMLDecoder(conn.getInputStream());
			localeResources = (RemoteResourceList) decoder.readObject();
			logger.info("resources loaded : "+localeResources.getList().size());
		}
		return localeResources;
	}

	public void clear() {
		remoteResources = null;
		localeResources = null;
	}

}
