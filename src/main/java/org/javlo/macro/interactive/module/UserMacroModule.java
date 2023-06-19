package org.javlo.macro.interactive.module;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.macro.interactive.AbstractDisplayModule;
import org.javlo.module.user.UserAction;
import org.javlo.module.user.UserModuleContext;

public class UserMacroModule extends AbstractDisplayModule {
	
	private UserAction action = null;

	@Override
	public String getName() {
		return "users";
	}

	@Override
	protected AbstractModuleAction getModuleAction(ContentContext ctx) {
		if (action == null) {
			action = new UserAction();
		}
		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
		userContext.setMode(UserModuleContext.VIEW_USERS_LIST);

		return action;
	}
	
	@Override
	public String getIcon() {	
		return "bi bi-person";
	}

}
