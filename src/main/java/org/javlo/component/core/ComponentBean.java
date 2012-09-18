/*
 * Created on 09-janv.-2004
 */
package org.javlo.component.core;

import org.javlo.helper.StringHelper;

/**
 * @author pvandermaesen component state less.
 */
public class ComponentBean {

	public static final String DEFAULT_AREA = "content";

	String id;
	String type;
	String value;
	String style;
	boolean list = false;
	String language;
	String renderer;

	boolean repeat = false;
	boolean modify = false;

	String area = DEFAULT_AREA;

	public ComponentBean() {
		id = "";
		type = "";
		value = "";
		language = "";
	}

	public ComponentBean(String newId, String newType, String newValue) {
		id = newId;
		type = newType;
		value = newValue;
	}

	public ComponentBean(String newId, String newType, String newValue, String newLanguague, boolean newRepeat) {
		id = newId;
		type = newType;
		value = newValue;
		language = newLanguague;
		repeat = newRepeat;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		ComponentBean bean = (ComponentBean) obj;
		return bean.id.equals(id) && bean.language.equals(language) && bean.type.equals(type) && bean.value.equals(value) && (bean.repeat == repeat) && (bean.area.equals(area));
	}

	public String getArea() {
		return area;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return
	 */
	public String getLanguage() {
		return language;
	}

	public String getStyle() {
		return style;
	}

	/**
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return
	 */
	public String getValue() {
		return value;
	}

	public boolean isList() {
		return list;
	}

	public boolean isModify() {
		return modify;
	}

	/**
	 * @return Returns the repeat.
	 */
	public boolean isRepeat() {
		return repeat;
	}

	public String getRenderer() {
		return renderer;
	}

	public void setRenderer(String renderer) {
		this.renderer = renderer;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param string
	 */
	public void setLanguage(String string) {
		language = string;
	}

	public void setList(boolean list) {
		this.list = list;
	}

	public void setModify(boolean modify) {
		this.modify = modify;
	}

	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setValue(String value) {
		this.value = StringHelper.escapeWordChar(value);
	}

}
