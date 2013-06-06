/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.dynamic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.javlo.bean.Link;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IDate;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.fields.Field;
import org.javlo.fields.FieldFactory;
import org.javlo.fields.IFieldContainer;
import org.javlo.fields.MetaField;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.resource.Resource;
import org.javlo.ztatic.IStaticContainer;

/**
 * @author pvandermaesen
 */
public class DynamicComponent extends AbstractVisualComponent implements IStaticContainer, IFieldContainer, IDate {

	public static final String HIDDEN = "hidden";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(DynamicComponent.class.getName());

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "standard", HIDDEN };
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		I18nAccess i18nAccess;
		try {
			i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return new String[] { "standard", i18nAccess.getText("global.hidden") };
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getStyleList(ctx);
	}

	public class FieldOrderComparator implements Comparator<Field> {
		@Override
		public int compare(Field o1, Field o2) {
			return o1.getOrder() - o2.getOrder();
		}
	}

	private Properties properties = null;

	private Properties configProperties = null;

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
		reloadProperties();
	}

	protected void reloadProperties() {
		try {
			if (properties != null) {
				properties.load(stringToStream(getValue()));
			}
		} catch (IOException e) {
			// not possible -> load from string
			e.printStackTrace();
		}
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		if (!isWrapped()) {
			return "";
		} else {
			return super.getPrefixViewXHTMLCode(ctx);
		}
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		if (!isWrapped()) {
			return "";
		} else {
			return super.getSuffixViewXHTMLCode(ctx);
		}
	}

	@Override
	public String getViewListXHTMLCode(ContentContext ctx) throws Exception {
		return getViewXHTMLCode(ctx, true);
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return getViewXHTMLCode(ctx, false);
	}

	public String getViewXHTMLCode(ContentContext ctx, boolean asList) throws Exception {
		if (getStyle(ctx).equals(HIDDEN)) {
			String emptyCode = getEmptyCode(ctx);
			if (emptyCode != null) {
				return emptyCode;
			} else {
				return "";
			}
		}

		Collection<Field> fields = getFields(ctx);

		for (Field field : fields) {
			if (field instanceof MetaField) {
				MetaField mField = (MetaField) field;
				if (!mField.isPublished(ctx)) {
					return "";
				}
			}
		}

		if (asList) {
			if (getListRenderer() != null) {
				for (Field field : fields) {
					if (field != null) {
						field.fillRequest(ctx);
					}
				}
				String linkToJSP = URLHelper.createStaticTemplateURLWithoutContext(ctx, ctx.getCurrentTemplate(), "" + getListRenderer());
				return executeJSP(ctx, linkToJSP);
			}
		} else {
			if (getRenderer() != null) {
				for (Field field : fields) {
					if (field != null) {
						field.fillRequest(ctx);
					}
				}
				String linkToJSP = URLHelper.createStaticTemplateURLWithoutContext(ctx, ctx.getCurrentTemplate(), "" + getRenderer());
				return executeJSP(ctx, linkToJSP);
			}
		}

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		boolean allValid = true;
		for (Field field : fields) {
			if (!field.validate()) {
				allValid = false;
			}
		}

		String cssClass = "";
		if (getCSSClass().trim().length() > 0) {
			cssClass = ' ' + getCSSClass();
		}

		out.println(getPrefix());

		if (isWrapped()) {
			if (allValid) {
				out.println("<div class=\"valid" + cssClass + "\">");
			} else {
				out.println("<div class=\"not-valid" + cssClass + "\">");
			}
		}
		String firstFiledClass = " first-field";
		for (Field field : fields) {
			if (field != null) {

				if (field.getTranslation() != null) {
					field.setCurrentLocale(new Locale(ctx.getRequestContentLanguage()));
				}

				if (field.isDiplayedInList(ctx) || !asList) {
					if (field.isViewDisplayed() && field.isPertinent()) {
						cssClass = "";
						if (field.getCSSClass() != null && field.getCSSClass().trim().length() > 0) {
							cssClass = ' ' + field.getCSSClass();
						}
						out.println(field.getFieldPrefix(ctx));
						if (field.isWrapped()) {
							out.println("<div class=\"field " + field.getName() + firstFiledClass + cssClass + "\">");
						}
						out.println(field.getViewXHTMLCode(ctx));
						if (field.isWrapped()) {
							out.println("</div>");
						}
						out.println(field.getFieldSuffix(ctx));
						firstFiledClass = "";
					}
				}
			}
		}
		if (isWrapped()) {
			out.println("<div class=\"end\"><span>&nbsp;</span></div>");
			out.println("</div>");
		}
		out.println(getSuffix());
		out.close();
		return writer.toString();
	}

	@Override
	public java.util.List<String> getFieldsNames() {
		java.util.List<String> outFields = new LinkedList<String>();
		Collection keys = properties.keySet();
		for (Object keyObj : keys) {
			String key = (String) keyObj;
			if (key.startsWith("field.")) {
				String[] keySplit = key.split("\\.");
				if (keySplit.length > 1) {
					String name = keySplit[1];
					if (!outFields.contains(name)) {
						outFields.add(name);
					}
				}
			}
		}
		return outFields;
	}

	@Override
	public java.util.List<Field> getFields(ContentContext ctx) throws FileNotFoundException, IOException {

		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		java.util.List<Field> outFields = new LinkedList<Field>();
		java.util.List<String> fieldExecuted = new LinkedList<String>();

		Collection keys = properties.keySet();
		for (Object keyObj : keys) {
			String key = (String) keyObj;
			if (key.startsWith("field.")) {
				String[] keySplit = key.split("\\.");
				if (keySplit.length > 1) {
					String name = keySplit[1];
					Field field = FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), null, name, getType(name), getId());
					if (field != null) {
						if (!fieldExecuted.contains(name)) {
							outFields.add(field);
						}
						fieldExecuted.add(name);
					} else {
						logger.severe("field not found : " + getType(name));
					}
				}
			}
		}
		Collections.sort(outFields, new FieldOrderComparator());
		return outFields;
	}

	@Override
	public Field getField(ContentContext ctx, String name) throws FileNotFoundException, IOException {
		java.util.List<Field> fields = getFields(ctx);
		for (Field field : fields) {
			if (field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}

	@Override
	public String getFieldValue(ContentContext ctx, String name) throws FileNotFoundException, IOException {
		Field field = getField(ctx, name);
		if (field != null) {
			return field.getValue();
		}
		return null;
	}

	public String getCSSClass() {
		return properties.getProperty("component.css-class", "");
	}

	public String getPrefix() {
		return properties.getProperty("component.prefix", "");
	}

	public String getSuffix() {
		return properties.getProperty("component.suffix", "");
	}

	public boolean isWrapped() {
		return StringHelper.isTrue(properties.getProperty("component.wrapped", "true"));
	}

	private String getRenderer() {
		return properties.getProperty("component.renderer", null);
	}

	private String getListRenderer() {
		return properties.getProperty("component.list-renderer", null);
	}

	@Override
	public Map<String, String> getList(String listName, Locale locale) {
		Map<String, String> res = new HashMap<String, String>();
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
					res.put(splitedValue[0].trim(), value);
				} else {
					if (properties.getProperty("list." + listName + "." + i + "." + locale.getLanguage()) != null) {
						value = properties.getProperty("list." + listName + "." + i + "." + locale.getLanguage());
					}
					res.put(value, value);
				}
			}
		}
		return res;
	}

	@Override
	public Map<String, String> getList(String listName) {
		Map<String, String> res = new HashMap<String, String>();
		for (int i = 0; i < 9999; i++) {
			String value = properties.getProperty("list." + listName + "." + i);
			if (value != null) {
				String[] splitedValue = value.split(",");
				if (splitedValue.length > 1) {
					res.put(splitedValue[0].trim(), splitedValue[1].trim());
				} else {
					res.put(value, value);
				}
			}
		}
		return res;
	}

	@Override
	protected String getInputName(String field) {
		return field + "-" + getId();
	}

	protected String getType(String field) {
		return properties.getProperty("field." + field + ".type");
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		Collection<Field> fields = getFields(ctx);
		boolean allValid = true;
		for (Field field : fields) {
			if (!field.validate()) {
				allValid = false;
			}
		}

		if (allValid) {
			out.println("<div class=\"dynamic-component valid\">");
		} else {
			out.println("<div class=\"dynamic-component not-valid\">");
		}
		for (Field field : fields) {
			if (field != null) {
				if (field.getTranslation() != null) {
					I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
					out.println("<fieldset><legend>" + i18nAccess.getText("field.translated") + "</legend>");
				}

				Collection<Locale> translatedField = new LinkedList<Locale>();
				if (field.getTranslation() == null) {
					translatedField = new LinkedList<Locale>();
					translatedField.add(null);
				} else {
					translatedField = field.getTranslation();
				}

				for (Locale locale : translatedField) {
					if (locale != null) {
						out.println("<fieldset><legend>" + locale.getDisplayLanguage(new Locale(GlobalContext.getInstance(ctx.getRequest()).getEditLanguage(ctx.getRequest().getSession()))) + "</legend>");
					}
					field.setCurrentLocale(locale);
					out.println(field.getEditXHTMLCode(ctx));
					if (locale != null) {
						out.println("</fieldset>");
					}
				}
				if (field.getTranslation() != null) {
					out.println("</fieldset>");
				}
			}
		}
		out.println("</div>");
		out.close();
		return writer.toString();
	}

	@Override
	public String getType() {
		return properties.getProperty("component.type");
	}

	public void storeProperties() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			properties.store(out, "component: " + getType());
		} catch (IOException e) {
			e.printStackTrace();
		}
		String res = new String(out.toByteArray());
		setValue(res);
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {

		java.util.List<Field> fieldsName = getFields(ctx);

		for (Field field : fieldsName) {

			Collection<Locale> languages;
			if (field.getTranslation() == null) {
				languages = new LinkedList<Locale>();
				languages.add(null);
			} else {
				languages = field.getTranslation();
			}

			for (Locale locale : languages) {
				field.setCurrentLocale(locale);
				boolean modify = field.process(ctx.getRequest());
				if (modify) {
					setModify();
					if (field.isNeedRefresh()) {
						setNeedRefresh(true);
					}
				}
			}
		}

		if (isModify()) {
			storeProperties();
		}
	}

	@Override
	public void setValue(String inContent) {
		super.setValue(inContent);
		reloadProperties();
	}

	@Override
	public String getHexColor() {
		return properties.getProperty("component.color", IContentVisualComponent.DYN_COMP_COLOR);
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void setConfigProperties(Properties properties) {
		this.configProperties = properties;
	}

	public Properties getConfigProperties() {
		return configProperties;
	}

	@Override
	public IContentVisualComponent newInstance(ComponentBean bean, ContentContext newCtx) throws Exception {
		DynamicComponent res = (DynamicComponent) this.clone();
		Properties newProp = new Properties();
		newProp.putAll(getConfigProperties());
		res.setProperties(newProp); // transfert meta-data of
		// dynamiccomponent
		res.init(bean, newCtx);

		return res;
	}

	@Override
	public String getComponentLabel(ContentContext ctx, String lg) {
		if (properties == null) {
			return super.getComponentLabel(ctx, lg);
		}
		String langLabel = properties.getProperty("component.label." + lg);
		if (langLabel != null) {
			return langLabel;
		}
		String genericLabel = properties.getProperty("component.label");
		if (genericLabel != null) {
			return genericLabel;
		}
		return super.getComponentLabel(ctx, lg);
	}

	@Override
	public String getKey() {
		return getType();
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return !isEmpty(ctx);
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		try {
			java.util.List<Field> fieldsName = getFields(ctx);
			for (Field field : fieldsName) {
				if (!field.isContentCachable()) {
					return false;
				}
				if (field instanceof MetaField) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
		return true;
	}

	@Override
	public boolean contains(ContentContext ctx, String uri) {
		try {
			for (Field field : getFields(ctx)) {
				if (field instanceof IStaticContainer) {
					if (((IStaticContainer) field).contains(ctx, uri)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Collection<Resource> getAllResources(ContentContext ctx) {
		Collection<Resource> outResources = new LinkedList<Resource>();
		try {
			for (Field field : getFields(ctx)) {
				if (field instanceof IStaticContainer) {
					outResources.addAll(((IStaticContainer) field).getAllResources(ctx));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outResources;
	}

	@Override
	public boolean renameResource(ContentContext ctx, File oldName, File newName) {
		boolean outRename = false;
		try {
			for (Field field : getFields(ctx)) {
				if (field instanceof IStaticContainer) {
					if (((IStaticContainer) field).renameResource(ctx, oldName, newName)) {
						outRename = true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outRename;
	}

	@Override
	public String getLabel(ContentContext ctx) {
		return properties.getProperty("component.label-" + ctx.getRequestContentLanguage(), properties.getProperty("component.label", properties.getProperty("component.type")));
	}

	@Override
	public boolean isContentTimeCachable(ContentContext ctx) {
		return false;
	}

	@Override
	public Collection<Link> getAllResourcesLinks(ContentContext ctx) {
		Collection<Link> outResources = new LinkedList<Link>();
		try {
			for (Field field : getFields(ctx)) {
				if (field instanceof IStaticContainer) {
					outResources.addAll(((IStaticContainer) field).getAllResourcesLinks(ctx));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outResources;
	}

	@Override
	public Date getDate(ContentContext ctx) {
		try {
			for (Field field : getFields(ctx)) {
				if (field instanceof IDate) {
					return ((IDate) field).getDate(ctx);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getPopularity(ContentContext ctx) {
		try {
			for (Field field : getFields(ctx)) {
				if (field instanceof IStaticContainer) {
					return ((IStaticContainer) field).getPopularity(ctx);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int contentHashCode() {
		StringBuffer value = new StringBuffer();
		List<String> keys = new LinkedList(properties.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			value.append(key);
			value.append("=");
			value.append(properties.get(key));
			value.append('/');
		}
		return value.toString().hashCode();
	}

	@Override
	public String getTextForSearch() {
		StringBuffer outText = new StringBuffer();
		for (Object key : properties.keySet()) {
			outText.append(properties.get(key));
			outText.append(' ');
		}
		return outText.toString();
	}
	
	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		reloadProperties();
		boolean outInit = false;
		System.out.println("***** DynamicComponent.initContent : size = "+getFields(ctx).size()); //TODO: remove debug trace
		for (Field field : getFields(ctx)) {
			System.out.println("***** DynamicComponent.initContent : field = "+field.getName()); //TODO: remove debug trace
			if (field.initContent(ctx)) {
				outInit = true;
			}
		}
		return outInit;
	}

}