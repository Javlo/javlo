package org.javlo.module.communication;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.service.IMService;
import org.javlo.service.IMService.IMItem;

public class CommunicationBean {

	private String currentSite;
	private String currentUser;
	private final Map<String, CommunicationSiteBean> sitesByKey = new LinkedHashMap<String, CommunicationSiteBean>();
	private final List<IMItem> messages = new LinkedList<IMItem>();

	private Long lastMessageId;

	public boolean isAllSites() {
		return IMService.ALL_SITES.equals(currentSite);
	}

	public String getCurrentSite() {
		return currentSite;
	}
	public void setCurrentSite(String currentSite) {
		this.currentSite = currentSite;
	}

	public String getCurrentUser() {
		return currentUser;
	}
	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}
	
	public Map<String, CommunicationSiteBean> getSitesByKey() {
		return sitesByKey;
	}
	
	public Collection<CommunicationSiteBean> getSites() {
		return sitesByKey.values();
	}

	public void addSite(CommunicationSiteBean site) {
		sitesByKey.put(site.getKey(), site);
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

}
