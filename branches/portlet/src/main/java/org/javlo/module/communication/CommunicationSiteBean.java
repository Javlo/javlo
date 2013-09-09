package org.javlo.module.communication;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommunicationSiteBean {

	private String key;
	private Map<String, CommunicationUserBean> usersByName = new LinkedHashMap<String, CommunicationUserBean>();

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLabel() {
		return getKey();
	}

	public Map<String, CommunicationUserBean> getUsersByName() {
		return usersByName;
	}

	public Collection<CommunicationUserBean> getUsers() {
		return usersByName.values();
	}

	public void addUser(CommunicationUserBean user) {
		usersByName.put(user.getUsername(), user);
	}
}
