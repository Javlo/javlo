package org.javlo.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;

public class I18nResource {

	private static Logger logger = Logger.getLogger(I18nResource.class.getName());

	private Map<String, PropertiesConfiguration> viewFiles = new HashMap<String, PropertiesConfiguration>();

	private Map<String, PropertiesConfiguration> editFiles = new HashMap<String, PropertiesConfiguration>();

	private Map<String, Properties> contextFiles = new HashMap<String, Properties>();

	private static final String KEY = I18nResource.class.getName();

	private static File contextI18nFolder = null;

	public static I18nResource getInstance(GlobalContext globalContext) {
		I18nResource out = (I18nResource) globalContext.getAttribute(KEY);
		if (out == null) {
			out = new I18nResource();
			out.staticConfig = globalContext.getStaticConfig();
			globalContext.setAttribute(KEY, out);
		}
		contextI18nFolder = new File(URLHelper.mergePath(globalContext.getDataFolder(), "i18n"));
		return out;
	}

	private StaticConfig staticConfig = null;

	public PropertiesConfiguration getEditFile(String lg, boolean reload) throws IOException, ConfigurationException {
		return getI18nFile(ContentContext.EDIT_MODE, lg, reload);
	}

	private PropertiesConfiguration getI18nFile(int mode, String lg, boolean reload) throws IOException, ConfigurationException {

		PropertiesConfiguration i18nProp = viewFiles.get(lg);
		if (mode == ContentContext.EDIT_MODE) {
			i18nProp = editFiles.get(lg);
		} else {
			i18nProp = viewFiles.get(lg);
		}

		if (i18nProp == null || reload) {

			if (i18nProp == null) {
				i18nProp = new PropertiesConfiguration();
			}

			synchronized (i18nProp) {

				logger.fine("init view language : " + lg);

				i18nProp.clear();

				i18nProp.setListDelimiter(Character.MAX_VALUE);

				File viewFile;
				if (mode == ContentContext.EDIT_MODE) {
					viewFile = new File(staticConfig.getI18nEditFile() + lg + ".properties");
				} else {
					viewFile = new File(staticConfig.getI18nViewFile() + lg + ".properties");
				}

				if (viewFile.exists()) {
					InputStream stream = new FileInputStream(viewFile);
					if (stream != null) {
						Reader reader = null;
						try {
							reader = new InputStreamReader(stream, ContentContext.CHARACTER_ENCODING);
							i18nProp.load(reader);
						} finally {
							ResourceHelper.closeResource(reader);
							ResourceHelper.closeResource(stream);
						}
					}
				} else {
					logger.severe("i18n file not found : " + viewFile);
				}

				File specificViewFile;
				if (mode == ContentContext.EDIT_MODE) {
					specificViewFile = new File(staticConfig.getI18nSpecificEditFile() + lg + ".properties");
				} else {
					specificViewFile = new File(staticConfig.getI18nSpecificViewFile() + lg + ".properties");
				}
				if (specificViewFile.exists()) {
					InputStream stream = new FileInputStream(specificViewFile);
					if (stream != null) {
						Reader reader = null;
						try {
							reader = new InputStreamReader(stream, ContentContext.CHARACTER_ENCODING);
							i18nProp.load(reader);
						} finally {
							ResourceHelper.closeResource(reader);
							ResourceHelper.closeResource(stream);
						}
					}
				} else {
					logger.fine("i18n specific file not found : " + specificViewFile);
				}

				if (mode == ContentContext.EDIT_MODE) {
					editFiles.put(lg, i18nProp);
				} else {
					viewFiles.put(lg, i18nProp);
				}

			} // synchronized

		}
		return i18nProp;
	}

	public Properties getContextI18nFile(int mode, String lg, boolean reload) throws IOException, ConfigurationException {
		System.out.println("***** I18nResource.getContextI18nFile : START"); //TODO: remove debug trace
		System.out.println("***** I18nResource.getContextI18nFile : contextI18nFolder"); //TODO: remove debug trace
		if (!contextI18nFolder.exists()) {
			System.out.println("***** I18nResource.getContextI18nFile : contextI18nFolder NOT FOUND"); //TODO: remove debug trace
			return null;
		} else {
			String key = lg + '-' + mode;
			if (!reload) {
				Properties prop = contextFiles.get(key);
				if (prop != null) {
					return prop;
				}
			}
			File i18nFile;
			if (mode == ContentContext.EDIT_MODE) {
				i18nFile = new File(URLHelper.mergePath(contextI18nFolder.getAbsolutePath(), "edit_" + lg + ".properties"));
			} else {
				i18nFile = new File(URLHelper.mergePath(contextI18nFolder.getAbsolutePath(), "view_" + lg + ".properties"));
			}
			if (i18nFile.exists()) {
				System.out.println("***** I18nResource.getContextI18nFile : load : "+i18nFile); //TODO: remove debug trace
				Properties prop = ResourceHelper.loadProperties(i18nFile);				
				contextFiles.put(key, prop);
				return prop;
			} else {
				System.out.println("***** I18nResource.getContextI18nFile : i18nFile not found : "+i18nFile); //TODO: remove debug trace
				return null;
			}
		}
	}

	public PropertiesConfiguration getViewFile(String lg, boolean reload) throws IOException, ConfigurationException {
		return getI18nFile(ContentContext.VIEW_MODE, lg, reload);
	}

	public void clearAllCache() {
		viewFiles.clear();
		editFiles.clear();
		contextFiles.clear();
	}

}
