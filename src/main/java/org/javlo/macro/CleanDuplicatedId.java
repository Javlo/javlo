package org.javlo.macro;

import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;

public class CleanDuplicatedId extends AbstractMacro {

	protected static Logger logger = Logger.getLogger(CleanDuplicatedId.class.getName());

	@Override
	public String getName() {
		return "clear-component-id";
	}

	private int clearComponentIntegrity(HashSet<String> componentsId, ComponentBean[] comps) {
		int errorCount = 0;

		for (ComponentBean comp : comps) {
			if (componentsId.contains(comp.getId())) {
				comp.setId(StringHelper.getRandomId());
				errorCount++;
			} else {
				componentsId.add(comp.getId());
			}
		}
		return errorCount;
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		HashSet<String> componentsId = new HashSet<String>();
		HashSet<String> pageId = new HashSet<String>();
		ContentService content = ContentService.getInstance(ctx.getRequest());
		try {
			int compError = 0;
			int pageError = 0;
			for (MenuElement page : content.getNavigation(ctx).getAllChildrenList()) {
				compError += clearComponentIntegrity(componentsId, page.getContent());
				if (pageId.contains(page.getId())) {
					pageError++;
					page.setId(StringHelper.getRandomId());
				}
			}
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage("bad component id found : " + compError + " - bad page id found : " + pageError, GenericMessage.INFO));
			if (compError > 0 || pageError > 0) {
				PersistenceService.getInstance(ctx.getGlobalContext()).store(ctx);
				content.releaseAll(ctx, ctx.getGlobalContext());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return null;
	}

	@Override
	public boolean isPreview() {
		return false;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}
};
