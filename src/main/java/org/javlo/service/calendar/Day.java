package org.javlo.service.calendar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.javlo.helper.StringHelper;

public class Day {
	
	private int weekDay;
	private int monthDay;
	private String lg;
	private boolean active = true;
	private Calendar cal = Calendar.getInstance();
	
	public Day(int weekDay, int monthDay, String lg) {
		this.weekDay = weekDay%7;
		this.monthDay = monthDay;
		this.lg = lg;
	}
	
	public Day(Date date, String lg) {
		Calendar cal = Calendar.getInstance();		
		cal.setTime(date);
		this.weekDay = cal.get(Calendar.DAY_OF_WEEK);
		this.monthDay = cal.get(Calendar.DAY_OF_MONTH);
		this.lg = lg;		
	}
	
	public Day(Calendar cal, String lg) {		
		this.cal.setTime(cal.getTime());
		this.weekDay = cal.get(Calendar.DAY_OF_WEEK);
		this.monthDay = cal.get(Calendar.DAY_OF_MONTH);
		this.lg = lg;		
	}
	
	public int getHumanPosition() {
		if (weekDay == 1) {
			return 7;
		} else {
			return weekDay-1;
		}		 
	}
	
	public int getMonthDay() {
		return monthDay;
	}
	
	public int getWeekDay() {
		return weekDay;
	}
	
	public String getExtraShortLabel() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, weekDay);
		SimpleDateFormat format = new SimpleDateFormat("E", new Locale(lg));
		return format.format(cal.getTime());
	}
	
	public String getShortLabel() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, weekDay);
		SimpleDateFormat format = new SimpleDateFormat("EE", new Locale(lg));
		return format.format(cal.getTime());
	}
	
	public String getLargeLabel() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, weekDay);
		SimpleDateFormat format = new SimpleDateFormat("EEEE", new Locale(lg));
		return format.format(cal.getTime());
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public String getSortableDate() {
		if (cal==null) {
			return "?";
		}
		return StringHelper.renderSortableDate(cal.getTime());
	}
	
	public boolean isToDay() {
		Calendar today = Calendar.getInstance();
		if (cal == null) {
			return false;
		}  else {
			return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
		}
	}
	
	public static void main(String[] args) {
		System.out.println(new SimpleDateFormat("E").format(new Date()));
		System.out.println(DateFormatUtils.format(Calendar.getInstance(), "E"));
	}

}
