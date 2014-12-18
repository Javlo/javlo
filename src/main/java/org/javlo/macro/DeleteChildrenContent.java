package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;

public class DeleteChildrenContent extends AbstractMacro {

	@Override
	public String getName() {
		return "delete-children-content";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());		
		for (MenuElement menuElement : ctx.getCurrentPage().getAllChildren()) {
			MacroHelper.deleteContentByLanguage(ctx, menuElement, ctx.getRequestContentLanguage());
		}
		ctx.getCurrentPage().releaseCache();
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);

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
