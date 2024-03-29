/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.form;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.LocalLogger;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;

/**
 * @author pvandermaesen
 */
public abstract class FormComponent extends AbstractVisualComponent implements IAction {

	public static String performSubmit(HttpServletRequest request, HttpServletResponse response) {

		String message = null;
		try {
			Form form = Form.getCurrentSessionForm(request.getSession());
			form.setValues(request);
			if (!form.isValid()) {
				message = "form.unvalid";
			}
		} catch (RuntimeException e) {
			LocalLogger.log(e);
		}

		return message;
	}

	public boolean formIsSaved(ContentContext ctx) {
		return getForm(ctx).isSaved();
	}

	public boolean formIsValid(ContentContext ctx) {
		return getForm(ctx).isValid();
	}

	/**
	 * @see org.javlo.actions.IAction#getActionGroupName()
	 */
	@Override
	public String getActionGroupName() {
		return "form";
	}

	/**
	 * @see org.javlo.component.AbstractVisualComponent#getJSPPath()
	 */
	@Override
	protected String getComponentPath() {
		return "form";
	}

	/**
	 * @see org.javlo.component.AbstractVisualComponent#getEditXHTMLCode()
	 */
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		return "not editable component.";
	}

	public String getErrorMessage(ContentContext ctx, String fieldName) throws ResourceNotFoundException {
		String msg = getForm(ctx).getErrorMessage(fieldName);
		if ((msg == null) || (msg.trim().length() == 0)) {
			msg = "&nbsp;";
		} else {
			msg = getViewText(ctx, msg);
		}
		return msg;
	}

	abstract protected Form getForm(ContentContext ctx);

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getViewText(java.lang.String)
	 */
	public String getFormViewText(ContentContext ctx, String key) throws ResourceNotFoundException {
		I18nAccess i18n = null;
		String res = "ERROR I18N ACCESS.";
		try {
			i18n = I18nAccess.getInstance(ctx.getRequest());
			res = i18n.getContentViewText(key.trim());
		} catch (Exception e) {
			LocalLogger.log(e);
		}
		return res;
	}

	public String getHelpMessage(ContentContext ctx, String fieldName) throws ResourceNotFoundException {
		String msg = getForm(ctx).getHelpMessage(fieldName);
		if ((msg == null) || (msg.trim().length() == 0)) {
			msg = "&nbsp;";
		} else {
			msg = getViewText(ctx, msg);
		}
		return msg;
	}

	public GenericMessage getI18nMessage(ContentContext ctx, String fieldName) throws ResourceNotFoundException {
		GenericMessage msg = getMessage(ctx, fieldName);
		if (msg != null) {
			msg = new GenericMessage(getViewText(ctx, msg.getMessage()), msg.getType());
		}
		return msg;
	}

	public GenericMessage getMessage(ContentContext ctx, String fieldName) {
		GenericMessage msg = getForm(ctx).getMessage(fieldName);
		if (msg == null) {
			return null;
		} else {
			String i18nMsg;
			try {
				i18nMsg = getViewText(ctx, msg.getMessage());
			} catch (ResourceNotFoundException e) {
				i18nMsg = "[key: " + msg.getMessage() + "] fatal error : " + e.getMessage();
			}
			return new GenericMessage(i18nMsg, msg.getType());
		}
	}

	public String getSpecialTag() {
		// TODO replace "form.submit" by getActionGroupName() + ".submit"
		String out = "<input type=\"hidden\" name=\"webaction\" value=\"" + getActionGroupName() + ".submit\"/>";
		if (getValue().trim().length() > 0) {
			out = out + "<input type=\"hidden\" name=\"component_id\" value=\"" + getId() + "\"/>";
			;
		}
		return out;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getType()
	 */
	@Override
	public String getType() {
		return "form";
	}

	/**
	 * return the value of a field, defaultValue is returned if the field is not found
	 * 
	 * @param fieldName
	 *            the name of the field,
	 * @param defaultValue
	 *            the default value if the field does'nt exist.
	 * @return a field value or defaultValue
	 */
	public String getValue(ContentContext ctx, String fieldName, String defaultValue) {
		String res = getForm(ctx).getValue(fieldName);
		if (res == null) {
			res = defaultValue;
		}
		return res;
	}

	/***********
	 * ACTIONS *
	 ***********/

	/**
	 * @see org.javlo.component.AbstractVisualComponent#getViewXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		// Form form = FormMailing.getFormRegister(ctx.getRequest().getSession());
		includeComponentJSP(ctx, getForm(ctx).getJSP());
		return "";
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		return 0;
	}

	@Override
	public boolean isUnique() {
		return true;
	}

}
