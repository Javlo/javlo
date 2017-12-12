package org.javlo.service.calendar;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.utils.TimeMap;

public abstract class AbstractICalProvider implements ICalProvider {

	private Map<String, Object> cache = new TimeMap<String, Object>(60*60*24, 100000);
	
	protected File folder;
	
	public void init(ContentContext ctx) {
		folder = new File(ctx.getGlobalContext().getCalendarFolder());
		cache = new TimeMap<String, Object>(getCacheTime(), getCacheSize());
	}
	
	protected int getCacheTime() {
		return 60*60*24;
	}
	
	protected int getCacheSize() {
		return 100000;
	}

	protected void cache(ICal ical) {
		cache.put(ical.getId(), ical);
	}
	
	protected void cacheAll(List<ICal> icals) {
		cache.put("_all_ical", icals);
	}
	
	protected List<ICal> getAllInCache() {
		Object cal = cache.get("_all_ical");
		if (cal != null) {
			return (List<ICal>) cal;
		} else {
			return Collections.emptyList();
		}
	}
	
	protected ICal getInCache(String id) {
		Object cal = cache.get(id);
		if (cal != null) {
			if (cal instanceof ICal) {
				return (ICal)cal;
			}
		}
		return null;		
	}
	
	protected boolean removeFromCache(String id) {
		return cache.remove(id) != null;
	}
	
	protected void clearCache() {
		cache.clear();
	}
	
	@Override
	public boolean isEditable() {	
		return false;
	}
	
}
