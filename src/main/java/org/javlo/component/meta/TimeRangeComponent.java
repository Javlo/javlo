/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.StringHelper;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class TimeRangeComponent extends DateComponent {

	protected static final String VALUE_SEPARATOR = "%";

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
	public Date getDate() {
		return getStartDate();
	}

	@Override
	protected void init() throws ResourceNotFoundException {
		super.init();

		/** convert old version **/
		if (getValue().contains("-") && !getValue().contains(VALUE_SEPARATOR)) {
			setValue(getValue().replaceAll("-", VALUE_SEPARATOR));
		}

		if (getEndDate() == null && initDate) {
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
		finalCode.append("<div class=\"row\"><div class=\"col-sm-6\"><div class=\"form-group\"><label>Start : <input class=\"form-control\" id=\"contentdate\" style=\"width: 120px;\" type=\"text\" id=\"" + getInputStartDateName() + "\" name=\"" + getInputStartDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getStartDate(), "") + "\"/></label></div></div>");
		finalCode.append("<div class=\"col-sm-6\"><div class=\"form-group\"><label>End : <input class=\"form-control\" style=\"width: 120px;\" type=\"text\" id=\"" + getInputEndDateName() + "\" name=\"" + getInputEndDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getEndDate(), "") + "\"/></label></div></div></div>");
		return finalCode.toString();
	}

	public Date getStartDate() {
		Date date = null;
		try {
			String dateStr = getValue().split(VALUE_SEPARATOR)[0];
			date = StringHelper.parseTime(dateStr);
		} catch (Throwable t) {
			// logger.warning(t.getMessage());
		}
		return date;
	}

	public Date getEndDate() {
		Date date = null;
		try {
			String dateStr = getValue().split(VALUE_SEPARATOR)[1];
			date = StringHelper.parseTime(dateStr);
		} catch (Throwable t) {
			// logger.warning(t.getMessage());
		}
		return date;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		
		if (getConfig(ctx).getProperty("prefix", null) != null) {
			return getConfig(ctx).getProperty("prefix", null) + "<div " + getSpecialPreviewCssClass(ctx, "") + getSpecialPreviewCssId(ctx) + ">";
		}

		return "<div " + getSpecialPreviewCssClass(ctx, getStyle(ctx) + " " + getType()) + getSpecialPreviewCssId(ctx) + ">";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		
		if (getConfig(ctx).getProperty("suffix", null) != null) {
			return "</div>" + getConfig(ctx).getProperty("suffix", null);
		}
		return "</div>";
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {		
		if (getCurrentRenderer(ctx) != null && getCurrentRenderer(ctx).equalsIgnoreCase(HIDDEN)) {
			return "";
		}
		if (getStartDate() != null && getEndDate() != null) {
			return renderDate(ctx, getStartDate()) + " - " + renderDate(ctx, getEndDate());
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
	public boolean isEmpty(ContentContext ctx) {
		return true;
	}

	@Override
	public String getHexColor() {
		return TEXT_COLOR;
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
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
			} catch (ParseException p) {
				p.printStackTrace();
				setNeedRefresh(true);
			}
			
			String dateStr = StringHelper.renderTime(startDate) + '-' + StringHelper.renderTime(endDate);
			if (!dateStr.equals(getValue())) {
				setValue(dateStr);
				setModify();
			}
		}
	}

}
