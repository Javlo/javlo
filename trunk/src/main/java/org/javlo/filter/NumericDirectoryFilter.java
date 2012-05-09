/*
 * Created on 27-d�c.-2003
 */
package org.javlo.filter;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.lang.StringUtils;

/**
 * filter for receive only direcotry composed only by number. (sample: a34 ->
 * refuse, 546 -> accept).
 * 
 * @author pvandermaesen
 */
public class NumericDirectoryFilter implements FilenameFilter {

	/**
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File file, String name) {
		return file.isDirectory() && StringUtils.isNumeric(name);
	}

}
