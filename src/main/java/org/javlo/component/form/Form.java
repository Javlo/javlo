/*
 * Created on 06-janv.-2004
 */
package org.javlo.component.form;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.message.GenericMessage;


/**
 * @author pvandermaesen
 */
public abstract class Form {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(Form.class.getName());

	static final String FORM_SESSION_KEY = "--form--";

	Map messages = new HashMap();

	Map values = new HashMap();

	boolean saved = false;

	/**
	 * define a pattern for all field. the first col is field name, the second
	 * is the pattern.
	 */
	public abstract String[][] getFieldPattern();

	/**
	 * get the JSP for render the form
	 * 
	 * @return
	 */
	public String getJSP() {
		return "form.jsp";
	}

	public boolean isValid() {
		return isValid(false);
	}

	/**
	 * validate the form
	 * 
	 * @return true if all fields is valid, false else.
	 */
	public boolean isValid(boolean isLogged) {
		boolean res = true;
		messages.clear();
		for (int i = 0; i < getFieldPattern().length; i++) {
			String value = getValue(getFieldPattern()[i][0]);
			if (value != null) {

				boolean isPreValid = false;
				if (isLogged && getFieldPattern()[i][0].equals("password") && value.trim().length() == 0) {
					isPreValid = true;
				}
				if (isLogged && getFieldPattern()[i][0].equals("password2") && value.trim().length() == 0) {
					isPreValid = true;
				}
				
				if (!isPreValid) {
					if (!Pattern.matches(getFieldPattern()[i][1], value)) {
						if (getFieldPattern()[i][2] == null) { // this field
																// can not
							// be bad
							res = true;
							GenericMessage msg = new GenericMessage("form.ok", GenericMessage.INFO);
							messages.put(getFieldPattern()[i][0], msg);
						} else {
							res = false;
							GenericMessage msg = new GenericMessage(getFieldPattern()[i][2], GenericMessage.ERROR);
							messages.put(getFieldPattern()[i][0], msg);
						}
					} else {
						GenericMessage msg = new GenericMessage("form.ok", GenericMessage.INFO);
						messages.put(getFieldPattern()[i][0], msg);
					}
				}
			} else {
				if (getFieldPattern()[i][2] == null) { // this field can not be
					// bad
					GenericMessage msg = new GenericMessage("form.ok", GenericMessage.INFO);
					messages.put(getFieldPattern()[i][0], msg);
				} else {
					logger.warning("value not found for field : " + getFieldPattern()[i][0]);
					res = false;
				}
			}
		}
		return res;
	}

	public GenericMessage getMessage(String fieldName) {
		GenericMessage res = (GenericMessage) messages.get(fieldName);
		return res;
	}

	/**
	 * return the error message for a specific field
	 * 
	 * @param fieldName
	 *            the name of the field
	 * @return a text represant a error message
	 */
	public String getErrorMessage(String fieldName) {
		GenericMessage msg = getMessage(fieldName);
		String res = "";
		if (msg != null) {
			if (msg.getType() == GenericMessage.ERROR) {
				res = msg.getMessage();
			}
		}
		return res;
	}

	/**
	 * return the help message for a specific field
	 * 
	 * @param fieldName
	 *            the name of the field
	 * @return a text represant a help message
	 */
	public String getHelpMessage(String fieldName) {
		String res = "";
		GenericMessage msg = getMessage(fieldName);
		if (msg.getType() == GenericMessage.HELP) {
			res = msg.getMessage();
		}
		return res;
	}

	public void setHelpMessage(String fieldName, String key) {
		GenericMessage msg = new GenericMessage(key, GenericMessage.HELP);
		messages.put(fieldName, msg);
	}

	/**
	 * return fields name
	 * 
	 * @return a array of field name
	 */
	public String[] getFields() {
		String[] res = new String[getFieldPattern().length];
		for (int i = 0; i < res.length; i++) {
			res[i] = getFieldPattern()[i][0];
		}
		return res;
	}

	public static Form getSpecialSessionForm(HttpSession session, Object formKey) {
		return (Form) session.getAttribute("" + formKey.hashCode());
	}

	public static Form getCurrentSessionForm(HttpSession session) {
		return (Form) session.getAttribute(FORM_SESSION_KEY);
	}

	public void setValue(String fieldName, String fieldValue) {
		values.put(fieldName, fieldValue);
	}

	public String getValue(String fieldName) {
		return (String) values.get(fieldName);
	}

	public void setValues(Map map) {
		Iterator keys = map.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			String value = (String) map.get(key);
			setValue(key, value);
		}
	}

	public void setValues(HttpServletRequest request) {
		String[] fields = getFields();
		for (int i = 0; i < fields.length; i++) {
			setValue(fields[i], request.getParameter(fields[i]));
		}
	}

	public void clear() {
		values.clear();
	}

	public boolean isSaved() {
		return saved;
	}

	public void setSaved(boolean saved) {
		this.saved = saved;
	}

}