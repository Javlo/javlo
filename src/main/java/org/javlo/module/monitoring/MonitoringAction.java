package org.javlo.module.monitoring;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.LogService;
import org.javlo.service.LogService.LogLine;

public class MonitoringAction extends AbstractModuleAction {

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MonitoringAction.class.getName());

	@Override
	public String getActionGroupName() {
		return "monitoring";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception {

		HttpServletRequest request = ctx.getRequest();

		Module currentModule = moduleContext.getCurrentModule();

		Long lastLine = null;
		if (ctx.isAjax()) {
			lastLine = StringHelper.safeParseLong(request.getParameter("lastLine"), null);
		}

		List<LogLine> logLines = new LinkedList<LogLine>();
		LogService.getInstance(request.getSession()).fillLineList(lastLine, logLines);

		request.setAttribute("logLines", logLines);
		request.setAttribute("logLastLine", lastLine);
		if (ctx.isAjax()) {
			ctx.addAjaxZone("log-next-lines", ServletHelper.executeJSP(ctx, currentModule.getPath() + "/jsp/log_content.jsp"));
		}

		return null;
	}

}
