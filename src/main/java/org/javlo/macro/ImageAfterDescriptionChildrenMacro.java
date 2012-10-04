package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class ImageAfterDescriptionChildrenMacro extends ImageAfterDescriptionMacro {

	@Override
	public String getName() {
		return "image-after-description-children";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		ContentService.getInstance(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();
		imageUnderDescritpion(ctx, currentPage);

		int imageMoved = 0;
		MenuElement[] children = currentPage.getAllChilds();
		for (MenuElement child : children) {
			if (imageUnderDescritpion(ctx, child)) {
				imageMoved++;
			}
		}

		MessageRepository.getInstance(ctx).clearGlobalMessage();
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage("images found : " + imageMoved, GenericMessage.INFO));

		return null;
	}
};
