package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class DeleteBadTemplate extends AbstractMacro {

	@Override
	public String getName() {
		return "delete-bad-template";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService content = ContentService.getInstance(globalContext);
		Map<String, Template> templates = TemplateFactory.getDiskTemplates(ctx.getRequest().getSession().getServletContext());
		int modif = 0;
		for (MenuElement page : content.getNavigation(ctx).getAllChildren()) {
			if (templates.get(page.getTemplateId()) == null) {
				page.setTemplateName(null);
				modif++;
			}
		}
		return modif + " page(s) corrected.";
	}

	@Override
	public boolean isAdmin() {
		return true;
	}

	@Override
	public boolean isPreview() {
		return false;
	}

}
