package org.javlo.service;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.utils.TimeMap;

public class IMService {

	private static final String IM_SERVICE_ATTRIBUTE_NAME = IMService.class.getName();
	public static final String ALL_SITES = "_ALL";
	public static final String ALL_USERS = "_ALL";
	private static final String WIZZ_MESSAGE = "*";

	public static IMService getInstance(HttpSession session) {
		IMService instance = (IMService) session.getServletContext().getAttribute(IM_SERVICE_ATTRIBUTE_NAME);
		if (instance == null) {
			instance = new IMService();
			session.getServletContext().setAttribute(IM_SERVICE_ATTRIBUTE_NAME, instance);
		}
		return instance;
	}

	private Map<Long, IMItem> messages = new TimeMap<Long, IMItem>(4 * 60 * 60); //4H
	private long lastMessageId = 0;
	private Map<String, String> userColors = new HashMap<String, String>();
	private Map<String, Long> userLastReadMessageId = new HashMap<String, Long>();
	private Map<String, Set<Long>> userReceivedMessageIds = new HashMap<String, Set<Long>>();
	private int userColorIndex = 0;

	private IMService() {
	}

	public void appendMessage(String fromSite, String fromUser, String receiverSite, String receiverUser, String message) {
		synchronized (messages) {
			receiverUser = StringHelper.trimAndNullify(receiverUser);
			if (receiverUser == null) {
				receiverUser = ALL_USERS;
			}
			IMItem item = new IMItem(++lastMessageId, new Date(), fromSite, fromUser, receiverSite, receiverUser, message, message.equals(WIZZ_MESSAGE));
			messages.put(item.getId(), item);
		}
	}

	public void fillMessageList(String site, String username, Date lastDate, List<IMItem> list) {
		long currentLastMessageId = lastMessageId;
		long currentId;
		try {
			currentId = Collections.min(messages.keySet());
		} catch (NoSuchElementException e) {
			currentId = currentLastMessageId + 1;
		}
		boolean allSite = site.equals(ALL_SITES);
		for (; currentId <= currentLastMessageId; currentId++) {
			IMItem item = messages.get(currentId);
			if (item != null) {
				if (lastDate == null || item.getSentDate().after(lastDate)) {
					boolean currentUserIsSender = item.getFromSite().equals(site) && item.getFromUser().equals(username);
					if ((allSite
							|| item.getReceiverSite().equals(ALL_SITES)
							|| (item.getReceiverSite().equals(site) && (item.getReceiverUser().equals(username) || item.getReceiverUser().equals(ALL_USERS)))
							) && !currentUserIsSender) {
						list.add(item);
					}
				}
			}
		}
	}

	public Long fillMessageList(String site, String username, Long currentId, List<IMItem> list) {
		long currentLastMessageId = lastMessageId;
		if (currentId == null) {
			try {
				currentId = Collections.min(messages.keySet());
			} catch (NoSuchElementException e) {
				currentId = currentLastMessageId + 1;
			}
		} else {
			currentId++;
		}
		String userKey = site + "::" + username;
		Set<Long> receivedIds = userReceivedMessageIds.get(userKey);
		if (receivedIds == null) {
			receivedIds = new HashSet<Long>();
			userReceivedMessageIds.put(userKey, receivedIds);
			receivedIds.addAll(messages.keySet());
		}
		boolean allSite = site.equals(ALL_SITES);
		for (; currentId <= currentLastMessageId; currentId++) {
			IMItem item = messages.get(currentId);
			if (item != null) {
				boolean currentUserIsSender = item.getFromSite().equals(site) && item.getFromUser().equals(username);
				if (allSite
						|| item.getReceiverSite().equals(ALL_SITES)
						|| (item.getReceiverSite().equals(site) && (item.getReceiverUser().equals(username) || item.getReceiverUser().equals(ALL_USERS)))
						|| (currentUserIsSender)) {
					boolean isWizz = item.isWizz();
					isWizz = isWizz && !currentUserIsSender;
					isWizz = isWizz && !receivedIds.contains(item.getId());
					receivedIds.add(item.getId());
					if (!Boolean.valueOf(item.isWizz()).equals(isWizz)) {
						item = new IMItem(item.getId(), item.getSentDate(), item.getFromSite(), item.getFromUser(), item.getReceiverSite(), item.getReceiverUser(), item.getMessage(), isWizz);
					}
					list.add(item);
				}
			}
		}
		return currentLastMessageId;
	}

	public String getUserColor(String site, String username) {
		String key = site + "::" + username;
		String out = userColors.get(key);
		if (out == null) {
			out = XHTMLHelper.getTextColor(++userColorIndex);
			userColors.put(key, out);
		}
		return out;
	}

	public Long getLastReadMessageId(String site, String username) {
		String key = site + "::" + username;
		return userLastReadMessageId.get(key);
	}

	public void setLastReadMessageId(String site, String username, Long lastReadMessageId) {
		String key = site + "::" + username;
		userLastReadMessageId.put(key, lastReadMessageId);
	}

	public static class IMItem {

		private final long id;
		private final Date sentDate;
		private final String fromSite;
		private final String fromUser;
		private final String receiverSite;
		private final String receiverUser;
		private final String message;
		private final boolean wizz;

		public IMItem(long id, Date sentDate, String fromSite, String fromUser, String receiverSite, String receiverUser, String message, boolean wizz) {
			super();
			this.id = id;
			this.sentDate = sentDate;
			this.fromSite = fromSite;
			this.fromUser = fromUser;
			this.receiverSite = receiverSite;
			this.receiverUser = receiverUser;
			this.message = message;
			this.wizz = wizz;
		}

		public long getId() {
			return id;
		}

		public Date getSentDate() {
			return sentDate;
		}

		public String getFromSite() {
			return fromSite;
		}

		public boolean isFromAllSites() {
			return IMService.ALL_SITES.equals(fromSite);
		}

		public String getFromUser() {
			return fromUser;
		}

		public String getReceiverSite() {
			return receiverSite;
		}

		public boolean isToAllSites() {
			return IMService.ALL_SITES.equals(receiverSite);
		}

		public String getReceiverUser() {
			return receiverUser;
		}

		public boolean isToAllUsers() {
			return IMService.ALL_USERS.equals(receiverUser);
		}

		public String getMessage() {
			return message;
		}

		public boolean isWizz() {
			return wizz;
		}

	}

}
