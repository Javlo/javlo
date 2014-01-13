/*
 * Created on 27-d�c.-2003
 */
package org.javlo.filter;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author pvandermaesen
 */
public class NotDirectoryFilter implements FilenameFilter {
	
	/**
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File file, String name) {
        File checkFile = new File(file+"/"+name);
		return !checkFile.isDirectory();
	}

}
