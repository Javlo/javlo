package org.javlo.macro;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ImportHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

public class ImportHTMLPageMacro implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(ImportHTMLPageMacro.class.getName());

	@Override
	public String getName() {
		return "import-html";
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
		return "/jsp/macros/import-html.jsp";
	}

	@Override
	public String getActionGroupName() {
		return "macro-import-html";
	}

	public static String performImport(RequestService rs, ContentContext ctx, EditContext editCtx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws MalformedURLException, Exception {
		String url = rs.getParameter("url", null);
		String titleCSS = rs.getParameter("title", null);
		String contentCSS = rs.getParameter("content", null);
		String imageCSS = rs.getParameter("image", null);
		String dir = rs.getParameter("folder", "imported");

		if (rs.getParameter("import", null) != null) {

			String msg = "";

			if (url == null || titleCSS == null || contentCSS == null || imageCSS == null) {
				return "bad request structure : need 'url', 'title', 'content' and 'image' as parameter.";
			}

			if (ImportHelper.importHTML(ctx, url, new ImportHelper.ContentSelector(titleCSS, imageCSS, contentCSS, dir)) == null) {
				messageRepository.setGlobalMessage(new GenericMessage("page imported.", GenericMessage.INFO));
			}

			if (msg.trim().length() == 0) {
				msg = null;
			}

		} else { // change context
			String context = rs.getParameter("context", null);
			if (context != null) {
				ctx.getRequest().setAttribute("currentContext", context);
				Properties config = getConfig(ctx);
				ctx.getRequest().setAttribute("titleSelector", config.get("selector." + context + ".title"));
				ctx.getRequest().setAttribute("contentSelector", config.get("selector." + context + ".content"));
				ctx.getRequest().setAttribute("imageSelector", config.get("selector." + context + ".image"));
			}

		}

		if (ctx.isEditPreview()) {
			ctx.setClosePopup(true);
		}

		return null;
	}

	static Properties getConfig(ContentContext ctx) throws Exception {
		if (ctx.getCurrentTemplate() != null) {
			return ctx.getCurrentTemplate().getMacroProperties(GlobalContext.getInstance(ctx.getRequest()), "import-html");
		} else {
			return null;
		}
	}

	@Override
	public String prepare(ContentContext ctx) {
		try {
			Properties config = getConfig(ctx);
			if (config != null) {
				List<String> contextList = new LinkedList<String>();
				Collection<Object> keys = config.keySet();
				for (Object key : keys) {
					if (key.toString().startsWith("selector.")) {
						String selectorName = key.toString().split("\\.")[1];
						if (!contextList.contains(selectorName)) {
							contextList.add(selectorName);
						}
					}
				}
				ctx.getRequest().setAttribute("contexts", contextList);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
