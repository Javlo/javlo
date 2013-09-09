package org.javlo.service;

import org.javlo.context.GlobalContext;

public class TrackingService {
	
	private static class Track {
		private String url;
		private String ip;
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}		
	}
	
	public static final String KEY = TrackingService.class.getName();

	public static final TrackingService getInstance(GlobalContext context) {
		TrackingService instance = (TrackingService) context.getAttribute(KEY);
		if (instance == null) {
			instance = new TrackingService();
			context.setAttribute(KEY, instance);
		}
		return instance;
	}

}
