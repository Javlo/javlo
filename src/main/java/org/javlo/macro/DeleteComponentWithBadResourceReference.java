package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.files.AbstractFileComponent;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;

public class DeleteComponentWithBadResourceReference extends AbstractMacro {

	@Override
	public String getName() {
		return "delete-component-bad-resource";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		ContentService contentService = ContentService.getInstance(ctx.getRequest());
		MenuElement root = contentService.getNavigation(ctx);
		for (MenuElement page : root.getAllChildrenList()) {
			ContentContext allAreaContext = ctx.getContextWithArea(null);
			ContentElementList content = page.getContent(allAreaContext);
			while (content.hasNext(allAreaContext)) {
				IContentVisualComponent comp = content.next(allAreaContext);
				if (comp instanceof AbstractFileComponent) {
					AbstractFileComponent fileComp = (AbstractFileComponent) comp;
					if (!fileComp.getFile(ctx).exists()) {
						page.removeContent(ctx, comp.getId());
					}
				}
			}
		}
		PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		return null;
	}

	@Override
	public boolean isPreview() {
		return false;
	}

}
