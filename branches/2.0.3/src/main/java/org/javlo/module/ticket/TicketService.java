package org.javlo.module.ticket;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;

public class TicketService {

	private static final String KEY = TicketService.class.getName();

	private File folder;

	private Map<String, TicketBean> tickets;

	public static TicketService getInstance(GlobalContext globalContext) throws IOException {
		TicketService service = (TicketService) globalContext.getAttribute(KEY);
		if (service == null) {
			service = new TicketService();
			service.folder = new File(URLHelper.mergePath(globalContext.getDataFolder(), "tickets"));
			service.loadTickets();
			globalContext.setAttribute(KEY, service);
		}
		return service;
	}

	private void loadTickets() throws IOException {
		tickets = new HashMap<String, TicketBean>();
		File[] files = folder.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".xml"));
		if (files != null) {
			for (File file : files) {
				TicketBean bean = loadTicket(file);
				if (bean != null && !bean.isDeleted()) {
					tickets.put(bean.getId(), bean);
				}
			}
		}
	}

	private TicketBean loadTicket(File file) throws IOException {
		if (file == null || !file.exists()) {
			return null;
		}
		String xml = ResourceHelper.loadStringFromFile(file);
		TicketBean bean = (TicketBean) ResourceHelper.loadBeanFromXML(xml);
		return bean;
	}

	private void storeTicket(TicketBean bean) throws IOException {
		File ticketFile = new File(URLHelper.mergePath(folder.getAbsolutePath(), bean.getId() + ".xml"));
		ticketFile.getParentFile().mkdirs();
		String xml = ResourceHelper.storeBeanFromXML(bean);
		ResourceHelper.writeStringToFile(ticketFile, xml, ContentContext.CHARACTER_ENCODING);
	}

	public void updateTicket(TicketBean ticket) throws IOException {
		if (!ticket.isDeleted()) {
			tickets.put(ticket.getId(), ticket);
		} else {
			tickets.remove(ticket.getId());
		}
		storeTicket(ticket);
	}

	public TicketBean getTicket(String id) {
		return tickets.get(id);
	}

	public Collection<TicketBean> getTickets() {
		return tickets.values();
	}

	public static List<TicketBean> getAllTickets(ContentContext ctx) throws IOException, ConfigurationException {
		List<TicketBean> allTickets = new LinkedList<TicketBean>();
		Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(ctx.getRequest().getSession().getServletContext());
		for (GlobalContext gc : allContext) {
			TicketService ticketService = TicketService.getInstance(gc);
			allTickets.addAll(ticketService.getTickets());
		}
		return allTickets;
	}
}
