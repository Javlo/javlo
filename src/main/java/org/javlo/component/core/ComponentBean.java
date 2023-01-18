/*
 * Created on 09-janv.-2004
 */
package org.javlo.component.core;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.javlo.helper.DebugHelper;
import org.javlo.helper.StringHelper;
import org.javlo.service.visitors.CookiesService;
import org.javlo.user.User;

/**
 * @author pvandermaesen component state less.
 */
public class ComponentBean implements Serializable, Comparable<ComponentBean> {
	
	public static int INSTANCE = 0;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_AREA = "content";

	private String id;
	private String type;
	private String value;	
	private String style;
	private boolean list = false;
	private boolean hidden = false;
	private String language;
	private String renderer;
	private String authors = "";
	private String backgroundColor = null;
	private String textColor = null;
	private ComponentLayout layout = null;
	private String manualCssClass = null; 
	private int columnSize = -1;
	private String columnStyle = null;

	private boolean repeat = false;
	//private boolean forceCachable = false;
	private boolean modify = false;
	private boolean nolink = false;
	private Set<Integer> hiddenModes;
	private int cookiesDisplayStatus = CookiesService.ALWAYS_STATUS;

	private Date creationDate = new Date();
	private Date modificationDate = new Date();
	private Date deleteDate = null;

	private String area = DEFAULT_AREA;

	public ComponentBean() {		
		super();
		id = "";
		type = "";
		value = "";
		language = "";
		INSTANCE++;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		INSTANCE--;
	}

	public ComponentBean(String newType, String newValue, String newLanguague) {	
		INSTANCE++;
		type = newType;
		value = newValue;
		language = newLanguague;
	}

	public ComponentBean(String newId, String newType, String newValue, String newLanguague, boolean newRepeat, User authors) {
		INSTANCE++;
		id = newId;
		type = newType;
		value = newValue;
		language = newLanguague;
		repeat = newRepeat;
		if (authors != null) {
			this.authors = authors.getLogin();
		}
	}
	
	public ComponentBean(ComponentBean bean) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		INSTANCE++;
		BeanUtils.copyProperties(this, bean);
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

	public Set<Integer> getHiddenModes() {
		return hiddenModes;
	}

	public void setHiddenModes(Set<Integer> hiddenModes) {
		this.hiddenModes = hiddenModes;
	}
	
	public void resetArea() {
		area = null;
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
		if (modify) {			
			modificationDate = new Date();
		}
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

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}
	
	@Override
	public int compareTo(ComponentBean bean) {		
		return getModificationDate().compareTo(bean.getModificationDate());
	}

	public ComponentLayout getLayout() {
		return layout;
	}

	public void setLayout(ComponentLayout layout) {
		this.layout = layout;
	}

	public boolean isNolink() {
		return nolink;
	}

	public void setNolink(boolean autolink) {
		this.nolink = autolink;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public int getCookiesDisplayStatus() {
		return cookiesDisplayStatus;
	}

	public void setCookiesDisplayStatus(int cookiesDisplayStatus) {
		this.cookiesDisplayStatus = cookiesDisplayStatus;
	}

	public Date getDeleteDate() {
		return deleteDate;
	}

	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}
	
	public static void main(String[] args) {
		ComponentBean c = new ComponentBean("sldkj","l", "d");
		ComponentBean c2 = new ComponentBean();
		System.out.println(">>>>>>>>> ComponentBean.main : instance = "+ComponentBean.INSTANCE); //TODO: remove debug trace
	}

	public String getManualCssClass() {
		return manualCssClass;
	}

	public void setManualCssClass(String manualCssClass) {
		this.manualCssClass = manualCssClass;
	}

	public int getColumnSize() {
		return columnSize;
	}

	public void setColumnSize(int columnSize) {
		this.columnSize = columnSize;
	}

//	public boolean isForceCachable() {
//		return forceCachable;
//	}
//
//	public void setForceCachable(boolean forceCachable) {
//		this.forceCachable = forceCachable;
//	}

	public String getColumnStyle() {
		return columnStyle;
	}

	public void setColumnStyle(String columnStyle) {
		this.columnStyle = columnStyle;
	}

}
