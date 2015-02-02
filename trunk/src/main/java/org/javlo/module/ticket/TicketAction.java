package org.javlo.module.ticket;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

public class TicketAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "ticket";
	}

	public static Map<String, TicketBean> getMyTicket(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Map<String, TicketBean> myTickets = new HashMap<String, TicketBean>();

		String status = ctx.getRequest().getParameter("filter_status");

		Collection<TicketBean> tickets = TicketService.getAllTickets(ctx);

		if (globalContext.isMaster() && ctx.getCurrentEditUser() != null) {
			for (TicketBean ticket : tickets) {
				if (status == null || status.trim().length() == 0 || ticket.getStatus().equals(status)) {
					myTickets.put(ticket.getId(), ticket);
				}
			}
		} else {
			for (TicketBean ticket : tickets) {
				if (ctx.getCurrentEditUser() != null && (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser()) || ticket.getAuthors().equals(ctx.getCurrentEditUser().getLogin())) && ticket.getContext().equals(globalContext.getContextKey())) {
					if (status == null || status.trim().length() == 0 || ticket.getStatus().equals(status)) {
						myTickets.put(ticket.getId(), ticket);
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
							myTickets.put(ticket.getId(), ticket);
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
		Map<String, TicketBean> myTickets = getMyTicket(ctx);
		ctx.getRequest().setAttribute("tickets", myTickets.values());

		TicketBean ticket = myTickets.get(rs.getParameter("id", null));
		if (ticket != null && rs.getParameter("back", null) == null && !StringHelper.isTrue(ctx.getRequest().getAttribute("back-list"))) {
			if (ticket != null && ticket.getAuthors().equals(ctx.getCurrentEditUser().getLogin())) {
				ticket.setRead(true);
			}
			ctx.getRequest().setAttribute("ticket", ticket);
			if (ticketModule.getBox("main") == null) {
				ticketModule.addMainBox("main", "update ticket : " + rs.getParameter("id", ""), "/jsp/update_ticket.jsp", true);
				ticketModule.setRenderer(null);
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
		TicketBean ticket;
		if (id.trim().length() > 0) { // update
			ticket = getMyTicket(ctx).get(id);
			if (ticket == null) {
				return "ticket not found : " + id;
			} else {
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
			ticket.setUsers(rs.getParameterListValues("users", Collections.<String> emptyList()));
			if (rs.getParameter("comment", "").trim().length() > 0) {
				ticket.addComments(new Comment(user.getLogin(), rs.getParameter("comment", "")));
				if (!ticket.getAuthors().equals(ctx.getCurrentEditUser().getLogin())) {
					ticket.setRead(false);
				}
			} else {
				ctx.getRequest().setAttribute("back-list", true);
			}
		}
		ticketService.updateTicket(ctx, ticket);

		messageRepository.addMessage(new GenericMessage("ticket updated.", GenericMessage.INFO));
		return null;
	}
}
