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
	};
	
}
