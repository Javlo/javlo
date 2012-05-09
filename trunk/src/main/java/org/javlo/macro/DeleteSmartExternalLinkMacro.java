package org.javlo.macro;

import java.util.List;
import java.util.Map;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.links.SmartExternalLink;
import org.javlo.context.ContentContext;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;

public class DeleteSmartExternalLinkMacro extends AbstractMacro {

	public String getName() {
		return "delete-smart-external-link";
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		MenuElement currentPage = ctx.getCurrentPage();

		List<IContentVisualComponent> comps = currentPage.getContentByType(ctx, SmartExternalLink.TYPE);
		int countChange = 0;
		for (IContentVisualComponent comp : comps) {			
			currentPage.removeContent(ctx, comp.getId());
			countChange++;
		}

		String msg = "" + countChange + " SmartExternalLink(s) deleted.";
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));

		return null;
	}
};
