/*
 * Created on 27-d�c.-2003
 */
package org.javlo.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.javlo.component.core.ComponentContext;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.exception.RessourceNotFoundException;
import org.javlo.filter.PropertiesFilter;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.Module;
import org.javlo.module.ModuleContext;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageConfiguration;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.UserFactory;
import org.javlo.ztatic.FileCache;

/**
 * @author pvandermaesen
 */
public class ConfigHelper {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ConfigHelper.class.getName());

	static final String CONFIG_DIR = "/WEB-INF/config";

	static final String CONFIG_COMPONENT_DIR = "/WEB-INF/components";

	static final String COMPONENTS_FILE = CONFIG_DIR + "/components.txt";
	static final String DEFAULT_COMPONENTS_FILE = CONFIG_DIR + "/default_components.txt";
	static final String SPECIFIC_COMPONENTS_FILE = CONFIG_DIR + "/specific_components.txt";
	static final String SPECIFIC_DEFAULT_COMPONENTS_FILE = CONFIG_DIR + "/specific_default_components.txt";

	static final String COMPONENTS_PROPERTIES_FOLDER = CONFIG_DIR + "/dynamic_components";

	/**
	 * start of the title of a group of comopents in the components list file.
	 */
	static final String TITLE_START = "--";

	static String[] components = null;

	private static void addComponentClasses(InputStream in, List<String> components) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		int insertIndex = -1;

		String line = reader.readLine();
		while (line != null) {
			line = line.trim();
			if ((line.length() > 0) && (line.charAt(0) != '#')) {
				if (line.startsWith(TITLE_START)) {
					if (components.contains(line)) {
						insertIndex = components.indexOf(line);
						components.remove(insertIndex);
					} else {
						insertIndex = components.size();
					}
				}
				int insertAt = insertIndex;
				if (line.startsWith(".") && components.contains(line.substring(1))) {
					insertAt = components.indexOf(line.substring(1));
					components.remove(insertAt);
				} else if (!line.startsWith(".") && components.contains("." + line)) {
					insertAt = components.indexOf("." + line);
					components.remove(insertAt);
				} else {
					insertIndex++;
				}
				components.add(insertAt, line);
			}
			line = reader.readLine();
		}
		reader.close();
	}

	public static final InputStream getComponentConfigRessourceAsStream(ServletContext servletContext, String type, String fileName) throws RessourceNotFoundException {

		String ressourceName = CONFIG_COMPONENT_DIR + "/" + type + fileName;
		InputStream in = servletContext.getResourceAsStream(ressourceName);
		if (in == null) {
			throw new RessourceNotFoundException("ressource not found : " + ressourceName);
		} else {
			logger.fine("load ressource : " + ressourceName);
		}
		return in;
	}

	public static final String[] getComponentsClasses(ServletContext serveltContext) throws IOException {
		if (components == null) {
			ArrayList<String> array = new ArrayList<String>();
			InputStream in = serveltContext.getResourceAsStream(COMPONENTS_FILE);

			if (in != null) {
				try {
					addComponentClasses(in, array);
				} finally {
					ResourceHelper.closeResource(in);
				}
			} else {
				logger.severe("file: " + COMPONENTS_FILE + " not found.");
			}

			InputStream inSpecific = serveltContext.getResourceAsStream(SPECIFIC_COMPONENTS_FILE);
			if (inSpecific != null) {
				try {
					addComponentClasses(inSpecific, array);
				} finally {
					ResourceHelper.closeResource(inSpecific);
				}
			} else {
				logger.info("no " + SPECIFIC_COMPONENTS_FILE + " found.");
			}

			components = new String[array.size()];
			array.toArray(components);
		}
		return components;
	}

	public static final String[] getDefaultComponentsClasses(ServletContext serveltContext) throws IOException {

		ArrayList<String> array = new ArrayList<String>();
		InputStream in = serveltContext.getResourceAsStream(DEFAULT_COMPONENTS_FILE);

		if (in != null) {
			try {
				addComponentClasses(in, array);
			} finally {
				ResourceHelper.closeResource(in);
			}
		} else {
			logger.severe("file: " + DEFAULT_COMPONENTS_FILE + " not found.");
		}

		InputStream inSpecific = serveltContext.getResourceAsStream(SPECIFIC_DEFAULT_COMPONENTS_FILE);
		if (inSpecific != null) {
			try {
				addComponentClasses(inSpecific, array);
			} finally {
				ResourceHelper.closeResource(inSpecific);
			}
		} else {
			logger.info("no " + SPECIFIC_DEFAULT_COMPONENTS_FILE + " found.");
		}

		String[] components = new String[array.size()];
		array.toArray(components);

		return components;
	}

	public static final List<Properties> getDynamicComponentsProperties(ServletContext serveltContext) throws IOException {
		if (serveltContext.getRealPath(COMPONENTS_PROPERTIES_FOLDER) == null) {
			return Collections.emptyList();
		}
		File dynCompDir = new File(serveltContext.getRealPath(COMPONENTS_PROPERTIES_FOLDER));
		if (!dynCompDir.exists()) {

		}
		List<Properties> outProperties = new LinkedList<Properties>();
		File[] propertiesFile = dynCompDir.listFiles(new PropertiesFilter());
		if (propertiesFile != null) {
			for (File element : propertiesFile) {
				Properties prop = new Properties();
				InputStream in = new FileInputStream(element);
				try {
					prop.load(in);
				} finally {
					ResourceHelper.closeResource(in);
				}
				outProperties.add(prop);
			}
		}
		return outProperties;
	}

	/**
	 * return the instance of a class.
	 * 
	 * @return list of class returned :
	 *         <ul>
	 *         <li>HttpServletRequest</li>
	 *         <li>HttpServletResponse</li>
	 *         <li>HttpSession</li>
	 *         <li>ServletContext</li>
	 *         <li>StaticConfig</li>
	 *         <li>ContentContext</li>
	 *         <li>GlobalContext</li>
	 *         <li>I18nAccess</li>
	 *         <li>RequestService</li>
	 *         <li>EditContext</li>
	 *         <li>ContentService</li>
	 *         <li>ComponentContext</li>
	 *         <li>MenuElement : return the current page.</li>
	 *         <li>UserFactory</li>
	 *         <li>AdminUserFactory</li>
	 *         <li>AdminUserSecurity</li>
	 *         <li>PageConfiguration</li>
	 *         <li>ModuleContext</li>
	 *         <li>Module : current module.</li>
	 *         <li>MessageRepository</li>
	 *         <li>FileCache</li>
	 *         </ul>
	 * @throws Exception
	 * @throws
	 */
	public static Object smartInstance(Class c, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (c.equals(HttpServletRequest.class)) {
			return request;
		} else if (c.equals(HttpServletResponse.class)) {
			return response;
		} else if (c.equals(HttpServletResponse.class)) {
			return response;
		} else if (c.equals(HttpSession.class)) {
			return request.getSession();
		} else if (c.equals(ServletContext.class)) {
			return request.getSession().getServletContext();
		} else if (c.equals(ContentContext.class)) {
			return ContentContext.getContentContext(request, response);
		} else if (c.equals(GlobalContext.class)) {
			return GlobalContext.getInstance(request);
		} else if (c.equals(StaticConfig.class)) {
			return StaticConfig.getInstance(request.getSession());
		} else if (c.equals(I18nAccess.class)) {		
			return I18nAccess.getInstance(ContentContext.getContentContext(request, response));
		} else if (c.equals(RequestService.class)) {
			return RequestService.getInstance(request);
		} else if (c.equals(EditContext.class)) {
			return EditContext.getInstance(GlobalContext.getInstance(request), request.getSession());
		} else if (c.equals(ContentService.class)) {
			return ContentService.getInstance(GlobalContext.getInstance(request));
		} else if (c.equals(ComponentContext.class)) {
			return ComponentContext.getInstance(request);
		} else if (c.equals(MenuElement.class)) {
			return ContentContext.getContentContext(request, response).getCurrentPage();
		} else if (c.equals(UserFactory.class)) {
			return UserFactory.createUserFactory(GlobalContext.getInstance(request), request.getSession());
		} else if (c.equals(AdminUserFactory.class)) {
			return AdminUserFactory.createUserFactory(GlobalContext.getInstance(request), request.getSession());
		} else if (c.equals(AdminUserSecurity.class)) {
			return AdminUserSecurity.getInstance(request.getSession().getServletContext());
		} else if (c.equals(PageConfiguration.class)) {
			return PageConfiguration.getInstance(GlobalContext.getInstance(request));
		} else if (c.equals(ModuleContext.class)) {
			return ModuleContext.getInstance(GlobalContext.getInstance(request), request.getSession());
		} else if (c.equals(Module.class)) {
			return ModuleContext.getInstance(GlobalContext.getInstance(request), request.getSession()).getCurrentModule();
		} else if (c.equals(MessageRepository.class)) {
			return MessageRepository.getInstance(request);
		} else if (c.equals(FileCache.class)) {
			return FileCache.getInstance(request.getSession().getServletContext());
		}
		return null;
	}
}
