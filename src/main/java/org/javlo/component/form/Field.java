package org.javlo.component.form;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.javlo.helper.StringHelper;
import org.javlo.helper.Comparator.StringComparator;

public class Field {

	public static final class FieldComparator implements Comparator<Field> {

		@Override
		public int compare(Field o1, Field o2) {
			if (o1.getOrder() > 0 && o2.getOrder() > 0) {
				return o1.getOrder().compareTo(o2.getOrder());
			} else {
				return StringComparator.compareText(o1.getName(), o2.getName());
			}
		}
	}

	protected static final char SEP = '|';

	private String name;
	private String label;
	private String condition = "";
	private String type = "text";
	private String value;
	private String list = "";
	private String registeredList = "";
	private int order = 0;
	private int width = 12;
	private boolean last = false;
	private boolean first = false;

	protected static List<? extends Object> FIELD_TYPES = Arrays.asList(new String[] { "text", "large-text", "yes-no", "true-false", "email", "radio", "list", "registered-list", "file", "validation" });

	public Field(String name, String label, String type, String condition, String value, String list, String registeredList, int order, int width) {
		this.name = name;
		this.label = label;		
		this.condition = condition;
		this.type = type;
		this.value = value;
		this.list = list;
		this.registeredList = registeredList;
		this.order = order;
		this.setWidth(width);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = StringHelper.createASCIIString(name).replace(' ', '_');
	}

	@Override
	public String toString() {
		return getLabel() + SEP + getType() + SEP + getValue() + SEP + list + SEP + getOrder() + SEP + getRegisteredList() + SEP + getOrder() + SEP + getWidth() + SEP + getCondition();
	}

	public boolean isRequire() {
		if (getName().length() > 0) {
			return Character.isUpperCase(getName().charAt(0));
		} else {
			return false;
		}
	}

	public void setRequire(boolean require) {
		if (getName().length() > 0) {
			if (require) {
				setName(getName().substring(0, 1).toUpperCase() + getName().substring(1));
			} else {
				setName(getName().substring(0, 1).toLowerCase() + getName().substring(1));
			}
		}

	}

	public List<String> getList() {
		List<String> outList = StringHelper.stringToCollection(list);
		return outList;
	}

	public void setList(String list) {
		this.list = StringHelper.replaceCR(list, StringHelper.DEFAULT_LIST_SEPARATOR);
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(int ordre) {
		this.order = ordre;
	}

	public String getPrefix() {
		return "field";
	}

	public List<? extends Object> getFieldTypes() {
		return FIELD_TYPES;
	}

	public String getRegisteredList() {
		return registeredList;
	}

	public void setRegisteredList(String registeredList) {
		this.registeredList = registeredList;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * is the last element of cols sequence. That mean width with next field
	 * is greater than 12.
	 * 
	 * @return
	 */
	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	/**
	 * is the first element of cols sequence.
	 * 
	 * @return
	 */
	public boolean isFirst() {
		return first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public String getConditionField() {
		if (condition == null || !condition.contains("=")) {
			return null;
		} else {
			return condition.substring(0,condition.indexOf('='));
		}
	}
	
	public String getConditionTest() {
		if (condition == null || !condition.contains("=")) {
			return null;
		} else {
			return condition.substring(condition.indexOf('=')+1);
		}
		
	}
	
	public boolean isFilledWidth( String value ) {
		List<String> list = getList();
		if (list.size() == 0) {
			return getName().length() > 0 && !StringHelper.isEmpty(value);
		} else {
			return value != null && !list.iterator().next().equals(value);
		}
	}

}