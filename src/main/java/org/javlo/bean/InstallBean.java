package org.javlo.bean;

public class InstallBean {
	
	public static final int ERROR = -1;
	public static final int SUCCESS = 1;
	public static final int NONE = 0;
	
	private int templateStatus = NONE;
	private int demoStatus = NONE;
	private int configStatus = SUCCESS;
	
	public int getTemplateStatus() {
		return templateStatus;
	}
	public void setTemplateStatus(int templateStatus) {
		this.templateStatus = templateStatus;
	}
	public int getDemoStatus() {
		return demoStatus;
	}
	public void setDemoStatus(int demoStatus) {
		this.demoStatus = demoStatus;
	}
	public int getConfigStatus() {
		return configStatus;
	}
	public void setConfigStatus(int configStatus) {
		this.configStatus = configStatus;
	}
	
}
