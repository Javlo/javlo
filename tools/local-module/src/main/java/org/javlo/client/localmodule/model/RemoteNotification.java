package org.javlo.client.localmodule.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.javlo.service.NotificationService.NotificationContainer;

public class RemoteNotification {

	private final ServerConfig server;
	private final NotificationContainer container;

	public RemoteNotification(ServerConfig server, NotificationContainer notificationContainer) {
		this.server = server;
		this.container = notificationContainer;
	}

	public ServerConfig getServer() {
		return server;
	}

	public Date getCreationDate() {
		return container.getNotification().getCreationDate();
	}

	public boolean isRead() {
		return container.isRead();
	}

	public String getTitle() {
		return getServer().getTitle();
	}

	public String getMessage() {
		return container.getNotification().getMessage();
	}

	public int getType() {
		return container.getNotification().getType();
	}

	public String getUrl() {
		return container.getNotification().getUrl();
	}

	public String getMenuLabel() {
		return new SimpleDateFormat("HH:mm").format(getCreationDate()) + " > " + getTitle() + ": " + getMessage();
	}

}
