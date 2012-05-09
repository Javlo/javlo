package org.javlo.service.syncro;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionManagerFactory;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.servlet.SynchronisationServlet;



/**
 * Service managing all http request to a javlo server.
 * @author bdumont
 */
public class HttpClientService {

	private static final Logger logger = Logger.getLogger(HttpClientService.class.getName());

	DefaultHttpClient client;
	HttpContext context;

	public final Object lock = this;

	private String serverURL = null;
	private String synchroCode;
	private String username;
	private String password;
	private String proxyHost = null;
	private Integer proxyPort = null;

	public String getServerURL() {
		return serverURL;
	}
	public synchronized void setServerURL(String serverURL) {
		if (serverURL == null ? this.serverURL != null : !serverURL.equals(this.serverURL)) {
			this.serverURL = serverURL;
			setClient(null);
		}
	}

	public String getSynchroCode() {
		return synchroCode;
	}
	public void setSynchroCode(String synchroCode) {
		this.synchroCode = synchroCode;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
		//TODO logout?
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getProxyHost() {
		return proxyHost;
	}
	public synchronized void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
		refreshProxy();
	}

	public Integer getProxyPort() {
		return proxyPort;
	}
	public synchronized void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
		refreshProxy();
	}

	private void refreshProxy() {
		if (client != null) {
			if (proxyHost != null && proxyPort != null && proxyPort > 0) {
				HttpHost proxy = new HttpHost(proxyHost, proxyPort, Protocol.getProtocol("http"));
				client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			} else {
				client.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
			}
		}
	}

	public synchronized DefaultHttpClient getClient() {
		if (client == null) {
			client = new DefaultHttpClient();
			//client.getParams().setParameter(ClientPNames.CONNECTION_MANAGER_FACTORY_CLASS_NAME, HttpClientServiceClientConnectionManagerFactory.class.getName());
			refreshProxy();
		}
		return client;
	}
	public HttpContext getContext() {
		if (context == null) {
			context = new BasicHttpContext();
		}
		return context;
	}
	protected synchronized void setClient(DefaultHttpClient client) {
		if (this.client != client && this.client != null) {
			this.client.getConnectionManager().shutdown();
			context = null;
		}
		this.client = client;
	}

	public String encodeURL(String relativeURL) {
		if (serverURL == null) {
			throw new NullPointerException("serverURL is null");
		}
		String out = URLHelper.mergePath(serverURL, relativeURL);
		if (synchroCode != null) {
			out = URLHelper.addParam(out, SynchronisationServlet.SHYNCRO_CODE_PARAM_NAME, synchroCode);
		}
		return out;
	}

	public synchronized String downloadAsString(String relativeURL) throws IOException {
		HttpResponse resp = null;
		try {
			String url = encodeURL(relativeURL);
			HttpGet req = new HttpGet(url);
			resp = execute(req);
			HttpEntity entity = resp.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity);
			}
			return null;
		} finally {
			safeConsume(resp);
		}
	}

	public synchronized StatusLine callURL(String relativeURL) throws ClientProtocolException, IOException {
		HttpResponse resp = null;
		try {
			String url = encodeURL(relativeURL);
			HttpGet req = new HttpGet(url);
			resp = execute(req);
			return resp.getStatusLine();
		} finally {
			safeConsume(resp);
		}
	}

	public HttpResponse execute(HttpUriRequest request) throws ClientProtocolException, IOException {
		HttpResponse resp = getClient().execute(request, getContext());
		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
			safeConsume(resp);
			logger.info("auto login " + username + " on " + getServerURL());
			String loginId = retrieveRemoteLoginId();
			if (loginId == null) {
				//TODO better
				throw new ClientProtocolException("401 UNAUTHORIZED");
			}
			resp = getClient().execute(request, getContext());
		}
		return resp;
	}

	public void safeConsume(HttpResponse resp) {
		if (resp != null) {
			HttpEntity entity = resp.getEntity();
			if (entity != null) {
				try {
					entity.consumeContent();
				} catch (IOException ex) {
					//Ignore exception
				}
			}
		}
	}

	public synchronized String retrieveRemoteLoginId() throws IOException {
		HttpResponse resp = null;
		try {
			String relativeURL = "/jsp/remoteservice/remote_login.jsp";
			relativeURL = URLHelper.addParam(relativeURL, "login", URLEncoder.encode(username, ContentContext.CHARACTER_ENCODING));
			relativeURL = URLHelper.addParam(relativeURL, "password", URLEncoder.encode(password, ContentContext.CHARACTER_ENCODING));

			String url = encodeURL(relativeURL);
			HttpGet req = new HttpGet(url);
			resp = getClient().execute(req, getContext());
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = resp.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity);
				}
			}
			return null;
		} finally {
			safeConsume(resp);
		}
	}

	public static class HttpClientServiceClientConnectionManagerFactory implements ClientConnectionManagerFactory {
		@Override
		public ClientConnectionManager newInstance(org.apache.http.params.HttpParams params, SchemeRegistry schemeRegistry) {
			return new ThreadSafeClientConnManager(params, schemeRegistry);
		}

	}

}
