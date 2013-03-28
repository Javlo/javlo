package org.javlo.client.localmodule.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.javlo.client.localmodule.model.AppConfig;
import org.javlo.client.localmodule.model.ServerConfig;
import org.javlo.client.localmodule.ui.ClientTray;

public class ServiceFactory {

	private static ServiceFactory instance;
	public static ServiceFactory getInstance() {
		synchronized (ServiceFactory.class) {
			if (instance == null) {
				instance = new ServiceFactory();
			}
			return instance;
		}
	}

	private NotificationService notificationService;
	private Map<String, ServerClientService> clients = new HashMap<String, ServerClientService>();

	private ServiceFactory() {
	}

	public I18nService getI18n() {
		return I18nService.getInstance();
	}

	public ConfigService getConfig() {
		return ConfigService.getInstance();
	}

	public ClientTray getTray() {
		return ClientTray.getInstance();
	}

	public NotificationService getNotificationService() {
		if (notificationService == null) {
			notificationService = new NotificationService();
		}
		return notificationService;
	}

	public ActionService getAction() {
		return ActionService.getInstance();
	}

	public NotificationClientService getNotificationClient() {
		return NotificationClientService.getInstance();
	}

	public ServerClientService getClient(ServerConfig serverConfig) {
		ServerClientService client = clients.get(serverConfig.getServerURL());
		if (client == null) {
			synchronized (clients) {
				client = clients.get(serverConfig.getServerURL());
				if (client == null) {
					AppConfig c = getConfig().getBean();
					client = new ServerClientService(serverConfig,
							c.getProxyHost(), c.getProxyPort(), c.getProxyUsername(), c.getProxyPassword());
					clients.put(serverConfig.getServerURL(), client);
				}
			}
		}
		return client;
	}

	public void onConfigChange() {
		getNotificationClient().start();
		synchronized (clients) {
			for (Iterator<ServerClientService> iterator = clients.values().iterator(); iterator.hasNext();) {
				iterator.next().dispose();
				iterator.remove();
			}
		}
		getNotificationService().clear();
	}

}
