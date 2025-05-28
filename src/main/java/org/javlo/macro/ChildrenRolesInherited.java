package org.javlo.macro;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;

import java.util.Map;
import java.util.logging.Logger;

public class ChildrenRolesInherited extends AbstractMacro {

	private static Logger logger = Logger.getLogger(ChildrenRolesInherited.class.getName());

	@Override
	public String getName() {
		return "children-role-inherited";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		for(MenuElement child : ctx.getCurrentPage().getChildMenuElements()) {
			childrenInherited(child);
		}
		PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		return null;
	}

	private static void childrenInherited(MenuElement page) throws Exception {
		page.setUserRolesInherited(true);
		for(MenuElement child : page.getChildMenuElements()) {
			childrenInherited(child);
		}
	}

	@Override
	public boolean isPreview() {
		return true;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}
};
