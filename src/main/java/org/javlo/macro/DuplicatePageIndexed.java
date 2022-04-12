package org.javlo.macro;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.links.MirrorComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.content.Edit;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class DuplicatePageIndexed extends AbstractMacro implements IInteractiveMacro, IAction {

	private static String INDEX_KEY = "-index-";
	
	private static String TYPE = "duplicate-page-indexed";

	@Override
	public String getName() {
		return TYPE;
	}

	private static boolean isMirroredContent(ContentContext ctx) throws IOException, Exception {
		return false;
	}

	private static String replaceIndex(String text, int index) {
		return text.replace(INDEX_KEY, StringHelper.renderNumber(index, 3));
	}

	private static void duplicatePage(ContentContext ctx, MenuElement parent, MenuElement page, int index) throws Exception {

		if (parent == null) {
			parent = page.getParent();
		}

		ContentService content = ContentService.getInstance(ctx.getGlobalContext());

		String newPageName = NavigationHelper.getNewName(page, replaceIndex(page.getName(), index));

		MenuElement newPage = MacroHelper.addPageIfNotExist(ctx, parent, newPageName, false, false);
		newPage.setTemplateId(page.getTemplateId());
		ContentContext noAreaCtx = ctx.getContextWithoutArea();
		Map<String, String> compTranslation = new HashMap<>();
		Map<String, Collection<MirrorComponent>> outTranslation = new HashMap<>();
		for (String lg : ctx.getGlobalContext().getContentLanguages()) {
			noAreaCtx.setContentLanguage(lg);
			ContentElementList comps = page.getContent(noAreaCtx);
			String parentId = "0";
			while (comps.hasNext(noAreaCtx)) {
				IContentVisualComponent next = comps.next(noAreaCtx);
				if (!next.isRepeat() || next.getPage().equals(parent)) {
					if (isMirroredContent(ctx)) {
						parentId = content.createContentMirrorIfNeeded(noAreaCtx.getContextWidthOtherRequestLanguage(next.getComponentBean().getLanguage()), newPage, next, parentId, false);
					} else {
						ComponentBean bean = next.getComponentBean();
						bean.setValue(replaceIndex(bean.getValue(), index));
						parentId = content.createContent(noAreaCtx.getContextWidthOtherRequestLanguage(next.getComponentBean().getLanguage()), newPage, bean, parentId, false, page, outTranslation);
						compTranslation.put(next.getId(), parentId); // old id > new id (for mirror component translation)
					}
				}
			}
		}
		for (String id : compTranslation.keySet()) {
			if (outTranslation.get(id) != null) {
				for (MirrorComponent comp : outTranslation.get(id)) {
					comp.setValue(compTranslation.get(id));
				}
			}
		}

		for (MenuElement child : page.getChildMenuElements()) {
			duplicatePage(ctx, newPage, child, index);
		}
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/duplicate-page-indexed.jsp";
	}

	@Override
	public boolean isPreview() {
		return true;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public boolean isAdd() {
		return true;
	}

	@Override
	public String prepare(ContentContext ctx) {
		return null;
	}

	@Override
	public String getModalSize() {
		return SMALL_MODAL_SIZE;
	}

	public static String performDuplicate(ContentContext ctx, RequestService rs) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		if (currentPage.getParent() == null) {
			return "you can't duplicate the root page.";
		}
		if (!Edit.checkPageSecurity(ctx, currentPage.getParent())) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}
		int from = Integer.parseInt(rs.getParameter("from"));
		int to = Integer.parseInt(rs.getParameter("to"));
		if (from<1) {
			return "from < 1";
		} else if (from >= to) {
			return "form >= to";
		} else if (to > 999) {
			return "to > 999";
		}
		for (int i = from; i <= to; i++) {
			duplicatePage(ctx, null, currentPage, i);
		}
		return null;
	}

	@Override
	public String getActionGroupName() {
		return TYPE;
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return true;
	}

}
