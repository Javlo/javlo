package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.javlo.context.ContentContext;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;

public class FieldEmail extends Field {

	@Override
	public boolean validate() {
		if (getValue() != null) {
			if (getValue().trim().length() == 0) {
				if (isNeeded()) {
					setMessage(i18nAccess.getText("global.message.error.needed"));
					setMessageType(Field.MESSAGE_ERROR);
					return false;
				} else {
					return true;
				}
			}
			if (!PatternHelper.MAIL_PATTERN.matcher(getValue()).matches()) {
				setMessage(i18nAccess.getText("component.error.mail"));
				setMessageType(Field.MESSAGE_ERROR);
				return false;
			}
		} else if (isNeeded()) {
			setMessage(i18nAccess.getText("global.message.error.needed"));
			setMessageType(Field.MESSAGE_ERROR);
			return false;
		}
		return super.validate();
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) {		
		try {
			String refCode = referenceViewCode(ctx);
			if (refCode != null) {
				return refCode;
			}
		} catch (Exception e) {		
			e.printStackTrace();
		}
		
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String displayStr = StringHelper.neverNull(getValue());
		if (displayStr.trim().length() == 0 || !isViewDisplayed()) {
			return "";
		}

		if (isWrapped()) {
			out.println("<p class=\"" + getType() + " " + getName() + "\">");
		}
		out.println(XHTMLHelper.textToXHTML(StringHelper.neverNull(getValue())));
		if (isWrapped()) {
			out.println("</p>");
		}

		out.close();
		return writer.toString();
	}

	public String getType() {
		return "email";
	}

}
