package org.javlo.macro.interactive;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.logging.Logger;

public class CreateRedirectionForAllLanguages implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(CreateRedirectionForAllLanguages.class.getName());

	@Override
	public String getName() {
		return "create-redirection-all-lg";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}

	@Override
	public String getActionGroupName() {
		return getName();
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/create-redirection-all-lg.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		return null;
	}

	@Override
	public String getInfo(ContentContext ctx) {
		return null;
	}

	public static String performCreate(RequestService rs, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		String source = rs.getParameter("source", null);
		String target = rs.getParameter("target", null);
		if (StringHelper.isOneEmpty(source, target)) {
			return "error : define a target and a source URL.";
		} else if (!source.contains("#lg#")) {
			return "#lg# need to be defined at least on source URL.";
		} else {
			Writer outStr = new StringWriter();
			BufferedWriter out = new BufferedWriter(outStr);
			for (String lg : ctx.getGlobalContext().getLanguages()) {
				String lgSource = source.replace("#lg#", lg);
				String lgTarget = target.replace("#lg#", lg);
				out.write(lgSource + "=" + lgTarget);
				out.newLine();
			}
			out.close();
			ctx.getRequest().setAttribute("redirect", outStr.toString());
		}
		return null;
	}

	@Override
	public boolean isPreview() {
		return false;
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
	public boolean isActive() {
		return true;
	}

	@Override
	public void init(ContentContext ctx) {
	}

	@Override
	public String getModalSize() {
		return DEFAULT_MAX_MODAL_SIZE;
	}

	@Override
	public String getIcon() {
		return "bi bi-nut";
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
