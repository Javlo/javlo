/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author pvandermaesen
 */
public abstract class ComplexPropertiesLink extends AbstractVisualComponent {

	protected static final String HEADER_V1_0 = "link storage V.1.0";

	public static final String LINK_KEY = "link";

	protected static final String LABEL_KEY = "label";

	protected Properties properties = new Properties();

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
		return "label" + ID_SEPARATOR + getId();
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

		boolean eq = getComponentBean().getStyle().equals(comp.getComponentBean().getStyle());
		eq = eq && getComponentBean().isList() == comp.getComponentBean().isList();
		eq = eq && getComponentBean().isRepeat() == comp.getComponentBean().isRepeat();
		eq = eq && getComponentBean().getLanguage().equals(comp.getComponentBean().getLanguage());
		eq = eq && getComponentBean().getValue().equals(comp.getComponentBean().getValue());
		eq = properties.equals(comp.properties);

		return eq;
	}

}
