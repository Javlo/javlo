package org.javlo.macro;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;

public class ImportDefaultLanguageMacro extends AbstractMacro {
	
	private static Logger logger = Logger.getLogger(ImportDefaultLanguageMacro.class.getName());

	@Override
	public String getName() {
		return "import-default-language";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		
		ContentContext deftLanguageCtx = new ContentContext(ctx);
		deftLanguageCtx.setLanguage(globalContext.getDefaultLanguages().iterator().next());
		deftLanguageCtx.setRequestContentLanguage(globalContext.getDefaultLanguages().iterator().next());
		
		MenuElement currentPage = ctx.getCurrentPage();
		
		if (currentPage.isRealContent(ctx)) {
			logger.info(currentPage.getPath()+" have allready content.");
			return null;
		}

		MacroHelper.copyLanguageStructure(currentPage, deftLanguageCtx, Arrays.asList(new ContentContext[] {ctx}), true);

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);

		return null;
	}

}
