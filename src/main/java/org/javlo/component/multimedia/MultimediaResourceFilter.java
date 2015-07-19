package org.javlo.component.multimedia;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.RequestService;
import org.javlo.utils.CollectionAsMap;

public class MultimediaResourceFilter {

	private boolean active = false;
	private String query = "";
	private Set<String> tags = new HashSet<String>();
	private Calendar startDate = null;
	private Calendar endDate = null;

	public static MultimediaResourceFilter getInstance(ContentContext ctx) throws ParseException {
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		MultimediaResourceFilter outFilter = (MultimediaResourceFilter) ctx.getRequest().getSession().getAttribute("multimediaFilter");
		if (outFilter == null) {
			outFilter = new MultimediaResourceFilter();
			ctx.getRequest().getSession().setAttribute("multimediaFilter", outFilter);
		}
		if (rs.getParameter("clear", null) != null) {
			outFilter = new MultimediaResourceFilter();
			ctx.getRequest().getSession().setAttribute("multimediaFilter", outFilter);
		} else if (rs.getParameter("filter", null) != null) {
			outFilter.setQuery(rs.getParameter("mqr", null));
			String startDate = rs.getParameter("msd", null);
			String rangeDate = rs.getParameter("mrd", "");
			if (rs.getParameter("y", null) != null) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, Integer.parseInt(rs.getParameter("y", null)));
				cal.set(Calendar.MONTH, 0);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				outFilter.startDate = cal;
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(cal.getTime());				
				endCal.roll(Calendar.YEAR, true);
				outFilter.endDate = endCal;
				outFilter.active = true;
			} else if (rangeDate.trim().length()>0) {
				Date[] date = StringHelper.parseRangeDate(rangeDate);
				outFilter.active = true;
				outFilter.startDate = Calendar.getInstance();
				outFilter.startDate.setTime(date[0]);
				if (date.length > 1) {
					outFilter.endDate = Calendar.getInstance();
					outFilter.endDate.setTime(date[1]);
				}
			}
			if (outFilter.startDate == null && startDate != null && startDate.trim().length() > 6) {
				outFilter.startDate = Calendar.getInstance();
				try {
					outFilter.active = true;
					outFilter.startDate.setTime(StringHelper.parseDate(startDate));
				} catch (ParseException e) {
				}
			}
			String endDate = rs.getParameter("med", null);
			if (outFilter.endDate == null && endDate != null && endDate.trim().length() > 6) {
				outFilter.endDate = Calendar.getInstance();
				try {
					outFilter.active = true;
					outFilter.endDate.setTime(StringHelper.parseDate(endDate));
				} catch (ParseException e) {
				}
			}
			if (!StringHelper.isEmpty(outFilter.getQuery())) {
				outFilter.active = true;
			}
			outFilter.tags.clear();
			outFilter.tags.addAll(rs.getParameterListValues("mtg", Collections.EMPTY_LIST));
			if (outFilter.tags.size() > 0) {
				outFilter.active = true;
			}
		}
		return outFilter;
	}
	
	public boolean accept(MultimediaResource resource) {
		if (resource == null) {
			return false;
		} else {
			if (!active) {
				return true;
			} else {
				boolean outAccept = true;
				if (!StringHelper.isEmpty(query)) {
					outAccept = resource.getFullDescription().toLowerCase().contains(query.toLowerCase());
				}
				if (outAccept && tags.size() > 0) {
					outAccept = !Collections.disjoint(resource.getTags(), tags);
				}
				if (outAccept) {
					if (resource.getDate() != null) {
						Calendar cal = Calendar.getInstance();
						cal.setTime(resource.getDate());
						if (startDate != null) {
							outAccept = startDate.before(cal);
						}
						if (outAccept) {
							if (endDate != null) {
								outAccept = endDate.after(cal);
							}
						}
					}
				}
				return outAccept;
			}
		}
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Map<String, String> getTags() {
		return new CollectionAsMap<String>(tags);
	}

	public Date getStartDate() {
		if (startDate == null) {
			return null;
		} else {
			return startDate.getTime();
		}
	}

	public String getRawStartDate() {
		return StringHelper.renderDate(getStartDate());
	}

	public String getRawRangeDate() {
		if (getRawStartDate() == null) {
			return null;
		} else {
			if (getRawEndDate() != null) {
				return getRawStartDate() + " - " + getRawEndDate();
			} else {
				return getRawStartDate();
			}
		}
	}

	public String getRawEndDate() {
		return StringHelper.renderDate(getEndDate());
	}

	public void setStartDate(Date startDate) {
		if (this.startDate == null) {
			this.startDate = Calendar.getInstance();
		}
		this.startDate.setTime(startDate);
	}

	public Date getEndDate() {
		if (endDate == null) {
			return null;
		} else {
			return endDate.getTime();
		}
	}

	public void setEndDate(Date endDate) {
		if (this.endDate == null) {
			this.endDate = Calendar.getInstance();
		}
		this.endDate.setTime(endDate);
	}

}
