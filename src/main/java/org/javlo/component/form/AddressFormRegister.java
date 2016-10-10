/*
 * Created on 06-janv.-2004
 */
package org.javlo.component.form;

import javax.servlet.http.HttpSession;

import org.javlo.helper.PatternHelper;


/**
 * @author pvandermaesen
 */
public class AddressFormRegister extends Form {

	/** 
	 * name, pattern, error key if pattern not match
	 */
	String[][] fieldsPattern = { { "id", "", "" },
			{ "login", "[\\w\\-_@\\.]+", "form.error.user" }, {
			"password", "[\\w\\-_@\\.]+", "form.error.password" }, {
			"password2", "[\\w\\-_@\\.]+", "form.error.password" }, {
			"email", PatternHelper.MAIL_PATTERN.pattern(), "form.error.email" }, {
			"firstName", "..*", "form.error.firstName" }, {
			"lastName", "..*", "form.error.lastName" }, {
			"address", "..*", "form.error.address" }, {
			"postcode", "..*", "form.error.postcode" }, {
			"city", "..*", "form.error.city" }
	};

	public static AddressFormRegister getFormRegister(HttpSession session) {
		Form form = (Form) session.getAttribute(FORM_SESSION_KEY);

		if ((form == null) || (!(form instanceof AddressFormRegister))) {
			form = new AddressFormRegister();
			session.setAttribute(FORM_SESSION_KEY, form);
		}

		return (AddressFormRegister) form;
	}

	/**
	 * @see org.javlo.component.form.Form#getJSP()
	 */
	public String getJSP() {
		return "register_address.jsp";
	}
	/**
	 * @see org.javlo.component.form.Form#getFieldPattern()
	 */
	public String[][] getFieldPattern() {
		return fieldsPattern;
	}

	public boolean isValid() {
		boolean res = super.isValid();
		if (res) {
			String pwd1 = getValue("password");
			if (pwd1 != null) {
				if (!pwd1.equals(getValue("password2"))) {
					res = false;
					messages.put("password2", "form.error.password2");
				}
			}
		}
		return res;
	}
	
	
}
