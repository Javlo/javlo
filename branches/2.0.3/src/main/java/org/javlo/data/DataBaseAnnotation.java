package org.javlo.data;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface DataBaseAnnotation {
	
	public String type();
	public String name();
	public boolean isPrimaryKey() default false;
	
}
