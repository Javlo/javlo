package org.javlo.template;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.javlo.navigation.DefaultTemplate;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.NavigationWithContent;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;

public class TemplateFactory {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(TemplateFactory.class.getName());

	private static final String TEMPLATE_KEY = "wcms-templates";

	public static List<String> TEMPLATE_COLOR_AMBIANCE = Arrays.asList(new String[] { "none", "black", "white", "gray", "red", "green", "blue", "orange", "yellow", "purple", "pink", "brun" });

	private static final Set<String> templateNotExist = new HashSet<String>();

	public static void cleanAllRenderer(ContentContext ctx, boolean secure) throws Exception {
		cleanRenderer(ctx, null, secure);

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
	public static void cleanRenderer(ContentContext ctx, Collection<String> inTemplates, boolean secure) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		AdminUserSecurity security = AdminUserSecurity.getInstance();
		if (secure) {
			if (!security.haveRight(editCtx.getEditUser(), AdminUserSecurity.FULL_CONTROL_ROLE)) {
				return;
			}
		}

		Collection<Template> templates;
		templates = getAllTemplates(ctx.getRequest().getSession().getServletContext());

		for (Template template : templates) {
			if (inTemplates == null || inTemplates.contains(template.getId())) {
				template.clearRenderer(ctx);
			}
		}

	}

	public static void clearTemplate(ServletContext application) {
		application.removeAttribute(TEMPLATE_KEY);
		templateNotExist.clear();
	}

	public static List<String> getAllAuthors(ServletContext application) throws Exception {
		Collection<Template> templates = getAllTemplates(application);
		List<String> authors = new LinkedList<String>();
		for (Template template : templates) {
			if (!authors.contains(template.getAuthors())) {
				authors.add(template.getAuthors());
			}
		}
		return authors;
	}

