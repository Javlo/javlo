package org.javlo.remote;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.apache.commons.lang3.StringUtils;
import org.javlo.data.taxonomy.TaxonomyBean;
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.module.content.Edit;
import org.javlo.helper.NavigationHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.shared.ISharedContentProvider;
import org.javlo.service.shared.JavloSharedContentProvider;
import org.javlo.service.shared.SharedContentService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
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
 *   nav.get    : path (required) — returns the page properties (incl. taxonomy)
 *   nav.edit   : path (required) + any page property to change (partial update, see performEdit),
 *                incl. taxonomy (comma separated node references) and taxonomyMode (replace|add|remove)
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
	public static String performRemove(RequestService rs, ContentContext ctx, PersistenceService persistenceService) throws Exception {
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
		// NavigationService is not injectable by ActionManager (would be null)
		NavigationService.getInstance(ctx.getGlobalContext()).removeNavigation(ctx, page);
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
	// nav.get
	// Params: path (required — id/name/path)
	// -------------------------------------------------------------------------
	public static String performGet(RequestService rs, ContentContext ctx) throws Exception {
		String pageRef = rs.getParameter("path", null);
		if (pageRef == null || pageRef.trim().isEmpty()) {
			return "nav.get: missing required parameter 'path'";
		}
		MenuElement page = NavigationHelper.searchPage(ctx, pageRef);
		if (page == null) {
			return "nav.get: page not found: " + pageRef;
		}
		ctx.getAjaxData().put("page", propertiesToMap(ctx, page));
		return null;
	}

	// -------------------------------------------------------------------------
	// nav.edit — partial update of the page properties : only the given parameters are changed.
	// Params: path (required), name, visible, active, model, admin, breakRepeat,
	//         childrenAssociation, type, seoWeight, sharedName, template ("" = inherited),
	//         startPublish, endPublish ("" = none), ipSecurity ("" = none),
	//         userRoles (comma separated, "" = none), userRolesInherited, noValidation (admin only),
	//         taxonomy (comma separated node references, "" = none), taxonomyMode (replace|add|remove)
	// -------------------------------------------------------------------------
	public static String performEdit(RequestService rs, ContentContext ctx, PersistenceService persistenceService) throws Exception {
		String pageRef = rs.getParameter("path", null);
		if (pageRef == null || pageRef.trim().isEmpty()) {
			return "nav.edit: missing required parameter 'path'";
		}
		MenuElement page = NavigationHelper.searchPage(ctx, pageRef);
		if (page == null) {
			return "nav.edit: page not found: " + pageRef;
		}
		if (!Edit.checkPageSecurity(ctx, page)) {
			return "nav.edit: no right on page: " + page.getName();
		}
		if (page.isBlocked() && !page.getBlocker().equals(ctx.getCurrentEditUser().getLogin())) {
			return "nav.edit: page is blocked by " + page.getBlocker();
		}

		/* validation first : nothing is changed if a parameter is invalid */
		String newName = rs.getParameter("name", null);
		if (newName != null) {
			newName = newName.trim();
			if (newName.isEmpty() || newName.contains(".") || newName.contains(" ")) {
				return "nav.edit: page name cannot be empty or contain space and '.'";
			}
			if (!newName.equals(page.getName()) && ContentService.getInstance(ctx.getRequest()).getNavigation(ctx).searchChildFromName(newName) != null) {
				return "nav.edit: page name already exists: " + newName;
			}
			if (page.getParent() == null && !newName.equals(page.getName())) {
				return "nav.edit: cannot rename the root page";
			}
		}
		String seoWeight = rs.getParameter("seoWeight", null);
		if (!StringHelper.isEmpty(seoWeight) && !StringHelper.isDigit(seoWeight)) {
			return "nav.edit: seoWeight must be an integer";
		}
		Template template = null;
		String templateName = rs.getParameter("template", null);
		if (!StringHelper.isEmpty(templateName)) {
			template = TemplateFactory.getTemplates(ctx.getRequest().getServletContext()).get(templateName);
			if (template == null || !ctx.getCurrentTemplates().contains(template)) {
				return "nav.edit: template not found: " + templateName;
			}
		}
		Date startPublish = null;
		Date endPublish = null;
		String startPublishParam = rs.getParameter("startPublish", null);
		if (!StringHelper.isEmpty(startPublishParam)) {
			startPublish = StringHelper.smartParseDate(startPublishParam.trim());
			if (startPublish == null) {
				return "nav.edit: bad date for startPublish: " + startPublishParam;
			}
		}
		String endPublishParam = rs.getParameter("endPublish", null);
		if (!StringHelper.isEmpty(endPublishParam)) {
			endPublish = StringHelper.smartParseDate(endPublishParam.trim());
			if (endPublish == null) {
				return "nav.edit: bad date for endPublish: " + endPublishParam;
			}
		}
		String ipSecurity = rs.getParameter("ipSecurity", null);
		if (!StringHelper.isEmpty(ipSecurity) && ContentService.getInstance(ctx.getRequest()).getNavigation(ctx).searchChildFromName(ipSecurity) == null) {
			return "nav.edit: ipSecurity page not found: " + ipSecurity;
		}
		Set<String> taxonomy = null;
		String taxonomyParam = rs.getParameter("taxonomy", null);
		if (taxonomyParam != null) {
			String mode = rs.getParameter("taxonomyMode", "replace");
			if (!"replace".equals(mode) && !"add".equals(mode) && !"remove".equals(mode)) {
				return "nav.edit: bad taxonomyMode '" + mode + "' (replace, add or remove)";
			}
			TaxonomyService ts = TaxonomyService.getInstance(ctx);
			Set<String> ids = new LinkedHashSet<>();
			for (String ref : StringHelper.stringToCollection(taxonomyParam, ",")) {
				if (StringHelper.isEmpty(ref)) continue;
				TaxonomyBean bean = TaxonomyRemoteAction.searchBean(ts, ref.trim());
				if (bean == null) {
					return "nav.edit: taxonomy node not found: " + ref;
				}
				ids.add(bean.getId());
			}
			taxonomy = new LinkedHashSet<>();
			if (!"replace".equals(mode) && page.getTaxonomy() != null) {
				taxonomy.addAll(page.getTaxonomy());
			}
			if ("remove".equals(mode)) {
				taxonomy.removeAll(ids);
			} else {
				taxonomy.addAll(ids);
			}
		}

		/* update */
		if (newName != null && !newName.equals(page.getName())) {
			String oldName = page.getName();
			if (page.isRootChildrenAssociation()) {
				for (MenuElement child : page.getAllChildrenList()) {
					child.setName(StringUtils.replaceOnce(child.getName(), oldName, newName));
				}
			}
			page.setName(newName);
		}
		if (rs.getParameter("visible", null) != null) {
			page.setVisible(StringHelper.isTrue(rs.getParameter("visible", null)));
		}
		if (rs.getParameter("active", null) != null) {
			page.setActive(StringHelper.isTrue(rs.getParameter("active", null)));
		}
		if (rs.getParameter("model", null) != null) {
			page.setModel(StringHelper.isTrue(rs.getParameter("model", null)));
		}
		if (rs.getParameter("admin", null) != null) {
			page.setAdmin(StringHelper.isTrue(rs.getParameter("admin", null)));
		}
		if (rs.getParameter("breakRepeat", null) != null) {
			page.setBreakRepeat(StringHelper.isTrue(rs.getParameter("breakRepeat", null)));
		}
		if (rs.getParameter("childrenAssociation", null) != null) {
			page.setChildrenAssociation(StringHelper.isTrue(rs.getParameter("childrenAssociation", null)));
		}
		if (rs.getParameter("type", null) != null) {
			page.setType(rs.getParameter("type", null));
		}
		if (seoWeight != null) {
			page.setSeoWeight(StringHelper.isEmpty(seoWeight) ? MenuElement.SEO_HEIGHT_INHERITED : Integer.parseInt(seoWeight));
		}
		String sharedName = rs.getParameter("sharedName", null);
		if (sharedName != null) {
			page.setSharedName(sharedName);
			ISharedContentProvider provider = SharedContentService.getInstance(ctx).getProvider(ctx, JavloSharedContentProvider.NAME);
			if (provider != null) {
				provider.refresh(ctx);
			}
		}
		if (templateName != null) {
			page.setTemplateId(template == null ? null : template.getName());
			ctx.setCurrentTemplate(null);
		}
		if (startPublishParam != null) {
			page.setStartPublishDate(startPublish);
		}
		if (endPublishParam != null) {
			page.setEndPublishDate(endPublish);
		}
		if (ipSecurity != null) {
			page.setIpSecurityErrorPageName(StringHelper.isEmpty(ipSecurity) ? null : ipSecurity);
		}
		String userRoles = rs.getParameter("userRoles", null);
		if (userRoles != null) {
			// explicit roles are only effective when not inherited from the parent
			if (rs.getParameter("userRolesInherited", null) == null) {
				page.setUserRolesInherited(false);
			}
			Set<String> roles = new HashSet<>();
			for (String role : StringHelper.stringToCollection(userRoles, ",")) {
				if (!StringHelper.isEmpty(role)) roles.add(role.trim());
			}
			page.setUserRoles(roles);
		}
		if (rs.getParameter("userRolesInherited", null) != null) {
			page.setUserRolesInherited(StringHelper.isTrue(rs.getParameter("userRolesInherited", null)));
		}
		if (rs.getParameter("noValidation", null) != null && AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser())) {
			page.setNoValidation(StringHelper.isTrue(rs.getParameter("noValidation", null)));
		}
		if (taxonomy != null) {
			page.setTaxonomy(new HashSet<>(taxonomy));
		}

		page.setModificationDate(new Date());
		page.setLatestEditor(ctx.getCurrentEditUser().getLogin());
		page.setValid(false);
		page.setNeedValidation(false);
		page.releaseCache();
		page.clearPageBean(ctx);
		persistenceService.setAskStore(true);

		ctx.getAjaxData().put("page", propertiesToMap(ctx, page));
		logger.info("nav.edit: updated properties of page '" + page.getPath() + "'");
		return null;
	}

	// -------------------------------------------------------------------------
	// helpers
	// -------------------------------------------------------------------------
	private static java.util.Map<String, Object> propertiesToMap(ContentContext ctx, MenuElement page) throws Exception {
		java.util.Map<String, Object> map = new java.util.LinkedHashMap<>(pageToMap(page));
		map.put("visible", page.isVisible());
		map.put("active", page.isActive());
		map.put("model", page.isModel());
		map.put("admin", page.isAdmin());
		map.put("breakRepeat", page.isBreakRepeat());
		map.put("childrenAssociation", page.isChildrenAssociation());
		map.put("type", page.getType());
		map.put("seoWeight", page.getSeoWeight());
		map.put("sharedName", page.getSharedName());
		map.put("template", page.getTemplateId());
		map.put("startPublish", page.getStartPublishDate() == null ? null : StringHelper.renderSortableTime(page.getStartPublishDate()));
		map.put("endPublish", page.getEndPublishDate() == null ? null : StringHelper.renderSortableTime(page.getEndPublishDate()));
		map.put("ipSecurity", page.getIpSecurityErrorPageName());
		map.put("userRoles", page.getUserRoles());
		map.put("userRolesInherited", page.isUserRolesInherited());
		map.put("noValidation", page.isNoValidation());
		java.util.List<java.util.Map<String, String>> taxonomy = new java.util.ArrayList<>();
		if (page.getTaxonomy() != null) {
			TaxonomyService ts = TaxonomyService.getInstance(ctx);
			for (String id : page.getTaxonomy()) {
				java.util.Map<String, String> t = new java.util.LinkedHashMap<>();
				t.put("id", id);
				TaxonomyBean bean = ts.getTaxonomyBeanMap().get(id);
				t.put("path", bean == null ? null : bean.getPath());
				taxonomy.add(t);
			}
		}
		map.put("taxonomy", taxonomy);
		return map;
	}

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
