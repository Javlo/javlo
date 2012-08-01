package org.javlo.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpSession;

import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.utils.TimeMap;

public class IMService {

	private static final String IM_SERVICE_ATTRIBUTE_NAME = IMService.class.getName();
	public static final String ALL_SITES = "_ALL";
	public static final String ALL_USERS = "_ALL";

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
	private int userColorIndex = 0;

	private IMService() {
	}

	public void appendMessage(String fromSite, String fromUser, String receiverSite, String receiverUser, String message) {
		synchronized (messages) {
			receiverUser = StringHelper.trimAndNullify(receiverUser);
			if (receiverUser == null) {
				receiverUser = ALL_USERS;
			}
			messages.put(++lastMessageId, new IMItem(fromSite, fromUser, receiverSite, receiverUser, message));
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
		boolean allSite = site.equals(ALL_SITES);
		for (; currentId <= currentLastMessageId; currentId++) {
			IMItem item = messages.get(currentId);
			if (item != null) {
				if (allSite
						|| item.getReceiverSite().equals(ALL_SITES)
						|| (item.getReceiverSite().equals(site) && (item.getReceiverUser().equals(username) || item.getReceiverUser().equals(ALL_USERS)))
						|| (item.getFromSite().equals(site) && item.getFromUser().equals(username))) {
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

		private String fromSite;
		private String fromUser;
		private String receiverSite;
		private String receiverUser;
		private String message;

		public IMItem(String fromSite, String fromUser, String receiverSite, String receiverUser, String message) {
			super();
			this.fromSite = fromSite;
			this.fromUser = fromUser;
			this.receiverSite = receiverSite;
			this.receiverUser = receiverUser;
			this.message = message;
		}

		public String getFromSite() {
			return fromSite;
		}

		public String getFromUser() {
			return fromUser;
		}

		public String getReceiverSite() {
			return receiverSite;
		}

		public String getReceiverUser() {
			return receiverUser;
		}

		public String getMessage() {
			return message;
		}

	}

}
