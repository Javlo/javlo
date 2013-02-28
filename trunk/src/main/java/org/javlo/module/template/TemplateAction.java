package org.javlo.module.template;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.media.jai.JAI;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.ImageConfig;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.module.file.FileModuleContext;
import org.javlo.module.template.remote.IRemoteResourcesFactory;
import org.javlo.module.template.remote.RemoteTemplateFactoryManager;
import org.javlo.navigation.MenuElement;
import org.javlo.remote.IRemoteResource;
import org.javlo.service.RequestService;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.utils.ReadOnlyPropertiesConfigurationMap;
import org.javlo.utils.StructuredProperties;
import org.javlo.ztatic.FileCache;

public class TemplateAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "template";
	}

	public static String getPreviewImageURL(ContentContext ctx, String filter, String area) {
		return null;
	}

	private static List<String> getTextProperties() {
		List<String> textProperties = new LinkedList<String>();
		textProperties.add("width");
		textProperties.add("height");
		textProperties.add("max-width");
		textProperties.add("max-height");
		textProperties.add("margin-top");
		textProperties.add("margin-right");
		textProperties.add("margin-bottom");
		textProperties.add("margin-left");
		textProperties.add("adjust-color");
		textProperties.add("replace-alpha");
		textProperties.add("web2.height");
		textProperties.add("web2.separation");
		textProperties.add("background-color");
		return textProperties;
	}

	private static List<String> getBooleanProperties() {
		List<String> booleanProperties = new LinkedList<String>();
		booleanProperties.add("grayscale");
		booleanProperties.add("add-border");
		booleanProperties.add("crop-resize");
		booleanProperties.add("crystallize");
		booleanProperties.add("edge");
		booleanProperties.add("framing");
		booleanProperties.add("emboss");
		booleanProperties.add("web2");
		booleanProperties.add("round-corner");
		return booleanProperties;

	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception {

		if (ctx.getRequest().getRequestURL().toString().endsWith(".wav")) { // hack for elfinder js
			return null;
		}

		String msg = null;
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Module module = ModulesContext.getInstance(ctx.getRequest().getSession(), globalContext).getCurrentModule();
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		TemplateContext templateContext = TemplateContext.getInstance(ctx.getRequest().getSession(), globalContext, module);

		Collection<Template> allTemplate = TemplateFactory.getAllDiskTemplates(ctx.getRequest().getSession().getServletContext());
		Collection<String> contextTemplates = globalContext.getTemplatesNames();

		Collection<Template.TemplateBean> templates = new LinkedList<Template.TemplateBean>();
		if (templateContext.getCurrentLink().equals(TemplateContext.MY_TEMPLATES_LINK.getUrl())) {
			ctx.getRequest().setAttribute("nobrowse", "true");
		}
		for (Template template : allTemplate) {
			if (!template.isTemplateInWebapp(ctx)) {
				template.importTemplateInWebapp(StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext()), ctx);
			}
			if (!templateContext.getCurrentLink().equals(TemplateContext.MY_TEMPLATES_LINK.getUrl()) || contextTemplates.contains(template.getName())) {
				templates.add(new Template.TemplateBean(ctx, template));
			}
		}
		ctx.getRequest().setAttribute("templates", templates);

		Map<String, String> params = new HashMap<String, String>();
		String templateName = requestService.getParameter("templateid", null);

		if (templateName != null) {
			Template template = TemplateFactory.getDiskTemplate(ctx.getRequest().getSession().getServletContext(), templateName);
			if (template == null) {
				msg = "template not found : " + templateName;
				module.clearAllBoxes();
				module.restoreAll();
			} else {
				ctx.getRequest().setAttribute("currentTemplate", new Template.TemplateBean(ctx, template));
				params.put("templateid", templateName);
				FileModuleContext fileModuleContext = FileModuleContext.getInstance(ctx.getRequest());
				fileModuleContext.clear();
				fileModuleContext.setRoot(template.getTemplateRealPath());
				fileModuleContext.setTitle("<a href=\"" + URLHelper.createModuleURL(ctx, ctx.getPath(), TemplateContext.NAME, params) + "\">" + template.getId() + "</a>");

				if (requestService.getParameter("filter", null) != null && requestService.getParameter("back", null) == null) {

					ImageConfig imageConfig = ImageConfig.getNewInstance(globalContext, ctx.getRequest().getSession(), template);
					ctx.getRequest().setAttribute("filters", imageConfig.getFilters());

					ctx.getRequest().setAttribute("areas", template.getAreas());
					ctx.getRequest().setAttribute("textProperties", getTextProperties());
					ctx.getRequest().setAttribute("booleanProperties", getBooleanProperties());
					ctx.getRequest().setAttribute("allValues", new ReadOnlyPropertiesConfigurationMap(imageConfig.getProperties(), false));
					if (template.getImageConfigFile().exists()) {
						Properties values = new Properties();
						Reader fileReader = new FileReader(template.getImageConfigFile());
						values.load(fileReader);
						fileReader.close();
						ctx.getRequest().setAttribute("values", values);
					}

					module.getMainBoxes().iterator().next().setRenderer("/jsp/images.jsp");

					// module.setRenderer("/jsp/images.jsp");
				} else if (requestService.getParameter("css", null) != null && requestService.getParameter("back", null) == null) {
					module.getMainBoxes().iterator().next().setRenderer("/jsp/css.jsp");
				}

			}
		} else if (requestService.getParameter("list", null) == null) {
			FileModuleContext fileModuleContext = FileModuleContext.getInstance(ctx.getRequest());
			fileModuleContext.clear();
			fileModuleContext.setRoot(globalContext.getStaticConfig().getTemplateFolder());
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			fileModuleContext.setTitle("<a href=\"" + URLHelper.createModuleURL(ctx, ctx.getPath(), TemplateContext.NAME, params) + "\">" + i18nAccess.getText("template.action.browse") + "</a>");
			module.clearAllBoxes();
			module.restoreAll();
		}

		params.clear();
		params.put("webaction", "browse");
		ctx.getRequest().setAttribute("fileURL", URLHelper.createInterModuleURL(ctx, ctx.getPath(), FileModuleContext.MODULE_NAME, params));

		/** choose template if we come from admin module **/
		if (moduleContext.getFromModule() != null && moduleContext.getFromModule().getName().equals("admin")) {
			ctx.getRequest().setAttribute("selectUrl", moduleContext.getFromModule().getBackUrl());
		}
		return msg;
	}

	public String performGoEditTemplate(ServletContext application, HttpServletRequest request, ContentContext ctx, RequestService requestService, Module module, I18nAccess i18nAccess) throws Exception {
		String msg = null;
		Template template = TemplateFactory.getDiskTemplate(application, requestService.getParameter("templateid", null));
		if (template == null) {
			msg = "template not found : " + requestService.getParameter("templateid", null);
			module.clearAllBoxes();
			module.restoreAll();
		} else {
			request.setAttribute("currentTemplate", new Template.TemplateBean(ctx, template));
			module.setRenderer(null);
			// module.setToolsRenderer(null);
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
		Template template = TemplateFactory.getDiskTemplate(application, requestService.getParameter("templateid", null));
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

				template.setImageFiltersRAW(requestService.getParameter("image-filter", template.getImageFiltersRAW()));
				template.setParentName(requestService.getParameter("parent", template.getParentName()));

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
			IRemoteResourcesFactory tempFact = RemoteTemplateFactoryManager.getInstance(session.getServletContext()).getRemoteTemplateFactory(globalContext, list);
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
		Template template = TemplateFactory.getDiskTemplate(application, requestService.getParameter("templateid", null));
		template.deleteArea(area);
		return null;
	}

	public String performImport(RequestService requestService, HttpSession session, ContentContext ctx, GlobalContext globalContext, Module currentModule, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String list = requestService.getParameter("list", null);
		String templateName = requestService.getParameter("templateid", null);
		if (list == null || templateName == null) {
			return "bad request structure : need 'list' and 'name' as parameter.";
		}
		TemplateContext templateContext = TemplateContext.getInstance(session, globalContext, currentModule);
		templateContext.setCurrentLink(list);
		if (list != null) {
			IRemoteResourcesFactory tempFact = RemoteTemplateFactoryManager.getInstance(session.getServletContext()).getRemoteTemplateFactory(globalContext, list);
			IRemoteResource template = tempFact.getResource(templateName);
			if (template == null) {
				return "template not found : " + templateName;
			}
			Template newTemplate = TemplateFactory.createDiskTemplates(session.getServletContext(), templateName);

			newTemplate.setAuthors(template.getAuthors());

			InputStream in = null;
			OutputStream out = null;
			try {
				URL zipURL = new URL(template.getDownloadURL());
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
		Template template = TemplateFactory.getDiskTemplate(session.getServletContext(), requestService.getParameter("templateid", null));
		if (template == null) {
			Collection<Template> templates;
			templates = TemplateFactory.getAllDiskTemplates(session.getServletContext());
			for (Template template2 : templates) {
				template2.setValid(true);
			}
		} else {
			template.setValid(true);
		}
		return null;
	}

	public String performDelete(RequestService requestService, HttpSession session, ContentContext ctx) throws IOException {
		Template template = TemplateFactory.getDiskTemplate(session.getServletContext(), requestService.getParameter("templateid", null));
		if (template != null) {
			template.delete();
		}
		return null;
	}

	public String performCommit(RequestService requestService, ServletContext application, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		Template template = TemplateFactory.getDiskTemplate(application, requestService.getParameter("templateid", null));
		template.clearRenderer(ctx);
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("template.message.commited", new String[][] { { "name", requestService.getParameter("templateid", null) } }), GenericMessage.INFO));
		return null;
	}

	public String performCommitChildren(RequestService requestService, ServletContext application, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		Template template = TemplateFactory.getDiskTemplate(application, requestService.getParameter("templateid", null));
		template.clearRenderer(ctx);
		Collection<Template> children = TemplateFactory.getTemplateAllChildren(application, template);
		for (Template child : children) {
			child.clearRenderer(ctx);
		}
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("template.message.commited", new String[][] { { "name", requestService.getParameter("templateid", null) } }), GenericMessage.INFO));
		return null;
	}

	public static String performChangeFromPreview(RequestService rs, ContentContext ctx, Module currentModule, MessageRepository messageRepository, I18nAccess i18nAccess) {
		return null;
	}

	public static String performSelectTemplate(RequestService rs, ContentContext ctx, EditContext editContext, MenuElement currentPage, MessageRepository messageRepository, I18nAccess i18nAccess) {
		String templateName = rs.getParameter("templateid", null);
		currentPage.setTemplateName(templateName);

		if (editContext.isEditPreview()) {
			ctx.setClosePopup(true);
		}

		return null;
	}

	public static String performUpdateFilter(RequestService rs, ServletContext application, GlobalContext globalContext, HttpSession session, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException, ConfigurationException {
		String filter = rs.getParameter("filter", null);

		if (filter == null) {
			return "need 'filter' as parameter.";
		}
		Template template = TemplateFactory.getDiskTemplate(application, rs.getParameter("templateid", null));
		if (template == null) {
			return "error, template not found.";
		}

		StructuredProperties imageConfig = new StructuredProperties();
		Reader fileReader = new FileReader(template.getImageConfigFile());
		imageConfig.load(fileReader);
		fileReader.close();

		boolean modifiy = false;

		for (String prop : getTextProperties()) {
			String allKey = filter + '.' + prop;
			String val = rs.getParameter(allKey, "").trim();
			if (val.length() == 0) {
				if (rs.getParameter(allKey, null) != null) {
					if (!val.equals(imageConfig.getProperty(allKey))) {
						imageConfig.remove(allKey);
						modifiy = true;
					}
				}
			} else {
				imageConfig.setProperty(allKey, val);
				modifiy = true;
			}
			for (String area : template.getAreas()) {
				String key = filter + '.' + area + '.' + prop;
				val = rs.getParameter(key, "").trim();
				if (val.length() == 0) {
					if (rs.getParameter(key, null) != null) {
						imageConfig.remove(key);
					}
				} else {
					if (!val.equals(imageConfig.getProperty(key))) {
						imageConfig.setProperty(key, val);
						ctx.getRequest().setAttribute("modifiedArea", area);
						modifiy = true;
					}
				}
			}
		}

		for (String prop : getBooleanProperties()) {
			String key = filter + '.' + prop;
			boolean val = rs.getParameter(key, null) != null;

			if (rs.getParameter('_' + key, null) != null) {
				if (val != StringHelper.isTrue(imageConfig.getProperty(key))) {
					imageConfig.setProperty(key, "" + val);
					modifiy = true;
				}
			}

			for (String area : template.getAreas()) {
				key = filter + '.' + area + '.' + prop;
				val = rs.getParameter(key, null) != null;
				if (rs.getParameter('_' + key, null) != null) {
					if (val != StringHelper.isTrue(imageConfig.getProperty(key))) {
						imageConfig.setProperty(key, "" + val);
						modifiy = true;
						ctx.getRequest().setAttribute("modifiedArea", area);
					}
				}
			}
		}

		if (modifiy) {
			FileCache.getInstance(application).clear(globalContext.getContextKey());
			Writer fileWriter = new FileWriter(template.getImageConfigFile());
			imageConfig.store(fileWriter, "template module store.");
			fileWriter.close();
			ImageConfig.getNewInstance(globalContext, session, template);
		}

		return null;
	}

	public static String performEditCSS(RequestService rs, ServletContext application, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		String css = rs.getParameter("css", null);
		if (css == null) {
			return "error : no 'css' param.";
		} else {
			Template template = TemplateFactory.getTemplates(application).get(rs.getParameter("templateid", ""));
			if (template == null) {
				return "template not found";
			} else {

				// store new value
				if (rs.getParameter("text", null) != null) {
					File cssFile = new File(URLHelper.mergePath(template.getSourceFolder().getAbsolutePath(), rs.getParameter("file", "")));
					if (cssFile.exists() && cssFile.isFile()) {
						ResourceHelper.writeStringToFile(cssFile, rs.getParameter("text", null));
					} else {
						return "file not found : " + cssFile;
					}
				}

				// load current value
				File cssFile = new File(URLHelper.mergePath(template.getSourceFolder().getAbsolutePath(), css));
				if (!cssFile.exists()) {
					return "file not found : " + cssFile;
				} else {
					String text = ResourceHelper.loadStringFromFile(cssFile);
					ctx.getRequest().setAttribute("text", text);
				}
			}
		}
		return null;
	}
}
