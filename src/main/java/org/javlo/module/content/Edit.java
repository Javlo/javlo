package org.javlo.module.content;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentContext;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentComponentsList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.component.core.IUploadResource;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.component.links.MirrorComponent;
import org.javlo.component.links.PageMirrorComponent;
import org.javlo.component.title.Title;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.ContextException;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.UserInterfaceContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.DebugHelper;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.LangHelper;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.Module.Box;
import org.javlo.module.core.ModulesContext;
import org.javlo.module.file.FileModuleContext;
import org.javlo.module.mailing.MailingModuleContext;
import org.javlo.navigation.IURLFactory;
import org.javlo.navigation.MenuElement;
import org.javlo.search.SearchResult;
import org.javlo.search.SearchResult.SearchElement;
import org.javlo.service.ClipBoard;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;
import org.javlo.service.PublishListener;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.service.resource.ResourceStatus;
import org.javlo.service.shared.ISharedContentProvider;
import org.javlo.service.shared.JavloSharedContentProvider;
import org.javlo.service.shared.SharedContent;
import org.javlo.service.shared.SharedContentService;
import org.javlo.service.syncro.SynchroHelper;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.ztatic.FileCache;

public class Edit extends AbstractModuleAction {

	public static String CONTENT_RENDERER = "/jsp/view/content_view.jsp";

	private static Logger logger = Logger.getLogger(Edit.class.getName());

	private static void prepareUpdateInsertLine(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editContext = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		ClipBoard clipBoard = ClipBoard.getInstance(ctx.getRequest());

		ctx = ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE);

		IContentVisualComponent currentTypeComponent = ComponentFactory.getComponentWithType(ctx, editContext.getActiveType());

		String typeName = StringHelper.getFirstNotNull(currentTypeComponent.getComponentLabel(ctx, globalContext.getEditLanguage(ctx.getRequest().getSession())), i18nAccess.getText("content." + currentTypeComponent.getType()));
		String insertHere = i18nAccess.getText("content.insert-here", new String[][] { { "type", typeName } });

		String pastePageHere = null;
		if (editContext.getContextForCopy(ctx) != null) {
			pastePageHere = i18nAccess.getText("content.paste-here", new String[][] { { "page", editContext.getContextForCopy(ctx).getCurrentPage().getName() } });
		}

		String pasteHere = null;
		if (clipBoard.getCopiedComponent(ctx) != null) {
			pasteHere = i18nAccess.getText("content.paste-comp", new String[][] { { "type", clipBoard.getCopiedComponent(ctx).getType() } });
		}

		String previewParam = "";
		if (ctx.isEditPreview()) {
			previewParam = "previewEdit=true&";
		}

		String insertXHTML = "<a class=\"action-button ajax\" href=\"" + URLHelper.createURL(ctx) + "?" + previewParam + "webaction=insert&previous=0&type=" + currentTypeComponent.getType() + "\">" + insertHere + "</a>";
		if (pastePageHere != null) {
			insertXHTML = insertXHTML + "<a class=\"action-button\" href=\"" + URLHelper.createURL(ctx) + "?" + previewParam + "webaction=pastePage&previous=0\">" + pastePageHere + "</a>";
		}
		if (pasteHere != null) {
			insertXHTML = insertXHTML + "<a class=\"action-button ajax\" href=\"" + URLHelper.createURL(ctx) + "?" + previewParam + "webaction=pasteComp&previous=0\">" + pasteHere + "</a>";
		}
		ctx.addAjaxInsideZone("insert-line-0", insertXHTML);

		ContentContext areaCtx = ctx.getContextWithArea(null);

