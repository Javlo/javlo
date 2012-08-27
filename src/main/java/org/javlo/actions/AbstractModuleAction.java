package org.javlo.actions;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.javlo.bean.LinkToRenderer;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.Module.Box;
import org.javlo.module.core.Module.BoxStep;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;

public abstract class AbstractModuleAction implements IModuleAction {
	
	protected static boolean isLightInterface(ContentContext ctx) {
		AdminUserSecurity userSecurity = AdminUserSecurity.getInstance();
		return userSecurity.haveRole(ctx.getCurrentEditUser(), AdminUserSecurity.LIGHT_INTERFACE_ROLE);
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		getModuleContext(ctx.getRequest().getSession(), modulesContext.getCurrentModule()); // load module context
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		if (i18nAccess.getCurrentModule() == null || i18nAccess.getCurrentModule().equals(modulesContext.getCurrentModule())) {
			i18nAccess.setCurrentModule(globalContext, modulesContext.getCurrentModule());			
		}
		if (isLightInterface(ctx)) {
			ctx.getRequest().setAttribute("lightInterface", "true");
		}
		return null;
	}
	
	@Override
	public String performSearch(ContentContext ctx, ModulesContext moduleContext, String query) throws Exception {	
		throw new UnsupportedOperationException();
	}
	
	public AbstractModuleContext getModuleContext(HttpSession session, Module module) throws Exception {
		return null; 
	}
	
	public String performChangeRenderer( RequestService rs, Module currentModule ) throws Exception {		
		String page = rs.getParameter("page", null);
		if (page == null) {
			return "bad request structure : need 'page' parameter.";
		}
		List<LinkToRenderer> links = getModuleContext(rs.getRequest().getSession(), currentModule).getFlatNavigation();		
		for (LinkToRenderer linkToRenderer : links) {		
			if (page.equals(linkToRenderer.getName())) {				
				getModuleContext(rs.getRequest().getSession(), currentModule).setCurrentLink(linkToRenderer.getName());
				getModuleContext(rs.getRequest().getSession(), currentModule).setRendererFromNavigation(linkToRenderer.getRenderer());
				return null;
			}
		}		
		return "page not found : "+page;
	}

	public String performWizard(ContentContext ctx, RequestService rs, Module currentModule, AbstractModuleContext moduleContext) throws Exception {
		String boxName = rs.getParameter("box", null);
		if (boxName == null) {
			return "bad request structure: need 'box' parameter.";
		}
		Box b = currentModule.getBox(boxName);
		if (b.getSteps() == null) {
			return "the box '" + boxName + "' don't have wizard steps.";
		}
		boolean doNext = null != rs.getParameter("next", null);
		boolean doPrevious = null != rs.getParameter("previous", null);
		int step = moduleContext.getWizardStep(boxName);
		if (doNext) {
			step++;
		} else if (doPrevious) {
			step--;
		}
		step = Math.max(step, 1);
		step = Math.min(step, b.getSteps().size());
		moduleContext.setWizardStep(boxName, step);
		BoxStep s = b.getSteps().get(step - 1);
		b.setTitle(s.getTitle());
		b.setRenderer(s.getRenderer());

		if (ctx.isAjax()) {
			b.update(ctx);
		}
		
		return null;
	}

	@Override
	public Boolean haveRight(HttpSession session, User user) throws ModuleException {
		return null;
	}

}
