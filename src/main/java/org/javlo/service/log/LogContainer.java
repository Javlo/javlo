package org.javlo.service.log;

import java.util.List;

import org.javlo.utils.TimeList;

public class LogContainer {
	
	private TimeList<Log> logs = new TimeList<Log>(60*60);
	private TimeList<String> groups = new TimeList<String>(60*60+1);
	
	public void add(Log log) {
		this.logs.add(log);
		if (!groups.contains(log.getGroup())) {
			groups.add(log.getGroup());
		}
	}
	
	public void add(String group, String message) {
		logs.add(new Log(group, message));
		if (!groups.contains(group)) {
			groups.add(group);
		}
	}
	
	public List<String> getGroups() {
		return groups;
	}
	
	public TimeList<Log> getLogs() {
		return logs;
	}
}