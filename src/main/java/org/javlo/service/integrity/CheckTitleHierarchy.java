package org.javlo.service.integrity;

import java.util.HashSet;
import java.util.Set;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.ISubTitle;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class CheckTitleHierarchy extends AbstractIntegrityChecker {

	@Override
	public boolean checkPage(ContentContext ctx, MenuElement page) throws Exception {
		Template template = TemplateFactory.getTemplate(ctx, page);
		int error = 0;
		int lastLevel = 0;
		Set<String> titleFound = new HashSet<String>();
		if (template != null) {
			ContentContext areaCtx = new ContentContext(ctx);
			for (String area : template.getAreas()) {
				areaCtx.setArea(area);
				ContentElementList content = page.getContent(areaCtx);
				IContentVisualComponent comp = content.next(areaCtx);
				while (comp != null) {
					String value = null;
					if (comp instanceof ISubTitle) {
						value = comp.getTextTitle(areaCtx);
						int level = ((ISubTitle) comp).getSubTitleLevel(areaCtx);
						if (level > 0) {
							if (lastLevel > 0 && Math.abs(level - lastLevel) > 1) {
								if (getComponentId(ctx) == null) {
									setComponentId(comp.getId());
								}
								error++;
							}
							lastLevel = level;
						}
						
					} else if (comp instanceof Title) {
						value = comp.getValue(areaCtx);
						if (lastLevel > 2) {
							error++;
						}
						lastLevel = 1;
					}
					if (value != null && titleFound.contains(value)) {
						I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
						setErrorMessage(i18nAccess.getText("integrity.error.same_title", "All titles must be different."));
						if (getComponentId(ctx) == null) {
							setComponentId(comp.getId());
						}
						error++;
					} else {
						if (value != null) {
							titleFound.add(value);
						}
					}
					comp = content.next(areaCtx);
				}
			}
		}
		if (error > 0) {
			if (StringHelper.isEmpty(getErrorMessage(ctx))) {
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				setErrorMessage(i18nAccess.getText("integrity.error.title_hierarchy", "Bad title hierachy, max one level between two title."));
			}
			setErrorCount(error);
			return false;
		} else {
			return true;
		}
	}

}
