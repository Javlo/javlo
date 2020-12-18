/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.javlo.context.ContentContext;
import org.javlo.service.google.translation.ITranslator;

/**
 * @author pvandermaesen
 */
public abstract class ComplexPropertiesLink extends AbstractVisualComponent {

	protected static final String HEADER_V1_0 = "link storage V.1.0";

	public static final String LINK_KEY = "link";

	protected static final String LABEL_KEY = "label";

	protected Properties properties = new Properties();

	protected static final String REVERSE_LINK_KEY = "reverse-link";

	@Override
	protected void init() throws org.javlo.exception.ResourceNotFoundException {
		reloadProperties();
	};

	String getHeader() {
		return HEADER_V1_0;
	}

	public String getLinkName() {
		return "link" + ID_SEPARATOR + getId();
	}

	public String getLinkLabelName() {
		return LABEL_KEY + ID_SEPARATOR + getId();
	}

	@Override
	public void setValue(String inContent) {
		super.setValue(inContent);
		reloadProperties();
	}

	public void storeProperties() {
		synchronized (properties) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				properties.store(out, getHeader());
			} catch (IOException e) {
				e.printStackTrace();
			}
			String res = new String(out.toByteArray());
			if (!res.equals(getValue())) {
				setValue(res);
				setModify();
			}
		}
	}

	public void reloadProperties() {
		synchronized (properties) {
			properties.clear();
			try {
				properties.load(stringToStream(getValue()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected String getField(String key) {
		return properties.getProperty(key);
	}

	protected String getField(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	protected void setField(String key, String value) {
		properties.setProperty(key, value);
		storeProperties();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ComplexPropertiesLink)) {
			return false;
		}
		ComplexPropertiesLink comp = (ComplexPropertiesLink) obj;

		if (getComponentBean().getStyle() != null && comp.getComponentBean().getStyle() != null) {
			boolean eq = getComponentBean().getStyle().equals(comp.getComponentBean().getStyle());
			eq = eq && getComponentBean().isList() == comp.getComponentBean().isList();
			eq = eq && getComponentBean().isRepeat() == comp.getComponentBean().isRepeat();
			eq = eq && getComponentBean().getLanguage().equals(comp.getComponentBean().getLanguage());
			eq = eq && getComponentBean().getValue().equals(comp.getComponentBean().getValue());
			eq = properties.equals(comp.properties);
			return eq;
		} else {
			return false;
		}
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
	}

	@Override
	public String getFontAwesome() {
		return "link";
	}

	@Override
	protected boolean isValueTranslatable() {
		return true;
	}

	@Override
	public boolean transflateFrom(ContentContext ctx, ITranslator translator, String lang) {
		if (!isValueTranslatable()) {
			return false;
		} else {
			boolean translated = true;
			for (Object key : properties.keySet()) {
				if (!key.equals(REVERSE_LINK_KEY) && !key.equals(LINK_KEY)) {
					String value = (String) properties.getProperty((String) key);
					String newValue = translator.translate(ctx, value, lang, ctx.getRequestContentLanguage());
					properties.setProperty((String) key, newValue);
					if (newValue == null) {
						translated = false;
						newValue = ITranslator.ERROR_PREFIX + getValue();
					}
				}
			}
			storeProperties();
			return translated;
		}
	}

}
