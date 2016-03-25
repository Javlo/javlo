package org.javlo.service.event;

import java.util.List;

import org.javlo.context.ContentContext;

public interface IEventsProvider {

	public List<Event> getEvents(ContentContext ctx) throws Exception;
	
	public Event getEvent(ContentContext ctx, String id) throws Exception;

}
