package org.javlo.module.monitoring;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.LogService;
import org.javlo.service.LogService.LogLine;
import org.javlo.service.RequestService;
import org.javlo.utils.TimeTracker;

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

//		if (StringHelper.isTrue(request.getParameter("addRandomLogs"), false)) {
//			Level[] levels = {Level.SEVERE, Level.WARNING, Level.INFO, Level.FINE};
//			LogService logService = LogService.getInstance(request.getSession());
//			int max = (int) (Math.random() * 2);
//			for (int i = 0; i < max; i++) {
//				Level level = levels[(int) (Math.random() * levels.length)];
//				logService.addLine(level, level.getLocalizedName() + " - " + StringHelper.getRandomString((int) (Math.random() * 500)));
//			}
//		}

		List<LogLine> logLines = new LinkedList<LogLine>();
		lastLine = LogService.getInstance(request.getSession()).fillLineList(lastLine, logLines);

		request.setAttribute("logLines", logLines);
		request.setAttribute("logLastLine", lastLine);
		
		double totalTime = (double)TimeTracker.GENERAL_START;
		double currentTime = ((double)new Date().getTime());
		double partTime = (currentTime - totalTime);		
		request.setAttribute("totalPercentage", StringHelper.renderDoubleAsPercentage(TimeTracker.getGeneralTime()/partTime));
		request.setAttribute("totalEventTime", StringHelper.renderTimeInSecond(TimeTracker.getGeneralTime()));
		request.setAttribute("timeEvents", TimeTracker.getTimeEvents());
		if (ctx.isAjax()) {
			ctx.addAjaxZone("log-next-lines", ServletHelper.executeJSP(ctx, currentModule.getPath() + "/jsp/log_content.jsp"));
		}

		return null;
	}
	
	public static String performMainPage(RequestService rs, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		ModulesContext dashboardContext = ModulesContext.getInstance(session, ctx.getGlobalContext());
		dashboardContext.getCurrentModule().restoreAll();
		return null;
	}

	public static String performPerformancePage(RequestService rs, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		ModulesContext dashboardContext = ModulesContext.getInstance(session, ctx.getGlobalContext());
		dashboardContext.getCurrentModule().setRenderer(null);
		dashboardContext.getCurrentModule().clearAllBoxes();
		dashboardContext.getCurrentModule().addMainBox("performance", "performance", "/jsp/performance.jsp", false);
		return null;

	}

}
