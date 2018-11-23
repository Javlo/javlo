package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.image.GlobalImage;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;

public class NoDashOnImageMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "no-dash-image";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		ContentContext areaNoArea = ctx.getContextWithArea(null);
		for (MenuElement page : content.getNavigation(ctx).getAllChildrenList()) {
			ContentElementList compList = page.getContent(areaNoArea);
			while (compList.hasNext(areaNoArea)) {
				IContentVisualComponent comp = compList.next(areaNoArea);
				if (comp instanceof GlobalImage) {
					String url  = ((GlobalImage)comp).getLink();
					if (url.trim().equals("#")) {
						((GlobalImage)comp).setLink("");
						((GlobalImage)comp).storeProperties();
						((GlobalImage)comp).setModify();						
						PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}

	@Override
	public boolean isPreview() {
		return false;
	}
};
