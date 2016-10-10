/*
 * Created on 06-janv.-2004
 */
package org.javlo.component.form;

import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;

/**
 * @author pvandermaesen
 */
public class FormMailing extends Form {

	/** 
	 * name, pattern, error key if pattern not match
	 */
	String[][] fieldsPattern = { {
			"email", "^[a-zA-Z][\\w\\.-]*@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$", "form.error.email" }, {
			"firstName", ".*", "form.error.firstName" }, {
			"lastName", ".*", "form.error.lastName" }
	};

	public static FormMailing getFormRegister(HttpSession session) {
		Form form = (Form) session.getAttribute(FORM_SESSION_KEY);

		if ((form == null) || (!(form instanceof FormMailing))) {
			form = new FormMailing();
			session.setAttribute(FORM_SESSION_KEY, form);
		}

		return (FormMailing) form;
	}

	/**
	 * @see org.javlo.component.form.Form#getJSP()
	 */
	public String getJSP() {
		return "mailing.jsp";
	}
	/**
	 * @see org.javlo.component.form.Form#getFieldPattern()
	 */
	public String[][] getFieldPattern() {
		return fieldsPattern;
	}
	


}
