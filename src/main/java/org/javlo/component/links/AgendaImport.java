/*
 * Created on 30/11/2009
 */
package org.javlo.component.links;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.component.meta.LocationComponent;
import org.javlo.context.ContentContext;
import org.javlo.fields.Field;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class AgendaImport extends AbstractVisualComponent {

	private static final String ONLY_TODAY = "only_today";

	private static final String STANDARD = "standard";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(AgendaImport.class.getName());

	protected static final String VALUE_SEPARATOR = "-";

	public static final String TYPE = "agenda-import";

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { STANDARD, ONLY_TODAY };
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			return new String[] { i18n.getText("global.standard"), i18n.getText("content.agenda-import.only_today") };
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

	@Override
	public String getType() {
		return TYPE;
	}

	private String getAgendaPageName() {
		return "agenda";
	}

	public String getInputStartDateName() {
		return "__" + getId() + ID_SEPARATOR + "start-date";
	}

	public String getInputEndDateName() {
		return "__" + getId() + ID_SEPARATOR + "end-start";
	}

	public Date getStartDate() {
		Date date = null;
		try {
			String dateStr = getValue().split(VALUE_SEPARATOR)[0];
			date = StringHelper.parseTime(dateStr);
		} catch (Throwable t) {
			logger.warning(t.getMessage());
		}
		if (date == null) {
			Calendar cal = GregorianCalendar.getInstance();
			cal.roll(Calendar.WEEK_OF_YEAR, false);
			date = cal.getTime();
		}
		return date;
	}

	public Date getEndDate() {
		Date date = new Date();
		try {
			String dateStr = getValue().split(VALUE_SEPARATOR)[1];
			date = StringHelper.parseTime(dateStr);
		} catch (Throwable t) {
			logger.warning(t.getMessage());
		}
		return date;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		Date date = getStartDate();
		if (date == null) {
			date = new Date();
		}
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());
		finalCode.append("<input id=\"contentdate\" style=\"width: 120px;\" type=\"text\" id=\"" + getInputStartDateName() + "\" name=\"" + getInputStartDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getStartDate(), "") + "\"/> - ");
		finalCode.append("<input style=\"width: 120px;\" type=\"text\" id=\"" + getInputEndDateName() + "\" name=\"" + getInputEndDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getEndDate(), "") + "\"/>&nbsp;&nbsp;");
		return finalCode.toString();
	}

	protected boolean dateMatch(Date date) {
		return TimeHelper.betweenInDay(date, getStartDate(), getEndDate());
	}

	protected Date getCloseToToDay(Map<Date, List<IContentVisualComponent>> contentByDate) {
		boolean existBeforeDate = false;
		boolean existAfterDate = false;
		Iterator<Date> datesIte = contentByDate.keySet().iterator();
		Calendar today = Calendar.getInstance();
		while (datesIte.hasNext()) {
			Date date = datesIte.next();
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			if (cal.before(today)) {
				existBeforeDate = true;
			} else if (cal.after(today)) {
				existAfterDate = true;
			} else if (TimeHelper.isEqualForDay(cal.getTime(), today.getTime())) {
				return date;
			}
		}
		Date previousDate = null;
		if (existAfterDate) {
			List<Date> dates = new LinkedList<Date>(contentByDate.keySet());
			Collections.sort(dates);
			Calendar cal = Calendar.getInstance();
			for (Date date : dates) {
				cal.setTime(date);
				if (cal.after(today)) {
					return date;
				}
			}
		} else if (existBeforeDate) {
			List<Date> dates = new LinkedList<Date>(contentByDate.keySet());
			Collections.sort(dates);
			for (Date date : dates) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				if (cal.after(today)) {
					if (previousDate != null) {
						return previousDate;
					}
				}
				previousDate = date;
			}
		}
		return previousDate;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		if (getRenderer(ctx) != null) {
			ContentService content = ContentService.getInstance(ctx.getRequest());
			MenuElement agendaPage = content.getNavigation(ctx).searchChildFromName(getAgendaPageName());
			MenuElement[] children = agendaPage.getAllChildren();
			if (children.length > 0) {

				Map<Date, List<IContentVisualComponent>> contentByDate = new HashMap<Date, List<IContentVisualComponent>>();
				for (MenuElement element : children) {
					contentByDate.putAll(element.getContentByDate(ctx));
				}
				logger.fine("date content size : " + contentByDate.size());
				Date date = getCloseToToDay(contentByDate);
				if (date != null) {
					ctx.getRequest().setAttribute("title", StringHelper.renderShortDate(ctx, date));
					List<IContentVisualComponent> contentForDate = contentByDate.get(date);
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					PrintStream out = new PrintStream(outStream);
					for (IContentVisualComponent contentVisualComponent : contentForDate) {
						if (contentVisualComponent instanceof LocationComponent) {
							ctx.getRequest().setAttribute("location", contentVisualComponent.getValue(ctx));
						} else {
							if (contentVisualComponent instanceof DynamicComponent) {
								DynamicComponent dynComp = (DynamicComponent) contentVisualComponent;
								Field location = dynComp.getField(ctx, "location");
								if (location != null) {
									ctx.getRequest().setAttribute("location", location.getValue());
								}
							}
							out.println("<div class=\""+contentVisualComponent.getType()+"\">"+contentVisualComponent.getXHTMLCode(ctx)+"</div>");
						}

					}
					out.close();
					ctx.getRequest().setAttribute("xhtml", new String(outStream.toByteArray()));
				} else {
					logger.warning("close to today date not found.");
				}
			}
		}
	}

	protected String getViewXHTMLCodeToday(ContentContext ctx) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement agendaPage = content.getNavigation(ctx).searchChildFromName(getAgendaPageName());
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		MenuElement[] children = agendaPage.getAllChildren();
		if (children.length > 0) {
			out.println("<div class=\"" + getType() + "\">");
			out.println("<ul>");

			Map<Date, List<IContentVisualComponent>> contentByDate = new HashMap<Date, List<IContentVisualComponent>>();
			for (MenuElement element : children) {
				contentByDate.putAll(element.getContentByDate(ctx));
			}
			logger.fine("date content size : " + contentByDate.size());
			Date date = getCloseToToDay(contentByDate);
			if (date != null) {
				out.println("<li class=\"date\">" + StringHelper.renderShortDate(ctx, date));
				out.println("<ul><li>");
				List<IContentVisualComponent> contentForDate = contentByDate.get(date);
				for (IContentVisualComponent contentVisualComponent : contentForDate) {
					out.println(contentVisualComponent.getXHTMLCode(ctx));
				}
				out.println("</li></ul>");
				if (contentForDate.size() > 0) {
					out.println("</li>");
				}
			} else {
				logger.warning("close to today date not found.");
			}

			out.println("</ul>");
			out.println("</div>");
		}
		out.close();
		return writer.toString();
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (getStyle(ctx).equals(ONLY_TODAY)) {
			return getViewXHTMLCodeToday(ctx);
		} else {
			ContentService content = ContentService.getInstance(ctx.getRequest());
			MenuElement agendaPage = content.getNavigation(ctx).searchChildFromName(getAgendaPageName());
			StringWriter writer = new StringWriter();
			PrintWriter out = new PrintWriter(writer);
			MenuElement[] children = agendaPage.getAllChildren();
			out.println("<div class=\"" + getType() + "\">");
			out.println("<ul>");
			for (MenuElement element : children) {
				Map<Date, List<IContentVisualComponent>> contentByDate = element.getContentByDate(ctx);
				Iterator<Date> dates = contentByDate.keySet().iterator();
				while (dates.hasNext()) {
					Date key = dates.next();
					if (dateMatch(key)) {
						out.println("<li class=\"date\">" + StringHelper.renderShortDate(ctx, key));
						List<IContentVisualComponent> contentForDate = contentByDate.get(key);
						out.println("<ul><li>");
						for (IContentVisualComponent contentVisualComponent : contentForDate) {
							out.println(contentVisualComponent.getXHTMLCode(ctx));
						}
						out.println("</li></ul></li>");
					}
				}
			}
			out.println("</ul>");
			out.println("</div>");
			out.close();
			return writer.toString();
		}
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

			String dateStr = StringHelper.renderTime(startDate) + VALUE_SEPARATOR + StringHelper.renderTime(endDate);
			if (!dateStr.equals(getValue())) {
				setValue(dateStr);
				setModify();
			}
		}
	}

	@Override
	protected Object getLock(ContentContext ctx) {
		return ctx.getGlobalContext().getLockLoadContent();
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

}
