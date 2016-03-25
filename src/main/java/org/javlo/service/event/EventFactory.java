package org.javlo.service.event;

import org.javlo.context.ContentContext;

public class EventFactory {

	public static IEventsProvider getEventProvider(ContentContext ctx) {
		EventService eventService = (EventService)ctx.getGlobalContext().getAttribute("events");
		if (eventService == null) {
			eventService = new EventService();
			ctx.getGlobalContext().setAttribute("events", eventService);
		}
		return eventService;
	}

}
