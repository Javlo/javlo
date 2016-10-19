package org.javlo.service.event;

import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class EventService implements IEventsProvider {

	public EventService() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public List<Event> getEvents(ContentContext ctx) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		List<Event> outList = new LinkedList<Event>();
		for (MenuElement page : content.getNavigation(ctx).getAllChildrenList()) {
			Event event = page.getEvent(ctx);
			if (event != null) {
				outList.add(event);
			}
		}
		return outList;
	}
	
	@Override
	public Event getEvent(ContentContext ctx, String id) throws Exception {
		for (Event event : getEvents(ctx)) {
			if (event.getId().equals(id)) {
				return event;
			}
		}
		return null;
	}
	
}
