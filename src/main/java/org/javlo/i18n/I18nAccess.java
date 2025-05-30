/*
 * Created on 20 aout 2003
 */
package org.javlo.i18n;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.*;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.service.exception.ServiceException;
import org.javlo.template.Template;
import org.javlo.utils.ConfigurationProperties;
import org.javlo.utils.KeyMap;
import org.javlo.utils.ReadOnlyMultiMap;

import javax.naming.ConfigurationException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

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

	public static final String KEY_NOT_FOUND = "[KEY NOT FOUND";

	private static final Map<String, Map<String, String>> countries = Collections.synchronizedMap(new HashMap<String, Map<String, String>>());

	private final Map<String, Map<String, String>> cachedCountries = new TreeMap<>();

	public static I18nAccess getInstance(ContentContext ctx) throws ServiceException, Exception {
		/** test **/
		if (ctx == null) {
			return new I18nAccess(null);
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18n = getInstance(ctx.getRequest());
		i18n.initEdit(globalContext, ctx.getRequest().getSession());
		if (ctx.getRenderMode() == ContentContext.EDIT_MODE || ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			ModulesContext moduleContext = ModulesContext.getInstance(ctx.getRequest().getSession(), globalContext);
			i18n.setCurrentModule(globalContext, ctx.getRequest().getSession(), moduleContext.getCurrentModule());
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
		// for test
		if (request == null) {
			return new I18nAccess(null);
		}
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
			i18nAccess.initEdit(globalContext, session);
			session.setAttribute(SESSION_KEY, i18nAccess);
		}
		return i18nAccess;
	}

	private ConfigurationProperties propEdit = new ConfigurationProperties();

	private ConfigurationProperties propView = null;

	private ConfigurationProperties propContentView = null;

	private String latestViewTemplateId = "";

	private String latestEditTemplateId = "";

	private String latestViewTemplateLang = "";

	private String latestEditTemplateLang = "";

	private final Properties templateView = new Properties();

	private final Properties templateEdit = new Properties();

	private boolean templateViewImported = false;

	private boolean templateEditImported = false;

	private Properties moduleEdit = null;

	private Properties contextEdit = null;

	private ReadOnlyMultiMap propViewMap = null;

	private Map<String, String> calendarViewMap = null;

	private Map<String, String> calendarEditMap = null;

	private final Object lockViewMap = new Object();

	private Map<String, String> propEditMap = null;

	private Map<String, String> propEditWidthEmptyMap = null;

	private Boolean moduleImported = false;

	private String editLg = "";

	private String forceEditLg = null;

	private String viewLg = "";

	private String contentViewLg = "";

	private ServletContext servletContext = null;

	private Map<String, String> helpMap = null;

	private final Map<String, Properties> componentsPath = new HashMap<String, Properties>();

	private boolean displayKey = false;

	private I18nResource i18nResource = null;

	private Module currentModule;

	private String contextKey;

	private Object lock = null;

	private Map<String, String> requestMap = Collections.EMPTY_MAP;

	public synchronized void setCurrentModule(GlobalContext globalContext, HttpSession session, Module currentModule) throws IOException {
		if (this.currentModule == null || !currentModule.getName().equals(this.currentModule.getName())) {
			this.currentModule = currentModule;
			moduleEdit = currentModule.loadEditI18n(globalContext, session);
			propEditMap = null;
			moduleImported = false;
		}
	}

	public Module getCurrentModule() {
		return currentModule;
	}

	private I18nAccess(GlobalContext globalContext) {
		if (globalContext != null) {
			lock = globalContext.getI18nLock();
			servletContext = globalContext.getServletContext();
			i18nResource = I18nResource.getInstance(globalContext);
			contextKey = globalContext.getContextKey();
		}
	};

	public void changeViewLanguage(ContentContext ctx) throws ServiceException, Exception {
		if (ctx.getLanguage() != null && !ctx.getLanguage().equals(viewLg)) {
			latestViewTemplateId = "";
			initView(ctx.getLanguage());
			propViewMap = null;
		}
		if (ctx.getRequestContentLanguage() != null && !ctx.getRequestContentLanguage().equals(contentViewLg)) {
			latestViewTemplateId = "";
			initContentView(ctx, ctx.getRequestContentLanguage());
			propViewMap = null;
		}
		updateTemplate(ctx);
		countries.clear();
		cachedCountries.clear();
	}

	public void resetViewLanguage(ContentContext ctx) throws ServiceException, Exception {
		latestViewTemplateId = "";
		initView(ctx.getLanguage());
		propViewMap = null;
		latestViewTemplateId = "";
		initContentView(ctx, ctx.getRequestContentLanguage());
		propViewMap = null;
		updateTemplate(ctx);
		cachedCountries.clear();
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
				in = ConfigHelper.getComponentConfigResourceAsStream(servletContext, componentPath, fileName);
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
			synchronized (lock) {
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
			synchronized (lock) {
				text = propContentView.getString(key);
			}
		}
		if (text == null) {
			return defaultValue;
		}
		return text;
	}

	public Map<String, String> getCountries() {
		String lg = viewLg; // Supposons que viewLg est défini ailleurs dans votre classe

		// Vérification du cache
		if (cachedCountries.containsKey(lg)) {
			return cachedCountries.get(lg);
		}

		// Création de la carte des pays triés
		Map<String, String> outCountries = new TreeMap<>();

		for (String countryCode : Locale.getISOCountries()) {
			Locale locale = new Locale("", countryCode);
			String countryName = locale.getDisplayCountry(new Locale(lg));

			if (!countryName.isEmpty()) { // On s'assure que le nom du pays est valide
				outCountries.put(countryCode, countryName);
			}
		}

		// Mise en cache des pays triés
		cachedCountries.put(lg, outCountries);

		return outCountries;
	}

	public Map _getCountries() throws Exception {
		String lg = viewLg;
		Map outCountries = countries.get(lg);
		if (outCountries == null) {
			String fileName = I18N_COUNTRIES_FILE_NAME + lg + ".properties";
			InputStream stream = servletContext.getResourceAsStream(fileName);
			try {
				if (stream != null) {
					Properties countries = new Properties();
					countries.load(stream);
					outCountries = countries;
				}
			} finally {
				ResourceHelper.closeResource(stream);
			}
			if (outCountries == null) {
				outCountries = Countries.getCountriesList(viewLg);
			}

			outCountries = MapHelper.sortByValue(outCountries);
			countries.put(lg, outCountries);
		}
		return outCountries;
	}

	public Map getCountries(ContentContext ctx) throws Exception {
		Map outCountries = countries.get(viewLg);
		if (outCountries == null) {
			Template template = ctx.getCurrentTemplate();
			if (template != null) {
				File i18nFile = new File(URLHelper.mergePath(template.getWorkTemplateRealPath(ctx.getGlobalContext()), "/i18n/countries_" + viewLg + ".properties"));
				if (i18nFile.exists()) {
					outCountries = ResourceHelper.loadProperties(i18nFile);
				}
			}
			if (outCountries == null) {
				String fileName = I18N_COUNTRIES_FILE_NAME + viewLg + ".properties";
				InputStream stream = servletContext.getResourceAsStream(fileName);
				try {
					if (stream != null) {
						Properties countries = new Properties();
						countries.load(stream);
						outCountries = countries;
					}
				} finally {
					ResourceHelper.closeResource(stream);
				}
				if (outCountries == null) {
					outCountries = Countries.getCountriesList(viewLg);
				}
			}
			countries.put(viewLg, outCountries);
		}
		return outCountries;
	}

	public Map<String, String> getEdit() {

		if (displayKey) {
			return KeyMap.stringInstance;
		}

		boolean createPropEditMap = false;

		if (propEditMap == null) {
			synchronized (lock) {
				if (propEditMap == null) {
					propEditMap = new Hashtable<String, String>();
					createPropEditMap = true;
					Iterator<?> keys = propEdit.getKeys();
					while (keys.hasNext()) {
						String key = (String) keys.next();
						propEditMap.put(key, "" + propEdit.getProperty(key));
					}
				}
			}
		}

		if (!moduleImported || createPropEditMap) {
			if (moduleEdit != null) {
				moduleImported = true;
				Set<?> keysList = moduleEdit.keySet();
				for (Object key : keysList) {
					propEditMap.put(key.toString(), "" + moduleEdit.getProperty((String) key));
				}
			}
		}

		if (contextEdit != null) {
			Set<?> keysList = contextEdit.keySet();
			for (Object key : keysList) {
				propEditMap.put(key.toString(), "" + contextEdit.getProperty((String) key));
			}
		}

		if (templateEdit != null && !templateEditImported) {
			for (Object key : templateEdit.keySet()) {
				propEditMap.put((String) key, (String) templateEdit.get(key));
			}
			templateEditImported = true;
		}

		return propEditMap;
	}

	public static void main(String[] args) {
		Map<String,String> balises = new HashMap<>();
		balises.put("test", "patrick");
		String text = "ceci est ${test} qui fait un test.";
		Collection<?> keys =  balises.keySet();
		for (Object name : keys) {
			if (name != null) {
				String baliseName = name.toString();
				String value = balises.get(name);
				System.out.println(">>>>>>>>> I18nAccess.main : value = "+value); //TODO: remove debug trace
				if (value != null) {
					text = text.replaceAll(START_BALISE + baliseName + END_BALISE, value.toString());
				}
			}
			
		}
		System.out.println(text);
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
		ConfigurationProperties editText = getPropEdit();
		if (helpMap == null) {
			helpMap = new HashMap<String, String>();
			Iterator<?> keys = editText.getKeys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				if (key.startsWith("help.")) {
					helpMap.put(key.replaceFirst("help.", ""), editText.getString(key));
				}
			}
		}
		return helpMap;
	}

	private ConfigurationProperties getPropEdit() {
		return propEdit;
	}

	public String getTest() {
		return "test-test-test";
	}

	public String getText(ContentContext ctx, String key) {
		if (displayKey) {
			return key;
		}
		if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {
			return getText(key);
		} else {
			return getContentViewText(key);
		}
	}

	public String getText(String key) {
		return getText(key, "[KEY NOT FOUND (" + editLg + "): " + key + "]");
	}

	/**
	 * replace the balise in text, value of balise is defined in the map. you can
	 * defined a balise as : ${balise_name}, this balise is replace with the value
	 * of balise name in the map. if the value is not found the balise is not
	 * replaced.
	 * 
	 * @param key
	 *            the key of i18n text
	 * @param balises
	 *            in map with [ balise_name, balise_value ]
	 * @return a string with balise replace.
	 */
	public String getText(String key, Map<?, ?> balises) {
		String text = getText(key);
		Collection<?> keys = balises.keySet();
		for (Object name : keys) {
			text = text.replaceAll(START_BALISE + name + END_BALISE, (String) balises.get(name));
		}
		return text;
	}

	/* VIEW */

	public String getText(String key, String notFoundValue) {

		if (displayKey) {
			return key;
		}

		String text;
		if (templateEdit != null) {
			text = templateEdit.getProperty(key);
			if (text != null) {
				return text;
			}
		}
		text = propEdit.getString(key);
		if (text == null) {
			if (moduleEdit != null) {
				text = moduleEdit.getProperty(key, notFoundValue);
			} else {
				text = notFoundValue;
			}
		}

		if (contextEdit != null) {
			String contextText = contextEdit.getProperty(key);
			if (contextText != null) {
				text = contextText;
			}
		}

		return text;
	}

	/**
	 * replace the balise in text, value of balise is defined in the array. you can
	 * defined a balise as : ${balise_name}, this balise is replace with the value
	 * of balise name in the array. if the value is not found the balise is not
	 * replaced.
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

	/**
	 * return trad for calendar
	 * 
	 * @return
	 */
	public Map<String, String> getCalendarView() {
		// calendar
		if (calendarViewMap == null) {
			synchronized (this) {
				if (calendarViewMap == null) {
					calendarViewMap = new HashMap();
					Calendar cal = Calendar.getInstance();
					Locale locale = new Locale(viewLg, "BE");
					for (int i = 1; i <= 7; i++) {
						cal.set(Calendar.DAY_OF_WEEK, i);
						calendarViewMap.put("calendar.day.short." + i, cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale));
						calendarViewMap.put("calendar.day.long." + i, cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale));
					}
				}
			}
		}
		return calendarViewMap;
	}

	/**
	 * return trad for calendar
	 * 
	 * @return
	 */
	public Map<String, String> getCalendarEdit() {
		// calendar
		if (calendarEditMap == null) {
			synchronized (this) {
				if (calendarEditMap == null) {
					calendarEditMap = new HashMap();
					Calendar cal = Calendar.getInstance();
					Locale locale = new Locale(viewLg, "BE");
					for (int i = 1; i <= 7; i++) {
						cal.set(Calendar.DAY_OF_WEEK, i);
						calendarEditMap.put("calendar.day.short." + i, cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale));
						calendarEditMap.put("calendar.day.long." + i, cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale));
					}
				}
			}
		}
		return calendarEditMap;
	}

	public Map<String, String> getView() {

		if (displayKey) {
			return KeyMap.stringInstance;
		}

		boolean createPropViewMap = false;
		synchronized (lock) {
			if (propViewMap == null) {
				propViewMap = new ReadOnlyMultiMap<String, String>();
				createPropViewMap = true;
				ConfigurationProperties localPropView = propView;
				if (localPropView != null) {
					// propViewMap.addMap(new ReadOnlyPropertiesConfigurationMap(localPropView,
					// displayKey));
					propViewMap.addMap(localPropView.getProperties());
				}
			}
			if (templateView != null && (!templateViewImported || createPropViewMap)) {
				propViewMap.addMap(templateView);
				templateViewImported = true;
			}
		}

		ReadOnlyMultiMap outMap = propViewMap;
		if (getRequestMap().size() > 0) {
			outMap = new ReadOnlyMultiMap();
			outMap.addMap(propViewMap);
			outMap.addMap(getRequestMap());
		}

		return outMap;
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
					synchronized (lock) {
						text = propView.getString(key);
					}
				}
			}
		}

		if (text == null) {
			text = KEY_NOT_FOUND + " : " + key + "]";
		}
		return text;
	}

	/**
	 * replace the balise in text, value of balise is defined in the map. you can
	 * defined a balise as : ${balise_name}, this balise is replace with the value
	 * of balise name in the map. if the value is not found the balise is not
	 * replaced.
	 * 
	 * @param key
	 *            the key of i18n text
	 * @param balises
	 *            in map with [ balise_name, balise_value ]
	 * @return a string with balise replace.
	 */
	public String getViewText(String key, Map<?, ?> balises) {
		String text = getContentViewText(key);
		Collection<?> keys = balises.keySet();
		for (Object name : keys) {
			if (name != null) {
				Object value = balises.get(name);
				String baliseName = name.toString();
				if (value != null) {
					text = text.replaceAll(START_BALISE + baliseName + END_BALISE, value.toString());
				}
			}
			
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
			if (text == null && propView != null) {
				synchronized (lock) {
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
	 * replace the balise in text, value of balise is defined in the array. you can
	 * defined a balise as : ${balise_name}, this balise is replace with the value
	 * of balise name in the array. if the value is not found the balise is not
	 * replaced.
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
		// propEditMap = null;
	}

	private void initEdit(GlobalContext globalContext, HttpSession session) throws IOException {
		String newEditLg = globalContext.getEditLanguage(session);
		if (!newEditLg.equals(editLg) && forceEditLg == null) {
			propEditMap = null;
			editLg = newEditLg;
			componentsPath.clear();
			propEdit = i18nResource.getEditFile(newEditLg, true);
			if (currentModule != null) {
				moduleEdit = currentModule.loadEditI18n(globalContext, session);
			}
			contextEdit = i18nResource.getContextI18nFile(ContentContext.EDIT_MODE, newEditLg, true);
		}
	}

	public String getEditLg() {
		return editLg;
	}

	public void resetForceEditLg() {
		editLg = null;
		forceEditLg = null;
	}

	public void forceReloadEdit(GlobalContext globalContext, HttpSession session, String lg) throws IOException {
		if (forceEditLg == null || !lg.equals(forceEditLg)) {
			forceEditLg = lg;
			propEditMap = null;
			editLg = lg;
			componentsPath.clear();
			propEdit = i18nResource.getEditFile(lg, true);
			if (currentModule != null) {
				moduleEdit = currentModule.loadEditI18n(globalContext, session);
			}
			contextEdit = i18nResource.getContextI18nFile(ContentContext.EDIT_MODE, lg, true);
		}
	}

	private void initView(String newViewLg) throws IOException {

		logger.finest("init view language : " + newViewLg);

		propView = i18nResource.getViewFile(newViewLg, true);

		viewLg = newViewLg;

		propViewMap = null;

		calendarViewMap = null;
		// propEditMap = null;
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

	public boolean isDisplayKey() {
		return displayKey;
	}

	private void updateTemplate(ContentContext ctx) throws IOException, ServiceException, Exception {
		// updateTemplate(ctx, ContentContext.EDIT_MODE);
		// updateTemplate(ctx, ContentContext.VIEW_MODE);
		updateTemplate(ctx, ctx.getRenderMode());
	}

	private synchronized void updateTemplate(ContentContext ctx, int mode) throws IOException, ServiceException, Exception {

		String latestTemplateId = latestViewTemplateId;
		String latestTemplateLang = latestViewTemplateLang;
		if (mode == ContentContext.EDIT_MODE) {
			latestTemplateId = latestEditTemplateId;
			latestTemplateLang = latestEditTemplateLang;
		}

		if (ctx.getLanguage() != null) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			if (!ctx.isFree()) {
				Template template = ctx.getCurrentTemplate();

				String lg = ctx.getLanguage();
				if (mode == ContentContext.EDIT_MODE) {
					lg = globalContext.getEditLanguage(ctx.getRequest().getSession());
				}

				if (template != null && template.getId() != null && (!latestTemplateId.equals(template.getId()) || !latestTemplateLang.equals(lg))) {
					propViewMap = null;
					latestTemplateId = template.getId();
					latestTemplateLang = lg;
					template = template.getFinalTemplate(ctx);
					if (!template.isTemplateInWebapp(ctx)) {
						template.importTemplateInWebapp(globalContext.getStaticConfig(), ctx);
					}
					Stack<Map> stack = new Stack<Map>();
					stack.push(template.getI18nProperties(globalContext, new Locale(lg), mode));
					Template parent = template.getParent();
					while (parent != null) {
						if (!parent.isTemplateInWebapp(ctx)) {
							parent.importTemplateInWebapp(globalContext.getStaticConfig(), ctx);
						}
						Map i18n = parent.getI18nProperties(globalContext, new Locale(lg), mode);
						if (i18n != null) {
							stack.push(i18n);
						}
						parent = parent.getParent();
					}
					if (mode == ContentContext.EDIT_MODE) {
						templateEdit.clear();
						while (!stack.empty()) {
							templateEdit.putAll(stack.pop());
						}
					} else {
						templateView.clear();
						while (!stack.empty()) {
							templateView.putAll(stack.pop());
						}
					}
					templateViewImported = false;
				}
			}

		}

		if (mode == ContentContext.EDIT_MODE) {
			latestEditTemplateId = latestTemplateId;
			latestEditTemplateLang = latestTemplateLang;
		} else {
			latestViewTemplateId = latestTemplateId;
			latestViewTemplateLang = latestTemplateLang;
		}
	}

	public String getContextKey() {
		return contextKey;
	}

	public void setContextKey(String contextKey) {
		this.contextKey = contextKey;
	}

	public Collection<String> getMonths() {
		List<String> months = new LinkedList<String>();
		Calendar cal = Calendar.getInstance(new Locale(viewLg));
		cal.set(2000, 0, 1);
		SimpleDateFormat format = new SimpleDateFormat("MMM");
		for (int i = 0; i < 12; i++) {
			months.add(format.format(cal.getTime()));
			cal.roll(Calendar.MONTH, true);
		}
		return months;
	}

	public String getAllText(String key, String defautlValue) {
		return getViewText(key, getText(key, defautlValue));
	}

	public Map<String, String> getRequestMap() {
		return requestMap;
	}

	public void setRequestMap(Map<String, String> requestMap) {
		this.requestMap = requestMap;
	}

	public void resetRequestMap() {
		requestMap = Collections.EMPTY_MAP;
	}

}