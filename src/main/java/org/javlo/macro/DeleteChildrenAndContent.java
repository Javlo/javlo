package org.javlo.macro;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;

public class DeleteChildrenAndContent extends AbstractMacro {

	@Override
	public String getName() {
		return "delete-children-and-content";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NavigationService service = NavigationService.getInstance(globalContext);

		ctx.getCurrentPage().setContent(new ComponentBean[0]);

		Collection<MenuElement> pages = new LinkedList<MenuElement>(ctx.getCurrentPage().getChildMenuElements());
		for (MenuElement menuElement : pages) {
			service.removeNavigationNoStore(ctx, menuElement);
		}
		pages = null;

		ctx.getCurrentPage().releaseCache();

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
		return false;
	}

};
