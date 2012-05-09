package org.javlo.service;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpSession;

import org.javlo.context.GlobalContext;
import org.javlo.helper.XHTMLHelper;
import org.javlo.utils.TimeMap;
import org.json.JSONException;

public class IMService {

	private static final String IM_SERVICE_ATTRIBUTE_NAME = IMService.class.getName();
	private static final String DEST_USER_SEPARATOR = ":";
	private static final String GLOBAL_USERNAME = "all";

	@SuppressWarnings("unchecked")
	public static IMService getInstance(GlobalContext globalContext, HttpSession session) {
		IMService instance = (IMService) session.getAttribute(IM_SERVICE_ATTRIBUTE_NAME);
		if (instance == null) {
			WeakReference<IMService> serviceRef = (WeakReference<IMService>) globalContext.getAttribute(IM_SERVICE_ATTRIBUTE_NAME);
			if (serviceRef != null) {
				instance = serviceRef.get();
			}
			if (instance == null) {
				instance = new IMService();
				serviceRef = new WeakReference<IMService>(instance);
				globalContext.setAttribute(IM_SERVICE_ATTRIBUTE_NAME, serviceRef);
			}
			session.setAttribute(IM_SERVICE_ATTRIBUTE_NAME, instance);
		}
		return instance;
	}

	private Map<Long, IMItem> messages = new TimeMap<Long, IMItem>(4 * 60 * 60); //4H
	private long lastMessageId = 0;
	private Map<String, String> userColors = new HashMap<String, String>();
	private int userColorIndex = 0;

	private IMService() {
	}

	public void appendMessage(String from, String message) {
		synchronized (messages) {
			messages.put(++lastMessageId, new IMItem(from, message));
		}
	}

	public Long fillMessageList(String username, Long currentId, List<IMItem> list) throws JSONException {
		synchronized (messages) {
			if (currentId == null) {
				try {
					currentId = Collections.min(messages.keySet());
				} catch (NoSuchElementException e) {
					currentId = lastMessageId + 1;
				}
			} else {
				currentId++;
			}
			for (long i = currentId; i <= lastMessageId; i++) {
				IMItem item = messages.get(i);
				if (item != null) {
					if (username.equals(item.getFrom())) {
						list.add(item);
						continue;
					} else {
						int pos = item.getMessage().indexOf(DEST_USER_SEPARATOR);
						if (pos < 0) {
							list.add(item);
						} else {
							String prefix = item.getMessage().substring(0, pos);
							if (prefix.equals(username) || prefix.equals(GLOBAL_USERNAME)) {
								list.add(item);
							}
						}
					}
				}
			}
			return lastMessageId;
		}
	}
	public String getUserColor(String username) {
		String out = userColors.get(username);
		if (out == null) {
			out = XHTMLHelper.getTextColor(++userColorIndex);
			userColors.put(username, out);
		}
		return out;
	}

	public class IMItem {

		private String message;
		private String from;

		public IMItem(String from, String message) {
			this.from = from;
			this.message = message;
		}

		public String getFrom() {
			return from;
		}

		public String getMessage() {
			return message;
		}
	}
}
