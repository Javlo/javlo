package org.javlo.macro;

import java.util.Collection;
import java.util.LinkedList;
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
		NavigationService service = NavigationService.getInstance(globalContext);
		
		Collection<MenuElement> mustDelete = new LinkedList<MenuElement>(ctx.getCurrentPage().getChildMenuElements());
		for (MenuElement menuElement : mustDelete) {
			service.removeNavigationNoStore(ctx, menuElement);
		}
		mustDelete=null;

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

};
