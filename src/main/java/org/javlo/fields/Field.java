package org.javlo.fields;

import org.apache.commons.lang3.StringUtils;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.rest.IRestItem;
import org.javlo.helper.*;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.*;
import org.javlo.service.google.translation.ITranslator;
import org.owasp.encoder.Encode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Field implements Cloneable, IRestItem, Comparable<Field> {
	
	private static Logger logger = Logger.getLogger(Field.class.getName());
	
	protected static final String DEFAULT_SEARCH_TYPE = "default";
	
	public static String LABEL_CSS = "col-sm-4 col-form-label";
	public static String VALUE_SIZE = "col-sm-8";
	public static String SMALL_VALUE_SIZE = "col-sm-6";
	public static String SMALL_PART_SIZE = "col-sm-2";
	
	private static final String OPEN_ROW_KEY = "_field_open_row";

	public class FieldBean {

		protected final ContentContext ctx;
		protected final Locale contentLocale;

		protected FieldBean refBean = null;

		public FieldBean(ContentContext ctx) {
			this.ctx = ctx;
			this.contentLocale = ctx.getLocale();
		}

		public String getId() {
			return Field.this.getId();
		}

		public String getName() {
			return Field.this.getName();
		}

		public String getLabel() {
			return Field.this.getLabel(ctx, contentLocale);
		}
		
		public String getUnity() {
			return Field.this.getUnity(ctx, contentLocale);
		}		
		
		public String getValue() {
			String value = Field.this.getValue();
			if (StringHelper.isEmpty(value)) {
				try {
					return getReferenceValue(ctx);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return value;
		}
		
		public String getFormatedValue() throws Exception {
			String value = Field.this.getReferenceFormatedValue(ctx);
			if (StringHelper.isEmpty(value)) {
				try {
					return getReferenceValue(ctx);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return value;
		}
		
		public String getText() {
			return StringHelper.removeTag(StringHelper.removeCR(getValue()));
		}
		
		public String getTextForAttribute() {
			return Encode.forHtmlAttribute(StringHelper.removeTag(StringHelper.removeCR(getValue())));
		}
		
		public String getHtml() {
			return Field.this.getHtml(ctx);
		}
		
		public String getXHTMLValue() {
			return Field.this.getXHTMLValue();
		}
		
		public List<String> getValues() {
			try {
				return Field.this.getValues(ctx, contentLocale);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		/**
		 * i18n value of the field.
		 * 
		 * @return
		 * @throws Exception
		 */
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
		
		public String getAllXHTML() throws Exception {
			String cssClass = "";
			if (getCSSClass() != null && getCSSClass().trim().length() > 0) {
				cssClass = ' ' + getCSSClass();
			}
			String prefix = "";
			String suffix = "";
			if (isWrapped()) {
				prefix = "<div class=\"field " + getName() + cssClass + "\">";
				suffix = "</div>";
			}				
			String unity = getUnity();
			if (!StringHelper.isEmpty(unity)) {
				unity= "<span class=\"unity\">"+unity+"</span>";
			}
			return prefix+Field.this.getFieldPrefix(ctx)+Field.this.getViewXHTMLCode(ctx)+Field.this.getFieldSuffix(ctx)+suffix;
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
		
		public String getUrl() {
			if (Field.this instanceof FieldFile) {
				return ((FieldFile) Field.this).getURL(ctx);
			} else {
				return null;
			}
		}

		@Deprecated
		public String getURL() {
			return getUrl();
		}

		public boolean isPertinent() {
			return Field.this.isPertinent(ctx);
		}

		public FieldBean getReference() throws Exception {
			if (Field.this.isI18n()) {
				return this;
			} else if (refBean == null) {
				DynamicComponent ref = Field.this.getReferenceComponent(ctx);
				if (ref == null) {
					return this;
				} else {
					refBean = ref.getField(ctx, Field.this.getName()).newFieldBean(ctx);
				}
			}
			return refBean;
		}
		
		public String getSize() {
			return Field.this.getSize(ctx);
		}

	}

	protected static final int MESSAGE_ERROR = 1;
	protected static final int MESSAGE_INFO = 2;

	public Properties properties;
	private String name;

	private String group;
	private String id;
	private String message;
	private String label = null;
	private String placeholder = null;
	private boolean readOnly = false;
	private int messageType = MESSAGE_INFO;
	protected transient StaticConfig staticConfig;
	protected transient GlobalContext globalContext;
	protected transient I18nAccess i18nAccess;
	private boolean needRefresh = false;
	private Map<String, String> keyValue = null;
	private Map<String, String> replacementCode = null;
	private Locale currentLocale = null;
	protected IContentVisualComponent comp = null;
	private boolean last = false;
	private boolean first = false;

	/**
	 * Filed can only be create with FieldFactory
	 */
	Field() {
	};

	public String getFormatedValue(ContentContext ctx) {
		return getValue();
	}

	public Field newInstance(IContentVisualComponent inComp) {
		Field newInstance;
		try {
			newInstance = (Field) this.clone();
			newInstance.staticConfig = staticConfig;
			newInstance.globalContext = globalContext;
			newInstance.i18nAccess = i18nAccess;
			newInstance.comp = inComp;
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
		ListService listService = ListService.getInstance(ctx);
		List<IListItem> items  = listService.getList(ctx, listName);
		if (items != null && items.size() > 0) {
			Map<String,String> outMap = new HashMap<String,String>();
			for (IListItem item : items) {
				outMap.put(item.getKey(), item.getValue());
			}
			return outMap;
		}
		String path = properties.getProperty("list." + listName + ".path");
		boolean addEmpty = StringHelper.isTrue(properties.getProperty("list." + listName + ".empty", null));
		if (path != null) {
			ContentService content = ContentService.getInstance(ctx.getRequest());
			MenuElement page = content.getNavigation(ctx).searchChild(ctx, path);
			if (page != null) {
				Collection<MenuElement> children = page.getChildMenuElements();
				if (addEmpty) {
					keyValue.put("", "");
				}
				for (MenuElement child : children) {
					keyValue.put(child.getName(), child.getTitle(ctx));
				}
			}
		} else {
			for (int i = 0; i < 9999; i++) {
				String value = properties.getProperty("list." + listName + '.' + i);
				if (value != null) {
					String key = value;
					String[] splitedValue = value.split(",");
					if (splitedValue.length > 1) {
						if (properties.getProperty("list." + listName + "." + i + '.' + locale.getLanguage()) != null) {
							value = properties.getProperty("list." + listName + '.' + i + '.' + locale.getLanguage());
						} else {
							value = splitedValue[1].trim();
						}
						keyValue.put(splitedValue[0].trim(), value);
					} else {
						if (properties.getProperty("list." + listName + '.' + i + ".key") != null) {
							I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
							String newValue = i18nAccess.getViewText(properties.getProperty("list." + listName + '.' + i + ".key"), (String) null);
							if (newValue != null && newValue.trim().length() > 0) {
								value = newValue;
							}
						}
						if (properties.getProperty("list." + listName + '.' + i + '.' + locale.getLanguage()) != null) {
							value = properties.getProperty("list." + listName + '.' + i + "." + locale.getLanguage());
						}
						keyValue.put(key, value);
					}
				}
			}
			if (keyValue.size() == 0) {				
				List<IListItem> list = ListService.getInstance(ctx).getList(ctx,listName);		
				if (list != null) {
					keyValue = ListService.listToStringMap(list);
				} else {
					logger.warning("list not found : "+listName);
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
		Integer maxSize = getMaxSize();
		if (maxSize != null) {
			if (StringHelper.removeTag(getValue()).length() > maxSize) {
				setMessage(i18nAccess.getText("content.dynamic-component.error.max-size")+maxSize);
				setMessageType(Field.MESSAGE_ERROR);
				return false;	
			}			
		}
		Integer minSize = getMinSize();
		if (minSize != null) {
			if (StringHelper.removeTag(getValue()).length() < minSize) {
				setMessage(i18nAccess.getText("content.dynamic-component.error.min-size")+minSize);
				setMessageType(Field.MESSAGE_ERROR);
				return false;	
			}			
		}
		if (isNeeded() && StringHelper.isEmpty(getValue())) {
			setMessage(i18nAccess.getText("content.dynamic-component.error.needed"));
			setMessageType(Field.MESSAGE_ERROR);
			return false;	
		}		
		return true;
	}
	
	public boolean isValid() {
		return getMessageType() != Field.MESSAGE_ERROR;
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

	/**
	 * for field with i18n false, the value come from default language version
	 * of the page.
	 * 
	 * @throws Exception
	 */
	public DynamicComponent getReferenceComponent(ContentContext ctx) throws Exception {
		if (ctx.getRequestContentLanguage().equals(ctx.getGlobalContext().getDefaultLanguage())) {
			return null;
		}
		int componentPosition = ComponentHelper.getComponentPosition(ctx, comp);
		ContentContext lgCtx = ctx.getContextForDefaultLanguage();
		IContentVisualComponent refComp = ComponentHelper.getComponentWidthPosition(lgCtx, comp.getPage(), comp.getArea(), comp.getType(), componentPosition);
		if (refComp == null) {
			return null;
		} else {
			return (DynamicComponent) refComp;
		}
	}

	/**
	 * return true if this field is translated.
	 * 
	 * @return
	 */
	public boolean isI18n() {
		String key = createKey("i18n");
		return StringHelper.isTrue(properties.getProperty(key, "true"));
	}

	/**
	 * render the field when he is used as reference value in a other language.
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	protected String getReferenceFieldView(ContentContext ctx) throws Exception {
		return "<div class=\"slave-field line form-group\"><label>" + getLabel(ctx, ctx.getLocale()) + "</label>" + getViewXHTMLCode(ctx) + "</div>";
	}

	protected String referenceEditCode(ContentContext ctx) throws Exception {
		if (!isI18n()) {
			DynamicComponent refComp = getReferenceComponent(ctx);
			if (refComp != null) {
				return refComp.getField(ctx, getName()).getReferenceFieldView(ctx);
			}
		}
		return null;
	}

	protected String referenceViewCode(ContentContext ctx) throws Exception {
		if (!isI18n()) {
			DynamicComponent refComp = getReferenceComponent(ctx);
			if (refComp != null) {
				return refComp.getField(ctx, getName()).getViewListXHTMLCode(ctx);
			}
		}
		return null;
	}
	
	protected String getReferenceValue(ContentContext ctx) throws Exception {
		String value = getValue();
		if (!StringHelper.isEmpty(value)) {
			return value;
		} else if (!isI18n()) {
			DynamicComponent refComp = getReferenceComponent(ctx);
			if (refComp != null) {
				return refComp.getField(ctx, getName()).getValue();
			}
		}
		return null;
	}
	
	protected String getReferenceFormatedValue(ContentContext ctx) throws Exception {
		String value = getFormatedValue(ctx);
		if (!StringHelper.isEmpty(value)) {
			return value;
		} else if (!isI18n()) {
			DynamicComponent refComp = getReferenceComponent(ctx);
			if (refComp != null) {
				return refComp.getField(ctx, getName()).getFormatedValue(ctx);
			}
		}
		return null;
	}
	
	public String getSpecialClass() {
		return "";
	}
	
	public String getForceModifFieldName() {
		return "_force_modif_"+getId();
	}

	public final String getEditXHTMLCode(ContentContext ctx) throws Exception {
		return getEditXHTMLCode(ctx, false);
	}
	
	public String getEditXHTMLCode(ContentContext ctx, boolean search) throws Exception {
		String refCode = referenceEditCode(ctx);
		if (refCode != null) {
			return refCode;
		}
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);		
		out.println("<div class=\"row field-"+getName()+"\"><div class=\""+LABEL_CSS+"\">");
		out.println(getEditLabelCode());	
		String label=null;;
		if (search) {
			label = getSearchLabel(ctx, ctx.getLocale());
		}
		if (StringHelper.isEmpty(label)) {
			label = getLabel(ctx, ctx.getLocale());
		}
		out.println("	<label for=\"" + getInputName() + "\">" + label + "</label>");
		String readOnlyHTML = "";
		if (isReadOnly()) {
			readOnlyHTML = " readonly=\"readonly\"";
		}
		String value = Encode.forHtmlAttribute(StringHelper.neverNull(getValue()));
		out.println("</div><div class=\""+VALUE_SIZE+"\"><input" + readOnlyHTML + " id=\"" + getInputName() + "\" class=\"form-control"+getSpecialClass()+"\" name=\"" + getInputName() + "\" value=\"" + value + "\"/>");
		if (getMessage() != null && getMessage().trim().length() > 0) {
			out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
		}
		out.println("</div></div>");		
		out.close();
		return writer.toString();
	}
	
	public String getSearchEditXHTMLCode(ContentContext ctx) throws Exception {
		return getEditXHTMLCode(ctx, true);
	}

	public String getFieldPrefix(ContentContext ctx) {
		Locale locale = ctx.getLocale();
		String prefix = properties.getProperty("field." + getUnicName() + ".prefix", getDefaultPrefix());
		if (isLabelDisplayed()) {
			return prefix + "<div class=\"label "+getUnicName()+"\">" + StringHelper.neverNull(getUserLabel(ctx, locale)) + "</div>";
		} else {
			return prefix;
		}
	}
	
	public String getDefaultPrefix() {
		return properties.getProperty("default.prefix", "<div class=\"dc-field dc-field-"+getName()+" dc-field-type-"+getType()+"\">");
	}
	
	public String getDefaultSuffix() {
		return properties.getProperty("default.suffix", "</div>");
	}
	
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		String refCode = referenceViewCode(ctx);		
		if (refCode != null) {
			return refCode;
		}
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		String displayStr = StringHelper.neverNull(getDisplayValue(ctx, new Locale(ctx.getContextLanguage())));
		if (!isPertinent(ctx) || displayStr.trim().length() == 0 || !isViewDisplayed()) {
			return "";
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
		if (!displayStr.toLowerCase().contains("<a")) {
			displayStr = XHTMLHelper.autoLink(displayStr);
		}
		displayStr = reverserLinkService.replaceLink(ctx, comp, displayStr);

		if (isWrapped()) {
			out.println("<" + getTag() + (StringHelper.isEmpty(getPlaceholder())?"":" placeholder=\""+getPlaceholder()+"\"")+" class=\"field-value\">" + displayStr + "</" + getTag() + ">");
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
	
	protected String getCssClass() {
		return getPropertyValue("css", "");
	}

	/**
	 * return the value "displayable"
	 * 
	 * @param locale
	 * @return
	 * @throws Exception
	 */
	public String getDisplayValue(ContentContext ctx, Locale locale) throws Exception {
		String outValue;
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
				outValue = value;
			} else {
				outValue = properties.getProperty(key);
			}
		} else {
			outValue = getValue();
		}
		if (getRemplacementCode().size() > 0) {
			for (Map.Entry<String, String> entry : getRemplacementCode().entrySet()) {
				String value = XHTMLHelper.textToXHTML(entry.getValue());
				value = XHTMLHelper.replaceJSTLData(ctx, value);
				value = StringUtils.replace(value, "${source}", "" + entry.getKey());
				outValue = StringUtils.replace(outValue, entry.getKey(), value);
			}
		}
		outValue = XHTMLHelper.autoLink(outValue, globalContext);
		outValue = XHTMLHelper.replaceJSTLData(ctx, outValue);
		outValue = ReverseLinkService.getInstance(globalContext).replaceLink(ctx, comp, outValue);
		outValue = XHTMLHelper.replaceLinks(ctx, outValue);

		String unity = getUnity(ctx);
		if (!StringHelper.isEmpty(unity)) {
			return outValue+"<span class=\"unity\">"+unity+"</span>";
		} else {
			return outValue;
		}
	}
	
	public String getPropertyValue(String suffix, String defaultValue) {
		return properties.getProperty("field." + getUnicName() + '.' + suffix, defaultValue);
	}
	
	public String getSearchType() {
		return getPropertyValue("search.type", DEFAULT_SEARCH_TYPE);
	}

	public String getFieldSuffix(ContentContext ctx) {
		return properties.getProperty("field." + getUnicName() + ".suffix", getDefaultSuffix());
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
	
	/**
	 * try to return values with locale and return default value if not.
	 * 
	 * @param locale
	 * @return
	 */
	public List<String> getValues(ContentContext ctx, Locale locale) throws Exception {
		setCurrentLocale(locale);
		List<String> out = new LinkedList<String>();
		if (getValue() != null) {
			out.add(getValue());
		} else {
			setCurrentLocale(null);
			out.add(getValue()); 
		}
		return out;
	}

	public String getValue() {
		String key = createKey("value");
		if (getCurrentLocale() != null) {
			key = createKey("value-" + getCurrentLocale());
		}
		return properties.getProperty(key);
	}
	
	public String getXHTMLValue() {
		return XHTMLHelper.textToXHTML(getValue());
	}
	
	public String getHtml(ContentContext ctx) {
		try {
			return getViewXHTMLCode(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return "Error in field ["+getType()+"] : "+e.getMessage();
		}
	}
	
	public String getInitValue() {
		String key = createKey("init-value");
		if (getCurrentLocale() != null) {
			key = createKey("init-value-" + getCurrentLocale());
		}
		return properties.getProperty(key);
	}
	
	public Collection<String> getValues() {
		return StringHelper.stringToCollection(getValue(), ",", true);
	}
	
	protected void setValue(Collection<String> values) {
		setValue(StringHelper.collectionToString(values));
	}

	public boolean isSearch() {
		String key = createKey("search");
		return StringHelper.isTrue(properties.getProperty(key));
	}

	public boolean isNeeded() {
		return StringHelper.isTrue(properties.getProperty("field." + getUnicName() + ".needed", "false"));
	}
	
	public Integer getMaxSize() {
		String maxSize = getPropertyValue("max-size", null);
		if (maxSize == null || !StringHelper.isDigit(maxSize)) {
			return null;
		} else {
			return Integer.parseInt(maxSize);
		}
	}
	
	public int getWidthEdit() {
		String widthEdit = getPropertyValue("width-edit", null);
		widthEdit = StringHelper.trimAndNullify(widthEdit);
		if (widthEdit == null || !StringHelper.isDigit(widthEdit)) {
			return 12;
		} else {
			return Integer.parseInt(widthEdit);
		}
	}

	public String getOpenRow(ContentContext ctx) {
		
		boolean openRow = StringHelper.isTrue(ctx.getRequest().getAttribute(OPEN_ROW_KEY));
		
		final String STATUS_KEY = "_widthStatus";
		int width = getWidthEdit();
		
		if (width == 12) {
			if (openRow) {
				ctx.getRequest().removeAttribute(OPEN_ROW_KEY);
				return "</div>";
			} else {
				return "";
			}
		}
		Integer widthStatus = (Integer)ctx.getRequest().getAttribute(STATUS_KEY);		
		if (widthStatus == null || width+widthStatus > 12) {
			if (widthStatus == null) {
				widthStatus=0;
				ctx.getRequest().setAttribute(STATUS_KEY, width);
			} else {
				ctx.getRequest().removeAttribute(STATUS_KEY);
			}
			ctx.getRequest().setAttribute(OPEN_ROW_KEY, true);
			if (!openRow) {
				return "<div class=\"row\"><div class=\"col-md-"+width+"\">";
			} else {
				return "</div> <!-- close row "+(width+widthStatus)+" --> <div class=\"row\"><div class=\"col-md-"+width+"\">";
			}			
		} else {
			if (width + widthStatus<12) {
				ctx.getRequest().setAttribute(STATUS_KEY, width + widthStatus);
			} else {
				ctx.getRequest().removeAttribute(STATUS_KEY);
			}
			return "<div class=\"col-md-"+width+"\">";
		}
	}
	
	public String getCloseRow(ContentContext ctx) {
		int width = getWidthEdit();
		if (width == 12) {
			return "";
		}
		if (last) {
			ctx.getRequest().removeAttribute(OPEN_ROW_KEY);
			return "</div></div>";	
		} else {
			return "</div>";
		}
		
	}
	
	public void setLast(boolean last) {
		this.last = last;
	}
	
	public boolean isLast() {
		return last;
	}
	
	public void setFirst(boolean first) {
		this.first = first;
	}
	
	public boolean isFirst() {
		return first;
	}
	
	public Integer getMinSize() {
		String minSize = getPropertyValue("min-size", null);
		if (minSize == null || !StringHelper.isDigit(minSize)) {
			return null;
		} else {
			return Integer.parseInt(minSize);
		}
	}

	public int getOrder() {
		String orderStr = properties.getProperty("field." + getUnicName() + ".order", "0").trim();
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
	
	public void setValues(List<String> values) {
		String sep = "";
		String value = "";
		for (String val : values) {
			value = value + sep + val;
			sep=";";
		}
		setValue(value);
	}

	public void setLabelValue(String value) {
		properties.setProperty("field." + getUnicName() + ".user-label", value);
	}

	public boolean isLabelDisplayed() {
		return StringHelper.isTrue(properties.getProperty("field." + getUnicName() + ".label-displayed"), defaultLabelDisplayed());
	}
	
	public boolean defaultLabelDisplayed() {
		return StringHelper.isTrue(properties.getProperty("default.label-displayed"), false);
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

	public boolean isPertinent(ContentContext ctx) {
		String value;
		try {
			value = getReferenceValue(ctx);
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
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

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	protected String getLabel(ContentContext ctx, Locale locale) {
		if (getLabel().trim().length() != 0) {
			return getLabel();
		}
		String key = createKey("label." + locale.getLanguage());		
		String label = properties.getProperty(key);		
		if (label == null) {
			label = properties.getProperty(createKey("label"));
			if (label == null) {
				label = getUnicName();
			}
		}
		key = properties.getProperty(createKey("label.key"));
		if (key != null) {
			label = i18nAccess.getAllText(key.trim(), label);
		}

		return label;
	}
	
	protected String getUnity(ContentContext ctx, Locale locale) {		
		String unity = properties.getProperty(createKey("unity." + locale.getLanguage()));
		if (unity == null) {
			unity = properties.getProperty(createKey("unity"));			
		}
		String key = properties.getProperty(createKey("unity.key"));
		if (key != null) {
			unity = i18nAccess.getAllText(key.trim(), unity);
		}

		return unity;
	}
	
	protected String getUnity(ContentContext ctx) {
		return getUnity(ctx, ctx.getLocale());
	}
	
	protected String getSearchLabel(ContentContext ctx, Locale locale) {
		String label = properties.getProperty(createKey("label.search." + locale.getLanguage()));
		if (label == null) {
			label = properties.getProperty(createKey("label.search"));
			if (label == null) {
				label = getLabel(ctx, locale);
			}
		}
		String key = properties.getProperty(createKey("label.search.key"));
		if (key != null) {
			label = i18nAccess.getAllText(key.trim(), label);
		}
		return label;
	}

	protected final String getUserLabel() {
		return properties.getProperty(createKey("user-label"), null);
	}

	public String getUserLabel(ContentContext ctx, Locale locale) {
		String label = getUserLabel();
		if (label == null) {
			label = getLabel(ctx, locale);
		}
		return label;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	protected String getUnicName() {
		return getName();
	}

	protected boolean isHtml() {
		return false;
	}

	/**
	 * process the field
	 * @return true if the field is modified.
	 */
	public boolean process(ContentContext ctx) {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		List<String> values = requestService.getParameterListValues(getInputName(), null);
		String label = requestService.getParameter(getInputLabelName(), null);
		boolean modify = false;
		if (label != null) {
			if (!label.equals(getUserLabel())) {
				modify = true;
				setLabelValue(label);
			}
		}
		if (values != null) {
			String value;
			if (values.size()>1) {
				value = StringHelper.collectionToString(values, ", ");
			} else {
				value = requestService.getParameter(getInputName(), "");
			}
			if (isHtml()) {
				try {
					value = XHTMLHelper.replaceAbsoluteLinks(ctx, value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (!value.equals(getValue())) {
				setValue(value);
				if (!validate()) {
					setNeedRefresh(true);
				}
				modify = true;
			}
		} else {
			setValue("");
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
	
	public String getMetaData(String inKey, String defaultValue) {
		String key = createKey(inKey);
		String localKey = null;
		if (getCurrentLocale() != null) {
			localKey = createKey(inKey + '-' + getCurrentLocale());
		}
		if (localKey != null && properties.get(localKey) != null) {
			return properties.getProperty(localKey);
		}
		String outData = properties.getProperty(key);
		if (outData == null) {
			outData = defaultValue;
		}
		return outData;
	}

	public String getMetaData(String inKey) {
		return getMetaData(inKey, null);
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

	public boolean initContent(ContentContext ctx) throws Exception {
		String initVal = getInitValue();
		if (initVal == null) {
			initVal = getLabel(ctx, ctx.getLocale());
		}
		if (getValue() == null || getValue().trim().length() == 0) {
			setValue(initVal);
		}
		return true;
	}

	public Map<String, String> getRemplacementCode() {
		if (replacementCode == null) {
			replacementCode = new HashMap<String, String>();
			String keyPrefix = createKey("replacement-");
			for (int i = 0; i < 1000; i++) {
				String source = properties.getProperty(keyPrefix + i + ".source");
				String target = properties.getProperty(keyPrefix + i + ".target");
				if (source != null && target != null) {
					replacementCode.put(source, target);
				}
			}
		}
		return replacementCode;
	}

	public void reload(ContentContext ctx) {
		replacementCode = null;
	}

	public boolean isTitle() {
		return false;
	}

	public String getPageDescription() {
		return null;
	}
	
	public Field getReference(ContentContext ctx) throws Exception {
		if (isI18n()) {
			return this;
		} else {
			DynamicComponent ref = Field.this.getReferenceComponent(ctx);
			if (ref == null) {
				return this;
			} else {
				return ref.getField(ctx, Field.this.getName());
			}
		}		
	}
	
	public String renderSelect(ContentContext ctx, String label, String inValue, Map<String,String> inValues, boolean sort, String cssClass) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		Map<String,String> valuesMap = inValues;				
		List<Map.Entry<String, String>> values = new LinkedList<Entry<String, String>>(valuesMap.entrySet());
		if (sort) {
			Collections.sort(values, new JavaHelper.MapEntriesSortOnValue());
		}
		if (label == null) {
			label = getLabel(ctx, ctx.getLocale());
		}

		out.println("<div class=\"form-group "+StringHelper.neverNull(cssClass)+"\">");
		out.println(getEditLabelCode());
		out.println("<div class=\"row\"><div class=\"col-sm-3\"><label for=\"" + getInputName() + "\">" + label + "</label></div>");
		out.println("<div class=\"col-sm-9\"><select class=\"form-control\" id=\"" + getInputName() + "\" name=\"" + getInputName() + "\" value=\"" + StringHelper.neverNull(getValue()) + "\">");
 
		for (Map.Entry<String, String> value : values) {
			String selected = "";
			if (getValue() != null) {
				if (getValue().equals(value.getKey())) {
					selected = " selected=\"selected\"";
				}
			}
			if (value.getKey() != null) {
				out.println("		<option value=\"" + value.getKey() + "\"" + selected + ">" + value.getValue() + "</option>");
			} else {
				out.println("		<option" + selected + ">" + value.getValue() + "</option>");
			}
		}

		out.println("	</select>");
		if (getMessage() != null && getMessage().trim().length() > 0) {
			out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
		}
		out.println("</div>");
		
		out.println("</div></div>");

		out.close();
		return writer.toString();
	}
	
	public boolean isRealContent(ContentContext ctx) {
		return getValue() != null && getValue().trim().length() > 0;
	}
	
	/**
	 * return the size of the component
	 * @param ctx
	 * @return large, small or normal
	 */
	public String getSize(ContentContext ctx) {
		if (getValue() == null) {
			return "normal";
		}
		int size = getValue().length();
		if (size < Integer.parseInt(getPropertyValue("size.small", "16"))) {
			return "small";
		} else if (size > Integer.parseInt(getPropertyValue("size.large", "64"))) {
			return "large";
		} else {
			return "normal";
		}		
	}

	@Override
	public Map<String, Object> getContentAsMap(ContentContext ctx) throws Exception {
		return BeanHelper.bean2Map(newFieldBean(ctx));
	}
	
	public boolean search(ContentContext ctx, String query) {
		if (getValue() == null || query == null) {
			return false;
		} else {
			return getValue().toLowerCase().contains(query.toLowerCase().trim());
		}
	}

	@Override
	public int compareTo(Field o) {
		if (o == null || o.getValue() == null) {
			return -1;
		}
		if (getValue() == null) {
			return 1;
		}
		Date localDate = StringHelper.smartParseDate(getValue());
		if (localDate != null) {
			Date inDate = StringHelper.smartParseDate(o.getValue());
			if (inDate != null) {
				return localDate.compareTo(inDate);
			}
		}
		if (StringHelper.isDigit(getValue()) && StringHelper.isDigit(o.getValue())) {
			return Integer.parseInt(o.getValue()) - Integer.parseInt(getValue());
		}
		return getValue().compareTo(o.getValue());
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public FieldBean getBean(ContentContext ctx) {
		return newFieldBean(ctx);
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}
	
	protected boolean isValueTranslatable() {
		if (getType().equals("text")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean transflateFrom(ContentContext ctx, ITranslator translator, String lang) {
		if (!isValueTranslatable()) {
			return false;
		} else {
			boolean translated = false;
			String newValue="";
			if (!StringHelper.isEmpty(getValue())) {
				translated = true;
				newValue = translator.translate(ctx, getValue(), lang, ctx.getRequestContentLanguage());
				if (newValue == null) {
					translated=false;
					newValue = ITranslator.ERROR_PREFIX+getValue();
				}
			}
			setValue(newValue);
			return translated;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(">>>>>>>>> Field.main : "+Pattern.matches("[A-Z]*", "COUCOU")); //TODO: remove debug trace
	}
}

