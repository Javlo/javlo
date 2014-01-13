package org.javlo.macro.core;

import org.javlo.context.ContentContext;

public interface IInteractiveMacro extends IMacro {
	
	/**
	 * get the renderer for interactive macro (return null if macro is'nt interactive)
	 * 
	 * @return
	 */
	public String getRenderer();
	
	public String prepare(ContentContext ctx);

}
