package org.javlo.utils;

import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeRange implements Serializable {

	private Calendar startDateCal = null;

	private Calendar endDateCal = null;

	public TimeRange(Date startDate, Date endDate) {
		if (startDate != null) {
			startDateCal = Calendar.getInstance();
			startDateCal.setTime(startDate);
		}
		if (endDate != null) {
			endDateCal = Calendar.getInstance();
			endDateCal.setTime(endDate);
		}
	}

	public Date getEndDate() {
		if (endDateCal == null) {
			return null;
		}
		return endDateCal.getTime();
	}

	public Date getStartDate() {
		if (startDateCal == null) {
			return null;
		}
		return startDateCal.getTime();
	}

	public LocalDate getStartLocalDate() {
		TimeZone tz = startDateCal.getTimeZone();
		ZoneId zid = tz == null ? ZoneId.systemDefault() : tz.toZoneId();
		return LocalDateTime.ofInstant(startDateCal.toInstant(), zid).toLocalDate();
	}

	public LocalDate getEndLocalDate() {
		TimeZone tz = endDateCal.getTimeZone();
		ZoneId zid = tz == null ? ZoneId.systemDefault() : tz.toZoneId();
		return LocalDateTime.ofInstant(endDateCal.toInstant(), zid).toLocalDate();
	}

	public void setEndData(Date endDate) {
		if (endDateCal == null) {
			endDateCal = Calendar.getInstance();
		}
		this.endDateCal.setTime(endDate);
	}

	public void setStartDate(Date startDate) {
		if (startDateCal == null) {
			startDateCal = Calendar.getInstance();
		}
		this.startDateCal.setTime(startDate);
	}

	public boolean isBefore(Date date) {
		Calendar refCal = startDateCal;
		if (refCal == null) {
			refCal = endDateCal;
			if (refCal == null) {
				return true;
			}
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.before(refCal);
	}

	public boolean isInside(LocalDate date) {
		return isInside(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
	}

	public boolean isInside(Date date) {
		if (startDateCal == null && endDateCal == null) {
			return true;
		}
		if (startDateCal == null || endDateCal == null) {
			return false;
		}
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
		Calendar refCal = endDateCal;
		if (refCal == null) {
			refCal = startDateCal;
			if (refCal == null) {
				return true;
			}
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.after(refCal);
	}

	public boolean isNull() {
		return startDateCal == null && endDateCal == null;
	}

	@Override
	public String toString() {
		return StringHelper.renderTime(getStartDate()) + " > " + StringHelper.renderTime(getEndDate());
	}

}
