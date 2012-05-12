package org.javlo.module.content;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.javlo.actions.IModuleAction;
import org.javlo.component.core.ComponentContext;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentComponentsList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.Module;
import org.javlo.module.Module.Box;
import org.javlo.module.ModuleContext;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageConfiguration;
import org.javlo.service.ClipBoard;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;

public class Edit implements IModuleAction {
	
	private static void prepareUpdateInsertLine(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editContext = EditContext.getInstance(globalContext,ctx.getRequest().getSession());
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		
		IContentVisualComponent currentTypeComponent = ComponentFactory.getComponentWithType(ctx, editContext.getActiveType());

		String typeName = StringHelper.getFirstNotNull( currentTypeComponent.getComponentLabel(ctx,globalContext.getEditLanguage()), i18nAccess.getText ( "content."+currentTypeComponent.getType()));
		String insertHere = i18nAccess.getText("content.insert-here", new String[][] {{"type",typeName}});
		
		String insertXHTML = "<a class=\"action-button ajax\" href=\""+URLHelper.createURL(ctx)+"?webaction=insert&previous=0&type="+currentTypeComponent.getType()+"\">"+insertHere+"</a>";
		ctx.addAjaxInsideZone("insert-line-0", insertXHTML);
		
		IContentComponentsList elems = ctx.getCurrentPage().getContent(ctx);	
		while (elems.hasNext(ctx)) {
			IContentVisualComponent comp = elems.next(ctx);
			insertXHTML = "<a class=\"action-button ajax\" href=\""+URLHelper.createURL(ctx)+"?webaction=insert&previous="+comp.getId()+"&type="+currentTypeComponent.getType()+"\">"+insertHere+"</a>";
			ctx.addAjaxInsideZone("insert-line-"+comp.getId(), insertXHTML);
		}
	}
	
