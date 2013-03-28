package org.javlo.helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
		outCal.set(Calendar.HOUR, 0);
		return outCal;
	}

	public static Calendar convertRemoveAfterMonth(Calendar cal) {
		Calendar outCal = Calendar.getInstance();
		outCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0);
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

	public static String exportAgenda(ContentContext ctx, MenuElement agendaPage, Date startDate, Date endDate) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		MenuElement[] children = agendaPage.getAllChildren();
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<agenda lang=\"" + ctx.getRequestContentLanguage() + "\" start-date=\"" + StringHelper.renderSortableDate(startDate) + "\" end-date=\"" + StringHelper.renderSortableDate(endDate) + "\">");
		for (MenuElement element : children) {
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
							out.print("<title type=\"" + contentVisualComponent.getStyle(ctx) + "\">");
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

	public static void main(String[] args) {
		Calendar outCal = Calendar.getInstance();
		outCal.set(Calendar.DAY_OF_MONTH, 1);
		System.out.println("***** TimeHelper.main : " + StringHelper.renderDate(outCal.getTime())); // TODO: remove debug trace

	}
}
