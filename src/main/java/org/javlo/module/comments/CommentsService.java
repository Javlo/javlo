package org.javlo.module.comments;

import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.web2.ReactionComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class CommentsService {

	private static final String KEY = CommentsService.class.getName();

	public static CommentsService getCommentsService(GlobalContext globalContext) {
		CommentsService outService = (CommentsService) globalContext.getAttribute(KEY);
		if (outService == null) {
			outService = new CommentsService();
			globalContext.setAttribute(KEY, outService);
		}
		return outService;
	}

	public List<ReactionComponent> getComments(ContentContext ctx) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		List reactions = new LinkedList();
		MenuElement page = content.getNavigation(ctx);
		List<IContentVisualComponent> comps = page.getContentByType(ctx, ReactionComponent.TYPE);
		reactions.addAll(comps);
		for (MenuElement child : page.getAllChildren()) {
			reactions.addAll(child.getContentByType(ctx, ReactionComponent.TYPE));
		}
		return reactions;
	}
}
