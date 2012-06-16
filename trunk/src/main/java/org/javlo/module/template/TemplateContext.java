package org.javlo.module.template;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.javlo.bean.Link;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.core.Module;

public class TemplateContext {
	
	public static final String NAME = "template";
	
	private static final String KEY = "templateContext";	
	public static Link MY_TEMPLATES_LINK = null;
	public static Link ALL_TEMPLATES_LINK = null;
	private I18nAccess i18nAccess = null;	
	private String currentLink;
	
	private TemplateContext() {};
	
	public static final TemplateContext getInstance(HttpSession session, GlobalContext globalContext, Module module) throws FileNotFoundException, IOException {
		TemplateContext outCtx = (TemplateContext)session.getAttribute(KEY);
		if (outCtx == null) {
			outCtx = new TemplateContext();
			outCtx.i18nAccess = I18nAccess.getInstance(globalContext, session);			
			TemplateContext.MY_TEMPLATES_LINK = new Link("mytemplates", outCtx.i18nAccess.getText("template.renderer.home"));
			TemplateContext.ALL_TEMPLATES_LINK = new Link("allmtemplates", outCtx.i18nAccess.getText("template.renderer.all"));
			session.setAttribute(KEY, outCtx);
		}
		return outCtx;
	}
	
	public List<Link> getLocalNavigation() {
		List<Link> outRenderers = new LinkedList<Link>();		
		outRenderers.add(MY_TEMPLATES_LINK);		
		outRenderers.add(ALL_TEMPLATES_LINK);
		return outRenderers;
	}
	
	public String getCurrentLink() {
		if (currentLink == null) {
			return MY_TEMPLATES_LINK.getUrl();
		}
		return currentLink;
	}

	public void setCurrentLink(String currentLink) {
		this.currentLink = currentLink;
	}

}
