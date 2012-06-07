package org.javlo.actions;

import org.javlo.context.ContentContext;
import org.javlo.module.core.ModulesContext;

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
	public String performSearch(ContentContext ctx, ModulesContext moduleContext, String query) throws Exception;
	
}
