package org.javlo.macro;

import java.util.Collection;
import java.util.Map;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.service.PersistenceService;

public class MacroRendererCorrection extends AbstractMacro {

	@Override
	public String getName() {
		return "set-default-renderer";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		Collection<IContentVisualComponent> comps = getAllComponent(ctx);
		for (IContentVisualComponent comp : comps) {
			if (comp instanceof AbstractVisualComponent) {
				AbstractVisualComponent c = (AbstractVisualComponent)comp;
				String renderer = comp.getCurrentRenderer(ctx);
				if (renderer != null && c.getRenderes(ctx) != null && c.getRenderes(ctx).size() > 0) {
					if (!c.getRenderer(ctx).contains(renderer)) {
						c.setRenderer(ctx, c.getDefaultRenderer(ctx));
					}
				}
			}
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.setAskStore(true);

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

}
