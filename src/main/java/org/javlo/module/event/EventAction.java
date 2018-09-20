package org.javlo.module.event;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.service.calendar.CalendarBean;
import org.javlo.service.calendar.CalendarService;
import org.javlo.service.calendar.ICal;
import org.javlo.service.event.Event;
import org.javlo.service.event.EventFactory;
import org.javlo.service.event.IEventsProvider;
import org.javlo.user.IUserInfo;

public class EventAction extends AbstractModuleAction {
	
	public static class EventBean {
		private Event event;
		ContentContext ctx;
		public EventBean(ContentContext ctx, Event event) {
			this.event = event;
			this.ctx = ctx;
		}
		public String getId() {
			return event.getId();
		}
		public String getSummary() {
			return event.getSummary();
		}
		public String getDescription() {
			return event.getDescription();
		}
		public String getStart() throws FileNotFoundException, IOException {
			return StringHelper.renderShortDate(ctx, event.getStart());
		}
		public String getEnd() throws FileNotFoundException, IOException {
			return StringHelper.renderShortDate(ctx, event.getEnd());
		}
		public List<IUserInfo> getParticipants() throws Exception {
			return event.getParticipants(ctx);
		}
		public String getParticipantsFileURL() {
			return event.getParticipantsFileURL();
		}
		public String getImageURL() throws Exception {
			if (event.getImage() != null) {
				return URLHelper.createTransformURL(ctx, event.getImage().getResourceURL(ctx), "standard");	
			} else {
				return null;
			}			
		}		
		public String getImageDescription() {
			if (event.getImage() != null) {
				return event.getImage().getImageDescription(ctx);
			} else {
				return null;
			}
		}
		public String getUrl() {
			return URLHelper.addParam(event.getUrl().toString(), "module", "content");
		}		
	}

	public EventAction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getActionGroupName() {
		return "event";
	}
	
	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {	
		String msg = super.prepare(ctx, modulesContext);
		IEventsProvider eventService = EventFactory.getEventProvider(ctx);
		ModulesContext moduleContext = ModulesContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext());
		Module currentModule = moduleContext.getCurrentModule();		
		if (ctx.getRequest().getParameter("calkey") != null) {
			CalendarBean.getInstance(ctx, ctx.getRequest().getParameter("calkey"));
			ctx.getRequest().setAttribute("calendarPage", true);
		} else {
			CalendarBean.getInstance(ctx);
		}
		boolean calendar = StringHelper.isTrue(ctx.getRequest().getParameter("calendar"));
		ctx.getRequest().setAttribute("events", eventService.getEvents(ctx));
		if (!calendar) {			
			if (ctx.getRequest().getParameter("event") != null) {
				ctx.getRequest().setAttribute("event", new EventBean(ctx, eventService.getEvent(ctx, ctx.getRequest().getParameter("event"))));
			} else {
				calendar = true;
			}
		}	
		ctx.getRequest().setAttribute("icalproviders", StringHelper.propertiesToString(CalendarService.getInstance(ctx).getICalProviders()));		
		if (calendar) {
			currentModule.clearAllBoxes();
			currentModule.addMainBox("calendar", "calendar", "/jsp/calendar.jsp", false);
			currentModule.createSideBox("add-event", "add event", "/jsp/addevent.jsp", true);
			ctx.getRequest().setAttribute("calendarPage", true);
		} else {
			currentModule.restoreAll();
		}		
		return msg;
	}
	
	public static String performAddical(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {		
		String startdate = rs.getParameter("startdate");
		String enddate = rs.getParameter("enddate");		
		if (StringHelper.isAllEmpty(startdate, enddate)) {
			return "no date defined.";
		}
		ICal ical = new ICal(true);
		if (startdate != null) {
			ical.setStartDate(StringHelper.parseSortableDate(startdate));
		}
		if (enddate != null) {
			ical.setEndDate(StringHelper.parseSortableDate(enddate));
		}
		ical.setSummary(rs.getParameter("summary"));
		ical.setCategories(rs.getParameter("categories"));
		
		CalendarService.getInstance(ctx).store(ical);
		
		return null;
	}
	
	public static String performAddicalurl(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {		
		String urllist = rs.getParameter("urllist");
		if (urllist != null) {
			CalendarService.getInstance(ctx).setICalProviders(ctx, urllist);
		}
		return null;
	}
	
	public static String performDeleteical(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException, Exception {
		String icalId=rs.getParameter("ical");
		if (icalId != null) {
			CalendarService.getInstance(ctx).deleteICal(icalId);
		}
		return null;
	}
}
