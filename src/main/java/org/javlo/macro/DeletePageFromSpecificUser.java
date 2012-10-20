package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;


public class DeletePageFromSpecificUser extends AbstractMacro {
	
	private static final String USER_NAME = "newtest-pluxl13";

	public String getName() {
		return "delete-page-of-"+USER_NAME;
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		ContentService content = ContentService.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NavigationService service = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
		

		MenuElement[] pages = content.getNavigation(ctx).getAllChildren();
		for (int i = 0; i < pages.length; i++) {
			if (pages[i].getCreator().equals(USER_NAME)) {
				if (pages[i].getChildMenuElements().length == 0) {
					service.removeNavigationNoStore(ctx, pages[i]);
				}
			}
		}
		
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);

		return null;
	}

};
