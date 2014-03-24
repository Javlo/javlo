package org.javlo.module.template;

import javax.servlet.http.HttpSession;

import org.javlo.component.core.ComponentBean;
import org.javlo.template.Area;
import org.javlo.template.Row;
import org.javlo.template.Template;

public class TemplateEditorContext {
	
	private Template currentTemplate;
	private String area;
	private boolean showContent = false;
	
	public static TemplateEditorContext getInstance(HttpSession session) {
		final String KEY = "templateEditorContext";
		TemplateEditorContext outCtx = (TemplateEditorContext)session.getAttribute(KEY);
		if (outCtx == null) {
			outCtx = new TemplateEditorContext();
			session.setAttribute(KEY, outCtx);
		}
		return outCtx;
	}

	public Template getCurrentTemplate() {
		return currentTemplate;
	}

	public void setCurrentTemplate(Template currentTemplate) {
		this.currentTemplate = currentTemplate;
		if (currentTemplate != null) {
			if (getArea() == null || !currentTemplate.getAreas().contains(getArea().getName())) {
				setArea(ComponentBean.DEFAULT_AREA);
			}
		}
	}

	public Row getRow() {
		return currentTemplate.getArea(currentTemplate.getRows(), area).getRow();
	}

	public Area getArea() {
		return currentTemplate.getArea(currentTemplate.getRows(), area);
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
