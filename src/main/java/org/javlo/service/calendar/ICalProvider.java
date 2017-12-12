package org.javlo.service.calendar;

import java.util.List;

import org.javlo.context.ContentContext;

public interface ICalProvider {
	
	public String getName();

	public List<ICal> getICals() throws Exception;
	
	public List<ICal> getICals(int year, int month) throws Exception;
	
	public void store(ICal ical) throws Exception;
	
	public void deleteICal(ICal ical) throws Exception;
	
	public void deleteICal(String id) throws Exception;
	
	public void reset(ContentContext ctx) throws Exception;
	
	public boolean isEditable();
	
}
