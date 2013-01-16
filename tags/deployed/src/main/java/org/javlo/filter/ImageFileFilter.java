package org.javlo.filter;

import java.io.File;
import java.io.FileFilter;

import org.javlo.helper.StringHelper;

public class ImageFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		return StringHelper.isImage(file.getName());
	}

}
