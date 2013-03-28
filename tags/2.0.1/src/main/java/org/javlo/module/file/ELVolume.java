package org.javlo.module.file;

public class ELVolume {
	private String id;
	private ELFile root;

	public ELVolume(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public ELFile getRoot() {
		return root;
	}

	public void setRoot(ELFile root) {
		this.root = root;
	}

}