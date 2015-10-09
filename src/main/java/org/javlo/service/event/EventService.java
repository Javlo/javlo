package org.javlo.service.event;

import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class EventService {

	public EventService() {
		// TODO Auto-generated constructor stub
	}

	public static EventService getInstance(ContentContext ctx) {
		EventService eventService = (EventService)ctx.getGlobalContext().getAttribute("events");
		if (eventService == null) {
			eventService = new EventService();
			ctx.getGlobalContext().setAttribute("events", eventService);
		}
		return eventService;
	}
	
	public List<Event> getEvents(ContentContext ctx) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		List<Event> outList = new LinkedList<Event>();
		for (MenuElement page : content.getNavigation(ctx).getAllChildren()) {
			Event event = page.getEvent(ctx);
			if (event != null) {
				outList.add(event);
			}
		}
		return outList;
	}
	
	public Event getEvent(ContentContext ctx, String id) throws Exception {
		for (Event event : getEvents(ctx)) {
			if (event.getId().equals(id)) {
				return event;
			}
		}
		return null;
	}
	
}
