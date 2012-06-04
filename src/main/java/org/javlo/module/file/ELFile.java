package org.javlo.module.file;

import java.io.File;
import java.util.List;

public abstract class ELFile {

	private ELVolume volume;

	protected ELFile(ELVolume volume) {
		this.volume = volume;
	}

	public abstract String getURL();

	public abstract String getThumbnailURL();

	public ELVolume getVolume() {
		return volume;
	}
	public abstract File getFile();

	public abstract List<ELFile> getChildren();

	public abstract ELFile getParentFile();

	public boolean isRoot() {
		return volume.getRoot().getFile().equals(getFile());
	}

	public boolean isDirectory() {
		return getFile().isDirectory();
	}

	public String getRelativePath() {
		String path = "";
		ELFile parent = getParentFile();
		if (parent != null) {
			path += parent.getRelativePath() + "/";
		}
		path += getFile().getName();
		return path;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj == this) {
			return true;
		} else if (!(obj instanceof ELFile)) {
			return false;
		} else {
			return obj.hashCode() == this.hashCode();
		}
	}

	@Override
	public int hashCode() {
		return getFile().hashCode();
	}

	@Override
	public String toString() {
		return getFile().toString();
	}
}