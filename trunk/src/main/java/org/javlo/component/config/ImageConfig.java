package org.javlo.component.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.javlo.helper.ResourceHelper;

public class ImageConfig {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ImageConfig.class.getName());

	PropertiesConfiguration properties = new PropertiesConfiguration();

	private static final String FILE = "/WEB-INF/config/image-config.properties";

	private static final String FILE_BASE = "/WEB-INF/config/image-config-base.properties";

	private static final String KEY = ImageConfig.class.getName();

	private static String[] filters = new String[0];

	private ImageConfig(ServletContext servletContext) {
		
		Collection<String> filtersCol = new LinkedList<String>();

		InputStream in = servletContext.getResourceAsStream(FILE_BASE);
		if (in == null) {
			logger.warning("config file for thunbnails not found : " + FILE_BASE);
		} else {
			try {
				properties.load(new InputStreamReader(in));
				servletContext.setAttribute(KEY, this);
			} catch (Exception e) {
				logger.warning("config file for thumbnails can not be loaded (msg: " + e.getMessage() + ")");
			} finally {
				ResourceHelper.closeResource(in);
			}
		}

		in = servletContext.getResourceAsStream(FILE);
		if (in == null) {
			logger.warning("config file for thunbnails not found : " + FILE);
		} else {
			try {
				properties.load(new InputStreamReader(in));				
				servletContext.setAttribute(KEY, this);
			} catch (Exception e) {
				logger.warning("config file for thumbnails can not be loaded (msg: " + e.getMessage() + ")");
			} finally {
				ResourceHelper.closeResource(in);
			}
		}
		
		Iterator<String> propList = properties.getKeys();
		while (propList.hasNext()) {
			String key = propList.next();
			if (key.indexOf(".") > 0) {
				String filter = key.substring(0, key.indexOf("."));
				if (!filtersCol.contains(filter)) {
					filtersCol.add(filter);
				}
			}
		}
		
		filters = new String[filtersCol.size()];
		filtersCol.toArray(filters);
		

	}

	public static ImageConfig getInstance(ServletContext servletContext) {
		ImageConfig outCfg;
		synchronized (FILE) {
			outCfg = (ImageConfig) servletContext.getAttribute(KEY);
			if (outCfg == null) {
				outCfg = new ImageConfig(servletContext);
			}
		}
		return outCfg;
	}

	/**
	 * return the number of little image
	 * 
	 * @return
	 */
	public String getLittleFilter() {
		return properties.getString("little.filter", "little");
	}

	/**
	 * return the number of little image
	 * 
	 * @return
	 */
	public String getBigFilter() {
		return properties.getString("big.filter", "big");
	}
}
