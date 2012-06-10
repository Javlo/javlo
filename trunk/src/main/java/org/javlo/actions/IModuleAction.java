package org.javlo.actions;

import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.user.User;

public interface IModuleAction extends IAction {

	/**
	 * method called before module rendering
	 * @param ctx the current context.
	 * @param moduleContext the context of the module.  You can call getCurrentModule for recover the module.
	 * @return eventually the error message
	 * @throws Exception
	 */
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception;
	
	/**
	 * method called when a seach in lauched from search form.
	 * @param ctx
	 * @param moduleContext the context of the module.  You can call getCurrentModule for recover the module.
	 * @param query the text entered in the search input field.
	 * @return eventually the error message
	 * @throws Exception
	 */
	public String performSearch(ContentContext ctx, ModulesContext modulesContext, String query) throws Exception;
	
	/**
	 * check if a specific user can use the module.
	 * @param user
	 * @return true if access, false if no access and null if this method can't determine access.  If access is not determined by the action javlo will take the user group in config.properties.
	 */
	public Boolean haveRight(HttpSession session, User user) throws ModuleException;
	
}
