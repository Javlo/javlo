package org.javlo.remote;

import org.apache.commons.fileupload2.core.FileItem;
import org.javlo.actions.IAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.service.RequestService;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Template management actions — callable via WebAction or AjaxServlet.
 *
 * Authentication : standard Javlo session OR Authorization: Bearer header OR j_token parameter.
 *
 * Actions:
 *
 *   template.upload:
 *     name  (required) — target template name / ID (created if absent)
 *     url   (opt)      — URL of a .zip file to download and extract
 *     OR multipart file upload field named "file"
 *
 *   template.commit:
 *     name  (required) — template name / ID
 *     Clears the renderer cache and re-imports the template from its source folder.
 *     Equivalent to the commit-template macro.
 *
 *   template.commitAll:
 *     name  (required) — template name / ID
 *     Commits the template AND all its child templates (descendants).
 *     Returns the list of committed template names.
 *
 *   template.download:
 *     name  (required) — template name / ID
 *     Returns a zip file containing the entire template source folder.
 *     Response: application/zip (binary) — NOT a JSON envelope.
 *
 * JSON response (via AjaxServlet): data is placed in ctx.getAjaxData().
 */
public class TemplateAction implements IAction {

	private static final Logger logger = Logger.getLogger(TemplateAction.class.getName());

	@Override
	public String getActionGroupName() {
		return "template";
	}

	/** Requires global admin or template-editor role. */
	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		if (ctx.getCurrentEditUser() == null) return false;
		User currentUser = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession())
				.getCurrentUser(ctx.getRequest().getSession());
		return AdminUserSecurity.getInstance().isAdmin(currentUser);
	}

	// -------------------------------------------------------------------------
	// template.upload
	// Params: name (required), url (opt) or multipart file field "file"
	// -------------------------------------------------------------------------
	public static String performUpload(RequestService rs, ContentContext ctx, StaticConfig staticConfig) throws Exception {
		String name = rs.getParameter("name", null);
		if (name == null || name.trim().isEmpty()) {
			return "template.upload: missing required parameter 'name'";
		}
		name = name.trim();

		String templateFolder = staticConfig.getTemplateFolder();
		InputStream in = null;

		// Priority 1: multipart file upload
		FileItem fileItem = rs.getFileItem("file");
		if (fileItem != null && fileItem.getSize() > 0) {
			in = fileItem.getInputStream();
			try {
				ZipManagement.uploadZipTemplate(templateFolder, in, name);
			} finally {
				in.close();
			}
			logger.info("template.upload: installed template '" + name + "' from multipart upload");
		} else {
			// Priority 2: URL download
			String urlStr = rs.getParameter("url", null);
			if (urlStr == null || urlStr.trim().isEmpty()) {
				return "template.upload: provide either a multipart 'file' field or a 'url' parameter";
			}
			in = new URL(urlStr.trim()).openConnection().getInputStream();
			try {
				ZipManagement.uploadZipTemplate(templateFolder, in, name);
			} finally {
				in.close();
			}
			logger.info("template.upload: installed template '" + name + "' from URL " + urlStr);
		}

		// Reload template from disk so the new files are picked up
		TemplateFactory.clearTemplate(ctx.getRequest().getServletContext());
		Template template = TemplateFactory.getDiskTemplate(ctx.getRequest().getServletContext(), name);
		if (template != null) {
			ctx.getAjaxData().put("template", templateToMap(template));
		} else {
			ctx.getAjaxData().put("name", name);
		}
		return null;
	}

	// -------------------------------------------------------------------------
	// template.commit
	// Params: name (required)
	// -------------------------------------------------------------------------
	public static String performCommit(RequestService rs, ContentContext ctx) throws Exception {
		String name = rs.getParameter("name", null);
		if (name == null || name.trim().isEmpty()) {
			return "template.commit: missing required parameter 'name'";
		}

		Template template = findTemplate(ctx, name.trim());
		if (template == null) {
			return "template.commit: template not found: " + name;
		}

		template.clearRenderer(ctx);

		ctx.getAjaxData().put("template", templateToMap(template));
		logger.info("template.commit: committed template '" + template.getName() + "'");
		return null;
	}

	// -------------------------------------------------------------------------
	// template.commitAll
	// Params: name (required) — commits the template and all its descendants
	// -------------------------------------------------------------------------
	public static String performCommitAll(RequestService rs, ContentContext ctx) throws Exception {
		String name = rs.getParameter("name", null);
		if (name == null || name.trim().isEmpty()) {
			return "template.commitAll: missing required parameter 'name'";
		}

		Template template = findTemplate(ctx, name.trim());
		if (template == null) {
			return "template.commitAll: template not found: " + name;
		}

		// Commit root + all descendants
		Collection<Template> children = TemplateFactory.getTemplateAllChildren(ctx.getRequest().getServletContext(), template);
		List<String> committed = new ArrayList<>();

		template.clearRenderer(ctx);
		committed.add(template.getName());

		for (Template child : children) {
			child.clearRenderer(ctx);
			committed.add(child.getName());
		}

		ctx.getAjaxData().put("committed", committed);
		ctx.getAjaxData().put("count", committed.size());
		logger.info("template.commitAll: committed " + committed.size() + " template(s): " + committed);
		return null;
	}

	// -------------------------------------------------------------------------
	// template.download
	// Params: name (required)
	// Writes the template source folder as a zip directly to the HTTP response.
	// Returns null on success; the response is committed and AjaxServlet skips JSON.
	// -------------------------------------------------------------------------
	public static String performDownload(RequestService rs, ContentContext ctx) throws Exception {
		String name = rs.getParameter("name", null);
		if (name == null || name.trim().isEmpty()) {
			return "template.download: missing required parameter 'name'";
		}
		name = name.trim();

		Template template = findTemplate(ctx, name);
		if (template == null) {
			return "template.download: template not found: " + name;
		}

		java.io.File folder = template.getFolder();
		if (!folder.exists() || !folder.isDirectory()) {
			return "template.download: template folder not found: " + folder.getAbsolutePath();
		}

		HttpServletResponse response = ctx.getResponse();
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + name + ".zip\"");

		ZipManagement.zipDirectory(response.getOutputStream(), folder.getAbsolutePath(), ctx.getRequest());
		response.flushBuffer();

		logger.info("template.download: zipped template '" + name + "' from " + folder.getAbsolutePath());
		return null;
	}

	// -------------------------------------------------------------------------
	// helpers
	// -------------------------------------------------------------------------

	private static Template findTemplate(ContentContext ctx, String name) throws Exception {
		List<Template> templates = TemplateFactory.getAllTemplates(ctx.getRequest().getServletContext());
		for (Template t : templates) {
			if (t.getName().equalsIgnoreCase(name) || t.getId().equalsIgnoreCase(name)) {
				return t;
			}
		}
		return null;
	}

	private static Map<String, String> templateToMap(Template t) {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("id",   t.getId());
		map.put("name", t.getName());
		if (t.getParent() != null) {
			map.put("parent", t.getParent().getName());
		}
		return map;
	}
}
