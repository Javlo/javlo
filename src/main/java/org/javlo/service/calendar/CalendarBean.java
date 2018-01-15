package org.javlo.service.calendar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.javlo.context.ContentContext;

public class CalendarBean {

	private ContentContext ctx;
	private static final String KEY = "calendar";
	private List<ICal> icals;
	private Map<Integer, List<ICal>> mapEvents = null;
	private int year;
	private int month;
	private Day[][] daysBloc = new Day[6][7];
	private ICalFilter filter = ICalFilter.NO_FILTER;

	private static int getDayOfWeekFromZeroAndMonday(Calendar cal) {
		int d = cal.get(Calendar.DAY_OF_WEEK);
		if (d >= 2) {
			return d - 2;
		} else {
			return 6;
		}
	}

	public static CalendarBean getInstance(ContentContext ctx) throws Exception {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		return getInstance(ctx, year, month);
	}

	public static CalendarBean getInstance(ContentContext ctx, String key) throws Exception {
		return getInstance(ctx, Integer.parseInt(key.split("-")[0]), Integer.parseInt(key.split("-")[1]), 0);
	}

	public static CalendarBean getInstance(ContentContext ctx, String key, int step) throws Exception {
		return getInstance(ctx, Integer.parseInt(key.split("-")[0]), Integer.parseInt(key.split("-")[1]), step);
	}

	public static CalendarBean getInstance(ContentContext ctx, int year, int month) throws Exception {
		return getInstance(ctx, year, month, 0);
	}

	public static CalendarBean getInstance(ContentContext ctx, int year, int month, int step) throws Exception {
		CalendarBean outBean = (CalendarBean) ctx.getRequest().getAttribute(KEY);
		if (outBean == null) {
			outBean = new CalendarBean();
			outBean.ctx = ctx;

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, month);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			if (step != 0) {
				cal.add(Calendar.MONTH, step);
			}

			outBean.year = cal.get(Calendar.YEAR);
			outBean.month = cal.get(Calendar.MONTH);
			outBean.icals = CalendarService.getInstance(ctx).loadICals(outBean.year, outBean.month);

			Calendar previous = Calendar.getInstance();
			Calendar next = Calendar.getInstance();
			next.setTime(cal.getTime());
			next.add(Calendar.MONTH, 1);
			int dayOfWeek = getDayOfWeekFromZeroAndMonday(cal);
			int lastDay = cal.getMaximum(Calendar.DAY_OF_MONTH) + dayOfWeek - 1;
			for (int d = 0; d < outBean.daysBloc.length * outBean.daysBloc[0].length; d++) {
				Day day;
				/** days before current month **/
				if (d < dayOfWeek) {
					previous.setTime(cal.getTime());
					previous.add(Calendar.DAY_OF_MONTH, -(dayOfWeek - d));
					day = new Day(previous, ctx.getContextLanguage());
					day.setActive(false);
					/** current month **/
				} else if (d > lastDay) {
					day = new Day(next, ctx.getContextLanguage());
					day.setActive(false);
					next.add(Calendar.DAY_OF_YEAR, 1);
					/** days afther current month **/
				} else {
					day = new Day(cal, ctx.getContextLanguage());
					day.setActive(true);
					cal.add(Calendar.DAY_OF_YEAR, 1);
				}
				if (cal.get(Calendar.MONTH) == outBean.month) {
					int x = d / outBean.daysBloc[0].length;
					int y = d % outBean.daysBloc[0].length;
					outBean.daysBloc[x][y] = day;
				}
			}
			ctx.getRequest().setAttribute(KEY, outBean);
		}
		return outBean;
	}

	public Map<Integer, List<ICal>> getMonthEvents() {
		if (mapEvents == null) {
			mapEvents = new HashMap<Integer, List<ICal>>();
			for (ICal ical : icals) {
				if (filter.accept(ical)) {
					List<ICal> list = mapEvents.get(ical.getDay());
					if (list == null) {
						list = new LinkedList<ICal>();
						mapEvents.put(ical.getDay(), list);
					}
					list.add(ical);
				}
			}
		}
		LinkedList<String> categories = new LinkedList<String>();
		for (List<ICal> icals : mapEvents.values()) {
			for (ICal ical : icals) {
				if (!categories.contains(ical.getCategories())) {
					categories.add(ical.getCategories());
				}
				ical.setColorGroup((categories.indexOf(ical.getCategories()) % 5) + 1);
			}
		}
		return mapEvents;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public String getKey() {
		return year + "-" + month;
	}

	public String getNextKey() {
		if (month >= 11) {
			return (year + 1) + "-0";
		} else {
			return year + "-" + (month + 1);
		}
	}

	public String getPreviousKey() {
		if (month <= 0) {
			return (year - 1) + "-11";
		} else {
			return year + "-" + (month - 1);
		}
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public String getLabel() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", new Locale(ctx.getContextLanguage()));
		return format.format(cal.getTime());
	}

	public List<Day> getDays() {
		List<Day> outDays = new LinkedList<Day>();
		for (int i = 2; i < 9; i++) {
			outDays.add(new Day(i, 0, ctx.getContextLanguage()));
		}
		return outDays;
	}

	public Day[][] getDaysBloc() {
		return daysBloc;
	}

	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		System.out.println("1> cal = " + cal.get(Calendar.DAY_OF_WEEK));
		cal.setFirstDayOfWeek(2);
		cal.setTime(new Date());
		System.out.println("2> cal = " + cal.get(Calendar.DAY_OF_WEEK));

	}

	public ICalFilter getFilter() {
		return filter;
	}

	public void setFilter(ICalFilter filter) {
		mapEvents = null;
		this.filter = filter;
	}

}
