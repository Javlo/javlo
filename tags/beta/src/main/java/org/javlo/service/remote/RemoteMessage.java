package org.javlo.service.remote;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.javlo.helper.StringHelper;

public class RemoteMessage {

	public static final int ERROR = 0;
	public static final int WARNING = 1;
	public static final int INFO = 2;

	public static final int GOD_LEVEL = 0;
	public static final int ADMIN_LEVEL = 1;
	public static final int USER_LEVEL = 2;

	private String message = null;
	private String messageKey = null;
	private Set<String> deliveredClient = new HashSet<String>();
	private int type = INFO; // defaut type is info
	private int level = USER_LEVEL; // defaut level is user

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	private Date creationDate = new Date();
	private String id = StringHelper.getRandomId();

	public String getId() {
		return id;
	}

	public boolean isDelivered(String clientId) {
		return deliveredClient.contains(clientId);
	}

	public void addDeleveredClient(String clientId) {
		deliveredClient.add(clientId);
	}

	public String getMessage(Map<String, String> i18nAccess) {
		if (message != null) {
			return message;
		} else if (i18nAccess != null && messageKey != null) {
			return i18nAccess.get(messageKey);
		} else {
			return null;
		}
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

	public Date getCreationDate() {
		return creationDate;
	}

}