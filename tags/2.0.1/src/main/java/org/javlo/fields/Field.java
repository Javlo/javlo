package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;

public class Field implements Cloneable {

	public class FieldBean {

		protected final ContentContext ctx;
		protected final Locale contentLocale;

		public FieldBean(ContentContext ctx) {
			this.ctx = ctx;
			this.contentLocale = new Locale(ctx.getRequestContentLanguage());
		}

		public String getId() {
			return Field.this.getId();
		}

		public String getName() {
			return Field.this.getName();
		}

		public String getLabel() {
			return Field.this.getLabel(contentLocale);
		}

		public String getValue() {
			return Field.this.getValue();
		}

		public String getDisplayValue() throws Exception {
			return getDisplayValue(ctx);
		}

		public String getDisplayValue(ContentContext ctx) throws Exception {
			return Field.this.getDisplayValue(ctx, contentLocale);
		}

		public String getCssClass() {
			return Field.this.getCSSClass();
		}

		public String getPrefix() {
			return Field.this.getFieldPrefix(ctx);
		}

		public String getSuffix() {
			return Field.this.getFieldSuffix(ctx);
		}

		public String getType() {
			return Field.this.getType();
		}

		public String getInputLabelName() {
			return Field.this.getInputLabelName();
		}

		public String getInputName() {
			return Field.this.getInputName();
		}

		public int getOrder() {
			return Field.this.getOrder();
		}

		public String getTag() {
			return Field.this.getTag();
		}

		public String getViewXHTMLCode() throws Exception {
			return Field.this.getViewXHTMLCode(ctx);
		}

		public boolean isLabelDisplayed() {
			return Field.this.isLabelDisplayed();
		}

		public boolean isLabelEditable() {
			return Field.this.isLabelEditable();
		}

		public boolean isNeeded() {
			return Field.this.isNeeded();
		}

		public boolean isReadOnly() {
			return Field.this.isReadOnly();
		}

		public boolean isViewDisplayed() {
			return Field.this.isViewDisplayed();
		}

		public boolean isWrapped() {
			return Field.this.isWrapped();
		}

		public String getURL() {
			if (Field.this instanceof FieldFile) {
				return ((FieldFile) Field.this).getURL(ctx);
			} else {
				return null;
			}
		}

	}

	protected static final int MESSAGE_ERROR = 1;
	protected static final int MESSAGE_INFO = 2;

	protected Properties properties;
	private String name;
	private String id;
	private String message;
	private String label = null;
	private boolean readOnly = false;
	private int messageType = MESSAGE_INFO;
	protected transient StaticConfig staticConfig;
	protected transient GlobalContext globalContext;
	protected transient I18nAccess i18nAccess;
	private boolean needRefresh = false;
	private Map<String, String> keyValue = null;
	private Locale currentLocale = null;

	/**
	 * Filed can only be create with FieldFactory
	 */
	Field() {
	};

	public Field newInstance() {
		Field newInstance;
		try {
			newInstance = (Field) this.clone();
			newInstance.staticConfig = staticConfig;
			newInstance.globalContext = globalContext;
			newInstance.i18nAccess = i18nAccess;
			return newInstance;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, String> getList(ContentContext ctx, String listName, Locale locale) throws Exception {

		if (keyValue != null) {
			return keyValue;
		}
		keyValue = new LinkedHashMap<String, String>();

		String path = properties.getProperty("list." + listName + ".path");
		if (path != null) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			MenuElement page = globalContext.getPage(ctx, path);
			if (page != null) {
				Collection<MenuElement> children = page.getChildMenuElements();
				for (MenuElement child : children) {
					keyValue.put(child.getName(), child.getTitle(ctx));
				}
			}
		} else {
			for (int i = 0; i < 9999; i++) {
				String value = properties.getProperty("list." + listName + "." + i);
				if (value != null) {
					String[] splitedValue = value.split(",");
					if (splitedValue.length > 1) {
						if (properties.getProperty("list." + listName + "." + i + "." + locale.getLanguage()) != null) {
							value = properties.getProperty("list." + listName + "." + i + "." + locale.getLanguage());
						} else {
							value = splitedValue[1].trim();
						}
						keyValue.put(splitedValue[0].trim(), value);
					} else {
						if (properties.getProperty("list." + listName + "." + i + "." + locale.getLanguage()) != null) {
							value = properties.getProperty("list." + listName + "." + i + "." + locale.getLanguage());
						}
						keyValue.put(value, value);
					}
				}
			}
		}
		return keyValue;
	}

	public String getInputName() {
		if (getCurrentLocale() != null) {
			return getName() + '-' + getId() + '-' + getCurrentLocale().getLanguage();
		} else {
			return getName() + '-' + getId();
		}
	}

	public String getInputLabelName() {
		return getName() + "-label-" + getId();
	}

	public boolean validate() {
		return true;
	}

	protected String getEditLabelCode() {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		if (isLabelEditable()) {
			out.println("	<div class=\"edit-label\">");
			out.println("		<label for=\"" + getInputLabelName() + "\">label : </label>");
			out.println("		<input id=\"" + getInputLabelName() + "\" name=\"" + getInputLabelName() + "\" value=\"" + StringHelper.neverNull(getUserLabel()) + "\" />");
			out.println("   </div>");
		}
		out.close();
		return writer.toString();
	}

	public String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		out.println("<div class=\"line\">");
		out.println(getEditLabelCode());
		out.println("	<label for=\"" + getInputName() + "\">" + getLabel(new Locale(globalContext.getEditLanguage(ctx.getRequest().getSession()))) + " : </label>");
		String readOnlyHTML = "";
		if (isReadOnly()) {
			readOnlyHTML = " readonly=\"readonly\"";
		}
		out.println("	<input" + readOnlyHTML + " id=\"" + getInputName() + "\" name=\"" + getInputName() + "\" value=\"" + StringHelper.neverNull(getValue()) + "\"/>");
		if (getMessage() != null && getMessage().trim().length() > 0) {
			out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
		}
		out.println("</div>");

		out.close();
		return writer.toString();
	}

