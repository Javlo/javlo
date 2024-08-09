/**
 * Created on Aug 13, 2003
 */
package org.javlo.actions;

import java.io.File;
import java.io.FileFilter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.mail.internet.InternetAddress;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.filefilter.FileFileFilter;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.PaginationContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.io.SessionFolder;
import org.javlo.mailing.MailingBuilder;
import org.javlo.message.MessageRepository;
import org.javlo.module.file.FileBean;
import org.javlo.module.file.FileModuleContext;
import org.javlo.module.ticket.TicketAction;
import org.javlo.navigation.MenuElement;
import org.javlo.rendering.Device;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.visitors.CookiesService;
import org.javlo.service.visitors.VisitorsMessageService;
import org.javlo.servlet.IVersion;
import org.javlo.template.Template;
import org.javlo.user.UserFactory;
import org.javlo.ztatic.FileCache;
import org.javlo.ztatic.StaticInfo;

/**
 * @author pvandermaesen list of actions for search in cms.
 */
public class ViewActions implements IAction {

	private static final String CHANGES_NOTIFICATION_TIME_A = ViewActions.class.getName() + ".TIME_A";
	private static final String CHANGES_NOTIFICATION_TIME_B = ViewActions.class.getName() + ".TIME_B";
	
	private static final String CHANGES_NOTIFICATION_DYNAMIC_COMPONENT_TIME_A = ViewActions.class.getName() + ".TIME_DC_A";
	private static final String CHANGES_NOTIFICATION_DYNAMIC_COMPONENT_TIME_B = ViewActions.class.getName() + ".TIME_DC_B";
	
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
	
