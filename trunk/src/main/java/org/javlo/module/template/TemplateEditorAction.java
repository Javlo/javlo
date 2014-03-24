package org.javlo.module.template;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.template.Area;
import org.javlo.template.Row;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.template.TemplateStyle;
import org.javlo.ztatic.FileCache;

public class TemplateEditorAction extends AbstractModuleAction {

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);

		List<String> editableTemplate = new LinkedList<String>();
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());

		for (Template template : TemplateFactory.getAllTemplates(ctx.getRequest().getSession().getServletContext())) {
			if (template.isEditable() && !template.isValid()) {
				editableTemplate.add(template.getName());
				// choose first template as current template.
				if (editorContext.getCurrentTemplate() == null) {
					editorContext.setCurrentTemplate(template);
				}
			}
		}

		TemplateEditorContext editorCtx = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		String templateURL = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE));
		if (editorCtx.getCurrentTemplate() != null) {
			editorContext.getCurrentTemplate().resetRows();
			templateURL = URLHelper.addParam(templateURL, Template.FORCE_TEMPLATE_PARAM_NAME, editorCtx.getCurrentTemplate().getId());
			templateURL = URLHelper.addParam(templateURL, "_display-zone", "" + !editorCtx.isShowContent());
			ctx.getRequest().setAttribute("templateURL", templateURL);
		}
		ctx.getRequest().setAttribute("templates", editableTemplate);

		Template.TemplateBean templateBean = new Template.TemplateBean(ctx, editorCtx.getCurrentTemplate());
		ctx.getRequest().setAttribute("template", templateBean);
		ctx.getRequest().setAttribute("fonts", XHTMLHelper.WEB_FONTS);

		return msg;
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
			ctx.getAjaxZone().put("template-properties", ServletHelper.executeJSP(ctx, "/modules/template_editor/jsp/properties.jsp"));
			return null;
		}
	}

	public static String performUpdateArea(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		Collection<Row> rows = editorContext.getCurrentTemplate().getRows();
		Area area = Template.getArea(rows, editorContext.getArea().getName());
		if (rs.getParameter("delete", null) != null) {
			editorContext.getCurrentTemplate().deleteArea(editorContext.getArea().getName());
			editorContext.setArea(ComponentBean.DEFAULT_AREA);
		} else {
			if (area == null) {
				return "no active area.";
			} else {
				area.setWidth(rs.getParameter("width", ""));
				area.setHeight(rs.getParameter("height", ""));
				area.setMargin(rs.getParameter("margin", ""));
				area.setPadding(rs.getParameter("padding", ""));
				area.setBorderWidth(rs.getParameter("borderWidth", ""));
				area.setBorderColor(rs.getParameter("borderColor", ""));
				area.setTextColor(rs.getParameter("textColor", ""));
				area.setTextSize(rs.getParameter("textSize", ""));
				area.setFont(rs.getParameter("font", ""));
				area.setBackgroundColor(rs.getParameter("backgroundColor", ""));
				editorContext.getCurrentTemplate().storeRows(rows);
			}
		}
		editorContext.getCurrentTemplate().clearRenderer(ctx);
		return null;
	}

	public static String performUpdateRow(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		Collection<Row> rows = editorContext.getCurrentTemplate().getRows();
		Row row = Template.getArea(rows, editorContext.getArea().getName()).getRow();
		if (row == null) {
			return "no active area.";
		} else {
			if (rs.getParameter("delete", null) != null) {
				editorContext.getCurrentTemplate().deleteRow(row.getName());
			} else {
				row.setWidth(rs.getParameter("width", ""));
				row.setHeight(rs.getParameter("height", ""));
				row.setMargin(rs.getParameter("margin", ""));
				row.setPadding(rs.getParameter("padding", ""));
				row.setBorderWidth(rs.getParameter("borderWidth", ""));
				row.setBorderColor(rs.getParameter("borderColor", ""));
				row.setTextColor(rs.getParameter("textColor", ""));
				row.setTextSize(rs.getParameter("textSize", ""));
				row.setFont(rs.getParameter("font", ""));
				row.setBackgroundColor(rs.getParameter("backgroundColor", ""));
				editorContext.getCurrentTemplate().storeRows(rows);
			}
		}
		editorContext.getCurrentTemplate().clearRenderer(ctx);
		return null;
	}

	public static String performUpdateStyle(RequestService rs, ServletContext application, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		TemplateStyle style = editorContext.getCurrentTemplate().getStyle();

		if (rs.getParameter("delete", null) != null) {
			if (editorContext.getCurrentTemplate().getParent().equals("default")) {
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
				style.setTextColor(rs.getParameter("textColor", ""));
				style.setTextSize(rs.getParameter("textSize", ""));
				style.setFont(rs.getParameter("font", ""));
				style.setBackgroundColor(rs.getParameter("backgroundColor", ""));
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
		}
		editorContext.getCurrentTemplate().clearRenderer(ctx);
		return null;
	}

	public static String performCreateArea(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		editorContext.getCurrentTemplate().addArea(editorContext.getArea().getRow().getName());
		editorContext.getCurrentTemplate().importTemplateInWebapp(ctx.getGlobalContext().getStaticConfig(), ctx);
		return null;
	}

	public static String performCreateRow(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		editorContext.getCurrentTemplate().addRow();
		editorContext.getCurrentTemplate().importTemplateInWebapp(ctx.getGlobalContext().getStaticConfig(), ctx);
		return null;
	}

	public static String performChangeTemplate(RequestService rs, ServletContext application, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		String templateName = rs.getParameter("template", "");
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		Template template = TemplateFactory.getTemplates(application).get(templateName);
		if (template == null) {
			return "template not found : " + templateName;
		}
		editorContext.setCurrentTemplate(template);
		return null;
	}

	public static String performCreateTemplate(RequestService rs, ContentContext ctx, StaticConfig staticConfig, ServletContext application, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		String templateName = rs.getParameter("template", "");
		if (templateName.trim().length() < 3) {
			return "template name must be at least 4 chars.";
		} else {
			Template newTemplate = TemplateFactory.createDiskTemplates(application, templateName);
			newTemplate.setParentName("editable");
			Row row = new Row();
			row.setName("row-1");
			Area area = new Area();
			area.setName("content");
			row.addArea(area);
			List<Row> rows = new LinkedList<Row>();
			rows.add(row);
			newTemplate.storeRows(rows);
			Template template = TemplateFactory.getTemplates(application).get(newTemplate.getName());
			TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());			
			editorContext.setCurrentTemplate(template);
		}
		return null;
	}

	public static String performShowContent(RequestService rs, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) {
		TemplateEditorContext editorCtx = TemplateEditorContext.getInstance(session);
		editorCtx.setShowContent(StringHelper.isTrue(rs.getParameter("show-content", null)));
		return null;
	}

}
