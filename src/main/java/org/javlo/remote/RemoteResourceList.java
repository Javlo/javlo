package org.javlo.remote;

import java.io.Serializable;
import java.util.List;

public class RemoteResourceList implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_LAYOUT = "table";
	public static final String GALLERY_LAYOUT = "gallery";
	
	private List<IRemoteResource> list;
	private String layout = TABLE_LAYOUT;

	public RemoteResourceList() {
	}

	public List<IRemoteResource> getList() {
		return list;
	}

	public void setList(List<IRemoteResource> list) {
		this.list = list;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

}
