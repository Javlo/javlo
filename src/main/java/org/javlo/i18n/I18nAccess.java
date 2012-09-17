/*
 * Created on 20 ao�t 2003
 */
package org.javlo.i18n;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ConfigHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.service.exception.ServiceException;
import org.javlo.template.Template;
import org.javlo.utils.ReadOnlyMultiMap;
import org.javlo.utils.ReadOnlyPropertiesConfigurationMap;

/**
 * @author pvanderm
 */
public class I18nAccess implements Serializable {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(I18nAccess.class.getName());

	static final String START_BALISE = "\\$\\{";

	static final String END_BALISE = "\\}";

	static final String SESSION_KEY = "i18n";

	static final String I18N_COUNTRIES_FILE_NAME = "/WEB-INF/i18n/countries_";

	public static final Properties FAKE_I18N_FILE = new Properties();

	// static final String I18N_EDIT_FILE_NAME = "/WEB-INF/i18n/edit_";

	// static final String I18N_VIEW_FILE_NAME = "/WEB-INF/i18n/view_";

	// static final String I18N_SPECEFIC_FILE_NAME =
	// "/WEB-INF/i18n/specific_edit_";

	// static final String I18N_SPECEFIC_VIEW_FILE_NAME =
	// "/WEB-INF/i18n/specific_view_";

	/**
	 * @param session
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ConfigurationException
	 * @deprecated use getInstance(HttpSession)
	 */
	@Deprecated
	public static final I18nAccess createI18nAccess(ContentContext ctx) throws FileNotFoundException, IOException, ConfigurationException {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess res = getInstance(globalContext, ctx.getRequest().getSession());
		return res;
	}

