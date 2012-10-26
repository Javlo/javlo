package org.javlo.helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;

public class TimeHelper {

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TimeHelper.class.getName());

	/**
	 * set the date at the first day of the week.
	 * 
	 * @param date
	 * @return
	 */
	public static Date toStartWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, 3);
		return cal.getTime();
	}

	/**
	 * check if date is after other date, if the day is the same it is ok.
	 */
	public static boolean isAfterOrEqualForDay(Date date, Date ref) {
		if (ref == null) {
			return true;
		}
		Calendar calDate = Calendar.getInstance();
		calDate.setTime(date);
		Calendar calRef = Calendar.getInstance();
		calRef.setTime(ref);
		if (calDate.get(Calendar.YEAR) < calRef.get(Calendar.YEAR)) {
			return false;
		} else if (calDate.get(Calendar.YEAR) == calRef.get(Calendar.YEAR)) {
			if (calDate.get(Calendar.DAY_OF_YEAR) < calRef.get(Calendar.DAY_OF_YEAR)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * check if date is after other date, if the day is the same it is ok.
	 */
	public static boolean isBeforeOrEqualForDay(Date date, Date ref) {
		if (ref == null) {
			return true;
		}
		Calendar calDate = Calendar.getInstance();
		calDate.setTime(date);
		Calendar calRef = Calendar.getInstance();
		calRef.setTime(ref);
		if (calDate.get(Calendar.YEAR) > calRef.get(Calendar.YEAR)) {
			return false;
		} else if (calDate.get(Calendar.YEAR) == calRef.get(Calendar.YEAR)) {
			if (calDate.get(Calendar.DAY_OF_YEAR) > calRef.get(Calendar.DAY_OF_YEAR)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * check if date is after other date, if the day is the same it is ok.
	 */
	public static boolean isEqualForDay(Date date, Date ref) {
		Calendar calDate = Calendar.getInstance();
		calDate.setTime(date);
		Calendar calRef = Calendar.getInstance();
		calRef.setTime(ref);
		if (calDate.get(Calendar.YEAR) != calRef.get(Calendar.YEAR)) {
			return false;
		} else if (calDate.get(Calendar.DAY_OF_YEAR) != calRef.get(Calendar.DAY_OF_YEAR)) {
			return false;
		}
		return true;
	}

	/**
	 * check id a date in between two other date, but date in range.
	 * 
	 * @param date
	 * @param start
	 * @param end
	 * @return
	 */
	public static boolean betweenInDay(Date date, Date start, Date end) {
		return isAfterOrEqualForDay(date, start) && isBeforeOrEqualForDay(date, end);
	}

	public static Calendar convertRemoveAfterMinutes(Calendar cal) {
		Calendar outCal = Calendar.getInstance();
		outCal.setTimeInMillis(0);
		outCal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		outCal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		outCal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
		outCal.set(Calendar.HOUR, cal.get(Calendar.HOUR));
		outCal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
		return outCal;
	}

	public static Calendar convertRemoveAfterHour(Calendar cal) {
		Calendar outCal = Calendar.getInstance();
		outCal.setTimeInMillis(0);
		outCal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		outCal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		outCal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
		outCal.set(Calendar.HOUR, cal.get(Calendar.HOUR));
		return outCal;
	}

	public static Calendar convertRemoveAfterDay(Calendar cal) {
		Calendar outCal = Calendar.getInstance();
		outCal.setTimeInMillis(0);
		outCal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		outCal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		outCal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
		return outCal;
	}

	/**
	 * return the date format defined in the system, depend of rendering mode, globalContext config or default java config.
	 * 
	 * @param ctx
	 *            current content
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static DateFormat getDefaultDateFormat(ContentContext ctx) throws FileNotFoundException, IOException {

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		String dateFormatString = null;
		Locale locale;
		if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {
			dateFormatString = i18nAccess.getText("date.full", (String) null);
			locale = new Locale(globalContext.getEditLanguage(ctx.getRequest().getSession()));
		} else {
			dateFormatString = i18nAccess.getContentViewText("date.full", (String) null);
			locale = new Locale(ctx.getRequestContentLanguage());
		}

		DateFormat dateFormat;
		if (dateFormatString != null) {
			try {
				dateFormat = new SimpleDateFormat(dateFormatString, locale);
				return dateFormat;
			} catch (Throwable t) {
				logger.warning("error with date format : " + dateFormatString);
				logger.warning(t.getMessage());
			}
		}

		String manualDateFormat = globalContext.getFullDateFormat();
		if (manualDateFormat == null || manualDateFormat.trim().length() == 0) {
			if (locale.getLanguage().equals("el")) { // in Greek the full
				// rendering of date is
				// not correct.
				dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
			} else {
				dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
			}
		} else {
			dateFormat = new SimpleDateFormat(manualDateFormat);
		}
		return dateFormat;
	}

	public static void main(String[] args) {
		Date date = new Date();
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Date date1 = new Date();

		Calendar cal = Calendar.getInstance();
		cal.roll(Calendar.YEAR, false);

		System.out.println("** remove after minute : " + convertRemoveAfterMinutes(Calendar.getInstance()).getTime());
		System.out.println("** remove after hour : " + convertRemoveAfterHour(Calendar.getInstance()).getTime());

		System.out.println("** date     : " + StringHelper.renderTime(date));
		System.out.println("** date 1   : " + StringHelper.renderTime(date1));
		System.out.println("** cal      : " + StringHelper.renderTime(cal.getTime()));

		System.out.println("** after    : " + isAfterOrEqualForDay(date, date1));
		System.out.println("** before   : " + isBeforeOrEqualForDay(date, date1));
		System.out.println("** after  c : " + isAfterOrEqualForDay(date, cal.getTime()));
		System.out.println("** before c : " + isBeforeOrEqualForDay(date, cal.getTime()));
		System.out.println("** equal 1 : " + isEqualForDay(date, date1));
		System.out.println("** equal c : " + isEqualForDay(date, cal.getTime()));

		System.out.println("** betweenInDay c - d : " + betweenInDay(date, cal.getTime(), date1));
		System.out.println("** betweenInDay d - c : " + betweenInDay(date, date1, cal.getTime()));

	}
}
