package org.javlo.utils;

import org.javlo.bean.DateBean;
import org.javlo.context.ContentContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;

public class SmartTimeRange extends TimeRange {
	
	private WeakReference<ContentContext> ctx;
	
	public SmartTimeRange(ContentContext ctx, Date startDate, Date endDate) {
		super(startDate, endDate);
		this.ctx = new WeakReference<ContentContext>(ctx);
	}
	
	public SmartTimeRange(ContentContext ctx, TimeRange tr) {
		super(tr.getStartDate(), tr.getEndDate());
		this.ctx = new WeakReference<ContentContext>(ctx);
	}
	
	public DateBean getStartDateBean() throws FileNotFoundException, IOException {
		return new DateBean(ctx.get(), getStartDate());
	}
	
	public DateBean getEndDateBean() throws FileNotFoundException, IOException {
		return new DateBean(ctx.get(), getEndDate());
	}

	public String toString() {
		try {
			return getStartDateBean().getShortDate() + " - " + getEndDateBean().getShortDate();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
