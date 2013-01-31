package org.javlo.macro;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.htmlparser.Node;
import org.htmlparser.util.NodeList;
import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

public class ImportZIPPageMacro implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(ImportZIPPageMacro.class.getName());

	@Override
	public String getName() {
		return "import-zip";
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
		return null;
	}

	static Properties getConfig(ContentContext ctx) throws Exception {
		if (ctx.getCurrentTemplate() != null) {
			return ctx.getCurrentTemplate().getMacroProperties(GlobalContext.getInstance(ctx.getRequest()), "import-zip");
		} else {
			return null;
		}
	}

	@Override
	public String prepare(ContentContext ctx) {
		try {
			Properties config = getConfig(ctx);
			if (config != null) {

			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return null;
	}
}