		IContentComponentsList elems = ctx.getCurrentPage().getContent(areaCtx);
		while (elems.hasNext(areaCtx)) {
			IContentVisualComponent comp = elems.next(areaCtx);
			insertXHTML = "<a class=\"action-button ajax\" href=\"" + URLHelper.createURL(ctx) + "?" + previewParam + "webaction=insert&previous=" + comp.getId() + "&type=" + currentTypeComponent.getType() + "\">" + insertHere + "</a>";
			if (pastePageHere != null) {
				insertXHTML = insertXHTML + "<a class=\"action-button\" href=\"" + URLHelper.createURL(ctx) + "?" + previewParam + "webaction=pastePage&previous=" + comp.getId() + "\">" + pastePageHere + "</a>";
			}
			if (pasteHere != null) {
				insertXHTML = insertXHTML + "<a class=\"action-button ajax\" href=\"" + URLHelper.createURL(ctx) + "?" + previewParam + "webaction=pasteComp&previous=" + comp.getId() + "\">" + pasteHere + "</a>";
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
	public static void updateComponent(ContentContext ctx, Module currentModule, String newId, String previousId) throws Exception {
		ComponentContext compCtx = ComponentContext.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService content = ContentService.getInstance(globalContext);
		compCtx.addNewComponent(content.getComponent(ctx, newId)); // prepare
																	// ajax
																	// rendering
		String componentRenderer = URLHelper.mergePath(currentModule.getPath() + "/jsp/content.jsp");
		String newComponentXHTML = ServletHelper.executeJSP(ctx, componentRenderer);
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
	 * @param currentModule
	 * @param newId
	 *            the id of the component
	 * @param previousId
	 *            the id, null for update and previous component for insert.
	 * @throws Exception
	 */
	public static void updatePreviewCommands(ContentContext ctx, String tab) throws Exception {
		ctx.getRequest().setAttribute("editPreview", ctx.isEditPreview());
		ctx.getRequest().setAttribute("components", ComponentFactory.getComponentForDisplay(ctx));
		SharedContentService.prepare(ctx);
		String updateURL = ctx.getGlobalContext().getStaticConfig().getPreviewCommandFilePath();
		if (tab != null) {
			updateURL = URLHelper.addParam(updateURL, "_preview_tab", tab);
		}
		String previewCommandsXHTML = ServletHelper.executeJSP(ctx, updateURL);
		ctx.addAjaxZone("preview_command", previewCommandsXHTML);
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
	private static void updatePreviewComponent(ContentContext ctx, Module currentModule, String newId, String previousId) throws Exception {
		ComponentContext compCtx = ComponentContext.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService content = ContentService.getInstance(globalContext);
		IContentVisualComponent comp = content.getComponent(ctx, newId);
		compCtx.addNewComponent(comp); // prepare ajax rendering
		ctx.getRequest().setAttribute("specific-comp", comp);
		String componentRenderer = "/jsp/view/content_view.jsp";
		int mode = ctx.getRenderMode();
		ctx.setRenderMode(ContentContext.PREVIEW_MODE);
		String newComponentXHTML = ServletHelper.executeJSP(ctx, componentRenderer);
		ctx.setRenderMode(mode);
		compCtx.clearComponents();
		ctx.addAjaxZone("cp_" + newId, newComponentXHTML);
	}

	private static boolean nameExist(String name, ContentContext ctx, ContentService content) throws Exception {
		MenuElement page = content.getNavigation(ctx);
		return (page.searchChildFromName(name) != null);
	}

	@Override
	public AbstractModuleContext getModuleContext(HttpSession session, Module module) throws Exception {
		return FileModuleContext.getInstance(session, GlobalContext.getSessionInstance(session), module, ContentModuleContext.class);
	}

	public static class ComponentWrapper {
		private ContentContext ctx;
		private IContentVisualComponent comp;
		private boolean selected;
		private String hexColor;

		public ComponentWrapper(ContentContext ctx, IContentVisualComponent comp) {
			this.ctx = ctx;
			this.comp = comp;
			hexColor = comp.getHexColor();
		}

		public String getType() {
			return comp.getType();
		}

		public String getLabel() {
			return comp.getComponentLabel(ctx, ctx.getGlobalContext().getEditLanguage(ctx.getRequest().getSession()));
		}

		public String getValue() {
			return comp.getValue(ctx);
		}

		public boolean isMetaTitle() {
			return comp.isMetaTitle();
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		public int getComplexityLevel() {
			return comp.getComplexityLevel(ctx);
		}

		public boolean isDynamicComponent() {
			return comp instanceof DynamicComponent;
		}

		public String getHexColor() {
			return hexColor;
		}

		public void setHexColor(String hexColor) {
			this.hexColor = hexColor;
		}

		public IContentVisualComponent getComponent() {
			return comp;
		}

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
		AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		if (adminUserFactory.getCurrentUser(ctx.getRequest().getSession()) == null) {
			return false;
		}
		ContentService.getInstance(globalContext);
		if (page.getEditorRoles().size() > 0) {
			if (!adminUserSecurity.haveRight(adminUserFactory.getCurrentUser(ctx.getRequest().getSession()), AdminUserSecurity.FULL_CONTROL_ROLE)) {
				if (!adminUserFactory.getCurrentUser(ctx.getRequest().getSession()).validForRoles(page.getEditorRoles())) {
					MessageRepository messageRepository = MessageRepository.getInstance(ctx);
					I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.security.noright-onpage"), GenericMessage.ERROR));
					return false;
				}
			}
		}
		return true;
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
		currentPage.setModificationDate(new Date());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService.getInstance(globalContext);
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		currentPage.setLatestEditor(editCtx.getUserPrincipal().getName());
		currentPage.setValid(false);
		currentPage.releaseCache();
	}

	private static void loadComponentList(ContentContext ctx) throws Exception {
		Collection<Edit.ComponentWrapper> comps = ComponentFactory.getComponentForDisplay(ctx);
		/*
		 * for (IContentComponentsList iContentComponentsList : comps) {
		 * System.out
		 * .println("***** Edit.loadComponentList : iContentComponentsList = "
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

		if (currentPage.isBlocked()) {
			if (!currentPage.getBlocker().equals(adminUserFactory.getCurrentUser(ctx.getRequest().getSession()).getName())) {
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
			if (!page.getBlocker().equals(adminUserFactory.getCurrentUser(ctx.getRequest().getSession()).getName())) {
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

		if (modulesContext.searchModule("shared-content") != null) {
			ctx.getRequest().setAttribute("sharedContent", "true");
		}

		if (ResourceStatus.isInstance(ctx.getRequest().getSession())) {
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

				request.setAttribute("previewURL", URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE)));

				switch (modCtx.getMode()) {
				case ContentModuleContext.PREVIEW_MODE:
					currentModule.setToolsRenderer("/jsp/actions.jsp?button_edit=true&button_page=true" + publish);
					currentModule.setRenderer("/jsp/preview.jsp");
					currentModule.setBreadcrumbTitle(I18nAccess.getInstance(ctx.getRequest()).getText("content.preview"));
					break;
				case ContentModuleContext.PAGE_MODE:
					currentModule.setToolsRenderer("/jsp/actions.jsp?button_edit=true&button_preview=true&button_delete_page=true" + publish);
					request.setAttribute("page", ctx.getCurrentPage().getPageBean(ctx));
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
			Set<String> roleSet = new HashSet<String>();
			if (ctx.getCurrentEditUser() != null) {
				for (String role : globalContext.getAdminUserRoles()) {
					roleSet.clear();
					roleSet.add(role);
					if (ctx.getCurrentEditUser().validForRoles(roleSet)) {
						roles.add(role);
					}
				}
				Collections.sort(roles);
				ctx.getRequest().setAttribute("adminRoles", roles);
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

		/** COMPONENT LIST **/
		loadComponentList(ctx);

		/** CONTENT **/
		/*
		 * ComponentContext compCtx = ComponentContext.getInstance(request);
		 * IContentComponentsList elems = ctx.getCurrentPage().getContent(ctx);
		 * if (compCtx.getNewComponents().length == 0) { while
		 * (elems.hasNext(ctx)) { compCtx.addNewComponent(elems.next(ctx)); } }
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
		if (ctx.getCurrentPage().getParent() != null) {
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
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.no-content"), GenericMessage.ERROR));
		}

		if (ctx.getCurrentTemplate() == null) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage("no template found on this page.", GenericMessage.ALERT));
			return null;
		}

		/** check area **/
		String badArea = "";
		String sep = "";
		ComponentBean[] content = ctx.getCurrentPage().getContent();
		for (ComponentBean componentBean : content) {
			if (componentBean != null) {
				Collection<String> areas = ctx.getCurrentTemplate().getAreas();
				if (!areas.contains(componentBean.getArea())) {
					if (badArea != null && componentBean.getArea() != null && !badArea.contains(componentBean.getArea())) {
						badArea = badArea + sep + componentBean.getArea();
						sep = ",";
					}
				}
			}
		}
		if (badArea.length() > 0) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			ContentContext absCtx = ctx.getContextForAbsoluteURL();
			String url = URLHelper.createURL(absCtx);
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.bad-area") + " \"" + badArea + "\"", GenericMessage.ALERT, url));
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

	public static final String performInsert(HttpServletRequest request, HttpServletResponse response, RequestService rs, GlobalContext globalContext, HttpSession session, EditContext editContext, ContentContext ctx, ContentService content, Module currentModule, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return i18nAccess.getText("action.block");
		}
		String previousId = rs.getParameter("previous", null);

		String type = request.getParameter("type");

		if (previousId == null || type == null) {
			return "bad insert request need previousId and component type.";
		}

		String areaKey = null;
		String area = rs.getParameter("area", null);

		MenuElement parentPage = ctx.getCurrentPage();
		MenuElement targetPage = content.getNavigation(ctx).searchChildFromId(rs.getParameter("pageContainerID", null));

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
				return "area not found : " + area;
			}
			// ctx = ctx.getContextWithArea(areaKey);
			editContext.setCurrentArea(areaKey);
		}

		if (areaKey == null) {
			areaKey = EditContext.getInstance(globalContext, session).getCurrentArea();
		}

		String newId;
		if (type.equals("clipboard")) {
			ClipBoard cb = ClipBoard.getInstance(ctx.getRequest());
			Object copied = cb.getCopied();
			if (copied == null || !(copied instanceof ComponentBean)) {
				return "error no item in clipBoard";
			} else {
				ComponentBean bean = (ComponentBean) copied;
				if (globalContext.isMailingPlatform()) {
					newId = content.createContent(ctx, targetPage, areaKey, previousId, bean, true);
				} else {
					ComponentBean mirrorComponentBean = new ComponentBean(MirrorComponent.TYPE, bean.getId(), ctx.getRequestContentLanguage());
					newId = content.createContent(ctx, targetPage, areaKey, previousId, mirrorComponentBean, true);
				}
			}
		} else {
			newId = content.createContent(ctx, targetPage, areaKey, previousId, type, "", true);
		}

		IContentVisualComponent comp = content.getComponent(ctx, newId);
		comp.setPreviousComponent(ComponentHelper.getPreviousComponent(comp, ctx));
		comp.setNextComponent(ComponentHelper.getNextComponent(comp, ctx));
		if (!type.equals("clipboard") && StringHelper.isTrue(rs.getParameter("init", null))) {
			comp.initContent(ctx);
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
			ctx.getAjaxInsideZone().put(selecterPrefix + area, ServletHelper.executeJSP(ctx, "/jsp/view/content_view.jsp?area=" + areaKey));
		}
		ctx.resetCurrentPageCached();
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);
		modifPage(ctx, targetPage);
		autoPublish(request, response);

		return null;
	}

