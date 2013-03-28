package org.javlo.macro;

import java.util.Collection;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;

public class DeleteChildren extends AbstractMacro {

	@Override
	public String getName() {
		return "delete-children";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NavigationService service = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());

		Collection<MenuElement> pages = ctx.getCurrentPage().getChildMenuElements();
		for (MenuElement menuElement : pages) {
			service.removeNavigationNoStore(ctx, menuElement);
		}

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

};
