package org.javlo.macro;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.service.PersistenceService;

import java.util.Map;

public class DeletePageContent extends AbstractMacro {

	@Override
	public String getName() {
		return "delete-page-content";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		ContentContext ctxNoArea = ctx.getContextWithArea(null);
		ContentElementList content = ctx.getCurrentPage().getContent(ctxNoArea);
		while (content.hasNext(ctxNoArea)) {
			IContentVisualComponent comp = content.next(ctxNoArea);
			ctx.getCurrentPage().removeContent(ctxNoArea, comp.getId());
		}
		PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);

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
	public String getIcon() {
		return "bi bi-trash";
	}
};
