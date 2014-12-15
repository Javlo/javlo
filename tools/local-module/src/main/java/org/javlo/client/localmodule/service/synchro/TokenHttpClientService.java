package org.javlo.client.localmodule.service.synchro;

import java.io.IOException;

import org.apache.http.impl.client.DefaultHttpClient;
import org.javlo.client.localmodule.service.ServerClientService;
import org.javlo.helper.URLHelper;
import org.javlo.service.syncro.HttpClientService;

public class TokenHttpClientService extends HttpClientService {

	private ServerClientService serverClient;

	public TokenHttpClientService(ServerClientService client) {
		this.serverClient = client;
		try {
			String url = URLHelper.changeMode(client.getServer().getServerURL(), "ajax");
			url = url.substring(0, url.indexOf("/ajax/"));
			setServerURL(url);
		} catch (Exception ex) {
			throw new RuntimeException("Forward exception: " + ex.getMessage(), ex);
		}
	}

	@Override
	public synchronized DefaultHttpClient getClient() {
		return serverClient.getHttpClient();
	}

	@Override
	public synchronized String retrieveRemoteLoginId() throws IOException {
		//TODO
		serverClient.tokenifyUrl("temp");
		return "done";
	}

}
