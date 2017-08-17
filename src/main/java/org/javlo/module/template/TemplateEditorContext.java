package org.javlo.module.template;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.javlo.component.core.ComponentBean;
import org.javlo.template.Area;
import org.javlo.template.Row;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class TemplateEditorContext {
	
	private String currentTemplate;
	private String area;
	private boolean showContent = false;
	private ServletContext application;
	
	public static TemplateEditorContext getInstance(HttpSession session) {
		final String KEY = "templateEditorContext";
		TemplateEditorContext outCtx = (TemplateEditorContext)session.getAttribute(KEY);
		if (outCtx == null) {
			outCtx = new TemplateEditorContext();
			outCtx.application = session.getServletContext();
			session.setAttribute(KEY, outCtx);
		}
		return outCtx;
	}

	public Template getCurrentTemplate() {
		try {
			return TemplateFactory.getTemplates(application).get(currentTemplate);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setCurrentTemplate(String currentTemplate) {
		this.currentTemplate = currentTemplate;
		Template template = getCurrentTemplate(); 
		if (template != null) {
			if (getArea() == null || !template.getAreas().contains(getArea().getName())) {
				setArea(ComponentBean.DEFAULT_AREA);
			}
		}
	}

	public Row getRow() {
		return getCurrentTemplate().getArea(getCurrentTemplate().getRows(), area).getRow();
	}

	public Area getArea() {
		if (currentTemplate == null) {
			return null;
		}
		return getCurrentTemplate().getArea(getCurrentTemplate().getRows(), area);
	}

	public void setArea(String area) {
		this.area = area;
	}

	public boolean isShowContent() {
		return showContent;
	}

	public void setShowContent(boolean showContent) {
		this.showContent = showContent;
	}

}
