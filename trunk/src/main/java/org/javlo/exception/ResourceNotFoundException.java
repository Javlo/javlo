/*
 * Created on 04-janv.-2004
 */
package org.javlo.exception;

/**
 * @author pvandermaesen
 * Exception when a ressource can not be found.
 */
public class ResourceNotFoundException extends Exception {

	public ResourceNotFoundException(Throwable cause) {
		super(cause);
	}

	public ResourceNotFoundException(String msg) {
		super(msg);
	}
}
