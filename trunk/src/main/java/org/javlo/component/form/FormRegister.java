/*
 * Created on 06-janv.-2004
 */
package org.javlo.component.form;

import javax.servlet.http.HttpSession;

/**
 * @author pvandermaesen
 */
public class FormRegister extends Form {

	/** 
	 * name, pattern, error key if pattern not match
	 */
	String[][] fieldsPattern = { { "login", "[\\w\\-_@\\.]+", "form.error.user" }, {
			"password", "[\\w\\-_@\\.]+", "form.error.password" }, {
			"password2", "[\\w\\-_@\\.]+", "form.error.password" }, {
			"email", "^[a-zA-Z][\\w\\.-]*@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$", "form.error.email" }, {
			"firstName", "..*", "form.error.firstName" }, {
			"lastName", "..*", "form.error.lastName" }
	};

	public static FormRegister getFormRegister(HttpSession session) {
		Form form = (Form) session.getAttribute(FORM_SESSION_KEY);

		if ((form == null) || (!(form instanceof FormRegister))) {
			form = new FormRegister();
			session.setAttribute(FORM_SESSION_KEY, form);
		}

		return (FormRegister) form;
	}

	/**
	 * @see org.javlo.component.form.Form#getJSP()
	 */
	public String getJSP() {
		return "register.jsp";
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
