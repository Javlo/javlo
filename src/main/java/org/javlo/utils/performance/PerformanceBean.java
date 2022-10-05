package org.javlo.utils.performance;

public class PerformanceBean {
	
	private String group;
	private String method;
	
	public PerformanceBean(String group, String method) {
		super();
		this.group = group;
		this.method = method;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	
	

}
