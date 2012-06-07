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
	
	private static final String KEY = "templateContext";
	private static Link HOME_LINK = null;
	private I18nAccess i18nAccess = null;
	private Module module;
	private String currentLink;
	
	private TemplateContext() {};
	
	public static final TemplateContext getInstance(HttpSession session, GlobalContext globalContext, Module module) throws FileNotFoundException, IOException {
		TemplateContext outCtx = (TemplateContext)session.getAttribute(KEY);
		if (outCtx == null) {
			outCtx = new TemplateContext();
			outCtx.i18nAccess = I18nAccess.getInstance(globalContext, session);
			outCtx.module = module;
			TemplateContext.HOME_LINK = new Link("home", outCtx.i18nAccess.getText("template.renderer.home"));
			session.setAttribute(KEY, outCtx);
		}
		return outCtx;
	}
	
	public List<Link> getLocalNavigation() {
		List<Link> outRenderers = new LinkedList<Link>();		
		outRenderers.add(HOME_LINK);		
		return outRenderers;
	}
	
	public List<Link> getRemoteNavigation() {
		List<Link> outRenderers = new LinkedList<Link>();
		outRenderers.add(new Link("freecsstemplate", "free css template"));
		return outRenderers;
	}

	public String getCurrentLink() {
		if (currentLink == null) {
			return HOME_LINK.getUrl();
		}
		return currentLink;
	}

	public void setCurrentLink(String currentLink) {
		this.currentLink = currentLink;
	}

}
