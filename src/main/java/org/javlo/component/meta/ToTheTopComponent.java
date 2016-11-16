/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.meta;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.StringHelper;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class ToTheTopComponent extends AbstractVisualComponent {

	public static final String TYPE = "to-the-top";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	protected void init() throws ResourceNotFoundException {
		super.init();
	}

	public String getInputDateName() {
		return "__" + getId() + ID_SEPARATOR + "date";
	}

	public String getInputTimeName() {
		return "__" + getId() + ID_SEPARATOR + "time";
	}
	
	@Override
	public String getStyleLabel(ContentContext ctx) {	
		return "power";
	}
	
	@Override
	public String[] getStyleList(ContentContext ctx) {	
		return new String[] {"1","2","3","4","5"};
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		Date date = getDate();
		if (date == null) {
			date = new Date();
		}

		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());
		finalCode.append("<input id=\"contentdate\" style=\"width: 120px;\" type=\"text\" id=\"" + getInputDateName() + "\" name=\"" + getInputDateName() + "\" value=\"" + StringHelper.renderDate(date) + "\"/>");
		finalCode.append("<input style=\"margin-left: 30px;width: 120px;\" type=\"text\" id=\"" + getInputTimeName() + "\" name=\"" + getInputTimeName() + "\" value=\"" + StringHelper.renderOnlyTime(date) + "\"/>");
		return finalCode.toString();
	}

	public Date getDate() {
		Date date = null;
		try {
			date = StringHelper.parseTime(getValue());
		} catch (ParseException e) {
			try {
				date = StringHelper.parseDate(getValue());
			} catch (ParseException e2) {
			}
		}
		return date;
	}

	public void setDate(Date date) {
		setValue(StringHelper.renderTime(date));
	}
	
	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "<div " + getSpecialPreviewCssClass(ctx, getStyle(ctx) + " " + getType()) + getSpecialPreviewCssId(ctx) + ">";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "</div>";
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (ctx.isPreviewEdit()) {
			return getType()+" >> "+StringHelper.renderDate(getDate());
		} else {
			return "";
		}
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return true;
	}

	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return super.isEmpty(ctx); // this component is never not empty -> use empty parent method
	}

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String newDate = requestService.getParameter(getInputDateName(), null);
		String newTime = requestService.getParameter(getInputTimeName(), null);
		if (newDate != null && newTime != null) {
			String dateStr = newDate + ' ' + newTime;
			if (!dateStr.equals(StringHelper.renderTime(getDate()))) {
				setValue(dateStr);
				setModify();
			}
		}
		return null;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {	
		return true;
	}
	
	@Override
	public boolean isMirroredByDefault(ContentContext ctx) {	
		return true;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
	}
	
	public int getPower() {
		if (!StringHelper.isEmpty(getValue()) && StringHelper.isDigit(getStyle())) {
			Calendar date = Calendar.getInstance();
			date.setTime(getDate());
			if (date.after(Calendar.getInstance())) {
				return Integer.parseInt(getStyle());
			}
		}
		return 0;
	}

}
