package org.javlo.client.localmodule.service;

import java.awt.TrayIcon.MessageType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.javlo.client.localmodule.model.RemoteNotification;
import org.javlo.client.localmodule.ui.NotificationActionListener;
import org.javlo.message.GenericMessage;
import org.javlo.utils.TimeMap;

public class NotificationService {


	private ServiceFactory factory = ServiceFactory.getInstance();

	private static final int TIME_MAP_DEFAULT = 10 * 60; //10 min

	private Map<Object, RemoteNotification> notifications = new TimeMap<Object, RemoteNotification>(
			new LinkedHashMap<Object, RemoteNotification>(), TIME_MAP_DEFAULT);

	public void pushNotifications(List<RemoteNotification> notifications, boolean showLastNotif) {
		boolean doRefreshNotifications = false;
		RemoteNotification lastNotification = null;
		for (RemoteNotification notification : notifications) {
			if (!notification.isRead()) {
				lastNotification = notification;
			}
			this.notifications.put(notification, notification);
			doRefreshNotifications = true;
		}
		if (doRefreshNotifications) {
			factory.getTray().refreshNotifications(this.notifications.values());
		}
		if (showLastNotif && lastNotification != null) {
			String caption = lastNotification.getTitle();
			String message = lastNotification.getMessage();
			MessageType type = translateType(lastNotification.getType());
			factory.getTray().displayMessage(caption, message, type, false, new NotificationActionListener(lastNotification));
		}
	}

	private MessageType translateType(int type) {
		switch (type) {
		case GenericMessage.ERROR:
			return MessageType.ERROR;

		case GenericMessage.INFO:
			return MessageType.INFO;

		case GenericMessage.HELP:
			return MessageType.INFO;

		case GenericMessage.ALERT:
			return MessageType.WARNING;

		case GenericMessage.SUCCESS:
			return MessageType.INFO;

		default:
			return MessageType.NONE;
		}
	}

	public void clear() {
		boolean doRefreshNotifications = !notifications.isEmpty();
		if (doRefreshNotifications) {
			notifications.clear();
			factory.getTray().refreshNotifications(this.notifications.values());
		}
	}

}
