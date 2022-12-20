package org.javlo.component.form;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.Comparator.StringComparator;
import org.javlo.service.IListItem;
import org.javlo.service.ListService;

public class Field {

	private static Logger logger = Logger.getLogger(Field.class.getName());

	private static final int SMALL_SIZE = 8;
	private static final int MEDIUM_SIZE = 32;
	private static final int LARGE_SIZE = 64;

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
	private String autocomplete = "";
	private String type = "text";
	private String role = "";
	private String value;
	private List<String> list = Collections.EMPTY_LIST;
	private String rawList = null;
	private String registeredList = "";
	private int order = 0;
	private int width = 12;
	private boolean last = false;
	private boolean first = false;

	private static final String TYPE_EMAIL = "email";

	private static final String TYPE_NUMBER = "number";

	public static final String STATIC_TEXT = "static-text";

	public static final String STATIC_TITLE = "static-title";

	public static final String TYPE_VAT = "vat";

	protected static List<? extends Object> FIELD_TYPES = Arrays.asList(new String[] { "text", "large-text", "yes-no", "true-false", TYPE_EMAIL, TYPE_NUMBER, "radio", "list", "list-multi", "registered-list", "file", "validation", STATIC_TEXT, STATIC_TITLE, TYPE_VAT, "hidden" });

	public static String ROLE_COUNT_PART = "count-participants";

	protected static List<? extends Object> FIELD_ROLES = Arrays.asList(new String[] { "", ROLE_COUNT_PART, "user_firstName", "user_lastName", "user_email", "user_gender", "user_birthdate", "user_organization", "user_vat", "user_address", "user_postCode", "user_city", "user_country", "user_phone", "user_mobile", "user_function" });

	public Field(ContentContext ctx, String name, String label, String type, String role, String condition, String value, String list, String registeredList, int order, int width, String autocomplete) {
		this.name = name;
		this.label = label;
		this.condition = condition;
		this.type = type;
		this.value = value;
		this.rawList = StringHelper.collectionToTextarea(StringHelper.stringToCollection(list));
		this.registeredList = registeredList;
		this.order = order;
		this.role = role;
		this.autocomplete = autocomplete;
		this.setWidth(width);
		if (ctx != null && list.startsWith(">")) {
			String listName = list.substring(1);
			try {
				List<IListItem> items = ListService.getInstance(ctx).getList(ctx, listName);
				if (items != null) {
					this.list = new LinkedList<>();
					for (IListItem i : items) {
						this.list.add(i.getValue());

					}
				} else {
					logger.warning("list not found : " + listName);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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

	public String getSqlType() {
		if (TYPE_NUMBER.equals(getType())) {
			return "integer";
		} else {
			return "text";
		}
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
		return getLabel() + SEP + getType() + SEP + getValue() + SEP + StringHelper.replaceCR(rawList, StringHelper.DEFAULT_LIST_SEPARATOR) + SEP + getOrder() + SEP + getRegisteredList() + SEP + getOrder() + SEP + getWidth() + SEP + getCondition() + SEP + getRole() + SEP + getAutocomplete();
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
		if (list == Collections.EMPTY_LIST) {
			list = StringHelper.textToList(rawList);
		}
		return list;
	}

	public void setRawList(String rawList) {
		this.rawList = rawList;
	}

	public String getRawList() {
		return rawList;
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

	public List<? extends Object> getFieldRoles() {
		return FIELD_ROLES;
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
	 * is the last element of cols sequence. That mean width with next field is
	 * greater than 12.
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

	private static List<String> OPERATOR = Arrays.asList(new String[] { "=", ">", "<", " in " });

	public String getConditionField() {
		if (condition == null) {
			return null;
		} else {
			for (String op : OPERATOR) {
				if (condition.contains(op)) {
					return condition.substring(0, condition.indexOf(op));
				}
			}
		}
		return null;
	}

	public String getConditionTest() {
		if (condition == null) {
			return null;
		} else {
			for (String op : OPERATOR) {
				if (condition.contains(op)) {
					return condition.substring(condition.indexOf(op) + 1);
				}
			}
		}
		return null;
	}

	public String getConditionOperator() {
		if (condition.contains("=")) {
			return "==";
		}
		if (condition.contains(">")) {
			return ">";
		}
		if (condition.contains("<")) {
			return "<";
		}
		return "-- NO OPERATOR --";
	}

	public boolean isFilledWidth(String value, boolean firstIsSelection) {
		if (StringHelper.isEmpty(value)) {
			return false;
		} else if (firstIsSelection) {
			return true;
		}
		List<String> list = getList();
		if (list.size() == 0) {
			return getName().length() > 0 && !StringHelper.isEmpty(value);
		} else {
			String item = list.iterator().next();
			if (item.startsWith(".") && StringHelper.isEmpty(value)) {
				return false;
			} else {
				return value != null && !item.equals(value);
			}
		}
	}

	public boolean isValueValid(String value) {
		if (getType().equals(TYPE_EMAIL)) {
			return StringHelper.isMail(value);
		} else if (getType().equals(TYPE_NUMBER)) {
			return StringHelper.isDigit(value);
		} else {
			return true;
		}
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getLabelSize() {
		if (getLabel() == null) {
			return "e";
		} else {
			int size = getLabel().length();
			if (size > SMALL_SIZE) {
				if (size > MEDIUM_SIZE) {
					if (size > LARGE_SIZE) {
						return "l";
					} else {
						return "m";
					}
				} else {
					return "s";
				}
			} else {
				return "xs";
			}
		}
	}

	public String getListLabelSize() {
		if (getList() == null || getList().size() == 0) {
			return "e";
		} else {
			int size = 0;
			for (String label : getList()) {
				if (label.length() > size) {
					size = label.length();
				}
			}
			if (size > SMALL_SIZE) {
				if (size > MEDIUM_SIZE) {
					if (size > LARGE_SIZE) {
						return "l";
					} else {
						return "m";
					}
				} else {
					return "s";
				}
			} else {
				return "xs";
			}
		}
	}

	public String getAutocomplete() {
		return autocomplete;
	}

	public void setAutocomplete(String autocomplete) {
		this.autocomplete = autocomplete;
	}

	public boolean isNeedList() {
		return getType().startsWith("list") || getType().equals("radio");
	}

}