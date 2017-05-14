package org.javlo.module.template;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.template.Template;
import org.javlo.template.Template.TemplateBean;

public class TemplateHierarchy {
	
	private TemplateBean template;
	private List<TemplateHierarchy> children = new LinkedList<TemplateHierarchy>();
	
	public TemplateHierarchy(TemplateBean template) {
		super();
		this.template = template;
	}
	
	public TemplateBean getTemplate() {
		return template;
	}
	
	public List<TemplateHierarchy> getChildren() {
		return children;
	}
	
	public static void insertTemplateInHirarchy(ContentContext ctx, List<TemplateHierarchy> rootTemplates, Map<String, TemplateHierarchy> templateInList, Template template) throws Exception {			
		if (template.getParent() != null) {
			if (!templateInList.containsKey(template.getParent().getName())) {
				insertTemplateInHirarchy(ctx, rootTemplates, templateInList, template.getParent());
			}
			TemplateHierarchy parentH = templateInList.get(template.getParent().getName());				
			TemplateHierarchy tempH = new TemplateHierarchy(new TemplateBean(ctx,template));
			if (!templateInList.containsKey(template.getName())) {
				templateInList.put(template.getName(), tempH);
			} else {
				templateInList.get(template.getName()).children.addAll(tempH.children);			
				tempH=templateInList.get(template.getName());
			}
			if (!parentH.children.contains(tempH)) {
				parentH.children.add(tempH);
			}
		} else {
			TemplateHierarchy rootTemplate = templateInList.get(template.getName());
			if (rootTemplate == null) {
				rootTemplate = new TemplateHierarchy(new TemplateBean(ctx,template));
				rootTemplates.add(rootTemplate);
				templateInList.put(template.getName(), rootTemplate);
			}
		}
	}
}
