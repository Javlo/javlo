package org.javlo.macro;

import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

import java.util.Map;

public class TransfertComponentBadAreaToContent extends AbstractMacro {

	@Override
	public String getName() {
		return "move-component-from-bad-area-to-content";
	}

	public boolean isOnCurrentPage() {
		return false;
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
			for (String lg : ctx.getGlobalContext().getContentLanguages()) {
				ComponentBean firstContentSourceComp = null;
				for (ComponentBean bean : beans) {
					if (bean != null && bean.getLanguage().equals(lg) && bean.getArea().equals(ComponentBean.DEFAULT_AREA) && firstContentSourceComp == null) {
						firstContentSourceComp = bean;
					}
				}
				ComponentBean parent = null;
				for (ComponentBean bean : beans) {
					if (bean != null && bean.getLanguage().equals(lg) && !template.getAreas().contains(bean.getArea())) {
						bean.setArea(ComponentBean.DEFAULT_AREA);
						bean.setModify(true);
						page.removeContent(ctx, bean.getId());
						page.addContent(parent != null?parent.getId():"0", bean);
						countMoved++;
						parent = bean;
					}
				}
				if (firstContentSourceComp != null) {
					page.removeContent(ctx, firstContentSourceComp.getId());
					page.addContent(parent != null ? parent.getId() : "0", firstContentSourceComp);
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
