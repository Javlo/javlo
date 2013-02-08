package org.javlo.macro;

import java.util.List;
import java.util.Map;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.title.SubTitle;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;

public class IncreaseSubtitleLevelMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "increase-subtitle-level";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		MenuElement currentPage = ctx.getCurrentPage();

		List<IContentVisualComponent> comps = currentPage.getContentByType(ctx, SubTitle.TYPE);
		int countChange = 0;
		for (IContentVisualComponent comp : comps) {
			for (int level = 2; level <= 8; level++) {
				if (StringHelper.neverEmpty(comp.getStyle(ctx), "").equals("" + level)) {
					comp.setStyle(ctx, "" + (level - 1));
					countChange++;
				}
			}
		}

		String msg = "change " + countChange + " subtitle(s).";
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
};
