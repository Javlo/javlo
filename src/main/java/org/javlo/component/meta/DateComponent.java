/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.meta;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IDate;
import org.javlo.context.ContentContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class DateComponent extends AbstractVisualComponent implements IDate {

	public static final String TYPE = "date";

	private static final String USER_FRIENDLY_DATE_TYPE = "userfriendly-date";

	private static final String MEDIUM_DATE_TYPE = "medium-date";

	private static final String SHORT_DATE_TYPE = "short-date";

	private static final String SHORT_DATE_WIDTH_DAY = "short-date-width-day";

	private static final String VISIBLE_DATE_TYPE = "visible-date";

	private final String VISIBLE_TIME_TYPE = "visible-time";

	public static final String NOT_VISIBLE_TYPE = "not-visible";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {		
		if (getConfig(ctx).getStyleList() == null || getConfig(ctx).getStyleList().length == 0) {
			return new String[] { USER_FRIENDLY_DATE_TYPE, MEDIUM_DATE_TYPE, SHORT_DATE_TYPE, SHORT_DATE_WIDTH_DAY, VISIBLE_DATE_TYPE, VISIBLE_TIME_TYPE, NOT_VISIBLE_TYPE };
		} else {
			return getConfig(ctx).getStyleList();
		}
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		try {			
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			String[] styleList = getConfig(ctx).getStyleList();
			if (styleList == null || styleList.length == 0) {
				return new String[] { i18n.getText("content.date.userfriendly-date"), i18n.getText("content.date.medium-date"), i18n.getText("content.date.short-date"), i18n.getText("content.date.short-date-width-day"), i18n.getText("content.date.visible-date"), i18n.getText("content.date.visible-time"), i18n.getText("global.hidden") };
			} else {
				String[] outStyleLabel = new String[styleList.length];
				int i=0;
				for (String key : styleList) {
					outStyleLabel[i] = i18n.getText("content.date."+key);
					i++;
				}
				return outStyleLabel;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return getStyleList(ctx);
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			return i18n.getText("content.date.style-title");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	protected boolean initDate = true;

	@Override
	protected void init() throws ResourceNotFoundException {
		super.init();
		if (getDate() == null && initDate) {
			setDate(new Date());
		}
	}

	public String getInputDateName() {
		return "__" + getId() + ID_SEPARATOR + "date";
	}

	public String getInputTimeName() {
		return "__" + getId() + ID_SEPARATOR + "time";
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		Date date = getDate();
		if (date == null) {
			date = new Date();
		}

		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());
		finalCode.append("<input id=\"contentdate\" style=\"width: 120px;\" type=\"date\" id=\"" + getInputDateName() + "\" name=\"" + getInputDateName() + "\" value=\"" + StringHelper.renderInputDate(date) + "\"/>");
		finalCode.append("<input style=\"margin-left: 30px;width: 120px;\" type=\"time\" id=\"" + getInputTimeName() + "\" name=\"" + getInputTimeName() + "\" value=\"" + StringHelper.renderOnlyTime(date) + "\"/>");
		return finalCode.toString();
	}

	public Date getDate() {
		Date date = null;
		if (StringHelper.isEmpty(getValue())) {
			return null;
		}
		try {
			if (getValue().contains("/")) {
				date = StringHelper.parseTime(getValue());
			} else {
				date = StringHelper.parseInputDateAndTime(getValue());
			}
		} catch (ParseException e) {
			try {
				date = StringHelper.parseInputDate(getValue());
			} catch (ParseException e2) {
			}
		}
		return date;
	}

	public void setDate(Date date) {
		setValue(StringHelper.renderTime(date));
	}
	
//	@Override
//	public String getPrefixViewXHTMLCode(ContentContext ctx) {
//		return "<div " + getSpecialPreviewCssClass(ctx, getComponentCssClass(ctx) + " " + getType()) + getSpecialPreviewCssId(ctx) + ">";
//	}
//
//	@Override
//	public String getSuffixViewXHTMLCode(ContentContext ctx) {
//		return "</div>";
//	}

	protected String renderDate(ContentContext ctx, Date date) throws FileNotFoundException, IOException {
		if (getStyle() == null) {
			return StringHelper.renderDate(date);
		}
		if (getStyle().equals(USER_FRIENDLY_DATE_TYPE)) {
			return StringHelper.renderUserFriendlyDate(ctx, date);
		} else if (getStyle().equals(MEDIUM_DATE_TYPE)) {
			return StringHelper.renderMediumDate(ctx, date);
		} else if (getStyle().equals(SHORT_DATE_TYPE)) {
			return StringHelper.renderShortDate(ctx, date);
		} else if (getStyle().equals(SHORT_DATE_WIDTH_DAY)) {
			return StringHelper.renderShortDateWidthDay(ctx, date);
		} else if (getStyle().equals(VISIBLE_DATE_TYPE)) {
			return StringHelper.renderDate(date);
		} else if (getStyle().equals(VISIBLE_TIME_TYPE)) {
			return StringHelper.renderTime(date);
		} else if (getStyle().equals(NOT_VISIBLE_TYPE)) {
			return "";
		} else {
			return "style not found : "+getStyle();
		}
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("date", renderDate(ctx, getDate()));
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return renderDate(ctx, getDate());
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_HIGH;
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
	public Date getDate(ContentContext ctx) {
		return getDate();
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

	@Override
	public boolean isValidDate(ContentContext ctx) {
		return !StringHelper.isEmpty(getValue());
	}
	
	@Override
	public String getFontAwesome() {	
		return "calendar-o";
	}
}
