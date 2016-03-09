package org.javlo.macro;

import java.util.Map;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;

public class DeleteTestPage extends AbstractMacro {
	
	private static Logger logger = Logger.getLogger(DeleteTestPage.class.getName());
	
	private static final String TEST_PAGE_TOKEN = "__test__";

	@Override
	public String getName() {
		return "delete-test-page";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		
		String token = getMacroProperties(ctx).getProperty("token", TEST_PAGE_TOKEN);

		ContentService content = ContentService.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NavigationService service = NavigationService.getInstance(globalContext);

		MenuElement[] pages = content.getNavigation(ctx).getAllChildren();
		for (MenuElement page : pages) {			
			if (page.getName().contains(token)) {
				logger.info("delete page : "+page.getName());
				service.removeNavigationNoStore(ctx, page);				
			}
		}

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx, false);
		ContentService.clearCache(ctx, globalContext);
		System.gc();

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

};
