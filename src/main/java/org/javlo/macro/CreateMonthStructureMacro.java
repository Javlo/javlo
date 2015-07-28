package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;

public class CreateMonthStructureMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "create-month-structure-here";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		MenuElement yearPage = ctx.getCurrentPage();

		MacroHelper.createMonthStructure(ctx, yearPage);

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.setAskStore(true);

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

}
