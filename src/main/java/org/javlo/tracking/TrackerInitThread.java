package org.javlo.tracking;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.javlo.context.StatContext;
import org.jcodec.common.logging.Logger;

public class TrackerInitThread extends Thread {
	
	private Tracker tracker;
	
	public TrackerInitThread(Tracker tracker) {
		this.tracker = tracker;
	}
	
	private static StatContext createStatContext(int year) {
		StatContext statCtx = StatContext.getInstance(null);

		// 30 days is default period
		Calendar now = Calendar.getInstance();
		statCtx.setTo(new Date(now.getTime().getTime()));
		now.add(Calendar.DAY_OF_YEAR, -30);
		statCtx.setFrom(new Date(now.getTime().getTime()));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, 11);
		cal.set(Calendar.DAY_OF_MONTH, 31);
		cal.set(Calendar.HOUR, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		statCtx.setTo(cal.getTime());
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		statCtx.setFrom(cal.getTime());
		return statCtx;
	}
	
	@Override
	public void run() {
		Logger.info("start TrackerInitThread.");
		int year = Calendar.getInstance().get(Calendar.YEAR);
		for (int i=year; i>year-10; i--) {
			try {
				tracker.getDayInfos(createStatContext(i));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Logger.info("end TrackerInitThread.");
	}

}
