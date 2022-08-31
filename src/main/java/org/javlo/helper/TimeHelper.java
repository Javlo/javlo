package org.javlo.helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.meta.LocationComponent;
import org.javlo.component.title.SubTitle;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;

public class TimeHelper {

	public static final Date NO_DATE = new Date(0);

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TimeHelper.class.getName());

	/**
	 * set the date at the first day of the week.o
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
	 * check if date is after other date.
	 */
	public static boolean isAfterForDay(Date date, Date ref) {
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
			if (calDate.get(Calendar.DAY_OF_YEAR) <= calRef.get(Calendar.DAY_OF_YEAR)) {
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
	public static boolean isBeforeForDay(Date date, Date ref) {
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
			if (calDate.get(Calendar.DAY_OF_YEAR) >= calRef.get(Calendar.DAY_OF_YEAR)) {
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

	public static Calendar getCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
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
		outCal.set(Calendar.HOUR, 0);
		outCal.set(Calendar.MINUTE, 0);
		outCal.set(Calendar.SECOND, 0);
		outCal.set(Calendar.MILLISECOND, 0);
		return outCal;
	}

	public static Calendar convertEndOfDay(Calendar cal) {
		Calendar outCal = Calendar.getInstance();
		outCal.setTimeInMillis(0);
		outCal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		outCal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		outCal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
		outCal.set(Calendar.HOUR, 23);
		outCal.set(Calendar.MINUTE, 59);
		outCal.set(Calendar.SECOND, 59);
		outCal.set(Calendar.MILLISECOND, 999);
		return outCal;
	}

	public static Calendar convertRemoveAfterMonth(Calendar cal) {
		Calendar outCal = Calendar.getInstance();
		outCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0);
		return outCal;
	}

	/**
	 * return the date format defined in the system, depend of rendering mode,
	 * globalContext config or default java config.
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
			locale = ctx.getLocale();
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

	public static String exportAgenda(ContentContext ctx, MenuElement agendaPage, Date startDate, Date endDate) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<agenda lang=\"" + ctx.getRequestContentLanguage() + "\" start-date=\"" + StringHelper.renderSortableDate(startDate) + "\" end-date=\"" + StringHelper.renderSortableDate(endDate) + "\">");
		for (MenuElement element : agendaPage.getAllChildrenList()) {
			Map<Date, List<IContentVisualComponent>> contentByDate = element.getContentByDate(ctx);
			Iterator<Date> dates = contentByDate.keySet().iterator();
			while (dates.hasNext()) {
				Date key = dates.next();
				if (betweenInDay(key, startDate, endDate)) {
					out.println("<event date=\"" + StringHelper.renderSortableDate(key) + "\" >");
					out.println("<url>" + URLHelper.createURL(ctx.getContextForAbsoluteURL(), element) + "</url>");
					List<IContentVisualComponent> contentForDate = contentByDate.get(key);
					StringBuffer content = new StringBuffer();
					for (IContentVisualComponent contentVisualComponent : contentForDate) {
						if (contentVisualComponent.getType().equals(LocationComponent.TYPE)) {
							out.print("<location>");
							out.print(contentVisualComponent.getValue(ctx));
							out.println("</location>");
						} else if (contentVisualComponent.getType().equals(SubTitle.TYPE)) {
							out.print("<title type=\"" + contentVisualComponent.getComponentCssClass(ctx) + "\">");
							out.print(contentVisualComponent.getValue(ctx));
							out.println("</title>");
						} else {
							content.append(contentVisualComponent.getXHTMLCode(ctx));
						}
					}
					out.println("<content><![CDATA[");
					out.println(content);
					out.println("]]></content>");
					out.println("</event>");
				}
			}
		}
		out.println("</agenda>");
		out.close();
		return writer.toString();
	}

	/**
	 * get the distance between 2 dates in day
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int getDaysDistance(Date date1, Date date2) {
		double reduce = 1000 * 60 * 60 * 24;
		long distMili = Math.abs(Math.round(Math.floor(date1.getTime() / reduce) - Math.floor(date2.getTime() / reduce)));
		return (int) Math.round(distMili);
	}

	public static void main(String[] args) {
		Date d1 = new Date();
		Date d2 = new Date();
		System.out.println(">>>>>>>>> TimeHelper.main : d1 <-> s2 = " + getDaysDistance(d1, d2)); // TODO: remove debug trace
	}

	/**
	 * get the distance between 2 dates in day
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static long getDaysDistance(LocalDate date1, LocalDate date2) {
		return ChronoUnit.DAYS.between(date1, date2);
	}

	public static int getAge(Date born) {
		int age = 0;
		try {
			Calendar now = Calendar.getInstance();
			Calendar dob = Calendar.getInstance();
			dob.setTime(born);
			if (dob.after(now)) {
				throw new IllegalArgumentException("Can't be born in the future");
			}
			int year1 = now.get(Calendar.YEAR);
			int year2 = dob.get(Calendar.YEAR);
			age = year1 - year2;
			int month1 = now.get(Calendar.MONTH);
			int month2 = dob.get(Calendar.MONTH);
			if (month2 > month1) {
				age--;
			} else if (month1 == month2) {
				int day1 = now.get(Calendar.DAY_OF_MONTH);
				int day2 = dob.get(Calendar.DAY_OF_MONTH);
				if (day2 > day1) {
					age--;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return age;
	}

	public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
		if (dateToConvert == null) {
			return null;
		}
		return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public static LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
		if (dateToConvert == null) {
			return null;
		}
		return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

}
