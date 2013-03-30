package org.javlo.client.localmodule.model;

import java.net.URL;

public class ServerConfig {

	private String serverURL;
	private String title;

	public String getTitle() {
		if (title == null) {
			try {
				URL url = new URL(getServerURL());
				title = url.getHost();
			} catch (Exception ignore) {
				//ignore
			}
		}
		return title;
	}

	public String getServerURL() {
		return serverURL;
	}
	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	@Override
	public String toString() {
		return getServerURL();
	}

}
