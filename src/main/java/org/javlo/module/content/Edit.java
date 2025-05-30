package org.javlo.module.content;

import org.apache.commons.lang3.StringUtils;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.column.TableBreak;
import org.javlo.component.container.IContainer;
import org.javlo.component.core.*;
import org.javlo.component.links.MirrorComponent;
import org.javlo.component.links.PageMirrorComponent;
import org.javlo.component.title.Heading;
import org.javlo.component.title.Title;
import org.javlo.config.StaticConfig;
import org.javlo.context.*;
import org.javlo.data.InfoBean;
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.helper.*;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.admin.AdminAction;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.IMainModuleName;
import org.javlo.module.core.Module;
import org.javlo.module.core.Module.Box;
import org.javlo.module.core.ModulesContext;
import org.javlo.module.file.FileModuleContext;
import org.javlo.module.mailing.MailingModuleContext;
import org.javlo.module.ticket.Ticket;
import org.javlo.module.ticket.TicketBean;
import org.javlo.module.ticket.TicketService;
import org.javlo.navigation.MenuElement;
import org.javlo.search.SearchEngineFactory;
import org.javlo.search.SearchResult;
import org.javlo.search.SearchResult.SearchElement;
import org.javlo.service.*;
import org.javlo.service.integrity.IntegrityFactory;
import org.javlo.service.resource.ResourceStatus;
import org.javlo.service.shared.ISharedContentProvider;
import org.javlo.service.shared.JavloSharedContentProvider;
import org.javlo.service.shared.SharedContent;
import org.javlo.service.shared.SharedContentService;
import org.javlo.service.syncro.SynchroHelper;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.*;
import org.javlo.utils.TimeTracker;
import org.javlo.ztatic.FileCache;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class Edit extends AbstractModuleAction {

	public static String CONTENT_RENDERER = "/jsp/view/content_view.jsp";

	private static Logger logger = Logger.getLogger(Edit.class.getName());

	private static void prepareUpdateInsertLine(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editContext = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		ClipBoard clipBoard = ClipBoard.getInstance(ctx.getRequest());

		ContentContext editCtx = ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE);

		IContentVisualComponent currentTypeComponent = ComponentFactory.getComponentWithType(editCtx, editContext.getActiveType());

		String typeName = StringHelper.getFirstNotNull(currentTypeComponent.getComponentLabel(editCtx, globalContext.getEditLanguage(ctx.getRequest().getSession())), i18nAccess.getText("content." + currentTypeComponent.getType()));
		String insertHere = i18nAccess.getText("content.insert-here", new String[][] { { "type", typeName } });

		String pastePageHere = null;
		if (editContext.getContextForCopy(editCtx) != null) {
			pastePageHere = i18nAccess.getText("content.paste-here", new String[][] { { "page", editContext.getContextForCopy(editCtx).getCurrentPage().getName() } });
		}

		String pasteHere = null;
		if (clipBoard.getCopiedComponent(editCtx) != null) {
			pasteHere = i18nAccess.getText("content.paste-comp", new String[][] { { "type", clipBoard.getCopiedComponent(editCtx).getType() } });
		}

		String previewParam = "";
		if (ctx.isEditPreview()) {
			previewParam = ContentContext.PREVIEW_EDIT_PARAM + "=true&";
		}

		String insertXHTML = "<a class=\"btn btn-default ajax btn-xs\" href=\"" + URLHelper.createURL(editCtx) + "?" + previewParam + "webaction=insert&previous=0&type=" + currentTypeComponent.getType() + "\">" + insertHere + "</a>";
		if (pastePageHere != null) {
			insertXHTML = insertXHTML + "<a class=\"btn btn-default btn-xs\" href=\"" + URLHelper.createURL(editCtx) + "?" + previewParam + "webaction=pastePage&previous=0\">" + pastePageHere + "</a>";
		}
		if (pasteHere != null) {
			insertXHTML = insertXHTML + "<a class=\"btn btn-default btn-xs ajax\" href=\"" + URLHelper.createURL(editCtx) + "?" + previewParam + "webaction=pasteComp&previous=0\">" + pasteHere + "</a>";
		}
		ctx.addAjaxInsideZone("insert-line-0", insertXHTML);

		ContentContext areaCtx = editCtx.getContextWithArea(null);

		IContentComponentsList elems = ctx.getCurrentPage().getContent(areaCtx);
		while (elems.hasNext(areaCtx)) {
			IContentVisualComponent comp = elems.next(areaCtx);
			insertXHTML = "<a class=\"btn btn-default btn-xs ajax\" href=\"" + URLHelper.createURL(editCtx) + "?" + previewParam + "webaction=insert&previous=" + comp.getId() + "&type=" + currentTypeComponent.getType() + "\">" + insertHere + "</a>";
			if (pastePageHere != null) {
				insertXHTML = insertXHTML + "<a class=\"btn btn-default btn-xs\" href=\"" + URLHelper.createURL(editCtx) + "?" + previewParam + "webaction=pastePage&previous=" + comp.getId() + "\">" + pastePageHere + "</a>";
			}
			if (pasteHere != null) {
				insertXHTML = insertXHTML + "<a class=\"btn btn-default btn-xs ajax\" href=\"" + URLHelper.createURL(editCtx) + "?" + previewParam + "webaction=pasteComp&previous=" + comp.getId() + "\">" + pasteHere + "</a>";
			}
			ctx.addAjaxInsideZone("insert-line-" + comp.getId(), insertXHTML);
		}
	}

	/**
	 * update component
	 * 
	 * @param ctx
	 * @param currentModule
	 * @param newId
	 *            the id of the component
	 * @param previousId
	 *            the id, null for update and previous component for insert.
	 * @throws Exception
	 */
	private static void updateComponent(ContentContext ctx, Module currentModule, String newId, String previousId) throws Exception {
		ComponentContext compCtx = ComponentContext.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService content = ContentService.getInstance(globalContext);
		compCtx.addNewComponent(content.getComponent(ctx, newId)); // prepare ajax rendering
		String componentRenderer = URLHelper.mergePath(currentModule.getPath() + "/jsp/content.jsp");
		String newComponentXHTML = ServletHelper.executeJSP(ctx, componentRenderer);
		/*** DEBUG ***/
		// ResourceHelper.writeStringToFile(new File("c:/trans/comp.html"),
		// newComponentXHTML);
		compCtx.clearComponents();
		if (previousId != null) {
			ctx.addAjaxZone("comp-child-" + previousId, newComponentXHTML);
		} else {
			ctx.addAjaxZone("comp-" + newId, newComponentXHTML);
		}
	}

	/**
	 * update previous command zone.
	 * 
	 * @param ctx
	 * @param data.currentModule
	 * @param newId
	 *            the id of the component
	 * @param previousId
	 *            the id, null for update and previous component for insert.
	 * @throws Exception
	 */
	public static void updatePreviewCommands(ContentContext ctx, String tab) throws Exception {
		ctx.getRequest().setAttribute("editPreview", ctx.isEditPreview());
		ctx.getRequest().setAttribute("components", ComponentFactory.getComponentForDisplay(ctx, false));
		SharedContentService.prepare(ctx);
		IntegrityFactory.getInstance(ctx);
		String updateURL = ctx.getGlobalContext().getStaticConfig().getPreviewCommandFilePath();
		if (tab != null) {
			updateURL = URLHelper.addParam(updateURL, "_preview_tab", tab);
		}
		String previewCommandsXHTML = ServletHelper.executeJSP(ctx, updateURL);
		ctx.addAjaxZone("preview_command", previewCommandsXHTML);
	}

	// /**
	// * update component
	// *
	// * @param ctx
	// * @param currentModule
	// * @param newId
	// * the id of the component
	// * @param previousId
	// * the id, null for update and previous component for insert.
	// * @throws Exception
	// */
	// private static void updatePreviewComponent(ContentContext ctx, Module
	// currentModule, String newId, String previousId) throws Exception {
	// ComponentContext compCtx = ComponentContext.getInstance(ctx.getRequest());
	// GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
	// ContentService content = ContentService.getInstance(globalContext);
	// IContentVisualComponent comp = content.getComponent(ctx, newId);
	// compCtx.addNewComponent(comp); // prepare ajax rendering
	// ctx.getRequest().setAttribute("specific-comp", comp);
	// String componentRenderer = "/jsp/view/content_view.jsp";
	// int mode = ctx.getRenderMode();
	// ctx.setRenderMode(ContentContext.PREVIEW_MODE);
	// String newComponentXHTML = ServletHelper.executeJSP(ctx, componentRenderer);
	// ctx.setRenderMode(mode);
	// compCtx.clearComponents();
	// ctx.addAjaxZone("cp_" + newId, newComponentXHTML);
	// }

	private static boolean nameExist(String name, ContentContext ctx, ContentService content) throws Exception {
		MenuElement page = content.getNavigation(ctx);
		return (page.searchChildFromName(name) != null);
	}

	@Override
	public AbstractModuleContext getModuleContext(HttpSession session, Module module) throws Exception {
		return FileModuleContext.getInstance(session, GlobalContext.getSessionInstance(session), module, ContentModuleContext.class);
	}

	@Override
	public String getActionGroupName() {
		return "edit";
	}

	/*******************/
	/** methods utils **/
	/*******************/

	/**
	 * verify the validity of a page name.
	 * 
	 * @param name
	 * @param i18nAccess
	 * @return
	 */
	public static String validNodeName(String name, I18nAccess i18nAccess) {
		String res = null;
		if (name.length() == 0) {
			res = i18nAccess.getText("action.validation.name-not-empty");
		} else if (!name.matches("([a-z]|[A-Z]|[0-9]|_|-)*")) {
			res = i18nAccess.getText("action.validation.name-syntax");
		}
		return res;
	}

	/**
	 * check if a pageName allready exist in the website
	 * 
	 * @param name
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	private static boolean nameExist(ContentContext ctx, String name) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService content = ContentService.getInstance(globalContext);
		MenuElement page = content.getNavigation(ctx);
		return (page.searchChildFromName(name) != null);
	}

	/**
	 * check is user have all right for modify the current page.
	 * 
	 * @param ctx
	 *            the contentcontext
	 * @return true if user have all right for modify the current page
	 * @throws Exception
	 */
	public static boolean checkPageSecurity(ContentContext ctx) throws Exception {
		return checkPageSecurity(ctx, ctx.getCurrentPage());
	}

	/**
	 * 
	 * check is user have all right for modify a specific page.
	 * 
	 * @param ctx
	 * @param page
	 * @return true if current user can modfify the page.
	 * @throws Exception
	 */
	public static boolean checkPageSecurity(ContentContext ctx, MenuElement page) throws Exception {
		if (page == null) {
			return false;
		} else {
			AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
			User user = adminUserFactory.getCurrentUser(globalContext, ctx.getRequest().getSession());
			if (user == null) {
				return false;
			}
			// if (user.getRoles().contains(AdminUserSecurity.CONTRIBUTOR_ROLE)) {
			// return page.getCreator().equals(user.getLogin());
			// }
			ContentService.getInstance(globalContext);
			if (page.getEditorRoles().size() > 0) {
				if (!adminUserSecurity.haveRight(adminUserFactory.getCurrentUser(globalContext, ctx.getRequest().getSession()), AdminUserSecurity.FULL_CONTROL_ROLE)) {
					if (!adminUserFactory.getCurrentUser(globalContext, ctx.getRequest().getSession()).validForRoles(page.getEditorRoles())) {
						MessageRepository messageRepository = MessageRepository.getInstance(ctx);
						I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
						messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.security.noright-onpage"), GenericMessage.ERROR), false);
						return false;
					}
				}
			}
			return true;
		}
	}

	/**
	 * auto publish the content is necessary
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private static void autoPublish(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		if (globalContext.isEasy()) {
			// performPublish(request, response);
		}
	}

	/**
	 * mark page as modified.
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	private static void modifPage(ContentContext ctx, MenuElement currentPage) throws Exception {
		if (currentPage==null) {
			return;
		}
		currentPage.setModificationDate(new Date());
		ContentService.getInstance(ctx.getGlobalContext());
		EditContext editCtx = EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession());
		currentPage.setLatestEditor(editCtx.getUserPrincipal().getName());
		currentPage.setValid(false);
		currentPage.setNeedValidation(false);
		currentPage.releaseCache();
	}

	private static void loadComponentList(ContentContext ctx) throws Exception {
		Collection<ComponentWrapper> comps = ComponentFactory.getComponentForDisplay(ctx, false);
		/*
		 * for (IContentComponentsList iContentComponentsList : comps) { System.out
		 * .println( "***** Edit.loadComponentList : iContentComponentsList = "
		 * +iContentComponentsList); //TODO: remove debug trace }
		 */
		ctx.getRequest().setAttribute("components", comps);
	}

	/**
	 * check if the currentPage is blocked.
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	private static boolean canModifyCurrentPage(ContentContext ctx) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());

		if (currentPage != null && currentPage.isBlocked()) {
			if (!currentPage.getBlocker().equals(adminUserFactory.getCurrentUser(globalContext, ctx.getRequest().getSession()).getName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * check if a specific page is blocked
	 * 
	 * @param ctx
	 * @param page
	 * @return
	 * @throws Exception
	 */
	private static boolean canModifyCurrentPage(ContentContext ctx, MenuElement page) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		if (page.isBlocked()) {
			if (!page.getBlocker().equals(adminUserFactory.getCurrentUser(globalContext, ctx.getRequest().getSession()).getName())) {
				return false;
			}
		}
		return true;
	}

	/***************/

	/** WEBACTION **/
	/***************/

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);

		HttpServletRequest request = ctx.getRequest();

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		Module currentModule = modulesContext.getCurrentModule();

		if (modulesContext.searchModule(IMainModuleName.TICKET) != null) {
			ctx.getRequest().setAttribute("sharedContent", "true");
		}

		if (globalContext.getStaticConfig().getIPMasks().size() > 0) {
			ctx.getRequest().setAttribute("ipsecurity", "true");
		}

		ctx.getRequest().setAttribute("pageHistoryUrl", URLHelper.createStaticURL(ctx, "/rest/pagehistory/"+ctx.getCurrentPage().getId()));

		if (ResourceStatus.isResource(ctx.getRequest().getSession())) {
			ResourceStatus resourceStatus = ResourceStatus.getInstance(ctx.getRequest().getSession());
			String previewSourceCode = "<a class=\"action-button\" href=\"" + URLHelper.createResourceURL(ctx, resourceStatus.getSource().getUri()) + "\">Download</a>";
			String previewTargetCode = "<a class=\"action-button\" href=\"" + URLHelper.createResourceURL(ctx, resourceStatus.getTarget().getUri()) + "\">Download</a>";
			if (StringHelper.isImage(resourceStatus.getSource().getFile().getName())) {
				previewSourceCode = "<a href=\"" + URLHelper.createResourceURL(ctx, resourceStatus.getSource().getUri()) + "\">";
				String url = URLHelper.createTransformURL(ctx, resourceStatus.getSource().getUri(), "preview");
				url = URLHelper.addParam(url, "hash", "" + resourceStatus.getSource().getFile().length());
				previewSourceCode = previewSourceCode + "<figure><img src=\"" + url + "\" alt=\"source\" /><figcaption>" + StringHelper.renderSize(resourceStatus.getSource().getFile().length()) + "</figcaption></figure></a>";
				previewTargetCode = "<a href=\"" + URLHelper.createResourceURL(ctx, resourceStatus.getTarget().getUri()) + "\">";
				url = URLHelper.createTransformURL(ctx, resourceStatus.getTarget().getUri(), "preview");
				url = URLHelper.addParam(url, "hash", "" + resourceStatus.getTarget().getFile().length());
				previewTargetCode = previewTargetCode + "<figure><img src=\"" + url + "\"  alt=\"source\" /><figcaption>" + StringHelper.renderSize(resourceStatus.getTarget().getFile().length()) + "</figcaption></figure></a>";
			}
			ctx.getRequest().setAttribute("previewSourceCode", previewSourceCode);
			ctx.getRequest().setAttribute("previewTargetCode", previewTargetCode);
			ctx.getRequest().setAttribute("sourceDate", StringHelper.renderTime(new Date(resourceStatus.getSource().getFile().lastModified())));
			ctx.getRequest().setAttribute("targetDate", StringHelper.renderTime(new Date(resourceStatus.getTarget().getFile().lastModified())));
			currentModule.setRenderer("/jsp/confirm_replace.jsp");
			currentModule.setToolsRenderer(null);
			currentModule.setSidebar(false);
		} else {
			/** set the principal renderer **/
			ContentModuleContext modCtx = (ContentModuleContext) LangHelper.smartInstance(request, ctx.getResponse(), ContentModuleContext.class);
			if (request.getParameter("query") == null) {
				currentModule.setBreadcrumb(true);
				currentModule.setSidebar(true);
				UserInterfaceContext userIterfaceContext = UserInterfaceContext.getInstance(ctx.getRequest().getSession(), globalContext);

				if (!userIterfaceContext.isComponentsList()) {
					currentModule.clearAllBoxes();
				}

				String publish = "";
				if (globalContext.isPreviewMode()) {
					publish = "&button_publish=true";
				}

				Map<String, String> params = new HashMap<String, String>();
				params.put("webaction", "edit.previewedit");
				params.put("preview", "false");
				request.setAttribute("previewURL", URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), params));

				switch (modCtx.getMode()) {
				case ContentModuleContext.PREVIEW_MODE:
					currentModule.setToolsRenderer("/jsp/actions.jsp?button_edit=true&button_page=true" + publish);
					currentModule.setRenderer("/jsp/preview.jsp");
					currentModule.setBreadcrumbTitle(I18nAccess.getInstance(ctx.getRequest()).getText("content.preview"));
					break;
				case ContentModuleContext.PAGE_MODE:
					currentModule.setToolsRenderer("/jsp/actions.jsp?button_edit=true&button_preview=true&button_delete_page=true" + publish);
					if (ctx.getCurrentPage() != null) {
						request.setAttribute("page", ctx.getCurrentPage().getPageBean(ctx));
					}
					if (globalContext.getAllTaxonomy(ctx) != null) {
						request.setAttribute("taxonomySelect", globalContext.getAllTaxonomy(ctx).getSelectHtml(ctx.getCurrentPage().getTaxonomy(), ctx.getGlobalContext().getSpecialConfig().isTaxonomyUnderlineActive()));
					}
					currentModule.setRenderer("/jsp/page_properties.jsp");
					currentModule.setBreadcrumbTitle(I18nAccess.getInstance(ctx.getRequest()).getText("item.title"));
					break;
				default:
					currentModule.setToolsRenderer("/jsp/actions.jsp?button_preview=true&button_page=true&button_save=true&button_copy=true&languages=true&areas=true" + publish);
					currentModule.setRenderer("/jsp/content_wrapper.jsp");
					currentModule.setBreadcrumbTitle(I18nAccess.getInstance(ctx.getRequest()).getText("content.mode.content"));
					break;
				}
			}

			List<String> roles = new LinkedList<String>();
			List<String> adminOtherRole = new LinkedList<String>();
			Set<String> roleSet = new HashSet<String>();

			if (ctx.getCurrentEditUser() != null) {
				for (String role : globalContext.getAdminUserRoles()) {
					roleSet.clear();
					roleSet.add(role);
					if (ctx.getCurrentEditUser().validForRoles(roleSet)) {
						roles.add(role);
					} else {
						adminOtherRole.add(role);
					}
				}
				Collections.sort(roles);
				ctx.getRequest().setAttribute("adminRoles", roles);
				ctx.getRequest().setAttribute("adminOtherRole", adminOtherRole);
			}
		}

		ComponentContext componentContext = ComponentContext.getInstance(request);
		if (ctx.isEditPreview() && request.getParameter("comp_id") != null || (componentContext.getNewComponents() != null && componentContext.getNewComponents().size() == 1)) {
			InfoBean.getCurrentInfoBean(ctx).setTools(false);
			ctx.getRequest().setAttribute("noinsert", "true");
		}
		if (ctx.isEditPreview() && request.getParameter("mode") != null && request.getParameter("mode").equals("3")) {
			InfoBean.getCurrentInfoBean(ctx).setTools(false);
			ctx.getRequest().setAttribute("noinsert", "true");
		}

		Map<String, String> screenshortParam = new HashMap<String, String>();
		screenshortParam.put(ContentContext.TAKE_SCREENSHOT, "true");
		if (ctx.getCurrentPage() != null) {
			screenshortParam.put(ContentContext.TAKE_SCREENSHOT_PAGE_NAME, ctx.getCurrentPage().getName());
		}
		ctx.getRequest().setAttribute("takeSreenshotUrl", URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), screenshortParam));

		/** COMPONENT LIST **/
		loadComponentList(ctx);

		/** CONTENT **/
		/*
		 * ComponentContext compCtx = ComponentContext.getInstance(request);
		 * IContentComponentsList elems = ctx.getCurrentPage().getContent(ctx); if
		 * (compCtx.getNewComponents().length == 0) { while (elems.hasNext(ctx)) {
		 * compCtx.addNewComponent(elems.next(ctx)); } }
		 */

		/** page properties **/
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		List<Template> templates = ctx.getCurrentTemplates();
		Collections.sort(templates);

		if (ctx.getCurrentTemplate() != null) {
			ctx.getRequest().setAttribute("areas", ctx.getCurrentTemplate().getAreas(AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser())));
		}
		ctx.getRequest().setAttribute("currentArea", editCtx.getCurrentArea());

		request.setAttribute("templates", templates);

		if (ctx.getCurrentTemplate() != null) {
			String templateImageURL = URLHelper.createTransformStaticTemplateURL(ctx, ctx.getCurrentTemplate(), "template", ctx.getCurrentTemplate().getVisualFile());
			request.setAttribute("templateImageUrl", templateImageURL);
		}

		Template inheritedTemplate = null;
		if (ctx.getCurrentPage() != null && ctx.getCurrentPage().getParent() != null) {
			inheritedTemplate = TemplateFactory.getTemplate(ctx, ctx.getCurrentPage().getParent());
		}
		if (inheritedTemplate != null) {
			ctx.getRequest().setAttribute("inheritedTemplate", inheritedTemplate);
		} else {
			if (globalContext.getDefaultTemplate() != null) {
				ctx.getRequest().setAttribute("inheritedTemplate", TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(globalContext.getDefaultTemplate()));
			}
		}

		if (ctx.getCurrentPage() == null) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.no-content"), GenericMessage.ERROR), false);
		}

		if (ctx.getCurrentTemplate() == null) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage("no template found on this page.", GenericMessage.ALERT));
			return null;
		}

		/** check area **/
		String badArea = "";
		String sep = "";
		if (ctx.getCurrentPage() != null) {
			ComponentBean[] content = ctx.getCurrentPage().getContent();
			for (ComponentBean componentBean : content) {
				if (componentBean != null) {
					Collection<String> areas = ctx.getCurrentTemplate().getAreas();
					if (!areas.contains(componentBean.getArea())) {
						if (componentBean.getArea() != null && !badArea.contains(componentBean.getArea())) {
							badArea = badArea + sep + componentBean.getArea();
							sep = ",";
						}
					}
				}
			}
		}
		if (badArea.length() > 0) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			ContentContext absCtx = ctx.getContextForAbsoluteURL();
			String url = URLHelper.createURL(absCtx);
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.bad-area") + " \"" + badArea + "\"", GenericMessage.ALERT, url), false);
		}

		return msg;
	}

	@Override
	public String performSearch(ContentContext ctx, ModulesContext moduleContext, String query) throws Exception {
		String msg = null;
		if (query != null) {
			query = query.trim();
			if (query.length() > 0) {
				if (ctx.getCurrentTemplate() != null && ctx.getCurrentTemplate().getSearchRenderer(ctx) != null) {
					ctx.setSpecialContentRenderer(ctx.getCurrentTemplate().getSearchRenderer(ctx));
				}

				SearchResult search = SearchResult.getInstance(ctx);
				search.cleanResult();

				if (query.startsWith("comp:")) {
					query = query.replaceFirst("comp:", "").trim();
					search.searchComponentInPage(ctx, query);
				} else {
					search.search(ctx, (String) null, query, (String) null, null);
				}

				Collection<SearchElement> result = search.getSearchResult();
				if (result.size() > 0) {
					ctx.getRequest().setAttribute("searchList", result);
					Module currentModule = moduleContext.getCurrentModule();
					currentModule.setAbsoluteRenderer("/jsp/edit/generic_renderer/search.jsp");
					currentModule.setToolsRenderer(null);
					currentModule.setBreadcrumb(false);
					currentModule.setSidebar(false);
				}
			}
		} else {
			msg = "error no query for search.";
		}
		return msg;
	}

	public static final String performChangeComponent(GlobalContext globalContext, EditContext editCtx, ContentContext ctx, ComponentContext componentContext, RequestService requestService, I18nAccess i18nAccess, Module currentModule, ContentModuleContext modCtx) throws Exception {
		String newType = requestService.getParameter("type", null);
		String message = null;
		if (newType != null) {
			editCtx.setActiveType(newType);
			newType = i18nAccess.getText("content." + newType, newType);
			// String msg = i18nAccess.getText("content.new-type", new
			// String[][] { { "type", newType } });
			// MessageRepository.getInstance(ctx).setGlobalMessage(new
			// GenericMessage(msg, GenericMessage.INFO));
			if (requestService.getParameter("comp_id", null) != null) {
				prepareUpdateInsertLine(ctx);
				// return performEditpreview(requestService, ctx,
				// componentContext, editCtx,
				// ContentService.getInstance(globalContext),
				// ModulesContext.getInstance(ctx.getRequest().getSession(),
				// globalContext), modCtx);
			} else {
				Box componentBox = currentModule.getBox("components");
				if (componentBox != null) {
					loadComponentList(ctx);
					componentBox.update(ctx);
				}
				prepareUpdateInsertLine(ctx);
			}
		} else {
			message = "Fatal error : type not found";
		}
		return message;
	}

	public static final String performInsert(HttpServletRequest request, HttpServletResponse response, RequestService rs, ContentService contentService, GlobalContext globalContext, HttpSession session, EditContext editContext, ContentContext ctx, ContentService content, Module currentModule, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {
		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR), false);
			return i18nAccess.getText("action.block");
		}
		String previousId = rs.getParameter("previous", null);

		String type = request.getParameter("type");

		if (previousId == null || type == null) {
			logger.warning("bad insert request need previousId and component type.");
			return "bad insert request need previousId and component type.";
		}

		String areaKey = null;
		String area = rs.getParameter("area", null);

		MenuElement parentPage = ctx.getCurrentPage();
		MenuElement targetPage = NavigationHelper.searchPage(ctx, rs.getParameter("pageContainerID", null));

		if (targetPage == null) {
			targetPage = ctx.getCurrentPage();
		}

		if (area != null) {
			Template template = TemplateFactory.getTemplate(ctx, targetPage);
			for (Map.Entry<String, String> areaId : template.getAreasMap().entrySet()) {
				if (areaId.getValue().equals(area)) {
					areaKey = areaId.getKey();
				}
			}
			if (areaKey == null) {
				logger.warning("area not found : " + area);
				return "area not found : " + area;
			}
			// ctx = ctx.getContextWithArea(areaKey);
			editContext.setCurrentArea(areaKey);
		}

		if (areaKey == null) {
			areaKey = EditContext.getInstance(globalContext, session).getCurrentArea();
		}

		String newId = null;

		if (type.equals("clipboard")) {
			ClipBoard cb = ClipBoard.getInstance(ctx.getRequest());
			Object copied = cb.getCopied();
			if (copied == null || !(copied instanceof ComponentBean)) {
				return "error no item in clipBoard";
			} else {
				ComponentBean bean = (ComponentBean) copied;
				IContentVisualComponent sourceComp = contentService.getComponent(ctx, bean.getId());
				if (!bean.getType().equals(TableBreak.TYPE)) {
					if (!globalContext.getSpecialConfig().isPasteAsMirror() || globalContext.isMailingPlatform() || !sourceComp.isMirroredByDefault(ctx) && !(sourceComp instanceof IContainer)) {
						newId = content.createContent(ctx, targetPage, areaKey, previousId, bean, true);
					} else {
						if (!(sourceComp instanceof IContainer)) {
							ComponentBean mirrorComponentBean = new ComponentBean(MirrorComponent.TYPE, bean.getId(), ctx.getRequestContentLanguage());
							newId = content.createContent(ctx, targetPage, areaKey, previousId, mirrorComponentBean, true);
						} else {
							IContentVisualComponent nextComp = sourceComp;
							newId = content.createContent(ctx, targetPage, areaKey, newId, nextComp.getComponentBean(), true);
							boolean closeFound = false;
							int depth = 0;
							while (!closeFound) {
								nextComp = ComponentHelper.getNextComponent(nextComp, ctx);
								if (nextComp != null) {
									newId = content.createContent(ctx, targetPage, areaKey, newId, nextComp.getComponentBean(), true);
									if (nextComp.getType().equals(sourceComp.getType())) {
										if (((IContainer) nextComp).isOpen(ctx)) {
											depth++;
										} else {
											if (depth == 0) {
												closeFound = true;
											} else {
												depth--;
											}
										}
									}
								} else {
									closeFound = true;
								}
							}
						}
					}
				} else if (bean.getType().equals(TableBreak.TYPE)) {
					IContentVisualComponent currentComp = content.getComponent(ctx, bean.getId());
					IContentVisualComponent openTable = ((TableBreak) currentComp).getOpenTableComponent(ctx);
					if (openTable == null) {
						return "error table empty";
					} else {
						ContentContext compAreaContext = ctx.getContextWithArea(currentComp.getArea());
						ContentElementList tableContent = currentComp.getPage().getContent(compAreaContext);
						boolean inTable = false;
						while (tableContent.hasNext(compAreaContext)) {
							IContentVisualComponent nextComp = tableContent.next(compAreaContext);
							if (nextComp.getId().equals(openTable.getId())) {
								inTable = true;
							}
							if (inTable) {
								newId = content.createContent(compAreaContext, targetPage, areaKey, previousId, nextComp.getComponentBean(), true);
								if (compAreaContext.isAjax()) {
									compAreaContext.getRequest().setAttribute(AbstractVisualComponent.SCROLL_TO_COMP_ID_ATTRIBUTE_NAME, newId);
								}
								previousId = newId;
								if (nextComp instanceof TableBreak) {
									inTable = false;
								}
							}
						}
					}
				}
			}
		} else if (type.equals("clipboard-page")) {
			EditContext editCtx = EditContext.getInstance(globalContext, session);
			MenuElement copiedPage;
			if (rs.getParameter("pagename", null) != null) {
				copiedPage = content.getNavigation(ctx).searchChildFromName(rs.getParameter("pagename", null));
			} else {
				copiedPage = content.getNavigation(ctx).searchChildFromId(editCtx.getContextForCopy(ctx).getCurrentPage().getId());
			}
			if (copiedPage != null) {
				if (copiedPage.getId().equals(targetPage.getId())) {
					return i18nAccess.getText("edit.message.page_paste_it_self", "couldn't paste the page on it self");
				} else {
					ComponentBean mirrorComponentBean = new ComponentBean(PageMirrorComponent.TYPE, copiedPage.getId(), ctx.getRequestContentLanguage());
					newId = content.createContent(ctx, targetPage, areaKey, previousId, mirrorComponentBean, true);
				}
			} else {
				return "error : page not found.";
			}
		} else {
			boolean foundType = false;
			for (ComponentWrapper comp : ComponentFactory.getComponentForDisplay(ctx, false)) {
				if (comp.getType().equals(type)) {
					foundType = true;
				}
			}
			if (!foundType) {
				ctx.setNeedRefresh(true);
				return "component type not found : " + type;
			}
			IContentVisualComponent previousComp = contentService.getComponent(ctx, previousId);
			newId = content.createContent(ctx, targetPage, areaKey, previousId, type, "", true);
			IContentVisualComponent openBox = contentService.getComponent(ctx, newId);
			if (openBox instanceof IContainer && ctx.isEditPreview()) {
				if (previousComp != null && !(previousComp instanceof IContainer)) {
					ComponentHelper.moveComponent(ctx, previousComp, openBox, targetPage, area);
				}
				String closePreviousid = previousId;
				if (previousComp == null) {
					closePreviousid = newId;
				}
				if (previousComp instanceof IContainer) {
					closePreviousid = openBox.getId();
				}
				newId = content.createContent(ctx, targetPage, areaKey, closePreviousid, type, "", true);
				IContentVisualComponent closeBox = contentService.getComponent(ctx, newId);
				((IContainer) openBox).setOpen(ctx, true);
				((IContainer) closeBox).setOpen(ctx, false);
			}
		}
		if (newId == null) {
			return "error no component create.";
		}

		IContentVisualComponent comp = content.getComponent(ctx, newId);
		if (comp != null) {
			comp.markAsNew(ctx);
			comp.setRepeat(false);
			// comp.setPreviousComponent(ComponentHelper.getPreviousComponent(comp, ctx));
			// comp.setNextComponent(ComponentHelper.getNextComponent(comp, ctx));
			ComponentHelper.updateNextAndPrevious(ctx, comp.getPage(), comp.getArea());
			if (!type.equals("clipboard") && StringHelper.isTrue(rs.getParameter("init", null))) {
				String defaultValue = content.getDefaultValue(ctx, comp.getType());
				if (defaultValue != null) {
					comp.setValue(defaultValue);
				} else {
					comp.initContent(ctx);
				}
			}
		}

		if (ctx.isAjax()) {
			if (!ctx.isEditPreview()) {
				updateComponent(ctx, currentModule, newId, previousId);
			}
			String selecterPrefix = "";
			if (parentPage.isChildrenAssociation()) {
				selecterPrefix = "#page_" + rs.getParameter("pageContainerID", "#ID_NOT_DEFINED") + " #";

				if (targetPage != null) {
					ctx.setCurrentPageCached(targetPage);
				}
			}

			String mode = rs.getParameter("render-mode", null);
			if (mode != null) {
				ctx.setRenderMode(Integer.parseInt(mode));
			}
			ctx.getRequest().setAttribute(AbstractVisualComponent.SCROLL_TO_COMP_ID_ATTRIBUTE_NAME, newId);

			String htmlArea = ComponentHelper.renderArea(ctx, areaKey);
			ctx.getAjaxInsideZone().put(selecterPrefix + area, htmlArea);
		}
		ctx.resetCurrentPageCached();
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.setAskStore(true);
		modifPage(ctx, targetPage);
		autoPublish(request, response);

		if (ctx.isPreview()) {
			updatePreviewCommands(ctx, null);
		}

		return null;
	}

	public static final String performDelete(ContentContext ctx, HttpServletRequest request, ContentService content, EditContext editContext, HttpServletResponse response, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR), false);
			return null;
		}

		RequestService rs = RequestService.getInstance(request);

		String id = rs.getParameter("id", null);
		if (id != null) {

			if (id.equals("clipboard")) {
				ClipBoard.getInstance(request).clear();
				updatePreviewCommands(ctx, null);
				return null;
			}

			MenuElement targetPage = NavigationHelper.searchPage(ctx, request.getParameter("pageCompID"));
			if (targetPage == null) {
				targetPage = ctx.getCurrentPage();
			}

			IContentVisualComponent comp = content.getComponent(ctx, id);
			if (comp == null) {
				return "component not found : " + id;
			}

			if (!AdminUserSecurity.getInstance().canModifyConponent(ctx, id)) {
				logger.warning("user : " + ctx.getCurrentUserId() + " can't delete component : " + id);
				return "security error";
			}

			if (comp.getPreviousComponent() != null) {
				ctx.getRequest().setAttribute(AbstractVisualComponent.SCROLL_TO_COMP_ID_ATTRIBUTE_NAME, comp.getPreviousComponent().getId());
			}
			if (comp instanceof IContainer && ((IContainer) comp).isOpen(ctx) && ctx.isEditPreview()) {
				List<String> compToRemove = new LinkedList<String>();
				compToRemove.add(comp.getId());
				boolean closeFound = false;
				IContentVisualComponent nextComp = ComponentHelper.getNextComponent(comp, ctx);
				while (!closeFound) {
					if (nextComp != null) {
						compToRemove.add(nextComp.getId());
						if (nextComp.getType().equals(comp.getType()) && !((IContainer) nextComp).isOpen(ctx)) {
							compToRemove.add(nextComp.getId());
							closeFound = true;
						}
					} else {
						closeFound = true;
					}
					nextComp = ComponentHelper.getNextComponent(nextComp, ctx);
				}
				for (String idToRemove : compToRemove) {
					targetPage.removeContent(ctx, idToRemove);
				}
			} else {
				targetPage.removeContent(ctx, id);
			}
			GlobalContext globalContext = GlobalContext.getInstance(request);
			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.setAskStore(true);

			if (ctx.isAjax()) {
				ctx.addAjaxZone("comp-" + id, "");
				ctx.addAjaxZone("comp-child-" + id, "");
				ctx.addAjaxInsideZone("insert-line-" + id, "");

				String selecterPrefix = "";
				if (ctx.getCurrentPage().isChildrenAssociation()) {
					if (rs.getParameter("pageCompID", null) != null) {
						selecterPrefix = "#page_" + rs.getParameter("pageCompID", "#ID_NOT_DEFINED") + " #";
					}
					ctx.setCurrentPageCached(targetPage);
				}
				updateArea(ctx, selecterPrefix, comp);
				if (ctx.isEditPreview()) {
					ctx.setClosePopup(true);
				}
			}

			ReverseLinkService.getInstance(globalContext).clearCache();

			modifPage(ctx, targetPage);
			autoPublish(request, response);

			if (ctx.isPreview()) {
				updatePreviewCommands(ctx, null);
			}
			ComponentHelper.updateNextAndPrevious(ctx, comp.getPage(), comp.getArea());

		}
		return null;
	}

	private static void updateArea(ContentContext ctx, String selecterPrefix, IContentVisualComponent comp) throws Exception {
		selecterPrefix = StringHelper.neverNull(selecterPrefix);
		Template template = TemplateFactory.getTemplate(ctx, comp.getPage());
		if (template != null && template.getAreasMap() != null) {
			String areaHTMLid = template.getAreasMap().get(comp.getArea());
			ctx.getAjaxInsideZone().put(selecterPrefix + areaHTMLid, ComponentHelper.renderArea(ctx, comp.getArea()));
		}
	}

	public static final String performSave(ContentContext ctx, EditContext editContext, GlobalContext globalContext, ContentService content, ComponentContext componentContext, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository, Module currentModule, AdminUserFactory adminUserFactory) throws Exception {
		return performModifComponent(ctx, editContext, globalContext, content, componentContext, requestService, i18nAccess, messageRepository, currentModule, adminUserFactory, false);
	}

	public static final String performUpload(ContentContext ctx, EditContext editContext, GlobalContext globalContext, ContentService content, ComponentContext componentContext, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository, Module currentModule, AdminUserFactory adminUserFactory) throws Exception {
		return performModifComponent(ctx, editContext, globalContext, content, componentContext, requestService, i18nAccess, messageRepository, currentModule, adminUserFactory, true);
	}

	private static final String performModifComponent(ContentContext ctx, EditContext editContext, GlobalContext globalContext, ContentService content, ComponentContext componentContext, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository, Module currentModule, AdminUserFactory adminUserFactory, boolean upload) throws Exception {
		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR), false);
			return null;
		}

		org.javlo.helper.LocalLogger.forceStartCount("save");

		String message = null;

		// IContentComponentsList contentList = currentPage.getAllContent(ctx);
		List<String> components = requestService.getParameterListValues("components", Collections.EMPTY_LIST);

		// boolean needRefresh = false;

		for (String compId : components) {
			IContentVisualComponent elem = content.getComponent(ctx, compId);
			if (elem != null && StringHelper.isTrue(requestService.getParameter("id-" + elem.getId(), null))) {
				if (AdminUserSecurity.getInstance().canModifyConponent(ctx, compId)) {
					if (!upload) {
						elem.performConfig(ctx);
						message = elem.performEdit(ctx);
					} else {
						if (elem instanceof IUploadResource) {
							((IUploadResource) elem).performUpload(ctx);
						} else {
							logger.warning("you can't upload file in " + elem.getType());
							return "you can't upload file in " + elem.getType();
						}
					}
					if (ctx.isEditPreview()) {
						componentContext.addNewComponent(elem);
					}

					if (ctx.isEditPreview() && componentContext.getNewComponents() != null && componentContext.getNewComponents().size() == 1) {
						InfoBean.getCurrentInfoBean(ctx).setTools(false);
						ctx.getRequest().setAttribute("noinsert", "true");
					}

					String rawValue = requestService.getParameter("raw_value_" + elem.getId(), null);
					if (rawValue != null) { // if elem not modified check
											// modification via rawvalue
						if (!rawValue.equals(elem.getValue(ctx))) {
							logger.info("raw value modification for " + elem.getType());
							elem.setValue(rawValue);
							elem.setNeedRefresh(true);
						}
					} else if (elem instanceof IReverseLinkComponent) {
						ReverseLinkService.getInstance(globalContext).clearCache();
					}
					if (elem.isNeedRefresh() && ctx.isAjax()) {
						updateComponent(ctx, currentModule, elem.getId(), null);
					}
				}
			}

			if (elem == null) {
				logger.severe("fatal error : component not found : " + compId + " (" + ctx + ')');
				return "fatal error : component not found : " + compId;
			}

			if (elem.isModify()) {
				elem.stored();
			}
		}

		// ctx.setNeedRefresh(needRefresh);
		modifPage(ctx, ctx.getCurrentPage());
		if (adminUserFactory.getCurrentUser(ctx.getRequest().getSession()) != null) {
			content.setAttribute(ctx, "user.update", adminUserFactory.getCurrentUser(ctx.getRequest().getSession()).getLogin());
		}
		// PersistenceService.getInstance(globalContext).store(ctx);
		PersistenceService.getInstance(globalContext).setAskStore(true);

		if (message == null) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.updated"), GenericMessage.INFO));
			autoPublish(ctx.getRequest(), ctx.getResponse());
		} else {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.not-updated"), GenericMessage.ALERT));
		}

		if (requestService.getParameter("save", null) != null && editContext.isPreviewEditionMode() && !ResourceStatus.isResource(ctx.getRequest().getSession()) && requestService.getParameter("upload", null) == null) {
			String url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE));
			if (!StringHelper.isEmpty(requestService.getParameter("forward_anchor")) && !url.contains("#")) {
				url = url + '#' + requestService.getParameter("forward_anchor");
			}
			ctx.setParentURL(url);
			if (message == null) {
				ctx.setClosePopup(true);
			}
		}

		if (ctx.isEditPreview() && componentContext.getNewComponents() != null && componentContext.getNewComponents().size() == 1) {
			InfoBean.getCurrentInfoBean(ctx).setTools(false);
			ctx.getRequest().setAttribute("noinsert", "true");
		}

		if (ctx.isClosePopup() && ctx.isAjax()) {
			if (ctx.getParentURL() != null) {
				ctx.getAjaxInsideZone().put("main-body", "<script>closePopup('" + ctx.getParentURL() + "');</script>");
			} else {
				ctx.getAjaxInsideZone().put("main-body", "<script>closePopup();</script>");
			}
		}

		return message;
	}

	public static final String performChangeMode(HttpSession session, RequestService requestService, ContentModuleContext modCtx) {
		modCtx.setMode(Integer.parseInt(requestService.getParameter("mode", "" + ContentModuleContext.EDIT_MODE)));
		return null;
	}

	public static final String performPageProperties(ServletContext application, GlobalContext globalContext, ContentContext ctx, HttpSession session, ContentService content, EditContext editCtx, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR), false);
		} else {
			boolean isView = requestService.getParameter("view", null) != null;
			String pageName = requestService.getParameter("name", null);
			String newName = requestService.getParameter("new_name", null);
			if (pageName == null || newName == null) {
				return "bad parameter for change page properties.";
			}
			if (newName.contains(".") || newName.contains(" ")) {
				return "new page name could not contains space and '.'";
			}
			MenuElement page = content.getNavigation(ctx).searchChildFromName(pageName);
			if (page == null) {
				return "page not found : " + pageName;
			} else {
				String errorMessage = null;
				boolean modify = false;
				if (!pageName.equals(StringHelper.createFileName(newName))) {
					if (nameExist(ctx, newName)) {
						errorMessage = i18nAccess.getText("action.validation.name-allready-exist", new String[][] { { "name", pageName } });
					}
					if (errorMessage == null) {
						if (page.isRootChildrenAssociation()) {
							for (MenuElement child : page.getAllChildrenList()) {
								child.setName(StringUtils.replaceOnce(child.getName(), pageName, newName));
							}
						}
						page.setName(newName);
						modify = true;
					}
				}

				if (page.isVisible() != isView) {
					page.setVisible(isView);
					modify = true;
				}

				UserInterfaceContext userInterface = UserInterfaceContext.getInstance(session, globalContext);
				if (!userInterface.isLight()) {
					boolean isActive = StringHelper.isTrue(requestService.getParameter("active", null));
					if (page.isActive(ctx) != isActive) {
						page.setActive(isActive);
						modify = true;
					}
				}
				if (userInterface.isModel()) {
					boolean isModel = StringHelper.isTrue(requestService.getParameter("model", null));
					if (page.isModel() != isModel) {
						page.setModel(isModel);
						modify = true;
					}
				}

				if (userInterface.isAdmin()) {
					boolean isAdmin = StringHelper.isTrue(requestService.getParameter("admin", null));
					if (page.isAdmin() != isAdmin) {
						page.setAdmin(isAdmin);
						modify = true;
					}
				}

				if (requestService.getParameter("shorturl", null) != null) {
					if (!page.isShortURL()) {
						page.getShortURL(ctx); // create short url
						messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.shorturl"), GenericMessage.ALERT), false);
					} else {
						return "this page have already short url.";
					}
				}

				boolean postCheck = requestService.getParameter("special_input", null) != null;

				boolean isBreakRepeat = requestService.getParameter("break_repeat", null) != null;
				if (postCheck && page.isBreakRepeat() != isBreakRepeat) {
					page.setBreakRepeat(isBreakRepeat);
					modify = true;
				}

				/** children agregator **/
				boolean childrenAssociation = StringHelper.isTrue(requestService.getParameter("association", null));
				if (postCheck && page.isChildrenAssociation() != childrenAssociation) {
					page.setChildrenAssociation(childrenAssociation);
					modify = true;
				}

				/** roles **/
				InfoBean infoBean = InfoBean.getCurrentInfoBean(ctx);
				Set<String> userRoles = new HashSet<String>();
				for (String role : infoBean.getRoles()) {
					if (requestService.getParameter("user-" + role, null) != null) {
						userRoles.add(role);
					}
				}
				page.setUserRoles(userRoles);
				page.clearEditorGroups();

				page.setUserRolesInherited(StringHelper.isTrue(requestService.getParameter("userRolesInherited", null), false));

				for (String role : infoBean.getAdminRoles()) {
					if (requestService.getParameter("admin-" + role, null) != null) {
						page.addEditorRole(role);
					}
				}

				/** page type **/
				String pageType = requestService.getParameter("page_type", null);
				if (pageType != null) {
					page.setType(pageType);
				}

				/** seo weight **/
				String seoWeight = requestService.getParameter("seo_weight", null);
				if (seoWeight != null) {
					page.setSeoWeight(Integer.parseInt(seoWeight));
				}

				/** shared **/
				String pageShared = requestService.getParameter("share", null);
				if (pageShared != null) {
					page.setSharedName(pageShared);
					ISharedContentProvider provider = SharedContentService.getInstance(ctx).getProvider(ctx, JavloSharedContentProvider.NAME);
					if (provider != null) {
						provider.refresh(ctx);
					}
				}

				/** need validation **/
				if (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser())) {
					page.setNoValidation(StringHelper.isTrue(requestService.getParameter("noval", null)));
				}

				/** ipsecurity **/
				String ipsecurity = requestService.getParameter("ipsecurity", null);
				if (StringHelper.isEmpty(ipsecurity)) {
					ipsecurity = null;
				}
				if (ipsecurity != null && ContentService.getInstance(globalContext).getNavigation(ctx).searchChildFromName(ipsecurity) == null) {
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage("page not found  : " + ipsecurity, GenericMessage.ERROR), false);
				} else {
					page.setIpSecurityErrorPageName(ipsecurity);
				}

				/** taxonomy **/
				if (ctx.getGlobalContext().getAllTaxonomy(ctx).isActive()) {
					String[] taxonomies = requestService.getParameterValues("taxonomy", null);
					if (taxonomies != null) {
						if (page.getTaxonomy() == null) {
							page.setTaxonomy(new HashSet<String>());
						}
						page.getTaxonomy().clear();
						for (String taxonomy : taxonomies) {
							page.getTaxonomy().add(taxonomy);
						}
					} else {
						page.setTaxonomy(new HashSet<String>());
					}
				}

				/** publish time range **/
				if (requestService.getParameter("start_publish", null) != null) {
					String startPublish = requestService.getParameter("start_publish", "").trim();
					if (startPublish.length() > 0) {
						Date startDate = StringHelper.smartParseDate(startPublish);
						page.setStartPublishDate(startDate);
					} else {
						page.setStartPublishDate(null);
					}
					String endPublish = requestService.getParameter("end_publish", "").trim();
					if (endPublish.length() > 0) {
						Date endDate = StringHelper.smartParseDate(endPublish);
						page.setEndPublishDate(endDate);
					} else {
						page.setEndPublishDate(null);
					}
				}

				String templateName = requestService.getParameter("template", null);
				if (templateName != null) {
					MailingModuleContext mailingCtx = MailingModuleContext.getInstance(ctx.getRequest());
					mailingCtx.setCurrentTemplate(null);
					if (templateName.length() > 1) {
						Template template = TemplateFactory.getTemplates(application).get(templateName);
						if (template != null && ctx.getCurrentTemplates().contains(template)) {
							page.setTemplateId(template.getName());
							modify = true;
						} else {
							return "template not found : " + templateName;
						}
					} else {
						page.setTemplateId(null); // inherited
					}
					ctx.setCurrentTemplate(null); // reset current template
				}
				if (errorMessage != null) {
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(errorMessage, GenericMessage.ERROR), false);
				} else {
					if (modify) {
						messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("message.update-page-properties"), GenericMessage.INFO), false);
					}
				}
				modifPage(ctx, page);
				page.clearPageBean(ctx);
				PersistenceService.getInstance(globalContext).setAskStore(true);
			}
			if (ctx.isEditPreview()) {
				String url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), page.getPath());
				if (page.getChildMenuElements().size() > 0 && page.getChildMenuElements().iterator().next().isChildrenAssociation()) {
					url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), page.getChildMenuElements().iterator().next().getPath());
				}
				ctx.setParentURL(url);
			}
		}

		if (ctx.isEditPreview()) {
			ctx.setClosePopup(true);
		}

		return null;
	}

	public static final String performChangeLanguage(RequestService requestService, ContentContext ctx, GlobalContext globalContext, I18nAccess i18nAccess, MessageRepository messageRepository) throws IOException {
		String lg = requestService.getParameter("language", null);
		if (lg != null) {
			if (globalContext.getLanguages().contains(lg)) {
				ctx.setLanguage(lg);
			}
			ctx.setContentLanguage(lg);
			ctx.setRequestContentLanguage(lg);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("edit.message.new-language") + ' ' + lg, GenericMessage.INFO));
			String newURL = URLHelper.createURL(ctx);

			if (requestService.getParameter(ElementaryURLHelper.BACK_PARAM_NAME, null) != null) {
				newURL = URLHelper.addRawParam(newURL, ElementaryURLHelper.BACK_PARAM_NAME, requestService.getParameter(ElementaryURLHelper.BACK_PARAM_NAME, null));
			}
			newURL = messageRepository.forwardMessage(newURL);

			ctx.sendRedirect(newURL);
		} else {
			return "bad request structure : 'language' not found.";
		}
		return null;
	}

	public static String performAddPage(RequestService requestService, ContentContext ctx, I18nAccess i18nAccess, ContentService content) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		String message = null;

		try {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());

			String path = ctx.getPath();
			String title = requestService.getParameter("name", null);

			if (StringHelper.isEmpty(title)) {
				int index = 1;
				title = ctx.getCurrentPage().getName() + "_" + index;
				while (content.getNavigation(ctx).searchChildFromName(title) != null) {
					index++;
					title = ctx.getCurrentPage().getName() + "_" + index;
				}
			}

			String nodeName = StringHelper.createFileName(title);
			String parentName = requestService.getParameter("parent", null);

			if (nodeName != null) {
				message = validNodeName(nodeName, i18nAccess);
			}

			if (nodeName != null && nameExist(nodeName, ctx, content)) {
				message = i18nAccess.getText("action.validation.name-allready-exist", new String[][] { { "name", nodeName } });
			}
			if (message == null) {
				MenuElement elem = MenuElement.getInstance(ctx);
				elem.setCreator(editCtx.getUserPrincipal().getName());
				elem.setVisible(globalContext.isNewPageVisible());
				MenuElement parent = ctx.getCurrentPage();
				if (parentName != null) {
					parent = content.getNavigation(ctx).searchChildFromName(parentName);
					if (parent == null) {
						return "page not found : " + parentName;
					}
				}
				boolean needInitContent = true;
				if (nodeName == null) {
					needInitContent = false;
					nodeName = parent.getName() + "-1";
					int index = 2;
					while (content.getNavigation(ctx).searchChildFromName(nodeName) != null) {
						nodeName = parent.getName() + '-' + index;
						index++;
					}
				}
				elem.setName(nodeName);
				if (requestService.getParameter("add-first", null) == null) {
					parent.addChildMenuElementAutoPriority(elem);
				} else {
					elem.setPriority(0);
					parent.addChildMenuElement(elem);
				}

				path = path + "/" + nodeName;

				/** initial content **/
				if (needInitContent && globalContext.getSpecialConfig().isContentAddTitle()) {
					List<ComponentBean> initContent = new LinkedList<ComponentBean>();
					for (String lg : globalContext.getContentLanguages()) {
						i18nAccess.requestInit(ctx);
						if (globalContext.hasComponent(Title.class)) {
							initContent.add(new ComponentBean(title, Title.TYPE, elem.getName(), lg, false, ctx.getCurrentEditUser()));
						} else if (globalContext.hasComponent(Heading.class)) {
							initContent.add(new ComponentBean(title, Heading.TYPE, Heading.TEXT + '=' + title, lg, false, ctx.getCurrentEditUser()));
						}
					}
					content.createContent(ctx, elem, initContent, "0", false);
				}

				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				persistenceService.setAskStore(true);
				autoPublish(ctx.getRequest(), ctx.getResponse());

				NavigationService navigationService = NavigationService.getInstance(globalContext);
				navigationService.clearPage(ctx);

				String msg = i18nAccess.getText("action.add.new-page", new String[][] { { "path", path } });
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));

				if (parentName != null) { // if parentpage defined new page
											// become the active page.
					ctx.setPath(elem.getPath());
					String forwardURL = ctx.getResponse().encodeRedirectURL(URLHelper.createURL(ctx));
					ctx.getResponse().sendRedirect(forwardURL);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		return message;
	}

	public static final String performChangeArea(ContentContext ctx, RequestService requestService, EditContext editContext, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {
		String area = requestService.getParameter("area", null);
		if (area == null) {
			return "bad request structure : need 'area' parameter";
		}
		if (!ctx.getCurrentTemplate().getAreas().contains(area)) {
			return "bad area : " + area;
		}
		editContext.setCurrentArea(area);
		messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("edit.message.new-area") + " : " + area, GenericMessage.INFO));
		return null;
	}

	public static String performNeedValidation(ContentContext ctx, MenuElement currentPage, I18nAccess i18nAccess) throws Exception {
		AdminUserSecurity userSecurity = AdminUserSecurity.getInstance();
		if (userSecurity.canRole(ctx.getCurrentEditUser(), AdminUserSecurity.CONTENT_ROLE)) {
			currentPage.setNeedValidation(true);
			PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);

			AdminUserFactory userFact = AdminUserFactory.createAdminUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
			TicketBean ticket = new TicketBean();
			ticket.setAuthors(ctx.getCurrentEditUser().getLogin());
			ticket.setTitle(i18nAccess.getText("flow.ticket.title") + " : " + currentPage.getTitle(ctx));
			ticket.setCategory("validation");
			ticket.setContext(ctx.getGlobalContext().getContextKey());
			ticket.setCreationDate(new Date());
			ticket.setUrl(URLHelper.createURL(ctx));
			List<String> users = new LinkedList<String>();
			for (IUserInfo userInfo : userFact.getUserInfoList()) {
				if (userSecurity.haveRole(userInfo, AdminUserSecurity.VALIDATION_ROLE)) {
					users.add(userInfo.getLogin());
				}
			}
			ticket.setUsers(users);
			TicketService.getInstance(ctx.getGlobalContext()).updateTicket(ctx, ticket, false);
		} else {
			return "Security error !";
		}
		return null;
	}

	public static String performValidate(ContentContext ctx, MenuElement currentPage) throws Exception {
		AdminUserSecurity userSecurity = AdminUserSecurity.getInstance();
		if (userSecurity.canRole(ctx.getCurrentEditUser(), AdminUserSecurity.VALIDATION_ROLE)) {
			currentPage.setValid(true);
			currentPage.setValidater(ctx.getCurrentEditUser().getLogin());
			TicketService ticketService = TicketService.getInstance(ctx.getGlobalContext());
			String pageURL = URLHelper.createURL(ctx);
			for (Ticket ticket : ticketService.getTickets()) {
				if (ticket.getUrl() != null && ticket.getUrl().equals(pageURL) && ticket instanceof TicketBean) {
					((TicketBean) ticket).setStatus(Ticket.STATUS_DONE);
					ticketService.updateTicket(ctx, (TicketBean) ticket, true);
				}
			}
			PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		} else {
			return "Security error !";
		}
		return null;
	}

	public static String performPublish(ServletContext application, HttpServletRequest request, StaticConfig staticConfig, GlobalContext globalContext, ContentService content, ContentContext ctx, I18nAccess i18nAccess) throws Exception {

		int trackerNumber = TimeTracker.start(globalContext.getContextKey(), "publish");

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		synchronized (globalContext.getLockLoadContent()) {

			DebugHelper.writeInfo(null, System.out);

			String message = null;

			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);

			if (!globalContext.isPortail()) {
				persistenceService.publishPreviewFile(ctx);
				globalContext.setPublishDate(new Date());
				globalContext.setLatestPublisher(ctx.getCurrentEditUser().getLogin());
				globalContext.storeRedirectUrlList();
				content.releaseViewNav(globalContext);
				String msg = i18nAccess.getText("content.published");
				MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), new GenericMessage(msg, GenericMessage.INFO), false);
			} else {
				ContentContext viewCtx = new ContentContext(ctx);
				viewCtx.setRenderMode(ContentContext.VIEW_MODE);
				MenuElement viewNav = content.getNavigation(viewCtx);
				MenuElement previewNav = content.getNavigation(ctx);

				// PULISH STATIC INFO
				content.publishAttributeMap(ctx);

				int modif = NavigationHelper.publishNavigation(ctx, previewNav, viewNav);
				//if (modif>0) {
					TaxonomyService.pushPreviewInView(ctx);
					persistenceService.store(viewCtx, ContentContext.VIEW_MODE, false);
					persistenceService.store(ctx, ContentContext.PREVIEW_MODE, false);
					globalContext.setPublishDate(new Date());
					globalContext.setLatestPublisher(ctx.getCurrentEditUser().getLogin());
					globalContext.storeRedirectUrlList();
					content.releaseViewNav(globalContext);
					content.releasePreviewNav(ctx);
					content.getNavigation(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE));
					String msg = i18nAccess.getText("content.published-portail")+' '+modif;
					MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), new GenericMessage(msg, GenericMessage.INFO), false);
				/*} else {
					String msg = i18nAccess.getText("content.no-published");
					MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), new GenericMessage(msg, GenericMessage.INFO), false);
				}*/
			}

			globalContext.setPublishDate(new Date());
			globalContext.setLatestPublisher(ctx.getCurrentEditUser().getLogin());
			globalContext.storeRedirectUrlList();

			content.releaseViewNav(globalContext);
			content.getNavigation(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE));

			String msg = i18nAccess.getText("content.published");
			MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), new GenericMessage(msg, GenericMessage.INFO), false);
			// MessageRepository.getInstance(ctx).setGlobalMessage(new
			// GenericMessage(msg, GenericMessage.INFO));

			SynchroHelper.performSynchro(ctx);

			NavigationService navigationService = NavigationService.getInstance(globalContext);
			navigationService.clearAllViewPage();

			// clean component list when publish
			ComponentFactory.cleanComponentList(request.getSession().getServletContext(), globalContext);

			ReverseLinkService.getInstance(globalContext).clearCache();

			globalContext.resetURLFactory();

			FileCache.getInstance(application).clearPDF(ctx);

			AdminAction.clearCache(ctx);

			TimeTracker.end(globalContext.getContextKey(), "publish", trackerNumber);

			if (SearchEngineFactory.getEngine(ctx) != null) {
				SearchEngineFactory.getEngine(ctx).updateData(ctx);
			} else {
				logger.severe("no search engine.");
			}

			String eventMessage = staticConfig.getGeneralLister().onPublish(ctx);
			if (eventMessage != null) {
				message = eventMessage;
			}

			return message;
		}

	}

	public static String performDeletePage(GlobalContext globalContext, ContentService content, ContentContext ctx, I18nAccess i18nAccess) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		String message = null;
		String id = ctx.getRequest().getParameter("page");
		MenuElement menuElement;
		menuElement = content.getNavigation(ctx).searchChildFromId(id);
		String path = menuElement.getPath();
		if (menuElement.getParent() == null) {
			return i18nAccess.getText("action.remove.can-not-delete");
		}
		String newPath = menuElement.getParent().getPath();
		if (menuElement.isChildrenOfAssociation()) {
			if (menuElement.getRootOfChildrenAssociation() != null && menuElement.getRootOfChildrenAssociation().getFirstChild() != null) {
				newPath = menuElement.getRootOfChildrenAssociation().getFirstChild().getPath();
			}
		}
		if (menuElement.isChildrenAssociation()) {
			newPath = "/";
			menuElement = menuElement.getParent();
		}
		if (menuElement == null) {
			message = i18nAccess.getText("action.remove.can-not-delete");
		} else {
			synchronized (menuElement) {
				menuElement.clearVirtualParent();
			}
			NavigationService service = NavigationService.getInstance(globalContext);
			service.removeNavigation(ctx, menuElement);
			String msg = i18nAccess.getText("action.remove.deleted", new String[][] { { "path", path } });
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
			autoPublish(ctx.getRequest(), ctx.getResponse());
		}
		NavigationService navigationService = NavigationService.getInstance(globalContext);
		navigationService.clearPage(ctx);
		ReverseLinkService.getInstance(globalContext).clearCache();

		if (ctx.isEditPreview()) {
			ctx.setRenderMode(ContentContext.PREVIEW_MODE);
		}

		ctx.setPath(newPath);
		String forwardURL = ctx.getResponse().encodeRedirectURL(URLHelper.createURL(ctx));
		ctx.setClosePopup(true);
		ctx.setParentURL(forwardURL);
		// ctx.getResponse().sendRedirect(forwardURL);
		return message;
	}

	public static String performMovePageToTrash(RequestService rs, ContentContext ctx, ContentService content, PersistenceService persistenceService, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}
		String message = null;
		String id = ctx.getRequest().getParameter("page");
		MenuElement menuElement;
		menuElement = content.getNavigation(ctx).searchChildFromId(id);
		if (menuElement.isTrash() || menuElement.isInTrash()) {
			return performDeletePage(ctx.getGlobalContext(), content, ctx, i18nAccess);
		}
		String newPath = menuElement.getParent().getPath();
		if (menuElement.isChildrenOfAssociation()) {
			newPath = menuElement.getRootOfChildrenAssociation().getFirstChild().getPath();
		}
		if (menuElement.isChildrenAssociation()) {
			newPath = "/";
			menuElement = menuElement.getParent();
		}
		if (menuElement.getParent() == null) {
			return i18nAccess.getText("action.remove.can-not-delete");
		}
		menuElement.setSavedParent(menuElement.getParent().getId());
		NavigationHelper.movePage(ctx, content.getTrashPage(ctx), null, menuElement);
		String msg = i18nAccess.getText("action.remove.deleted", new String[][] { { "path", menuElement.getPath() } });
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
		autoPublish(ctx.getRequest(), ctx.getResponse());
		ctx.setPath(newPath);
		ctx.setClosePopup(true);

		persistenceService.setAskStore(true);

		ctx.setPath(newPath);
		String forwardURL = ctx.getResponse().encodeRedirectURL(URLHelper.createURL(ctx));
		ctx.getResponse().sendRedirect(forwardURL);

		return message;
	}

	public static final String performPreviewedit(HttpServletRequest request, RequestService rs, EditContext editCtx) {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		if (globalContext.isPreviewMode()) {
			if (rs.getParameter("preview", null) == null) {
				editCtx.setPreviewEditionMode(!editCtx.isPreviewEditionMode());
			} else {
				editCtx.setPreviewEditionMode(StringHelper.isTrue(rs.getParameter("preview", null)));
			}
		}
		return null;
	}

	public static String performEditpreview(RequestService requestService, ContentContext ctx, ComponentContext componentContext, EditContext editContext, ContentService content, ModulesContext moduleContext, ContentModuleContext modCtx) throws Exception {
		moduleContext.searchModule("content").restoreAll();
		performChangeMode(ctx.getRequest().getSession(), requestService, modCtx);
		String[] compsId = requestService.getParameterValues("comp_id", null);
		if (compsId != null) {
			for (String compId : compsId) {
				compId = compId.trim();
				if (compId.startsWith("cp_")) {
					compId = compId.substring(3);
				}
				IContentVisualComponent comp = content.getComponent(ctx, compId);
				if (comp == null) {
					return "component not found : " + compId;
				} else {
					editContext.setCurrentArea(comp.getArea());
				}
				componentContext.addNewComponent(comp);
				modifPage(ctx, ctx.getCurrentPage());
			}
		}
		return null;
	}

	public static String performDisplayComponentsList(RequestService requestService, UserInterfaceContext userInterfaceContext, Module currentModule) throws ContextException {
		userInterfaceContext.setComponentsList(!userInterfaceContext.isComponentsList());
		if (userInterfaceContext.isComponentsList()) {
			currentModule.restoreBoxes();
		} else {
			currentModule.clearAllBoxes();
		}
		return null;
	}

	public static String performMovePage(RequestService rs, ContentContext ctx, GlobalContext globalContext, ContentService content, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {
		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}
		String pageName = rs.getParameter("page", null);
		String pagePreviousName = rs.getParameter("previous", null);
		if (pageName == null || pagePreviousName == null) {
			return "bad request structure : need 'page' and 'previous' parameters";
		}
		pageName = pageName.replaceFirst("page-", "");
		MenuElement pagePrevious = null;
		if (pagePreviousName.startsWith("page-")) {
			pagePreviousName = pagePreviousName.replaceFirst("page-", "");
			pagePrevious = content.getNavigation(ctx).searchChildFromName(pagePreviousName);
		}
		if (pagePrevious == null && !pagePreviousName.equals("0")) {
			return "previous page not found : " + pagePreviousName;
		}
		MenuElement page = content.getNavigation(ctx).searchChildFromName(pageName);
		if (page == null) {
			return "page not found : " + pageName;
		}
		if (!canModifyCurrentPage(ctx, page) || !checkPageSecurity(ctx, page)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR), false);
			return null;
		}
		if (pagePrevious == null) {
			NavigationHelper.movePage(ctx, page.getParent(), null, page);
		} else if (StringHelper.isTrue(rs.getParameter("as-child", null))) {
			NavigationHelper.movePage(ctx, pagePrevious, null, page);
		} else {
			NavigationHelper.movePage(ctx, pagePrevious.getParent(), pagePrevious, page);
		}
		modifPage(ctx, page);
		if (pagePrevious != null) {
			modifPage(ctx, pagePrevious);
		}
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.setAskStore(true);
		autoPublish(ctx.getRequest(), ctx.getResponse());

		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.moved", new String[][] { { "name", page.getName() } }), GenericMessage.INFO), false);

		if (page.isChildrenOfAssociation()) {
			ctx.setNeedRefresh(true);
		} else {
			if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
				updatePreviewCommands(ctx, null);
			}
		}

		return null;
	}

	public static String performCopyPage(RequestService rs, ContentContext ctx, EditContext editCtx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		editCtx.setPathForCopy(ctx);
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.copy-page", new String[][] { { "name", ctx.getCurrentPage().getName() } }), GenericMessage.INFO), false);
		prepareUpdateInsertLine(ctx);
		return null;
	}

	public static String performDuplicate(RequestService rs, ContentContext ctx, EditContext editCtx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		IContentVisualComponent comp = content.getComponent(ctx, rs.getParameter("id"));

		MenuElement targetPage = NavigationHelper.searchPage(ctx, rs.getParameter("pageCompID"));
		if (targetPage == null) {
			targetPage = ctx.getCurrentPage();
		}

		if (comp == null) {
			logger.warning("component not found : " + rs.getParameter("id"));
			return "component not found : " + rs.getParameter("id");
		} else {
			if (comp instanceof IContainer) {
				IContainer openComponent = ((IContainer) comp).getOpenComponent(ctx);
				IContainer closeComponent = ((IContainer) comp).getCloseComponent(ctx);
				if (openComponent == null || closeComponent == null) {
					logger.severe("no bloc found for container : " + comp.getId() + " (" + comp.getType() + ')');
				} else {
					String previousId = content.createContent(ctx, openComponent.getComponentBean(), closeComponent.getId(), false);
					comp = comp.getNextComponent();
					while (!comp.getId().equals(closeComponent.getId())) {
						previousId = content.createContent(ctx, comp.getComponentBean(), previousId, false);
						comp = comp.getNextComponent();
					}
					String newId = content.createContent(ctx, closeComponent.getComponentBean(), previousId, false);
				}
			} else {
				String newId = content.createContent(ctx, comp.getComponentBean(), comp.getId(), true);
				content.getComponent(ctx, newId);
			}
			if (ctx.isAjax()) {
				String id = rs.getParameter("id");
				ctx.addAjaxZone("comp-" + id, "");
				ctx.addAjaxZone("comp-child-" + id, "");
				ctx.addAjaxInsideZone("insert-line-" + id, "");

				String selecterPrefix = "";
				if (ctx.getCurrentPage().isChildrenAssociation()) {
					if (rs.getParameter("pageCompID", null) != null) {
						selecterPrefix = "#page_" + rs.getParameter("pageCompID", "#ID_NOT_DEFINED") + " #";
					}
					ctx.setCurrentPageCached(targetPage);
				}
				updateArea(ctx, selecterPrefix, comp);
				if (ctx.isEditPreview()) {
					ctx.setClosePopup(true);
				}
			}
			return null;
		}
	}

	public static String performPastePage(RequestService rs, ContentContext ctx, GlobalContext globalContext, EditContext editCtx, ContentService content, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String msg = null;

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR), false);
			return null;
		}

		ContentContext newCtx = editCtx.getContextForCopy(ctx);
		newCtx.setRenderMode(ContentContext.EDIT_MODE);
		newCtx.setRequest(ctx.getRequest());
		newCtx.setResponse(ctx.getResponse());
		newCtx.setArea(null);

		ContentElementList elems = newCtx.getCurrentPage().getContent(newCtx);

		String parentId = rs.getParameter("previous", null);
		if (parentId == null) {
			parentId = "0";
			ctx = ctx.getContextWithArea(null);
		}
		IContentVisualComponent parent = content.getComponent(ctx, parentId);

		int c = 0;
		String latestArea = null;
		while (elems.hasNext(newCtx)) {
			IContentVisualComponent comp = elems.next(newCtx);
			ComponentBean bean = new ComponentBean(comp.getComponentBean());
			if (latestArea != null && !bean.getArea().equals(latestArea)) {
				parentId = "0";
			}
			latestArea = bean.getArea();
			bean.setLanguage(ctx.getRequestContentLanguage());
			// parentId = content.createContent(ctx, bean, parentId, true);

			if (globalContext.isMailingPlatform() || !comp.isMirroredByDefault(ctx) || !globalContext.getSpecialConfig().isPasteAsMirror()) {
				parentId = content.createContent(ctx, ctx.getCurrentPage(), bean, parentId, true);
			} else {
				if (!(comp instanceof IContainer)) {
					IContentVisualComponent targetComp = content.getComponent(ctx, bean.getId());
					if (targetComp != null) {
						ComponentBean mirrorComponentBean = new ComponentBean(MirrorComponent.TYPE, bean.getId(), ctx.getRequestContentLanguage());
						parentId = content.createContent(ctx, ctx.getCurrentPage(), targetComp.getArea(), parentId, mirrorComponentBean, true);
					}
				} else {
					IContentVisualComponent nextComp = comp;
					parentId = content.createContent(ctx, ctx.getCurrentPage(), nextComp.getComponentBean(), parentId, true);
					boolean closeFound = false;
					int depth = 0;
					while (!closeFound) {
						nextComp = ComponentHelper.getNextComponent(nextComp, ctx);
						if (nextComp != null) {
							parentId = content.createContent(ctx, ctx.getCurrentPage(), nextComp.getComponentBean(), parentId, true);
							if (nextComp.getType().equals(comp.getType())) {
								if (((IContainer) nextComp).isOpen(ctx)) {
									depth++;
								} else {
									if (depth == 0) {
										closeFound = true;
									} else {
										depth--;
									}
								}
							}
						} else {
							closeFound = true;
						}
					}
				}
			}

			// parentId = content.createContent(ctx, ctx.getCurrentPage(), bean,
			// parentId, true);
			c++;
		}

		if (parent != null) {
			IContentVisualComponent nextParent = parent.next();
			IContentVisualComponent newParent = content.getComponent(ctx, parentId);
			newParent.setNextComponent(nextParent); // reconnect the list
			if (nextParent != null) {
				nextParent.setPreviousComponent(newParent);
			}
		}
		modifPage(ctx, ctx.getCurrentPage());
		PersistenceService.getInstance(globalContext).setAskStore(true);
		autoPublish(ctx.getRequest(), ctx.getResponse());

		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.paste-page", new String[][] { { "count", "" + c } }), GenericMessage.INFO), false);

		return msg;
	}

	public static String performCopy(RequestService rs, ContentContext ctx, EditContext editCtx, ContentService content, ClipBoard clipBoard, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String compId = rs.getParameter("id", null);
		if (compId == null) {
			return "bad request structure : need 'id'.";
		}
		IContentVisualComponent comp = content.getComponent(ctx, compId);
		if (comp == null) {
			return "component not found : " + compId;
		} else {
			clipBoard.copy(ctx, new ComponentBean(comp.getComponentBean()), comp.getIcon());
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.copy", new String[][] { { "type", "" + comp.getType() } }), GenericMessage.INFO), false);
			prepareUpdateInsertLine(ctx);
		}

		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			updatePreviewCommands(ctx, null);
		}

		return null;
	}

	public static String performPasteComp(RequestService rs, ContentContext ctx, ContentService content, EditContext editContext, ClipBoard clipboard, Module currentModule, PersistenceService persistenceService, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR), false);
			return null;
		}

		String previous = rs.getParameter("previous", null);
		if (previous == null) {
			return "bad request structure : need 'previous' parameter.";
		}
		ComponentBean comp = clipboard.getCopiedComponent(ctx);
		if (comp == null) {
			return "nothing to paste.";
		}

		MenuElement targetPage = NavigationHelper.searchPage(ctx, rs.getParameter("pageContainerID", null));
		if (targetPage == null) {
			targetPage = ctx.getCurrentPage();
		}
		if (!ctx.isEditPreview()) {
			ctx.setArea(editContext.getCurrentArea());
			comp.resetArea(); // paste in current area
		}
		comp.setLanguage(null);

		String newId = content.createContent(ctx, targetPage, comp, previous, true);
		IContentVisualComponent newComp = content.getComponent(ctx, newId);
		if (newComp != null) {
			newComp.setRepeat(false);
		}
		if (ctx.isAjax()) {
			ctx.getRequest().setAttribute(AbstractVisualComponent.SCROLL_TO_COMP_ID_ATTRIBUTE_NAME, newId);
			updateComponent(ctx, currentModule, newId, previous);
		}

		String msg = i18nAccess.getText("action.component.created", new String[][] { { "type", comp.getType() } });
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.INFO), false);

		persistenceService.setAskStore(true);
		modifPage(ctx, ctx.getCurrentPage());
		autoPublish(ctx.getRequest(), ctx.getResponse());

		return null;
	}

	public static String performPasteCompAsPage(RequestService rs, ContentContext ctx, ContentService content, EditContext editContext, ClipBoard clipboard, Module currentModule, PersistenceService persistenceService, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR), false);
			return null;
		}

		String previous = rs.getParameter("previous", null);
		if (previous == null) {
			return "bad request structure : need 'previous' parameter.";
		}
		ComponentBean comp = clipboard.getCopiedComponent(ctx);
		if (comp == null) {
			return "nothing to paste.";
		}

		MenuElement targetPage = NavigationHelper.searchPage(ctx, rs.getParameter("pageContainerID", null));
		if (targetPage == null) {
			targetPage = ctx.getCurrentPage();
		}
		if (!ctx.isEditPreview()) {
			ctx.setArea(editContext.getCurrentArea());
			comp.resetArea(); // paste in current area
		}
		comp.setLanguage(null);

		String newId = content.createContent(ctx, targetPage, comp, previous, true);
		if (ctx.isAjax()) {
			ctx.getRequest().setAttribute(AbstractVisualComponent.SCROLL_TO_COMP_ID_ATTRIBUTE_NAME, newId);
			updateComponent(ctx, currentModule, newId, previous);
		}

		String msg = i18nAccess.getText("action.component.created", new String[][] { { "type", comp.getType() } });
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.INFO), false);

		persistenceService.setAskStore(true);
		modifPage(ctx, ctx.getCurrentPage());
		autoPublish(ctx.getRequest(), ctx.getResponse());

		return null;
	}

	public static String performMoveComponent(RequestService rs, ContentContext ctx, ContentService content, ClipBoard clipboard, Module currentModule, PersistenceService persistenceService, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR), false);
			return null;
		}

		String previous = rs.getParameter("previous", null);
		String compId = rs.getParameter("comp-id", null);
		String areaId = rs.getParameter("area", null);

		// only move in one area
		final String KEY = "component-moved-" + compId;
		if (ctx.getRequest().getAttribute(KEY) != null) {
			return null;
		} else {
			ctx.getRequest().setAttribute(KEY, KEY);
		}

		if (previous == null || compId == null || areaId == null) {
			return "bad request structure : need 'previous', 'comp-id' and 'area' as parameters.";
		}
		IContentVisualComponent comp = content.getComponent(ctx, compId);
		IContentVisualComponent newPrevious = content.getComponent(ctx, previous);

		MenuElement fromPage = ctx.getCurrentPage();
		if (ctx.getCurrentPage().isChildrenAssociation()) {
			fromPage = comp.getPage();
		}

		if (comp == null) {
			return "component not found : " + compId;
		}

		String fromArea = comp.getArea();

		String area = null;
		for (Map.Entry<String, String> templateArea : ctx.getCurrentTemplate().getAreasMap().entrySet()) {
			if (templateArea.getValue().equals(areaId)) {
				area = templateArea.getKey();
			}
		}

		MenuElement targetPage = NavigationHelper.searchPage(ctx, rs.getParameter("pageContainerID", null));
		if (targetPage == null) {
			targetPage = ctx.getCurrentPage();
		}

		ComponentHelper.smartMoveComponent(ctx, comp, newPrevious, targetPage, area);

		if (ctx.isAjax()) {
			// updatePreviewComponent(ctx, currentModule, comp.getId(),
			// previous);

			ctx.getRequest().setAttribute(AbstractVisualComponent.SCROLL_TO_COMP_ID_ATTRIBUTE_NAME, compId);

			MenuElement parentPage = ctx.getCurrentPage();

			List<IContentVisualComponent> areaToBeUpdated = new LinkedList<IContentVisualComponent>();
			areaToBeUpdated.add(comp);
			if (newPrevious != null) {
				areaToBeUpdated.add(newPrevious);
			}

			Collection<String> areas = new LinkedList<String>();
			Map<String, String> areaMap = ctx.getCurrentTemplate().getAreasMap();
			for (Map.Entry<String, String> aID : areaMap.entrySet()) {
				areas.add(aID.getKey());
			}
			for (IContentVisualComponent compToBeUpdated : areaToBeUpdated) {
				String selecterPrefix = "";
				if (parentPage.isChildrenAssociation()) {
					selecterPrefix = "#page_" + compToBeUpdated.getPage().getId() + " #";
					ctx.setCurrentPageCached(compToBeUpdated.getPage());
				}
				ctx.setCurrentPageCached(compToBeUpdated.getPage());
				updateArea(ctx, selecterPrefix, compToBeUpdated);
			}
			String selecterPrefix = "";
			if (parentPage.isChildrenAssociation()) {
				selecterPrefix = "#page_" + rs.getParameter("pageContainerID", "#ID_NOT_DEFINED") + " #";

				if (targetPage != null) {
					ctx.setCurrentPageCached(targetPage);
				}
			}

			String mode = rs.getParameter("render-mode", null);
			if (mode != null) {
				ctx.setRenderMode(Integer.parseInt(mode));
			}
			ctx.getAjaxInsideZone().put(selecterPrefix + areaMap.get(areaId), ComponentHelper.renderArea(ctx, areaId));
			ctx.getAjaxInsideZone().put(selecterPrefix + areaMap.get(fromArea), ComponentHelper.renderArea(ctx, fromArea));

			ctx.setCurrentPageCached(fromPage);
			String fromPageSelector = "";
			if (parentPage.isChildrenAssociation()) {
				fromPageSelector = "#page_" + fromPage.getId() + " #";
				ctx.setCurrentPageCached(fromPage);
			}
			String fromAreaId = TemplateFactory.getTemplate(ctx, fromPage).getAreasMap().get(fromArea);
			ctx.getAjaxInsideZone().put(fromPageSelector + fromAreaId, ComponentHelper.renderArea(ctx, fromArea));

			ctx.setCurrentPageCached(parentPage);
		}

		String msg = i18nAccess.getText("action.component.moved", new String[][] { { "type", comp.getType() } });
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.INFO), false);

		persistenceService.setAskStore(true);
		modifPage(ctx, ctx.getCurrentPage());
		autoPublish(ctx.getRequest(), ctx.getResponse());

		if (ctx.isAjax()) {
			updatePreviewCommands(ctx, null);
		}

		return null;
	}

	public static String performClearClipboard(ClipBoard clipboard, ContentContext ctx, EditContext editCtx) throws Exception {
		editCtx.setPathForCopy(null);
		clipboard.clear();
		if (ctx.isAjax()) {
			ctx.getAjaxZone().put("paste", "");
		}
		return null;
	}

	public static String performInsertPage(RequestService rs, ContentContext ctx, MessageRepository messageRepository, ContentService content, EditContext editContext, PersistenceService persistenceService, I18nAccess i18nAccess) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		String path = editContext.getContextForCopy(ctx).getPath();
		MenuElement pageToBeMoved = content.getNavigation(ctx).searchChild(ctx, path);
		if (pageToBeMoved == null) {
			return "page not found : " + path;
		}
		if (pageToBeMoved.getParent() != null) {
			if (pageToBeMoved.getId().equals(ctx.getCurrentPage().getId())) {
				return "you can't paste a page a page on him self.";
			}

			pageToBeMoved.moveToParent(ctx.getCurrentPage());
			pageToBeMoved.setPriority(1);
			persistenceService.setAskStore(true);
			editContext.setPathForCopy(null);
			String[][] balises = { { "path", path }, { "new-path", pageToBeMoved.getPath() } };
			String msg = i18nAccess.getText("navigation.move", balises);
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
			modifPage(ctx, ctx.getCurrentPage());
		}
		return null;
	}

	public static String performConfirmReplace(RequestService rs, ContentContext ctx, GlobalContext globalContext, EditContext editCtx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		if (!ResourceStatus.isResource(session)) {
			return null;
		}
		if (rs.getParameter("cancel", null) != null) {
			ResourceStatus.getInstance(session).release(ctx);
		} else if (rs.getParameter("confirm", null) != null) {
			ResourceStatus resourceStatus = ResourceStatus.getInstance(session);
			if (resourceStatus.getSource().getId().equals(rs.getParameter("source", "-")) && resourceStatus.getTarget().getId().equals(rs.getParameter("target", "-"))) {
				FileCache.getInstance(session.getServletContext()).deleteAllFile(globalContext.getContextKey(), resourceStatus.getTarget().getFile().getName());
				resourceStatus.getTarget().getFile().delete();
				ResourceHelper.writeFileToFile(resourceStatus.getSource().getFile(), resourceStatus.getTarget().getFile());
				resourceStatus.release(ctx);
			} else {
				return "error : bad file hash.";
			}
		}
		if (editCtx.isPreviewEditionMode()) {
			ctx.setClosePopup(true);
		}
		return null;
	}

	public static String performInsertShared(RequestService rs, ContentContext ctx, GlobalContext globalContext, EditContext editContext, ContentService content, SharedContentService sharedContentService, Module currentModule, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		String sharedData = rs.getParameter("sharedContent", null);
		String previousId = rs.getParameter("previous", null);

		if (sharedData == null || previousId == null) {
			return "bad request structure, need sharedData and previousId as parameter.";
		} else {
			// sharedContentService.clearCache();
			SharedContent sharedContent = sharedContentService.getSharedContent(ctx, sharedData);
			if (sharedContent == null) {
				String activeProvider = StringHelper.collectionToString(sharedContentService.getAllActiveProvider(ctx));
				String msg = "error : shared content not found : " + sharedData;
				logger.warning(msg + " activeProvider : " + activeProvider);
				return msg;
			}
			String areaKey = null;
			String area = rs.getParameter("area", null);
			if (area != null) {
				for (Map.Entry<String, String> areaId : ctx.getCurrentTemplate().getAreasMap().entrySet()) {
					if (areaId.getValue().equals(area)) {
						areaKey = areaId.getKey();
					}
				}
				if (areaKey == null) {
					return "area not found : " + area;
				}
				editContext.setCurrentArea(areaKey);
			}
			sharedContent.loadContent(ctx);

			MenuElement parentPage = ctx.getCurrentPage();
			MenuElement targetPage = NavigationHelper.searchPage(ctx, rs.getParameter("pageContainerID", null));
			if (targetPage == null) {
				targetPage = ctx.getCurrentPage();
			}
			ComponentHelper.updateNextAndPrevious(ctx, targetPage, area);

			if (sharedContent.getLinkInfo() == null) {
				if (sharedContent.getContent() != null) {
					List<ComponentBean> beans = new LinkedList<ComponentBean>(sharedContent.getContent());
					for (ComponentBean componentBean : beans) {
						componentBean.setArea(areaKey);
					}
					String newId = content.createContent(ctx, targetPage, beans, previousId, true);
					ctx.getRequest().setAttribute(AbstractVisualComponent.SCROLL_TO_COMP_ID_ATTRIBUTE_NAME, newId);
				} else {
					logger.warning("content not found : "+sharedContent.getTitle());
				}
			} else {
				ComponentBean mirrorBean = new ComponentBean(PageMirrorComponent.TYPE, sharedContent.getLinkInfo(), ctx.getRequestContentLanguage());
				mirrorBean.setArea(areaKey);
				mirrorBean.setAuthors(ctx.getCurrentUserId());
				content.createContent(ctx, targetPage, mirrorBean, previousId, true);
			}

			PersistenceService.getInstance(globalContext).setAskStore(true);

			if (ctx.isAjax()) {
				// updateComponent(ctx, currentModule, newId, previousId);
				String selecterPrefix = "";
				if (parentPage.isChildrenAssociation()) {
					selecterPrefix = "#page_" + rs.getParameter("pageContainerID", "#ID_NOT_DEFINED") + " #";
					if (targetPage != null) {
						ctx.setCurrentPageCached(targetPage);
					}
				}
				String mode = rs.getParameter("render-mode", null);
				if (mode != null) {
					ctx.setRenderMode(Integer.parseInt(mode));
				}

				ctx.getAjaxInsideZone().put(selecterPrefix + area, ComponentHelper.renderArea(ctx, areaKey));
				modifPage(ctx, targetPage);
				logger.info("update area : " + selecterPrefix + area);
			}
		}
		return null;

	}

	public static String performClosepopup(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		ctx.setParentURL(rs.getParameter("url", null));
		ctx.setClosePopup(true);
		return null;
	}

	public static String performRefresh(ContentContext ctx) throws Exception {
		// SynchroHelper.performSynchro(ctx);
		if (ctx.getCurrentPage() != null) {
			ctx.getCurrentPage().releaseCache();
		}
		return null;
	}
}
