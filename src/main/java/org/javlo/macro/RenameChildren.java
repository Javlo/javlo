package org.javlo.macro;

import java.util.Map;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;

public class RenameChildren implements IInteractiveMacro, IAction {
	
	private static Logger logger = Logger.getLogger(RenameChildren.class.getName());

	@Override
	public String getName() {
		return "rename-chilren";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public String getActionGroupName() {
		return "rename-children";
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/rename-children.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {		
		return null;
	}

	public static String performRename(RequestService rs, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String text = rs.getParameter("text", null);
		String newText = rs.getParameter("new", null);
		MenuElement root = ctx.getCurrentPage().getRoot();
		int countBadRename = 0;
		int countOkRename = 0;
		for (MenuElement child : ctx.getCurrentPage().getAllChildren()) {
			if (child.getName().contains(text)) {
				String newName = child.getName().replace(text, newText);
				if (root.searchChildFromName(newName) == null) {
					child.setName(newName);
					countOkRename++;
				} else {
					countBadRename++;
				}
			}
		}
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage("rename page (done:"+countOkRename+") (allready exist:"+countBadRename+")", GenericMessage.INFO));
		ctx.setClosePopup(true);
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
