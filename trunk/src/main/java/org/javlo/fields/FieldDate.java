package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.javlo.component.core.IDate;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;

public class FieldDate extends Field implements IDate {

	@Override
	public boolean validate() {
		if (getValue() == null || getValue().trim().length() == 0) {
			return true;
		}
		try {
			StringHelper.parseDate(getValue());
		} catch (ParseException e) {
			setMessage(e.getMessage());
			setMessageType(Field.MESSAGE_ERROR);
			return false;
		}
		return super.validate();
	}

	@Override
	public Date getDate(ContentContext ctx) {
		try {
			if (getValue() != null && getValue().trim().length() > 0) {
				return StringHelper.parseDate(getValue());
			}
		} catch (ParseException e) {
		}
		return null;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String displayStr = StringHelper.neverNull(getValue());
		if (displayStr.trim().length() == 0 || !isViewDisplayed()) {
			return "";
		}

		String format = getMetaData("format");

		if (format == null) {
			out.println(XHTMLHelper.textToXHTML(displayStr));
		} else {
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			try {
				out.println(dateFormat.format(StringHelper.parseDate(displayStr)));
			} catch (ParseException e) {
				e.printStackTrace();
				return e.getMessage();
			}
		}

		out.close();
		return writer.toString();
	}

	@Override
	public String getType() {
		return "date";
	}
	
	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		setValue(StringHelper.renderDate(new Date()));
		return true;
	}

}
