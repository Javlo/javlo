package org.javlo.utils;

import org.javlo.component.meta.ITimeRange;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TimeRange implements Serializable {

	private LocalDateTime startDateCal = null;

	private LocalDateTime endDateCal = null;

	public TimeRange(ContentContext ctx, ITimeRange tr) {
		startDateCal = tr.getTimeRangeStart(ctx);
		endDateCal = tr.getTimeRangeEnd(ctx);
	}

	private static LocalDateTime convert(Date date) {
		return date.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
	}

	private static LocalDate convertInDate(Date date) {
		return date.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
	}

	public TimeRange(Date startDate, Date endDate) {
		if (startDate != null) {
			startDateCal = convert(startDate);
		}
		if (endDate != null) {
			endDateCal = convert(endDate);
		}
	}

	public Date getStartDate() {
		return java.util.Date.from(startDateCal
				.atZone(ZoneId.systemDefault())
				.toInstant());
	}

	public Date getEndDate() {
		return java.util.Date.from(endDateCal
				.atZone(ZoneId.systemDefault())
				.toInstant());
	}

	public LocalDate getStartLocalDate() {
		return startDateCal.toLocalDate();
	}

	public LocalDate getEndLocalDate() {
		return endDateCal.toLocalDate();
	}

	public void setEndData(Date endDate) {
		endDateCal = convert(endDate);
	}

	public void setStartDate(Date startDate) {
		startDateCal = convert(startDate);
	}

	public boolean isBefore(Date date) {
		return endDateCal.isBefore(convert(date));
	}

	public boolean isInside(LocalDate date) {
		LocalDate lds = startDateCal.toLocalDate();
		LocalDate lde = endDateCal.toLocalDate();
		if (date.isBefore(lds)) {
			return false;
		}
		if (date.isAfter(lde)) {
			return false;
		}
		return true;
	}

	public boolean isInside(Date date) {
		return isInside(convertInDate(date));
	}

	public boolean isAfter(Date date) {
		return endDateCal.isAfter(convert(date));
	}

	public boolean isAfter(LocalDateTime date) {
		return endDateCal.isAfter(date);
	}

	public boolean isAfter(LocalDate date) {
		return endDateCal.isAfter(date.atTime(0,0));
	}

	public boolean isNull() {
		return startDateCal == null && endDateCal == null;
	}

	public String getRenderDates() {
		if (!getStartDate().equals(getEndDate())) {
			return StringHelper.renderDate(getStartDate()) + " - " + StringHelper.renderDate(getEndDate());
		} else {
			return StringHelper.renderDate(getStartDate());
		}
	}

	@Override
	public String toString() {
		return getStartDate() + " > " + getEndDate();
	}

	public static void main(String[] args) throws ParseException {
		TimeRange tr = new TimeRange(StringHelper.parseInputDate("2023-06-06"), StringHelper.parseInputDate("2023-06-12"));
		System.out.println(StringHelper.renderDate(tr.getStartDate()));
		System.out.println(StringHelper.renderDate(tr.getEndDate()));
	}

}
