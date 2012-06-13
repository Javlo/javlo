/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.links;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class WeekChildrenLink extends AbstractVisualComponent implements IAction {
	
	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(WeekChildrenLink.class.getName());
	
	public static final String createWeekPageName(String parentName, Date date) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(date);
		String year = "" + cal.get(Calendar.YEAR);
		return parentName+"_"+year + "_" + cal.get(Calendar.WEEK_OF_YEAR);
	}

	public static final String TYPE = "week-link";

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		
		out.println("<form id=\"week-select-"+getId()+"\" action=\""+URLHelper.createURL(ctx)+"\" method=\"post\">");
		
		out.println("<div id=\"calendarweek_0\" class=\"content_calendar\">");		
		
		out.println("<input type=\"hidden\" name=\"webaction\" value=\"week.goto\" />");
		
		out.println("<input type=\"hidden\" name=\"parent\" value=\""+getPage().getId()+"\" />");
		
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		out.println("<input id=\"dateweek_field_0\" type=\"text\" name=\"week\" value=\""+requestService.getParameter("week", "")+"\" />");
		
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		out.println("<input type=\"submit\" name=\"ok\" value=\""+i18nAccess.getViewText("global.ok")+"\" />");
	
		out.println("</div>");

		out.println("</form>");
		
		out.close();
		return writer.toString();
	}

	public String getType() {
		return TYPE;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		
	}

	@Override
	public String getActionGroupName() {
		return "week";
	}

	public static final String performGoto(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		String week = requestService.getParameter("week", null);
		//String parent = requestService.getParameter("parent", null);
		
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (week != null) {
			if (week.contains(":")) {
				week = week.substring(week.indexOf(':')+1);
			}
			if (week.contains("-")) {
				week = week.substring(0,week.indexOf('-'));
			}
			try {
				Date date = StringHelper.parseDate(week);				
				MenuElement rootPage = ContentService.createContent(request).getNavigation(ctx);
				
				MenuElement currentPage = null;//rootPage.searchChildFromId(parent);
				if (currentPage == null) {
					logger.warning("page not defined in request.");
					currentPage = ctx.getCurrentPage();
				}

				String pageName = createWeekPageName(currentPage.getName(), date);
				MenuElement weekPage = rootPage.searchChildFromName(pageName);
				if (weekPage != null) {
					response.sendRedirect(URLHelper.createURL(ctx, weekPage.getPath()));
					return null;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}			
		}
		
		MessageRepository msgRepo = MessageRepository.getInstance(ctx);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		msgRepo.setGlobalMessage(new GenericMessage(i18nAccess.getText("global.page-not-found"), GenericMessage.ERROR));
		
		return null;
	}
	
	@Override
	public boolean needJavaScript(ContentContext ctx) {
		return true;
	}
	

}
