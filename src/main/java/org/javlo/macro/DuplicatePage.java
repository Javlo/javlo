package org.javlo.macro;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.links.MirrorComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.content.Edit;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class DuplicatePage extends AbstractMacro {

	@Override
	public String getName() {
		return "duplicate-page";
	}
	
	private boolean isMirroredContent(ContentContext ctx) throws IOException, Exception {
		Properties prop = getMacroProperties(ctx);
		if (prop == null) {
			return false;
		}
		return StringHelper.isTrue(prop.getProperty("content.mirrored"), false);
	}
	
	private void duplicatePage(ContentContext ctx, MenuElement parent, MenuElement page) throws Exception {
		
		if (parent == null) {
			parent = page.getParent();
		}
		
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		
		String newPageName = NavigationHelper.getNewName(page);
		
		MenuElement newPage = MacroHelper.addPageIfNotExist(ctx, parent, newPageName, false, false);
		newPage.setTemplateId(page.getTemplateId());
		ContentContext noAreaCtx = ctx.getContextWithoutArea();
		Map<String,String> compTranslation = new HashMap<>();
		Map<String, MirrorComponent> outTranslation = new HashMap<>();
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
						parentId = content.createContent(noAreaCtx.getContextWidthOtherRequestLanguage(next.getComponentBean().getLanguage()), newPage, next.getComponentBean(), parentId, false, page, outTranslation);
						compTranslation.put(next.getId(), parentId); // old id > new id (for mirror component translation)
					}
				}
			}
		}
		for (String id : compTranslation.keySet()) {
			if (outTranslation.get(id) != null) {
				outTranslation.get(id).setValue(compTranslation.get(id));
			}
		}
		
		for (MenuElement child : page.getChildMenuElements()) {
			duplicatePage(ctx, newPage, child);
		}
	}
	
	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		if (currentPage.getParent() == null) {
			return "you can't duplicate the root page.";
		}
//		if (ctx.getCurrentPage().getChildMenuElements().size() > 0) {
//			if (ctx.getCurrentPage().isChildrenOfAssociation()) {
//				return "No page selected. Please choose a page to duplicate an try again.";
//			} else {
//				return "you can't duplicate a page width children.";
//			}
//		}
		
		if (!Edit.checkPageSecurity(ctx, currentPage.getParent())) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}
	
		duplicatePage(ctx, null, currentPage);

		return null;
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

}
