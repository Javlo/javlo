package org.javlo.macro;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;

public class CopyLanguageStructureMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "copy-language-structure";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		ContentService content = ContentService.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		MenuElement rootPage = content.getNavigation(ctx);

		List<ContentContext> otherLanguageContexts = new LinkedList<ContentContext>();
		for (String lg : globalContext.getLanguages()) {
			if (!lg.equals(ctx.getLanguage())) {
				ContentContext lgCtx = new ContentContext(ctx);
				lgCtx.setRequestContentLanguage(lg);
				otherLanguageContexts.add(lgCtx);
			}
		}

		MacroHelper.copyLanguageStructure(rootPage, ctx, otherLanguageContexts, true);
		for (MenuElement child : rootPage.getAllChildren()) {
			MacroHelper.copyLanguageStructure(child, ctx, otherLanguageContexts, true);
		}

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

}
