package org.javlo.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.module.template.remote.freecsstemplates.FreeCSSTemplateFactory;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;


/**
 * access to the local version of remotable resources
 * @author Patrick Vandermaesen
 *
 */
public class LocalResourceFactory extends AbstractResourceFactory {
	

	public static final String TYPE_TEMPLATES = "templates";
	public static final String TYPE_COMPONENTS = "components";
	public static final String TYPE_MODULES = "modules";
	
	private static final ArrayList<String> types = new ArrayList<String>(Arrays.asList(new String[] {TYPE_TEMPLATES, TYPE_COMPONENTS, TYPE_MODULES }));
	
	private GlobalContext globalContext;

	private RemoteResourceList localeResources = null;
	private RemoteResourceList localeResourcesForProxy = null;
	
	private static final String KEY = LocalResourceFactory.class.getName();

	public static LocalResourceFactory getInstance(GlobalContext globalContext) {
		LocalResourceFactory outFact = (LocalResourceFactory) globalContext.getAttribute(KEY);
		if (outFact == null) {
			outFact = new LocalResourceFactory();
			outFact.globalContext = globalContext;
			globalContext.setAttribute(KEY, outFact);
		}
		return outFact;
	}
	
	public static IRemoteResource getRemoteBean(IContentVisualComponent comp, Locale locale) {
		RemoteBean outBean = new RemoteBean();
		outBean.setName(comp.getType());
		outBean.setVersion(comp.getVersion());
		outBean.setDescription(comp.getDescription(locale));
		outBean.setAuthors(comp.getAuthors());
		return outBean;
	}

	public IRemoteResource getLocalResource(ContentContext ctx, String id) {
		List<IRemoteResource> resources = getResources(ctx).getList();
		for (IRemoteResource iRemoteResource : resources) {
			if (iRemoteResource.getId().equals(id)) {
				return iRemoteResource;
			}
		}
		return null;
	}

	public IRemoteResource getLocalResource(ContentContext ctx, String name, String type) {
		List<IRemoteResource> resources = getResources(ctx).getList();
		for (IRemoteResource iRemoteResource : resources) {
			if (iRemoteResource.getName().equals(name) && iRemoteResource.getType().equals(type)) {
				return iRemoteResource;
			}
		}
		return null;
	}
	
	@Override
	public RemoteResourceList getResources(ContentContext ctx) {
		if (localeResources == null) {
			List<IRemoteResource> list = new ArrayList<IRemoteResource>();
			
			/**** LOCAL TEMPLATE ****/
			List<Template> templates;
			try {
				templates = TemplateFactory.getAllDiskTemplates(globalContext.getServletContext());
				for (Template template : templates) {
					template.getRenderer(ctx); // import template into webapp
					list.add(new Template.TemplateBean(ctx, template));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			localeResources = new RemoteResourceList();
			localeResources.setList(list);
		}
		return localeResources;
	}
	
	public RemoteResourceList getResourcesForProxy(ContentContext ctx, String type) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		if (localeResourcesForProxy == null) {
			List<IRemoteResource> list = new ArrayList<IRemoteResource>();
			
			/**** LOCAL TEMPLATE ****/
			List<Template> templates;
			try {
				templates = TemplateFactory.getAllDiskTemplates(globalContext.getServletContext());
				for (Template template : templates) {
					template.getRenderer(ctx); // import template into webapp
					list.add(new Template.TemplateBean(ctx, template));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			localeResourcesForProxy = new RemoteResourceList();
			localeResourcesForProxy.setList(list);
			
			/**** FREE CSS TEMPLATE ****/
			FreeCSSTemplateFactory outFactory = new FreeCSSTemplateFactory();			
			try {
				outFactory.refresh();
				localeResourcesForProxy.getList().addAll(outFactory.getResources());
			} catch (Exception e) {				
				e.printStackTrace();
			}
			
			/**** components ****/
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			IContentVisualComponent[] components = ComponentFactory.getComponents(globalContext);
			for (IContentVisualComponent comp : components) {
				localeResourcesForProxy.getList().add(getRemoteBean(comp, new Locale(globalContext.getEditLanguage())));
			}
			
		}
		return localeResourcesForProxy;
	}

	public void clear() {
		localeResources = null;
		localeResourcesForProxy = null;
	}

	public ArrayList<String> getTypes() {		
		return types;
	}

}
