package org.javlo.module.template;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.javlo.bean.ParentLink;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.core.Module;

public class TemplateContext {
	
	public static final String NAME = "template";
	
	private static final String KEY = "templateContext";	
	public static ParentLink MY_TEMPLATES_LINK = null;
	public static ParentLink ALL_TEMPLATES_LINK = null;
	private I18nAccess i18nAccess = null;	
	private String currentLink;
	private boolean editPreview = false;
	
	private TemplateContext() {};
	
	public static final TemplateContext getInstance(HttpSession session, GlobalContext globalContext, Module module) throws FileNotFoundException, IOException {
		TemplateContext outCtx = (TemplateContext)session.getAttribute(KEY);
		if (outCtx == null) {
			outCtx = new TemplateContext();
			outCtx.i18nAccess = I18nAccess.getInstance(globalContext, session);			
			TemplateContext.MY_TEMPLATES_LINK = new ParentLink("mytemplates", outCtx.i18nAccess.getText("template.renderer.home"));
			TemplateContext.ALL_TEMPLATES_LINK = new ParentLink("allmtemplates", outCtx.i18nAccess.getText("template.renderer.all"));
			session.setAttribute(KEY, outCtx);
		}
		
		EditContext editContext = EditContext.getInstance(globalContext, session);
		outCtx.editPreview = editContext.isEditPreview();		

		return outCtx;
	}
	
	public List<ParentLink> getLocalNavigation() {
		List<ParentLink> outRenderers = new LinkedList<ParentLink>();		
		outRenderers.add(MY_TEMPLATES_LINK);		
		outRenderers.add(ALL_TEMPLATES_LINK);
		return outRenderers;
	}
	
	public void checkEditMode(ContentContext ctx) {
		if (ctx.isAsEditMode() && !ctx.isEditPreview()) {
			editPreview = false;
		} else {
			editPreview = EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession()).isEditPreview();
		}
	}
	
	public String getCurrentLink() {
		if (currentLink == null || editPreview) {
			return MY_TEMPLATES_LINK.getUrl();
		}
		return currentLink;
	}

	public void setCurrentLink(String currentLink) {
		this.currentLink = currentLink;
	}

}
