package org.javlo.macro;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.MessageRepository;
import org.javlo.module.macro.MacroModuleContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class CreateArticle implements IInteractiveMacro, IAction {

	@Override
	public String getName() {
		return "create-article";
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
		return "macro-create-article";
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/create-article.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		Map<String, String> rootPages = new HashMap<String, String>();
		try {
			for (MenuElement page : MacroHelper.searchArticleRoot(ctx)) {
				rootPages.put(page.getName(), page.getTitle(ctx));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		ctx.getRequest().setAttribute("pages", rootPages);

		return null;
	}

	public static String performCreate(RequestService rs, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		String pageName = rs.getParameter("root", null);
		String date = rs.getParameter("date", null);
		boolean create = rs.getParameter("create", null) != null;
		String message = null;
		String newURL = null;
		if (pageName == null || date == null) {
			message = "page or date not found.";
		}
		try {
			Date articleDate = StringHelper.parseDate(date);
			Calendar cal = Calendar.getInstance();
			cal.setTime(articleDate);
			MenuElement rootPage = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx).searchChildFromName(pageName);
			if (rootPage != null) {
				String yearPageName = rootPage.getName() + "-" + cal.get(Calendar.YEAR);
				MenuElement yearPage = MacroHelper.addPageIfNotExist(ctx, rootPage.getName(), yearPageName, true);
				MacroHelper.createMonthStructure(ctx, yearPage);
				String mountPageName = MacroHelper.getMonthPageName(ctx, yearPage.getName(), articleDate);
				MenuElement mountPage = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx).searchChildFromName(mountPageName);
				if (mountPage != null) {
					MenuElement newPage = MacroHelper.createArticlePageName(ctx, mountPage);
					if (newPage != null) {
						if (create) {
							MacroHelper.addContentInPage(ctx, newPage, rootPage.getName().toLowerCase());
						}
						newURL = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), newPage);
					}
				} else {
					message = "mount page not found : " + mountPageName;
				}

			} else {
				message = pageName + " not found.";
			}
			MacroModuleContext.getInstance(ctx.getRequest()).setActiveMacro(null);

			if (ctx.isEditPreview()) {
				ctx.setClosePopup(true);
				if (newURL != null) {
					ctx.setParentURL(newURL);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return message;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
