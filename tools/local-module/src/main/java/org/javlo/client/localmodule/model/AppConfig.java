package org.javlo.client.localmodule.model;


public class AppConfig {

	private ServerConfig[] servers;

	private String computerName;

	private String proxyHost;
	private Integer proxyPort;
	private String proxyUsername;
	private String proxyPassword;

	public boolean isValid() {
		return true
				&& servers != null && servers.length > 0;
	}

	public ServerConfig[] getServers() {
		return servers;
	}

	public void setServers(ServerConfig[] servers) {
		this.servers = servers;
	}

	public String getComputerName() {
		return computerName;
	}

	public void setComputerName(String computerName) {
		this.computerName = computerName;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUsername() {
		return proxyUsername;
	}

	public void setProxyUsername(String proxyUsername) {
		this.proxyUsername = proxyUsername;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

}