	public static final String performDelete(ContentContext ctx, HttpServletRequest request, ContentService content, EditContext editContext, HttpServletResponse response, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
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

			MenuElement targetPage = content.getNavigation(ctx).searchChildFromId(request.getParameter("pageCompID"));
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

			ClipBoard clipBoard = ClipBoard.getInstance(request);
			if (id.equals(clipBoard.getCopied())) {
				clipBoard.clear();
			}

			if (comp.getPreviousComponent() != null) {
				ctx.getRequest().setAttribute(AbstractVisualComponent.SCROLL_TO_COMP_ID_ATTRIBUTE_NAME, comp.getPreviousComponent().getId());
			}
			targetPage.removeContent(ctx, id);
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

				ctx.getAjaxInsideZone().put(selecterPrefix + comp.getArea(), ServletHelper.executeJSP(ctx, "/jsp/view/content_view.jsp?area=" + comp.getArea()));
				if (ctx.isEditPreview()) {
					ctx.setClosePopup(true);
				}
			}

			ReverseLinkService.getInstance(globalContext).clearCache();

			modifPage(ctx, targetPage);
			autoPublish(request, response);

		}
		return null;
	}
	
	public static final String performSave(ContentContext ctx, EditContext editContext, GlobalContext globalContext, ContentService content, ComponentContext componentContext, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository, Module currentModule, AdminUserFactory adminUserFactory) throws Exception {
		return performModifComponent(ctx, editContext, globalContext, content, componentContext, requestService, i18nAccess, messageRepository, currentModule, adminUserFactory, false);
	}
	
	public static final String performUpload(ContentContext ctx, EditContext editContext, GlobalContext globalContext, ContentService content, ComponentContext componentContext, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository, Module currentModule, AdminUserFactory adminUserFactory) throws Exception {		
		return performModifComponent(ctx, editContext, globalContext, content, componentContext, requestService, i18nAccess, messageRepository, currentModule, adminUserFactory, true);
	}

	private static final String performModifComponent(ContentContext ctx, EditContext editContext, GlobalContext globalContext, ContentService content, ComponentContext componentContext, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository, Module currentModule, AdminUserFactory adminUserFactory, boolean upload) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		org.javlo.helper.Logger.forceStartCount("save");

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
						elem.performEdit(ctx);
					} else {
						if (elem instanceof IUploadResource) {
							((IUploadResource)elem).performUpload(ctx);
						} else {
							logger.warning("you can't upload file in "+elem.getType());
							return "you can't upload file in "+elem.getType();
						}
					}
					if (ctx.isEditPreview()) {
						componentContext.addNewComponent(elem);
					}
					if (!elem.isModify()) { // if elem not modified check
											// modification via rawvalue
						String rawValue = requestService.getParameter("raw_value_" + elem.getId(), null);
						if (rawValue != null && !rawValue.equals(elem.getValue(ctx))) {
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
			// NavigationService navigationService =
			// NavigationService.getInstance(globalContext);
			// navigationService.clearPage(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.updated"), GenericMessage.INFO));
			autoPublish(ctx.getRequest(), ctx.getResponse());
		} else {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.not-updated"), GenericMessage.ALERT));
		}

		if (requestService.getParameter("save", null) != null && editContext.isEditPreview() && !ResourceStatus.isInstance(ctx.getRequest().getSession()) && requestService.getParameter("upload", null) == null) {
			ctx.setParentURL(URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE)));
			ctx.setClosePopup(true);
		}

		if (ctx.isEditPreview() && componentContext.getNewComponents() != null && componentContext.getNewComponents().size() == 1) {
			InfoBean.getCurrentInfoBean(ctx).setTools(false);
			ctx.getRequest().setAttribute("noinsert", "true");
		}

		return message;
	}

	public static final String performChangeMode(HttpSession session, RequestService requestService, ContentModuleContext modCtx) {
		modCtx.setMode(Integer.parseInt(requestService.getParameter("mode", "" + ContentModuleContext.EDIT_MODE)));
		return null;
	}

	public static final String performPageProperties(ServletContext application, GlobalContext globalContext, ContentContext ctx, ContentService content, EditContext editCtx, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		boolean isView = requestService.getParameter("view", null) != null;

		String pageName = requestService.getParameter("name", null);
		String newName = requestService.getParameter("new_name", null);
		if (pageName == null || newName == null) {
			return "bad parameter for change page properties.";
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
					page.setName(newName);
					modify = true;
				}
			}

			if (page.isVisible() != isView) {
				page.setVisible(isView);
				modify = true;
			}

			if (requestService.getParameter("shorturl", null) != null) {
				if (!page.isShortURL()) {
					page.getShortURL(ctx); // create short url
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.shorturl"), GenericMessage.ALERT));
				} else {
					return "this page have allready short url.";
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

			/** change notifications **/
			boolean changeNotification = StringHelper.isTrue(requestService.getParameter("changeNotification", null));
			if (postCheck && page.isChangeNotification() != changeNotification) {
				page.setChangeNotification(changeNotification);
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
			for (String role : infoBean.getAdminRoles()) {
				if (requestService.getParameter("admin-" + role, null) != null) {
					page.addEditorRoles(role);
				}
			}

			/** page type **/
			String pageType = requestService.getParameter("page_type", null);
			if (pageType != null) {
				page.setType(pageType);
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
					if (template != null && ctx.getCurrentTemplates().contains(template)) { // TODO:
																							// check
																							// this
																							// test
						page.setTemplateName(template.getName());
						modify = true;
					} else {
						return "template not found : " + templateName;
					}
				} else {
					page.setTemplateName(null); // inherited
				}
				ctx.setCurrentTemplate(null); // reset current template
			}
			if (errorMessage != null) {
				messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(errorMessage, GenericMessage.ERROR));
			} else {
				if (modify) {
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("message.update-page-properties"), GenericMessage.INFO));
				}
			}

			page.clearPageBean(ctx);
			PersistenceService.getInstance(globalContext).setAskStore(true);

			if (ctx.isEditPreview()) {
				String url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), page.getPath());
				if (page.getChildMenuElements().size() > 0 && page.getChildMenuElements().iterator().next().isChildrenAssociation()) {
					url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), page.getChildMenuElements().iterator().next().getPath());
				}
				ctx.setParentURL(url);
				ctx.setClosePopup(true);
			}

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
				newURL = URLHelper.addParam(newURL, ElementaryURLHelper.BACK_PARAM_NAME, requestService.getParameter(ElementaryURLHelper.BACK_PARAM_NAME, null));
			}
			newURL = messageRepository.forwardMessage(newURL);
			ctx.sendRedirect(newURL);
		} else {
			return "bad request structure : 'language' not found.";
		}
		return null;
	}

	public static String performAddPage(RequestService requestService, ContentContext ctx, ContentService content) {

		String message = null;

		try {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());

			String path = ctx.getPath();
			String nodeName = requestService.getParameter("name", null);
			String parentName = requestService.getParameter("parent", null);

			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());
			if (nodeName != null) {
				message = validNodeName(nodeName, i18nAccess);
			}

			if (nodeName != null && nameExist(nodeName, ctx, content)) {
				message = i18nAccess.getText("action.validation.name-allready-exist", new String[][] { { "name", nodeName } });
			}

			if (message == null) {
				MenuElement elem = MenuElement.getInstance(globalContext);

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
				if (needInitContent) {
					List<ComponentBean> initContent = new LinkedList<ComponentBean>();
					for (String lg : globalContext.getContentLanguages()) {
						i18nAccess.requestInit(ctx);
						initContent.add(new ComponentBean("", Title.TYPE, elem.getName(), lg, false, ctx.getCurrentEditUser()));
					}
					content.createContent(ctx, elem, initContent, "0", false);
				}

				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				persistenceService.store(ctx);
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

	public static String performPublish(ServletContext application, HttpServletRequest request, StaticConfig staticConfig, GlobalContext globalContext, ContentService content, ContentContext ctx, I18nAccess i18nAccess) throws Exception {
		synchronized (content.getNavigation(ctx).getLock()) {

			DebugHelper.writeInfo(System.out);

			String message = null;

			synchronized (globalContext.getLockLoadContent()) {

				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);

				if (!globalContext.isPortail()) {
					persistenceService.publishPreviewFile(ctx);
				} else {
					ContentContext viewCtx = new ContentContext(ctx);
					viewCtx.setRenderMode(ContentContext.VIEW_MODE);

					MenuElement viewNav = content.getNavigation(viewCtx);
					NavigationHelper.publishNavigation(ctx, content.getNavigation(ctx), viewNav);
					persistenceService.store(viewCtx, ContentContext.VIEW_MODE);
				}

				globalContext.setPublishDate(new Date());
				globalContext.setLatestPublisher(ctx.getCurrentEditUser().getLogin());

				content.releaseViewNav(ctx, globalContext);

				String msg = i18nAccess.getText("content.published");
				MessageRepository.getInstance(ctx).setGlobalMessageAndNotificationToAll(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), new GenericMessage(msg, GenericMessage.INFO));
				// MessageRepository.getInstance(ctx).setGlobalMessage(new
				// GenericMessage(msg, GenericMessage.INFO));

				SynchroHelper.performSynchro(application, staticConfig, globalContext);

				NavigationService navigationService = NavigationService.getInstance(globalContext);
				navigationService.clearAllViewPage();

				// clean component list when publish
				ComponentFactory.cleanComponentList(request.getSession().getServletContext(), globalContext);
			}

			/*** check url ***/
			ContentContext lgCtx = new ContentContext(ctx);
			Collection<String> lgs = globalContext.getContentLanguages();
			IURLFactory urlFactory = globalContext.getURLFactory(lgCtx);
			String dblURL = null;
			Collection<String> errorPageNames = null;
			if (urlFactory != null) {
				Map<String, String> pages = new HashMap<String, String>();
				errorPageNames = new LinkedList<String>();
				// correct identical URL.
				NavigationService.checkSameUrl(ctx);
				for (String lg : lgs) {
					lgCtx.setRequestContentLanguage(lg);
					MenuElement[] children = ContentService.getInstance(globalContext).getNavigation(lgCtx).getAllChildren();
					for (MenuElement menuElement : children) {
						String url = lgCtx.getRequestContentLanguage() + urlFactory.createURL(lgCtx, menuElement);
						if (pages.keySet().contains(url)) {
							if (!errorPageNames.contains(menuElement.getName())) {
								errorPageNames.add(menuElement.getName());
							}
							logger.warning("page : " + menuElement.getName() + " is refered by a url allready user : " + url);
							if (!errorPageNames.contains(pages.get(url))) {
								errorPageNames.add(pages.get(url));
							}
							if (menuElement.isRealContent(lgCtx)) {
								dblURL = url;
							}
						} else {
							pages.put(url, menuElement.getName());
						}
					}
				}
			}

			if (dblURL != null) {
				String msg = i18nAccess.getText("action.publish.error.same-url", new String[][] { { "url", dblURL }, { "pages", StringHelper.collectionToString(errorPageNames, ",") } });
				MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.ALERT));
			}

			// trick for PortletManager to clear view data, but should be
			// generalized in some PublishManager
			Collection<PublishListener> listeners = (Collection<PublishListener>) request.getSession().getServletContext().getAttribute(PublishListener.class.getName());
			if (listeners != null) {
				for (PublishListener listener : listeners) {
					listener.onPublish(ctx);
				}
			}

			ReverseLinkService.getInstance(globalContext).clearCache();

			return message;
		}

	}

	public static String performDeletePage(GlobalContext globalContext, ContentService content, ContentContext ctx, I18nAccess i18nAccess) throws Exception {

		if (!canModifyCurrentPage(ctx)) {
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
			newPath = menuElement.getRootOfChildrenAssociation().getFirstChild().getPath();
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

		ctx.setPath(newPath);
		String forwardURL = ctx.getResponse().encodeRedirectURL(URLHelper.createURL(ctx));
		ctx.getResponse().sendRedirect(forwardURL);

		return message;
	}

	public static final String performPreviewedit(HttpServletRequest request, RequestService rs, EditContext editCtx) {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		if (globalContext.isPreviewMode()) {
			if (rs.getParameter("preview", null) == null) {
				editCtx.setEditPreview(!editCtx.isEditPreview());
			} else {
				editCtx.setEditPreview(StringHelper.isTrue(rs.getParameter("preview", null)));
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
		if (pagePrevious == null) {
			return "previous page not found : " + pagePreviousName;
		}
		MenuElement page = content.getNavigation(ctx).searchChildFromName(pageName);
		if (page == null) {
			return "page not found : " + pageName;
		}
		if (!canModifyCurrentPage(ctx, page) || !checkPageSecurity(ctx, page)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		if (page == null) {
			return "page not found : " + pageName;
		}
		if (StringHelper.isTrue(rs.getParameter("as-child", null))) {
			NavigationHelper.movePage(ctx, pagePrevious, null, page);
		} else {
			NavigationHelper.movePage(ctx, pagePrevious.getParent(), pagePrevious, page);
		}

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);
		autoPublish(ctx.getRequest(), ctx.getResponse());

		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.moved", new String[][] { { "name", page.getName() } }), GenericMessage.INFO));

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
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.copy-page", new String[][] { { "name", ctx.getCurrentPage().getName() } }), GenericMessage.INFO));
		prepareUpdateInsertLine(ctx);
		return null;
	}

	public static String performPastePage(RequestService rs, ContentContext ctx, GlobalContext globalContext, EditContext editCtx, ContentService content, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String msg = null;

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		ContentContext newCtx = editCtx.getContextForCopy(ctx);
		newCtx.setRenderMode(ContentContext.EDIT_MODE);
		newCtx.setRequest(ctx.getRequest());
		newCtx.setResponse(ctx.getResponse());

		ContentElementList elems = newCtx.getCurrentPage().getContent(newCtx);

		String parentId = rs.getParameter("previous", null);
		if (parentId == null) {
			parentId = "0";
			ctx = ctx.getContextWithArea(null);
		}
		IContentVisualComponent parent = content.getComponent(ctx, parentId);

		int c = 0;
		String latestArea = null;
		while (elems.hasNext(ctx)) {
			ComponentBean bean = new ComponentBean(elems.next(ctx).getComponentBean());
			if (latestArea != null && !bean.getArea().equals(latestArea)) {
				parentId = "0";
			}
			latestArea = bean.getArea();
			bean.setLanguage(ctx.getRequestContentLanguage());
			parentId = content.createContent(ctx, bean, parentId, true);
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
		PersistenceService.getInstance(globalContext).store(ctx);
		autoPublish(ctx.getRequest(), ctx.getResponse());

		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.paste-page", new String[][] { { "count", "" + c } }), GenericMessage.INFO));

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
			clipBoard.copy(ctx, new ComponentBean(comp.getComponentBean()));
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.copy", new String[][] { { "type", "" + comp.getType() } }), GenericMessage.INFO));
			prepareUpdateInsertLine(ctx);
		}

		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			updatePreviewCommands(ctx, "_ep_content");
		}

		return null;
	}

	public static String performPasteComp(RequestService rs, ContentContext ctx, ContentService content, EditContext editContext, ClipBoard clipboard, Module currentModule, PersistenceService persistenceService, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
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

		MenuElement targetPage = content.getNavigation(ctx).searchChildFromId(rs.getParameter("pageContainerID", null));
		if (targetPage == null) {
			targetPage = ctx.getCurrentPage();
		}
		if (!ctx.isEditPreview()) {
			ctx.setArea(editContext.getCurrentArea());
			comp.setArea(null); // paste in current area
		}
		comp.setLanguage(null);
		String newId = content.createContent(ctx, targetPage, comp, previous, true);
		if (ctx.isAjax()) {
			ctx.getRequest().setAttribute(AbstractVisualComponent.SCROLL_TO_COMP_ID_ATTRIBUTE_NAME, newId);
			updateComponent(ctx, currentModule, newId, previous);
		}

		String msg = i18nAccess.getText("action.component.created", new String[][] { { "type", comp.getType() } });
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.INFO));

		persistenceService.store(ctx);
		modifPage(ctx, ctx.getCurrentPage());
		autoPublish(ctx.getRequest(), ctx.getResponse());

		return null;
	}

	public static String performMoveComponent(RequestService rs, ContentContext ctx, ContentService content, ClipBoard clipboard, Module currentModule, PersistenceService persistenceService, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
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

		String fromArea = comp.getArea();
		if (comp == null) {
			return "component not found.";
		}

		String area = null;
		for (Map.Entry<String, String> templateArea : ctx.getCurrentTemplate().getAreasMap().entrySet()) {
			if (templateArea.getValue().equals(areaId)) {
				area = templateArea.getKey();
			}
		}

		MenuElement targetPage = content.getNavigation(ctx).searchChildFromId(rs.getParameter("pageContainerID", null));
		if (targetPage == null) {
			targetPage = ctx.getCurrentPage();
		}

		ComponentHelper.moveComponent(ctx, comp, newPrevious, targetPage, area);

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
				ctx.getAjaxInsideZone().put(selecterPrefix + areaMap.get(compToBeUpdated.getArea()), ServletHelper.executeJSP(ctx, "/jsp/view/content_view.jsp?area=" + compToBeUpdated.getArea()));
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
			ctx.getAjaxInsideZone().put(selecterPrefix + areaMap.get(areaId), ServletHelper.executeJSP(ctx, "/jsp/view/content_view.jsp?area=" + areaId));

			ctx.setCurrentPageCached(fromPage);
			String fromPageSelector = "";
			if (parentPage.isChildrenAssociation()) {
				fromPageSelector = "#page_" + fromPage.getId() + " #";
				ctx.setCurrentPageCached(fromPage);
			}
			ctx.getAjaxInsideZone().put(fromPageSelector + fromArea, ServletHelper.executeJSP(ctx, "/jsp/view/content_view.jsp?area=" + fromArea));

			ctx.setCurrentPageCached(parentPage);
		}

		String msg = i18nAccess.getText("action.component.moved", new String[][] { { "type", comp.getType() } });
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.INFO));

		persistenceService.store(ctx);
		modifPage(ctx, ctx.getCurrentPage());
		autoPublish(ctx.getRequest(), ctx.getResponse());

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
		String path = editContext.getContextForCopy(ctx).getPath();
		MenuElement pageToBeMoved = content.getNavigation(ctx).searchChild(ctx, path);
		if (pageToBeMoved == null) {
			return "page not found : " + path;
		}
		if ((pageToBeMoved != null) && (pageToBeMoved.getParent() != null)) {
			pageToBeMoved.moveToParent(ctx.getCurrentPage());
			persistenceService.store(ctx);
			String[][] balises = { { "path", path }, { "new-path", pageToBeMoved.getPath() } };
			String msg = i18nAccess.getText("navigation.move", balises);
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
		}
		return null;
	}

	public static String performConfirmReplace(RequestService rs, ContentContext ctx, GlobalContext globalContext, EditContext editCtx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		if (!ResourceStatus.isInstance(session)) {
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
		if (editCtx.isEditPreview()) {
			ctx.setClosePopup(true);
		}
		return null;
	}

	public static String performInsertShared(RequestService rs, ContentContext ctx, GlobalContext globalContext, EditContext editContext, ContentService content, SharedContentService sharedContentService, Module currentModule, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		String sharedData = rs.getParameter("sharedContent", null);
		String previousId = rs.getParameter("previous", null);

		if (sharedData == null || previousId == null) {
			return "bad request structure, need sharedData and previousId as parameter.";
		} else {
			//sharedContentService.clearCache();
			SharedContent sharedContent = sharedContentService.getSharedContent(ctx, sharedData);
			if (sharedContent == null) {
				String msg = "error : shared content not found : " + sharedData;
				logger.warning(msg);
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
			MenuElement targetPage = content.getNavigation(ctx).searchChildFromId(rs.getParameter("pageContainerID", null));
			if (targetPage == null) {
				targetPage = ctx.getCurrentPage();
			}

			if (sharedContent.getLinkInfo() == null) {
				List<ComponentBean> beans = new LinkedList<ComponentBean>(sharedContent.getContent());
				for (ComponentBean componentBean : beans) {
					componentBean.setArea(areaKey);
				}
				String newId = content.createContent(ctx, targetPage, beans, previousId, true);
				ctx.getRequest().setAttribute(AbstractVisualComponent.SCROLL_TO_COMP_ID_ATTRIBUTE_NAME, newId);
			} else {				
				ComponentBean mirrorBean = new ComponentBean(PageMirrorComponent.TYPE, sharedContent.getLinkInfo(), ctx.getRequestContentLanguage());
				mirrorBean.setArea(areaKey);
				mirrorBean.setAuthors(ctx.getCurrentUserId());
				content.createContent(ctx, targetPage, mirrorBean, previousId, true);
			}

			PersistenceService.getInstance(globalContext).store(ctx);

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
				logger.info("update area : " + selecterPrefix + area);				
				ctx.getAjaxInsideZone().put(selecterPrefix + area, ServletHelper.executeJSP(ctx, "/jsp/view/content_view.jsp?area=" + areaKey));
			}

		}

		return null;
	}

	public static String performRefresh(StaticConfig staticConfig, ServletContext application, GlobalContext globalContext) throws Exception {
		SynchroHelper.performSynchro(application, staticConfig, globalContext);
		return null;
	}
}
