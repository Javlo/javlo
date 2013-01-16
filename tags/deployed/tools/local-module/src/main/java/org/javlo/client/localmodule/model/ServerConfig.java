package org.javlo.client.localmodule.model;

public class ServerConfig {

	private String serverURL;

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
