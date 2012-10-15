package org.javlo.remote;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.MetaTitle;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.template.remote.freecsstemplates.FreeCSSTemplateFactory;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

/**
 * access to the local version of remotable resources
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class LocalResourceFactory extends AbstractResourceFactory {

	public static final String TYPE_TEMPLATES = "templates";
	public static final String TYPE_COMPONENTS = "components";
	public static final String TYPE_MODULES = "modules";

	private static final ArrayList<String> types = new ArrayList<String>(Arrays.asList(new String[] { TYPE_TEMPLATES, TYPE_COMPONENTS, TYPE_MODULES }));

	private GlobalContext globalContext;

	private final Map<String, RemoteResourceList> remoteResourcesCache = new HashMap<String, RemoteResourceList>();
	private List<String> typesCache;
	private Map<String, List<String>> categoriesCache;
	private RemoteResourceList localeResources = null;

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

	public RemoteResourceList getResourcesForProxy(ContentContext ctx, String type, String category) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

		String key = type + '-' + category;
		RemoteResourceList localeResourcesForProxy = remoteResourcesCache.get(key);
		if (localeResourcesForProxy == null) {
			List<IRemoteResource> list = new ArrayList<IRemoteResource>();

			if (type == null || type.equals(TYPE_TEMPLATES)) {

				/**** LOCAL TEMPLATE ****/
				List<Template> templates;
				try {
					templates = TemplateFactory.getAllDiskTemplates(globalContext.getServletContext());
					for (Template template : templates) {
						IRemoteResource bean = new Template.TemplateBean(ctx, template);
						if (category == null || URLEncoder.encode(bean.getCategory(), ContentContext.CHARACTER_ENCODING).equals(category)) {
							template.getRenderer(ctx); // import template into webapp
							list.add(bean);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				localeResourcesForProxy = new RemoteResourceList();
				localeResourcesForProxy.setList(list);
				localeResourcesForProxy.setLayout(RemoteResourceList.GALLERY_LAYOUT);

				/**** FREE CSS TEMPLATE ****/
				FreeCSSTemplateFactory outFactory = new FreeCSSTemplateFactory();
				try {
					outFactory.refresh();
					List<IRemoteResource> resourceList = outFactory.getResources();
					if (category == null) {
						localeResourcesForProxy.getList().addAll(resourceList);
					} else {
						for (IRemoteResource bean : resourceList) {
							if (URLEncoder.encode(bean.getCategory(), ContentContext.CHARACTER_ENCODING).equals(category)) {
								localeResourcesForProxy.getList().add(bean);
							}
						}
					}
					localeResourcesForProxy.setLayout(RemoteResourceList.GALLERY_LAYOUT);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (type == null || type.equals(TYPE_COMPONENTS)) {
				/**** components ****/
				localeResourcesForProxy = new RemoteResourceList();
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
				IContentVisualComponent[] components = ComponentFactory.getComponents(globalContext);
				String currentCategory = "undefined";
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				for (IContentVisualComponent comp : components) {
					if (comp instanceof MetaTitle) {
						currentCategory = i18nAccess.getText(comp.getValue(ctx));
					} else {
						IRemoteResource bean = getRemoteBean(comp, new Locale(globalContext.getEditLanguage(ctx.getRequest().getSession())));
						bean.setCategory(currentCategory);
						if (category == null || URLEncoder.encode(currentCategory, ContentContext.CHARACTER_ENCODING).equals(category)) {
							localeResourcesForProxy.getList().add(bean);
						}
					}
				}
				localeResourcesForProxy.setLayout(RemoteResourceList.TABLE_LAYOUT);
			}
			remoteResourcesCache.put(key, localeResourcesForProxy);
		}
		return localeResourcesForProxy;
	}

	public void clear() {
		localeResources = null;
		remoteResourcesCache.clear();
	}

	public ArrayList<String> getTypes() {
		return types;
	}

}
