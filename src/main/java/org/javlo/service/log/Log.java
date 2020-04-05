package org.javlo.service.log;

import java.util.Date;

import org.javlo.helper.StringHelper;

public class Log {
	
	public static final String GROUP_INIT_CONTEXT = "init-context";
	
	public static final String WARNING = "WARNING";
	public static final String INFO = "INFO";
	public static final String SEVERE = "SEVERE";
	public static final String TEMPORARY = "TEMPORARY";
	
	private String group;
	private String text;
	private String level = INFO;
	private Date time;
	private StackTraceElement[] stackTrace;
	
	public Log(String group, String message) {
		super();
		this.group = group;
		this.text = message;
		Exception e = new Exception();
		this.setStackTrace(e.getStackTrace());
	}
	public Log(String level, String group, String message) {
		super();
		this.level = level;
		this.group = group;
		this.text = message;
		this.time = new Date();
		Exception e = new Exception();
		this.setStackTrace(e.getStackTrace());
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getText() {
		return text;
	}
	public void setText(String message) {
		this.text = message;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getSortableTime() {
		return StringHelper.renderSortableTime(time);
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public StackTraceElement[] getStackTrace() {
		return stackTrace;
	}
	public void setStackTrace(StackTraceElement[] stackTrace) {
		this.stackTrace = stackTrace;
	}
}
