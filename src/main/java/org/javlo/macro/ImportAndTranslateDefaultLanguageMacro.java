package org.javlo.macro;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;

public class ImportAndTranslateDefaultLanguageMacro extends AbstractMacro {

	private static Logger logger = Logger.getLogger(ImportAndTranslateDefaultLanguageMacro.class.getName());
	
	private boolean active = false;

	@Override
	public String getName() {
		return "import-and-translate-default-language";
	}
	
	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentContext deftLanguageCtx = new ContentContext(ctx);
		deftLanguageCtx.setLanguage(globalContext.getDefaultLanguages().iterator().next());
		deftLanguageCtx.setRequestContentLanguage(globalContext.getDefaultLanguages().iterator().next());
		MenuElement currentPage = ctx.getCurrentPage();
		if (currentPage.isRealContent(ctx)) {
			logger.info(currentPage.getPath() + " have already content.");
			return null;
		}
		MacroHelper.copyLanguageStructure(currentPage, deftLanguageCtx, Arrays.asList(new ContentContext[] { ctx }), true, true);
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
	
	@Override
	public void init(ContentContext ctx) {
		super.init(ctx);
		active = !StringHelper.isEmpty(ctx.getGlobalContext().getSpecialConfig().getTranslatorGoogleApiKey());
		if (!active) {
			active = !StringHelper.isEmpty(ctx.getGlobalContext().getSpecialConfig().getTranslatorDeepLApiKey());
		}
	}
	
	@Override
	public boolean isActive() {
		return active;
	}
	
	@Override
	public String getIcon() {
		return "bi bi-globe";
	}
	

}
