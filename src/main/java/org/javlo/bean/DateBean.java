package org.javlo.bean;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class DateBean {

	private Date date = null;
	private WeakReference<ContentContext> ctx;

	public DateBean(ContentContext ctx, Date date) {
		this.date = date;
		this.ctx = new WeakReference<ContentContext>(ctx);
	}

	public Date getDate() {
		return date;
	}

	public int getDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_WEEK);
	}
	
	public int getMonthDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	
	public String getDayText() {
		if (date == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getDisplayName( Calendar.DAY_OF_WEEK ,Calendar.LONG, ctx.get().getLocale());
	}

	public int getMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH);
	}
	
	public String getMonthText() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getDisplayName( Calendar.MONTH ,Calendar.LONG, ctx.get().getLocale());
	}

	public int getYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	public String getInputDate() throws ParseException {
		return StringHelper.renderInputDate(date);
	}

	public String getSortableDate() {
		return StringHelper.renderSortableDate(date);
	}

	public String getFormatRFC3339() {
		try {
			return StringHelper.renderDate(date, "yyyy-MM-dd");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getShortDate() throws FileNotFoundException, IOException {
		return StringHelper.renderShortDate(ctx.get(), date);
	}

	public String getMediumDate() {
		return StringHelper.renderMediumDate(ctx.get(), date);
	}

	public String getFullDate() {
		return StringHelper.renderFullDate(ctx.get(), date);
	}

	@Override
	public String toString() {
		try {
			return getShortDate();
		} catch (Exception e) {
			e.printStackTrace();
			return super.toString();
		}
	}

	@Override
	public boolean equals(Object obj) {
		return date.equals(((DateBean) obj).date);
	}

	public DateBean getDayPrevious() throws FileNotFoundException, IOException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return new DateBean(ctx.get(), cal.getTime());
	}

	public DateBean getDayNext() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, 1); // Add 1 day
		return new DateBean(ctx.get(), cal.getTime());
	}

	public DateBean getDays30Previous() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, -30); // Subtract 30 days
		return new DateBean(ctx.get(), cal.getTime());
	}

	public DateBean getMonthPrevious() throws FileNotFoundException, IOException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add( Calendar.MONTH, -1);
		return new DateBean(ctx.get(), cal.getTime());
	}

	public DateBean getMonthNext() throws FileNotFoundException, IOException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add( Calendar.MONTH, 1);
		return new DateBean(ctx.get(), cal.getTime());
	}

	public DateBean getYearPrevious() throws FileNotFoundException, IOException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.YEAR, -1);
		return new DateBean(ctx.get(), cal.getTime());
	}

	public DateBean getYearNext() throws FileNotFoundException, IOException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add( Calendar.YEAR, 1);
		return new DateBean(ctx.get(), cal.getTime());
	}

}
