package org.javlo.data;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * @author Gilles Hooghe ($Author: plemarch $)
 * @version $Revision: 1.1 $ - $Date: 2011-02-09 17:55:12 $
 * 
 * Annotation to be used as descriptor for Beans mapped into a Database.
 */
@Retention(RUNTIME)
public @interface Persistence {

	public String name();
	public int length() default 32;
	
	/**
	 * Is this field part of the primary key field(s)
	 */
	public boolean isPK() default false;
	
}
