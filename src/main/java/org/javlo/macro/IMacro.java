package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;

public interface IMacro {

	public String getName();

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception;

	/**
	 * macro for admin >> return true macro for contributor >> retrun false
	 * 
	 * @return
	 */
	public boolean isAdmin();

	/**
	 * get the renderer for interactive macro (return null if macro is'nt interactive)
	 * 
	 * @return
	 */
	public String getRenderer();

}
