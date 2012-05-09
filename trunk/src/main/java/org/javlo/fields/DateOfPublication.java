package org.javlo.fields;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class DateOfPublication extends MetaField {

	private static Logger logger = Logger.getLogger(DateOfPublication.class.getName());

	@Override
	public String getType() {
		return "start-date";
	}

	@Override
	public boolean isPublished(ContentContext ctx) {
		if (getValue() == null || getValue().trim().length() == 0) {
			return true;
		}
		Date date = new Date();
		try {
			date = StringHelper.parseDateOrTime(getValue());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (date == null) {
			logger.warning("bad date format : " + getValue());
			setValue("");
			return true;
		}
		Calendar currentTime = Calendar.getInstance();
		Calendar publishDate = Calendar.getInstance();
		publishDate.setTime(date);
		return publishDate.before(currentTime);
	}

}
