package org.javlo.module.demo;

import org.javlo.actions.IModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.module.ModuleContext;

public class Action implements IModuleAction {

	@Override
	public String getActionGroupName() {
		return "demo";
	}

	@Override
	public String prepare(ContentContext ctx, ModuleContext moduleContext) {
		if (ctx.getRequest().getAttribute("demoMessage") == null) {
			ctx.getRequest().setAttribute("demoMessage", "message from action prepare");
		}
		return null;
	}
	
	public static final String performTest (ContentContext ctx) {
		ctx.getRequest().setAttribute("demoMessage", "test performed");
		return null;
	}

}
