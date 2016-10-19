package org.javlo.module.dashboard;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.javlo.helper.StringHelper;

public class ReportFilter {

	private static final String KEY = "reportFilter";

	private Date startDate = null;

	private ReportFilter() {
	}

	public static ReportFilter getInstance(HttpSession session) {
		ReportFilter reportFilter = (ReportFilter) session.getAttribute(KEY);
		if (reportFilter == null) {
			reportFilter = new ReportFilter();
			session.setAttribute(KEY, reportFilter);
		}
		return reportFilter;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getStartDateLabel() {
		if (!StringHelper.isEmpty(startDate)) {
			return StringHelper.renderDate(startDate);
		} else {
			return "";
		}
	}
}
