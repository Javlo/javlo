package org.javlo.helper.filefilter;

import java.io.File;
import java.io.FileFilter;

import org.javlo.helper.StringHelper;

public class HTMLFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		return StringHelper.isHTML(file.getName());
	}

}
