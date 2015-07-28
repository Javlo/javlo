package org.javlo.macro;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;

public class CopyLanguageStructureHereMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "copy-language-structure-here";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();

		if (currentPage != null) {
			List<ContentContext> otherLanguageContexts = new LinkedList<ContentContext>();
			for (String lg : globalContext.getLanguages()) {
				if (!lg.equals(ctx.getLanguage())) {
					ContentContext lgCtx = new ContentContext(ctx);
					lgCtx.setRequestContentLanguage(lg);
					otherLanguageContexts.add(lgCtx);
				}
			}

			MacroHelper.copyLanguageStructure(currentPage, ctx, otherLanguageContexts, true);

			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.setAskStore(true);
		}

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

}
