package org.javlo.client.localmodule.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.javlo.client.localmodule.model.RemoteNotification;
import org.javlo.client.localmodule.service.ServiceFactory;

public class NotificationActionListener implements ActionListener {

	private final RemoteNotification notification;

	public NotificationActionListener(RemoteNotification notification) {
		this.notification = notification;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String url = notification.getUrl();
		if (url != null) {
			ServiceFactory.getInstance().getAction().openUrl(notification.getServer(), url);
		}
	}

}
