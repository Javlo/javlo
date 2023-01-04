package org.javlo.macro.interactive.module;

import org.javlo.actions.IModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.macro.interactive.AbstractDisplayModule;
import org.javlo.module.components.ComponentsAction;
import org.javlo.module.core.ModulesContext;

public class ComponentsMacroModule extends AbstractDisplayModule {
	
	private ComponentsAction action = null;

	@Override
	public String getName() {
		return "components";
	}
	
	@Override
	public String prepare(ContentContext ctx) {
		String out = null;
		try {
			getModuleAction(ctx).prepare(ctx, ModulesContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	@Override
	protected IModuleAction getModuleAction(ContentContext ctx) {
		if (action == null) {
			action = new ComponentsAction();
		}
		return action;
	}
	
}
