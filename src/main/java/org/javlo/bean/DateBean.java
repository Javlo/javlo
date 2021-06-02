package org.javlo.bean;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

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
	
	public String getDayText() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getDisplayName( Calendar.DAY_OF_WEEK ,Calendar.LONG, new Locale(ctx.get().getRequestContentLanguage()));
	}

	public int getMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH);
	}
	
	public String getMonthText() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getDisplayName( Calendar.MONTH ,Calendar.LONG, new Locale(ctx.get().getRequestContentLanguage()));
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
		cal.add(-1, Calendar.DAY_OF_MONTH);
		return new DateBean(ctx.get(), cal.getTime());
	}

	public DateBean getDayNext() throws FileNotFoundException, IOException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(1, Calendar.DAY_OF_MONTH);
		return new DateBean(ctx.get(), cal.getTime());
	}

}
