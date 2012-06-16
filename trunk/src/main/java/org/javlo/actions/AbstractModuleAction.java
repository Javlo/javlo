package org.javlo.actions;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.javlo.bean.LinkToRenderer;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.user.User;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class AbstractModuleAction implements IModuleAction {

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		getModuleContext(ctx.getRequest().getSession(), modulesContext.getCurrentModule()); // load module context
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
	
	@Override
	public Boolean haveRight(HttpSession session, User user) throws ModuleException {
		return null;
	}

}
