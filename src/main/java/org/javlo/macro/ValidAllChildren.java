package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;
import org.javlo.user.AdminUserSecurity;

public class ValidAllChildren extends AbstractMacro {

	@Override
	public String getName() {
		return "valid-all-children";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		AdminUserSecurity userSecurity = AdminUserSecurity.getInstance();
		if (userSecurity.canRole(ctx.getCurrentEditUser(), AdminUserSecurity.VALIDATION_ROLE)) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

			for (MenuElement menuElement : ctx.getCurrentPage().getAllChildrenList()) {
				menuElement.setValid(true);
			}

			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.setAskStore(true);
		} else {
			return "security error !";
		}

		return null;
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
