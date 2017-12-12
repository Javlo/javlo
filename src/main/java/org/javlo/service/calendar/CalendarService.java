package org.javlo.service.calendar;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.utils.StructuredProperties;

public class CalendarService {

	private static final String KEY = "calendarService";
	
	private ICalProvider mainLocalProvider = null;
	private ICalProvider mainRemoteProvider = null;
	protected File folder;
	
	public static CalendarService getInstance(ContentContext ctx) throws IOException {
		CalendarService outService = (CalendarService) ctx.getGlobalContext().getAttribute(KEY);
		if (outService == null) {
			outService = new CalendarService();
			ctx.getGlobalContext().setAttribute(KEY, outService);
			outService.mainLocalProvider = LocalICalProvider.getInstance(ctx);
			outService.folder = new File(ctx.getGlobalContext().getCalendarFolder());
			outService.mainRemoteProvider = RemoteICalProvider.getInstance(ctx, outService.getICalProviders());
		}
		return outService;
	}

	public List<ICal> loadICals(int year, int month) throws Exception {
		List<ICal> icals = new LinkedList<ICal>();
		icals.addAll(mainLocalProvider.getICals(year, month));
		icals.addAll(mainRemoteProvider.getICals(year, month));
		return icals;
	}
	
	public void deleteICal(ICal ical) throws Exception {
		mainLocalProvider.deleteICal(ical);
	}
	
	private File getICalProvidersFile() {
		return new File(URLHelper.mergePath(folder.getAbsolutePath(), "ical_providers.properties"));
	}
	
	public void setICalProviders(ContentContext ctx, String providers) throws Exception {
		StructuredProperties prop = new StructuredProperties();
		try {
			prop.load(new StringReader(providers));
			prop.save(getICalProvidersFile());
			mainLocalProvider.reset(ctx);
			mainLocalProvider = RemoteICalProvider.getInstance(ctx, prop);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Properties getICalProviders() throws IOException {
		StructuredProperties prop = new StructuredProperties();
		prop.load(getICalProvidersFile());
		return prop;
	}

	public void deleteICal(String icalId) throws Exception {
		mainLocalProvider.deleteICal(icalId);		
	}

	public void store(ICal ical) throws Exception {
		mainLocalProvider.store(ical);
	}

}
