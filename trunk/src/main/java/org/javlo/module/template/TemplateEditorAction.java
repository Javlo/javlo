package org.javlo.module.template;

import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.module.core.ModulesContext;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class TemplateEditorAction extends AbstractModuleAction {
	
	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {		
		String msg = super.prepare(ctx, modulesContext);
		
		List<String> editableTemplate = new LinkedList<String>();
		TemplateEditorContext editorContext = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		
		for (Template template : TemplateFactory.getAllTemplates(ctx.getRequest().getSession().getServletContext())) {
			if (template.isEditable()) {
				editableTemplate.add(template.getName());
			}
			// choose first template as current template.
			if (editorContext.getCurrentTemplate() == null) {
				editorContext.setCurrentTemplate(template);
			}
		}
		
		TemplateEditorContext editorCtx = TemplateEditorContext.getInstance(ctx.getRequest().getSession());
		String templateURL = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE));
		if (editorCtx.getCurrentTemplate() != null) {
			templateURL = URLHelper.addParam(templateURL, Template.FORCE_TEMPLATE_PARAM_NAME, editorCtx.getCurrentTemplate().getId());
			templateURL = URLHelper.addParam(templateURL, "_display-zone", "true");
			
			ctx.getRequest().setAttribute("templateURL", templateURL);
		}
		ctx.getRequest().setAttribute("templates", editableTemplate);
		
		return msg;
	}

	@Override
	public String getActionGroupName() {
		return "template-editor";
	}

}
