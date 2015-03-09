package org.javlo.module.remote;

import java.util.LinkedList;
import java.util.List;

public class RemoteInstance {

	private String port;
	private List<RemoteBean> sites = new LinkedList<RemoteBean>();

	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}

	public List<RemoteBean> getSites() {
		return sites;
	}
	public void setSites(List<RemoteBean> sites) {
		this.sites = sites;
	}

}
