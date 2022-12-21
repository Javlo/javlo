package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class TransfertComponentBadAreaToContent extends AbstractMacro {

	@Override
	public String getName() {
		return "move-component-from-bad-area-to-content";
	}

	protected int moveComponentInBadArea(ContentContext ctx, MenuElement page) {
		ComponentBean[] beans = page.getContent();
		Template template = null;
		int countMoved = 0;
		try {
			template = TemplateFactory.getTemplate(ctx, page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (template != null) {
			for (ComponentBean bean : beans) {
				if (!template.getAreas().contains(bean.getArea())) {
					bean.setArea(ComponentBean.DEFAULT_AREA);
					bean.setModify(true);
					countMoved++;
				}
			}
		}

		return countMoved;
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService content = ContentService.getInstance(globalContext);
		MenuElement root = content.getNavigation(ctx);
		int countDeleted = moveComponentInBadArea(ctx, root);
		;
		for (MenuElement child : root.getAllChildrenList()) {
			countDeleted = countDeleted + moveComponentInBadArea(ctx, child);
		}
		if (countDeleted > 0) {
			PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		}
		return "components moved : " + countDeleted;
	}

	@Override
	public boolean isPreview() {
		return false;
	}

}
