package org.javlo.actions;

import java.util.List;

import org.javlo.bean.LinkToRenderer;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class AbstractModuleAction implements IModuleAction {

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		if (i18nAccess.getCurrentModule().equals(modulesContext.getCurrentModule())) {
			i18nAccess.setCurrentModule(globalContext, modulesContext.getCurrentModule());			
		}
		return null;
	}
	
	@Override
	public String performSearch(ContentContext ctx, ModulesContext moduleContext, String query) throws Exception {	
		throw new NotImplementedException();
	}
	
	public String performChangeRenderer( RequestService rs, AbstractModuleContext moduleContext, Module currentModule ) {
		String page = rs.getParameter("page", null);
		if (page == null) {
			return "bad request structure : need 'page' parameter.";
		}
		List<LinkToRenderer> links = moduleContext.getNavigation();
		for (LinkToRenderer linkToRenderer : links) {			
			if (page.equals(linkToRenderer.getName())) {				
				moduleContext.setCurrentLink(linkToRenderer.getName());
				moduleContext.setRendererFromNavigation(linkToRenderer.getRenderer());
				return null;
			}
		}		
		return "page not found : "+page;
	}

}
