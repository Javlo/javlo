package org.javlo.filter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import org.javlo.helper.StringHelper;

public class ImageFileFilter implements FileFilter, FilenameFilter {

	@Override
	public boolean accept(File file) {
		return StringHelper.isImage(file.getName());
	}

	@Override
	public boolean accept(File dir, String name) {
		return StringHelper.isImage(name);
	}

}
