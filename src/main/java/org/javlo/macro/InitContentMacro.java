package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

public class InitContentMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "init-content-macro";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		MenuElement page = ctx.getCurrentPage();
		ContentContext noAreaCtx = ctx.getContextWithoutArea();
		ContentElementList comps = page.getContent(noAreaCtx);
		int countCreate = 0;
		while (comps.hasNext(noAreaCtx)) {
			IContentVisualComponent comp = comps.next(noAreaCtx);
			if (comp.isDefaultValue(noAreaCtx)) {
				comp.initContent(noAreaCtx);
				countCreate++;
			}
		}
		return "init components : "+countCreate;
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
