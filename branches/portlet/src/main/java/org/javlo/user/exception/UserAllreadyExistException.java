/**
 * Created on 09-mars-2004
 */
package org.javlo.user.exception;

/**
 * @author pvandermaesen
 */
public class UserAllreadyExistException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public UserAllreadyExistException ( String msg ) {
		super(msg);
	}

}