	public static I18nAccess getInstance(ContentContext ctx) throws ServiceException, Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18n = new I18nAccess(globalContext);
		if (ctx.getRenderMode() == ContentContext.EDIT_MODE || ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			i18n.initEdit(globalContext);
			ModulesContext moduleContext = ModulesContext.getInstance(ctx.getRequest().getSession(), globalContext);
			i18n.setCurrentModule(globalContext, moduleContext.getCurrentModule());
		}
		i18n.changeViewLanguage(ctx);
		return i18n;
	}

	/**
	 * call this method in view mode (language can change at any click).
	 * 
	 * @param request
	 *            the request
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public static final I18nAccess getInstance(HttpServletRequest request) throws FileNotFoundException, IOException {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		I18nAccess i18nAccess = getInstance(globalContext, request.getSession());
		return i18nAccess;
	}

	/**
	 * @param session
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public static final I18nAccess getInstance(GlobalContext globalContext, HttpSession session) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = (I18nAccess) session.getAttribute(SESSION_KEY);
		if (i18nAccess == null || !i18nAccess.getContextKey().equals(globalContext.getContextKey())) {
			i18nAccess = new I18nAccess(globalContext);
			session.setAttribute(SESSION_KEY, i18nAccess);
		}
		try {
			i18nAccess.initEdit(globalContext);
		} catch (ConfigurationException e) {
			throw new IOException(e.getMessage());
		}
		return i18nAccess;
	}

	private PropertiesConfiguration propEdit = new PropertiesConfiguration();

	private PropertiesConfiguration propView = null;

	private PropertiesConfiguration propContentView = null;

	private Properties templateView = null;

	private Properties moduleEdit = null;

	private Map<String, String> propViewMap = null;

	private final Object lockViewMap = new Object();

	private Map<String, String> propEditMap = null;

	private String editLg = "";

	private String viewLg = "";

	private String contentViewLg = "";

	private ServletContext servletContext = null;

	private Map<String, String> helpMap = null;

	private final Map<String, Properties> componentsPath = new HashMap<String, Properties>();

	private boolean displayKey = false;

	private I18nResource i18nResource = null;

	private Module currentModule;

	private String contextKey;

	public void setCurrentModule(GlobalContext globalContext, Module currentModule) throws IOException {
		if (!currentModule.equals(this.currentModule)) {
			this.currentModule = currentModule;
			moduleEdit = currentModule.loadEditI18n(globalContext);
			propEditMap = null;
		}
	}

	public Module getCurrentModule() {
		return currentModule;
	}

	private I18nAccess(GlobalContext globalContext) {
		servletContext = globalContext.getServletContext();
		PropertiesConfiguration.setDelimiter((char) 0);
		i18nResource = I18nResource.getInstance(globalContext);
		contextKey = globalContext.getContextKey();
	};

	public void changeViewLanguage(ContentContext ctx) throws ServiceException, Exception {
		if (ctx.getLanguage() != null && !ctx.getLanguage().equals(viewLg)) {
			initView(ctx.getLanguage());
		}
		if (ctx.getContentLanguage() != null && !ctx.getContentLanguage().equals(contentViewLg)) {
			initContentView(ctx, ctx.getContentLanguage());
		}
		updateTemplate(ctx);
	}

	public String getComponentText(String componentPath, String key) {
		if (displayKey) {
			return key;
		}
		Properties prop = componentsPath.get(componentPath);
		if (prop == null) {
			InputStream in = null;
			try {
				String fileName = "/i18n/" + AbstractVisualComponent.I18N_FILE.replaceAll("\\[lg\\]", editLg);
				prop = new Properties();
				componentsPath.put(componentPath, prop);
				in = ConfigHelper.getComponentConfigRessourceAsStream(servletContext, componentPath, fileName);
				if (in != null) {
					prop.load(in);
				}
			} catch (Exception e1) {
				// file can not be found.
			} finally {
				ResourceHelper.closeResource(in);
			}
		}
		return prop.getProperty(key);
	}

	public String getContentViewText(String key) {
		if (displayKey) {
			return key;
		}
		String text = "[KEY NULL : " + key + "]";
		if (key != null) {
			synchronized (propContentView) {
				text = propContentView.getString(key);
			}
		}
		if (text == null) {
			text = "[KEY NOT FOUND : " + key + "]";
		}
		return text;
	}

	public String getContentViewText(String key, String defaultValue) {
		if (displayKey) {
			return key;
		}
		String text = null;
		if (key != null && propContentView != null) {
			synchronized (propContentView) {
				text = propContentView.getString(key);
			}
		}
		if (text == null) {
			return defaultValue;
		}
		return text;
	}

	public Map getCountries() throws IOException {
		String fileName = I18N_COUNTRIES_FILE_NAME + viewLg + ".properties";
		InputStream stream = servletContext.getResourceAsStream(fileName);
		try {
			if (stream != null) {
				Properties countries = new Properties();
				countries.load(stream);
				return countries;
			}
		} finally {
			stream.close();
		}
		logger.warning("file : " + fileName + " not found.");
		return Collections.emptyMap();
	}

	public Map<String, String> getEdit() {
		if (propEditMap == null) {
			propEditMap = new Hashtable<String, String>();
			Iterator<?> keys = propEdit.getKeys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				propEditMap.put(key, "" + propEdit.getProperty(key));
			}
			if (moduleEdit != null) {
				Set<?> keysList = moduleEdit.keySet();
				for (Object key : keysList) {
					propEditMap.put((String) key, "" + moduleEdit.getProperty((String) key));
				}
			}
		}
		return propEditMap;
	}

	/* EDIT */

	public String getHelpText(String key) {
		if (!isHelp()) {
			return null;
		} else {
			return getText(key);
		}
	}

	public Map<String, String> getHelpTranslation() {
		PropertiesConfiguration editText = getPropEdit();
		if (helpMap == null) {
			helpMap = new HashMap<String, String>();
			Iterator<?> keys = editText.getKeys("");
			while (keys.hasNext()) {
				String key = (String) keys.next();
				if (key.startsWith("help.")) {
					helpMap.put(key.replaceFirst("help.", ""), editText.getString(key));
				}
			}
		}
		return helpMap;
	}

	private PropertiesConfiguration getPropEdit() {
		return propEdit;
	}

	public String getTest() {
		return "test-test-test";
	}

	public String getText(ContentContext ctx, String key) {
		if (displayKey) {
			return key;
		}
		if ((ctx.getRenderMode() == ContentContext.ADMIN_MODE) || (ctx.getRenderMode() == ContentContext.EDIT_MODE)) {
			return getText(key);
		} else {
			return getContentViewText(key);
		}
	}

	public String getText(String key) {
		return getText(key, "[KEY NOT FOUND (" + editLg + "): " + key + "]");
	}

	/**
	 * replace the balise in text, value of balise is defined in the map. you can defined a balise as : ${balise_name}, this balise is replace with the value of balise name in the map. if the value is not found the balise is not replaced.
	 * 
	 * @param key
	 *            the key of i18n text
	 * @param balises
	 *            in map with [ balise_name, balise_value ]
	 * @return a string with balise replace.
	 */
	public String getText(String key, Map<?, ?> balises) {
		String text = getText(key);
		Collection<?> keys = balises.entrySet();
		for (Object name : keys) {
			String baliseName = (String) name;
			text = text.replaceAll(START_BALISE + baliseName + END_BALISE, (String) balises.get(baliseName));
		}
		return text;
	}

	/* VIEW */

	public String getText(String key, String notFoundValue) {
		if (displayKey) {
			return key;
		}
		String text = propEdit.getString(key);
		if (text == null) {
			if (moduleEdit != null) {
				text = moduleEdit.getProperty(key, notFoundValue);
			} else {
				text = notFoundValue;
			}
		}
		return text;
	}

	/**
	 * replace the balise in text, value of balise is defined in the array. you can defined a balise as : ${balise_name}, this balise is replace with the value of balise name in the array. if the value is not found the balise is not replaced.
	 * 
	 * @param key
	 *            the key of i18n text
	 * @param balises
	 *            in array with [ balise_name, balise_value ]
	 * @return a string with balise replace.
	 */
	public String getText(String key, String[][] balises) {
		String text = getText(key);
		if (text == null) {
			return null;
		}
		for (String[] balise : balises) {
			if (balise[1] != null) {
				text = text.replaceAll(START_BALISE + balise[0] + END_BALISE, balise[1]);
			}
		}
		return text;
	}

	public Map<String, String> getView() {
		synchronized (lockViewMap) {
			if (propViewMap == null) {
				ReadOnlyMultiMap mutliMap = new ReadOnlyMultiMap<String, String>();
				propViewMap = mutliMap;
				if (propView != null) {
					synchronized (propView) {
						mutliMap.addMap(new ReadOnlyPropertiesConfigurationMap(propView));
					}
				}
				if (templateView != null) {
					mutliMap.addMap(templateView);
				}
			}
		}
		return propViewMap;
	}

	public String getViewText(String key) {
		if (displayKey) {
			return key;
		}
		String text = "[KEY NULL : " + key + "]";
		if (key != null) {
			text = null;
			if (templateView != null) {
				text = templateView.getProperty(key);
			}
			if (text == null) {
				if (propView != null) {
					synchronized (propView) {
						text = propView.getString(key);
					}
				}
			}
		}
		if (text == null) {
			text = "[KEY NOT FOUND : " + key + "]";
		}
		return text;

	}

	/**
	 * replace the balise in text, value of balise is defined in the map. you can defined a balise as : ${balise_name}, this balise is replace with the value of balise name in the map. if the value is not found the balise is not replaced.
	 * 
	 * @param key
	 *            the key of i18n text
	 * @param balises
	 *            in map with [ balise_name, balise_value ]
	 * @return a string with balise replace.
	 */
	public String getViewText(String key, Map<?, ?> balises) {
		String text = getContentViewText(key);
		Collection<?> keys = balises.entrySet();
		for (Object name : keys) {
			String baliseName = (String) name;
			text = text.replaceAll(START_BALISE + baliseName + END_BALISE, (String) balises.get(baliseName));
		}
		return text;
	}

	public String getViewText(String key, String defaultValue) {
		if (displayKey) {
			return key;
		}
		String text = "[KEY NULL : " + key + "]";
		if (key != null) {
			text = null;
			if (templateView != null) {
				text = templateView.getProperty(key);
			}
			if (text == null) {
				synchronized (propView) {
					text = propView.getString(key);
				}
			}
		}
		if (text == null) {
			text = defaultValue;
		}
		return text;
	}

	/**
	 * replace the balise in text, value of balise is defined in the array. you can defined a balise as : ${balise_name}, this balise is replace with the value of balise name in the array. if the value is not found the balise is not replaced.
	 * 
	 * @param key
	 *            the key of i18n text
	 * @param balises
	 *            in array with [ balise_name, balise_value ]
	 * @return a string with balise replace.
	 */
	public String getViewText(String key, String[][] balises) {
		String text = getContentViewText(key);
		for (String[] balise : balises) {
			text = text.replaceAll(START_BALISE + balise[0] + END_BALISE, balise[1]);
		}
		return text;
	}

	private void initContentView(ContentContext ctx, String newViewLg) throws ServiceException, Exception {
		logger.finest("init content view language : " + newViewLg);
		contentViewLg = newViewLg;
		propContentView = i18nResource.getViewFile(newViewLg, true);
		propViewMap = null;
		propEditMap = null;
	}

	private void initEdit(GlobalContext globalContext) throws IOException, ConfigurationException {
		String newEditLg = globalContext.getEditLanguage();
		if (!newEditLg.equals(editLg)) {
			editLg = newEditLg;
			componentsPath.clear();
			propEdit = i18nResource.getEditFile(newEditLg, true);
			if (currentModule != null) {
				moduleEdit = currentModule.loadEditI18n(globalContext);
			}
		}
	}

	private void initView(String newViewLg) throws IOException, ConfigurationException {

		logger.finest("init view language : " + newViewLg);

		propView = i18nResource.getViewFile(newViewLg, true);

		viewLg = newViewLg;

		propViewMap = null;
		propEditMap = null;
	}

	private boolean isHelp() {
		return true;
	}

	public void requestInit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String displayKeyStr = requestService.getParameter("display-key", null);
		if (displayKeyStr != null) {
			displayKey = StringHelper.isTrue(displayKeyStr);
		}
		updateTemplate(ctx);
		changeViewLanguage(ctx);
	}

	private void updateTemplate(ContentContext ctx) throws ConfigurationException, IOException, ServiceException, Exception {
		if (ctx.getLanguage() != null) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			if (!ctx.isFree()) {
				Template template = ctx.getCurrentTemplate();
				if (template != null) {
					template = template.getFinalTemplate(ctx);
					templateView = template.getI18nProperties(globalContext, new Locale(ctx.getLanguage()));
				}
			}
		}
	}

	public String getContextKey() {
		return contextKey;
	}

	public void setContextKey(String contextKey) {
		this.contextKey = contextKey;
	}

}