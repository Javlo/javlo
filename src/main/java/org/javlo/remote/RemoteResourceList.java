package org.javlo.remote;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RemoteResourceList implements Serializable {

	private static final long serialVersionUID = 2L;

	public static final String TABLE_LAYOUT = "table";
	public static final String GALLERY_LAYOUT = "gallery";
	
	private List<IRemoteResource> list = new ArrayList<IRemoteResource>();
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
	
	public static void main(String[] args) {
		XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(System.out));
		RemoteResourceList obj = new RemoteResourceList();
		obj.setLayout(GALLERY_LAYOUT);
		encoder.writeObject(obj);
		encoder.flush();
		encoder.close();
	}

}
