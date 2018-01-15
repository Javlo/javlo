package org.javlo.service.calendar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;

public class RemoteICalProvider extends AbstractICalProvider {

	private static final String KEY = RemoteICalProvider.class.getName();

	private Properties providers;

	public static RemoteICalProvider getInstance(ContentContext ctx, Properties providers) {
		RemoteICalProvider outService = (RemoteICalProvider) ctx.getGlobalContext().getAttribute(KEY);
		if (outService == null) {
			outService = new RemoteICalProvider();
			ctx.getGlobalContext().setAttribute(KEY, outService);
			outService.init(ctx);
			outService.providers = providers;
		}
		return outService;
	}

	@Override
	public void reset(ContentContext ctx) throws Exception {
		ctx.getGlobalContext().setAttribute(KEY, null);
	}

	@Override
	public String getName() {
		return "remote";
	}

	protected int getCacheTime() {
		return 60 * 2; // 2 minutes
	}

	@Override
	public List<ICal> getICals() {
		if (providers.size() == 0) {
			return Collections.emptyList();
		}
		List<ICal> outICals = new LinkedList<ICal>();
		for (Object cat : providers.keySet()) {
			try {
				URL url = new URL(providers.getProperty(cat.toString()));				
				InputStream in = url.openStream();
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
					ICal ical = new ICal(isEditable());
					while (ical.loadFromReader(reader)) {
						outICals.add(ical);
						ical.setCategories(cat.toString());
						ical = new ICal(isEditable());
					}					
				} finally {
					ResourceHelper.closeResource(in);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return outICals;
	}

	@Override
	public List<ICal> getICals(int year, int month) throws Exception {
		if (providers.size() == 0) {
			return Collections.emptyList();
		}
		List<ICal> outICal = new LinkedList<ICal>();
		List<ICal> iCals = getICals();
		for (ICal ical : iCals) {
			if (ical.isSameMonth(year, month)) {
				outICal.add(ical);
			}
			cache(ical);

			if (!ical.isOneDay()) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(ical.getStartDate());
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(ical.getEndDate());
				if (endCal.getTimeInMillis() > cal.getTimeInMillis()) {
					GhostICal gcal = null;
					while (!DateUtils.isSameDay(cal, endCal)) {
						cal.add(Calendar.DAY_OF_YEAR, 1);
						gcal = new GhostICal(ical, cal.getTime());
						if (gcal.isSameMonth(year, month)) {
							outICal.add(gcal);
						}
						// cache(gcal);c
					}
					/* set end date if time in day defined */
					gcal.setDate(ical.getEndDate());
					gcal.setNext(false);
				}
			}

		}
		return outICal;
	}

	@Override
	public void store(ICal ical) {
		throw new RuntimeException("not implementend");
	}

	@Override
	public void deleteICal(ICal ical) throws Exception {
		throw new RuntimeException("not implementend");
	}

	@Override 
	public void deleteICal(String ical) throws Exception {
		throw new RuntimeException("not implementend");
	}
	
	public static void main(String[] args) throws IOException {
		URL url = new URL("https://calendar.google.com/calendar/ical/noctis.be_0281cn1e5cao5ra0ab1gk38008%40group.calendar.google.com/public/basic.ics");				
		InputStream in = url.openStream();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			ICal ical = new ICal(true);
			while (ical.loadFromReader(reader)) {
				System.out.println(">>>>>>>>> RemoteICalProvider.main : uid="+ical.getSummary()); //TODO: remove debug trace
				System.out.println(">>>>>>>>> RemoteICalProvider.main : start date = "+StringHelper.renderDate(ical.getStartDate())); //TODO: remove debug trace
				System.out.println(">>>>>>>>> RemoteICalProvider.main : end date = "+StringHelper.renderDate(ical.getEndDate())); //TODO: remove debug trace
				ical = new ICal(true);				
			}					
		} finally {
			ResourceHelper.closeResource(in);
		}

	}

}
