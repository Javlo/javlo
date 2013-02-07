package org.javlo.module.ticket;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;

public class TicketAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "ticket";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);

		RequestService rs = RequestService.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Module ticketModule = modulesContext.getCurrentModule();

		TicketService ticketService = TicketService.getInstance(globalContext);
		List<TicketBean> myTickets = new LinkedList<TicketBean>();

		for (TicketBean ticket : ticketService.getTickets()) {
			if (!ticket.getStatus().equals("archived")) {
				if (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser()) || ticket.getAuthors().equals(ctx.getCurrentEditUser().getLogin())) {
					myTickets.add(ticket);
				}
			}
		}
		ctx.getRequest().setAttribute("tickets", myTickets);

		if (rs.getParameter("id", "").trim().length() > 0 && rs.getParameter("back", null) == null) {
			ctx.getRequest().setAttribute("ticket", ticketService.getTicket(rs.getParameter("id", null)));
			if (ticketModule.getBox("main") == null) {
				ticketModule.addMainBox("main", "update ticket : " + rs.getParameter("id", ""), "/jsp/update_ticket.jsp", true);
				ticketModule.setRenderer(null);
			}
		} else {
			ticketModule.setRenderer("/jsp/list.jsp");
			ticketModule.restoreBoxes();
		}

		return msg;
	}

	public static String performUpdate(RequestService rs, ContentContext ctx, GlobalContext globalContext, User user, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		TicketService ticketService = TicketService.getInstance(globalContext);
		String id = rs.getParameter("id", "");
		TicketBean ticket;
		if (id.trim().length() > 0) { // update
			ticket = ticketService.getTicket(id);
			if (ticket == null) {
				return "ticket not found : " + id;
			} else {
				ticket.setLastUpdateDate(new Date());
				ticket.setLatestEditor(user.getLogin());
			}
		} else { // create
			ticket = new TicketBean();
			ticket.setAuthors(user.getLogin());
			ticket.setContext(globalContext.getContextKey());
		}
		ticket.setTitle(rs.getParameter("title", ticket.getTitle()));
		ticket.setPriority(Integer.parseInt(rs.getParameter("priority", "" + ticket.getPriority())));
		ticket.setCategory(rs.getParameter("category", ticket.getCategory()));
		ticket.setMessage(rs.getParameter("message", ticket.getMessage()));
		ticket.setStatus(rs.getParameter("status", ticket.getStatus()));
		ticket.setUrl(rs.getParameter("url", ticket.getUrl()));
		if (rs.getParameter("comment", "").trim().length() > 0) {
			ticket.addComments(new Comment(user.getLogin(), rs.getParameter("comment", "")));
		}

		ticketService.updateTicket(ticket);

		messageRepository.addMessage(new GenericMessage("ticket updated.", GenericMessage.INFO));
		return null;
	}
}
