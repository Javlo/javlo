package org.javlo.macro.interactive.module;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.macro.interactive.AbstractDisplayModule;
import org.javlo.module.user.UserAction;
import org.javlo.module.user.UserModuleContext;

public class UserMacroModule extends AbstractDisplayModule {
	
	private UserAction action = null;
	private boolean admin = false;

	public UserMacroModule(boolean admin) {
		super();
		this.admin = admin;
	}


	@Override
	public String getName() {
		if (admin) {
			return "admin-users";
		} else {
			return "users";
		}
	}

	@Override
	public String getModuleName() {
		return "users";
	}

	@Override
	protected AbstractModuleAction getModuleAction(ContentContext ctx) {
		if (action == null) {
			action = new UserAction();
		}
		UserModuleContext userContext = UserModuleContext.getInstance(ctx.getRequest());
		if (admin) {
			userContext.setMode(UserModuleContext.ADMIN_USERS_LIST);
		} else {
			userContext.setMode(UserModuleContext.VIEW_USERS_LIST);
		}
		return action;
	}
	
	@Override
	public String getIcon() {
		if (admin) {
			return "bi bi-person-gear";
		} else {
			return "bi bi-person";
		}
	}

}
