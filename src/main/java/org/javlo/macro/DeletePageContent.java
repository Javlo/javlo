package org.javlo.macro;

import org.javlo.component.core.ContentElementList;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.service.NavigationService;

import java.util.Map;

public class DeletePageContent extends AbstractMacro {

	@Override
	public String getName() {
		return "delete-page-content";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NavigationService service = NavigationService.getInstance(globalContext);

		ContentContext ctxNoArea = ctx.getContextWithArea(null);
		ContentElementList content = ctx.getCurrentPage().getContent(ctxNoArea);
		while (content.hasNext(ctxNoArea)) {
			content.next(ctxNoArea).delete(ctxNoArea);
		}

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
