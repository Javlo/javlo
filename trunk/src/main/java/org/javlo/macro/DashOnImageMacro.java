package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.image.GlobalImage;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class DashOnImageMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "dash-image";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		ContentContext areaNoArea = ctx.getContextWithArea(null);
		for (MenuElement page : content.getNavigation(ctx).getAllChildren()) {
			ContentElementList compList = page.getContent(areaNoArea);
			while (compList.hasNext(areaNoArea)) {
				IContentVisualComponent comp = compList.next(areaNoArea);
				if (comp instanceof GlobalImage) {
					String url  = ((GlobalImage)comp).getLink();
					if (url.trim().length() == 0) {
						((GlobalImage)comp).setLink("#");
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