	public String getFieldPrefix(ContentContext ctx) {
		Locale locale = new Locale(ctx.getRequestContentLanguage());
		String prefix = properties.getProperty("field." + getUnicName() + ".prefix", "");
		if (isLabelDisplayed()) {
			return prefix + "<div class=\"label\">" + StringHelper.neverNull(getUserLabel(locale)) + "</div>";
		} else {
			return prefix;
		}
	}

	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String displayStr = StringHelper.neverNull(getDisplayValue(ctx, new Locale(ctx.getContextLanguage())));
		if (!isPertinent() || displayStr.trim().length() == 0 || !isViewDisplayed()) {
			return "";
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
		displayStr = XHTMLHelper.autoLink(displayStr);
		displayStr = reverserLinkService.replaceLink(ctx, displayStr);

		if (isWrapped()) {
			out.println("<" + getTag() + " class=\"field-value\">" + displayStr + "</" + getTag() + ">");
		} else {
			out.println(displayStr);
		}

		out.close();
		return writer.toString();
	}

	public String getViewListXHTMLCode(ContentContext ctx) throws Exception {
		return getViewXHTMLCode(ctx);
	}

	public boolean isDiplayedInList(ContentContext ctx) {
		return StringHelper.isTrue(properties.getProperty("field." + getUnicName() + ".list", "true"));
	}

	public List<String> getDefaultLanguages() {
		return Arrays.asList(properties.getProperty("component.default-languages", "").split(","));
	}

	/**
	 * return the value "displayable"
	 * 
	 * @param locale
	 * @return
	 * @throws Exception
	 */
	public String getDisplayValue(ContentContext ctx, Locale locale) throws Exception {
		if (getTranslation() != null) {
			String key = createKey("value");
			if (getCurrentLocale() != null) {
				Locale currentLocale = getCurrentLocale();
				Iterator<String> langs = getDefaultLanguages().iterator();
				while ((getValue() == null || getValue().trim().length() == 0) && langs.hasNext()) {
					String lg = langs.next();
					setCurrentLocale(new Locale(lg));
				}
				String value = getValue();
				setCurrentLocale(currentLocale);
				return value;
			} else {
				return properties.getProperty(key);
			}
		}
		return getValue();
	}

	public String getFieldSuffix(ContentContext ctx) {
		return properties.getProperty("field." + getUnicName() + ".suffix", "");
	}

	/**
	 * try to return value with locale and return default value if not.
	 * 
	 * @param locale
	 * @return
	 */
	public String getValue(Locale locale) {
		setCurrentLocale(locale);
		if (getValue() != null) {
			return getValue();
		} else {
			setCurrentLocale(null);
			return getValue();
		}
	}

	public String getValue() {
		String key = createKey("value");
		if (getCurrentLocale() != null) {
			key = createKey("value-" + getCurrentLocale());
		}
		return properties.getProperty(key);
	}

	public boolean isNeeded() {
		return StringHelper.isTrue(properties.getProperty("field." + getUnicName() + ".needed", "false"));
	}

