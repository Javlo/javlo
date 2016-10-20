package org.javlo.context;

import java.util.Map;

import org.javlo.helper.StringHelper;

public class SpecialConfigBean {
	
	private Map config;

	public SpecialConfigBean(Map config) {
		this.config = config;
	}
	
	public Map getMap() {
		return config;
	}

	public boolean isPasteAsMirror() {
			return StringHelper.isTrue(config.get("content.pasteasmirror"));
	}
	
	/**
	 * track access to resource and page.  Use for display the most readed picture or page.
	 * false by default
	 * @return
	 */
	public boolean isTrackingAccess() {
		return StringHelper.isTrue(config.get("tracking.access"));  
	}
	
}
