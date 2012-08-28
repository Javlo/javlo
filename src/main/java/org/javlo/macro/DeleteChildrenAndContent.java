package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;

public class DeleteChildrenAndContent extends AbstractMacro {

	public String getName() {
		return "delete-children-and-content";
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NavigationService service = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
		
		ctx.getCurrentPage().setContent(new ComponentBean[0]);

		MenuElement[] pages = ctx.getCurrentPage().getChildMenuElements();
		for (int i = 0; i < pages.length; i++) {
			service.removeNavigationNoStore(ctx, pages[i]);
		}

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);

		return null;
	}

};
