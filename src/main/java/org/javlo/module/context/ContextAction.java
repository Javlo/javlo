package org.javlo.module.context;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.admin.AdminAction;
import org.javlo.module.admin.AdminAction.GlobalContextBean;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.user.AdminUserSecurity;

public class ContextAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "context";
	}
	
	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		HttpServletRequest request = ctx.getRequest();		
		ContentContext viewCtx = new ContentContext(ctx);
		Module currentModule = modulesContext.getCurrentModule();
		viewCtx.setRenderMode(ContentContext.VIEW_MODE);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (globalContext.isMaster()) {			
			AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();

			/* breadcrumb */
			if (currentModule.getBreadcrumbList() == null || currentModule.getBreadcrumbList().size() == 0) {
				currentModule.pushBreadcrumb(new Module.HtmlLink(URLHelper.createURL(ctx), I18nAccess.getInstance(request).getText("global.home"), ""));
			}

			Collection<GlobalContextBean> ctxAllBean = new LinkedList<GlobalContextBean>();
			Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(request.getSession().getServletContext());
			Map<String, GlobalContextBeanWithComponents> masterCtx = new HashMap<String, GlobalContextBeanWithComponents>();
			for (GlobalContext context : allContext) {				
				if (ctx.getCurrentEditUser() != null) {
					if (adminUserSecurity.isAdmin(ctx.getCurrentEditUser()) || context.getUsersAccess().contains(ctx.getCurrentEditUser().getLogin())) {
						ContentContext externalCtx = new ContentContext(ctx);
						externalCtx.setRenderMode(ContentContext.VIEW_MODE);
						externalCtx.setForceGlobalContext(context);
						GlobalContextBeanWithComponents contextBean = new GlobalContextBeanWithComponents(externalCtx, request.getSession());
						ctxAllBean.add(contextBean);
						if (context.getAliasOf() == null || context.getAliasOf().length() == 0) {
							masterCtx.put(context.getContextKey(), contextBean);
						}
					}
				}
			}
			for (GlobalContextBean context : ctxAllBean) {
				if (!masterCtx.containsKey(context.getKey()) && masterCtx.containsKey(context.getAliasOf())) {
					masterCtx.get(context.getAliasOf()).addAlias(context);
				}
			}

			List<GlobalContextBean> sortedContext = new LinkedList<AdminAction.GlobalContextBean>(masterCtx.values());
			Collections.sort(sortedContext, new GlobalContextBean.SortOnKey());
			request.setAttribute("contextList", sortedContext);
		}
		return super.prepare(ctx, modulesContext);
	}

}
