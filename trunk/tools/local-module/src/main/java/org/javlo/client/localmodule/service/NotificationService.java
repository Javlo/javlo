package org.javlo.client.localmodule.service;

import java.awt.TrayIcon.MessageType;
import java.util.LinkedList;
import java.util.List;

import org.javlo.client.localmodule.model.RemoteNotification;
import org.javlo.client.localmodule.ui.NotificationActionListener;

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
		if (lastNotification != null) {
			String caption = lastNotification.getTitle();
			String message = lastNotification.getMessage();
			factory.getTray().displayMessage(caption, message, MessageType.INFO, false, new NotificationActionListener(lastNotification));
		}
		if (doRefreshNotifications) {
			factory.getTray().refreshNotifications(this.notifications);
		}
	}

}
