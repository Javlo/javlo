package org.javlo.client.localmodule.service;

import java.io.IOException;
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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.javlo.client.localmodule.model.RemoteNotification;
import org.javlo.client.localmodule.model.ServerConfig;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.NotificationService.NotificationContainer;
import org.javlo.utils.JSONMap;

import com.google.gson.reflect.TypeToken;

public class ServerClientService {

	private static final Logger logger = Logger.getLogger(NotificationClientService.class.getName());

	private DefaultHttpClient httpClient;

	private final ServerConfig server;

	public ServerClientService(ServerConfig server, String proxyHost, Integer proxyPort, String proxyUsername, String proxyPassword) {
		this.server = server;

		httpClient = new DefaultHttpClient();

		if (proxyHost != null) {
			httpClient.getCredentialsProvider().setCredentials(
					new AuthScope(proxyHost, proxyPort),
					new UsernamePasswordCredentials(proxyUsername, proxyPassword));

			HttpHost proxy = new HttpHost(proxyHost, proxyPort);

			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
	}

	public synchronized void dispose() {
		httpClient.getConnectionManager().shutdown();
	}

	public List<RemoteNotification> callDataNotifications(Date lastDate) throws ClientProtocolException, IOException {
		String url = URLHelper.addParam(server.getServerURL(), "webaction", "data.notifications");
		if (lastDate != null) {
			url = URLHelper.addParam(url, "lastdate", StringHelper.renderFileTime(lastDate));
		}
		url = URLHelper.changeMode(url, "ajax");
		logger.info("Start request to server: " + url);
		HttpGet httpget = new HttpGet(url);
		HttpResponse response = httpClient.execute(httpget);
		HttpEntity entity = response.getEntity();

		if (response.getStatusLine().getStatusCode() == 200) {
			String content = EntityUtils.toString(entity);
			logger.info("Server response: " + content);

			JSONMap ajaxMap = JSONMap.parseMap(content);

			List<NotificationContainer> notifications = ajaxMap.getMap("data").getValue("notifications",
					new TypeToken<List<NotificationContainer>>() {
					}.getType());

			List<RemoteNotification> out = new LinkedList<RemoteNotification>();
			for (NotificationContainer notificationContainer : notifications) {
				out.add(new RemoteNotification(server, notificationContainer));
			}
			return out;

		}
		return null;
	}

	public String tokenifyUrl(String simpleUrl) {
		try {
			String url = URLHelper.addParam(server.getServerURL(), "webaction", "data.oneTimeToken");
			url = URLHelper.changeMode(url, "ajax");
			logger.info("Start request to server: " + url);
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();

			if (response.getStatusLine().getStatusCode() == 200) {
				String content = EntityUtils.toString(entity);
				logger.info("Server response: " + content);

				JSONMap ajaxMap = JSONMap.parseMap(content);

				String token = ajaxMap.getMap("data").getValue("token", String.class);

				return URLHelper.addParam(simpleUrl, "j_token", token);
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Exception retreiving one time token.", ex);
		}
		return simpleUrl;
	}

}
