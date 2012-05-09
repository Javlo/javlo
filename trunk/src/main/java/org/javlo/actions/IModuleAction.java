package org.javlo.actions;

import org.javlo.context.ContentContext;
import org.javlo.module.ModuleContext;

public interface IModuleAction extends IAction {

	public String prepare(ContentContext ctx, ModuleContext moduleContext) throws Exception;
	
}
