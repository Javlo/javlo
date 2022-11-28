package org.javlo.macro.interactive.module;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.macro.interactive.AbstractDisplayModule;
import org.javlo.module.mailing.MailingAction;

public class MailingMacroModule extends AbstractDisplayModule {
	
	private MailingAction action = null;

	@Override
	public String getName() {
		return "mailing";
	}

	@Override
	protected AbstractModuleAction getModuleAction(ContentContext ctx) {
		if (action == null) {
			action = new MailingAction();
		}
		return action;
	}
	
	@Override
	public String getIcon() {
		return "fa fa-envelope-o";
	}

}
