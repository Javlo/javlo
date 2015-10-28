package org.javlo.filter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import org.javlo.helper.StringHelper;

public class ImageFileFilter implements FileFilter, FilenameFilter {
	
	private boolean reverse = false;

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return false;
		} else {
			return StringHelper.isImage(file.getName()) ^ reverse;
		}
	}

	@Override
	public boolean accept(File dir, String name) {		
		return StringHelper.isImage(name) ^ reverse;
	}

	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

}