	public static Collection<String> getAllCategories(ServletContext application) throws Exception {
		return getAllCategories(application, null);
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
	public static Collection<String> getAllCategories(ServletContext application, User user) throws Exception {
		Collection<Template> templates;
		templates = getAllTemplates(application);

		Collection<String> categories = new TreeSet<String>();
		for (Template template : templates) {
			if (user == null || template.visibleForRoles(user.getRoles())) {
				categories.addAll(template.getCategories());
			}
		}
		return categories;
	}

	public static List<String> getAllSources(ServletContext application) throws Exception {
		Collection<Template> templates = getAllTemplates(application);
		List<String> sources = new LinkedList<String>();
		for (Template template : templates) {
			if (!sources.contains(template.getSource())) {
				sources.add(template.getSource());
			}
		}
		return sources;
	}

	public static List<Template> getAllTemplates(ServletContext application) throws Exception {
		List<Template> outList = new LinkedList(getTemplates(application).values());
		Collections.sort(outList, Template.TemplateDateComparator.instance);
		return outList;
	}
	
	public static List<Template> getAllTemplatesFromContext(GlobalContext context) throws Exception {
		List<Template> outTemplates = new LinkedList<Template>();
		Set<String> templatesName = new HashSet<String>(context.getTemplatesNames());
		for (Template template : TemplateFactory.getAllTemplates(context.getServletContext())) {
			if (templatesName.contains(template.getName())) {
				outTemplates.add(template);
			}
		}
		return outTemplates;
	}

	private static void getTemplateChildren(List<Template> outList, ServletContext application, Template template) throws Exception {
		if (template != DefaultTemplate.INSTANCE) {
			return;
		}
		List<Template> templates = getAllTemplates(application);
		for (Template tpl : templates) {
			if (tpl.getParent() != null && tpl.getParent().getId() != null && tpl.getParent().getId().equals(template.getId())) {
				if (!outList.contains(tpl)) {
					outList.add(tpl);
					getTemplateChildren(outList, application, tpl);
				}
			}
		}
	}

	/**
	 * get children of the template.
	 * 
	 * @param application
	 * @param template
	 * @return
	 * @throws IOException
	 */
	public static Collection<Template> getTemplateChildren(ServletContext application, Template template) throws Exception {
		List<Template> outList = new LinkedList<Template>();
		List<Template> templates = getAllTemplates(application);
		for (Template tpl : templates) {
			if (tpl.getParent() != null && tpl.getParent().getId() != null && tpl.getParent().getId().equals(template.getId())) {
				if (!outList.contains(tpl)) {
					outList.add(tpl);
					getTemplateChildren(outList, application, tpl);
				}
			}
		}
		return outList;
	}

	private static void searchAllChildren(List<Template> currentChildren, ServletContext application, Template template) throws Exception {
		Collection<Template> children = getTemplateChildren(application, template);
		for (Template child : children) {
			if (!currentChildren.contains(child)) {
				currentChildren.add(child);
				searchAllChildren(currentChildren, application, child);
			}
		}
	}

	/**
	 * get all descendants of the template
	 * 
	 * @param application
	 * @param template
	 * @return
	 * @throws IOException
	 */
	public static Collection<Template> getTemplateAllChildren(ServletContext application, Template template) throws Exception {
		List<Template> outList = new LinkedList<Template>();
		searchAllChildren(outList, application, template);
		return outList;
	}

	/**
	 * get all templates from disk without cache
	 * 
	 * @param application
	 * @return
	 * @throws IOException
	 */
	public static List<Template> getAllDiskTemplates(ServletContext application) throws Exception {
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
	public static Template getDiskTemplate(ServletContext application, String templateName) throws Exception {
		templateName = StringHelper.createFileName(templateName);
		List<Template> templates = new LinkedList(getDiskTemplates(application).values());
		Template outTemplate = null;
		for (Template template : templates) {
			if (template.getName().equals(templateName)) {
				outTemplate = template;
			}
		}
		return outTemplate;
	}

	public static List<Template> getAllValidTemplates(ServletContext application) throws Exception {
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
	 * get template from disk.
	 * 
	 * @param application
	 * @return
	 * @throws IOException
	 */
	public synchronized static Map<String, Template> getDiskTemplates(ServletContext application) throws Exception {
		StaticConfig staticConfig = StaticConfig.getInstance(application);
		File templateFolder = new File(staticConfig.getTemplateFolder());
		File[] allTemplateFile = templateFolder.listFiles(new VisibleDirectoryFilter());
		if (allTemplateFile == null) {
			allTemplateFile = new File[0];
		}
		Map<String, Template> outTemplates = new HashMap<String, Template>();
		for (File element : allTemplateFile) {
			try {
				Template template = Template.getInstance(staticConfig, null, element.getName());
				outTemplates.put(template.getId(), template);
				logger.fine("load template : " + template.getId());
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
		}
		return outTemplates;
	}

	/**
	 * create new template with parent files.
	 * 
	 * @param application
	 * @return
	 * @throws IOException
	 */
	public static Template createDiskTemplates(ServletContext application, String name) throws Exception {
		StaticConfig staticConfig = StaticConfig.getInstance(application);
		File templateFolder = new File(URLHelper.mergePath(staticConfig.getTemplateFolder(), StringHelper.createFileName(name)));
		if (templateFolder.exists()) {
			logger.warning("Folder exist : "+templateFolder);
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
		TemplateFactory.clearTemplate(application);
		return getDiskTemplates(application).get(name);
	}
	
	/**
	 * create new template.
	 * 
	 * @param application
	 * @param name name of the new template, if all ready exist return null.
	 * @param source name of the source template, all files must be copied inside new template (!with config.properties) null = no source template.
	 * @return
	 * @throws IOException
	 */
	public static Template createDiskTemplates(ServletContext application, String name, String source) throws Exception {
		StaticConfig staticConfig = StaticConfig.getInstance(application);
		name = StringHelper.createFileName(name);
		File templateFolder = new File(URLHelper.mergePath(staticConfig.getTemplateFolder(), name));
		if (templateFolder.exists()) {
			logger.warning("Folder exist : "+templateFolder);
			return null;
		}
		File sourceFolder = null;
		if (source != null) {
			 sourceFolder = new File(URLHelper.mergePath(staticConfig.getTemplateFolder(), source));
		}
		if (sourceFolder != null && sourceFolder.exists()) {
			FileUtils.copyDirectory(sourceFolder, templateFolder);
		} else {
			File configFile = new File(URLHelper.mergePath(templateFolder.getAbsolutePath(), Template.CONFIG_FILE));
			configFile.getParentFile().mkdirs();
			configFile.createNewFile();
		}
		TemplateFactory.clearTemplate(application);
		return getDiskTemplates(application).get(name);
	}

	/**
	 * get templates from template list cache or from disk if the cache does'nt exist.
	 * 
	 * @param application
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Template> getTemplates(ServletContext application) throws Exception {
		Map<String, Template> outTemplate = null;
		synchronized (TEMPLATE_KEY) {
			outTemplate = (Map<String, Template>) application.getAttribute(TEMPLATE_KEY);
			if (outTemplate == null) {
				outTemplate = Collections.unmodifiableMap(getDiskTemplates(application));
				application.setAttribute(TEMPLATE_KEY, outTemplate);
			}
		}
		return outTemplate;
	}

	public static boolean isTemplateExistOnDisk(ServletContext application, String templateID, boolean mailing) throws Exception {
		Collection<Template> templates = getAllDiskTemplates(application);
		for (Template template : templates) {
			if (template.getId().equals(templateID)) {
				return true;
			}
		}
		return false;
	}

	public static Template getTemplate(ContentContext ctx, MenuElement elem) throws Exception {
		if (elem == null) {
			return null;
		}
		String key = "_template_" + elem.getId() + '_' + ctx.getRenderMode();
		if (ctx.getRequest().getAttribute(key) != null) {
			return (Template) ctx.getRequest().getAttribute(key);
		}
		Template template = null;
		
		template = getTemplates(ctx.getRequest().getSession().getServletContext()).get(elem.getTemplateId());
		if (template == null || !template.exist()) {
			while (elem.getParent() != null && ((template == null) || (!template.exist()) || (template.getRendererFullName(ctx) == null))) {
				elem = elem.getParent();
				template = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(elem.getTemplateId());
			}
		}
	
		if (template == null) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			template = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(globalContext.getDefaultTemplate());
		} else {
			ctx.getRequest().setAttribute(key, template);
		}
		return template;
	}
	
	public static void copyDefaultTemplate(ServletContext application) {
		File defaultTemplateFolder = new File(application.getRealPath("/WEB-INF/template/"));
		for (File template : defaultTemplateFolder.listFiles()) {
			if (template.isDirectory()) {
				File templateFolder = new File(URLHelper.mergePath(StaticConfig.getInstance(application).getTemplateFolder(), template.getName()));
				if (!templateFolder.exists()) {
					templateFolder.getParentFile().mkdirs();
					logger.info("import default template : " + template.getName());
					try {
						FileUtils.copyDirectory(template, templateFolder);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	
}