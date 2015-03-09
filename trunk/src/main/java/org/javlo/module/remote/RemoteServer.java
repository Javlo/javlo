package org.javlo.module.remote;

import java.util.LinkedList;
import java.util.List;

public class RemoteServer {

	private String address;
	private String hostname;
	private List<RemoteInstance> instances = new LinkedList<RemoteInstance>();

	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}

	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public List<RemoteInstance> getInstances() {
		return instances;
	}
	public void setInstances(List<RemoteInstance> instances) {
		this.instances = instances;
	}

}
