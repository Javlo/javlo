/**
 * Created on 9 oct. 2003
 */
package org.javlo.component.core;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.VFSClassLoader;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.UserInterfaceContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.ConfigHelper;
import org.javlo.module.content.ComponentWrapper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.utils.StructuredProperties;

import jakarta.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pvanderm
 */
public class ComponentFactory {
	
	public static String loadErrorMessage = null;

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ComponentFactory.class.getName());

	private ComponentFactory() {
	}; /* static class */

	private static final String KEY = ComponentFactory.class.getName();

	private static final String getKey(String contextKey) {
		return KEY + '_' + contextKey;
	}

	public static final void cleanComponentList(ServletContext application, GlobalContext globalContext) {
		String key = getKey(globalContext.getContextKey());
		application.removeAttribute(key);
	}

	/**
	 * return request scope component (add template component)
	 * 
	 * @param request
	 * @return list of application component + template components
	 * @throws Exception
	 */
	public static List<IContentVisualComponent> getComponents(ContentContext ctx, MenuElement page) throws Exception {
		String key;
		if (page == null) {
			key = "__components_request_key_nopage_" + ctx.getRenderMode();
		} else {
			key = "__components_request_key_" + page.getId() + '_' + ctx.getRenderMode();
		}
		List<IContentVisualComponent> array = Collections.EMPTY_LIST;
		List<IContentVisualComponent> outComp = (List<IContentVisualComponent>) ctx.getRequest().getAttribute(key);
		if (outComp == null) {
			Template template = null;
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			array = new LinkedList<IContentVisualComponent>();
			array.addAll(Arrays.asList(getComponents(ctx)));
			if (page != null) {
				template = TemplateFactory.getTemplate(ctx, page);
				if (template != null) {
					/* load dynamic component */
					if (!template.isTemplateInWebapp(ctx)) {
						template.importTemplateInWebapp(StaticConfig.getInstance(ctx.getRequest().getSession()), ctx);
					}
					List<Properties> propertiesClasses = template.getDynamicComponentsProperties(globalContext);
					if (propertiesClasses.size() > 0) {
						array.add(new MetaTitle("content.title.template"));
					} else {
						logger.fine("no business component found in template : " + template.getName());
					}
					for (Properties properties : propertiesClasses) {
						logger.fine("load dynamic component : " + properties.getProperty("component.type") + " [total:" + array.size() + "]");
						DynamicComponent comp = new DynamicComponent();
						StructuredProperties newProp = new StructuredProperties();
						newProp.putAll(properties);
						comp.setProperties(newProp);
						comp.setConfigProperties(properties);
						array.add(comp);
						comp.setValid(true);
					}
				} else {
					logger.fine("no template found for page : " + page.getName());
				}
			}
			ctx.getRequest().setAttribute(key, array);
		} else {
			array = outComp;
		}
		return array;

	}

	public static final void updateComponentsLogLevel(ServletContext application, Level level) throws Exception {

		logger.info("update components logger level : " + level);

		String[] classes = ConfigHelper.getComponentsClasses(application);
		for (String clazz : classes) {
			Class comp = null;
			try {
				try {
					comp = Class.forName(clazz);
				} catch (Throwable e) {
					logger.fine("update component log level : "+e.getMessage());
				}
				Field fields[] = comp.getDeclaredFields();
				for (Field field : fields) {
					synchronized (field) {
						boolean fieldAccess = field.isAccessible();
						field.setAccessible(true);
						if (field.getType().isAssignableFrom(Logger.class)) {
							Object loggerInstance = field.get(null);
							((Logger) loggerInstance).setLevel(level);
							logger.info("update logger for : " + comp);
						}
						field.setAccessible(fieldAccess);
					}
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
	}
	
	public static DynamicComponent getDynamicComponents(ContentContext ctx, String type) throws Exception {
		ContentContext noAreaCtx = ctx.getContextWithArea(null);
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		MenuElement root = content.getNavigation(noAreaCtx);
		ContentElementList pageContent = root.getContent(noAreaCtx);
		for (MenuElement page : root.getAllChildrenList()) {
			pageContent = page.getContent(noAreaCtx);
			while (pageContent.hasNext(noAreaCtx)) {
				IContentVisualComponent comp = pageContent.next(noAreaCtx);
				if (comp instanceof DynamicComponent && comp.getType().equals(type)) {
					return (DynamicComponent)comp;
				}
			}
		}
		return null;
	}

	public static List<DynamicComponent> getAllDynamicComponents(ContentContext ctx) throws Exception {
		ContentContext noAreaCtx = ctx.getContextWithArea(null);
		List<DynamicComponent> outComps = new LinkedList<DynamicComponent>();
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		MenuElement root = content.getNavigation(noAreaCtx);
		ContentElementList pageContent = root.getContent(noAreaCtx);
		/*
		 * while (pageContent.hasNext(noAreaCtx)) { IContentVisualComponent comp
		 * = pageContent.next(noAreaCtx); if (comp instanceof DynamicComponent)
		 * { outComps.add((DynamicComponent)comp); } }
		 */
		for (MenuElement page : root.getAllChildrenList()) {
			pageContent = page.getContent(noAreaCtx);
			while (pageContent.hasNext(noAreaCtx)) {
				IContentVisualComponent comp = pageContent.next(noAreaCtx);
				if (comp instanceof DynamicComponent) {
					outComps.add((DynamicComponent) comp);
				}
			}
		}
		return outComps;
	}

	public static Map<String, IContentVisualComponent> getComponents(ServletContext application) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Map<String, IContentVisualComponent> array = new HashMap<String, IContentVisualComponent>();
		String[] classes = ConfigHelper.getComponentsClasses(application);
		for (String clazz : classes) {
			logger.fine("load component : " + clazz);
			if (clazz.startsWith("--")) {
				array.put(clazz, new MetaTitle(clazz.substring(2).trim()));
			} else {
				try {
					String className = clazz;
					boolean visible = true;
					if (className.startsWith(".")) {
						className = className.substring(1);
						visible = false;
					}
					IContentVisualComponent comp;
					try {
						Class c = ComponentFactory.class.getClassLoader().loadClass(className);
						comp = (AbstractVisualComponent) c.newInstance();
					} catch (Throwable t) {
						//t.printStackTrace();
						logger.warning("getComponents application: "+t.getMessage());
						ComponentBean bean = new ComponentBean();
						bean.setValue(t.getMessage());
						comp = new Unknown(null, bean);
						loadErrorMessage = "component not found (ServletContext) : "+className;
					}
					comp.setValid(visible);
					array.put(clazz, comp);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return array;
	}
	
	public static IContentVisualComponent[] getComponents(ContentContext ctx) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		return getComponents(ctx, ctx.getGlobalContext());
	}

	public static IContentVisualComponent[] getComponents(ContentContext ctx, GlobalContext globalContext) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {		
		String key = getKey(globalContext.getContextKey());
		//IContentVisualComponent[] components = (IContentVisualComponent[]) globalContext.getServletContext().getAttribute(key);
		IContentVisualComponent[] components = (IContentVisualComponent[])ctx.getRequest().getAttribute(key);

		if (components == null) {
			ArrayList<AbstractVisualComponent> array = new ArrayList<AbstractVisualComponent>();
			String[] classes = ConfigHelper.getComponentsClasses(globalContext.getServletContext());
			List<String> selectedComponent = globalContext.getComponents();
			String group = null;
			for (String classe : classes) {
				logger.fine("load component : " + classe);
				if (classe.startsWith("--")) {
					group = classe.substring(2).trim();
					array.add(new MetaTitle(group));
				} else {
					try {
						String className = classe;
						boolean visible = true;
						boolean hidden = false;
						if (className.startsWith(".")) {
							className = className.substring(1);
							visible = false;
							hidden = true;
						} else {
							if (!selectedComponent.contains(className)) {
								visible = false;
							}
						}

						AbstractVisualComponent comp;
						try {
							Class c = ComponentFactory.class.getClassLoader().loadClass(className);
							comp = (AbstractVisualComponent) c.newInstance();
						} catch (Throwable t) {
							//t.printStackTrace();
							logger.warning("error on create component with globalContext : "+t.getMessage());
							ComponentBean bean = new ComponentBean();
							bean.setValue(t.getMessage());
							comp = new Unknown(null, bean);
							loadErrorMessage = "component not found (globalContext) : "+className;
						}

						comp.setValid(visible);
						comp.setHidden(hidden);
						comp.setGroup(group);
						array.add(comp);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			/* load dynamic component */
			List<Properties> propertiesClasses = ConfigHelper.getDynamicComponentsProperties(globalContext.getServletContext());
			if (propertiesClasses.size() > 0) {
				array.add(new MetaTitle("content.title.dynamic"));
			}

			for (Properties properties : propertiesClasses) {
				logger.info("load dynamic component : " + properties.getProperty("component.type") + " [total:" + array.size() + "]");
				DynamicComponent comp = new DynamicComponent();
				StructuredProperties newProp = new StructuredProperties();
				newProp.putAll(properties);
				comp.setProperties(newProp);
				comp.setConfigProperties(properties);
				array.add(comp);
				if (globalContext.getComponents().contains(comp.getKey())) {
					comp.setValid(true);
				}
			}
			
			// Load external components
			try {			
				
				File externalComponentFolder = new File(globalContext.getStaticConfig().getExternalComponentFolder());
				if (externalComponentFolder.exists() && externalComponentFolder.isDirectory()) {
					FileSystemManager vfsManager = VFS.getManager();
					List<String> jarClasses = new LinkedList<String>();
					List<FileObject> jarFiles = new LinkedList<FileObject>();
					FileObject rootFolder = vfsManager.resolveFile(externalComponentFolder.getAbsolutePath());
					for (FileObject fo : rootFolder.getChildren()) {
						if (vfsManager.canCreateFileSystem(fo)) {
							FileObject jarRoot = vfsManager.createFileSystem(fo);
							FileObject[] classFiles = jarRoot.findFiles(new FileSelector() {
								@Override
								public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
									return true;
								}

								@Override
								public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
									return fileInfo.getFile().getType() == FileType.FILE && "class".equalsIgnoreCase(fileInfo.getFile().getName().getExtension());
								}
							});
							if (classFiles != null && classFiles.length > 0) {
								jarFiles.add(fo);
								for (FileObject classFile : classFiles) {
									String name = classFile.getName().getPathDecoded();
									name = name.replaceFirst("^/", "").replaceFirst("\\.class$", "").replace('/', '.');
									jarClasses.add(name);
								}
							}
						}
					}
					if (!jarFiles.isEmpty()) {
						VFSClassLoader componentsClassLoader = new VFSClassLoader(jarFiles.toArray(new FileObject[jarFiles.size()]), vfsManager, AbstractVisualComponent.class.getClassLoader());
						for (String jarClass : jarClasses) {
							Class<?> cl = componentsClassLoader.loadClass(jarClass);
							if (AbstractVisualComponent.class.isAssignableFrom(cl)) {
								array.add((AbstractVisualComponent) cl.newInstance());
							}
						}
					}
				}
				
			} catch (Throwable e) {
				e.printStackTrace();
				logger.warning("general error on create component : "+e.getMessage());
			}

			components = new IContentVisualComponent[array.size()];
			array.toArray(components);
			//globalContext.getServletContext().setAttribute(key, components);
			ctx.getRequest().setAttribute(key,components);
		}
		return components;

	}

	public static IContentVisualComponent[] getDefaultComponents(ServletContext application) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		ArrayList<AbstractVisualComponent> array = new ArrayList<AbstractVisualComponent>();
		String[] classes = ConfigHelper.getDefaultComponentsClasses(application);
		IContentVisualComponent[] components = null;
		for (String classe : classes) {

			logger.fine("load default component : " + classe);

			if (classe.startsWith("--")) {
				array.add(new MetaTitle(classe.substring(2).trim()));
			} else {
				try {
					String className = classe;
					boolean visible = true;
					boolean hidden = false;

					if (className.startsWith(".")) {
						className = className.substring(1);
						visible = false;
						hidden = true;
					}

					Class c = ComponentFactory.class.getClassLoader().loadClass(className);
					AbstractVisualComponent comp = (AbstractVisualComponent) c.newInstance();

					comp.setValid(visible);
					comp.setHidden(hidden);

					array.add(comp);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			components = new IContentVisualComponent[array.size()];
			array.toArray(components);

		}

		if (components == null) {
			components = new IContentVisualComponent[0];
		}

		return components;

	}

	public static IContentVisualComponent getComponentWithType(ContentContext ctx, MenuElement page, String type) {
		IContentVisualComponent outComponent = null;
		try {
			Collection<IContentVisualComponent> components = getComponents(ctx, page);
			for (IContentVisualComponent component : components) {
				if ((component.getType().equals(type))) {
					outComponent = component;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return outComponent;
	}

	public static IContentVisualComponent getComponentWithType(ContentContext ctx, String type) {
		try {
			return getComponentWithType(ctx, ctx.getCurrentPage(), type);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static IContentVisualComponent createComponent(ContentContext ctx, ComponentBean bean, MenuElement inPage, IContentVisualComponent previous, IContentVisualComponent next) throws Exception {		
		AbstractVisualComponent comp = CreateComponent(ctx, bean, inPage, previous, next);
		comp.setStyle(ctx, bean.getStyle());
		return comp;
	}

	private static AbstractVisualComponent CreateComponent(ContentContext ctx, ComponentBean bean, MenuElement inPage, IContentVisualComponent previous, IContentVisualComponent next) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		List<String> selectedComponents = globalContext.getComponents();
		Collection<IContentVisualComponent> components = getComponents(ctx, inPage);
		AbstractVisualComponent component = null;
		for (IContentVisualComponent comp : components) {		
			if (comp != null && bean != null && comp.getType() != null) {
				if (comp.getType().equals(bean.getType())) {
					IContentVisualComponent newComp = comp.newInstance(bean, ctx, inPage);
					component = (AbstractVisualComponent) newComp;
					if (selectedComponents.contains(component.getClass().getName())) {
						break;
					}
				}
			}
		}
		if (component == null) {
			component = new Unknown(ctx, bean);
		}
		if (previous != null) {
			previous.setNextComponent(component);
		}
		component.setNextComponent(next);
		component.setPreviousComponent(previous);
		return component;
	}

	/*
	 * public Map<IContentVisualComponent, MenuElement>
	 * getContentByType(ContentContext ctx, String type) throws Exception {
	 * Content content = Content.createContent(ctx.getRequest()); MenuElement
	 * currentPage = content.getNavigation(ctx).getCurrentPage(ctx);
	 * Map<IContentVisualComponent, MenuElement> outMap = new
	 * HashMap<IContentVisualComponent, MenuElement>(); Collection<MenuElement>
	 * pages = currentPage.getAllChildsWithComponentType(ctx, type); for
	 * (MenuElement page : pages) { Collection<IContentVisualComponent> comps =
	 * currentPage.getContentByType(ctx, type); for (IContentVisualComponent
	 * comp : comps) { outMap.put(comp, page); } } return outMap; }
	 */

	public static List<ComponentBean> getContentByType(ContentContext ctx, String type) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement rootPage = content.getNavigation(ctx);
		List<ComponentBean> outComp = new LinkedList<ComponentBean>();
		for (MenuElement page : rootPage.getAllChildrenList()) {
			ComponentBean[] comps = page.getContent();
			for (ComponentBean comp : comps) {
				if (comp.getType().endsWith(type)) {
					outComp.add(comp);
				}
			}
		}
		return outComp;
	}
	
	/**
	 * create component from map, map is created from IRestComponent
	 * @param data
	 * @return
	 * @throws Exception 
	 */
	public static IContentVisualComponent createUnlinkedComponentFromMap(ContentContext ctx, Map<String,Object> data) throws Exception {
		ComponentBean bean = new ComponentBean();
		BeanHelper.copy(data, bean, false);
		return createComponent(ctx, bean, null, null, null);
	}

	public static List<IContentVisualComponent> getAllComponentsFromContext(ContentContext ctx) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement rootPage = content.getNavigation(ctx);
		List<IContentVisualComponent> outComp = new LinkedList<IContentVisualComponent>();
		for (MenuElement page : rootPage.getAllChildrenList()) {
			ContentElementList comps = page.getAllContent(ctx);
			while (comps.hasNext(ctx)) {
				IContentVisualComponent comp = comps.next(ctx);
				outComp.add(comp);
			}
		}
		return outComp;
	}
	
	public static List<IContentVisualComponent> getGlobalContextComponent(ContentContext ctx, Template template) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		return getGlobalContextComponent(ctx, Integer.MAX_VALUE, template);
	}

	public static List<IContentVisualComponent> getGlobalContextComponent(ContentContext ctx, int complexityLevel) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		return getGlobalContextComponent(ctx, complexityLevel, null);
	}

	public static List<IContentVisualComponent> getGlobalContextComponent(ContentContext ctx, int complexityLevel, Template template) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		List<String> currentComponents = null;
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		currentComponents = globalContext.getComponents();
		IContentVisualComponent[] componentsType = ComponentFactory.getComponents(ctx);
		List<IContentVisualComponent> components = new LinkedList<IContentVisualComponent>();
		for (int i = 0; i < componentsType.length; i++) {
			if (!componentsType[i].isHidden(ctx) && !(componentsType[i] instanceof MetaTitle) && currentComponents.contains(componentsType[i].getClass().getName())) {
				if (componentsType[i].getComplexityLevel(ctx) <= complexityLevel) {
					components.add(componentsType[i]);
				}
			}
		}
		if (template != null) {
			List<Properties> propertiesClasses = template.getDynamicComponentsProperties(globalContext);
			for (Properties properties : propertiesClasses) {
				DynamicComponent comp = new DynamicComponent();
				StructuredProperties newProp = new StructuredProperties();
				newProp.putAll(properties);
				comp.setProperties(newProp);
				comp.setConfigProperties(properties);
				components.add(comp);
				comp.setValid(true);
			}
		}
		return components;
	}

	public static List<ComponentWrapper> getComponentForDisplay(ContentContext ctx, boolean sort) throws Exception {

		List<ComponentWrapper> comps = new LinkedList<ComponentWrapper>();
		EditContext editCtx = EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession());
		ComponentWrapper titleWrapper = null;

		List<IContentVisualComponent> components = getComponents(ctx, ctx.getCurrentPage());
		if (ctx.getCurrentTemplate() != null) {
			Set<String> inludeComponents = null;
			Set<String> excludeComponents = null;
			if (ctx.isAsEditMode()) {
				inludeComponents = ctx.getCurrentTemplate().getComponentsIncludeForArea(editCtx.getCurrentArea());
				excludeComponents = ctx.getCurrentTemplate().getComponentsExcludeForArea(editCtx.getCurrentArea());
				Set<String> excludeTemplateComp = ctx.getCurrentTemplate().getComponentsExclude();
				if (excludeTemplateComp.size() > 0) {
					if (excludeComponents == null) {
						excludeComponents = excludeTemplateComp;
					} else {
						excludeComponents.addAll(excludeTemplateComp);
					}
				}
			}

			for (int i = 0; i < components.size() - 1; i++) {
				if (!components.get(i).isMetaTitle() || !components.get(i + 1).isMetaTitle()) {
					IContentVisualComponent comp = components.get(i);
					if (comp.isMetaTitle() || ctx.getGlobalContext().getComponents().contains(comp.getClass().getName()) || comp.getClass().equals(DynamicComponent.class)) {
						ComponentWrapper compWrapper = new ComponentWrapper(ctx, comp);
						if (components.get(i).isMetaTitle()) {
							titleWrapper = compWrapper;
						}
						if (comp.getType() == null) {
							logger.severe("ComponentFactory.getComponentForDisplay : comp null : " + comp);
						} else {
							if (comp.getType().equals(editCtx.getActiveType())) {
								compWrapper.setSelected(true);
								if (titleWrapper != null) {
									{
										titleWrapper.setSelected(true);
									}
								}
							}
						}
						if (compWrapper.isMetaTitle() || inludeComponents == null || inludeComponents.contains(compWrapper.getType())) {
							if (compWrapper.isMetaTitle() || excludeComponents == null || !excludeComponents.contains(compWrapper.getType())) {
								comps.add(compWrapper);
							}
						}
					}
				}
			}
		}

		if (!components.get(components.size() - 1).isMetaTitle()) {
			IContentVisualComponent comp = components.get(components.size() - 1);
			if (comp.isMetaTitle() || ctx.getGlobalContext().getComponents().contains(comp.getClass().getName()) || comp.getClass().equals(DynamicComponent.class)) {
				ComponentWrapper compWrapper = new ComponentWrapper(ctx, comp);
				comps.add(compWrapper);
				if (comp.getType().equals(editCtx.getActiveType())) {
					compWrapper.setSelected(true);
					if (titleWrapper != null) {
						{
							titleWrapper.setSelected(true);
						}
					}
				}
			}
		}

		List<ComponentWrapper> listWithoutEmptyTitle = new LinkedList<ComponentWrapper>();
		ComponentWrapper title = null;
		UserInterfaceContext uiContext = UserInterfaceContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext());
		for (ComponentWrapper comp : comps) {
			if (comp.isMetaTitle()) {
				title = comp;
			} else {
				if (title != null) {
					if (comp.getComplexityLevel() <= IContentVisualComponent.COMPLEXITY_STANDARD || !uiContext.isLight()) {
						listWithoutEmptyTitle.add(title);
					}
					title = null;
				}
				if (comp.getComplexityLevel() <= IContentVisualComponent.COMPLEXITY_STANDARD || !uiContext.isLight()) {
					listWithoutEmptyTitle.add(comp);
				}
			}
		}

		for (int i = 0; i < listWithoutEmptyTitle.size(); i++) {
			if (i < listWithoutEmptyTitle.size() - 1) {
				listWithoutEmptyTitle.get(i).setHexColor(listWithoutEmptyTitle.get(i + 1).getHexColor());
			}

		}
		
		if (sort) {
			Collections.sort(listWithoutEmptyTitle, new Comparator<ComponentWrapper>() {
				@Override
				public int compare(ComponentWrapper o1, ComponentWrapper o2) {
					return o1.getComplexityLevel() - o2.getComplexityLevel();
				}
			});
		}

		return listWithoutEmptyTitle;
	}
	
	public static void main(String[] args) {
		String html = "<h1>${field.text.title}</h1>";
		String title = "Titre";
		Pattern fieldPattern = Pattern.compile("\\$\\{field.*?\\}");
		Matcher matcher = fieldPattern.matcher(html);
		while (matcher.find()) {
			System.out.println(matcher.group());
		}
		System.out.println("html = "+html.replaceAll("\\$\\{field.*?\\}", title));
	}

}