	/**
	 * update component
	 * @param ctx
	 * @param currentModule
	 * @param newId the id of the component
	 * @param previousId the id, null for update and previous component for insert.
	 * @throws Exception
	 */
	private static void updateComponent(ContentContext ctx, Module currentModule, String newId, String previousId) throws Exception {
		ComponentContext compCtx = ComponentContext.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService content = ContentService.getInstance(globalContext);
		compCtx.addNewComponent(content.getComponent(ctx, newId)); // prepare ajax rendering
		String componentRenderer = URLHelper.mergePath(currentModule.getPath()+"/jsp/content.jsp");
		String newComponentXHTML = ServletHelper.executeJSP(ctx, componentRenderer);
		if (previousId != null) { 
			ctx.addAjaxZone("comp-child-"+previousId, newComponentXHTML);
		} else {
			ctx.addAjaxZone("comp-"+newId, newComponentXHTML);
		}
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
	private static boolean checkPageSecurity(ContentContext ctx) throws Exception {
		AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance(ctx.getRequest().getSession().getServletContext());
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
		//if (ctx.getRequest().getAttribute("components") == null) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			IContentVisualComponent[] components = ComponentFactory.getComponents(ctx);
			List<ComponentWrapper> comps = new LinkedList<ComponentWrapper>();
			EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
			ComponentWrapper titleWrapper = null;
			for (int i = 0; i < components.length - 1; i++) { // remove title without component
				if (!components[i].isMetaTitle() || !components[i + 1].isMetaTitle()) { // if next component is title too so the component group is empty
					IContentVisualComponent comp = components[i];
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
			ctx.getRequest().setAttribute("components", comps);
			
			Module currentModule = ModuleContext.getInstance(globalContext, ctx.getRequest().getSession()).getCurrentModule();
			Box componentBox = currentModule.getBox("components");
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());			
			IContentVisualComponent comp = ComponentFactory.getComponentWithType(ctx, editCtx.getActiveType());
			componentBox.setTitle(i18nAccess.getText("components.title", new String[][] { { "component", comp.getComponentLabel(ctx, globalContext.getEditLanguage()) } }));
		//}
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
	public String prepare(ContentContext ctx, ModuleContext moduleContext) throws Exception {

		HttpServletRequest request = ctx.getRequest();

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		Module currentModule = moduleContext.getCurrentModule();

		/** set the principal renderer **/
		ContentModuleContext modCtx = ContentModuleContext.getInstance(request.getSession());
		switch (modCtx.getMode()) {
		case ContentModuleContext.PREVIEW_MODE:
			currentModule.setToolsRenderer("/jsp/actions.jsp?button_edit=true&button_page=true&button_publish=true");
			request.setAttribute("previewURL", URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE)));
			currentModule.setRenderer("/jsp/preview.jsp");
			currentModule.setBreadcrumbTitle(I18nAccess.getInstance(ctx.getRequest()).getText("content.preview"));
			break;
		case ContentModuleContext.PAGE_MODE:
			currentModule.setToolsRenderer("/jsp/actions.jsp?button_edit=true&button_preview=true&button_publish=true");
			request.setAttribute("page", ctx.getCurrentPage().getPageBean(ctx));
			currentModule.setRenderer("/jsp/page_properties.jsp");
			currentModule.setBreadcrumbTitle(I18nAccess.getInstance(ctx.getRequest()).getText("item.title"));
			break;
		default:
			currentModule.setToolsRenderer("/jsp/actions.jsp?button_preview=true&button_page=true&button_save=true&button_publish=true&languages=true");
			currentModule.setRenderer("/jsp/content_wrapper.jsp");
			currentModule.setBreadcrumbTitle(I18nAccess.getInstance(ctx.getRequest()).getText("content.mode.content"));
			break;
		}

		/** COMPONENT LIST **/
		loadComponentList(ctx);

		/** CONTENT **/
		/*ComponentContext compCtx = ComponentContext.getInstance(request);
		IContentComponentsList elems = ctx.getCurrentPage().getContent(ctx);
		if (compCtx.getNewComponents().length == 0) {
			while (elems.hasNext(ctx)) {
				compCtx.addNewComponent(elems.next(ctx));
			}
		}*/

		/** page properties **/
		PageConfiguration pageConfig = PageConfiguration.getInstance(globalContext);
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		List<Template> templates = pageConfig.getContextTemplates(editCtx);
		Collections.sort(templates);

		request.setAttribute("templates", templates);

		String templateImageURL = URLHelper.createTransformStaticTemplateURL(ctx, ctx.getCurrentTemplate(), "template", ctx.getCurrentTemplate().getVisualFile());
		request.setAttribute("templateImageUrl", templateImageURL);

		return null;
	}

	public static final String performChangeComponent(GlobalContext globalContext, EditContext editCtx, ContentContext ctx, RequestService requestService, I18nAccess i18nAccess, Module currentModule) throws Exception {
		String newType = requestService.getParameter("type", null);
		String message = null;
		if (newType != null) {
			editCtx.setActiveType(newType);
			newType = i18nAccess.getText("content." + newType, newType);
			String msg = i18nAccess.getText("content.new-type", new String[][] { { "type", newType } });
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));

			Box componentBox = currentModule.getBox("components");
			if (componentBox != null) {
				loadComponentList(ctx);
				componentBox.update(ctx);
				prepareUpdateInsertLine(ctx);
			} else {
				message = "component box not found.";
			}
		} else {
			message = "Fatal error : type not found";
		}
		return message;
	}

	public static final String performInsert(HttpServletRequest request, ContentContext ctx, ContentService content, Module currentModule, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {
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
			updateComponent(ctx,currentModule,newId,previousId);
		}
		
		String msg = i18nAccess.getText("action.component.created", new String[][] { { "type", type } });
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(msg, GenericMessage.INFO));

		return null;
	}

	public static final String performDelete(ContentContext ctx, HttpServletRequest request, HttpServletResponse response, I18nAccess i18nAccess) throws Exception {
		if (!canModifyCurrentPage(ctx)) {
			return i18nAccess.getText("action.block");
		}
		String id = request.getParameter("id");
		if (id != null) {
			ClipBoard clipBoard = ClipBoard.getClibBoard(request);
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
				ctx.addAjaxZone("comp-"+id, "");
				ctx.addAjaxZone("comp-child-"+id, "");
				ctx.addAjaxInsideZone("insert-line-"+id, "");
			}

			modifPage(ctx);
			autoPublish(request, response);

		}
		return null;
	}

	public static final String performSave(ContentContext ctx, GlobalContext globalContext, ContentService content, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository, MenuElement currentPage, Module currentModule, AdminUserFactory adminUserFactory) throws Exception {

		if (!canModifyCurrentPage(ctx) || !checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		String message = null;

		IContentComponentsList contentList = currentPage.getAllContent(ctx);

		boolean modif = false;

		boolean needRefresh = false;

		while (contentList.hasNext(ctx)) {
			IContentVisualComponent elem = contentList.next(ctx);
			if (StringHelper.isTrue(requestService.getParameter("id-" + elem.getId(), "false"))) {
				elem.performConfig(ctx);
				elem.refresh(ctx);
				if (elem.isNeedRefresh()) {
					updateComponent(ctx, currentModule, elem.getId(), null);
				}
			}			

			if (elem.isModify()) {
				modif = true;
			}
			if (message == null) {
				message = elem.getErrorMessage();
			}
		}
		
		ctx.setNeedRefresh(needRefresh);
		if (modif) {
			modifPage(ctx);
			if (adminUserFactory.getCurrentUser(ctx.getRequest().getSession()) != null) {
				content.setAttribute(ctx, "user.update", adminUserFactory.getCurrentUser(ctx.getRequest().getSession()).getLogin());
			}
			PersistenceService.getInstance(globalContext).store(ctx);
		}
		
		if (message == null) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.updated"), GenericMessage.INFO));
			autoPublish(ctx.getRequest(), ctx.getResponse());
		}

		NavigationService navigationService = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
		navigationService.clearPage(ctx);

		return null;
	}

	public static final String performChangeMode(HttpSession session, RequestService requestService) {
		ContentModuleContext modCtx = ContentModuleContext.getInstance(session);
		modCtx.setMode(Integer.parseInt(requestService.getParameter("mode", "" + ContentModuleContext.EDIT_MODE)));
		return null;
	}

	public static final String performPageProperties(ServletContext application, ContentContext ctx, ContentService content, EditContext editCtx, PageConfiguration pageConfig, RequestService requestService, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {

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
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("message.update-page-properties"), GenericMessage.INFO));
				}
			}
		}
		return null;
	}

	public static final String performChangeLanguage(RequestService requestService, ContentContext ctx, I18nAccess i18nAccess, MessageRepository messageRepository) {
		String lg = requestService.getParameter("language", null);
		if (lg != null) {
			ctx.setContentLanguage(lg);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("edit.message.new-language") + lg, GenericMessage.INFO));

		} else {
			return "bad request structure : 'language' not found.";
		}
		return null;
	}

}
