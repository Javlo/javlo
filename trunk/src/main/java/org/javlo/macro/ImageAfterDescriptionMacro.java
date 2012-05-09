package org.javlo.macro;

import java.util.Date;
import java.util.Map;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.text.Description;
import org.javlo.context.ContentContext;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

import com.sun.syndication.feed.rss.Image;

public class ImageAfterDescriptionMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "image-after-description";
	}

	protected boolean imageUnderDescritpion(ContentContext ctx, MenuElement currentPage) throws Exception {
		ContentElementList comps = currentPage.getAllContent(ctx);
		IContentVisualComponent imageComp = null;
		IContentVisualComponent descriptionComp = null;
		while (comps.hasNext(ctx)) {
			IContentVisualComponent comp = comps.next(ctx);
			if (imageComp == null && comp instanceof Image) {
				imageComp = comp;
			} else if (descriptionComp == null && comp instanceof Description) {
				descriptionComp = comp;
			}
		}

		boolean imageMoved = false;
		String msg = "image not moved.";
		if (imageComp != null && descriptionComp != null) {
			Date modifDate = currentPage.getModificationDate();
			currentPage.removeContent(ctx, imageComp.getId());
			currentPage.addContent(descriptionComp.getId(), imageComp.getBean(ctx));
			currentPage.setModificationDate(modifDate);
			msg = "image moved.";
			imageMoved = true;

		} else if (imageComp == null) {
			msg = "image not found.";
		} else if (descriptionComp == null) {
			msg = "description not found.";
		}

		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));

		return imageMoved;
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		ContentService.createContent(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();
		imageUnderDescritpion(ctx, currentPage);

		return null;
	}
};
