package org.javlo.actions;

import org.javlo.context.ContentContext;
import org.javlo.module.ModuleContext;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class AbstractModuleAction implements IModuleAction {

	@Override
	public String prepare(ContentContext ctx, ModuleContext moduleContext) throws Exception {
		return null;
	}
	
	@Override
	public String performSearch(ContentContext ctx, ModuleContext moduleContext, String query) throws Exception {	
		throw new NotImplementedException();
	}

}
