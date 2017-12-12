package org.javlo.service.calendar;

import java.util.Date;

import org.javlo.helper.StringHelper;

public class GhostICal extends ICal {
	
	private ICal main = null;	
	private Date date = null;
	
	public GhostICal(ICal main, Date date) {
		super(main.isEditable());
		this.main = main;
		this.date = date;
		setNext(true);
	}
	
	public String getSummaryOrCategories() {
		if (StringHelper.isEmpty(getSummary())) {
			return getCategories();
		}
		return getSummary();
	}

	@Override
	public String getSummary() {
		return main.getSummary();
	}
	
	@Override
	public String getCategories() {
		return main.getCategories();
	}
	
	@Override
	public Date getStartDate() {
		return date;
	}
	
	@Override
	public Date getEndDate() {
		return null;
	}
	
	@Override
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	@Override
	public boolean isPrevious() {	
		return true;
	}
}
