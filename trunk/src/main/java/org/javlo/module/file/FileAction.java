package org.javlo.module.file;

import java.util.HashMap;
import java.util.Map;

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
import org.javlo.service.RequestService;

public class FileAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "file";
	}
	
	@Override
	public String prepare(ContentContext ctx, ModuleContext moduleContext) throws Exception {
		FileModuleContext fileModuleContext = FileModuleContext.getInstance(ctx.getRequest().getSession());
		
		if (moduleContext.getFromModule() == null && ctx.getRequest().getParameter("changeRoot") == null) {			
			fileModuleContext.clear();
			moduleContext.getCurrentModule().restoreAll();
		} else {
			if (fileModuleContext.getTitle() != null) {				
				moduleContext.getCurrentModule().restoreAll();
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
	
	public String performUpdateBreadCrumb(RequestService rs, HttpSession session, StaticConfig staticConfig, ContentContext ctx, EditContext editContext, ModuleContext moduleContext, Module currentModule, I18nAccess i18nAccess) throws Exception {		
		
		FileModuleContext fileModuleContext = FileModuleContext.getInstance(ctx.getRequest().getSession());
		
		currentModule.clearBreadcrump();
		currentModule.setBreadcrumbTitle("");
		
		String[] pathItems = URLHelper.cleanPath(URLHelper.mergePath(fileModuleContext.getPath()), true).split("/");
		String currentPath = "/";
		for (String path : pathItems) {
			if (path.trim().length() > 0) {
				currentPath = currentPath + path + '/';
				
				Map<String, String> filesParams = new HashMap<String, String>();
				filesParams.put("path", currentPath);
				if (rs.getParameter("changeRoot", null) != null) {
					filesParams.put("changeRoot", "true");
				}
				String staticURL;
				if (moduleContext.getFromModule() != null) {
					staticURL = URLHelper.createInterModuleURL(ctx, ctx.getPath(), "file", moduleContext.getFromModule().getName(), filesParams);
				} else {
					staticURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);
				}
				
				currentModule.pushBreadcrumb(new HtmlLink(staticURL, path, path));
			}
		}

		String componentRenderer = editContext.getBreadcrumbsTemplate();
		String breadcrumbsHTML = ServletHelper.executeJSP(ctx, componentRenderer);
		
		ctx.getAjaxInsideZone().put("breadcrumbs", breadcrumbsHTML);
		return null;
	}
}
