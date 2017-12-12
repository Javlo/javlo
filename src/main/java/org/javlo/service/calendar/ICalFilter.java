package org.javlo.service.calendar;

import org.javlo.helper.StringHelper;

public class ICalFilter {
	
	public static final ICalFilter NO_FILTER = new ICalFilter(null,null);
	
	private String summary = null;
	
	private String category = null;

	public ICalFilter(String summary, String category) {
		super();
		this.summary = summary;
		this.category = category;
	}
	
	public boolean accept(ICal ical) {
		if (this==NO_FILTER) {
			return true;
		}
		boolean accept = true;
		if (!StringHelper.isEmpty(summary)) {
			if (!StringHelper.neverEmpty(ical.getSummary(), "").contains(summary)) {
				return false;
			}
		}
		if (!StringHelper.isEmpty(category)) {
			if (!StringHelper.neverEmpty(ical.getCategories(), "").contains(category)) {
				return false;
			}
		}
		return accept;
	}

}
