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
public class JSPFilter implements FilenameFilter {
	
	static final Pattern pattern = Pattern.compile("(.*\\.JSP)|(.*\\.jsp)");

	/**
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File file, String name) {		
		return pattern.matcher(name).matches();
	}

}
