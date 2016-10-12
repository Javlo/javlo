package org.javlo.utils;

import org.javlo.helper.StringHelper;

public class MemoryBean {
	
	private Runtime runtime = null;
	
	public MemoryBean() {
		this.runtime = Runtime.getRuntime();
	}
	
	public long getFreeMemory() {
		return runtime.freeMemory();
	}
	
	public long getTotalMemory() {
		return runtime.totalMemory();
	}
	
	public String getFreeMemoryLabel() {
		return StringHelper.renderSize(getFreeMemory());
	}
	
	public String getTotalMemoryLabel() {
		return StringHelper.renderSize(getTotalMemory());
	}
	
	public String getUsedMemoryLabel() {
		return StringHelper.renderSize(getTotalMemory()-getFreeMemory());
	}
	
	public int getUsedMemoryPercent() {
		return Math.round(((float)(getTotalMemory()-getFreeMemory())/(float)getTotalMemory())*100);
	}
	
}
