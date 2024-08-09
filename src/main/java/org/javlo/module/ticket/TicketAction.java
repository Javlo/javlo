package org.javlo.module.ticket;

import jakarta.mail.internet.InternetAddress;
import org.apache.commons.fileupload2.core.FileItem;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.actions.ViewActions;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.*;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.ImageEngine;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.IMainModuleName;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.service.notification.NotificationService;
import org.javlo.user.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

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
		} else if (ctx.getCurrentEditUser() != null) {
			for (TicketBean ticket : tickets) {
				boolean toCurrentUser = ticket.getUsers().contains(ctx.getCurrentEditUser().getLogin());
				if (ctx.getCurrentEditUser() != null && (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser()) || ticket.getAuthors().equals(ctx.getCurrentEditUser().getLogin()) || toCurrentUser) && ticket.getContext() != null && ticket.getContext().equals(globalContext.getContextKey())) {
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
				ticketService.updateTicket(ctx, newTicket, false);
				rs.setParameter("id", newTicket.getId());
				ctx.getRequest().setAttribute("newTicket", true);
				ticket = new TicketUserWrapper(newTicket, ctx);
			} else {
				ticket = myTickets.get(ticketId);
			}
			if (ticket != null && rs.getParameter("back", null) == null && !StringHelper.isTrue(ctx.getRequest().getAttribute("back-list"))) {
				ticket.onRead(ctx.getCurrentEditUser().getLogin());
				ticketService.updateTicket(ctx, new TicketBean(ticket), false);
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
	
	public static String performUpload(RequestService rs, ContentContext ctx, GlobalContext globalContext, User user, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		InputStream in = null;
		for (FileItem item : rs.getAllFileItem()) {
			JavaScriptBlob blob = new JavaScriptBlob(new String(item.get()));
			in = new ByteArrayInputStream(blob.getData());
		}
		if (in != null) {
			BufferedImage img = ImageIO.read(in);
			in.close();
			in=null;
			File imageFile = TicketService.getTempImageFile(ctx);
			imageFile.getParentFile().mkdirs();
			imageFile.createNewFile();
			img = ImageEngine.trim(img, Color.WHITE, 1);
			ImageEngine.storeImage(img, imageFile);
		}
		//ImageIO.write(img, "jpg", imageFile);
		return null;
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
		
		/** Business validation **/
		if (StringHelper.isDigit(rs.getParameter("proposition"))) {
			ticket.setPrice(Long.parseLong(rs.getParameter("proposition")));
			ticket.setBstatus(Ticket.BSTATUS_ASK);
		}
		
		if (ctx.getCurrentUser().isCustomer()) {
			if (rs.getParameter("bvalid","").equals("yes")) {
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("ticket.business.accepted"), GenericMessage.SUCCESS));
				ticket.setBstatus(Ticket.BSTATUS_VALIDED);
			} else if (rs.getParameter("bvalid","").equals("no")) {
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("ticket.business.refused"), GenericMessage.SUCCESS));
				ticket.setBstatus(Ticket.BSTATUS_REJECTED);
			}
		}

		if (rs.getParameter("delete", null) != null) {
			ticket.setDeleted(true);
		} else {
			ticket.setTitle(rs.getParameter("title", ticket.getTitle()));
			ticket.setPriority(Integer.parseInt(rs.getParameter("priority", "" + ticket.getPriority())));
			ticket.setCategory(rs.getParameter("category", ticket.getCategory()));
			if (!StringHelper.isEmpty(rs.getParameter("message"))) {
				ticket.setMessage(rs.getParameter("message", ticket.getMessage()));
			}
			ticket.setStatus(rs.getParameter("status", ticket.getStatus()));
			ticket.setShare(rs.getParameter("share", ticket.getShare()));
			ticket.setUrl(rs.getParameter("url", ticket.getUrl()));
			if (!StringHelper.isEmpty(title)) {
				ticket.setTitle(title);
			}
			ticket.setMessage(rs.getParameter("message", ticket.getMessage()));
			ticket.setUsers(rs.getParameterListValues("users", Collections.<String>emptyList()));
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
		ticketService.updateTicket(ctx, ticket, true);

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
				sendTicketSummaryNotification(ctx, toNotify, null, true);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

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
	public static String computeOpenAndSendNotifications(ContentContext ctx, GlobalContext globalContext) {
		Calendar now = Calendar.getInstance();		
		Calendar latestDate = globalContext.getLatestTicketNotificaitonTime();
		if (now.get(Calendar.HOUR_OF_DAY) > ctx.getGlobalContext().getStaticConfig().getNotificationHours() && (latestDate==null || now.get(Calendar.DAY_OF_YEAR) != latestDate.get(Calendar.DAY_OF_YEAR))) {
			globalContext.setLatestTicketNotificaitonTime(now);
			try {
				List<TicketBean> toNotify = new LinkedList<TicketBean>();
				List<TicketBean> tickets = TicketService.getAllTickets(ctx);
				for (TicketBean ticket : tickets) {
					if (globalContext.getContextKey().equals(ticket.getContext())) {						
						if (ticket.isOpen()) {
							toNotify.add(ticket);
						} 
					}
				}				
				if (!toNotify.isEmpty()) {
					sendTicketSummaryNotification(ctx, toNotify, "Tickets open on : "+globalContext.getGlobalTitle(), false);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return null;
	}
	
	private static void sendTicketSummaryNotification(ContentContext ctx, List<TicketBean> tickets, String subject, boolean reply) throws Exception {
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
			if (reply && StringHelper.isMail(ticket.getAuthors())) {
				List<TicketBean> list = ticketsByUser.get(ticket.getAuthors());
				if (list == null) {
					list = new LinkedList<TicketBean>();
					ticketsByUser.put(ticket.getAuthors(), list);
				}
				list.add(ticket);
			}
		}
		for (Entry<String, List<TicketBean>> entry : ticketsByUser.entrySet()) {
			sendUserTicketSummaryNotification(ctx, userFactory, entry.getKey(), entry.getValue(), subject);
		}
	}

	private static void sendUserTicketSummaryNotification(ContentContext ctx, AdminUserFactory userFactory, String userLogin, List<TicketBean> tickets, String subject) throws Exception {
		GlobalContext globalContext = ctx.getGlobalContext();
		IUserInfo userInfo = userFactory.getUserInfos(userLogin);
		if (userInfo != null) {
			String email = StringHelper.trimAndNullify(userInfo.getEmail());
			if (email != null) {
				String siteTitle = ctx.getGlobalContext().getGlobalTitle();
				String baseUrl = URLHelper.createInterModuleURL(ctx.getContextForAbsoluteURL().getContextWithOtherRenderMode(ContentContext.EDIT_MODE), "/", "ticket");
				Map ticketsMap = new HashMap();
				for (TicketBean ticket : tickets) {
					String messageHTML = "";
					String url = URLHelper.createAbsoluteURL(ctx.getContextForAbsoluteURL(), ticket.getUrl());
					messageHTML = "<div class=\"message\">" + ticket.getAuthors() + " : " + "<a href=\"" + url + "\">" + XHTMLHelper.textToXHTML(ticket.getTitle()) + "</a></div>";
					String key = ticket.getLastUpdateDateLabel() + " [" + ticket.getStatus() + ']';
					if (ticket.getComments().size() == 0) {
						ticketsMap.put(key, messageHTML);
					} else {
						ticketsMap.put(key, messageHTML + XHTMLHelper.collectionToList(ticket.getComments()));
					}
				}
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				String sujectPrefix = i18nAccess.getText("ticket.subject");
				String ticketUpdate = i18nAccess.getText("ticket.update");
				String goOnSite = i18nAccess.getText("ticket.go");
				String content = XHTMLHelper.createAdminMail(subject != null?subject:sujectPrefix + ' ' + siteTitle, ticketsMap.size() + "&nbsp;" + ticketUpdate, ticketsMap, baseUrl, goOnSite, null);
				NetHelper.sendXHTMLMail(ctx, new InternetAddress(globalContext.getAdministratorEmail()), new InternetAddress(email), null, null, ticketUpdate + ' ' + siteTitle, content.toString(), null);

			}
		}
	}

}
