package org.javlo.macro;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.links.PageMirrorComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.ModificationDateComparator;
import org.javlo.navigation.PageBean;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;

public class UndeletePage extends AbstractInteractiveMacro implements IAction {

	private static Logger logger = Logger.getLogger(UndeletePage.class.getName());

	private static final String NAME = "undelete-page";

	@Override
	public String getName() {
		return NAME;
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
		return NAME;
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/undelete-page.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		try {
			ContentService content = ContentService.getInstance(ctx.getRequest());
			MenuElement trash = content.getTrashPage(ctx);
			List<PageBean> pages = new LinkedList<PageBean>();
			for (MenuElement child : trash.getChildMenuElements()) {
				if (child.isEditAccess(ctx)) {
					pages.add(new PageBean(ctx, child));
				}
			}
			Collections.sort(pages, new ModificationDateComparator(false));
			ctx.getRequest().setAttribute("pages", pages);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected Collection<String> searchPageMirrorReference(MenuElement page) {
		Collection<String> outIds = new LinkedList<String>();
		for (ComponentBean bean : page.getContent()) {
			if (bean.getType().equals(PageMirrorComponent.TYPE)) {
				outIds.add(bean.getValue());
			}
		}
		return outIds;
	}

	public static String performAction(RequestService rs, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		Properties config = ctx.getCurrentTemplate().getMacroProperties(ctx.getGlobalContext(), NAME);
		if (config == null) {
			config = new Properties();
		}

		String page = rs.getParameter("restore", null);
		if (page != null) {
			MenuElement pageToRestore = ContentService.getInstance(ctx.getGlobalContext()).getNavigation(ctx).searchChildFromId(page);
			if (pageToRestore != null) {
				if (pageToRestore.isEditAccess(ctx)) {
					MenuElement parent = ContentService.getInstance(ctx.getGlobalContext()).getNavigation(ctx).searchChildFromId(pageToRestore.getSavedParent());
					if (parent == null) {
						return "original parent not found : "+pageToRestore.getSavedParent();
					} else {
						pageToRestore.getParent().removeChild(pageToRestore);
						pageToRestore.setParent(parent);
						parent.addChildMenuElementOnBottom(pageToRestore);
						parent.releaseCache();
						pageToRestore.releaseCache();
						messageRepository.setGlobalMessage(new GenericMessage("page restored : "+pageToRestore.getHumanName(), GenericMessage.INFO));
						if (!pageToRestore.getChildMenuElements().isEmpty() && pageToRestore.getChildMenuElements().iterator().next().isChildrenAssociation()) {
							ctx.setPath(pageToRestore.getChildMenuElements().iterator().next().getPath());
						} else {
							ctx.setPath(pageToRestore.getPath());
						}
						ctx.setParentURL(URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE)));
						ctx.setClosePopup(true);
						PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
					}
				} else {
					return "no access.";
				}
			}
		} else {
			page = rs.getParameter("delete", null);
			if (page != null) {
				MenuElement pageToDelete = ContentService.getInstance(ctx.getGlobalContext()).getNavigation(ctx).searchChildFromId(page);
				if (pageToDelete != null) {
					if (pageToDelete.isEditAccess(ctx)) {
						String path = pageToDelete.getPath();
						NavigationService service = NavigationService.getInstance(ctx.getGlobalContext());
						service.removeNavigation(ctx, pageToDelete);
						MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(i18nAccess.getText("action.remove.deleted", new String[][] { { "path", path } }), GenericMessage.INFO));			
						PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
					} else {
						return "no access.";
					}
				} else {
					return "page not found : "+page;
				}
			}
			}
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
