package org.javlo.module.template;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.media.jai.JAI;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.Module;
import org.javlo.module.ModuleContext;
import org.javlo.module.file.FileModuleContext;
import org.javlo.module.template.remote.IRemoteTemplate;
import org.javlo.module.template.remote.IRemoteTemplateFactory;
import org.javlo.module.template.remote.RemoteTemplateFactoryManager;
import org.javlo.service.RequestService;
import org.javlo.servlet.zip.ZipManagement;
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
		Module module = ModuleContext.getInstance(ctx.getRequest().getSession(), globalContext).getCurrentModule();
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		TemplateContext templateContext = TemplateContext.getInstance(ctx.getRequest().getSession(), globalContext, module);

		Collection<Template> allTemplate = TemplateFactory.getAllDiskTemplates(ctx.getRequest().getSession().getServletContext());
		Collection<Template.TemplateBean> templates = new LinkedList<Template.TemplateBean>();
		for (Template template : allTemplate) {
			if (!template.isTemplateInWebapp(ctx)) {
				template.importTemplateInWebapp(ctx);
			}
			templates.add(new Template.TemplateBean(ctx, template));
		}
		ctx.getRequest().setAttribute("templates", templates);

		String templateName = requestService.getParameter("name", null);
		if (templateName != null) {
			Template template = TemplateFactory.getDiskTemplate(ctx.getRequest().getSession().getServletContext(), templateName, StringHelper.isTrue(ctx.getRequest().getParameter("mailing")));
			if (template == null) {
				msg = "template not found : " + templateName;
				module.clearAllBoxes();
				module.restoreAll();
			} else {
				ctx.getRequest().setAttribute("currentTemplate", new Template.TemplateBean(ctx, template));

				Map<String, String> params = new HashMap<String, String>();
				params.put("name", templateName);

				FileModuleContext fileModuleContext = FileModuleContext.getInstance(ctx.getRequest().getSession());
				fileModuleContext.clear();
				fileModuleContext.setRoot(template.getTemplateRealPath());
				fileModuleContext.setTitle("<a href=\"" + URLHelper.createModuleURL(ctx, ctx.getPath(), "template", params) + "\">" + template.getId() + "</a>");

				params.clear();
				params.put("webaction", "browse");
				ctx.getRequest().setAttribute("fileURL", URLHelper.createInterModuleURL(ctx, ctx.getPath(), "file", params));
			}
		} else if (requestService.getParameter("list", null) == null) {
			module.clearAllBoxes();
			module.restoreAll();
		}

		/** choose template if we come from admin module **/
		if (moduleContext.getFromModule() != null && moduleContext.getFromModule().getName().equals("admin")) {
			ctx.getRequest().setAttribute("selectUrl", moduleContext.getFromModule().getBackUrl());
		}

		return msg;
	}

	public String performGoEditTemplate(ServletContext application, HttpServletRequest request, ContentContext ctx, RequestService requestService, Module module, I18nAccess i18nAccess) throws IOException {
		String msg = null;
		Template template = TemplateFactory.getDiskTemplate(application, requestService.getParameter("name", null), StringHelper.isTrue(requestService.getParameter("mailing", null)));
		if (template == null) {
			msg = "template not found : " + requestService.getParameter("name", null);
			module.clearAllBoxes();
			module.restoreAll();
		} else {
			request.setAttribute("currentTemplate", new Template.TemplateBean(ctx, template));
			module.setRenderer(null);
			//module.setToolsRenderer(null);
			module.clearAllBoxes();
			try {
				template.getRenderer(ctx); // prepare ids list
			} catch (BadXMLException e) {
				e.printStackTrace();
			}
			module.createMainBox("edit_template", i18nAccess.getText("template.edit.title") + " : " + template.getName(), "/jsp/edit_template.jsp", false);
		}
		return msg;
	}

	public String performEditTemplate(ServletContext application, StaticConfig staticConfig, ContentContext ctx, RequestService requestService, Module module, I18nAccess i18nAccess, MessageRepository messageRepository) throws IOException {
		String msg = null;
		Template template = TemplateFactory.getDiskTemplate(application, requestService.getParameter("name", null), StringHelper.isTrue(requestService.getParameter("mailing", null)));
		if (requestService.getParameter("back", null) != null) {
			module.clearAllBoxes();
			module.restoreAll();
		} else {
			try {
				template.setAuthors(requestService.getParameter("author", template.getAuthors()));
				Date date = StringHelper.parseDate(requestService.getParameter("creation-date", null), staticConfig.getDefaultDateFormat());
				template.setCreationDate(date);
				messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("template.message.updated"), GenericMessage.INFO));

				Collection<String> areas = template.getAreas();
				for (String area : areas) {
					String areaId = requestService.getParameter("free-area-" + area, "");
					if (areaId.trim().length() == 0) {
						areaId = requestService.getParameter("area-" + area, "");
					}
					if (areaId.trim().length() > 0 && !template.getAreasMap().get(area).equals(areaId)) {
						if (template.getAreasMap().values().contains(areaId)) {
							return i18nAccess.getText("template.error.same-id");
						} else {
							template.setArea(area, areaId);
						}
					}
				}

				String newArea = requestService.getParameter("new-area", "");
				if (newArea.trim().length() > 0) {
					String areaId = requestService.getParameter("free-area-new", "");
					if (areaId.trim().length() == 0) {
						areaId = requestService.getParameter("newarea-id", "");
					}
					if (areaId.trim().length() > 0) {
						if (template.getAreasMap().values().contains(areaId)) {
							return i18nAccess.getText("template.error.same-id");
						} else if (template.getAreasMap().keySet().contains(newArea)) {
							return i18nAccess.getText("template.error.same-area");
						} else {
							template.setArea(newArea, areaId);
						}
					} else {
						return i18nAccess.getText("template.error.choose-id");
					}
				}

			} catch (ParseException e) {
				msg = e.getMessage();
			}
		}
		return msg;
	}

	public String performChangeRenderer(HttpSession session, RequestService requestService, GlobalContext globalContext, Module currentModule, I18nAccess i18nAccess) throws Exception {

		currentModule.restoreAll();

		String list = requestService.getParameter("list", null);
		if (list == null) {
			return "bad request structure : need 'list' as parameter.";
		}
		TemplateContext.getInstance(session, globalContext, currentModule).setCurrentLink(list);
		if (list != null) {
			IRemoteTemplateFactory tempFact = RemoteTemplateFactoryManager.getInstance(session.getServletContext()).getRemoteTemplateFactory(globalContext, list);
			session.setAttribute("templateFactory", tempFact);
			if (tempFact != null) {
				try {
					tempFact.refresh();
				} catch (Throwable e) {
					e.printStackTrace();
					currentModule.restoreAll();
					return e.getMessage();
				}
				currentModule.setRenderer("/jsp/remote_templates.jsp");
				currentModule.createSideBox("sponsors", i18nAccess.getText("global.sponsors"), "/jsp/sponsors.jsp", false);
			} else {
				currentModule.clearAllBoxes();
				currentModule.restoreAll();
			}
		} else {
			currentModule.restoreAll();
			currentModule.clearAllBoxes();
		}
		return null;
	}

	public String performDeleteArea(ServletContext application, RequestService requestService) throws IOException {
		String area = requestService.getParameter("area", null);
		if (area == null) {
			return "bad request structure, need 'area' as parameter.";
		}
		Template template = TemplateFactory.getDiskTemplate(application, requestService.getParameter("name", null), StringHelper.isTrue(requestService.getParameter("mailing", null)));
		template.deleteArea(area);
		return null;
	}

	public String performImport(RequestService requestService, HttpSession session, ContentContext ctx, GlobalContext globalContext, Module currentModule, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String list = requestService.getParameter("list", null);
		String templateName = requestService.getParameter("name", null);
		if (list == null || templateName == null) {
			return "bad request structure : need 'list' and 'name' as parameter.";
		}
		TemplateContext templateContext = TemplateContext.getInstance(session, globalContext, currentModule);
		templateContext.setCurrentLink(list);
		if (list != null) {
			IRemoteTemplateFactory tempFact = RemoteTemplateFactoryManager.getInstance(session.getServletContext()).getRemoteTemplateFactory(globalContext, list);
			IRemoteTemplate template = tempFact.getTemplate(templateName);
			if (template == null) {
				return "template not found : " + templateName;
			}
			Template newTemplate = TemplateFactory.createDiskTemplates(session.getServletContext(), templateName);

			newTemplate.setAuthors(template.getAuthors());

			InputStream in = null;
			OutputStream out = null;
			try {
				URL zipURL = new URL(template.getZipURL());
				in = zipURL.openConnection().getInputStream();
				ZipManagement.uploadZipTemplate(ctx, in, newTemplate.getId(), false);
				in.close();

				URL imageURL = new URL(template.getImageURL());
				File visualFile = new File(URLHelper.mergePath(newTemplate.getTemplateRealPath(), newTemplate.getVisualFile()));
				RenderedImage image = JAI.create("url", imageURL);
				out = new FileOutputStream(visualFile);
				JAI.create("encode", image, out, "png", null);
				out.close();

				messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("template.message.imported", new String[][] { { "name", newTemplate.getId() } }), GenericMessage.INFO));

				templateContext.setCurrentLink(null); // return to local template list.
				currentModule.restoreAll();
				currentModule.clearAllBoxes();

			} catch (Exception e) {
				e.printStackTrace();
				newTemplate.delete();
				return e.getMessage();
			} finally {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			}
		}

		return null;
	}

	public String performValidate(RequestService requestService, HttpSession session, ContentContext ctx) throws IOException {
		Template template = TemplateFactory.getDiskTemplate(session.getServletContext(), requestService.getParameter("name", null), StringHelper.isTrue(requestService.getParameter("mailing", null)));
		if (template == null) {
			Collection<Template> templates;
			if (StringHelper.isTrue(requestService.getParameter("mailing", null))) {
				templates = TemplateFactory.getAllDiskMaillingTemplates(session.getServletContext());
			} else {
				templates = TemplateFactory.getAllDiskTemplates(session.getServletContext());
			}
			for (Template template2 : templates) {
				template2.setValid(true);
			}
		} else {
			template.setValid(true);
		}
		return null;
	}

	public String performDelete(RequestService requestService, HttpSession session, ContentContext ctx) throws IOException {
		Template template = TemplateFactory.getDiskTemplate(session.getServletContext(), requestService.getParameter("name", null), StringHelper.isTrue(requestService.getParameter("mailing", null)));
		if (template != null) {
			template.delete();
		}
		return null;
	}

	public String performCommit(RequestService requestService, ServletContext application, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		Template template = TemplateFactory.getDiskTemplate(application, requestService.getParameter("name", null), StringHelper.isTrue(requestService.getParameter("mailing", null)));
		template.clearRenderer(ctx);
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("template.message.commited", new String[][] { { "name", requestService.getParameter("name", null) } }), GenericMessage.INFO));
		return null;
	}
}
