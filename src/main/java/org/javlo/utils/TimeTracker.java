package org.javlo.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.config.StaticConfig;
import org.javlo.helper.StringHelper;

public class TimeTracker {

	public static long GENERAL_START = new Date().getTime();

	private static long generalTime = 0;

	private static Logger logger = Logger.getLogger(TimeTracker.class.getName());

	private static boolean ACTIVE = false;

	private static HashMap<String, Map<Integer, TimeEvent>> events = new HashMap<String, Map<Integer, TimeEvent>>();

	private TimeTracker() {
	}

	public static class TimeEvent {
		private String label;
		private long totalTime = 0;
		private long startTime = 0;
		private long latestTime = 0;
		private int number = 0;

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

		public int getNumber() {
			return number;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
		
		public String getTimeLabel() {
			return StringHelper.renderTimeInSecond(totalTime);
		}
		
		public String getPartOfTotalTime() {
			return StringHelper.renderDoubleAsPercentage((double)totalTime/(double)generalTime);
		}
		
	}

	private static TimeEvent getEvent(String group, String action, Integer number) {
		String key = createKey(group, action);
		Map<Integer, TimeEvent> eventMap = events.get(key);
		if (eventMap == null) {
			eventMap = new HashMap<Integer, TimeEvent>();
			events.put(key, eventMap);
		}
		/** event allready exist **/
		TimeEvent timeEvent;
		if (number != null) {
			timeEvent = eventMap.get(number);
			if (number == 0 && timeEvent == null) {
				timeEvent = new TimeEvent();
				timeEvent.setLabel(group + " - " + action);
				timeEvent.setNumber(number);
				eventMap.put(number, timeEvent);
			}
		} else {
			number = eventMap.size() + 1;
			synchronized (eventMap) {
				while (eventMap.containsKey(number)) {
					number++;
				}
				timeEvent = new TimeEvent();				
				timeEvent.setNumber(number);
				eventMap.put(number, timeEvent);
			}
		}
		return timeEvent;
	}

	private static String createKey(String group, String action) {
		return group + "____" + action;
	}

	public static int start(String group, String action) {
		if (ACTIVE) {
			TimeEvent timeEvent = getEvent(group, action, null);
			if (timeEvent.isInProgress()) {
				logger.warning("time all ready started : group=" + group + "  action=" + action);
			}
			timeEvent.setStartTime(new Date().getTime());
			return timeEvent.getNumber();
		}
		return -1;
	}

	public static TimeEvent getMainEvent(String group, String action) {
		return getEvent(group, action, 0);
	}

	public static List<TimeEvent> getTimeEvents() {
		List<TimeEvent> outEvents = new LinkedList<TimeTracker.TimeEvent>();
		for (Map<Integer, TimeEvent> eventMap : events.values()) {
			TimeEvent mainEvent = eventMap.get(0);
			if (mainEvent != null) {
				outEvents.add(mainEvent);
			}
		}
		return outEvents;
	}

	public static void end(String group, String action, Integer number) {
		if (ACTIVE) {
			String key = createKey(group, action);
			Map<Integer, TimeEvent> eventMap = events.get(key);
			TimeEvent timeEvent = getEvent(group, action, number);
			eventMap.remove(number);
			if (!timeEvent.isInProgress()) {
				logger.warning("time not started : group=" + group + "  action=" + action);
			} else {
				timeEvent.end(new Date().getTime());
				logger.warning("time finish : group=" + group + "  action=" + action + " (current:" + StringHelper.renderTimeInSecond(timeEvent.getLatestTime()) + " total group:" + StringHelper.renderTimeInSecond(timeEvent.getTotalTime()) + " total:" + StringHelper.renderTimeInSecond(new Date().getTime() - GENERAL_START) + ')' + ')');
			}
			TimeEvent mainEvent = getMainEvent(group, action);
			synchronized (mainEvent) {
				mainEvent.setTotalTime(mainEvent.getTotalTime() + timeEvent.getTotalTime());
			}
			generalTime += timeEvent.getTotalTime();
		}
	}

	public static long getGeneralTime() {
		return generalTime;
	}

	public static void reset(StaticConfig staticConfig) {
		ACTIVE = staticConfig.isTimeTracker();
		GENERAL_START = new Date().getTime();
		events.clear();
	}
	
}
