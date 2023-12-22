package org.javlo.macro.interactive;

import org.apache.commons.fileupload2.core.FileItem;
import org.htmlparser.Node;
import org.htmlparser.util.NodeList;
import org.javlo.actions.IAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ContentHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Logger;

public class ImportWordpressMacro implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(ImportWordpressMacro.class.getName());

	public static class Config {
		private Config() {
		};

		static Properties getConfig(ContentContext ctx) throws Exception {
			if (ctx.getCurrentTemplate() != null) {
				return ctx.getCurrentTemplate().getMacroProperties(GlobalContext.getInstance(ctx.getRequest()), "import-jcr");
			} else {
				return null;
			}
		}

		static File getImportFolder(ContentContext ctx) throws Exception {
			Properties config = getConfig(ctx);
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			File dir = new File(URLHelper.replaceFolderVariable(ctx, "$HOME/data/import"));
			if (config != null) {
				dir = new File(config.getProperty("folder", staticConfig.replaceFolderVariable("$HOME/data/import")));
			}
			return dir;
		}

		private static List<String> getRefusedFiles(ContentContext ctx) throws Exception {
			File refusedFile = new File(URLHelper.mergePath(getImportFolder(ctx).getAbsolutePath(), "not_imported.txt"));
			return ResourceHelper.loadCollectionFromFile(refusedFile);
		}

		private static void addRefusedFiles(ContentContext ctx, String fileName) throws Exception {
			File refusedFile = new File(URLHelper.mergePath(getImportFolder(ctx).getAbsolutePath(), "not_imported.txt"));
			ResourceHelper.appendStringToFile(refusedFile, fileName);
		}

		private static String getProperty(ContentContext ctx, String key, String defaultValue) {
			Properties prop;
			try {
				prop = getConfig(ctx);
			} catch (Exception e) {
				e.printStackTrace();
				return defaultValue;
			}
			if (prop == null) {
				return defaultValue;
			} else {
				return prop.getProperty(key, defaultValue);
			}
		}

	}

	@Override
	public String getName() {
		return "import-wordpress";
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
	public String getRenderer() {
		return "/jsp/macros/import-wordpress.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		return null;
	}

	@Override
	public String getModalSize() {
		return null;
	}

	@Override
	public String getActionGroupName() {
		return "macro-import-wordpress";
	}

	private static Collection<Node> getAllChildren(Node node) {
		Collection<Node> outNodes = new LinkedList<Node>();
		getAllChildren(node, outNodes);
		return outNodes;
	}

	private static void getAllChildren(Node inNode, Collection<Node> children) {
		NodeList nodeList = inNode.getChildren();
		if (nodeList != null) {
			for (Node node : nodeList.toNodeArray()) {
				children.add(node);
				getAllChildren(node, children);
			}
		}
	}

	public static String performImport(RequestService rs, ContentContext ctx, GlobalContext globalContext, ModulesContext modulesContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws MalformedURLException, Exception {
		if (rs.getAllFileItem().size() != 1) {
			logger.severe("problem with form structure : #fileItem = "+rs.getAllFileItem().size());
			return "problem with form structure : #fileItem = "+rs.getAllFileItem().size();
		} else {
			FileItem fileItem = rs.getAllFileItem().iterator().next();
			try (InputStream in = fileItem.getInputStream()) {
				ContentHelper.importWordPressXML(ctx, in, rs.getParameter("host", null), rs.getParameter("image", null));
			}
		}
		return null;
	}


	@Override
	public boolean isPreview() {
		return true;
	}

	@Override
	public boolean isAdd() {
		return false;
	}

	@Override
	public boolean isInterative() {
		return true;
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return ctx.getCurrentEditUser() != null;
	}

	@Override
	public String getInfo(ContentContext ctx) {
		return null;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void init(ContentContext ctx) {
	}

	@Override
	public String getIcon() {
		return "bi bi-wordpress";
	}
	
	@Override
	public String getUrl() {
		return null;
	}
	
	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}

	@Override
	public int getType() {
		return TYPE_TOOLS;
	}


}
