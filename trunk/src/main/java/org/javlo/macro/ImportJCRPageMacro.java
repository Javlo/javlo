package org.javlo.macro;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.zip.ZipException;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.htmlparser.Node;
import org.htmlparser.util.NodeList;
import org.javlo.actions.IAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ContentHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

public class ImportJCRPageMacro implements IInteractiveMacro, IAction {

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

		private static File getImportFolder(ContentContext ctx) throws Exception {
			Properties config = getConfig(ctx);
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			File dir = new File(URLHelper.replaceFolderVariable(ctx, "$HOME/data/import"));
			if (config != null) {
				dir = new File(config.getProperty("folder", staticConfig.replaceFolderVariable("$HOME/data/import")));
			}
			return dir;
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

		private static String getTitleXPath(ContentContext ctx) {
			return getProperty(ctx, "title.xpath", ".//subTitle/@value");
		}

		private static String getDateXPath(ContentContext ctx) {
			return getProperty(ctx, "date.xpath", ".//articleDate/@value");
		}

		private static String getContentXPath(ContentContext ctx) {
			return getProperty(ctx, "content.xpath", ".//textContent/@value");
		}

		private static String getDateFormat(ContentContext ctx) {
			return getProperty(ctx, "date.format", "yyy-MM-dd'T'HH:mm:ss");
		}

		private static String getPageRoot(ContentContext ctx) {
			return getProperty(ctx, "page.root", "articles");
		}

		public static boolean isExplodHTML(ContentContext ctx) {
			return StringHelper.isTrue(getProperty(ctx, "html.exploded", "true"));
		}

	}

	private static Logger logger = Logger.getLogger(ImportJCRPageMacro.class.getName());

	public static final class Page {
		private final String name;
		private final String label;

		public Page(String name, String label) {
			this.name = name;
			this.label = label;
		}

		public String getName() {
			return name;
		}

		public String getLabel() {
			return label;
		}

	}

	@Override
	public String getName() {
		return "import-jcr";
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
		return "/jsp/macros/import-zip.jsp";
	}

	@Override
	public String getActionGroupName() {
		return "macro-import-zip";
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

	public static String performImport(RequestService rs, ContentContext ctx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws MalformedURLException, Exception {

		File fileToImport = new File(URLHelper.mergePath(Config.getImportFolder(ctx).getAbsolutePath(), rs.getParameter("file", "_____")));
		if (!fileToImport.exists()) {
			return "file not found : " + fileToImport;
		} else {
			importFile(ctx, fileToImport);
		}

		return null;
	}

	private static Locale getLocalBySuffix(String name) {
		if (name.contains("_")) {
			String[] splitted = name.split("_");
			if (splitted.length == 2) {
				if (splitted[1].length() == 2) {
					return new Locale(splitted[1]);
				}
			} else if (splitted.length > 2) {
				if (splitted[splitted.length - 2].length() == 2) {
					return new Locale(splitted[splitted.length - 2], splitted[splitted.length - 1]);
				} else {
					return new Locale(splitted[splitted.length - 1]);
				}
			}
		}
		return null;
	}

	public static String importFile(ContentContext ctx, InputStream in, String name) throws ZipException, IOException {
		return ContentHelper.importJCRFile(ctx, in, name, Config.getTitleXPath(ctx), Config.getDateXPath(ctx), Config.getDateFormat(ctx), Config.getContentXPath(ctx), Config.getPageRoot(ctx), Config.isExplodHTML(ctx));
	}

	private static String importFile(ContentContext ctx, File zip) throws ZipException, IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(zip);
			return importFile(ctx, in, zip.getName());
		} finally {
			ResourceHelper.closeResource(in);
		}
	}

	@Override
	public String prepare(ContentContext ctx) {
		try {
			File dir = Config.getImportFolder(ctx);
			List<Page> pages = new LinkedList<Page>();
			if (dir.exists() && dir.isDirectory()) {
				for (File file : dir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".zip"))) {
					pages.add(new Page(file.getName(), file.getName().replace(".zip", "")));
				}
				ctx.getRequest().setAttribute("pages", pages);
				return null;
			} else {
				return "" + dir + " is not a folder.";
			}

		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	@Override
	public boolean isPreview() {
		return true;
	}

	public static void main(String[] args) {
		System.out.println("***** ImportZIPPageMacro.main : locale = " + getLocalBySuffix("index_fr")); // TODO: remove debug trace
		System.out.println("***** ImportZIPPageMacro.main : locale = " + getLocalBySuffix("index_fr_BE")); // TODO: remove debug trace
		System.out.println("***** ImportZIPPageMacro.main : locale = " + getLocalBySuffix("index_frdfsf")); // TODO: remove debug trace
		System.out.println("***** ImportZIPPageMacro.main : locale = " + getLocalBySuffix("index")); // TODO: remove debug trace
	}
}
