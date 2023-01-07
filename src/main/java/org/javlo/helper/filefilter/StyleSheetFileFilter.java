package org.javlo.helper.filefilter;

import java.io.File;
import java.io.FileFilter;

import org.javlo.helper.StringHelper;

public class StyleSheetFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		String ext = StringHelper.getFileExtension(file.getName());
		if (StringHelper.isEmpty(ext)) {
			return false;
		} else {
			ext = ext.toLowerCase();
			return ext.equals("css") || ext.equals("scss");
		}
	}

}
