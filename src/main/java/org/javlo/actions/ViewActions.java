/**
 * Created on Aug 13, 2003
 */
package org.javlo.actions;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.PaginationContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.MailingBuilder;
import org.javlo.module.ticket.TicketAction;
import org.javlo.navigation.MenuElement;
import org.javlo.rendering.Device;
import org.javlo.service.ContentService;
import org.javlo.service.visitors.VisitorsMessageService;
import org.javlo.template.Template;

/**
 * @author pvandermaesen list of actions for search in cms.
 */
public class ViewActions implements IAction {

	private static final String CHANGES_NOTIFICATION_TIME_A = ViewActions.class.getName() + ".TIME_A";
	private static final String CHANGES_NOTIFICATION_TIME_B = ViewActions.class.getName() + ".TIME_B";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ViewActions.class.getName());

	public static String performLanguage(HttpServletRequest request, HttpServletResponse response) {
		String msg = null;

		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			GlobalContext globalContext = GlobalContext.getInstance(request);
			String lang = request.getParameter("lg");			
			if (lang != null) {				
				if (globalContext.getLanguages().contains(lang) && !StringHelper.isTrue(request.getParameter("content"))) {
					ctx.setAllLanguage(lang);
					ctx.setCookieLanguage(lang);
					I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
					i18nAccess.changeViewLanguage(ctx);
					String url = URLHelper.createURL(ctx);
					NetHelper.sendRedirectTemporarily(response, url);
				} else if (globalContext.getContentLanguages().contains(lang)) {
					ctx.setContentLanguage(lang);
					ctx.setCookieLanguage(lang);
					I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
					i18nAccess.changeViewLanguage(ctx);
					String url = URLHelper.createURL(ctx);
					NetHelper.sendRedirectTemporarily(response, url);					
				}
			}
		} catch (Exception e) {
			msg = e.getMessage();
		}

		return msg;
	}

	public static String performReloadconfig(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("reload config");
		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession().getServletContext());
		staticConfig.reload();
		GlobalContext globalContext = GlobalContext.getInstance(request);
		globalContext.reload();
		return null;
	}

	public static String performUnabledalternativetemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ctx.getCurrentTemplate().enabledAlternativeTemplate(ctx);

		GlobalContext globalContext = GlobalContext.getInstance(request);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		i18nAccess.requestInit(ctx); // reload template i18n

		return null;
	}

	public static String performDisabledalternativetemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);

		GlobalContext globalContext = GlobalContext.getInstance(request);

		Template template = ctx.getCurrentTemplate();
		template.disabledAlternativeTemplate(ctx);

		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		i18nAccess.requestInit(ctx); // reload template i18n

		return null;
	}

	public static String performFrontCache(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String key = request.getParameter("key");
		String value = request.getParameter("value");
		String renderer = request.getParameter("renderer");
		if (key != null && value != null) {
			value = value.replace("<", "&lt;").replace(">", "&gt;");
			globalContext.putItemInFrontCache(ctx, key, value, renderer);
		}
		return null;
	}

	public static String performForcedefaultdevice(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Device device = Device.getDevice(ContentContext.getContentContext(request, response));
		device.forceDefault();
		logger.info("force default device : " + device.getCode());
		return null;
	}

	public static String performPagination(HttpServletRequest request, HttpServletResponse response) throws Exception {
		PaginationContext paginationContext = PaginationContext.getInstance(request, request.getParameter("key"));
		if (paginationContext != null) {
			logger.fine("new page selected : " + paginationContext.getPage());
		}
		return null;
	}

	/**
	 * Send notifications for pages modified between TimeA and TimeB. The timeline is TimeB -> TimeA -> Now .
	 * <br/>After the process: TimeA become TimeB and Now become TimeA. 
	 * @param ctx
	 * @param globalContext
	 * @param content
	 * @return
	 */
	public static String performCheckChangesAndNotify(ContentContext ctx, GlobalContext globalContext, ContentService content) {
		if (!globalContext.isCollaborativeMode()) {
			return "Collaborative mode not enabled.";
		}
		Date now = new Date();
		Date timeA = (Date) globalContext.getAttribute(CHANGES_NOTIFICATION_TIME_A);
		Date timeB = (Date) globalContext.getAttribute(CHANGES_NOTIFICATION_TIME_B);
		if (timeA == null || timeB == null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(now);
			int interval = globalContext.getStaticConfig().getTimeBetweenChangeNotification();
			cal.add(Calendar.SECOND, -interval);
			timeA = cal.getTime();
			cal.add(Calendar.SECOND, -interval);
			timeB = cal.getTime();
		}
		try {
			for (MenuElement page : content.getNavigation(ctx).getAllChildren()) {				
				if (page.isChangeNotification()) {
					Date mod = page.getModificationDate();
					if (mod != null && mod.after(timeB) && !mod.after(timeA)) {
						sendPageChangeNotification(ctx, page);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		timeB = timeA;
		timeA = now;
		globalContext.setAttribute(CHANGES_NOTIFICATION_TIME_A, timeA);
		globalContext.setAttribute(CHANGES_NOTIFICATION_TIME_B, timeB);

		return null;
	}
	private static void sendPageChangeNotification(ContentContext ctx, MenuElement page) throws Exception {
		ctx = ctx.getContextOnPage(page);
		GlobalContext globalContext = ctx.getGlobalContext();
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		MailingBuilder mb = new MailingBuilder();
		mb.setSender(globalContext.getAdministratorEmail());
		mb.setEditorGroups(new LinkedList<String>(page.getEditorRolesAndParent()));
		mb.getExcludedUsers().add(page.getLatestEditor());		
		mb.setSubject(i18nAccess.getText("collaborative.mail.modified", "Page modified: ") + page.getTitle(ctx));
		mb.prepare(ctx);
		mb.sendMailing(ctx);
	}

	public static String performSendTicketChangeNotifications(ContentContext ctx, GlobalContext globalContext) {
		return TicketAction.computeChangesAndSendNotifications(ctx, globalContext);
	}

	@Override
	public String getActionGroupName() {
		return "view";
	}
	
	public static String performAcceptCookies(ContentContext ctx) throws Exception {
		VisitorsMessageService.getInstance(ctx.getRequest().getSession()).markAsDisplayed("cookies");
		Cookie cookie = new Cookie(ctx.getCurrentTemplate().getCookiesMessageName(), "1");		
		String path = ctx.getCurrentTemplate().getCookiesMessagePath();
		if (path == null) {
			path = URLHelper.createStaticURL(ctx,"/");
		}		
		cookie.setPath(path);
		cookie.setMaxAge(60*60*24*365); // 1 year
		ctx.getResponse().addCookie(cookie);
		return null;
	}
	

}
