/**
 * Created on 9 oct. 2003
 */
package org.javlo.component.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.VFSClassLoader;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.UserInterfaceContext;
import org.javlo.helper.ConfigHelper;
import org.javlo.module.content.Edit;
import org.javlo.module.content.Edit.ComponentWrapper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

/**
 * @author pvanderm
 */
public class ComponentFactory {

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
	public static IContentVisualComponent[] getComponents(ContentContext ctx, MenuElement page) throws Exception {
		String key = "__components_request_key_" + page.getId() + '_' + ctx.getRenderMode();
		IContentVisualComponent[] outComp = (IContentVisualComponent[]) ctx.getRequest().getAttribute(key);
		if (outComp == null) {
			Template template = null;
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			IContentVisualComponent[] components = getComponents(globalContext);
			ArrayList<IContentVisualComponent> array = new ArrayList<IContentVisualComponent>();
			array.addAll(Arrays.asList(components));
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
						Properties newProp = new Properties();
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
			components = new IContentVisualComponent[array.size()];
			array.toArray(components);
			outComp = components;
			// if (template != null) { // don't cache if no template
			ctx.getRequest().setAttribute(key, outComp);
			// }
		}

		return outComp;

	}

	public static final void updateComponentsLogLevel(ServletContext application, Level level) throws Exception {

		logger.info("update components logger level : " + level);

		String[] classes = ConfigHelper.getComponentsClasses(application);
		for (String clazz : classes) {
			Class comp = null;
			;
			try {
				try {
					comp = Class.forName(clazz);
				} catch (Throwable e) {
					logger.warning(e.getMessage());
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

	public static IContentVisualComponent[] getComponents(GlobalContext globalContext) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		String key = getKey(globalContext.getContextKey());
		IContentVisualComponent[] components = (IContentVisualComponent[]) globalContext.getServletContext().getAttribute(key);

		if (components == null) {
			ArrayList<AbstractVisualComponent> array = new ArrayList<AbstractVisualComponent>();
			String[] classes = ConfigHelper.getComponentsClasses(globalContext.getServletContext());
			List<String> selectedComponent = globalContext.getComponents();
			for (String classe : classes) {

				logger.fine("load component : " + classe);

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
							logger.warning(t.getMessage());
							ComponentBean bean = new ComponentBean();
							bean.setValue(t.getMessage());
							comp = new Unknown(null, bean);
						}

						comp.setValid(visible);
						comp.setHidden(hidden);

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
				Properties newProp = new Properties();
				newProp.putAll(properties);
				comp.setProperties(newProp);
				comp.setConfigProperties(properties);
				array.add(comp);
				if (globalContext.getComponents().contains(comp.getKey())) {
					comp.setValid(true);
				}
			}

			// Load external components
			FileSystemManager vfsManager = VFS.getManager();
			List<String> jarClasses = new LinkedList<String>();
			List<FileObject> jarFiles = new LinkedList<FileObject>();
			File externalComponentFolder = new File(globalContext.getStaticConfig().getExternalComponentFolder());

			if (externalComponentFolder.exists() && externalComponentFolder.isDirectory()) {
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

			components = new IContentVisualComponent[array.size()];
			array.toArray(components);
			globalContext.getServletContext().setAttribute(key, components);

		}

		if (components == null) {
			components = new IContentVisualComponent[0];
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

	public static IContentVisualComponent getComponentWithType(ContentContext ctx, String type) {
		IContentVisualComponent outComponent = null;
		try {
			IContentVisualComponent[] components = getComponents(ctx, ctx.getCurrentPage());
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

	public static IContentVisualComponent createComponent(ContentContext ctx, ComponentBean bean, MenuElement inPage, IContentVisualComponent previous, IContentVisualComponent next) throws Exception {
		AbstractVisualComponent comp = CreateComponent(ctx, bean, inPage, previous, next);
		comp.setStyle(ctx, bean.getStyle());
		return comp;
	}

	public static AbstractVisualComponent CreateComponent(ContentContext ctx, ComponentBean bean, MenuElement inPage, IContentVisualComponent previous, IContentVisualComponent next) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		List<String> selectedComponents = globalContext.getComponents();

		IContentVisualComponent[] components = getComponents(ctx, inPage);
		AbstractVisualComponent component = null;
		for (IContentVisualComponent component2 : components) {
			if (component2 != null && bean != null && component2.getType() != null) {
				if (component2.getType().equals(bean.getType())) {
					IContentVisualComponent newComp = component2.newInstance(bean, ctx);
					component = (AbstractVisualComponent) newComp;
					component.setPage(inPage);
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
	 * public Map<IContentVisualComponent, MenuElement> getContentByType(ContentContext ctx, String type) throws Exception { Content content = Content.createContent(ctx.getRequest()); MenuElement currentPage = content.getNavigation(ctx).getCurrentPage(ctx); Map<IContentVisualComponent, MenuElement> outMap = new HashMap<IContentVisualComponent, MenuElement>(); Collection<MenuElement> pages = currentPage.getAllChildsWithComponentType(ctx, type); for (MenuElement page : pages) { Collection<IContentVisualComponent> comps = currentPage.getContentByType(ctx, type); for (IContentVisualComponent comp : comps) { outMap.put(comp, page); } } return outMap; }
	 */

	public static List<ComponentBean> getContentByType(ContentContext ctx, String type) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement rootPage = content.getNavigation(ctx);
		List<ComponentBean> outComp = new LinkedList<ComponentBean>();
		MenuElement[] pages = rootPage.getAllChildren();
		for (MenuElement page : pages) {
			ComponentBean[] comps = page.getContent();
			for (ComponentBean comp : comps) {
				if (comp.getType().endsWith(type)) {
					outComp.add(comp);
				}
			}
		}
		return outComp;
	}

	public static List<IContentVisualComponent> getAllComonentFromContext(ContentContext ctx) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement rootPage = content.getNavigation(ctx);
		List<IContentVisualComponent> outComp = new LinkedList<IContentVisualComponent>();
		MenuElement[] pages = rootPage.getAllChildren();
		for (MenuElement page : pages) {
			ContentElementList comps = page.getAllContent(ctx);
			while (comps.hasNext(ctx)) {
				IContentVisualComponent comp = comps.next(ctx);
				outComp.add(comp);
			}
		}
		return outComp;
	}

	public static List<IContentVisualComponent> getGlobalContextComponent(ContentContext ctx, int complexityLevel) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		List<String> currentComponents = null;
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		currentComponents = globalContext.getComponents();
		IContentVisualComponent[] componentsType = ComponentFactory.getComponents(globalContext);
		List<IContentVisualComponent> components = new LinkedList<IContentVisualComponent>();
		for (int i = 0; i < componentsType.length; i++) {
			if (!componentsType[i].isHidden(ctx) && !(componentsType[i] instanceof MetaTitle) && currentComponents.contains(componentsType[i].getClass().getName())) {
				if (componentsType[i].getComplexityLevel(ctx) <= complexityLevel) {
					components.add(componentsType[i]);
				}
			}
		}
		return components;
	}

	public static List<ComponentWrapper> getComponentForDisplay(ContentContext ctx) throws Exception {

		List<ComponentWrapper> comps = new LinkedList<ComponentWrapper>();
		EditContext editCtx = EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession());
		ComponentWrapper titleWrapper = null;

		IContentVisualComponent[] components = getComponents(ctx, ctx.getCurrentPage());

		if (ctx.getCurrentTemplate() != null) {
			Set<String> inludeComponents = null;
			Set<String> excludeComponents = null;
			if (ctx.isAsEditMode()) {
				inludeComponents = ctx.getCurrentTemplate().getComponentsIncludeForArea(editCtx.getCurrentArea());
				excludeComponents = ctx.getCurrentTemplate().getComponentsExcludeForArea(editCtx.getCurrentArea());
			}

			for (int i = 0; i < components.length - 1; i++) { // remove title without component
				if (!components[i].isMetaTitle() || !components[i + 1].isMetaTitle()) { // if next component is title too so the component group is empty
					IContentVisualComponent comp = components[i];
					if (comp.isMetaTitle() || ctx.getGlobalContext().getComponents().contains(comp.getClass().getName()) || comp instanceof DynamicComponent) {
						ComponentWrapper compWrapper = new ComponentWrapper(comp.getType(), comp.getComponentLabel(ctx, ctx.getGlobalContext().getEditLanguage(ctx.getRequest().getSession())), comp.getValue(ctx), comp.getHexColor(), comp.getComplexityLevel(ctx), comp.isMetaTitle());
						if (components[i].isMetaTitle()) {
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

		if (!components[components.length - 1].isMetaTitle()) {
			IContentVisualComponent comp = components[components.length - 1];
			ComponentWrapper compWrapper = new ComponentWrapper(comp.getType(), comp.getComponentLabel(ctx, ctx.getGlobalContext().getEditLanguage(ctx.getRequest().getSession())), comp.getValue(ctx), comp.getHexColor(), comp.getComplexityLevel(null), comp.isMetaTitle());
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

		List<ComponentWrapper> listWithoutEmptyTitle = new LinkedList<Edit.ComponentWrapper>();
		ComponentWrapper title = null;
		UserInterfaceContext uiContext = UserInterfaceContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext());
		for (ComponentWrapper comp : comps) {
			if (comp.isMetaTitle()) {
				title = comp;
			} else {
				if (title != null) {
					if (comp.getComplexityLevel() == 1 || !uiContext.isLight()) {
						listWithoutEmptyTitle.add(title);
					}
					title = null;

				}
				if (comp.getComplexityLevel() == 1 || !uiContext.isLight()) {
					listWithoutEmptyTitle.add(comp);
				}
			}
		}

		for (int i = 0; i < listWithoutEmptyTitle.size(); i++) {
			if (i < listWithoutEmptyTitle.size() - 1) {
				listWithoutEmptyTitle.get(i).setHexColor(listWithoutEmptyTitle.get(i + 1).getHexColor());
			}

		}

		return listWithoutEmptyTitle;
	}

}
