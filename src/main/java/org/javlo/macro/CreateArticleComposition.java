package org.javlo.macro;

import java.util.Calendar;
import java.util.Collection;
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
import org.javlo.component.core.ComponentBean;
import org.javlo.component.links.PageMirrorComponent;
import org.javlo.component.meta.ForceRealContent;
import org.javlo.component.title.Heading;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ContentHelper;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.MapHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.AbstractInteractiveMacro;
import org.javlo.message.MessageRepository;
import org.javlo.module.macro.MacroModuleContext;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageAssociationBean;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class CreateArticleComposition extends AbstractInteractiveMacro implements IAction {

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
		return "/jsp/macros/create-article-composition.jsp?canCreate=true";
	}

	@Override
	public String getInfo(ContentContext ctx) {
		return null;
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
		rootPages = MapHelper.sortByValue(rootPages);
		ctx.getRequest().setAttribute("pages", rootPages);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (globalContext.getTags().size() > 0) {
			ctx.getRequest().setAttribute("tags", globalContext.getTags());
		}

		String pageId = ctx.getRequest().getParameter("page");
		if (pageId != null) {
			ContentService content = ContentService.getInstance(ctx.getRequest());
			try {
				MenuElement page = content.getNavigation(ctx).searchChildFromId(pageId);
				if (page != null) {
					ctx.getRequest().setAttribute("sourcePage", page.getPageBean(ctx));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
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

	protected Collection<String> searchPageMirrorReference(MenuElement page) {
		Collection<String> outIds = new LinkedList<String>();
		for (ComponentBean bean : page.getContent()) {
			if (bean.getType().equals(PageMirrorComponent.TYPE)) {
				outIds.add(bean.getValue());
			}
		}
		return outIds;
	}

	public static String performCreate(RequestService rs, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		Properties config = ctx.getCurrentTemplate().getMacroProperties(ctx.getGlobalContext(), NAME);
		if (config == null) {
			config = new Properties();
		}
		String rootPageName = rs.getParameter("root", null);
		String newgroup = rs.getParameter("newgroup", "");
		String date = rs.getParameter("date", null);
		boolean duplicate = rs.getParameter("duplicate", null) != null;
		String pageId = rs.getParameter("page", null);
		String pageName = rs.getParameter("title", null);
		boolean visible = StringHelper.isTrue(rs.getParameter("visible", null), false);
		if (newgroup.trim().length() > 0) {
			MenuElement firstArticleGroup = MacroHelper.searchArticleRoot(ctx).iterator().next();
			if (firstArticleGroup.getParent() == null) {
				return "articles root could not be the root page of the site.";
			}
			MenuElement rootArticle = firstArticleGroup.getParent();
			String newPageName = StringHelper.createFileName(newgroup);
			MenuElement newPage = MacroHelper.addPage(ctx, rootArticle, newPageName, false, false);
			if (newPage == null) {
				return "a page with this name allready exist.";
			}
			newPage.setTemplateId(firstArticleGroup.getTemplateId());
			rootPageName = newPageName;
			MacroHelper.addContent(ctx.getRequestContentLanguage(), newPage, "0", Heading.TYPE, Heading.TEXT + '=' + newgroup, ctx.getCurrentEditUser());
			MenuElement yearPage = MacroHelper.addPage(ctx, newPage, newPageName + '-' + Calendar.getInstance().get(Calendar.YEAR), false, false);
			MacroHelper.addContent(ctx.getRequestContentLanguage(), yearPage, "0", Heading.TYPE, Heading.TEXT + '=' + Calendar.getInstance().get(Calendar.YEAR), ctx.getCurrentEditUser());
			if (pageName == null || pageName.trim().length() == 0) {
				return null;
			}
		}

		MenuElement sourcePage = null;
		if (pageId != null) {
			sourcePage = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx).searchChildFromId(pageId);
		}

		if (pageName == null || pageName.trim().length() == 0) {
			return "page name not defined.";
		}

		if (ContentService.getInstance(ctx.getRequest()).getNavigation(ctx).searchChildFromName(pageName) != null) {
			return "page name already exists.";
		}

		if (date == null || date.trim().length() == 0) {
			return "please choose a date.";
		}

		String message = null;
		String newURL = null;
		if (rootPageName == null) {
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
			MenuElement rootPage = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx).searchChildFromName(rootPageName);
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
				PageAssociationBean assBean = null;
				if (duplicate) {
					if (ctx.getCurrentPage().getRootOfChildrenAssociation() != null) {
						assBean = new PageAssociationBean(ctx, ctx.getCurrentPage().getRootOfChildrenAssociation());
					}
				}
				if (sourcePage != null) {
					assBean = new PageAssociationBean(ctx, sourcePage.getRootOfChildrenAssociation());
				}

				String yearPageName = rootPage.getName() + "-" + cal.get(Calendar.YEAR);
				MenuElement yearPage = MacroHelper.addPageIfNotExist(ctx, rootPage.getName(), yearPageName, true);
				MacroHelper.createMonthStructure(ctx, yearPage);
				String mountPageName = MacroHelper.getMonthPageName(ctx, yearPage.getName(), articleDate);
				MenuElement mountPage = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx).searchChildFromName(mountPageName);

				List<String> selectedRole;
				if (ctx.getGlobalContext().isCollaborativeMode()) {
					selectedRole = new LinkedList<String>();
					for (String role : roles) {
						if (rs.getParameter("role-" + role, null) != null) {
							selectedRole.add(role);
						}
					}
				} else {
					selectedRole = roles;
				}

				if (mountPage != null) {
					MenuElement newPage = MacroHelper.addPageIfNotExist(ctx, mountPage, pageName, true, false);
					if (newPage != null) {
						newPage.addEditorRoles(selectedRole);
						MacroHelper.addContent(ctx.getRequestContentLanguage(), newPage, "0", ForceRealContent.TYPE, "", ctx.getCurrentEditUser());
						newPage.setVisible(visible);
						MenuElement layoutPage = MacroHelper.addPageIfNotExist(ctx, newPage.getName(), newPage.getName() + "-composition", false);
						if (assBean != null) {
							newPage.setTemplateId(assBean.getPage().getTemplateId());
							ContentService content = ContentService.getInstance(ctx.getRequest());
							ContentHelper.copyPage(ctx, assBean.getAssociationPage().getPage(), layoutPage);
							layoutPage.setChildrenAssociation(true);
							for (MenuElement page : assBean.getAssociationPage().getPage().getChildMenuElements()) {
								String newPageName = layoutPage.getName() + "-1";
								int index = 2;
								while (content.getNavigation(ctx).searchChildFromName(newPageName) != null) {
									newPageName = layoutPage.getName() + '-' + index;
									index++;
								}
								MenuElement newChild = MacroHelper.addPageIfNotExist(ctx, layoutPage.getName(), newPageName, false);
								ContentHelper.copyPage(ctx, page, newChild);

								/* copy article */
								MenuElement articlePage = MacroHelper.addPageIfNotExist(ctx, newPage.getName(), newPage.getName() + "-article", false);
								articlePage.setSharedName(articlePage.getName());
								articlePage.setTemplateId(config.getProperty("template.article", null));
								MacroHelper.addContent(ctx.getRequestContentLanguage(), articlePage, "0", Title.TYPE, "Articles", ctx.getCurrentEditUser());
								for (ComponentBean bean : newChild.getContent()) {
									if (bean.getType().equals(PageMirrorComponent.TYPE)) {
										MenuElement article = content.getNavigation(ctx).searchChildFromId(bean.getValue());
										if (article != null) {
											MenuElement articleRoot = assBean.getArticleRoot().getPage();
											newPageName = articleRoot.getName() + "-1";
											index = 2;
											while (content.getNavigation(ctx).searchChildFromName(newPageName) != null) {
												newPageName = articleRoot.getName() + '-' + index;
												index++;
											}
											MenuElement newArticle = MacroHelper.addPageIfNotExist(ctx, articlePage.getName(), newPageName, false);
											ContentHelper.copyPage(ctx, article, newArticle);
											bean.setValue(newArticle.getId());
										}
									}
								}
							}
						} else {
							newPage.setTemplateId(config.getProperty("template.article", null));
							if (config.getProperty("template.composition", null) != null) {
								layoutPage.setTemplateId(config.getProperty("template.composition", null));
							}
							layoutPage.setChildrenAssociation(true);
							MacroHelper.addPageIfNotExist(ctx, layoutPage.getName(), layoutPage.getName() + "-1", false).addEditorRoles(selectedRole);
							;

							MenuElement articlePage = MacroHelper.addPageIfNotExist(ctx, newPage.getName(), newPage.getName() + "-article", false);
							articlePage.setSharedName(articlePage.getName());
							articlePage.setTemplateId(config.getProperty("template.article", null));
							MacroHelper.addContent(ctx.getRequestContentLanguage(), articlePage, "0", Title.TYPE, "Articles", ctx.getCurrentEditUser());
						}

						newURL = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), layoutPage);

					}
				} else {
					message = "mount page not found : " + mountPageName;
				}
			} else {
				message = rootPageName + " not found.";
			}
			MacroModuleContext.getInstance(ctx.getRequest()).setActiveMacro(null);
			if (ctx.isEditPreview()) {
				ctx.setClosePopup(true);
				if (newURL != null) {
					ctx.setParentURL(newURL);
				}
			}
			ctx.getGlobalContext().setStopUndo(true);
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

	@Override
	public boolean isAdd() {
		return true;
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return ctx.getCurrentEditUser() != null;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void init(ContentContext ctx) {
	}
	
	@Override
	public String getModalSize() {
		return "lg";
	}
	
	@Override
	public String getIcon() {
		return "bi bi-file-earmark-richtext";
	}
	
	@Override
	public String getUrl() {
		return null;
	}
	
	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}

}
