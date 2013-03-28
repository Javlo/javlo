package org.javlo.utils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class TimeRange implements Serializable {
	
	private Calendar startDateCal = Calendar.getInstance();

	private Calendar endDateCal = Calendar.getInstance();

	public TimeRange(Date startDate, Date endDate) {
		this.startDateCal.setTime(startDate);
		this.endDateCal.setTime(endDate);
	}

	public Date getEndDate() {
		return endDateCal.getTime();
	}

	public Date getStartDate() {
		return startDateCal.getTime();
	}

	public void setEndData(Date endDate) {
		this.endDateCal.setTime(endDate);
	}

	public void setStartDate(Date startDate) {
		this.startDateCal.setTime(startDate);
	}
	
	public boolean isBefore(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.before(startDateCal);
	}
	
	public boolean isInside(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.after(startDateCal) && cal.before(endDateCal);
	}
	
	public boolean isAfter(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.after(endDateCal);
	}
	
	public boolean isNull() {
		return startDateCal == null && endDateCal == null;
	}

}
