package org.javlo.client.localmodule.service;

import org.javlo.client.localmodule.ui.ClientTray;
import org.javlo.service.syncro.HttpClientService;

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

	private HttpClientService httpClient = new HttpClientService();

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

	public ActionService getAction() {
		return ActionService.getInstance();
	}

	public SynchroControlService getSynchroControl() {
		return SynchroControlService.getInstance();
	}

	public HttpClientService getHttpClient() {
//		httpClient.setServerURL(getConfig().getServerURL());
//		httpClient.setUsername(getConfig().getUsername());
//		httpClient.setPassword(getConfig().getPassword());
		httpClient.setProxyHost(getConfig().getProxyHost());
		httpClient.setProxyPort(getConfig().getProxyPort());
		return httpClient;
	}
}
