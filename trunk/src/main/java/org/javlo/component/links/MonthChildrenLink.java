/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.links;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLNavigationHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class MonthChildrenLink extends AbstractVisualComponent implements IAction {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(MonthChildrenLink.class.getName());

	public static final String TYPE = "month-link";

	public static final String createMonthPageName(String parentName, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String year = "" + cal.get(Calendar.YEAR);
		String englishMonth = MacroHelper.getDisplayName(cal, Calendar.MONTH, MacroHelper.CALENDAR_LONG, Locale.ENGLISH);
		return parentName + "_" + year + "_" + englishMonth;
	}

	public static final String performGoto(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		String week = requestService.getParameter("week", null);
		String parent = requestService.getParameter("parent", null);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (week != null) {
			if (week.contains(":")) {
				week = week.substring(week.indexOf(':') + 1);
			}
			if (week.contains("-")) {
				week = week.substring(0, week.indexOf('-'));
			}
			try {
				Date date = StringHelper.parseDate(week);
				MenuElement rootPage = ContentService.createContent(request).getNavigation(ctx);

				MenuElement currentPage = rootPage.searchChildFromId(parent);
				if (currentPage == null) {
					logger.warning("page not defined in request.");
					currentPage = ctx.getCurrentPage();
				}

				String pageName = createMonthPageName(currentPage.getName(), date);
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
	public String getActionGroupName() {
		return "month";
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	protected Collection<MenuElement> getTargetPages(ContentContext ctx) throws Exception {
		ContentService.createContent(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();
		while (currentPage.getChildMenuElementsList().size() == 0 && currentPage.getParent() != null) {
			currentPage = currentPage.getParent();
		}
		return currentPage.getChildMenuElementsList();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		
		/*out.println("<a href=\""+ URLHelper.createURL(ctx)+"\">"+ URLHelper.createURL(ctx)+"</a>");
		out.println("<p>"+ ctx.getPath() + " cp:" + ctx.getCurrentPage().getPath() + "</p>");*/

		out.println("<form class=\"" + getType() + "\" id=\"month-select-" + getId() + "\" action=\"" + URLHelper.createURL(ctx) + "\" method=\"post\">");

		out.println("<div id=\"calendarmonth_0\" class=\"content_calendar\">");

		out.println("<input type=\"hidden\" name=\"webaction\" value=\"month.goto\" />");

		out.println("<input type=\"hidden\" name=\"parent\" value=\"" + getPage().getId() + "\" />");

		out.println(XHTMLNavigationHelper.renderComboNavigation(ctx, getTargetPages(ctx)));

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		out.println("<input type=\"submit\" name=\"ok\" value=\"" + i18nAccess.getViewText("global.ok") + "\" />");

		out.println("</div>");

		out.println("</form>");

		out.close();
		return writer.toString();
	}

	@Override
	public boolean needJavaScript(ContentContext ctx) {
		return true;
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {

	}

}
