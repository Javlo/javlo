package org.javlo.client.localmodule.service;

import java.awt.TrayIcon.MessageType;
import java.util.LinkedList;
import java.util.List;

import org.javlo.client.localmodule.model.RemoteNotification;
import org.javlo.client.localmodule.ui.NotificationActionListener;
import org.javlo.message.GenericMessage;

public class NotificationService {

	private ServiceFactory factory = ServiceFactory.getInstance();

	private List<RemoteNotification> notifications = new LinkedList<RemoteNotification>();

	public void pushNotifications(List<RemoteNotification> notifications) {
		boolean doRefreshNotifications = false;
		RemoteNotification lastNotification = null;
		for (RemoteNotification notification : notifications) {
			if (!notification.isRead()) {
				lastNotification = notification;
			}
			this.notifications.add(notification);
			doRefreshNotifications = true;
		}
		if (doRefreshNotifications) {
			factory.getTray().refreshNotifications(this.notifications);
		}
		if (lastNotification != null) {
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
			factory.getTray().refreshNotifications(this.notifications);
		}
	}

}
