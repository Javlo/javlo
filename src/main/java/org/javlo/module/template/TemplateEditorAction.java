package org.javlo.module.template;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.fileupload2.core.FileItem;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.*;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.template.*;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.ztatic.FileCache;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class TemplateEditorAction extends AbstractModuleAction {

	public static final List<String> RESERVED_AREA_NAME = Arrays.asList(new String[] { "body", "area", "html", "div", "span" });

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);
		ajaxPrepare(ctx);
		return msg;
	}

	private static void ajaxPrepare(ContentContext ctx) throws Exception {
		List<String> editableTemplateUnvalid = new LinkedList<String>();
		List<String> editableTemplateValid = new LinkedList<String>();
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());

		if (editorContext.getCurrentTemplate() != null && (editorContext.getCurrentTemplate().isValid() || editorContext.getCurrentTemplate().isDeleted())) {
			editorContext.setCurrentTemplate(null);
		}
		User user = ctx.getCurrentEditUser();
		for (Template template : TemplateFactory.getAllTemplates(ctx.getRequest().getSession().getServletContext())) {
			if (template.isEditable() && !template.isValid() && !template.isDeleted()) {
				if (ctx.getGlobalContext().getTemplatesNames().contains(template.getName()) || AdminUserSecurity.getInstance().isAdmin(user)) {
					editableTemplateUnvalid.add(template.getName());
				}
				// choose first template as current template.
				if (editorContext.getCurrentTemplate() == null || editorContext.getCurrentTemplate().isValid() || editorContext.getCurrentTemplate().isDeleted()) {
					editorContext.setCurrentTemplate(template.getName());
				}
			} else if (template.isEditable() && template.isValid()) {
				editableTemplateValid.add(template.getName());
			}
		}
		ctx.getRequest().setAttribute("parentTemplates", editableTemplateValid);

		TemplateEditorContext editorCtx = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		String templateURL = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE));
		if (editorCtx.getCurrentTemplate() != null) {
			editorContext.getCurrentTemplate().resetRows();
			templateURL = URLHelper.addParam(templateURL, Template.FORCE_TEMPLATE_PARAM_NAME, editorCtx.getCurrentTemplate().getId());
			templateURL = URLHelper.addParam(templateURL, "_display-zone", "" + !editorCtx.isShowContent());
			templateURL = URLHelper.addParam(templateURL, "hash", "" + editorCtx.getCurrentTemplate().hashCode());
			templateURL = URLHelper.addParam(templateURL, "html-class", "edit-preview");
			templateURL = URLHelper.addParam(templateURL, "preview-command", "false");
			templateURL = URLHelper.addParam(templateURL, "webaction", "false");
			templateURL = URLHelper.addParam(templateURL, "preview", "false");
			ctx.getRequest().setAttribute("templateURL", templateURL);
		}
		ctx.getRequest().setAttribute("templates", editableTemplateUnvalid);
		if (editorCtx.getCurrentTemplate() != null) {
			Template.TemplateBean templateBean = new Template.TemplateBean(ctx, editorCtx.getCurrentTemplate());
			ctx.getRequest().setAttribute("template", templateBean);			
			ctx.getRequest().setAttribute("fonts", editorCtx.getCurrentTemplate().getFonts());
		}
	}

	@Override
	public String getActionGroupName() {
		return "template-editor";
	}

	public static String performSelectArea(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());

		Template.TemplateBean templateBean = new Template.TemplateBean(ctx, editorContext.getCurrentTemplate());
		ctx.getRequest().setAttribute("template", templateBean);
		ctx.getRequest().setAttribute("fonts", XHTMLHelper.WEB_FONTS);

		if (editorContext.getCurrentTemplate() == null) {
			return "no current template found.";
		}
		String areaName = rs.getParameter("area", "");
		Area newArea = Template.getArea(editorContext.getCurrentTemplate().getRows(), areaName);
		if (newArea == null) {
			return "area not found : " + areaName;
		} else {
			editorContext.setArea(areaName);
			ajaxPrepare(ctx);
			ctx.getAjaxZone().put("template-properties", ServletHelper.executeJSP(ctx, "/modules/template_editor/jsp/properties.jsp"));
			return null;
		}
	}

	public static String performUpdateArea(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		Collection<Row> rows = editorContext.getCurrentTemplate().getRows();
		Area area = Template.getArea(rows, editorContext.getArea().getName());
		String msg = null;
		if (rs.getParameter("delete", null) != null) {
			editorContext.getCurrentTemplate().deleteArea(editorContext.getArea().getName());
			editorContext.setArea(ComponentBean.DEFAULT_AREA);
		} else {
			if (area == null) {
				return "no active area.";
			} else {
				String newName = rs.getParameter("name", "");
				newName = StringHelper.createFileName(newName.trim());
				if (newName.length() > 0) {
					if (newName.length() < 2) {
						msg = i18nAccess.getText("template.message.error.area-to-smapp", "Area min size : 2 chars.");
					} else if (RESERVED_AREA_NAME.contains(newName)) {
						msg = i18nAccess.getText("template.message.error.area-reserved", newName + " is a reserved word.");
					} else if (StringHelper.isDigit(newName)) {
						msg = i18nAccess.getText("template.message.error.area-digit", "Area name can not be a number.");
					} else if (!area.getName().equals(newName)) {
						if (editorContext.getCurrentTemplate().getArea(editorContext.getCurrentTemplate().getRows(), newName) != null) {
							msg = i18nAccess.getText("template.message.error.area-exist", "Area already exists : " + newName);
						} else {
							if (area.getName().equals(ComponentBean.DEFAULT_AREA)) {
								msg = i18nAccess.getText("template.message.error.no-content", "All template need main area : " + ComponentBean.DEFAULT_AREA);
							}
							area.setName(newName);
							editorContext.setArea(newName);
						}
					}
				}
				area.setWidth(rs.getParameter("width", ""));
				area.setHeight(rs.getParameter("height", ""));
				area.setMargin(rs.getParameter("margin", ""));
				area.setPadding(rs.getParameter("padding", ""));
				area.setBorderWidth(rs.getParameter("borderWidth", ""));
				area.setBorderColor(rs.getParameter("borderColor", ""));
				area.setTitleColor(rs.getParameter("titleColor", ""));
				area.setTextColor(rs.getParameter("textColor", ""));
				area.setLinkColor(rs.getParameter("linkColor", ""));
				area.setTextSize(rs.getParameter("textSize", ""));
				area.setFont(rs.getParameter("font", ""));
				area.setBackgroundColor(rs.getParameter("backgroundColor", ""));
				area.setResponsive(""+StringHelper.isTrue(rs.getParameter("responsive", "false")));
				area.setH1Size(rs.getParameter("h1size", ""));
				area.setH2Size(rs.getParameter("h2size", ""));
				area.setH3Size(rs.getParameter("h3size", ""));
				area.setH4Size(rs.getParameter("h4size", ""));
				area.setH5Size(rs.getParameter("h5size", ""));
				area.setH6Size(rs.getParameter("h6size", ""));
				area.setPriority(Integer.parseInt(rs.getParameter("priority", "10")));
				editorContext.getCurrentTemplate().storeRows(rows);
				editorContext.getCurrentTemplate().resetRows();
			}
		}
		editorContext.getCurrentTemplate().reloadConfig(ctx);
		return msg;
	}

	public static String performUpdateRow(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		Collection<Row> rows = editorContext.getCurrentTemplate().getRows();
		Row row = Template.getArea(rows, editorContext.getArea().getName()).getRow();
		if (rs.getParameter("delete", null) != null) {
			editorContext.getCurrentTemplate().deleteRow(editorContext.getArea().getRow().getName());
			editorContext.setArea(ComponentBean.DEFAULT_AREA);
		} else {
			if (row == null) {
				return "no active area.";
			} else {
				if (rs.getParameter("delete", null) != null) {
					editorContext.getCurrentTemplate().deleteRow(row.getName());
				} else {					
					row.setName(rs.getParameter("name", row.getName()));
					row.setWidth(rs.getParameter("width", ""));
					row.setHeight(rs.getParameter("height", ""));
					row.setMargin(rs.getParameter("margin", ""));
					row.setPadding(rs.getParameter("padding", ""));
					row.setBorderWidth(rs.getParameter("borderWidth", ""));
					row.setBorderColor(rs.getParameter("borderColor", ""));
					row.setTitleColor(rs.getParameter("titleColor", ""));
					row.setTextColor(rs.getParameter("textColor", ""));
					row.setTextSize(rs.getParameter("textSize", ""));
					row.setFont(rs.getParameter("font", ""));
					row.setBackgroundColor(rs.getParameter("backgroundColor", ""));
					row.setLinkColor(rs.getParameter("linkColor", ""));
					row.setResponsive(""+StringHelper.isTrue(rs.getParameter("responsive", "false")));
					row.setH1Size(rs.getParameter("h1size", ""));
					row.setH2Size(rs.getParameter("h2size", ""));
					row.setH3Size(rs.getParameter("h3size", ""));
					row.setH4Size(rs.getParameter("h4size", ""));
					row.setH5Size(rs.getParameter("h5size", ""));
					row.setH6Size(rs.getParameter("h6size", ""));
					row.setPriority(Integer.parseInt(rs.getParameter("priority", "10")));
					editorContext.getCurrentTemplate().storeRows(rows);
				}
			}
		}
		editorContext.getCurrentTemplate().reloadConfig(ctx);		
		return null;
	}

	public static String performUpdateStyle(RequestService rs, ServletContext application, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		TemplateStyle style = editorContext.getCurrentTemplate().getStyle();
		boolean parentChange = false;

		if (rs.getParameter("delete", null) != null) {
			if (editorContext.getCurrentTemplate().getParent().getName().equals("default")) {
				return "error you can not delete a basic template.";
			}
			editorContext.getCurrentTemplate().delete();
			editorContext.setCurrentTemplate(null);
			TemplateFactory.clearTemplate(application);
		} else {
			if (style == null) {
				return "no active area.";
			} else {
				style.setWidth(rs.getParameter("width", ""));
				style.setHeight(rs.getParameter("height", ""));
				style.setMargin(rs.getParameter("margin", ""));
				style.setPadding(rs.getParameter("padding", ""));
				style.setBorderWidth(rs.getParameter("borderWidth", ""));
				style.setBorderColor(rs.getParameter("borderColor", ""));
				style.setBorderColor(rs.getParameter("borderColor", ""));
				style.setTitleColor(rs.getParameter("titleColor", ""));
				style.setTextColor(rs.getParameter("textColor", ""));
				style.setLinkColor(rs.getParameter("linkColor", ""));
				style.setTextSize(rs.getParameter("textSize", ""));
				style.setFont(rs.getParameter("font", ""));
				style.setResponsive(""+StringHelper.isTrue(rs.getParameter("responsive", "false")));

				style.setH1Size(rs.getParameter("h1size", ""));
				style.setH2Size(rs.getParameter("h2size", ""));
				style.setH3Size(rs.getParameter("h3size", ""));
				style.setH4Size(rs.getParameter("h4size", ""));
				style.setH5Size(rs.getParameter("h5size", ""));
				style.setH6Size(rs.getParameter("h6size", ""));
				
				editorContext.getCurrentTemplate().setProperty("pagination", ""+StringHelper.isTrue(rs.getParameter("pagination", null)));

				style.setBackgroundColor(rs.getParameter("backgroundColor", ""));
				style.setOuterBackgroundColor(rs.getParameter("outerBackgroundColor", ""));
				String parent = rs.getParameter("parent", null);				
				if (parent != null && !parent.equals(editorContext.getCurrentTemplate().getParentName())) {
					parentChange = true;
					Template template = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(parent);
					if (template != null) {
						style.setEmptyField(template.getStyle());
						editorContext.getCurrentTemplate().setParentName(parent);
					}
				}
				editorContext.getCurrentTemplate().storeStyle(style);
				FileItem file = rs.getFileItem("image");
				if (file != null) {
					InputStream in = file.getInputStream();
					if (in != null) {
						BufferedImage image = ImageIO.read(in);
						in.close();
						if (image != null) {
							Template template = editorContext.getCurrentTemplate();
							ImageIO.write(image, StringHelper.getFileExtension(template.getVisualFile()), template.getVisualAbsoluteFile());
							FileCache fileCache = FileCache.getInstance(application);
							fileCache.clear(ctx.getGlobalContext().getContextKey());
						}
					}
				}
			}
			if (parentChange) {
				editorContext.getCurrentTemplate().clearRenderer(ctx);
			} else {
				editorContext.getCurrentTemplate().reloadConfig(ctx);
			}
		}
		return null;
	}

	public static String performCreateArea(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		editorContext.getCurrentTemplate().addArea(editorContext.getArea().getRow().getName());
		editorContext.getCurrentTemplate().reloadConfig(ctx);
		return null;
	}

	public static String performCreateRow(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		editorContext.getCurrentTemplate().addRow();
		editorContext.getCurrentTemplate().reloadConfig(ctx);
		return null;
	}

	public static String performChangeTemplate(RequestService rs, ServletContext application, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String templateName = rs.getParameter("template", "");
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());				
		editorContext.setCurrentTemplate(templateName);
		return null;
	}
	
	public static String performSnapshot(RequestService rs, StaticConfig staticConfig, ContentContext ctx, ServletContext application, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		String base64Data = ""+rs.getParameterMap().get("visual");
		if (base64Data.length()>0) {
			JavaScriptBlob blob = new JavaScriptBlob(base64Data);
			if (blob.getContentType().equalsIgnoreCase("image/png")) {
				TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
				Template template = editorContext.getCurrentTemplate();	
				ResourceHelper.writeBytesToFile(template.getVisualAbsoluteFile(), blob.getData());
				FileCache fileCache = FileCache.getInstance(application);
				fileCache.clear(ctx.getGlobalContext().getContextKey());
				template.importTemplateInWebapp(staticConfig, ctx);
			}
		} else {
			return "no data";
		}
		return null;
	}

	public static String performCreateTemplate(RequestService rs, ContentContext ctx, StaticConfig staticConfig, ServletContext application, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String templateName = rs.getParameter("template", "");
		if (templateName.trim().length() < 3) {
			return "template name must be at least 4 chars.";
		} else if (TemplateFactory.getDiskTemplate(application, templateName) != null) {
			return "template name '" + templateName + "' already exist.";
		} else {
			Template newTemplate = TemplateFactory.createDiskTemplates(application, templateName, null);
			newTemplate.setParentName(staticConfig.getDefaultParentEditableTemplate());
			Row row = new Row(newTemplate);
			row.setName("row-0001");
			Area area = new Area();
			area.setName("content");
			row.addArea(area);
			List<Row> rows = new LinkedList<Row>();
			rows.add(row);
			newTemplate.storeRows(rows);
			TemplateFactory.clearTemplate(application);
			Template template = TemplateFactory.getTemplates(application).get(newTemplate.getName());
			TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
			editorContext.setCurrentTemplate(newTemplate.getName());
			template.importTemplateInWebapp(staticConfig, ctx);

		}
		return null;
	}

	public static String performShowContent(RequestService rs, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) {
		TemplateEditorContext editorCtx = TemplateEditorContext.getInstance(session);
		editorCtx.setShowContent(StringHelper.isTrue(rs.getParameter("show-content", null)));
		return null;
	}

}

