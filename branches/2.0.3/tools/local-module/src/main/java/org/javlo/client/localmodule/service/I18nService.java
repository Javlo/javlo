package org.javlo.client.localmodule.service;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * @author bdumont
 */
public class I18nService {

	private static final Logger logger = Logger.getLogger(I18nService.class.getName());

	private static I18nService instance;
	public static I18nService getInstance() {
		synchronized (I18nService.class) {
			if (instance == null) {
				instance = new I18nService();
			}
			return instance;
		}
	}

	private ResourceBundle bundle;

	private Locale currentLocale;
	private I18nService() {
		currentLocale = Locale.getDefault();
		bundle = ResourceBundle.getBundle("local-module", currentLocale);
	}

	public String get(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException ex) {
			logger.warning("I18n key not found '" + key + "' for " + currentLocale);
			return "NOT FOUND: " + key;
		}
	}
}
