/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.gadget;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.service.RequestService;


/**
 * @author pvandermaesen
 */
public class CountTool extends AbstractVisualComponent {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(CountTool.class.getName());

	public static final String TYPE = "counttool";

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "<div " + getSpecialPreviewCssClass(ctx, getStyle(ctx)) + getSpecialPreviewCssId(ctx) + " >";
	}

	@Override
	public String getSufixViewXHTMLCode(ContentContext ctx) {
		return "</div>";
	}

	protected String getDateName() {
		return "date_" + getId();
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
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		out.println(getSpecialInputTag());
		out.println("<div class=\"line\">");
		out.print("<label for=\"" + getDateName() + "\">");
		out.print(i18nAccess.getText("content.counttool.date"));
		out.println(" : </label>");
		out.print("<input id=\"" + getDateName() + "\" name=\"" + getDateName() + "\" value=\"");
		out.print(StringHelper.neverNull(StringHelper.renderTime(getDate())));
		out.println("\" />");
		out.println("</div>");
		out.println("<div class=\"line\">");
		out.print("<label for=\"" + getLinkName() + "\">");
		out.print(i18nAccess.getText("content.counttool.link"));
		out.println(" : </label>");
		out.print("<input id=\"" + getLinkName() + "\" name=\"" + getLinkName() + "\" value=\"");
		out.print(getLink());
		out.println("\" />");
		out.println("</div>");

		out.close();

		// validation
		if (getDate() == null) {
			setMessage(new GenericMessage(i18nAccess.getText("content.counttool.unvalid-date"), GenericMessage.ERROR));
		} else {
			setMessage(null);
		}

		return writer.toString();

	}

	public Date getDate() {
		String[] allValue = StringHelper.stringToArray(getValue());

		Date date = null;
		if (allValue.length > 0 && allValue[0].length() > 0) {
			try {
				date = StringHelper.parseTime(allValue[0]);
			} catch (ParseException e) {
				try {
					date = StringHelper.parseDate(allValue[0]);
				} catch (ParseException e1) {
					// return null = error
				}
			}
		}

		return date;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (getDate() == null) {
			return "";
		}
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		Calendar cal = GregorianCalendar.getInstance();
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		i18nAccess.changeViewLanguage(ctx);
		String js="";
		if (getLink().length() > 0) {
			out.println("<a href=\""+getLink()+"\">");
		}
		out.println("<div"+js+" class=\"count-tool count-tool-" + ctx.getRequestContentLanguage() + "\">");
		out.println("	<input type=\"text\" class=\"timer-days\" readonly=\"readonly\" />");
		out.println("	<input type=\"text\" class=\"timer-time\" readonly=\"readonly\" />");
		out.println("	<script language=\"javascript\">");
		cal.setTime(new Date());
		out.println("		var currentDate = new Date(" + cal.get(Calendar.YEAR) + "," + cal.get(Calendar.MONTH) + "," + cal.get(Calendar.DAY_OF_MONTH) + ","
				+ cal.get(Calendar.HOUR) + "," + cal.get(Calendar.MINUTE) + "," + cal.get(Calendar.SECOND) + ");");
		cal.setTime(getDate());
		out.println("		var endDate = new Date(" + cal.get(Calendar.YEAR) + "," + cal.get(Calendar.MONTH) + "," + cal.get(Calendar.DAY_OF_MONTH) + ","
				+ cal.get(Calendar.HOUR_OF_DAY) + "," + cal.get(Calendar.MINUTE) + "," + cal.get(Calendar.SECOND) + ");");
		out.println("		displayDate('" + i18nAccess.getContentViewText("global.days") + "');displayDate.periodical(1000);");
		out.println("	</script>");
		out.println("</div>");
		if (getLink().length() > 0) {
			out.println("</a>");
		}

		out.close();
		return writer.toString();
	}

	public String getType() {
		return TYPE;
	}

	/**
	 * you can check if it is possible to extract the selection of the component
	 * in a other component.
	 * 
	 * @return true if content is extractable
	 */
	@Override
	public boolean isExtractable() {
		return true;
	}

	@Override
	public int getComplexityLevel() {
		return COMPLEXITY_ADMIN;
	}

	@Override
	public String getHexColor() {
		return WEB2_COLOR;
	}

	@Override
	public boolean needJavaScript(ContentContext ctx) {
		return true;
	}

	@Override
	public void refresh(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String date = requestService.getParameter(getDateName(), "");
		String link = requestService.getParameter(getLinkName(), null);
		if (link == null) {
			return;
		}
		if (!link.equals(getLink())) {
			setModify();
		} else if (date.length() > 0) {
			try {
				Date dateParsed = StringHelper.parseTime(date);
				if (!dateParsed.equals(getDate())) {
					setModify();
				}
			} catch (ParseException e) {
				logger.warning(e.getMessage());
			}
		}
		if (isModify()) {
			String[] values = new String[] { date, link };
			setValue(StringHelper.arrayToString(values));
		}
	}

}
