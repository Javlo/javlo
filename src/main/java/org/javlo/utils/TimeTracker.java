package org.javlo.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.helper.StringHelper;

public class TimeTracker {
	
	private static long GENERAL_START = new Date().getTime();

	private static Logger logger = Logger.getLogger(TimeTracker.class.getName());

	private static boolean ACTIVE = true;

	private static Map<String, TimeEvent> events = new HashMap<String, TimeEvent>();

	private TimeTracker() {
	}

	public static class TimeEvent {
		private long totalTime = 0;
		private long startTime = 0;
		private long latestTime = 0;
		public long getTotalTime() {
			return totalTime;
		}

		public void setTotalTime(long totalTime) {
			this.totalTime = totalTime;
		}

		public void end(long newTime) {
			latestTime = newTime - startTime;
			totalTime += latestTime;
			startTime = 0;
		}

		public long getStartTime() {
			return startTime;
		}

		public void setStartTime(long currentStartTime) {
			this.startTime = currentStartTime;
		}

		public boolean isInProgress() {
			return startTime > 0;
		}
		public long getLatestTime() {
			return latestTime;
		}
	}

	private static TimeEvent getEvent(String group, String action) {
		String key = createKey(group, action);
		TimeEvent timeEvent = events.get(key);
		if (timeEvent == null) {
			timeEvent = new TimeEvent();
			events.put(key, timeEvent);
		}
		return timeEvent;
	}

	private static String createKey(String group, String action) {
		return group + "____" + action;
	}

	public static void start(String group, String action) {
		if (ACTIVE) {
			TimeEvent timeEvent = getEvent(group, action);
			if (timeEvent.isInProgress()) {
				logger.warning("time all ready started : group=" + group + "  action=" + action);
			}
			timeEvent.setStartTime(new Date().getTime());
		}
	}

	public static void end(String group, String action) {
		if (ACTIVE) {
			TimeEvent timeEvent = getEvent(group, action);
			if (!timeEvent.isInProgress()) {
				logger.warning("time not started : group=" + group + "  action=" + action);
			} else {
				timeEvent.end(new Date().getTime());
				logger.warning("time finish : group=" + group + "  action=" + action+ " (current:"+StringHelper.renderTimeInSecond(timeEvent.getLatestTime()) + " total group:"+StringHelper.renderTimeInSecond(timeEvent.getTotalTime())+ " total:"+StringHelper.renderTimeInSecond(new Date().getTime()-GENERAL_START)+')'+')' );
			}
		}
	}
	
	public static void reset() {
		GENERAL_START = new Date().getTime();
		events.clear();
	}
}
