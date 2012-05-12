package org.javlo.module.template;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.Module;
import org.javlo.module.ModuleContext;
import org.javlo.service.RequestService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class TemplateAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() { 
		return "template";
	}

	@Override
	public String prepare(ContentContext ctx, ModuleContext moduleContext) throws Exception {
		String msg = null;
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Module module = ModuleContext.getInstance(globalContext,ctx.getRequest().getSession()).getCurrentModule();
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		
		Collection<Template> allTemplate = TemplateFactory.getAllDiskTemplates(ctx.getRequest().getSession().getServletContext());
		Collection<Template.TemplateBean> templates = new LinkedList<Template.TemplateBean>();
		for (Template template : allTemplate) {
			if (!template.isTemplateInWebapp(ctx)) {
				template.importTemplateInWebapp(ctx);
			}
			templates.add(new Template.TemplateBean(ctx, template));
		}
		ctx.getRequest().setAttribute("templates", templates);
		
		String templateName = requestService.getParameter("name",null);
		if (templateName != null) {			
			Template template = TemplateFactory.getDiskTemplate(ctx.getRequest().getSession().getServletContext(), templateName, StringHelper.isTrue(ctx.getRequest().getParameter("mailing")));
			if (template == null) {
				msg = "template not found : "+templateName;
				module.clearAllBoxes();
				module.restoreAll();
			} else {
				ctx.getRequest().setAttribute("currentTemplate", new Template.TemplateBean(ctx, template));
			}
		} else {
			module.clearAllBoxes();
			module.restoreAll();
		}
		
		/** choose template if we come from admin module **/
		if (moduleContext.getFromModule() != null && moduleContext.getFromModule().getName().equals("admin")) {
			ctx.getRequest().setAttribute("selectUrl", moduleContext.getFromModule().getBackUrl() );
		}
		
		return msg;
	}
	
	public String performGoEditTemplate(ServletContext application, HttpServletRequest request, ContentContext ctx, RequestService requestService, Module module, I18nAccess i18nAccess) throws IOException {		
		String msg = null;
		Template template = TemplateFactory.getDiskTemplate(application, requestService.getParameter("name",null), StringHelper.isTrue(requestService.getParameter("mailing",null)));
		if (template == null) {
			msg = "template not found : "+requestService.getParameter("name",null);
			module.clearAllBoxes();
			module.restoreAll();
		} else {
			request.setAttribute("currentTemplate", new Template.TemplateBean(ctx, template));
			module.setRenderer(null);
			module.setToolsRenderer(null);
			module.clearAllBoxes();
			module.createMainBox("edit_template", i18nAccess.getText("template.edit.title")+" : "+template.getName(), "/jsp/edit_template.jsp", false);
		}
		return msg;
	}
	
	public String performEditTemplate(ServletContext application, StaticConfig staticConfig, ContentContext ctx, RequestService requestService, Module module, I18nAccess i18nAccess, MessageRepository messageRepository) throws IOException {
		String msg=null;
		Template template = TemplateFactory.getDiskTemplate(application, requestService.getParameter("name",null), StringHelper.isTrue(requestService.getParameter("mailing",null)));
		if (requestService.getParameter("back", null) != null) {
			module.clearAllBoxes();
			module.restoreAll();			
		} else {			
			try {
				template.setAuthors(requestService.getParameter("author", template.getAuthors()));
				Date date = StringHelper.parseDate(requestService.getParameter("creation-date", null), staticConfig.getDefaultDateFormat());				
				template.setCreationDate(date);
				messageRepository.setGlobalMessageAndNotification(ctx,new GenericMessage(i18nAccess.getText("template.message.updated"), GenericMessage.INFO));
			} catch (ParseException e) {
				msg = e.getMessage();
			}			
		}
		return msg;
	}
	
	

}
