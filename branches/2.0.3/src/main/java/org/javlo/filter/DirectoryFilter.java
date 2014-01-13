/*
 * Created on 27-dec.-2003
 */
package org.javlo.filter;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author pvandermaesen
 */
public class DirectoryFilter implements FilenameFilter {

	/**
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	@Override
	public boolean accept(File file, String name) {
		File checkFile = new File(file + "/" + name);
		return checkFile.isDirectory();
	}

}
