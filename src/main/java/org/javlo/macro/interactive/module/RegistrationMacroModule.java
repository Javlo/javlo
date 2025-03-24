package org.javlo.macro.interactive.module;

import jakarta.servlet.http.HttpSession;
import org.javlo.actions.IModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.macro.interactive.AbstractDisplayModule;
import org.javlo.module.components.ComponentsAction;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.user.User;

public class RegistrationMacroModule extends AbstractDisplayModule implements IModuleAction {
	
	private ComponentsAction action = null;

	@Override
	public String getName() {
		return "registration";
	}
	
	@Override
	public String prepare(ContentContext ctx) {
		System.out.println("RegistrationMacroModule.prepare");
		return null;
	}

	@Override
	public String getRenderer() {
		return "/jsp/preview/modules/registration/main_registration.jsp";
	}

	@Override
	protected IModuleAction getModuleAction(ContentContext ctx) {
		return this;
	}

	@Override
	public String getIcon() {
		return "bi bi-person-check";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception {
		return "";
	}

	@Override
	public String performSearch(ContentContext ctx, ModulesContext modulesContext, String query) throws Exception {
		return "";
	}

	@Override
	public Boolean haveRight(HttpSession session, User user) throws ModuleException {
		return null;
	}
}
