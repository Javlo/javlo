package org.javlo.module.content;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentContext;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentComponentsList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.ContextException;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.UserInterfaceContext;
import org.javlo.helper.DebugHelper;
import org.javlo.helper.LangHelper;
import org.javlo.helper.NavigationHelper;
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
import org.javlo.navigation.IURLFactory;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageConfiguration;
import org.javlo.search.SearchResult;
import org.javlo.search.SearchResult.SearchElement;
import org.javlo.service.ClipBoard;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;
import org.javlo.service.PublishListener;
import org.javlo.service.RequestService;
import org.javlo.service.syncro.SynchroThread;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.thread.AbstractThread;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;

public class Edit extends AbstractModuleAction {

	private static Logger logger = Logger.getLogger(Edit.class.getName());

	private static void prepareUpdateInsertLine(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editContext = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		ClipBoard clipBoard = ClipBoard.getInstance(ctx.getRequest());

		IContentVisualComponent currentTypeComponent = ComponentFactory.getComponentWithType(ctx, editContext.getActiveType());

		String typeName = StringHelper.getFirstNotNull(currentTypeComponent.getComponentLabel(ctx, globalContext.getEditLanguage()), i18nAccess.getText("content." + currentTypeComponent.getType()));
		String insertHere = i18nAccess.getText("content.insert-here", new String[][] { { "type", typeName } });

		String pastePageHere = null;
		if (editContext.getContextForCopy() != null) {
			pastePageHere = i18nAccess.getText("content.paste-here", new String[][] { { "page", editContext.getContextForCopy().getCurrentPage().getName() } });
		}

		String pasteHere = null;
		if (clipBoard.getCopiedComponent(ctx) != null) {
			pasteHere = i18nAccess.getText("content.paste-comp", new String[][] { { "type", clipBoard.getCopiedComponent(ctx).getType() } });
		}

		String insertXHTML = "<a class=\"action-button ajax\" href=\"" + URLHelper.createURL(ctx) + "?webaction=insert&previous=0&type=" + currentTypeComponent.getType() + "\">" + insertHere + "</a>";
		if (pastePageHere != null) {
			insertXHTML = insertXHTML + "<a class=\"action-button\" href=\"" + URLHelper.createURL(ctx) + "?webaction=pastePage&previous=0\">" + pastePageHere + "</a>";
		}
		if (pasteHere != null) {
			insertXHTML = insertXHTML + "<a class=\"action-button ajax\" href=\"" + URLHelper.createURL(ctx) + "?webaction=pasteComp&previous=0\">" + pasteHere + "</a>";
		}
		ctx.addAjaxInsideZone("insert-line-0", insertXHTML);

		ContentContext areaCtx = ctx.getContextWithArea(editContext.getCurrentArea());

		IContentComponentsList elems = ctx.getCurrentPage().getContent(areaCtx);
		while (elems.hasNext(areaCtx)) {
			IContentVisualComponent comp = elems.next(areaCtx);
			insertXHTML = "<a class=\"action-button ajax\" href=\"" + URLHelper.createURL(ctx) + "?webaction=insert&previous=" + comp.getId() + "&type=" + currentTypeComponent.getType() + "\">" + insertHere + "</a>";
			if (pastePageHere != null) {
				insertXHTML = insertXHTML + "<a class=\"action-button\" href=\"" + URLHelper.createURL(ctx) + "?webaction=pastePage&previous=" + comp.getId() + "\">" + pastePageHere + "</a>";
			}
			if (pasteHere != null) {
				insertXHTML = insertXHTML + "<a class=\"action-button ajax\" href=\"" + URLHelper.createURL(ctx) + "?webaction=pasteComp&previous=" + comp.getId() + "\">" + pasteHere + "</a>";
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
		if (previousId != null) {
			ctx.addAjaxZone("comp-child-" + previousId, newComponentXHTML);
		} else {
			ctx.addAjaxZone("comp-" + newId, newComponentXHTML);
		}
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
		private String type;
		private String label;
		private String value;
		private boolean metaTitle;
		private boolean selected;

		public ComponentWrapper(String type, String label, String value, boolean metaTitle) {
			this.type = type;
			this.label = label;
			this.value = value;
			this.metaTitle = metaTitle;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public boolean isMetaTitle() {
			return metaTitle;
		}

		public void setMetaTitle(boolean metaTitle) {
			this.metaTitle = metaTitle;
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
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
		AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		ContentService.getInstance(globalContext);
		MenuElement currentPage = ctx.getCurrentPage();
		if (currentPage.getEditorRoles().size() > 0) {
			if (!adminUserSecurity.haveRight(adminUserFactory.getCurrentUser(ctx.getRequest().getSession()), AdminUserSecurity.FULL_CONTROL_ROLE)) {
				if (!adminUserFactory.getCurrentUser(ctx.getRequest().getSession()).validForRoles(currentPage.getEditorRoles())) {
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
	private static void modifPage(ContentContext ctx) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		currentPage.setModificationDate(new Date());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService.getInstance(globalContext);
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		currentPage.setLatestEditor(editCtx.getUserPrincipal().getName());
		currentPage.setValid(false);
		currentPage.releaseCache();
	}

	private static void loadComponentList(ContentContext ctx) throws Exception {
		// if (ctx.getRequest().getAttribute("components") == null) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IContentVisualComponent[] components = ComponentFactory.getComponents(ctx);
		List<ComponentWrapper> comps = new LinkedList<ComponentWrapper>();
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		ComponentWrapper titleWrapper = null;
		for (int i = 0; i < components.length - 1; i++) { // remove title without component
			if (!components[i].isMetaTitle() || !components[i + 1].isMetaTitle()) { // if next component is title too so the component group is empty
				IContentVisualComponent comp = components[i];
				if (comp.isMetaTitle() || globalContext.getComponents().contains(comp.getClass().getName())) {
					ComponentWrapper compWrapper = new ComponentWrapper(comp.getType(), comp.getComponentLabel(ctx, globalContext.getEditLanguage()), comp.getValue(ctx), comp.isMetaTitle());
					if (components[i].isMetaTitle()) {
						titleWrapper = compWrapper;
					}
					if (comp.getType().equals(editCtx.getActiveType())) {
						compWrapper.setSelected(true);
						if (titleWrapper != null) {
							{
								titleWrapper.setSelected(true);
							}
						}
					}
					comps.add(compWrapper);
				}
			}
		}
		if (!components[components.length - 1].isMetaTitle()) {
			IContentVisualComponent comp = components[components.length - 1];
			ComponentWrapper compWrapper = new ComponentWrapper(comp.getType(), comp.getComponentLabel(ctx, globalContext.getEditLanguage()), comp.getValue(ctx), comp.isMetaTitle());
			comps.add(compWrapper);
			if (comp.getType().equals(editCtx.getActiveType())) {
				compWrapper.setSelected(true);
				if (titleWrapper != null) {
					{
						titleWrapper.setSelected(true);
					}
				}
			}
		}
		
		
		List<ComponentWrapper> listWithoutEmptyTitle = new LinkedList<Edit.ComponentWrapper>();
		ComponentWrapper title = null;
		for (ComponentWrapper comp : comps) {
			if (comp.isMetaTitle()) {
				title = comp;
			} else {
				if (title != null) {
					listWithoutEmptyTitle.add(title);
					title = null;
				}
				listWithoutEmptyTitle.add(comp);
			}
		}
		
		ctx.getRequest().setAttribute("components", listWithoutEmptyTitle);

		Module currentModule = ModulesContext.getInstance(ctx.getRequest().getSession(), globalContext).getCurrentModule();
		Box componentBox = currentModule.getBox("components");
		if (componentBox != null) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			IContentVisualComponent comp = ComponentFactory.getComponentWithType(ctx, editCtx.getActiveType());
			componentBox.setTitle(i18nAccess.getText("components.title", new String[][] { { "component", comp.getComponentLabel(ctx, globalContext.getEditLanguage()) } }));
		}
		// }
	}

	/**
	 * check security on the current page
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

	/***************/
	/** WEBACTION **/
	/***************/

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {

		String msg = super.prepare(ctx, modulesContext);

		HttpServletRequest request = ctx.getRequest();

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		Module currentModule = modulesContext.getCurrentModule();

		/** set the principal renderer **/
		ContentModuleContext modCtx = (ContentModuleContext) LangHelper.smartInstance(request, ctx.getResponse(), ContentModuleContext.class);
		if (request.getParameter("query") == null) {
			currentModule.setBreadcrumb(true);
			currentModule.setSidebar(true);
			UserInterfaceContext userIterfaceContext = UserInterfaceContext.getInstance(ctx.getRequest().getSession(), globalContext);
			if (!userIterfaceContext.isComponentsList()) {
				currentModule.clearAllBoxes();
			}
			switch (modCtx.getMode()) {
			case ContentModuleContext.PREVIEW_MODE:
				currentModule.setToolsRenderer("/jsp/actions.jsp?button_edit=true&button_page=true&button_publish=true");
				request.setAttribute("previewURL", URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE)));
				currentModule.setRenderer("/jsp/preview.jsp");
				currentModule.setBreadcrumbTitle(I18nAccess.getInstance(ctx.getRequest()).getText("content.preview"));
				break;
			case ContentModuleContext.PAGE_MODE:
				currentModule.setToolsRenderer("/jsp/actions.jsp?button_edit=true&button_preview=true&button_publish=true&button_delete_page=true");
				request.setAttribute("page", ctx.getCurrentPage().getPageBean(ctx));
				currentModule.setRenderer("/jsp/page_properties.jsp");
				currentModule.setBreadcrumbTitle(I18nAccess.getInstance(ctx.getRequest()).getText("item.title"));
				break;
			default:
				currentModule.setToolsRenderer("/jsp/actions.jsp?button_preview=true&button_page=true&button_save=true&button_copy=true&button_publish=true&languages=true&areas=true");
				currentModule.setRenderer("/jsp/content_wrapper.jsp");
				currentModule.setBreadcrumbTitle(I18nAccess.getInstance(ctx.getRequest()).getText("content.mode.content"));
				break;
			}
		}

		/** COMPONENT LIST **/
		loadComponentList(ctx);

		/** CONTENT **/
		/*
		 * ComponentContext compCtx = ComponentContext.getInstance(request); IContentComponentsList elems = ctx.getCurrentPage().getContent(ctx); if (compCtx.getNewComponents().length == 0) { while (elems.hasNext(ctx)) { compCtx.addNewComponent(elems.next(ctx)); } }
		 */

		/** page properties **/
		PageConfiguration pageConfig = PageConfiguration.getInstance(globalContext);
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		List<Template> templates = pageConfig.getContextTemplates(editCtx);
		Collections.sort(templates);

		if (ctx.getCurrentTemplate() != null) {
			ctx.getRequest().setAttribute("areas", ctx.getCurrentTemplate().getAreas());
		}
		ctx.getRequest().setAttribute("currentArea", editCtx.getCurrentArea());

		request.setAttribute("templates", templates);

		if (ctx.getCurrentTemplate() != null) {
			String templateImageURL = URLHelper.createTransformStaticTemplateURL(ctx, ctx.getCurrentTemplate(), "template", ctx.getCurrentTemplate().getVisualFile());
			request.setAttribute("templateImageUrl", templateImageURL);
		}


		if (isLightInterface(ctx)) {
			currentModule.setSidebar(false);
			currentModule.removeNavigation("persistence");
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
			String msg = i18nAccess.getText("content.new-type", new String[][] { { "type", newType } });
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));

			if (requestService.getParameter("comp_id", null) != null) {
				return performEditpreview(requestService, ctx, componentContext, ContentService.getInstance(globalContext), ModulesContext.getInstance(ctx.getRequest().getSession(), globalContext), modCtx);
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

	public static final String performInsert(HttpServletRequest request, HttpServletResponse response, GlobalContext globalContext, ContentContext ctx, ContentService content, Module currentModule, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {
		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}
		String previousId = request.getParameter("previous");
		String type = request.getParameter("type");
		if (previousId == null || type == null) {
			return "bad insert request need previousId and component type.";
		}

		String newId = content.createContent(ctx, previousId, type, "");
		if (ctx.isAjax()) {
			updateComponent(ctx, currentModule, newId, previousId);
		}

		String msg = i18nAccess.getText("action.component.created", new String[][] { { "type", type } });
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.INFO));

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);
		modifPage(ctx);
		autoPublish(request, response);

		return null;
	}

	public static final String performDelete(ContentContext ctx, HttpServletRequest request, HttpServletResponse response, I18nAccess i18nAccess) throws Exception {
		if (!canModifyCurrentPage(ctx)) {
			return i18nAccess.getText("action.block");
		}
		String id = request.getParameter("id");
		if (id != null) {
			ClipBoard clipBoard = ClipBoard.getInstance(request);
			if (id.equals(clipBoard.getCopied())) {
				clipBoard.clear();
			}
			MenuElement elem = ctx.getCurrentPage();
			String type = elem.removeContent(ctx, id);
			GlobalContext globalContext = GlobalContext.getInstance(request);
			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.store(ctx);

			if (type != null) {
				String typeName = type;
				String msg = i18nAccess.getText("action.component.removed", new String[][] { { "type", typeName } });
				MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.INFO));
			}

			if (ctx.isAjax()) {
				ctx.addAjaxZone("comp-" + id, "");
				ctx.addAjaxZone("comp-child-" + id, "");
				ctx.addAjaxInsideZone("insert-line-" + id, "");
			}

			modifPage(ctx);
			autoPublish(request, response);

		}
		return null;
	}

