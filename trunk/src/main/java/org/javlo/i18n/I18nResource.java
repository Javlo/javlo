package org.javlo.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;

public class I18nResource {

	private static Logger logger = Logger.getLogger(I18nResource.class.getName());

	private Map<String, PropertiesConfiguration> viewFiles = new HashMap<String, PropertiesConfiguration>();

	private Map<String, PropertiesConfiguration> editFiles = new HashMap<String, PropertiesConfiguration>();

	private static final String KEY = I18nResource.class.getName();

	public static I18nResource getInstance(GlobalContext globalContext) {
		I18nResource out = (I18nResource) globalContext.getAttribute(KEY);
		if (out == null) {
			out = new I18nResource();
			out.staticConfig = globalContext.getStaticConfig();
			globalContext.setAttribute(KEY, out);
		}
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

	public PropertiesConfiguration getViewFile(String lg, boolean reload) throws IOException, ConfigurationException {
		return getI18nFile(ContentContext.VIEW_MODE, lg, reload);
	}

}
