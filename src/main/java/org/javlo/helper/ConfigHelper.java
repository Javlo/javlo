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

import org.javlo.exception.ResourceNotFoundException;
import org.javlo.filter.PropertiesFilter;

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

	public static final InputStream getComponentConfigResourceAsStream(ServletContext servletContext, String type, String fileName) throws ResourceNotFoundException {

		String resourceName = CONFIG_COMPONENT_DIR + "/" + type + fileName;
		InputStream in = servletContext.getResourceAsStream(resourceName);
		if (in == null) {
			throw new ResourceNotFoundException("resource not found : " + resourceName);
		} else {
			logger.fine("load resource : " + resourceName);
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
}
