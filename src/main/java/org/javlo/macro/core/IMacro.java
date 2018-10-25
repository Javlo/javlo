package org.javlo.macro.core;

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
	 * is this macro can be execute in preview mode.
	 * 
	 * @return
	 */
	public boolean isPreview();
	
	public boolean isAdd();
	
	public boolean isInterative();
	
	/**
	 * is macro active in current context
	 * @return
	 */
	public boolean isActive();
	
	public void init(ContentContext ctx);
	
	public String getInfo(ContentContext ctx);


}
