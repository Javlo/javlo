package org.javlo.utils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;

public class TimeRange implements Serializable {

	private Calendar startDateCal = Calendar.getInstance();

	private Calendar endDateCal = Calendar.getInstance();

	public TimeRange(Date startDate, Date endDate) {
		this.startDateCal.setTime(startDate);
		if (endDate != null) {
			this.endDateCal.setTime(endDate);
		} else {
			this.endDateCal.setTime(startDate);
		}
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
		if (startDateCal.equals(endDateCal)) {
			if (TimeHelper.isAfterOrEqualForDay(startDateCal.getTime(), date)) {
				return true;
			} else {
				return false;
			}
		} else {

			if (cal.equals(startDateCal) || cal.equals(endDateCal)) {
				return true;
			} else {
				return cal.after(startDateCal) && cal.before(endDateCal);
			}
		}
	}

	public boolean isAfter(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.after(endDateCal);
	}

	public boolean isNull() {
		return startDateCal == null && endDateCal == null;
	}

	@Override
	public String toString() {
		return StringHelper.renderTime(getStartDate()) + " > " + StringHelper.renderTime(getEndDate());
	}

}
