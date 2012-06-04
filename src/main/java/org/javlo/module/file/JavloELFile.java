package org.javlo.module.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class JavloELFile extends ELFile {

	private File file;

	public JavloELFile(ELVolume volume, File file) {
		super(volume);
		this.file = file;
	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public List<ELFile> getChildren() {
		List<ELFile> children = new ArrayList<ELFile>();
		File[] array = file.listFiles();
		if (array != null) {
			for (File child : array) {
				children.add(new JavloELFile(getVolume(), child));
			}
		}
		return children;
	}

	@Override
	public JavloELFile getParentFile() {
		if (isRoot()) {
			return null;
		} else {
			return new JavloELFile(getVolume(), file.getParentFile());
		}
	}

	@Override
	public String getURL() {
		//TODO
		return null;
	}

	@Override
	public String getThumbnailURL() {
		//TODO
		return null;
	}

}
