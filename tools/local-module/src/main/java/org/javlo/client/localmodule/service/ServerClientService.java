package org.javlo.client.localmodule.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionManagerFactory;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;
import org.javlo.client.localmodule.model.RemoteNotification;
import org.javlo.client.localmodule.model.ServerConfig;
import org.javlo.client.localmodule.model.ServerStatus;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.NotificationService.NotificationContainer;
import org.javlo.utils.JSONMap;

import com.google.gson.reflect.TypeToken;

public class ServerClientService {

	private static final Logger logger = Logger.getLogger(NotificationClientService.class.getName());

	private DefaultHttpClient httpClient;

	private final ServerConfig server;

	private Date lastNotificationsCheckDate;

	private ServerStatus status = ServerStatus.UNKNOWN;

	private String statusInfo;

	public ServerClientService(ServerConfig server, String proxyHost, Integer proxyPort, String proxyUsername, String proxyPassword) {
		this.server = server;

		httpClient = new DefaultHttpClient();

		//httpClient.getParams().setParameter(ClientPNames.CONNECTION_MANAGER_FACTORY_CLASS_NAME, HttpClientServiceClientConnectionManagerFactory.class.getName());

		if (proxyHost != null) {
			httpClient.getCredentialsProvider().setCredentials(
					new AuthScope(proxyHost, proxyPort),
					new UsernamePasswordCredentials(proxyUsername, proxyPassword));

			HttpHost proxy = new HttpHost(proxyHost, proxyPort);

			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
	}

	public ServerConfig getServer() {
		return server;
	}

	public ServerStatus getStatus() {
		return status;
	}

	public String getStatusInfo() {
		return statusInfo;
	}

	private void onSuccess() {
		status = ServerStatus.OK;
		statusInfo = null;
		ServiceFactory.getInstance().onServerStatusChange(server);
	}

	private void onWarning(String info) {
		status = ServerStatus.WARNING;
		statusInfo = info;
		ServiceFactory.getInstance().onServerStatusChange(server);
	}

	private void onError(Exception ex) {
		status = ServerStatus.ERROR;
		statusInfo = ex.getMessage();
		ServiceFactory.getInstance().onServerStatusChange(server);
	}

	public synchronized void dispose() {
		httpClient.getConnectionManager().shutdown();
	}

	public List<RemoteNotification> getNewDataNotifications() {
		try {
			List<RemoteNotification> notifications = null;
			String url = server.getServerURL();
			url = URLHelper.changeMode(url, "ajax");
			if (lastNotificationsCheckDate != null) {
				url = URLHelper.addParam(url, "webaction", "data.notifications");
				url = URLHelper.addParam(url, "lastdate", StringHelper.renderFileTime(lastNotificationsCheckDate));
			}
			url = URLHelper.addParam(url, "webaction", "data.date");
			logger.info("Start request to server: " + url);
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();

			if (response.getStatusLine().getStatusCode() == 200) {
				onSuccess();
				String content = EntityUtils.toString(entity);
				logger.info("Server response: " + content);

				JSONMap ajaxMap = JSONMap.parseMap(content);

				String serverDateStr = null;
				List<NotificationContainer> rawNotifications = null;
				JSONMap dataMap = ajaxMap.getMap("data");
				if (dataMap != null) {
					serverDateStr = dataMap.getValue("date", String.class);
					rawNotifications = dataMap.getValue("notifications",
							new TypeToken<List<NotificationContainer>>() {
							}.getType());
				}

				if (serverDateStr != null) {
					try {
						lastNotificationsCheckDate = StringHelper.parseFileTime(serverDateStr);
					} catch (ParseException ex) {
						logger.log(Level.WARNING, "Cannot parse data.date result: " + serverDateStr, ex);
					}
				}

				notifications = new LinkedList<RemoteNotification>();
				if (rawNotifications != null) {
					for (NotificationContainer rawNotification : rawNotifications) {
						notifications.add(new RemoteNotification(server, rawNotification));
					}
				}
			} else {
				onWarning("HTTP status: " + response.getStatusLine());
			}
			return notifications;
		} catch (Exception ex) {
			onError(ex);
			logger.log(Level.WARNING, "Exception on notification request.", ex);
			return new ArrayList<RemoteNotification>();
		}
	}

	public String tokenifyUrl(String simpleUrl) {
		HttpResponse response = null;
		try {
			String url = URLHelper.addParam(server.getServerURL(), "webaction", "data.oneTimeToken");
			url = URLHelper.changeMode(url, "ajax");
			logger.info("Start request to server: " + url);
			HttpGet httpget = new HttpGet(url);
			response = httpClient.execute(httpget);
			onSuccess();

			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				String content = EntityUtils.toString(entity);
				logger.info("Server response: " + content);

				JSONMap ajaxMap = JSONMap.parseMap(content);

				String token = ajaxMap.getMap("data").getValue("token", String.class);

				return URLHelper.addParam(simpleUrl, "j_token", token);
			}
		} catch (Exception ex) {
			onError(ex);
			logger.log(Level.SEVERE, "Exception retreiving one time token.", ex);
		} finally {
			safeConsume(response);
		}
		return simpleUrl;
	}

	public void checkThePhrase() {
		HttpResponse response = null;
		try {
			String url = server.getServerURL();
			logger.info("Start request to server: " + url);
			HttpGet httpget = new HttpGet(url);
			response = httpClient.execute(httpget);
			onSuccess();

			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				String content = EntityUtils.toString(entity);
				if (server.getCheckPhrase() == null || server.getCheckPhrase().isEmpty()) {
					onWarning("No check phrase configured!");
				} else if (!content.contains(server.getCheckPhrase())) {
					onWarning("Check phrase not found!");
				}
			} else {
				onWarning("HTTP status: " + response.getStatusLine());
			}
		} catch (Exception ex) {
			onError(ex);
			logger.log(Level.SEVERE, "Exception retreiving the page.", ex);
		} finally {
			safeConsume(response);
		}
	}

	public String httpGetAsString(String url) {
		HttpResponse response = null;
		try {
			logger.fine("Start request to server: " + url);
			HttpGet httpget = new HttpGet(url);
			response = httpClient.execute(httpget);
			onSuccess();

			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity);
			} else {
				onWarning("HTTP status: " + response.getStatusLine());
				return null;
			}
		} catch (Exception ex) {
			onError(ex);
			logger.log(Level.SEVERE, "Exception when executing Http GET.", ex);
			return null;
		} finally {
			safeConsume(response);
		}
	}

	private void safeConsume(HttpResponse response) {
		if (response != null) {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				try {
					entity.consumeContent();
				} catch (IOException ignored) {
					//Ignore exception
				}
			}
		}
	}

	public static class HttpClientServiceClientConnectionManagerFactory implements ClientConnectionManagerFactory {
		@Override
		public ClientConnectionManager newInstance(org.apache.http.params.HttpParams params, SchemeRegistry schemeRegistry) {
			return new ThreadSafeClientConnManager(params, schemeRegistry);
		}

	}

}
