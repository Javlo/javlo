package org.javlo.macro;

import java.util.Collection;
import java.util.Map;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.image.GlobalImage;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.service.PersistenceService;

public class NoClickableImageMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "no-clickable-image-macro";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		Collection<IContentVisualComponent> comps = getAllComponent(ctx);
		for (IContentVisualComponent comp : comps) {
			if (comp.getType().equals(GlobalImage.TYPE)) {
				GlobalImage image = (GlobalImage) comp;
				if (image.getLink().trim().length() == 0) {
					image.setLink("#");
					image.setModify();
					image.storeProperties();
				}
			}
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

}
