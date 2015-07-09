package org.javlo.macro;

import java.util.Map;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.text.WysiwygParagraph;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class CreateContentChildren implements IInteractiveMacro, IAction {

	private static final String DEFAULT_PAGE_NAME = "default-child-page";

	private static Logger logger = Logger.getLogger(CreateContentChildren.class.getName());

	@Override
	public String getName() {
		return "create-content-children";
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
		return "create-content-children";
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/create-content-children.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		return null;
	}

	public static String performCreate(RequestService rs, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		int error = 0;
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();

		String newPageName = currentPage.getName() + "-1";
		int index = 2;
		while (content.getNavigation(ctx).searchChildFromName(newPageName) != null) {
			newPageName = currentPage.getName() + '-' + index;
			index++;
		}
		MenuElement newPage = MacroHelper.addPageIfNotExist(ctx, ctx.getCurrentPage(), newPageName, true, true);
		newPage.setChangeNotification(true);
		String title = rs.getParameter("title", null);
		String body = rs.getParameter("body", null);
		if (title == null || body == null) {
			return "bad parameters, title and body needed.";
		} else {
			MenuElement defaultPage = content.getNavigation(ctx).searchChildFromName(DEFAULT_PAGE_NAME);
			if (defaultPage == null) {
				String parentID = "0";
				parentID = MacroHelper.addContent(ctx.getRequestContentLanguage(), newPage, parentID, Title.TYPE, title, ctx.getCurrentEditUser());
				parentID = MacroHelper.addContent(ctx.getRequestContentLanguage(), newPage, parentID, WysiwygParagraph.TYPE, body, ctx.getCurrentEditUser());
			} else {
				ContentElementList comps = defaultPage.getContent(ctx);
				String parentId = "0";
				while (comps.hasNext(ctx)) {
					ComponentBean contentBean = comps.next(ctx).getComponentBean();
					if (contentBean.getType().equals(Title.TYPE)) {
						contentBean.setValue(title);
					} else if (contentBean.getType().equals(WysiwygParagraph.TYPE)) {
						contentBean.setValue(body);
					}
					parentId = content.createContent(ctx, newPage, contentBean, parentId, false);
				}
			}
			String newURL = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), newPage);
			
			messageRepository.clearGlobalMessage();
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("macro.create-content-children.create", "You message is create."), GenericMessage.INFO));
			
			ctx.setParentURL(newURL);
			ctx.setClosePopup(true);
		}

		if (error == 0) {
			return null;
		} else {
			return error + " errors found.";
		}
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
