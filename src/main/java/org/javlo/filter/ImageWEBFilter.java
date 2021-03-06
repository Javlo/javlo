/*
 * Created on 27-d�c.-2003
 */
package org.javlo.filter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * @author pvandermaesen
 */
public class ImageWEBFilter implements FilenameFilter {
	
	static final Pattern pattern = Pattern.compile("(.*\\.jpg)|(.*\\.JPG)|(.*\\.jpeg)|(.*\\.JPEG)|(.*\\.gif)|(.*\\.GIF)|(.*\\.png)|(.*\\.PNG)");

	/**
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File file, String name) {		
		return pattern.matcher(name).matches();
	}

}