	public static String performTest(HttpServletRequest request, HttpServletResponse response, RequestService rs, ContentContext ctx) throws Exception {
		System.out.println("***************");
		System.out.println("*** TEST ACTION");
		System.out.println("*** #params : "+rs.getParameterMap().size());
		System.out.println("*** version : "+IVersion.VERSION);
		System.out.println("*** uri     : "+request.getRequestURI());
		System.out.println("***************");
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
			for (MenuElement page : content.getNavigation(ctx).getAllChildrenList()) {				
				if (page.getFollowers(ctx).size() > 0) {					
					Date mod = page.getModificationDate(ctx);
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
	
	/**
	 * Send notifications for pages modified between TimeA and TimeB. The timeline is TimeB -> TimeA -> Now .
	 * <br/>After the process: TimeA become TimeB and Now become TimeA. 
	 * @param ctx
	 * @param globalContext
	 * @param content
	 * @return
	 */
	public static String performCheckChangesAndNotifyDynamicComponent(ContentContext ctx, GlobalContext globalContext, ContentService content) {		
		
		Date now = new Date();
		Date timeA = (Date) globalContext.getAttribute(CHANGES_NOTIFICATION_DYNAMIC_COMPONENT_TIME_A);
		Date timeB = (Date) globalContext.getAttribute(CHANGES_NOTIFICATION_DYNAMIC_COMPONENT_TIME_B);
		if (timeA == null || timeB == null) {			
			Calendar cal = Calendar.getInstance();
			cal.setTime(now);
			int interval = globalContext.getStaticConfig().getTimeBetweenChangeNotificationForDynamicComponent();
			cal.add(Calendar.SECOND, -interval);
			timeA = cal.getTime();
			cal.add(Calendar.SECOND, -interval);
			timeB = cal.getTime();
		}
		try {
			
			Map<String,String> mailData = new HashMap<String,String>();
			
			String type = null;
			for (IContentVisualComponent comp : ComponentFactory.getAllComponentsFromContext(ctx)) {
				if (comp instanceof DynamicComponent) {					
					DynamicComponent dcomp = (DynamicComponent)comp;
					if (dcomp.isNotififyCreation(ctx)) {
						type = dcomp.getType();
						String pageName = dcomp.getNotififyPageName(ctx);
						ContentService contentService = ContentService.getInstance(ctx.getRequest());
						MenuElement displayPage = contentService.getNavigation(ctx).searchChildFromName(pageName);
						if (displayPage != null) {
							String url = URLHelper.createURL(ctx.getContextForAbsoluteURL(), displayPage);
							url = URLHelper.addParam(url, "id", dcomp.getId());
							url = URLHelper.addParam(url, "edit", "1");
							mailData.put(dcomp.getLabel(ctx), "<a href=\""+url+"\">"+dcomp.getTextTitle(ctx)+"</a>");	
						} else {
							String url = URLHelper.createURL(ctx.getContextForAbsoluteURL());
							url = URLHelper.addParam(url, "_only_component", dcomp.getId());
							mailData.put(dcomp.getLabel(ctx), "<a href=\""+url+"\">"+dcomp.getTextTitle(ctx)+"</a>");
						}						
					}
				}
			}			
			
			if (type != null) {
				String subject = ctx.getGlobalContext().getGlobalTitle()+" : new "+type;
				String mail = XHTMLHelper.createAdminMail(null , "new "+type+" created, please click on the link to visual it", mailData, URLHelper.createAbsoluteURL(ctx, "/"), ctx.getGlobalContext().getGlobalTitle(), null);
				InternetAddress email = new InternetAddress(ctx.getGlobalContext().getAdministratorEmail());
				NetHelper.sendMail(globalContext,email,email, null, null, subject, mail, null, true);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		timeB = timeA;
		timeA = now;
		globalContext.setAttribute(CHANGES_NOTIFICATION_DYNAMIC_COMPONENT_TIME_A, timeA);
		globalContext.setAttribute(CHANGES_NOTIFICATION_DYNAMIC_COMPONENT_TIME_B, timeB);

		return null;
	}
	
	private static void sendPageChangeNotification(ContentContext ctx, MenuElement page) throws Exception {		
		ctx = ctx.getContextOnPage(page);
		GlobalContext globalContext = ctx.getGlobalContext();
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		MailingBuilder mb = new MailingBuilder();
		mb.setSender(globalContext.getAdministratorEmail());
		//mb.setEditorGroups(new LinkedList<String>(page.getEditorRolesAndParent()));
		mb.setAllRecipients(UserFactory.userListAsInternetAddressList(ctx, page.getFollowers(ctx)));
		mb.getExcludedUsers().add(page.getLatestEditor());		
		mb.setSubject(i18nAccess.getText("collaborative.mail.modified", "Page modified: ") + page.getTitle(ctx));
		//mb.prepare(ctx);
		mb.sendMailing(ctx);
	}

	public static String performSendTicketChangeNotifications(ContentContext ctx, GlobalContext globalContext) {		
		String outMsg = TicketAction.computeOpenAndSendNotifications(ctx, globalContext);
		if (outMsg == null) {
			outMsg = TicketAction.computeChangesAndSendNotifications(ctx, globalContext);
		}		
		return outMsg;
	}

	@Override
	public String getActionGroupName() {
		return "view";
	}
	
	public static String performAcceptCookies(ContentContext ctx) throws Exception {
		if (ctx.getCurrentTemplate() == null) {
			return "template not found.";
		}
		VisitorsMessageService.getInstance(ctx.getRequest().getSession()).markAsDisplayed("cookies");
		Cookie cookie = new Cookie(ctx.getCurrentTemplate().getCookiesMessageName(), "1");
		cookie.setPath("/");
		cookie.setMaxAge(60*60*24*365); // 1 year
		ctx.getResponse().addCookie(cookie);		
		CookiesService.getInstance(ctx).setAccepted(true);
		return null;
	}
	
	public static String performAcceptCookiesType(ContentContext ctx, RequestService rs) throws Exception {
		if (ctx.getCurrentTemplate() == null) {
			return "template not found.";
		}
		
		if (StringHelper.isTrue(rs.getParameter("accept"))) {
			return performAcceptCookies(ctx);
		}
		
		if (StringHelper.isTrue(rs.getParameter("refuse"))) {
			return performRefuseCookies(ctx);
		}
		
		List<String> acceptedType = new LinkedList<>();
		for (String type : CookiesService.COOKIES_TYPES) {
			if (rs.getParameter("cookies_"+type) != null) {
				acceptedType.add(type);
			}
		}
		
		VisitorsMessageService.getInstance(ctx.getRequest().getSession()).markAsDisplayed("cookies");
		
		Cookie cookie = new Cookie(ctx.getCurrentTemplate().getCookiesTypeName(), StringHelper.collectionToString(acceptedType, "-"));		
		cookie.setPath("/");
		cookie.setMaxAge(60*60*24*365); // 1 year
		ctx.getResponse().addCookie(cookie);
		CookiesService.getInstance(ctx).setAcceptedTypes(acceptedType);
		return null;
	}
	
	public String performUpdateFocus(RequestService rs, ContentContext ctx, GlobalContext globalContext, FileModuleContext fileModuleContext, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {
		File folder = null;
		if (rs.getParameter("image_path", null) != null) {
			folder = new File(globalContext.getDataFolder(), rs.getParameter("image_path", null));
		}
		boolean found = false;
		String latestFileName = "";
		FileBean latestFileBean = null;
		if (folder.exists()) {
			for (File file : folder.listFiles((FileFilter) FileFileFilter.FILE)) {
				latestFileName = file.getAbsolutePath();
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
				FileBean fileBean = new FileBean(ctx, staticInfo);
				latestFileBean = fileBean;
				String newFocusX = rs.getParameter("posx-" + fileBean.getId(), null);
				String newFocusY = rs.getParameter("posy-" + fileBean.getId(), null);
				if (newFocusX != null && newFocusY != null) {

					SessionFolder sessionFolder = SessionFolder.getInstance(ctx);
					if (sessionFolder.getImage() == null || !sessionFolder.getImage().equals(file)) {
						return "securtiy error.";
					}

					found = true;
					staticInfo.setFocusZoneX(ctx, (int) Math.round(Double.parseDouble(newFocusX)));
					staticInfo.setFocusZoneY(ctx, (int) Math.round(Double.parseDouble(newFocusY)));
					if (ctx.isAsViewMode()) {
						ContentContext editContext = ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE);
						staticInfo.setFocusZoneX(editContext, (int) Math.round(Double.parseDouble(newFocusX)));
						staticInfo.setFocusZoneY(editContext, (int) Math.round(Double.parseDouble(newFocusY)));
					}
					PersistenceService.getInstance(globalContext).setAskStore(true);

					// messageRepository.setGlobalMessageAndNotification(ctx,
					// new
					// GenericMessage(i18nAccess.getText("file.message.updatefocus",
					// new String[][] { { "file", file.getName() } }),
					// GenericMessage.INFO));

					FileCache fileCache = FileCache.getInstance(ctx.getRequest().getSession().getServletContext());
					fileCache.delete(ctx, file.getName());
				}
			}
			if (!found) {
				return "focus technical error - file not found : " + (latestFileBean != null ? latestFileBean.getId() : "no bean") + " - " + latestFileName;
			}
			return null;
		} else {
			return "folder not found : " + folder;
		}
	}
	
	public static String performRefuseCookies(ContentContext ctx) throws Exception {
		VisitorsMessageService.getInstance(ctx.getRequest().getSession()).markAsDisplayed("cookies");
		Cookie cookie = new Cookie(ctx.getCurrentTemplate().getCookiesMessageName(), "0");		
//		String path = ctx.getCurrentTemplate().getCookiesMessagePath();
//		if (path == null) {
//			path = URLHelper.createStaticURL(ctx,"/");
//		}		
		cookie.setPath("/");
		cookie.setMaxAge(60*60*24*30); // 30 days
		ctx.getResponse().addCookie(cookie);
		CookiesService.getInstance(ctx).setAccepted(false);
		return null;
	}
	
	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return true;
	}
	
	public static void main(String[] args) {
		Locale locale = new Locale("sv");
		System.out.println("##### ViewActions.main : locale = "+locale.getDisplayLanguage(Locale.FRENCH)); //TODO: remove debug trace
	}
	

}
