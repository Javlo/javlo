package org.javlo.bean;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class DateBean {
	
	private Date date = null;	
	private String shortDate = null;
	private String mediumDate = null;
	private String fullDate = null;

	public DateBean(ContentContext ctx, Date date) throws FileNotFoundException, IOException {
		this.date = date;	
		shortDate = StringHelper.renderShortDate(ctx, date);
		mediumDate = StringHelper.renderMediumDate(ctx, date);
		fullDate = StringHelper.renderFullDate(ctx, date);
	}
	
	public Date getDate() {
		return date;
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

}
