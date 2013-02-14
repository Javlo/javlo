package org.javlo.macro;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.meta.DateComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.navigation.MenuElement;

public class DownDateMacro extends AbstractMacro {

	private static Logger logger = Logger.getLogger(DownDateMacro.class.getName());

	@Override
	public String getName() {
		return "down-date";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		int i = 0;
		for (MenuElement page : ctx.getCurrentPage().getAllChildren()) {
			List<IContentVisualComponent> dates = page.getContentByType(ctx, DateComponent.TYPE);
			for (IContentVisualComponent date : dates) {
				if (date.getNextComponent() != null) {
					ComponentHelper.moveComponent(ctx, date, date.getNextComponent(), date.getArea());
					i++;
				}
			}
		}
		logger.info(i + " date component moved.");
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
};
