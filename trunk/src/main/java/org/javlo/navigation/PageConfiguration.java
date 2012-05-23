package org.javlo.navigation;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.mailing.MailingContext;
import org.javlo.service.exception.ServiceException;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class PageConfiguration {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(PageConfiguration.class.getName());

	private List<Template> templates = null;

	private List<Template> mailingTemplates = null;

	private static final String KEY = PageConfiguration.class.getName();

	public static final String FORCE_TEMPLATE_PARAM_NAME = "force-template";

	public static PageConfiguration getInstance(GlobalContext globalContext) throws ConfigurationException, IOException {		
		PageConfiguration pageConfig = (PageConfiguration) globalContext.getAttribute(KEY);
		if (pageConfig == null) {
			pageConfig = new PageConfiguration();
			pageConfig.loadTemplate(globalContext);
			globalContext.setAttribute(KEY, pageConfig);
		}
		return pageConfig;
	}

	public Template getCurrentTemplate(ContentContext ctx, MenuElement elem) throws ConfigurationException, IOException, ServiceException {

		Template template = null;

		boolean mailing = false;
		if (ctx.getRenderMode() == ContentContext.PAGE_MODE) {
			mailing = true;
		}
		String forceTemplate = ctx.getRequest().getParameter(FORCE_TEMPLATE_PARAM_NAME);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (forceTemplate != null) {
			logger.info("force template : " + forceTemplate);
			template = Template.getApplicationInstance(ctx.getRequest().getSession().getServletContext(), ctx, forceTemplate, mailing);
		}
		if (template == null) {
			template = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(elem.getTemplateId());
			if (mailing) {
				MailingContext mailingContext = MailingContext.getInstance(ctx.getRequest().getSession());
				template = TemplateFactory.getMailingTemplates(ctx.getRequest().getSession().getServletContext()).get(mailingContext.getCurrentTemplate());
			}
			if (template == null || !template.exist()) {
				while (elem.getParent() != null && ((template == null) || (!template.exist()) || (template.getRendererFullName(ctx) == null))) {
					elem = elem.getParent();
					template = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(elem.getTemplateId());
				}
			}
		}

		if ((template == null) || !template.exist()) {

			if (globalContext.getDefaultTemplate() != null) {
				template = Template.getApplicationInstance(ctx.getRequest().getSession().getServletContext(), ctx, globalContext.getDefaultTemplate(), mailing);
			}
		}
		if (template != null && ctx.getSpecialContentRenderer() != null) {
			if (template.getSpecialRendererTemplate() != null) {
				Template newTemplate = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(template.getSpecialRendererTemplate());
				if (newTemplate != null) {
					template = newTemplate;
				}
			}
		}

		if (template != null && !template.isTemplateInWebapp(ctx)) {
			template.importTemplateInWebapp(ctx);
		}

		return template;
	}

	public Collection<String> getMailingTemplateCategories() {
		Collection<String> categories = new LinkedList<String>();
		for (Template template : mailingTemplates) {
			Collection<String> tempCat = template.getCategories();
			for (String cat : tempCat) {
				if (!categories.contains(cat)) {
					categories.add(cat);
				}
			}
		}
		return categories;
	}

	public List<Template> getMailingTemplates() {
		return mailingTemplates;
	}

	public Collection<Template> getMailingTemplates(Collection<String> roles) {
		Collection<Template> outTemplate = new TreeSet<Template>(Template.TEMPLATE_COMPARATOR);
		for (Template template : mailingTemplates) {
			if (template.visibleForRoles(roles)) {
				outTemplate.add(template);
			}
		}
		return outTemplate;
	}

	public Template getTemplate(String id) {
		for (Template template : templates) {
			if (template.getId() != null) {
				if (template.getId().equals(id)) {
					return template;
				}
			}
		}
		return null;
	}

	public Collection<String> getTemplateCategories() {
		Collection<String> categories = new LinkedList<String>();
		for (Template template : templates) {
			Collection<String> tempCat = template.getCategories();
			for (String cat : tempCat) {
				if (!categories.contains(cat)) {
					categories.add(cat);
				}
			}
		}
		return categories;
	}

	public List<Template> getTemplates() {
		return templates;
	}
	
	public List<Template> getContextTemplates(EditContext editContext) {
		List<Template> allTemplate;
		if ((editContext.getTemplateType() == EditContext.MAILING_TEMPLATE)||(editContext.isMailing())) {
			allTemplate = getMailingTemplates();
		} else {
			allTemplate = getTemplates();
		}		
		return allTemplate;
	}

	public Collection<Template> getTemplates(Collection<String> roles) {
		Collection<Template> outTemplate = new TreeSet<Template>(Template.TEMPLATE_COMPARATOR);
		for (Template template : templates) {
			if (template.visibleForRoles(roles)) {
				outTemplate.add(template);
			}
		}
		return outTemplate;
	}

	public boolean isLayout() {
		return true;
	}

	public void loadTemplate(GlobalContext globalContext) throws IOException {
		synchronized (KEY) {
			Collection<Template> allTemplates = TemplateFactory.getAllTemplates(globalContext.getServletContext());
			templates = new LinkedList<Template>();
			for (Template template : allTemplates) {
				if (globalContext.getTemplates().contains(template.getId())) {
					templates.add(template);
				}
			}

			allTemplates = TemplateFactory.getAllMaillingTemplates(globalContext.getServletContext());
			mailingTemplates = new LinkedList<Template>();
			for (Template templateMailing : allTemplates) {
				if (globalContext.getMailingTemplates().contains(templateMailing.getId())) {
					mailingTemplates.add(templateMailing);
				}
			}

		}
	}
}
