package org.javlo.component.web2;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.links.SmartPageBean;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.service.event.Event;
import org.javlo.service.event.EventService;

public class MyEvents extends AbstractPropertiesComponent implements IAction {

	public static final String TYPE = "my-event";
	
	private static final List<String> FIELDS = new LinkedList(Arrays.asList(new String[] { "title", "list-title", "popup#checkbox"}));
	
	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isUnique() {
		return true;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		List<SmartPageBean> pageBeans = new LinkedList<SmartPageBean>();
		for(Event event : getEvents(ctx)) {
			if (event.getEventRegistration() instanceof IContentVisualComponent) {
				pageBeans.add(SmartPageBean.getInstance(ctx,ctx, ((IContentVisualComponent)event.getEventRegistration()).getPage(), null));
			}
		}
		ctx.getRequest().setAttribute("pages", pageBeans);
	}
	
	private static final String KEY = "eventsList"+MyEvents.class.getName();;
	
	public List<Event> getEvents(ContentContext ctx) throws Exception {
		List<Event> myEvents = (List<Event>)ctx.getAttribute(KEY);
		if (myEvents == null) {
			myEvents = new LinkedList<Event>();
			EventService eventService = new EventService();
			for(Event event : eventService.getEvents(ctx)) {
				if (event.getEventRegistration().getData(ctx, ctx.getCurrentUserId()).size() > 0) {
					myEvents.add(event);
				}
			}
			ctx.setAttribute(KEY, myEvents);
		}
		return myEvents;
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return IContentVisualComponent.COMPLEXITY_ADMIN;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		try {
			return getEvents(ctx).size()>0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}
	
	@Override
	public String getFontAwesome() {
		return "calendar-check-o";
	}
}
