package org.javlo.fields;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

import java.text.ParseException;

public class FieldTime extends Field {

	@Override
	public boolean validate() {
		if (getValue() == null || getValue().trim().length() == 0) {
			return true;
		}
		try {
			StringHelper.parseTimeOnly(getValue());
		} catch (ParseException e) {
			setMessage(e.getMessage());
			setMessageType(Field.MESSAGE_ERROR);
			return false;
		}
		return super.validate();
	}
	
	@Override
	public String getType() {
		return "time";
	}

	@Override
	public String getEditXHTMLCode(ContentContext ctx, boolean search) throws Exception {
		return super.getEditXHTMLCode(ctx, search);
	}

	public String getHtmlInputType() {
		return "time";
	}

}
