package org.javlo.search;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.utils.CollectionAsMap;

public class SearchFilter {

	GlobalContext globalContext;

	private static final String KEY = "searchFilter";

	public static final SearchFilter getInstance(ContentContext ctx) {
		SearchFilter filter = (SearchFilter) ctx.getRequest().getSession().getAttribute(KEY);
		if (filter == null) {
			filter = new SearchFilter();
			ctx.getRequest().getSession().setAttribute(KEY, filter);
		}
		filter.globalContext = GlobalContext.getInstance(ctx.getRequest());
		return filter;
	}

	private final List<String> rootPageName = new LinkedList<String>();

	private String tag;

	private Date startDate = null;

	private Date endDate = null;

	public void reset(ContentContext ctx) {
		ctx.getRequest().getSession().setAttribute(KEY, new SearchFilter());
	}

	public void clearRootPages() {
		rootPageName.clear();
	}

	public Map<String, String> getRootPageName() {
		return new CollectionAsMap<String>(rootPageName);
	}

	public void addRootPageName(String inRootPageName) {
		if (inRootPageName != null) {
			this.rootPageName.add(inRootPageName);
		}
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		if (tag != null) {
			if (tag.trim().length() == 0) {
				this.tag = null;
			} else {
				this.tag = tag;
			}
		}
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getStartDateStr() {
		return StringHelper.renderDate(getStartDate(), globalContext.getShortDateFormat());
	}

	public String getEndDateStr() {
		return StringHelper.renderDate(getEndDate(), globalContext.getShortDateFormat());
	}

	/**
	 * return true if date in between start date and end date
	 * 
	 * @param date
	 * @return
	 */
	public boolean isInside(Date date) {
		return TimeHelper.betweenInDay(date, getStartDate(), getEndDate());
	}
}
