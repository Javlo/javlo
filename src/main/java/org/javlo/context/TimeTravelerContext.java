package org.javlo.context;

import java.io.Serializable;
import java.util.Date;

import org.javlo.helper.StringHelper;

public class TimeTravelerContext implements Serializable {

	private Date travelTime;
	private Integer version = null;

	public Date getTravelTime() {
		return travelTime;
	}
	public void setTravelTime(Date viewInstant) {
		this.travelTime = viewInstant;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	
	public boolean isEmpty() {
		return version == null && travelTime == null;
	}
	
	public String getLabel() {
		return StringHelper.renderTime(travelTime);
	}
}
