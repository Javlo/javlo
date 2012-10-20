/**
 * Created on 9 oct. 2003
 */
package org.javlo.component.core;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ConfigHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.template.Template;

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
	public static IContentVisualComponent[] getComponents(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IContentVisualComponent[] components = getComponents(globalContext);
		ArrayList<IContentVisualComponent> array = new ArrayList<IContentVisualComponent>();
		array.addAll(Arrays.asList(components));
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement page = content.getNavigation(ctx).getNoErrorFreeCurrentPage(ctx);
		Template template = null;
		if (page != null) {
			template = ctx.getCurrentTemplate();
			if (template != null) {
				/* load dynamic component */
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
				logger.warning("no template found for page : " + page.getName());
			}
		}
		components = new IContentVisualComponent[array.size()];
		array.toArray(components);
		return components;

	}

	public static final void updateComponentsLogLevel(ServletContext application, Level level) throws Exception {

		logger.info("update components logger level : " + level);

		String[] classes = ConfigHelper.getComponentsClasses(application);
		for (String clazz : classes) {
			Class comp = null;
			;
			try {
				comp = Class.forName(clazz);
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

						Class c = ComponentFactory.class.getClassLoader().loadClass(className);
						AbstractVisualComponent comp = (AbstractVisualComponent) c.newInstance();

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
			IContentVisualComponent[] components = getComponents(ctx);
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

		IContentVisualComponent[] components = getComponents(ctx);
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
				if (componentsType[i].getComplexityLevel() <= complexityLevel) {
					components.add(componentsType[i]);
				}
			}
		}
		return components;
	}
}
