package org.javlo.module.file;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.helper.URLHelper;

public abstract class ELFile {
	
	public static final Comparator<ELFile> FILE_NAME_COMPARATOR = new ELFileNameComparator();
	
	private static class ELFileNameComparator implements Comparator<ELFile> {

		@Override
		public int compare(ELFile f1, ELFile f2) {
			return f1.getFile().getName().compareTo(f2.getFile().getName());
		}
		
	}

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
	
	public String getPath() {
		if (getParentFile() != null) {
			return URLHelper.mergePath(getParentFile().getPath(),getFile().getName());
		} else {
			return "/";
		}
	}
	
	public void updateInfo(HttpServletRequest request, HttpServletResponse respones) {
		return;
	}
}