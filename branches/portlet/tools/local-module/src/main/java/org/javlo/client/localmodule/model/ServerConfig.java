package org.javlo.client.localmodule.model;

import java.net.URL;

public class ServerConfig {

	private String label;
	private String serverURL;
	private String title;
	private ServerType type;
	private String checkPhrase;

	public String getLabel() {
		if (label == null) {
			label = title;
			if (label == null || label.trim().isEmpty()) {
				label = getLabelFromUrl(serverURL);
				if (label == null || label.trim().isEmpty()) {
					label = serverURL;
				}
			}
		}
		return label;
	}

	public static String getLabelFromUrl(String serverURL) {
		String label;
		try {
			URL url = new URL(serverURL);
			label = url.getHost();
			if (url.getPort() >= 0 && url.getPort() != 80) {
				label += ":" + url.getPort();
			}
		} catch (Exception ignored) {
			label = null;
		}
		return label;
	}

	public String getServerURL() {
		return serverURL;
	}
	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public ServerType getType() {
		return type;
	}
	public void setType(ServerType type) {
		this.type = type;
	}

	public String getCheckPhrase() {
		return checkPhrase;
	}
	public void setCheckPhrase(String checkPhrase) {
		this.checkPhrase = checkPhrase;
	}

	public ServerConfig copy() {
		ServerConfig out = new ServerConfig();
		out.setServerURL(getServerURL());
		out.setTitle(getTitle());
		out.setType(getType());
		out.setCheckPhrase(getCheckPhrase());
		return out;
	}

	@Override
	public String toString() {
		return getServerURL();
	}

}
