package org.javlo.template;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.filter.VisibleDirectoryFilter;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;

public class TemplateFactory {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(TemplateFactory.class.getName());

	private static final String TEMPLATE_KEY = "wcms-templates";

	private static final String MAILING_TEMPLATE_KEY = "wcms-mailing-templates";

	public static List<String> TEMPLATE_COLOR_AMBIANCE = Arrays.asList(new String[] { "none", "black", "white", "gray", "red", "green", "blue", "orange", "yellow", "purple", "pink", "brun" });

	public static void cleanAllRenderer(ContentContext ctx, boolean mailing, boolean secure) throws IOException {
		cleanRenderer(ctx, null, mailing, secure);

	}

	/**
	 * clear only template with id contains in inTemplates
	 * 
	 * @param ctx
	 * @param inTemplates
	 *            a list of template id
	 * @param mailing
	 *            true is mailing template
	 * @param secure
	 * @throws IOException
	 */
	public static void cleanRenderer(ContentContext ctx, Collection<String> inTemplates, boolean mailing, boolean secure) throws IOException {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		AdminUserSecurity security = AdminUserSecurity.getInstance();
		if (secure) {
			if (!security.haveRight(editCtx.getEditUser(), AdminUserSecurity.FULL_CONTROL_ROLE)) {
				return;
			}
		}

		Collection<Template> templates;
		if (mailing) {
			templates = getAllMaillingTemplates(ctx.getRequest().getSession().getServletContext());
		} else {
			templates = getAllTemplates(ctx.getRequest().getSession().getServletContext());
		}

		for (Template template : templates) {
			if (inTemplates == null || inTemplates.contains(template.getId())) {
				template.clearRenderer(ctx);
			}
		}

	}

	public static void clearTemplate(ServletContext application) {
		application.removeAttribute(TEMPLATE_KEY);
		application.removeAttribute(MAILING_TEMPLATE_KEY);
	}

	public static List<String> getAllAuthors(ServletContext application) throws IOException {
		Collection<Template> templates = getAllTemplates(application);
		List<String> authors = new LinkedList<String>();
		for (Template template : templates) {
			if (!authors.contains(template.getAuthors())) {
				authors.add(template.getAuthors());
			}
		}
		return authors;
	}

	public static Collection<String> getAllCategories(ServletContext application, boolean mailling) throws IOException {
		return getAllCategories(application, null, mailling);
	}

	/**
	 * list of categories for a specific user
	 * 
	 * @param session
	 * @param user
	 * @param mailling
	 * @return
	 * @throws IOException
	 */
	public static Collection<String> getAllCategories(ServletContext application, User user, boolean mailling) throws IOException {
		Collection<Template> templates;
		if (mailling) {
			templates = getAllMaillingTemplates(application);
		} else {
			templates = getAllTemplates(application);
		}
		Collection<String> categories = new TreeSet<String>();
		for (Template template : templates) {
			if (user == null || template.visibleForRoles(user.getRoles())) {
				categories.addAll(template.getCategories());
			}
		}
		return categories;
	}

	/**
	 * get templates from disk without cache.
	 * 
	 * @param application
	 * @return
	 * @throws IOException
	 */
	public static Collection<Template> getAllDiskMaillingTemplates(ServletContext application) throws IOException {
		List<Template> outList = new LinkedList(getMailingTemplates(application).values());
		Collections.sort(outList, Template.TemplateDateComparator.instance);
		return outList;
	}

	public static Collection<Template> getAllMaillingTemplates(ServletContext application) throws IOException {
		List<Template> outList = new LinkedList(getMailingTemplates(application).values());
		Collections.sort(outList, Template.TemplateDateComparator.instance);
		return outList;
	}

	public static List<String> getAllSources(ServletContext application) throws IOException {
		Collection<Template> templates = getAllTemplates(application);
		List<String> sources = new LinkedList<String>();
		for (Template template : templates) {
			if (!sources.contains(template.getSource())) {
				sources.add(template.getSource());
			}
		}
		return sources;
	}

	public static Collection<Template> getAllTemplates(ServletContext application) throws IOException {
		List<Template> outList = new LinkedList(getTemplates(application).values());
		Collections.sort(outList, Template.TemplateDateComparator.instance);
		return outList;
	}

	/**
	 * get all templates from disk without cache
	 * 
	 * @param application
	 * @return
	 * @throws IOException
	 */
	public static List<Template> getAllDiskTemplates(ServletContext application) throws IOException {
		List<Template> outList = new LinkedList(getDiskTemplates(application).values());
		Collections.sort(outList, Template.TemplateDateComparator.instance);
		return outList;
	}

	/**
	 * get a template from disk
	 * 
	 * @param application
	 *            appplication context
	 * @param templateName
	 *            the name of the template
	 * @return
	 * @throws IOException
	 */
	public static Template getDiskTemplate(ServletContext application, String templateName) throws IOException {
		List<Template> templates = new LinkedList(getDiskTemplates(application).values());
		Template outTemplate = null;
		for (Template template : templates) {
			if (template.getName().equals(templateName)) {
				outTemplate = template;
			}
		}
		return outTemplate;
	}

