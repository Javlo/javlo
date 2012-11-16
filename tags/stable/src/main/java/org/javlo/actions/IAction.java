/*
 * Created on 29-d�c.-2003
 */
package org.javlo.actions;

/**
 * @author pvandermaesen
 * when a composent implement this interface, the action manager
 * can call perform${action-name} method in the component when this
 * action groupe is called.  For call an action you must specify
 * a parameter in the request 'webaction' = ${group-name}.${action-name}.
 */
public interface IAction {
	
	/**
	 * the group name of the action
	 * @return a group name.
	 */
	public String getActionGroupName();

}
