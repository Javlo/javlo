package org.javlo.module.monitoring;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.service.log.Log;
import org.javlo.service.log.LogService;
import org.javlo.utils.TimeTracker;

public class MonitoringAction extends AbstractModuleAction {

	private static java.util.logging.Logger logger = java.util.logging.Logger
			.getLogger(MonitoringAction.class.getName());

	@Override
	public String getActionGroupName() {
		return "monitoring";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception {

		HttpServletRequest request = ctx.getRequest();

		Module currentModule = moduleContext.getCurrentModule();

		if (currentModule.getMainBoxes().size() == 0 || !currentModule.getMainBoxes().iterator().next().getName().equalsIgnoreCase("sitelog")) {
			Long lastLine = null;
			if (ctx.isAjax()) {
				lastLine = StringHelper.safeParseLong(request.getParameter("lastLine"), null);
			}
			List<Log> logLines = new LinkedList<Log>();
			lastLine = LogService.getInstance(request.getSession()).fillLineList(lastLine, logLines);

			request.setAttribute("logLines", logLines);
			request.setAttribute("logLastLine", lastLine);

		} else {
			if (request.getParameter("sitelog") != null) {
				request.setAttribute("page", request.getParameter("page"));
				ctx.getGlobalContext().setSiteLog(StringHelper.isTrue(request.getParameter("sitelog")));
				ctx.getGlobalContext().setSiteLogStack(StringHelper.isTrue(request.getParameter("sitelogstack")));
			}
			request.setAttribute("sitelogGroups", ctx.getGlobalContext().getLogList().getGroups());
			request.setAttribute("sitelogLevels", new String[] {Log.INFO, Log.WARNING, Log.SEVERE, Log.TEMPORARY});
			request.setAttribute("logActive", ctx.getGlobalContext().isSiteLog());
			request.setAttribute("logLines", ctx.getGlobalContext().getLogList().getLogs());
		}

		double totalTime = (double) TimeTracker.GENERAL_START;
		double currentTime = ((double) new Date().getTime());
		double partTime = (currentTime - totalTime);
		request.setAttribute("totalPercentage",
				StringHelper.renderDoubleAsPercentage(TimeTracker.getGeneralTime() / partTime));
		request.setAttribute("totalEventTime", StringHelper.renderTimeInSecond(TimeTracker.getGeneralTime()));
		request.setAttribute("timeEvents", TimeTracker.getTimeEvents());
		if (ctx.isAjax()) {
			ctx.addAjaxZone("log-next-lines",
					ServletHelper.executeJSP(ctx, currentModule.getPath() + "/jsp/log_content.jsp"));
		}

		return null;
	}

	public static String performMainPage(RequestService rs, ContentContext ctx, HttpSession session,
			MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		ModulesContext dashboardContext = ModulesContext.getInstance(session, ctx.getGlobalContext());
		dashboardContext.getCurrentModule().restoreAll();
		return null;
	}

	public static String performPerformancePage(RequestService rs, ContentContext ctx, HttpSession session,
			MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		ModulesContext dashboardContext = ModulesContext.getInstance(session, ctx.getGlobalContext());
		dashboardContext.getCurrentModule().setRenderer(null);
		dashboardContext.getCurrentModule().clearAllBoxes();
		dashboardContext.getCurrentModule().addMainBox("performance", "performance", "/jsp/performance.jsp", false);
		ctx.getRequest().setAttribute("page", "performance");
		return null;
	}

	public static String performSitelog(RequestService rs, ContentContext ctx, HttpSession session,
			MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		ModulesContext dashboardContext = ModulesContext.getInstance(session, ctx.getGlobalContext());
		dashboardContext.getCurrentModule().setRenderer(null);
		dashboardContext.getCurrentModule().clearAllBoxes();
		dashboardContext.getCurrentModule().addMainBox("sitelog", "sitelog", "/jsp/sitelog.jsp", false);
		ctx.getRequest().setAttribute("page", "sitelog");
		return null;
	}

}
