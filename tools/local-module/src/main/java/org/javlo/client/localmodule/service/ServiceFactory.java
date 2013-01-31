package org.javlo.client.localmodule.service;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.javlo.client.localmodule.ui.ClientTray;
import org.javlo.service.syncro.HttpClientService;

import com.google.gson.Gson;

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
	private HttpClientService httpClient = new HttpClientService();
	private DefaultHttpClient rawHttpClient;
	private Gson json = new Gson();

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

	public IMClientService getIMClient() {
		return IMClientService.getInstance();
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

	public HttpClient getRawHttpClient() {
		if (rawHttpClient == null) {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			rawHttpClient = httpClient;
		}
		synchronized (getConfig().lock) {
			if (getConfig().getProxyHost() != null) {
				rawHttpClient.getCredentialsProvider().setCredentials(
						new AuthScope(getConfig().getProxyHost(), getConfig().getProxyPort()),
						new UsernamePasswordCredentials(getConfig().getProxyUsername(), getConfig().getProxyPassword()));

				HttpHost proxy = new HttpHost(getConfig().getProxyHost(), getConfig().getProxyPort());

				rawHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			} else {
				rawHttpClient.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
			}
		}
		return rawHttpClient;
	}

	public Gson getJson() {
		return json;
	}

}
