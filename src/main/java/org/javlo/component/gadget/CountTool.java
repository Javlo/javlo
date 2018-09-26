/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.gadget;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class CountTool extends AbstractPropertiesComponent {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(CountTool.class.getName());

	public static final String TYPE = "counttool";

	private static final String FIELD_TARGET_DATE = "target-date";
	private static final String FIELD_LINK = "link";
	private static final String FIELD_LABEL = "label";

	private static final List<String> FIELDS = Arrays.asList(new String[] { FIELD_TARGET_DATE, FIELD_LINK, FIELD_LABEL });

	protected String getDateName() {
		return "date_" + getId();
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

	protected String getLinkName() {
		return "link_" + getId();
	}

	public String getLink() {
		String[] allValue = StringHelper.stringToArray(getValue());
		if (allValue.length > 1) {
			return allValue[1];
		}
		return "";
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		Date date = getDate();
		ctx.getRequest().setAttribute("targetDate", date.getTime());
		Date currentDate = new Date();
		ctx.getRequest().setAttribute("second", Math.round((date.getTime() - currentDate.getTime()) / 1000));
		ctx.getRequest().setAttribute("link", URLHelper.convertLink(ctx,getFieldValue(FIELD_LINK)));
		ctx.getRequest().setAttribute("label", getFieldValue(FIELD_LABEL));
	}

	public Date getDate() {
		String rawDate = getFieldValue(FIELD_TARGET_DATE);
		Date date = null;
		try {
			date = StringHelper.parseTime(rawDate);
		} catch (ParseException e) {
			try {
				date = StringHelper.parseDate(rawDate);
			} catch (ParseException e1) {
			}
		}
		return date;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "no renderer defined";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}

	@Override
	public String getHexColor() {
		return WEB2_COLOR;
	}

	@Override
	public String getFontAwesome() {
		return "clock-o";
	}

}
