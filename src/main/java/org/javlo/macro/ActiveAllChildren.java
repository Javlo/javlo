package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;

public class ActiveAllChildren extends AbstractMacro {

	@Override
	public String getName() {
		return "active-children";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		
		for (MenuElement menuElement : ctx.getCurrentPage().getAllChildren()) {
			menuElement.setActive(true);
		}		

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.setAskStore(true);

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
