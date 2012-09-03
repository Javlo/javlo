package org.javlo.module.mailing;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;
import org.javlo.module.content.ContentModuleContext;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;

public class Mailing extends AbstractModuleAction {
	
	public static final String MAILING_FEEDBACK_PARAM_NAME = "_mfb";

	private static Logger logger = Logger.getLogger(Mailing.class.getName());


	@Override
	public String getActionGroupName() {
		return "mailing";
	}

	@Override
	public AbstractModuleContext getModuleContext(HttpSession session, Module module) throws Exception {
		return AbstractModuleContext.getInstance(session, GlobalContext.getSessionInstance(session), module, MailingModuleContext.class);
	}

	/***************/
	/** WEBACTION **/
	/***************/

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {

		String msg = super.prepare(ctx, modulesContext);

		HttpServletRequest request = ctx.getRequest();

		request.setAttribute("previewURL", URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE)));

		return msg;
	}

	public static final String performChangeMode(HttpSession session, RequestService requestService, ContentModuleContext modCtx) {
		modCtx.setMode(Integer.parseInt(requestService.getParameter("mode", "" + ContentModuleContext.EDIT_MODE)));
		return null;
	}


}