	public static final String performSave(ContentContext ctx, GlobalContext globalContext, ContentService content, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository, Module currentModule, AdminUserFactory adminUserFactory) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		String message = null;

		// IContentComponentsList contentList = currentPage.getAllContent(ctx);
		List<String> components = requestService.getParameterListValues("components", Collections.EMPTY_LIST);

		boolean modif = false;

		// boolean needRefresh = false;

		for (String compId : components) {
			IContentVisualComponent elem = content.getComponent(ctx, compId);
			if (StringHelper.isTrue(requestService.getParameter("id-" + elem.getId(), "false"))) {
				elem.performConfig(ctx);
				elem.performEdit(ctx);
				if (!elem.isModify()) { // if elem not modified check modification via rawvalue
					String rawValue = requestService.getParameter("raw_value_" + elem.getId(), null);
					if (rawValue != null && !rawValue.equals(elem.getValue(ctx))) {
						logger.info("raw value modification for " + elem.getType());
						elem.setValue(rawValue);
						elem.setNeedRefresh(true);
					}
				}
				if (elem.isNeedRefresh() && ctx.isAjax()) {
					updateComponent(ctx, currentModule, elem.getId(), null);
				}
			}

			if (elem.isModify()) {
				modif = true;
				elem.stored();
			}
			if (message == null) {
				message = elem.getErrorMessage();
			}
		}

