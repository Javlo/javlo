package org.javlo.context;

import java.io.Serializable;
import java.util.Date;

public class TimeTravelerContext implements Serializable {

	private Date travelTime;

	public Date getTravelTime() {
		return travelTime;
	}
	public void setTravelTime(Date viewInstant) {
		this.travelTime = viewInstant;
	}
}
