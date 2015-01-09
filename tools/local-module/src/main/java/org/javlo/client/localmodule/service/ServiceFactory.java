package org.javlo.client.localmodule.service;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.javlo.client.localmodule.model.AppConfig;
import org.javlo.client.localmodule.model.ServerConfig;
import org.javlo.client.localmodule.service.synchro.ObserverSynchroService;
import org.javlo.client.localmodule.service.synchro.SynchroControlService;
import org.javlo.client.localmodule.ui.ClientTray;
import org.javlo.client.localmodule.ui.StatusFrame;

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
	private Map<String, ObserverSynchroService> synchroServices = new HashMap<String, ObserverSynchroService>();

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

	public SynchroControlService getSynchroControl() {
		return SynchroControlService.getInstance();
	}

	public ObserverSynchroService getSynchroService(ServerConfig server) {
		synchronized (synchroServices) {
			ObserverSynchroService out = synchroServices.get(server.getServerURL());
			File localFolder = server.getSynchronizedFolderFile();
			if (localFolder == null) {
				if (out != null) {
					//Sync is newly disabled: close service
					//If needed: out.dispose();
					out = null;
				}
			} else {
				if (out != null && !out.getBaseFolderFile().equals(localFolder)) {
					//Base folder is changed: new service
					//If needed: out.dispose();
					out = null;
				}
				if (out == null) {
					out = ObserverSynchroService.createInstance(getClient(server).getSyncHttpClient(), localFolder);
				}
			}
			synchroServices.put(server.getServerURL(), out);
			return out;
		}
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
		synchronized (clients) {
			for (Iterator<ServerClientService> iterator = clients.values().iterator(); iterator.hasNext();) {
				iterator.next().dispose();
				iterator.remove();
			}
		}
		synchronized (synchroServices) {
			for (Iterator<ObserverSynchroService> iterator = synchroServices.values().iterator(); iterator.hasNext();) {
				//If needed: iterator.next().dispose();
				iterator.remove();
			}
		}
		getNotificationService().clear();
		StatusFrame.onConfigChange();
		getNotificationClient().start();
		getSynchroControl().start();
	}

	public void onServerStatusChange(ServerConfig server) {
		ClientTray.onServerStatusChange(server);
		StatusFrame.onServerStatusChange(server);
	}

	public void onWorkStart() {
		getTray().setActiveState(true);
	}

	public void onWorkFinish() {
		getTray().setActiveState(false);
	}

}
