package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class deleteComponentInBadArea extends AbstractMacro {

	@Override
	public String getName() {
		return "delete-component-in-bad-area";
	}

	protected int deleteComponentInBadArea(ContentContext ctx, MenuElement page) {
		ComponentBean[] beans = page.getContent();
		Template template = null;
		int countDeleted = 0;
		try {
			template = TemplateFactory.getTemplate(ctx, page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (template != null) {
			for (ComponentBean bean : beans) {
				if (!template.getAreas().contains(bean.getArea())) {
					page.removeContent(ctx, bean.getId());
					countDeleted++;
				}
			}
		}
		return countDeleted;
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService content = ContentService.getInstance(globalContext);
		MenuElement root = content.getNavigation(ctx);
		deleteComponentInBadArea(ctx, root);
		int countDeleted = 0;
		for (MenuElement child : root.getAllChildren()) {
			countDeleted = countDeleted + deleteComponentInBadArea(ctx, child);
		}
		return "components deleted : " + countDeleted;
	}

}
