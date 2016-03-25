package org.javlo.module.event;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.event.Event;
import org.javlo.service.event.EventFactory;
import org.javlo.service.event.IEventsProvider;
import org.javlo.user.User;

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
		public List<User> getParticipants() throws Exception {
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
		return "events-module";
	}
	
	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {	
		String msg = super.prepare(ctx, modulesContext);
		IEventsProvider eventService = EventFactory.getEventProvider(ctx);
		ctx.getRequest().setAttribute("events", eventService.getEvents(ctx));
		if (ctx.getRequest().getParameter("event") != null) {
			ctx.getRequest().setAttribute("event", new EventBean(ctx, eventService.getEvent(ctx, ctx.getRequest().getParameter("event"))));
		} else {
			if (eventService.getEvents(ctx).size() > 0) {
				ctx.getRequest().setAttribute("event", new EventBean(ctx, eventService.getEvents(ctx).iterator().next()));
			}
		}
		return msg;
	}

}
