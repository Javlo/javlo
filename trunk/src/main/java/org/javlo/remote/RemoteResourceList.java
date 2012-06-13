package org.javlo.remote;

import java.io.Serializable;
import java.util.List;

public class RemoteResourceList implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<IRemoteResource> list;

	public RemoteResourceList() {
	}

	public List<IRemoteResource> getList() {
		return list;
	}

	public void setList(List<IRemoteResource> list) {
		this.list = list;
	}

}
