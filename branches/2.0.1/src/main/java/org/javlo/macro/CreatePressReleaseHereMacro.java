package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.helper.MacroHelper;
import org.javlo.navigation.MenuElement;

public class CreatePressReleaseHereMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "create-press-release-here";
	}

	public String getPageStructureName() {
		return "press-release";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		MenuElement newPage = MacroHelper.createArticlePageName(ctx, currentPage);
		if (newPage != null) {
			MacroHelper.addContentInPage(ctx, newPage, getPageStructureName());
		}
		return null;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
};
