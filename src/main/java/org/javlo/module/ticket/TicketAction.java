package org.javlo.module.ticket;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.internet.InternetAddress;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.actions.ViewActions;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.IMainModuleName;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.NotificationService;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

public class TicketAction extends AbstractModuleAction {

	private static final String LAST_NOTIFICATION_TIME = TicketAction.class.getName() + ".LAST";
	public static final String MODULE_NAME = IMainModuleName.TICKET;

	@Override
	public String getActionGroupName() {
		return "ticket";
	}

	public static Map<String, TicketUserWrapper> getMyTicket(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Map<String, TicketUserWrapper> myTickets = new HashMap<String, TicketUserWrapper>();

		String status = ctx.getRequest().getParameter("filter_status");

		Collection<TicketBean> tickets = TicketService.getAllTickets(ctx);

		if (globalContext.isMaster() && ctx.getCurrentEditUser() != null) {
			for (TicketBean ticket : tickets) {
				if (status == null || status.trim().length() == 0 || ticket.getStatus().equals(status)) {
					myTickets.put(ticket.getId(), new TicketUserWrapper(ticket, ctx));
				}
			}
		} else {
			for (TicketBean ticket : tickets) {
				if (ctx.getCurrentEditUser() != null && (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser()) || ticket.getAuthors().equals(ctx.getCurrentEditUser().getLogin())) &&  ticket.getContext() != null && ticket.getContext().equals(globalContext.getContextKey())) {
					if (status == null || status.trim().length() == 0 || ticket.getStatus().equals(status)) {
						myTickets.put(ticket.getId(), new TicketUserWrapper(ticket, ctx));
					}
				} else { // sharing ticket
					boolean sharing = false;
					if (ticket.getShare() != null) {
						if (ticket.getShare().equals("public")) {
							sharing = true;
						} else if (ticket.getShare().equals("")) {
							sharing = false;
						} else if (ticket.getShare().equals("site")) {
							sharing = globalContext.getContextKey().equals(ticket.getContext());
							if (sharing) {
								sharing = ctx.getCurrentEditUser() != null;
							}
						} else if (ticket.getShare().equals("allsites")) {
							sharing = ctx.getCurrentEditUser() != null;
						}
					}
					if (sharing) {
						if (status == null || status.trim().length() == 0 || ticket.getStatus().equals(status)) {
							myTickets.put(ticket.getId(), new TicketUserWrapper(ticket, ctx));
						}
					}
				}
			}
		}
		return myTickets;
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);

		RequestService rs = RequestService.getInstance(ctx.getRequest());

		Module ticketModule = modulesContext.getCurrentModule();
		Map<String, TicketUserWrapper> myTickets = getMyTicket(ctx);
		ctx.getRequest().setAttribute("tickets", myTickets.values());

		String ticketId = rs.getParameter("id", null);
		if (ticketId != null) {
			TicketService ticketService = TicketService.getInstance(ctx.getGlobalContext());
			TicketUserWrapper ticket;
			if (ticketId.equals("new")) {
				TicketBean newTicket = new TicketBean();
				newTicket.setAuthors(ctx.getCurrentUserId());
				newTicket.setContext(ctx.getGlobalContext().getContextKey());
				newTicket.setUrl(URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE)));
				ticketService.updateTicket(ctx, newTicket);
				rs.setParameter("id", newTicket.getId());
				ctx.getRequest().setAttribute("newTicket", true);
				ticket = new TicketUserWrapper(newTicket, ctx);
			} else {
				ticket = myTickets.get(ticketId);
			}
			if (ticket != null && rs.getParameter("back", null) == null && !StringHelper.isTrue(ctx.getRequest().getAttribute("back-list"))) {
				ticket.onRead(ctx.getCurrentEditUser().getLogin());
				ticketService.updateTicket(ctx, new TicketBean(ticket));
				ctx.getRequest().setAttribute("ticket", ticket);
				if (ticketModule.getBox("main") == null) {
					ticketModule.addMainBox("main", "update ticket : " + rs.getParameter("id", ""), "/jsp/update_ticket.jsp", true);
					ticketModule.setRenderer(null);
				}
				ctx.getRequest().setAttribute("noFilter", true);
			} else {
				ticketModule.setRenderer("/jsp/list.jsp");
				ticketModule.restoreBoxes();
			}
		} else {
			ticketModule.setRenderer("/jsp/list.jsp");
			ticketModule.restoreBoxes();
		}

		UserFactory userFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		ctx.getRequest().setAttribute("ticketAvailableUsers", userFactory.getUserInfoList());

		return msg;
	}

	public static String performUpdate(RequestService rs, ContentContext ctx, GlobalContext globalContext, User user, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		if (rs.getParameter("back", null) != null) {
			return null;
		}

		TicketService ticketService = TicketService.getInstance(globalContext);
		String id = rs.getParameter("id", "");
		String title = rs.getParameter("title", null);
		
		TicketBean ticket;
		if (id.trim().length() > 0) { // update
			if ("new".equals(id)) {
				ticket = new TicketBean();
			} else {
				TicketUserWrapper existing = getMyTicket(ctx).get(id);
				if (existing == null) {					
					return "ticket not found : " + id;
				}
				ticket = new TicketBean(existing);
				ticket.setLastUpdateDate(new Date());
				ticket.setLatestEditor(user.getLogin());
			}
		} else { // create
			if (rs.getParameter("title", "").trim().length() == 0) {				
				return "please enter a title.";
			}
			ticket = new TicketBean();
			ticket.setAuthors(user.getLogin());
			ticket.setContext(globalContext.getContextKey());
		}
		
		if (StringHelper.isAllEmpty(title, ticket.getTitle())) {
			ctx.getRequest().setAttribute("newTicket", true);
			return i18nAccess.getText("ticket.message.no-title");
		}

		if (rs.getParameter("delete", null) != null) {
			ticket.setDeleted(true);
		} else {
			ticket.setTitle(rs.getParameter("title", ticket.getTitle()));
			ticket.setPriority(Integer.parseInt(rs.getParameter("priority", "" + ticket.getPriority())));
			ticket.setCategory(rs.getParameter("category", ticket.getCategory()));
			ticket.setMessage(rs.getParameter("message", ticket.getMessage()));
			ticket.setStatus(rs.getParameter("status", ticket.getStatus()));
			ticket.setShare(rs.getParameter("share", ticket.getShare()));
			ticket.setUrl(rs.getParameter("url", ticket.getUrl()));
			if (!StringHelper.isEmpty(title)) {
				ticket.setTitle(title);
			}
			ticket.setMessage(rs.getParameter("message", ticket.getMessage()));
			ticket.setUsers(rs.getParameterListValues("users", Collections.<String> emptyList()));
			if (ticket.getUsers() == null || ticket.getUsers().isEmpty()) {
				AdminUserFactory userFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
				List<String> users = new LinkedList<String>();
				for (IUserInfo info : userFactory.getUserInfoForRoles(new String[] { AdminUserSecurity.FULL_CONTROL_ROLE })) {
					users.add(info.getLogin());
				}
				ticket.setUsers(users);
			}
			if (rs.getParameter("comment", "").trim().length() > 0) {
				ticket.addComments(new Comment(user.getLogin(), rs.getParameter("comment", "")));
			} else {
				ctx.getRequest().setAttribute("back-list", true);
			}
		}
		ticket.onUpdate(user.getLogin());
		ticketService.updateTicket(ctx, ticket);
		
		messageRepository.addMessage(new GenericMessage("ticket updated.", GenericMessage.INFO));
		NotificationService.getInstance(globalContext).notifExternalService(ctx, ticket.getTitle(), GenericMessage.INFO, ticket.getUrl(), ticket.getAuthors(), false, ticket.getUsers());
		
		return null;
	}

	/**
	 * Send notifications for tickets modified since the last notification time
	 * ONLY if there is no modification since 5 min (defined in
	 * {@link StaticConfig#getTimeBetweenChangeNotification()}) Not a webaction,
	 * called from
	 * {@link ViewActions#performSendTicketChangeNotifications(ContentContext, GlobalContext)}
	 * 
	 * @param ctx
	 * @param globalContext
	 * @return null
	 */
	public static String computeChangesAndSendNotifications(ContentContext ctx, GlobalContext globalContext) {
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.add(Calendar.SECOND, -globalContext.getStaticConfig().getTimeBetweenChangeNotification());
		Date inProgressTime = cal.getTime();
		Date lastNotificationTime = (Date) globalContext.getAttribute(LAST_NOTIFICATION_TIME);
		if (lastNotificationTime == null) {
			lastNotificationTime = now;
			globalContext.setAttribute(LAST_NOTIFICATION_TIME, lastNotificationTime);
		}
		try {
			List<TicketBean> toNotify = new LinkedList<TicketBean>();
			List<TicketBean> tickets = TicketService.getAllTickets(ctx);
			for (TicketBean ticket : tickets) {
				if (globalContext.getContextKey().equals(ticket.getContext())) {
					Date mod = ticket.getLastUpdateDate();
					if (mod == null) {
						mod = ticket.getCreationDate();
					}
					if (mod != null) {
						if (mod.after(inProgressTime)) {
							return null; // Work in progress detected:
											// notification canceled
						}
						if (mod.after(lastNotificationTime)) {
							toNotify.add(ticket);
						}
					}
				}
			}
			lastNotificationTime = inProgressTime; // Should be equivalent to
													// "now" because no change
													// between the 2 dates
			globalContext.setAttribute(LAST_NOTIFICATION_TIME, lastNotificationTime);
			if (!toNotify.isEmpty()) {
				sendTicketSummaryNotification(ctx, toNotify);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	private static void sendTicketSummaryNotification(ContentContext ctx, List<TicketBean> tickets) throws Exception {
		AdminUserFactory userFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		Map<String, List<TicketBean>> ticketsByUser = new HashMap<String, List<TicketBean>>();
		for (TicketBean ticket : tickets) {
			for (String user : ticket.getUsers()) {
				List<TicketBean> list = ticketsByUser.get(user);
				if (list == null) {
					list = new LinkedList<TicketBean>();
					ticketsByUser.put(user, list);
				}
				list.add(ticket);
			}
		}
		for (Entry<String, List<TicketBean>> entry : ticketsByUser.entrySet()) {
			sendUserTicketSummaryNotification(ctx, userFactory, entry.getKey(), entry.getValue());
		}
	}

	private static void sendUserTicketSummaryNotification(ContentContext ctx, AdminUserFactory userFactory, String userLogin, List<TicketBean> tickets) throws Exception {
		GlobalContext globalContext = ctx.getGlobalContext();
		IUserInfo userInfo = userFactory.getUserInfos(userLogin);
		if (userInfo != null) {
			String email = StringHelper.trimAndNullify(userInfo.getEmail());
			if (email != null) {
				String siteTitle = ctx.getGlobalContext().getGlobalTitle();				
				String baseUrl = URLHelper.createInterModuleURL(ctx.getContextForAbsoluteURL().getContextWithOtherRenderMode(ContentContext.EDIT_MODE), "/", "ticket");
				Map ticketsMap = new HashMap();
				for (TicketBean ticket : tickets) {	
					if (ticket.getComments().size() == 0) {
						ticketsMap.put("["+ticket.getAuthors()+"] - "+ticket.getTitle(), XHTMLHelper.collectionToList(ticket.getComments()));
					} else {
						ticketsMap.put("["+ticket.getAuthors()+"] - "+ticket.getTitle(), XHTMLHelper.collectionToList(ticket.getComments()));
					}
				}				
				String content = XHTMLHelper.createAdminMail("Ticket updates on " + siteTitle, ticketsMap.size()+" tickets updated.", ticketsMap, baseUrl, "go on site");				
				NetHelper.sendXHTMLMail(ctx, new InternetAddress(globalContext.getAdministratorEmail()), new InternetAddress(email), null, null, "Ticket updates on " + siteTitle, content.toString(), null);
				
			}
		}
	}

}
