package org.javlo.macro.interactive.module;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.macro.interactive.AbstractDisplayModule;
import org.javlo.module.template.TemplateAction;

public class TemplateMacroModule extends AbstractDisplayModule {
	
	private TemplateAction action = null;

	@Override
	public String getName() {
		return "template";
	}
	
	@Override
	public String prepare(ContentContext ctx) {
		String out = null;
		try {
			((TemplateAction)getModuleAction(ctx)).selectTemplateForEdit(ctx, ctx.getCurrentPage().getTemplateIdOnInherited(ctx));
			out = super.prepare(ctx);
			ctx.getRequest().setAttribute("forceActionBar", true);
			((TemplateAction)getModuleAction(ctx)).selectTemplateForEdit(ctx, ctx.getCurrentPage().getTemplateIdOnInherited(ctx));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	@Override
	protected AbstractModuleAction getModuleAction(ContentContext ctx) {
		if (action == null) {
			action = new TemplateAction();
		}
		return action;
	}
	
}
