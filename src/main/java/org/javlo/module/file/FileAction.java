package org.javlo.module.file;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.Module;
import org.javlo.module.Module.Box;
import org.javlo.module.Module.HtmlLink;
import org.javlo.module.ModuleContext;

public class FileAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "file";
	}
	
	@Override
	public String prepare(ContentContext ctx, ModuleContext moduleContext) throws Exception {
		FileModuleContext fileModuleContext = FileModuleContext.getInstance(ctx.getRequest().getSession());
		Module currentModule = moduleContext.getCurrentModule();
		
		if (moduleContext.getFromModule() == null) {			
			fileModuleContext.clear();
			moduleContext.getCurrentModule().restoreAll();
		} else {
			if (fileModuleContext.getTitle() != null) {
				Box box = moduleContext.getCurrentModule().getBox("filemanager");
				box.setTitle(box.getTitle()+" : "+fileModuleContext.getTitle());
			}
		}
		return super.prepare(ctx, moduleContext);
	}
	
	public String performBrowse(HttpServletRequest request) {
		request.setAttribute("changeRoot", "true");
		return null;
	}	
	
	public String performUpdateBreadCrumb(HttpSession session, StaticConfig staticConfig, ContentContext ctx, EditContext editContext, Module currentModule, I18nAccess i18nAccess) throws ServletException, IOException {		
		
		FileModuleContext fileModuleContext = FileModuleContext.getInstance(ctx.getRequest().getSession());
		
		currentModule.clearBreadcrump();
		currentModule.setBreadcrumbTitle("");
		
		String[] pathItems = URLHelper.cleanPath(URLHelper.mergePath(staticConfig.getStaticFolder(),fileModuleContext.getPath()), true).split("/");
		String currentPath = "/";
		for (String path : pathItems) {
			if (path.trim().length() > 0) {
				currentPath = currentPath + path + '/';
				
				Map<String, String> filesParams = new HashMap<String, String>();
				filesParams.put("path", currentPath);
				String staticURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams); 
				
				currentModule.pushBreadcrumb(new HtmlLink(staticURL, path, path));
			}
		}

		String componentRenderer = editContext.getBreadcrumbsTemplate();
		String breadcrumbsHTML = ServletHelper.executeJSP(ctx, componentRenderer);
		
		ctx.getAjaxInsideZone().put("breadcrumbs", breadcrumbsHTML);
		return null;
	}
}
