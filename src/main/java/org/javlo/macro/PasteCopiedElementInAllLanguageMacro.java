package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ClipBoard;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;

public class PasteCopiedElementInAllLanguageMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "paste-copied-comp-in-all-lg";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();
		ClipBoard clipBoard = ClipBoard.getInstance(ctx.getRequest());

		if (!clipBoard.isEmpty(ctx)) {
			ComponentBean bean = (ComponentBean) clipBoard.getCopied();
			String beanLg = bean.getLanguage();
			if (currentPage != null) {
				for (String lg : globalContext.getContentLanguages()) {
					ContentContext lgCtx = new ContentContext(ctx);
					lgCtx.setContentLanguage(lg);
					lgCtx.setRequestContentLanguage(lg);
					if (!beanLg.equals(lg)) {
						ContentService content = ContentService.getInstance(ctx.getRequest());
						bean.setArea(ctx.getArea());
						bean.setLanguage(lg);
						content.createContent(lgCtx, bean, null, true);
					}
				}
			}
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
