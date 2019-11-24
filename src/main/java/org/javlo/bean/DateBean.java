package org.javlo.bean;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class DateBean {
	
	private Date date = null;	
	private String shortDate = null;
	private String mediumDate = null;
	private String fullDate = null;
	private WeakReference<ContentContext> ctx;

	public DateBean(ContentContext ctx, Date date) throws FileNotFoundException, IOException {
		this.date = date;	
		shortDate = StringHelper.renderShortDate(ctx, date);
		mediumDate = StringHelper.renderMediumDate(ctx, date);
		fullDate = StringHelper.renderFullDate(ctx, date);
		this.ctx = new WeakReference<ContentContext>(ctx);
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getInputDate() throws ParseException {
		return StringHelper.renderInputDate(date);
	}
	
	public String getSortableDate() {
		return StringHelper.renderSortableDate(date);
	}

	public String getShortDate() {
		return shortDate;
	}

	public String getMediumDate() {
		return mediumDate;
	}

	public String getFullDate() {
		return fullDate;
	}
	
	@Override
	public String toString() { 
		return shortDate;
	}
	
	@Override
	public boolean equals(Object obj) {
		return date.equals(((DateBean)obj).date);
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
