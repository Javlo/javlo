/*
 * Created on 29-d�c.-2003
 */
package org.javlo.component.core;

import javax.servlet.ServletContext;

/**
 * @author pvandermaesen
 * if a component implement this interface, you can create CSS class for the mcomponent.
 * this class must be insered in a spacial global css file.
 */
public interface ICSS {
	
	/**
	 * create CSS code for define class for a specific component.
	 * @return a String contain CSS definition.
	 */
	public String getCSSCode(ServletContext application) throws Exception ;

}
