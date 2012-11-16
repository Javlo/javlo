package org.javlo.helper.filefilter;

import java.io.File;
import java.io.FileFilter;

import org.javlo.helper.StringHelper;

public class SoundFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		return StringHelper.isSound(file.getName());
	}

}
