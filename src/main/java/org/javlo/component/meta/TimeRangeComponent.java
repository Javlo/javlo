/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import org.javlo.context.ContentContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.service.RequestService;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

/**
 * @author pvandermaesen
 */
public class TimeRangeComponent extends DateComponent implements ITimeRange {

	public static final String VALUE_SEPARATOR = "%";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(TimeRangeComponent.class.getName());

	public static final String TYPE = "time-range";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public Date getDate(ContentContext ctx) {
		return getStartDate(ctx);
	}

	@Override
	protected void init() throws ResourceNotFoundException {
		super.init();

		/** convert old version **/
		if (getValue().contains("-") && !getValue().contains(VALUE_SEPARATOR)) {
			setValue(getValue().replaceAll("-", VALUE_SEPARATOR));
		}

		if (getEndDate(null) == null && initDate) {
			Calendar calStart = GregorianCalendar.getInstance();
			Calendar calEnd = GregorianCalendar.getInstance();
			calEnd.roll(Calendar.WEEK_OF_YEAR, true);
			String value = StringHelper.renderTime(calStart.getTime()) + VALUE_SEPARATOR + StringHelper.renderTime(calEnd.getTime());
			setValue(value);
		}
	}

	public String getInputStartDateName() {
		return "__" + getId() + ID_SEPARATOR + "start-date";
	}

	public String getInputTag(String tag) {
		return "__" + getId() + ID_SEPARATOR + tag;
	}

	public String getInputEndDateName() {
		return "__" + getId() + ID_SEPARATOR + "end-start";
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());
		finalCode.append("<div class=\"row\"><div class=\"col-sm-6\"><div class=\"form-group date\"><label>Start : <input class=\"form-control\" id=\"contentdate\" style=\"width: 120px;\" type=\"text\" id=\"" + getInputStartDateName() + "\" name=\"" + getInputStartDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getStartDate(ctx), "") + "\"/></label></div></div>");
		finalCode.append("<div class=\"col-sm-6\"><div class=\"form-group\"><label>End : <input class=\"form-control date\" style=\"width: 120px;\" type=\"text\" id=\"" + getInputEndDateName() + "\" name=\"" + getInputEndDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getEndDate(ctx), "") + "\"/></label></div></div></div>");
		return finalCode.toString();
	}

	public Date getStartDate(ContentContext ctx) {
		Date date = null;
		try {
			String dateStr = getValue().split(VALUE_SEPARATOR)[0];
			date = StringHelper.parseTime(dateStr);
		} catch (Throwable t) {
			// logger.warning(t.getMessage());
		}
		return date;
	}

	public Date getEndDate(ContentContext ctx) {
		Date date = null;
		try {
			String dateStr = getValue().split(VALUE_SEPARATOR)[1];
			date = StringHelper.parseTime(dateStr);
		} catch (Throwable t) {
			// logger.warning(t.getMessage());
		}
		return date;
	}

//	@Override
//	public String getPrefixViewXHTMLCode(ContentContext ctx) {
//		
//		if (getConfig(ctx).getProperty("prefix", null) != null) {
//			return getConfig(ctx).getProperty("prefix", null) + "<div " + getSpecialPreviewCssClass(ctx, "") + getSpecialPreviewCssId(ctx) + ">";
//		}
//
//		return "<div " + getSpecialPreviewCssClass(ctx, getComponentCssClass(ctx) + " " + getType()) + getSpecialPreviewCssId(ctx) + ">";
//	}
//
//	@Override
//	public String getSuffixViewXHTMLCode(ContentContext ctx) {
//		
//		if (getConfig(ctx).getProperty("suffix", null) != null) {
//			return "</div>" + getConfig(ctx).getProperty("suffix", null);
//		}
//		return "</div>";
//	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		String range = "";
		if (getStartDate(ctx) != null && getEndDate(ctx) != null) {
			range = renderDate(ctx, getStartDate(ctx)) + " - " + renderDate(ctx, getEndDate(ctx));
		}		
		ctx.getRequest().setAttribute("range", range );
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {		
		if (getCurrentRenderer(ctx) != null && getCurrentRenderer(ctx).equalsIgnoreCase(HIDDEN)) {
			return "";
		}
		if (getStartDate(ctx) != null && getEndDate(ctx) != null) {
			return renderDate(ctx, getStartDate(ctx)) + " - " + renderDate(ctx, getEndDate(ctx));
		} else {
			return "";
		}
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_HIGH;
	}

	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	public String getHexColor() {
		return TEXT_COLOR;
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String newStartDate = requestService.getParameter(getInputStartDateName(), null);
		String newEndDate = requestService.getParameter(getInputEndDateName(), null);
		if (newStartDate != null && newEndDate != null) {
			Date startDate = new Date();
			try {
				startDate = StringHelper.parseDateOrTime(newStartDate);
			} catch (ParseException p) {
				p.printStackTrace();
				setNeedRefresh(true);
			}

			Date endDate = new Date();
			
			try {
				endDate = StringHelper.parseDateOrTime(newEndDate);
				if (endDate.getTime() < startDate.getTime()) {
					endDate = startDate;
				}
				Calendar cal = Calendar.getInstance();
				cal.setTime(endDate);
				if (cal.get(Calendar.HOUR) == 0 && cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0) {
					cal = TimeHelper.convertEndOfDay(cal);
					endDate = cal.getTime();
				}
			} catch (ParseException p) {
				p.printStackTrace();
				setNeedRefresh(true);
			}
			
			String dateStr = StringHelper.renderTime(startDate) + VALUE_SEPARATOR + StringHelper.renderTime(endDate);
			if (!dateStr.equals(getValue())) {
				setValue(dateStr);
				setModify();
			}
		}
		return null;
	}
	
	@Override
	public boolean isMirroredByDefault(ContentContext ctx) {	
		return true;
	}

	public LocalDateTime getTimeRangeStart(ContentContext ctx) {
		Date startDate = getStartDate(ctx);
		if (startDate != null) {
			return  startDate.toInstant()
					.atZone(ZoneId.systemDefault())
					.toLocalDate().atTime(0,0);
		}
		return null;
	}

	@Override
	public LocalDateTime getTimeRangeEnd(ContentContext ctx) {
		Date startDate = getEndDate(ctx);
		if (startDate != null) {
			return  startDate.toInstant()
					.atZone(ZoneId.systemDefault())
					.toLocalDate().atTime(0,0);
		}
		return null;
	}
}
