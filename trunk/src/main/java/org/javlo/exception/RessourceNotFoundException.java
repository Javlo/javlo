/*
 * Created on 04-janv.-2004
 */
package org.javlo.exception;

/**
 * @author pvandermaesen
 * Exception when a ressource can not be found.
 */
public class RessourceNotFoundException extends Exception {

	public RessourceNotFoundException(Throwable cause) {
		super(cause);
	}

	public RessourceNotFoundException(String msg) {
		super(msg);
	}
}
