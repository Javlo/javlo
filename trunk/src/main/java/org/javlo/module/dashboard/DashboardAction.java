package org.javlo.module.dashboard;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.NotificationService;

public class DashboardAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "dashboard";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);		
		
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());		
		NotificationService notificationService = NotificationService.getInstance(globalContext);
		ctx.getRequest().setAttribute("notification", notificationService.getNotifications(9999));
		
		return msg;
	}

}
