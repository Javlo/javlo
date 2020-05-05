package org.javlo.security.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface HasAllRole {
	
	String[] roles() default {};

}
