package org.javlo.macro;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
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

public class CreateArticleComposition implements IInteractiveMacro, IAction {
	
	private static Logger logger = Logger.getLogger(CreateArticleComposition.class.getName());
	
	private static final String NAME = "create-article-composition";

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
		return "macro-create-article-composition";
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/create-article-composition.jsp";
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
		
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (globalContext.getTags().size() > 0) {
			ctx.getRequest().setAttribute("tags", globalContext.getTags());
		}
		
		List<String> roles = new LinkedList<String>();
		Set<String> roleSet = new HashSet<String>();
		for (String role : ctx.getGlobalContext().getAdminUserRoles()) {
			roleSet.clear();
			roleSet.add(role);
			if (ctx.getCurrentEditUser().validForRoles(roleSet)) {
				roles.add(role);
			}
		}			
		Collections.sort(roles);
		ctx.getRequest().setAttribute("adminRoles", roles);

		return null;
	}

	public static String performCreate(RequestService rs, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException, Exception {
		
		Properties config = ctx.getCurrentTemplate().getMacroProperties(ctx.getGlobalContext(), NAME);
		if (config == null) {
			config = new Properties();
		}
		
		String pageName = rs.getParameter("root", null);
		String date = rs.getParameter("date", null);
		
		String message = null;
		String newURL = null;
		if (pageName == null) {
			return "page or date not found.";
		}
		try {
			Date articleDate;
			if (date != null && date.trim().length() > 0) {
				articleDate = StringHelper.parseDate(date);
			} else {
				articleDate = new Date();
			}
			Calendar cal = Calendar.getInstance();
			cal.setTime(articleDate);
			MenuElement rootPage = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx).searchChildFromName(pageName);
			if (rootPage != null) {				
				List<String> roles = new LinkedList<String>();
				Set<String> roleSet = new HashSet<String>();
				for (String role : ctx.getGlobalContext().getAdminUserRoles()) {
					roleSet.clear();
					roleSet.add(role);
					if (ctx.getCurrentEditUser().validForRoles(roleSet)) {
						roles.add(role);
					}
				}
				String yearPageName = rootPage.getName() + "-" + cal.get(Calendar.YEAR);
				MenuElement yearPage = MacroHelper.addPageIfNotExist(ctx, rootPage.getName(), yearPageName, true);
				MacroHelper.createMonthStructure(ctx, yearPage);
				String mountPageName = MacroHelper.getMonthPageName(ctx, yearPage.getName(), articleDate);
				MenuElement mountPage = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx).searchChildFromName(mountPageName);
				if (mountPage != null) {
					MenuElement newPage = MacroHelper.createArticlePageName(ctx, mountPage);
					if (newPage != null) {				
						MenuElement layoutPage = MacroHelper.addPageIfNotExist(ctx, newPage.getName(), newPage.getName()+"-composition", false);						
						newPage.setTemplateName(config.getProperty("template.article","mailing_one_area"));
						layoutPage.setTemplateName(config.getProperty("template.composition", "mailing"));
						layoutPage.setChildrenAssociation(true);
						
						MenuElement page1 = MacroHelper.addPageIfNotExist(ctx, layoutPage.getName(), layoutPage.getName()+"-1", false);
						String title = rs.getParameter("title", "New letter title : "+StringHelper.renderTime(new Date()));
						MacroHelper.addContent(ctx.getRequestContentLanguage(), page1, "0", Title.TYPE, title, ctx.getCurrentEditUser());						
						
						MenuElement articlePage = MacroHelper.addPageIfNotExist(ctx, newPage.getName(), newPage.getName()+"-article", false);
						articlePage.setSharedName(articlePage.getName());
						articlePage.setTemplateName(config.getProperty("template.article","mailing_one_area"));
						MacroHelper.addContent(ctx.getRequestContentLanguage(), articlePage, "0", Title.TYPE, "Articles", ctx.getCurrentEditUser());
						
						newURL = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), layoutPage);
						
						List<String> selectedRole = new LinkedList<String>();
						for (String role :roles) {
							if (rs.getParameter("role-"+role, null) != null) {
								newPage.addEditorRoles(role);
								selectedRole.add(role);
							}
						}
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
