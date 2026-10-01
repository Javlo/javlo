package org.javlo.remote;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.data.taxonomy.TaxonomyBean;
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.helper.StringHelper;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;

import java.util.*;
import java.util.logging.Logger;

/**
 * Taxonomy API actions — callable via WebAction or AjaxServlet.
 *
 * The group name is "taxo" because "taxonomy" is already used by the taxonomy
 * admin module (module actions are resolved first).
 *
 * A node reference ("id", "parent", "previous", "target") can be the node id,
 * its path ("geo > be" or "geo/be") or its name when unique. The root node id is "0".
 *
 * Parameters per action:
 *   taxo.get     : id (opt — subtree root, default = whole tree)
 *   taxo.add     : name (required), parent (opt, default = root), id (opt, default = random),
 *                  labels (opt — JSON object {"fr":"...","en":"..."}), decoration (opt),
 *                  previous (opt — insert after this sibling ; "0" = first ; default = last)
 *   taxo.edit    : id (required), name (opt), newId (opt), labels (opt — JSON object, merged,
 *                  an empty value removes the label), decoration (opt, "" to clear)
 *   taxo.remove  : id (required) — removes the node and all its children
 *   taxo.move    : id (required), parent (opt) and/or previous (opt — insert after this sibling) ;
 *                  with parent only, the node is inserted as first child
 *   taxo.export  : — returns the whole tree in text format
 *   taxo.import  : text (required) — replaces the whole tree with the text format
 *                  (one node per line : '>' repeated depth times, then id|name[lang=label,...])
 *
 * The taxonomy of a page is set with nav.edit (taxonomy, taxonomyMode).
 *
 * JSON response (via AjaxServlet): data is placed in ctx.getAjaxData().
 */
public class TaxonomyRemoteAction implements IAction {

	private static final Logger logger = Logger.getLogger(TaxonomyRemoteAction.class.getName());

