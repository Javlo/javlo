package org.javlo.remote;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.NavigationHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

import java.util.logging.Logger;

/**
 * Navigation API actions — callable via WebAction or AjaxServlet.
 *
 * Authentication : standard Javlo session OR j_token parameter.
 *   If you need Bearer-token support in the Authorization header,
 *   add the header name to globalContext.getSpecialConfig().getSecureHeaderLoginKey().
 *
 * Invocation examples (request parameter or WebAction URL):
 *   webaction=nav.add    — POST /webaction/nav.add   or ?webaction=nav.add
 *   webaction=nav.remove — POST /webaction/nav.remove
 *   webaction=nav.move   — POST /webaction/nav.move
 *
 * Parameters per action:
 *   nav.add    : name (required), parent (opt — id/name/path, default = root), top (opt bool)
 *   nav.remove : path (required — id/name/path)
 *   nav.move   : path (required), parent (required), previousSibling (opt — insert after this sibling)
 *
 * JSON response (via AjaxServlet): data is placed in ctx.getAjaxData().
 */
public class NavAction implements IAction {

	private static final Logger logger = Logger.getLogger(NavAction.class.getName());

	@Override
	public String getActionGroupName() {
		return "nav";
	}

	/**
	 * Access is granted when the user is logged in (edit user).
	 * For Bearer-token calls the CatchAllFilter must have resolved the token to a session first.
	 */
	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return ctx.getCurrentEditUser() != null;
	}

	// -------------------------------------------------------------------------
	// nav.add
	// Params: name (required), parent (opt), top (opt, default false)
	// -------------------------------------------------------------------------
	public static String performAdd(RequestService rs, ContentContext ctx, PersistenceService persistenceService, HttpServletRequest request) throws Exception {
		String name = rs.getParameter("name", null);
		if (name == null || name.trim().isEmpty()) {
			return "nav.add: missing required parameter 'name'";
		}

		String parentRef = rs.getParameter("parent", null);
		boolean top = Boolean.parseBoolean(rs.getParameter("top", "false"));

		MenuElement parentPage;
		if (parentRef != null && !parentRef.trim().isEmpty()) {
			parentPage = NavigationHelper.searchPage(ctx, parentRef);
			if (parentPage == null) {
				return "nav.add: parent page not found: " + parentRef;
			}
		} else {
			parentPage = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx);
		}

		MenuElement newPage = MacroHelper.addPage(ctx, parentPage, name.trim(), top, true);
		if (newPage == null) {
			return "nav.add: page '" + name + "' already exists under this parent";
		}

		persistenceService.setAskStore(true);

		ctx.getAjaxData().put("page", pageToMap(newPage));
		logger.info("nav.add: created page '" + newPage.getPath() + "'");
		return null;
	}

	// -------------------------------------------------------------------------
	// nav.remove
	// Params: path (required — id/name/path)
	// -------------------------------------------------------------------------
	public static String performRemove(RequestService rs, ContentContext ctx, PersistenceService persistenceService, NavigationService navigationService) throws Exception {
		String pageRef = rs.getParameter("path", null);
		if (pageRef == null || pageRef.trim().isEmpty()) {
			return "nav.remove: missing required parameter 'path'";
		}

		MenuElement page = NavigationHelper.searchPage(ctx, pageRef);
		if (page == null) {
			return "nav.remove: page not found: " + pageRef;
		}
		if (page.getParent() == null) {
			return "nav.remove: cannot remove the root page";
		}

		String removedPath = page.getPath();
		navigationService.removeNavigation(ctx, page);
		persistenceService.setAskStore(true);

		ctx.getAjaxData().put("removed", removedPath);
		logger.info("nav.remove: deleted page '" + removedPath + "'");
		return null;
	}

	// -------------------------------------------------------------------------
	// nav.move
	// Params: path (required), parent (required), previousSibling (opt)
	// -------------------------------------------------------------------------
	public static String performMove(RequestService rs, ContentContext ctx, PersistenceService persistenceService) throws Exception {
		String pageRef = rs.getParameter("path", null);
		String parentRef = rs.getParameter("parent", null);

		if (pageRef == null || pageRef.trim().isEmpty()) {
			return "nav.move: missing required parameter 'path'";
		}
		if (parentRef == null || parentRef.trim().isEmpty()) {
			return "nav.move: missing required parameter 'parent'";
		}

		MenuElement page = NavigationHelper.searchPage(ctx, pageRef);
		if (page == null) {
			return "nav.move: page not found: " + pageRef;
		}

		MenuElement newParent = NavigationHelper.searchPage(ctx, parentRef);
		if (newParent == null) {
			return "nav.move: parent page not found: " + parentRef;
		}

		String siblingRef = rs.getParameter("previousSibling", null);
		MenuElement previousSibling = null;
		if (siblingRef != null && !siblingRef.trim().isEmpty()) {
			previousSibling = NavigationHelper.searchPage(ctx, siblingRef);
			if (previousSibling == null) {
				return "nav.move: previousSibling not found: " + siblingRef;
			}
		}

		NavigationHelper.movePage(ctx, newParent, previousSibling, page);
		persistenceService.setAskStore(true);		

		ctx.getAjaxData().put("page", pageToMap(page));
		logger.info("nav.move: moved page '" + page.getName() + "' to parent '" + newParent.getPath() + "'");
		return null;
	}

	// -------------------------------------------------------------------------
	// helpers
	// -------------------------------------------------------------------------
	private static java.util.Map<String, String> pageToMap(MenuElement page) {
		java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
		map.put("id", page.getId());
		map.put("name", page.getName());
		map.put("path", page.getPath());
		if (page.getParent() != null) {
			map.put("parentId", page.getParent().getId());
			map.put("parentPath", page.getParent().getPath());
		}
		return map;
	}
}