	public int getOrder() {
		String orderStr = properties.getProperty("field." + getUnicName() + ".order", "0");
		int order = 0;
		try {
			order = Integer.parseInt(orderStr);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return order;
	}

	public void setValue(String value) {
		String key = createKey("value");
		if (getCurrentLocale() != null) {
			key = createKey("value-" + getCurrentLocale());
		}
		properties.setProperty(key, StringHelper.neverNull(value));

	}

	public void setLabelValue(String value) {
		properties.setProperty("field." + getUnicName() + ".user-label", value);
	}

	public boolean isLabelDisplayed() {
		return StringHelper.isTrue(properties.getProperty("field." + getUnicName() + ".label-displayed", "false"));
	}

	public String getCSSClass() {
		return properties.getProperty("field." + getUnicName() + ".css-class", "");
	}

	public String getTag() {
		return properties.getProperty("field." + getUnicName() + ".tag", "div");
	}

	public boolean isLabelEditable() {
		return StringHelper.isTrue(properties.getProperty("field." + getUnicName() + ".label-editable", "false"));
	}

	public boolean isViewDisplayed() {
		return StringHelper.isTrue(properties.getProperty("field." + getUnicName() + ".displayed", "true"));
	}

	public boolean isPertinent() {
		String value = getValue();
		if (getTranslation() != null) {
			if (getCurrentLocale() != null) {
				Locale currentLocale = getCurrentLocale();
				Iterator<String> langs = getDefaultLanguages().iterator();
				while ((getValue() == null || getValue().trim().length() == 0) && langs.hasNext()) {
					setCurrentLocale(new Locale(langs.next()));
				}
				value = getValue();
				setCurrentLocale(currentLocale);
			}
		}

		return value != null && value.length() > 0;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return "text";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected String getLabel(Locale locale) {

		if (getLabel().trim().length() != 0) {
			return getLabel();
		}

		String key = properties.getProperty(createKey("label.key"));
		String label;
		if (key != null) {
			label = i18nAccess.getText(key);
		} else {
			label = properties.getProperty(createKey("label." + locale.getLanguage()));
			if (label == null) {
				label = properties.getProperty(createKey("label"));
				if (label == null) {
					label = getUnicName();
				}
			}
		}

		return label;
	}

	private String getUserLabel() {
		return properties.getProperty(createKey("user-label"), null);
	}

	public String getUserLabel(Locale locale) {
		String label = getUserLabel();
		if (label == null) {
			label = getLabel(locale);
		}
		return label;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	protected String getUnicName() {
		return getName();
	}

	/**
	 * process the field
	 * 
	 * @param request
	 * @return true if the field is modified.
	 */
	public boolean process(HttpServletRequest request) {
		RequestService requestService = RequestService.getInstance(request);
		String value = requestService.getParameter(getInputName(), null);
		String label = requestService.getParameter(getInputLabelName(), null);

		boolean modify = false;
		if (label != null) {
			if (!label.equals(getUserLabel())) {
				modify = true;
				setLabelValue(label);
			}
		}
		if (value != null) {
			if (!value.equals(getValue())) {
				setValue(value);
				if (!validate()) {
					setNeedRefresh(true);
				}
				modify = true;
			}
		}
		return modify;
	}

	public StaticConfig getStaticConfig() {
		return staticConfig;
	}

	public void setStaticConfig(StaticConfig staticConfig) {
		this.staticConfig = staticConfig;
	}

	public I18nAccess getI18nAccess() {
		return i18nAccess;
	}

	public void setI18nAccess(I18nAccess access) {
		i18nAccess = access;
	}

	public GlobalContext getGlobalContext() {
		return globalContext;
	}

	public void setGlobalContext(GlobalContext globalContext) {
		this.globalContext = globalContext;
	}

	public boolean isNeedRefresh() {
		return needRefresh;
	}

	public void setNeedRefresh(boolean needRefresh) {
		this.needRefresh = needRefresh;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getMessageType() {
		return messageType;
	}

	public String getMessageTypeCSSClass() {
		String messageType = "info";
		if (getMessageType() == MESSAGE_ERROR) {
			messageType = "error";
		}
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public String getLabel() {
		return StringHelper.neverNull(label);
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public boolean isWrapped() {
		return StringHelper.isTrue(properties.getProperty(createKey("wrapped"), "true"));
	}

	public void fillRequest(ContentContext ctx) {
		ctx.getRequest().setAttribute(getName(), newFieldBean(ctx));
	}

	protected FieldBean newFieldBean(ContentContext ctx) {
		return new FieldBean(ctx);
	}

	protected String createKey(String suffix) {
		return "field." + getUnicName() + '.' + suffix;
	}

	public String getMetaData(String inKey) {
		String key = createKey(inKey);
		String localKey = null;
		if (getCurrentLocale() != null) {
			localKey = createKey(inKey + '-' + getCurrentLocale());
		}
		if (localKey != null && properties.get(localKey) != null) {
			return properties.getProperty(localKey);
		}
		return properties.getProperty(key);
	}

	public final List<Locale> getTranslation() {
		String key = createKey("translation");
		String rawTranslation = properties.getProperty(key);
		if (rawTranslation == null) {
			return null;
		}
		List<Locale> outLocale = new LinkedList<Locale>();
		String[] translations = rawTranslation.split(",");
		for (String lg : translations) {
			outLocale.add(new Locale(lg));
		}
		return outLocale;
	}

	public Locale getCurrentLocale() {
		return currentLocale;
	}

	public void setCurrentLocale(Locale currentLocale) {
		this.currentLocale = currentLocale;
	}

	public boolean isContentCachable() {
		return true;
	}

}
