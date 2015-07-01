package org.javlo.service.integrity;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.image.GlobalImage;
import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class CheckImageLabel extends AbstractIntegrityChecker {

	@Override
	public boolean checkPage(ContentContext ctx, MenuElement page) throws Exception {
		Template template = TemplateFactory.getTemplate(ctx, page);
		int error = 0;
		if (template != null) {
			ContentContext areaCtx = new ContentContext(ctx);
			for (String area : template.getAreas()) {
				areaCtx.setArea(area);
				ContentElementList content = page.getContent(areaCtx);
				IContentVisualComponent comp = content.next(areaCtx);
				while (comp != null) {
					if (comp instanceof GlobalImage) {
						if (((GlobalImage) comp).getLabel().trim().length() == 0) {
							error++;
						}
					}
					comp = content.next(areaCtx);
				}
			}
		}
		if (error > 0) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			setMessage(i18nAccess.getText("integrity.error.image_label", "Some images have no label."));
			setErrorCount(error);
			return false;
		} else {
			return true;
		}
	}

	@Override
	public int getLevel(ContentContext ctx) {
		return DANGER_LEVEL;
	}

}