	@Override
	public String getActionGroupName() {
		return "taxo";
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		if (ctx.getCurrentEditUser() == null) return false;
		User currentUser = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession())
				.getCurrentUser(ctx.getRequest().getSession());
		if (AdminUserSecurity.getInstance().isAdmin(currentUser)) return true;
		return AdminUserSecurity.getInstance().haveRole(ctx.getCurrentEditUser(), AdminUserSecurity.CONTENT_ROLE);
	}

	// -------------------------------------------------------------------------
	// taxo.get
	// -------------------------------------------------------------------------
	public static String performGet(RequestService rs, ContentContext ctx) throws Exception {
		TaxonomyService ts = TaxonomyService.getInstance(ctx);
		TaxonomyBean bean = ts.getRoot();
		String ref = rs.getParameter("id", null);
		if (!StringHelper.isEmpty(ref)) {
			bean = searchBean(ts, ref);
			if (bean == null) {
				return "taxo.get: node not found: " + ref;
			}
		}
		ctx.getAjaxData().put("languages", ctx.getGlobalContext().getContentLanguages());
		ctx.getAjaxData().put("tree", beanToMap(bean, true));
		return null;
	}

	// -------------------------------------------------------------------------
	// taxo.add
	// -------------------------------------------------------------------------
	public static String performAdd(RequestService rs, ContentContext ctx, PersistenceService persistenceService) throws Exception {
		TaxonomyService ts = TaxonomyService.getInstance(ctx);
		String rawName = rs.getParameter("name", null);
		if (StringHelper.isEmpty(rawName)) {
			return "taxo.add: missing required parameter 'name'";
		}
		String name = cleanName(rawName.trim());

		TaxonomyBean parent = ts.getRoot();
		String parentRef = rs.getParameter("parent", null);
		if (!StringHelper.isEmpty(parentRef)) {
			parent = searchBean(ts, parentRef);
			if (parent == null) {
				return "taxo.add: parent node not found: " + parentRef;
			}
		}
		if (parent.searchChildByName(name) != null) {
			return "taxo.add: name '" + name + "' already exists under this parent";
		}

		String id = rs.getParameter("id", null);
		if (StringHelper.isEmpty(id)) {
			id = StringHelper.getRandomId();
		} else if (ts.getTaxonomyBeanMap().containsKey(id)) {
			return "taxo.add: id already exists: " + id;
		}

		Map<String, String> labels;
		try {
			labels = parseLabels(rs.getParameter("labels", null));
		} catch (Exception e) {
			return "taxo.add: bad 'labels' parameter (JSON object expected): " + e.getMessage();
		}
		labels.values().removeIf(StringHelper::isEmpty);
		if (labels.isEmpty() && !rawName.trim().equals(name)) {
			labels.put(ctx.getRequestContentLanguage(), rawName.trim());
		}

		TaxonomyBean newBean = new TaxonomyBean(id, name);
		newBean.setLabels(labels);
		String decoration = rs.getParameter("decoration", null);
		if (!StringHelper.isEmpty(decoration)) {
			newBean.setDecoration(decoration);
		}

		String previousRef = rs.getParameter("previous", null);
		synchronized (ts) {
			if (StringHelper.isEmpty(previousRef)) {
				parent.addChildAsLast(newBean);
			} else if ("0".equals(previousRef)) {
				parent.addChildAsFirst(newBean);
			} else {
				TaxonomyBean previous = searchBean(ts, previousRef);
				if (previous == null || previous.getParent() != parent) {
					return "taxo.add: previous node not found under parent: " + previousRef;
				}
				parent.addChild(newBean, previous.getId());
			}
			ts.clearCache();
		}
		persistenceService.setAskStore(true);

		ctx.getAjaxData().put("node", beanToMap(newBean, false));
		logger.info("taxo.add: created node '" + newBean.getPath() + "' (" + newBean.getId() + ")");
		return null;
	}

	// -------------------------------------------------------------------------
	// taxo.edit
	// -------------------------------------------------------------------------
	public static String performEdit(RequestService rs, ContentContext ctx, PersistenceService persistenceService) throws Exception {
		TaxonomyService ts = TaxonomyService.getInstance(ctx);
		String ref = rs.getParameter("id", null);
		if (StringHelper.isEmpty(ref)) {
			return "taxo.edit: missing required parameter 'id'";
		}
		TaxonomyBean bean = searchBean(ts, ref);
		if (bean == null) {
			return "taxo.edit: node not found: " + ref;
		}

		Map<String, String> labels;
		try {
			labels = parseLabels(rs.getParameter("labels", null));
		} catch (Exception e) {
			return "taxo.edit: bad 'labels' parameter (JSON object expected): " + e.getMessage();
		}

		String rawName = rs.getParameter("name", null);
		if (!StringHelper.isEmpty(rawName)) {
			String name = cleanName(rawName.trim());
			if (bean.getParent() != null && !bean.getName().equals(name) && bean.getParent().searchChildByName(name) != null) {
				return "taxo.edit: name '" + name + "' already exists under this parent";
			}
			bean.setName(name);
		}

		String newId = rs.getParameter("newId", null);
		if (!StringHelper.isEmpty(newId) && !newId.equals(bean.getId())) {
			if (ts.getTaxonomyBeanMap().containsKey(newId)) {
				return "taxo.edit: id already exists: " + newId;
			}
			ts.updateId(bean, newId);
		}

		for (Map.Entry<String, String> label : labels.entrySet()) {
			bean.updateLabel(label.getKey(), label.getValue());
		}
		bean.getLabels().values().removeIf(StringHelper::isEmpty);

		String decoration = rs.getParameter("decoration", null);
		if (decoration != null) {
			bean.setDecoration(StringHelper.isEmpty(decoration) ? null : decoration);
		}

		ts.clearCache();
		persistenceService.setAskStore(true);

		ctx.getAjaxData().put("node", beanToMap(bean, false));
		logger.info("taxo.edit: updated node '" + bean.getPath() + "' (" + bean.getId() + ")");
		return null;
	}

	// -------------------------------------------------------------------------
	// taxo.remove
	// -------------------------------------------------------------------------
	public static String performRemove(RequestService rs, ContentContext ctx, PersistenceService persistenceService) throws Exception {
		TaxonomyService ts = TaxonomyService.getInstance(ctx);
		String ref = rs.getParameter("id", null);
		if (StringHelper.isEmpty(ref)) {
			return "taxo.remove: missing required parameter 'id'";
		}
		TaxonomyBean bean = searchBean(ts, ref);
		if (bean == null) {
			return "taxo.remove: node not found: " + ref;
		}
		if (bean.getParent() == null) {
			return "taxo.remove: cannot remove the root node (use taxo.import to replace the whole tree)";
		}
		String removedPath = bean.getPath();
		synchronized (ts) {
			bean.getParent().removeChild(bean.getId());
			ts.clearCache();
		}
		persistenceService.setAskStore(true);

		ctx.getAjaxData().put("removed", bean.getId());
		ctx.getAjaxData().put("path", removedPath);
		logger.info("taxo.remove: deleted node '" + removedPath + "'");
		return null;
	}

	// -------------------------------------------------------------------------
	// taxo.move
	// -------------------------------------------------------------------------
	public static String performMove(RequestService rs, ContentContext ctx, PersistenceService persistenceService) throws Exception {
		TaxonomyService ts = TaxonomyService.getInstance(ctx);
		String ref = rs.getParameter("id", null);
		if (StringHelper.isEmpty(ref)) {
			return "taxo.move: missing required parameter 'id'";
		}
		TaxonomyBean bean = searchBean(ts, ref);
		if (bean == null) {
			return "taxo.move: node not found: " + ref;
		}
		if (bean.getParent() == null) {
			return "taxo.move: cannot move the root node";
		}

		String parentRef = rs.getParameter("parent", null);
		String previousRef = rs.getParameter("previous", null);
		if (StringHelper.isEmpty(parentRef) && StringHelper.isEmpty(previousRef)) {
			return "taxo.move: 'parent' or 'previous' is required";
		}

		TaxonomyBean parent = null;
		if (!StringHelper.isEmpty(parentRef)) {
			parent = searchBean(ts, parentRef);
			if (parent == null) {
				return "taxo.move: parent node not found: " + parentRef;
			}
		}
		TaxonomyBean previous = null;
		if (!StringHelper.isEmpty(previousRef) && !"0".equals(previousRef)) {
			previous = searchBean(ts, previousRef);
			if (previous == null) {
				return "taxo.move: previous node not found: " + previousRef;
			}
			if (parent != null && previous.getParent() != parent) {
				return "taxo.move: previous node is not a child of parent";
			}
			parent = previous.getParent();
		}
		if (parent == null) {
			parent = bean.getParent();
		}

		for (TaxonomyBean p = parent; p != null; p = p.getParent()) {
			if (p == bean) {
				return "taxo.move: cannot move a node into itself or its descendants";
			}
		}
		if (previous == bean) {
			return "taxo.move: previous cannot be the moved node";
		}
		TaxonomyBean sameName = parent.searchChildByName(bean.getName());
		if (sameName != null && sameName != bean) {
			return "taxo.move: name '" + bean.getName() + "' already exists under the target parent";
		}

		synchronized (ts) {
			bean.getParent().removeChild(bean.getId());
			if (previous == null) {
				parent.addChildAsFirst(bean);
			} else {
				parent.addChild(bean, previous.getId());
			}
			ts.clearCache();
		}
		persistenceService.setAskStore(true);

		ctx.getAjaxData().put("node", beanToMap(bean, false));
		logger.info("taxo.move: moved node '" + bean.getId() + "' to '" + bean.getPath() + "'");
		return null;
	}

	// -------------------------------------------------------------------------
	// taxo.export / taxo.import
	// -------------------------------------------------------------------------
	public static String performExport(ContentContext ctx) throws Exception {
		ctx.getAjaxData().put("text", TaxonomyService.getInstance(ctx).exportAsText());
		return null;
	}

	public static String performImport(RequestService rs, ContentContext ctx, PersistenceService persistenceService) throws Exception {
		String text = rs.getParameter("text", null);
		if (StringHelper.isEmpty(text)) {
			return "taxo.import: missing required parameter 'text'";
		}
		String firstLine = text.trim().split("\\R", 2)[0];
		if (firstLine.startsWith(">") || !firstLine.contains("|")) {
			return "taxo.import: first line must be the root node, ex: '0|root'";
		}
		TaxonomyService ts = TaxonomyService.getInstance(ctx);
		String backup = ts.exportAsText();
		try {
			ts.importText(text.trim());
			if (ts.getRoot() == null) {
				throw new IllegalArgumentException("no root node");
			}
		} catch (RuntimeException e) {
			ts.importText(backup);
			return "taxo.import: invalid text, taxonomy unchanged (" + e.getMessage() + ")";
		}
		persistenceService.setAskStore(true);
		ctx.getAjaxData().put("count", ts.getAllBeans().size() - 1);
		logger.info("taxo.import: taxonomy replaced (" + (ts.getAllBeans().size() - 1) + " nodes)");
		return null;
	}

	// -------------------------------------------------------------------------
	// helpers
	// -------------------------------------------------------------------------

	/** search a node by id, path ("a > b" or "a/b") or unique name. */
	static TaxonomyBean searchBean(TaxonomyService ts, String ref) {
		ref = ref.trim();
		TaxonomyBean bean = ts.getTaxonomyBeanMap().get(ref);
		if (bean != null) {
			return bean;
		}
		String path = ref.replaceAll("^/+|/+$", "").replaceAll("\\s*(/|>)\\s*", " > ");
		TaxonomyBean byName = null;
		int nameCount = 0;
		for (TaxonomyBean b : ts.getAllBeans()) {
			if (b.getParent() == null) continue;
			if (b.getPath().equals(path)) {
				return b;
			}
			if (b.getName().equals(ref)) {
				byName = b;
				nameCount++;
			}
		}
		return nameCount == 1 ? byName : null;
	}

	private static Map<String, String> parseLabels(String json) {
		if (StringHelper.isEmpty(json)) {
			return new HashMap<>();
		}
		Map<String, String> labels = new Gson().fromJson(json, new TypeToken<Map<String, String>>() {}.getType());
		return labels == null ? new HashMap<>() : new HashMap<>(labels);
	}

	private static Map<String, Object> beanToMap(TaxonomyBean bean, boolean recursive) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("id", bean.getId());
		map.put("name", bean.getName());
		map.put("path", bean.getPath());
		map.put("labels", bean.getLabels());
		if (!StringHelper.isEmpty(bean.getDecoration())) {
			map.put("decoration", bean.getDecoration());
		}
		if (bean.getParent() != null) {
			map.put("parentId", bean.getParent().getId());
		}
		if (recursive) {
			List<Map<String, Object>> children = new ArrayList<>();
			for (TaxonomyBean child : bean.getChildren()) {
				children.add(beanToMap(child, true));
			}
			map.put("children", children);
		} else {
			map.put("childrenCount", bean.getChildren().size());
		}
		return map;
	}

	/** same rules as the taxonomy admin module. */
	private static String cleanName(String name) {
		String newName = StringHelper.createFileName(name);
		if (name.startsWith("#")) {
			newName = '#' + newName.substring(1);
		} else if (name.startsWith(">")) {
			newName = '>' + newName.substring(1);
		}
		return newName.replace("-", "_");
	}
}
