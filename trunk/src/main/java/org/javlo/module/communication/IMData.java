package org.javlo.module.communication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javlo.service.IMService.IMItem;

public class IMData {

	private String currentUser;
	private Long lastMessageId;
	private List<IMItem> messages;

	private Collection<String> sites = new ArrayList<String>();
	private Map<String, Map<String, IMUser>> usersBySite = new HashMap<String, Map<String, IMUser>>();

	public String getCurrentUser() {
		return currentUser;
	}
	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}

	public Long getLastMessageId() {
		return lastMessageId;
	}
	public void setLastMessageId(Long lastMessageId) {
		this.lastMessageId = lastMessageId;
	}

	public List<IMItem> getMessages() {
		return messages;
	}
	public void setMessages(List<IMItem> messages) {
		this.messages = messages;
	}

	public Collection<String> getSites() {
		return sites;
	}
	public void setSites(Collection<String> sites) {
		this.sites = sites;
	}

	public Map<String, Map<String, IMUser>> getUsersBySite() {
		return usersBySite;
	}
	public void setUsersBySite(Map<String, Map<String, IMUser>> usersBySite) {
		this.usersBySite = usersBySite;
	}

}
