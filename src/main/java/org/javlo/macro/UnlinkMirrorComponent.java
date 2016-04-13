package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.links.MirrorComponent;
import org.javlo.context.ContentContext;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.PersistenceService;

public class UnlinkMirrorComponent extends AbstractMacro {

	@Override
	public String getName() {
		return "unlink-mirror-components";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {		
		ContentContext areaNoArea = ctx.getContextWithArea(null);
		int unlinkComp = 0;

		ContentElementList compList = ctx.getCurrentPage().getContent(areaNoArea);
		while (compList.hasNext(areaNoArea)) {
			IContentVisualComponent comp = compList.next(areaNoArea);
			if (comp instanceof MirrorComponent) {
				((MirrorComponent) comp).unlink(ctx);
				unlinkComp++;
			}
		}

		if (unlinkComp > 0) {
			PersistenceService persistenceService = PersistenceService.getInstance(ctx.getGlobalContext());
			persistenceService.setAskStore(true);
		}
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage("unlinked component : " + unlinkComp, GenericMessage.INFO));
		return null;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
};
