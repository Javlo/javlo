package org.javlo.module.comments;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.web2.ReactionComponent;
import org.javlo.component.web2.ReactionComponent.Reaction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement.PageBean;
import org.javlo.service.RequestService;

public class CommentsAction extends AbstractModuleAction {

	public class CommentContainerComparator implements Comparator<CommentsContainer> {

		@Override
		public int compare(CommentsContainer o1, CommentsContainer o2) {
			if (o1.getCountUnvalid() == o2.getCountUnvalid()) {
				return o1.getPage().getPath().compareTo(o2.getPage().getPath());
			} else {
				return o2.getCountUnvalid() - o1.getCountUnvalid();
			}

		}

	}

	public class CommentsContainer {

		private final ContentContext ctx;
		private final ReactionComponent reactions;

		public CommentsContainer(ContentContext ctx, ReactionComponent reactions) {
			this.ctx = ctx;
			this.reactions = reactions;
		}

		public PageBean getPage() {
			try {
				return reactions.getPage().getPageBean(ctx);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public Collection<Reaction> getComments() {
			return reactions.getReactions(ctx);
		}

		public Collection<Reaction> getDeletedComments() {
			return reactions.getDeletedReactions(ctx);
		}

		public int getCountUnvalid() {
			int i = 0;
			for (Reaction reaction : reactions.getReactions(ctx)) {
				if (!reaction.isValid()) {
					i++;
				}
			}
			return i;
		}

		public boolean isNeedValidation() {
			Collection<Reaction> reactions = getComments();
			for (Reaction reaction : reactions) {
				if (!reaction.isValid()) {
					return true;
				}
			}
			return false;
		}

		public String getId() {
			return reactions.getId();
		}
	}

	@Override
	public String getActionGroupName() {
		return "comments";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		CommentsService commentsService = CommentsService.getCommentsService(globalContext);
		List<CommentsContainer> allReactions = new LinkedList<CommentsContainer>();
		for (ReactionComponent reaction : commentsService.getComments(ctx)) {
			allReactions.add(new CommentsContainer(ctx, reaction));
		}

		Collections.sort(allReactions, new CommentContainerComparator());
		ctx.getRequest().setAttribute("comments", allReactions);

		return msg;
	}

	public static String performAccept(RequestService rs, GlobalContext globalContext, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String compId = rs.getParameter("comp_id", null);
		String commentId = rs.getParameter("comment_id", null);
		if (compId == null || commentId == null) {
			return "bad parameters structure : need 'comp_id' and 'comment_id'";
		}
		CommentsService commentsService = CommentsService.getCommentsService(globalContext);
		for (ReactionComponent reaction : commentsService.getComments(ctx)) {
			if (reaction.getId().equals(compId)) {
				reaction.validReaction(ctx, commentId);
			}
		}
		return null;
	}

	public static String performRefuse(RequestService rs, GlobalContext globalContext, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String compId = rs.getParameter("comp_id", null);
		String commentId = rs.getParameter("comment_id", null);
		if (compId == null || commentId == null) {
			return "bad parameters structure : need 'comp_id' and 'comment_id'";
		}
		CommentsService commentsService = CommentsService.getCommentsService(globalContext);
		for (ReactionComponent reaction : commentsService.getComments(ctx)) {
			if (reaction.getId().equals(compId)) {
				reaction.deleteReaction(ctx, commentId);
			}
		}
		return null;
	}
}
