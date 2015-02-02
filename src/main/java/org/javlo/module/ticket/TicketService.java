package org.javlo.module.ticket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.javlo.component.core.DebugNote;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;

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

	private void storeTicket(ContentContext ctx, TicketBean bean) throws Exception {
		if (!bean.isDebugNote()) {
			File ticketFile = new File(URLHelper.mergePath(folder.getAbsolutePath(), bean.getId() + ".xml"));
			ticketFile.getParentFile().mkdirs();
			String xml = ResourceHelper.storeBeanFromXML(bean);
			ResourceHelper.writeStringToFile(ticketFile, xml, ContentContext.CHARACTER_ENCODING);
		} else {
			IContentVisualComponent component = ContentService.getInstance(ctx.getGlobalContext()).getComponent(ctx, bean.getId());
			DebugNote d = (DebugNote) component;
			mapTicketToDebugNote(bean, d);
			PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		}
	}

	public void updateTicket(ContentContext ctx, TicketBean ticket) throws Exception {
		if (!ticket.isDebugNote()) {
			if (!ticket.isDeleted()) {
				tickets.put(ticket.getId(), ticket);
			} else {
				tickets.remove(ticket.getId());
			}
		}
		storeTicket(ctx, ticket);
	}

	public TicketBean getTicket(String id) {
		return tickets.get(id);
	}

	public Collection<TicketBean> getTickets() {
		return tickets.values();
	}

	public static List<TicketBean> getAllTickets(ContentContext ctx) throws Exception {
		List<TicketBean> allTickets = new LinkedList<TicketBean>();
		Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(ctx.getRequest().getSession().getServletContext());
		for (GlobalContext gc : allContext) {
			TicketService ticketService = TicketService.getInstance(gc);
			allTickets.addAll(ticketService.getTickets());
		}

		//Retrieve Debug notes from content
		ContentService content = ContentService.getInstance(ctx.getRequest());
		List<IContentVisualComponent> components = content.getAllContent(ctx);
		for (IContentVisualComponent comp : components) {
			if (comp.getType() == DebugNote.TYPE) {
				DebugNote d = (DebugNote) comp;
				TicketBean t = new TicketBean();
				mapDebugNoteToTicket(d, t, ctx);
				allTickets.add(t);
			}
		}

		return allTickets;
	}

	private static void mapDebugNoteToTicket(DebugNote d, TicketBean t, ContentContext ctx) throws Exception {
		String url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), d.getPage().getPath());
		t.setId(d.getId());
		t.setContext(ctx.getGlobalContext().getContextKey());
		t.setCategory(TicketBean.CATEGORY_DEBUG_NOTE);
		t.setCreationDate(d.getCreationDate());
		t.setLastUpdateDate(StringHelper.parseSortableTime(d.getModifDate()));
		t.setTitle(I18nAccess.getInstance(ctx).getText("content." + DebugNote.TYPE) + ": " + d.getPage().getTitle(ctx));
		t.setShare(TicketBean.SHARE_SITE);
		t.setComments(new LinkedList<Comment>());
		Reader in = new StringReader(d.getText());
		BufferedReader r = new BufferedReader(in);
		String line;
		StringWriter buffer = new StringWriter();
		PrintWriter w = new PrintWriter(buffer, true);
		Pattern splitter = Pattern.compile("^\\[(.+)\\]$");
		Comment comment = null;
		while ((line = r.readLine()) != null) {
			Matcher m = splitter.matcher(line);
			if (m.matches()) {
				w.flush();
				if (comment == null) {
					t.setMessage(StringHelper.trimLineReturn(buffer.toString()));
				} else {
					comment.setMessage(StringHelper.trimLineReturn(buffer.toString()));
					t.getComments().add(comment);
				}
				comment = new Comment();
				comment.setAuthors(m.group(1));
				buffer = new StringWriter();
				w = new PrintWriter(buffer, true);
			} else {
				w.println(line);
			}
		}
		w.flush();
		if (comment == null) {
			t.setMessage(StringHelper.trimLineReturn(buffer.toString()));
		} else {
			comment.setMessage(StringHelper.trimLineReturn(buffer.toString()));
			t.getComments().add(comment);
		}
		t.setPriority(StringHelper.safeParseInt(d.getPriority(), 0));
		t.setStatus(StringHelper.trimAndNullify(d.getStatus()));
		if (t.getStatus() == null) {
			t.setStatus(TicketBean.STATUS_NEW);
		}
		t.setAuthors(d.getAuthors());
		t.setUsers(d.getUserList());
		t.setUrl(url);
	}

	private void mapTicketToDebugNote(TicketBean t, DebugNote d) {
		d.setModifDate(StringHelper.renderSortableTime(t.getLastUpdateDate()));
		StringWriter text = new StringWriter();
		PrintWriter w = new PrintWriter(text);
		w.println(t.getMessage());
		for (Comment c : t.getComments()) {
			w.print('[');
			w.print(c.getAuthors());
			w.println(']');
			w.println(c.getMessage());
		}
		w.flush();
		d.setText(text.toString());
		d.setPriority("" + t.getPriority());
		d.setStatus(t.getStatus());
		d.setUserList(t.getUsers());
	}

}
