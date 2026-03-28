package org.javlo.remote;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentLayout;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.NavigationHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.content.Edit;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Content (component) management actions — callable via WebAction or AjaxServlet.
 *
 * Authentication : standard Javlo session OR j_token parameter.
 *
 * Invocation examples:
 *   webaction=content.add    — POST /webaction/content.add   or ?webaction=content.add
 *   webaction=content.edit   — POST /webaction/content.edit
 *   webaction=content.remove — POST /webaction/content.remove
 *   webaction=content.move   — POST /webaction/content.move
 *
 * Parameters per action:
 *
 *   content.add:
 *     page        (required) — id, name or path of the target page
 *     type        (required) — component type (e.g. "text", "title", "image")
 *     area        (required) — template area key
 *     previous    (opt)      — id of component after which to insert ("0" = first position)
 *     value       (opt)      — initial raw value
 *     style       (opt)      — CSS style class
 *     layout      (opt)      — layout flags: l=left r=right c=center j=justify b=bold i=italic u=underline t=line-through; append #font-family for font (e.g. "lcb#Arial")
 *     renderer    (opt)      — renderer key (as defined in the component config)
 *     columnSize  (opt)      — grid column width (integer, e.g. 6 for half-width in a 12-col grid)
 *     columnStyle (opt)      — CSS class applied to the column wrapper
 *
 *   content.edit:
 *     id          (required) — component id
 *     value       (opt)      — new raw value
 *     style       (opt)      — new style class
 *     layout      (opt)      — layout flags (see content.add)
 *     renderer    (opt)      — renderer key
 *     columnSize  (opt)      — grid column width (integer)
 *     columnStyle (opt)      — CSS class for the column wrapper
 *
 *   content.remove:
 *     id       (required) — component id
 *
 *   content.move:
 *     id       (required) — component id to move
 *     previous (required) — id of component after which to insert ("0" = first position)
 *     area     (opt)      — target area key (default = component's current area)
 *     page     (opt)      — target page id/name/path (default = component's current page)
 *
 *   content.clearPage:
 *     page     (required) — id, name or path of the page to clear
 *
 *   content.publish:
 *     (no parameters) — publishes the entire site (preview → view)
 *
 * JSON response (via AjaxServlet): data is placed in ctx.getAjaxData().
 */
public class ContentAction implements IAction {

	private static final Logger logger = Logger.getLogger(ContentAction.class.getName());

	@Override
	public String getActionGroupName() {
		return "content";
	}

	/** Access requires a logged-in edit user with content role, or a global admin. */
	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		if (ctx.getCurrentEditUser() == null) return false;
		User currentUser = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession())
				.getCurrentUser(ctx.getRequest().getSession());
		if (AdminUserSecurity.getInstance().isAdmin(currentUser)) return true;
		return AdminUserSecurity.getInstance().haveRole(ctx.getCurrentEditUser(), AdminUserSecurity.CONTENT_ROLE);
	}

	// -------------------------------------------------------------------------
	// content.add
	// Params: page (required), type (required), area (required),
	//         previous (opt, default "0"), value (opt), style (opt),
	//         layout (opt), renderer (opt), columnSize (opt), columnStyle (opt)
	// -------------------------------------------------------------------------
	public static String performAdd(RequestService rs, ContentContext ctx, ContentService contentService, PersistenceService persistenceService) throws Exception {
		String pageRef = rs.getParameter("page", null);
		String type    = rs.getParameter("type", null);
		String area    = rs.getParameter("area", null);

		if (pageRef == null || pageRef.trim().isEmpty()) {
			return "content.add: missing required parameter 'page'";
		}
		if (type == null || type.trim().isEmpty()) {
			return "content.add: missing required parameter 'type'";
		}
		if (area == null || area.trim().isEmpty()) {
			return "content.add: missing required parameter 'area'";
		}

		MenuElement page = NavigationHelper.searchPage(ctx, pageRef);
		if (page == null) {
			return "content.add: page not found: " + pageRef;
		}

		String previous    = rs.getParameter("previous", "0");
		String value       = rs.getParameter("value", "");
		String style       = rs.getParameter("style", null);
		String layout      = rs.getParameter("layout", null);
		String renderer    = rs.getParameter("renderer", null);
		String columnSize  = rs.getParameter("columnSize", null);
		String columnStyle = rs.getParameter("columnStyle", null);

		String newId = contentService.createContent(ctx, page, area, previous, type, value, true);

		IContentVisualComponent comp = contentService.getComponent(ctx, newId);
		if (comp != null) {
			if (style != null && !style.trim().isEmpty()) {
				comp.setStyle(ctx, style);
			}
			if (layout != null && !layout.trim().isEmpty()) {
				comp.getComponentBean().setLayout(new ComponentLayout(layout));
			}
			if (renderer != null && !renderer.trim().isEmpty()) {
				comp.setRenderer(ctx, renderer);
			}
			if (columnSize != null && !columnSize.trim().isEmpty()) {
				try {
					comp.setColumnSize(Integer.parseInt(columnSize.trim()));
				} catch (NumberFormatException e) {
					logger.warning("content.add: invalid columnSize value '" + columnSize + "' — ignored");
				}
			}
			if (columnStyle != null) {
				comp.getComponentBean().setColumnStyle(columnStyle);
			}
		}

		persistenceService.setAskStore(true);

		ctx.getAjaxData().put("component", componentToMap(contentService.getComponent(ctx, newId), ctx));
		logger.info("content.add: created component '" + newId + "' of type '" + type + "' on page '" + page.getPath() + "'");
		return null;
	}

	// -------------------------------------------------------------------------
	// content.edit
	// Params: id (required), value (opt), style (opt),
	//         layout (opt), renderer (opt), columnSize (opt), columnStyle (opt)
	// -------------------------------------------------------------------------
	public static String performEdit(RequestService rs, ContentContext ctx, ContentService contentService, PersistenceService persistenceService) throws Exception {
		String id = rs.getParameter("id", null);
		if (id == null || id.trim().isEmpty()) {
			return "content.edit: missing required parameter 'id'";
		}

		IContentVisualComponent comp = contentService.getComponent(ctx, id);
		if (comp == null) {
			return "content.edit: component not found: " + id;
		}

		if (!AdminUserSecurity.getInstance().canModifyConponent(ctx, id)) {
			return "content.edit: access denied for component: " + id;
		}

		boolean modified = false;

		String value = rs.getParameter("value", null);
		if (value != null && !value.equals(comp.getValue(ctx))) {
			comp.setValue(value);
			comp.setNeedRefresh(true);
			modified = true;
		}

		String style = rs.getParameter("style", null);
		if (style != null) {
			comp.setStyle(ctx, style);
			modified = true;
		}

		String layout = rs.getParameter("layout", null);
		if (layout != null) {
			comp.getComponentBean().setLayout(layout.trim().isEmpty() ? null : new ComponentLayout(layout));
			modified = true;
		}

		String renderer = rs.getParameter("renderer", null);
		if (renderer != null) {
			comp.setRenderer(ctx, renderer.trim().isEmpty() ? null : renderer);
			modified = true;
		}

		String columnSize = rs.getParameter("columnSize", null);
		if (columnSize != null && !columnSize.trim().isEmpty()) {
			try {
				comp.setColumnSize(Integer.parseInt(columnSize.trim()));
				modified = true;
			} catch (NumberFormatException e) {
				logger.warning("content.edit: invalid columnSize value '" + columnSize + "' — ignored");
			}
		}

		String columnStyle = rs.getParameter("columnStyle", null);
		if (columnStyle != null) {
			comp.getComponentBean().setColumnStyle(columnStyle);
			modified = true;
		}

		if (modified || comp.isModify()) {
			comp.stored();
		}

		persistenceService.setAskStore(true);

		ctx.getAjaxData().put("component", componentToMap(comp, ctx));
		logger.info("content.edit: updated component '" + id + "'");
		return null;
	}

	// -------------------------------------------------------------------------
	// content.remove
	// Params: id (required)
	// -------------------------------------------------------------------------
	public static String performRemove(RequestService rs, ContentContext ctx, ContentService contentService, PersistenceService persistenceService) throws Exception {
		String id = rs.getParameter("id", null);
		if (id == null || id.trim().isEmpty()) {
			return "content.remove: missing required parameter 'id'";
		}

		IContentVisualComponent comp = contentService.getComponent(ctx, id);
		if (comp == null) {
			return "content.remove: component not found: " + id;
		}

		if (!AdminUserSecurity.getInstance().canModifyConponent(ctx, id)) {
			return "content.remove: access denied for component: " + id;
		}

		String removedType = comp.getType();
		String removedId   = comp.getId();
		comp.getPage().removeContent(ctx, id);

		persistenceService.setAskStore(true);

		ctx.getAjaxData().put("removed", removedId);
		ctx.getAjaxData().put("type", removedType);
		logger.info("content.remove: deleted component '" + removedId + "' of type '" + removedType + "'");
		return null;
	}

	// -------------------------------------------------------------------------
	// content.move
	// Params: id (required), previous (required), area (opt), page (opt)
	// -------------------------------------------------------------------------
	public static String performMove(RequestService rs, ContentContext ctx, ContentService contentService, PersistenceService persistenceService) throws Exception {
		String id       = rs.getParameter("id", null);
		String previous = rs.getParameter("previous", null);

		if (id == null || id.trim().isEmpty()) {
			return "content.move: missing required parameter 'id'";
		}
		if (previous == null || previous.trim().isEmpty()) {
			return "content.move: missing required parameter 'previous' ('0' = insert first)";
		}

		IContentVisualComponent comp = contentService.getComponent(ctx, id);
		if (comp == null) {
			return "content.move: component not found: " + id;
		}

		if (!AdminUserSecurity.getInstance().canModifyConponent(ctx, id)) {
			return "content.move: access denied for component: " + id;
		}

		IContentVisualComponent newPrevious = null;
		if (!"0".equals(previous)) {
			newPrevious = contentService.getComponent(ctx, previous);
			if (newPrevious == null) {
				return "content.move: previous component not found: " + previous;
			}
		}

		String pageRef = rs.getParameter("page", null);
		MenuElement targetPage;
		if (pageRef != null && !pageRef.trim().isEmpty()) {
			targetPage = NavigationHelper.searchPage(ctx, pageRef);
			if (targetPage == null) {
				return "content.move: page not found: " + pageRef;
			}
		} else {
			targetPage = comp.getPage();
		}

		String area = rs.getParameter("area", comp.getArea());

		ComponentHelper.smartMoveComponent(ctx, comp, newPrevious, targetPage, area);
		persistenceService.setAskStore(true);

		ctx.getAjaxData().put("component", componentToMap(comp, ctx));
		logger.info("content.move: moved component '" + id + "' to page '" + targetPage.getPath() + "' area '" + area + "'");
		return null;
	}

	// -------------------------------------------------------------------------
	// content.clearPage
	// Params: page (required)
	// -------------------------------------------------------------------------
	public static String performClearPage(RequestService rs, ContentContext ctx, ContentService contentService, PersistenceService persistenceService) throws Exception {
		String pageRef = rs.getParameter("page", null);
		if (pageRef == null || pageRef.trim().isEmpty()) {
			return "content.clearPage: missing required parameter 'page'";
		}

		MenuElement page = NavigationHelper.searchPage(ctx, pageRef);
		if (page == null) {
			return "content.clearPage: page not found: " + pageRef;
		}

		ComponentBean[] beans = page.getContent();
		if (beans == null || beans.length == 0) {
			ctx.getAjaxData().put("pageId", page.getId());
			ctx.getAjaxData().put("removed", new ArrayList<>());
			ctx.getAjaxData().put("count", 0);
			return null;
		}

		List<String> removedIds = new ArrayList<>();
		for (ComponentBean bean : beans) {
			String id = bean.getId();
			if (id != null && !id.trim().isEmpty()) {
				page.removeContent(ctx, id, false);
				removedIds.add(id);
			}
		}
		page.releaseCache();
		persistenceService.setAskStore(true);

		ctx.getAjaxData().put("pageId", page.getId());
		ctx.getAjaxData().put("removed", removedIds);
		ctx.getAjaxData().put("count", removedIds.size());
		logger.info("content.clearPage: removed " + removedIds.size() + " component(s) from page '" + page.getPath() + "'");
		return null;
	}

	// -------------------------------------------------------------------------
	// content.publish
	// No parameters — publishes the site (syncs preview → view navigation + files)
	// -------------------------------------------------------------------------
	public static String performPublish(
			RequestService rs,
			ContentContext ctx,
			ContentService contentService,
			StaticConfig staticConfig,
			GlobalContext globalContext,
			ServletContext application,
			HttpServletRequest request,
			I18nAccess i18nAccess) throws Exception {

		String result = Edit.performPublish(application, request, staticConfig, globalContext, contentService, ctx, i18nAccess);

		ctx.getAjaxData().put("published", true);
		ctx.getAjaxData().put("publisher", ctx.getCurrentEditUser().getLogin());
		ctx.getAjaxData().put("date", new java.util.Date().toString());
		logger.info("content.publish: published by '" + ctx.getCurrentEditUser().getLogin() + "'");
		return result;
	}

	// -------------------------------------------------------------------------
	// helper
	// -------------------------------------------------------------------------
	private static Map<String, String> componentToMap(IContentVisualComponent comp, ContentContext ctx) {
		Map<String, String> map = new LinkedHashMap<>();
		if (comp == null) return map;
		map.put("id",   comp.getId());
		map.put("type", comp.getType());
		map.put("area", comp.getArea());
		map.put("style", comp.getStyle());
		map.put("layout",      comp.getLayout() != null ? comp.getLayout().getLayout() : null);
		map.put("renderer",    comp.getCurrentRenderer(ctx));
		map.put("columnSize",  String.valueOf(comp.getColumnSize(ctx)));
		map.put("columnStyle", comp.getColumnStyle(ctx));
		if (comp.getPage() != null) {
			map.put("pageId",   comp.getPage().getId());
			map.put("pagePath", comp.getPage().getPath());
		}
		try {
			map.put("value", comp.getValue(ctx));
		} catch (Exception e) {
			logger.fine("content: could not read value of component " + comp.getId() + ": " + e.getMessage());
		}
		return map;
	}
}