	public static List<Template> getAllValidTemplates(ServletContext application) throws IOException {
		List<Template> outTemplate = new LinkedList<Template>();
		Collection<Template> templates = getAllTemplates(application);
		for (Template template : templates) {
			if (template.isValid()) {
				outTemplate.add(template);
			}
		}
		return outTemplate;
	}

	/**
	 * get mailing templates from disk, without cache.
	 * 
	 * @param application
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Template> getDiskMailingTemplates(ServletContext application) throws IOException {
		StaticConfig staticConfig = StaticConfig.getInstance(application);
		File templateFolder = new File(staticConfig.getMailingTemplateFolder());
		File[] allTemplateFile = templateFolder.listFiles(new VisibleDirectoryFilter());
		Map<String, Template> outTemplate = new HashMap<String, Template>();
		if (allTemplateFile != null) {
			for (File element : allTemplateFile) {
				try {
					Template template = Template.getInstance(staticConfig, null, element.getName(), true);
					template.setMailing(true);
					outTemplate.put(template.getId(), template);
					logger.fine("load mailing template : " + template.getId());
				} catch (ConfigurationException e) {
					e.printStackTrace();
				}
			}
		}

		return outTemplate;
	}

	public static Map<String, Template> getMailingTemplates(ServletContext application) throws IOException {
		Map<String, Template> outTemplate = (Map<String, Template>) application.getAttribute(MAILING_TEMPLATE_KEY);
		if (outTemplate == null) {
			outTemplate = getDiskMailingTemplates(application);
			application.setAttribute(MAILING_TEMPLATE_KEY, outTemplate);
		}
		return outTemplate;
	}

	/**
	 * get template from disk.
	 * 
	 * @param application
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Template> getDiskTemplates(ServletContext application) throws IOException {
		StaticConfig staticConfig = StaticConfig.getInstance(application);
		File templateFolder = new File(staticConfig.getTemplateFolder());
		File[] allTemplateFile = templateFolder.listFiles(new VisibleDirectoryFilter());
		if (allTemplateFile == null) {
			allTemplateFile = new File[0];
		}
		Map<String, Template> outTemplates = new HashMap<String, Template>();
		for (File element : allTemplateFile) {
			try {
				Template template = Template.getInstance(staticConfig, null, element.getName(), false);
				outTemplates.put(template.getId(), template);
				logger.fine("load template : " + template.getId());
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
		}
		return outTemplates;
	}
	
	/**
	 * get template from disk.
	 * 
	 * @param application
	 * @return
	 * @throws IOException
	 */
	public static Template createDiskTemplates(ServletContext application, String name) throws IOException {
		StaticConfig staticConfig = StaticConfig.getInstance(application);		
		File templateFolder = new File(URLHelper.mergePath(staticConfig.getTemplateFolder(), StringHelper.createFileName(name)));
		if (templateFolder.exists()) {
			return null;
		}		
		File defaultTemplate = new File(staticConfig.getDefaultTemplateFolder());
		if (defaultTemplate.exists()) {
			FileUtils.copyDirectory(defaultTemplate, templateFolder);
		} else {
			File configFile = new File(URLHelper.mergePath(templateFolder.getAbsolutePath(), Template.CONFIG_FILE));
			configFile.getParentFile().mkdirs();
			configFile.createNewFile();
		}
		return getDiskTemplates(application).get(name);
	}

	public static Template getDiskTemplate(ServletContext application, String templateName, boolean mailing) throws IOException {
		Template template = null;
		if (templateName != null) {
			Map<String, Template> allTemplates;
			if (mailing) {
				allTemplates = TemplateFactory.getDiskMailingTemplates(application);
			} else {
				allTemplates = TemplateFactory.getDiskTemplates(application);
			}
			template = allTemplates.get(templateName);
		}
		return template;
	}

	/**
	 * get templates from template list cache or from disk if the cache does'nt exist.
	 * 
	 * @param application
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Template> getTemplates(ServletContext application) throws IOException {
		Map<String, Template> outTemplate = null;
		synchronized (TEMPLATE_KEY) {
			outTemplate = (Map<String, Template>) application.getAttribute(TEMPLATE_KEY);
			if (outTemplate == null) {
				outTemplate = getDiskTemplates(application);
				application.setAttribute(TEMPLATE_KEY, outTemplate);
			}
		}
		return outTemplate;
	}

	public static boolean isMailingTemplateExist(ServletContext application, String templateID) throws IOException {
		Collection<Template> templates = getAllMaillingTemplates(application);
		for (Template template : templates) {
			if (template.getId().equals(templateID)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isTemplateExistOnDisk(ServletContext application, String templateID, boolean mailing) throws IOException {
		Collection<Template> templates = getAllDiskTemplates(application);
		if (mailing) {
			templates = getAllDiskMaillingTemplates(application);
		}
		for (Template template : templates) {
			if (template.getId().equals(templateID)) {
				return true;
			}
		}
		return false;
	}

}