package org.javlo.module.remote;

import java.util.LinkedList;
import java.util.List;

public class RemoteInstance {

	private String port;

	private String systemUser;

	private String version;

	private List<RemoteBean> sites = new LinkedList<RemoteBean>();

	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}

	public String getSystemUser() {
		return systemUser;
	}
	public void setSystemUser(String systemUser) {
		this.systemUser = systemUser;
	}

	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	public List<RemoteBean> getSites() {
		return sites;
	}
	public void setSites(List<RemoteBean> sites) {
		this.sites = sites;
	}

}
