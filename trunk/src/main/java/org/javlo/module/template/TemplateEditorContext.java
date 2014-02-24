package org.javlo.module.template;

import javax.servlet.http.HttpSession;

import org.javlo.template.Template;

public class TemplateEditorContext {
	
	private Template currentTemplate;
	
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
	}

}
