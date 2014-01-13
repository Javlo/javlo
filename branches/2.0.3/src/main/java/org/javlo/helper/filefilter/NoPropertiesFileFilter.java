package org.javlo.helper.filefilter;

import java.io.File;
import java.io.FileFilter;

import org.javlo.helper.StringHelper;

/**
 * exclude propoerties files.
 * @author Patrick Vandermaesen
 *
 */
public class NoPropertiesFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		return !StringHelper.isProperties(file.getName());
	}

}