		// ctx.setNeedRefresh(needRefresh);
		if (modif) {
			modifPage(ctx);
			if (adminUserFactory.getCurrentUser(ctx.getRequest().getSession()) != null) {
				content.setAttribute(ctx, "user.update", adminUserFactory.getCurrentUser(ctx.getRequest().getSession()).getLogin());
			}
			PersistenceService.getInstance(globalContext).store(ctx);
		}

		if (message == null) {
			if (modif) {
				NavigationService navigationService = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
				navigationService.clearPage(ctx);

				messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.updated"), GenericMessage.INFO));
				autoPublish(ctx.getRequest(), ctx.getResponse());
			} else {
				messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.not-updated"), GenericMessage.ALERT));
			}
		}

		return message;
	}

	public static final String performChangeMode(HttpSession session, RequestService requestService, ContentModuleContext modCtx) {
		modCtx.setMode(Integer.parseInt(requestService.getParameter("mode", "" + ContentModuleContext.EDIT_MODE)));
		return null;
	}

	public static final String performPageProperties(ServletContext application, GlobalContext globalContext, ContentContext ctx, ContentService content, EditContext editCtx, PageConfiguration pageConfig, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {

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
			if (!pageName.equals(newName)) {
				errorMessage = validNodeName(newName, i18nAccess);

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

			String templateName = requestService.getParameter("template", null);
			if (templateName != null) {
				if (templateName.length() > 1) {
					Template template = TemplateFactory.getDiskTemplates(application).get(templateName);
					if (template != null && pageConfig.getContextTemplates(editCtx).contains(template)) {
						page.setTemplateName(template.getName());
						modify = true;
					} else {
						return "template not found : " + templateName;
					}
				} else {
					page.setTemplateName(null); // inherited
				}

			}
			if (errorMessage != null) {
				messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(errorMessage, GenericMessage.ERROR));
			} else {
				if (modify) {
					PersistenceService.getInstance(globalContext).store(ctx);
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("message.update-page-properties"), GenericMessage.INFO));
				}
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
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("edit.message.new-language") + lg, GenericMessage.INFO));
			String newURL = URLHelper.createURL(ctx);
			ctx.getResponse().sendRedirect(newURL);
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

			if (nodeName == null) {
				return "bad request structure : need 'name'.";
			}

			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());
			message = validNodeName(nodeName, i18nAccess);

			if (nameExist(nodeName, ctx, content)) {
				message = i18nAccess.getText("action.validation.name-allready-exist", new String[][] { { "name", nodeName } });
			}

			if (message == null) {
				MenuElement elem = MenuElement.getInstance(globalContext);
				elem.setName(nodeName);
				elem.setCreator(editCtx.getUserPrincipal().getName());
				elem.setVisible(globalContext.isNewPageVisible());
				if (requestService.getParameter("add-first", null) == null) {
					ctx.getCurrentPage().addChildMenuElementAutoPriority(elem);
				} else {
					elem.setPriority(0);
					ctx.getCurrentPage().addChildMenuElement(elem);
				}
				path = path + "/" + nodeName;

				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				persistenceService.store(ctx);
				autoPublish(ctx.getRequest(), ctx.getResponse());

				NavigationService navigationService = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
				navigationService.clearPage(ctx);

				String msg = i18nAccess.getText("action.add.new-page", new String[][] { { "path", path } });
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
			}

		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		return message;
	}

	public static final String performChangeArea(ContentContext ctx, RequestService requestService, EditContext editContext, I18nAccess i18nAccess, MessageRepository messageRepository) {
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

	public static String performSynchro(ServletContext application, StaticConfig staticConfig, GlobalContext globalContext) throws Exception {
		if (globalContext.getDMZServerIntra() != null) {
			SynchroThread synchro = (SynchroThread) AbstractThread.createInstance(staticConfig.getThreadFolder(), SynchroThread.class);
			synchro.initSynchronisationThread(staticConfig, globalContext, application);
			synchro.store();
		}
		return null;
	}

	public static String performPublish(ServletContext application, HttpServletRequest request, StaticConfig staticConfig, GlobalContext globalContext, ContentService content, ContentContext ctx, I18nAccess i18nAccess) throws Exception {

		DebugHelper.writeInfo(System.out);

		synchronized (content.getNavigation(ctx).getLock()) {

			String message = null;

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
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));

			performSynchro(application, staticConfig, globalContext);

			NavigationService navigationService = NavigationService.getInstance(globalContext, request.getSession());
			navigationService.clearAllPage();

			// clean component list when publish
			ComponentFactory.cleanComponentList(request.getSession().getServletContext(), globalContext);

			/*** check url ***/
			ContentContext lgCtx = new ContentContext(ctx);
			Collection<String> lgs = globalContext.getContentLanguages();
			Collection<String> urls = new HashSet<String>();
			String dblURL = null;
			IURLFactory urlFactory = globalContext.getURLFactory(lgCtx);
			if (urlFactory != null) {
				for (String lg : lgs) {
					lgCtx.setRequestContentLanguage(lg);
					MenuElement[] children = ContentService.getInstance(globalContext).getNavigation(lgCtx).getAllChilds();
					for (MenuElement menuElement : children) {
						String url = lgCtx.getRequestContentLanguage() + urlFactory.createURL(lgCtx, menuElement);
						if (urls.contains(url)) {
							dblURL = url;
						} else {
							urls.add(url);
						}
					}
				}
			}

			if (dblURL != null) {
				msg = i18nAccess.getText("action.publish.error.same-url", new String[][] { { "url", dblURL } });
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ALERT));
			}

			content.clearComponentCache();

			// trick for PortletManager to clear view data, but should be generalized in some PublishManager
			Collection<PublishListener> listeners = (Collection<PublishListener>) request.getSession().getServletContext().getAttribute(PublishListener.class.getName());
			if (listeners != null) {
				for (PublishListener listener : listeners) {
					listener.onPublish(ctx);
				}
			}

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

		if (message == null) {
			if (menuElement == null) {
				message = i18nAccess.getText("action.remove.can-not-delete");
			} else {
				synchronized (menuElement) {
					menuElement.clearVirtualParent();
				}
				NavigationService service = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
				service.removeNavigation(ctx, menuElement);
				String msg = i18nAccess.getText("action.remove.deleted", new String[][] { { "path", path } });
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
				autoPublish(ctx.getRequest(), ctx.getResponse());
			}
		}

		ctx.setPath(newPath);

		NavigationService navigationService = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
		navigationService.clearPage(ctx);

		return message;
	}

	public static final String performPreviewedit(EditContext editCtx) {
		editCtx.setEditPreview(!editCtx.isEditPreview());
		return null;
	}

	public static String performEditpreview(RequestService requestService, ContentContext ctx, ComponentContext componentContext, ContentService content, ModulesContext moduleContext, ContentModuleContext modCtx) throws Exception {
		moduleContext.searchModule("content").restoreAll();
		performChangeMode(ctx.getRequest().getSession(), requestService, modCtx);
		String compId = requestService.getParameter("comp_id", null).substring(3);
		IContentVisualComponent comp = content.getComponent(ctx, compId);
		if (comp == null) {
			return "component not found : " + compId;
		}
		componentContext.addNewComponent(comp);
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
			if (pagePrevious == null) {
				return "previous page not found : " + pagePreviousName;
			}
		}
		MenuElement page = content.getNavigation(ctx).searchChildFromName(pageName);
		if (page == null) {
			return "page not found : " + pageName;
		}
		if (pagePrevious == null) {
			page.setPriority(0);
		} else {
			if (page.getPreviousBrother() != null && page.getPreviousBrother().equals(pagePrevious)) { // page is not really moved
				return null;
			}
			page.setPriority(pagePrevious.getPriority() + 1);
		}
		NavigationHelper.changeStepPriority(page.getParent().getAllChilds(), 10);

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);
		autoPublish(ctx.getRequest(), ctx.getResponse());

		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.moved", new String[][] { { "name", page.getName() } }), GenericMessage.INFO));

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

		if (!checkPageSecurity(ctx)) {
			return null;
		}

		ContentContext newCtx = editCtx.getContextForCopy();
		newCtx.setRequest(ctx.getRequest());
		newCtx.setResponse(ctx.getResponse());

		ContentElementList elems = newCtx.getCurrentPage().getContent(newCtx);

		String parentId = rs.getParameter("previous", null);
		if (parentId == null) {
			return "bad request structure : need 'previous' as parameter.";
		}
		IContentVisualComponent parent = content.getComponent(ctx, parentId);

		int c = 0;
		while (elems.hasNext(ctx)) {
			ComponentBean bean = elems.next(ctx).getBean(ctx);
			bean.setArea(ctx.getArea());
			bean.setLanguage(ctx.getRequestContentLanguage());
			parentId = content.createContent(ctx, bean, parentId);
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
		modifPage(ctx);
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
			clipBoard.copy(comp.getBean(ctx));
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("edit.message.copy", new String[][] { { "type", "" + comp.getType() } }), GenericMessage.INFO));
			prepareUpdateInsertLine(ctx);
		}

		return null;
	}

	public static String performPasteComp(RequestService rs, ContentContext ctx, ContentService content, ClipBoard clipboard, Module currentModule, PersistenceService persistenceService, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String previous = rs.getParameter("previous", null);
		if (previous == null) {
			return "bad request structure : need 'previous' parameter.";
		}
		ComponentBean comp = clipboard.getCopiedComponent(ctx);
		if (comp == null) {
			return "nothing to paste.";
		}
		String newId = content.createContent(ctx, previous, comp.getType(), comp.getValue());
		if (ctx.isAjax()) {
			updateComponent(ctx, currentModule, newId, previous);
		}

		String msg = i18nAccess.getText("action.component.created", new String[][] { { "type", comp.getType() } });
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.INFO));

		persistenceService.store(ctx);
		modifPage(ctx);
		autoPublish(ctx.getRequest(), ctx.getResponse());

		return null;
	}

	public static String performClearClipboard(ClipBoard clipboard, EditContext editCtx) {
		editCtx.setPathForCopy(null);
		clipboard.clear();
		return null;
	}

}
