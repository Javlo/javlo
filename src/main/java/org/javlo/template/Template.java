package org.javlo.template;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.ConfigurationException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.javlo.bean.SortBean;
import org.javlo.component.core.ComponentBean;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.filter.PropertiesFilter;
import org.javlo.helper.LangHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.XMLManipulationHelper;
import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.javlo.helper.filefilter.HTMLFileFilter;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.ExtendedColor;
import org.javlo.mailing.Mail;
import org.javlo.message.GenericMessage;
import org.javlo.navigation.DefaultTemplate;
import org.javlo.remote.IRemoteResource;
import org.javlo.rendering.Device;
import org.javlo.service.IListItem;
import org.javlo.service.ListService;
import org.javlo.service.exception.ServiceException;
import org.javlo.utils.ConfigurationProperties;
import org.javlo.utils.ListAsMap;
import org.javlo.utils.ListMapValueValue;
import org.javlo.utils.ReadOnlyPropertiesConfigurationMap;
import org.javlo.utils.StructuredConfigurationProperties;
import org.javlo.utils.TimeMap;

public class Template implements Comparable<Template> {

	public static final String PREVIEW_EDIT_CODE = "#PREVIEW-EDIT#";

	public static final String FORCE_TEMPLATE_PARAM_NAME = "force-template";

	private static final String FONT_REFERENCE_FILE = "fonts_reference.properties";

	private static final String RAW_CSS_FILE = "raw_css.txt";
	
	private static Set<String> FILTER_FILE_EXTENSION = new HashSet<String>(Arrays.asList(new String[] { "html", "htm", "jsp", "js", "css", "scss", "less" }));
	
	private static class TemplateComparator implements Comparator<Template> {

		@Override
		public int compare(Template o1, Template o2) {
			return o1.getName().compareTo(o2.getName());
		}

	}

	public static final class TemplateBean implements IRemoteResource {
		String name;
		String previewURL;
		String viewURL;
		String HTMLURL;
		String HTMLFile;
		String creationDate;
		String downloadURL;
		String rootURL;
		List<String> ids;
		List<String> css;
		Map<String, String> areaMap;
		List<String> areas;
		boolean valid;
		String imageURL;
		String url;
		String authors;
		String description;
		String licence;
		Date date;
		String id = StringHelper.getRandomId();
		boolean mailing;
		String category;
		String version;
		String deployId = StringHelper.getRandomId();
		String type;
		String parent;
		String imageFilter;
		List<String> htmls;
		List<String> renderers;
		boolean bootstrap = false;
		private Map<String, List<String>> cssByFolder;
		private Template template;
		private TemplateBean parentBean;
		private boolean importParentComponents = true;

		public TemplateBean() {
		};

		public TemplateBean(ContentContext ctx, Template template) throws Exception {
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());

			name = template.getName();
			this.template = template;
			previewURL = URLHelper.createTransformStaticTemplateURL(ctx, template, "template", template.getVisualFile());
			viewURL = URLHelper.createTransformStaticTemplateURL(ctx, template, "template_view", template.getVisualFile());
			HTMLURL = URLHelper.createStaticTemplateURL(ctx, template, template.getHTMLFile(null));
			HTMLFile = template.getHTMLFile(ctx.getDevice());
			creationDate = StringHelper.renderDate(template.getCreationDate(), staticConfig.getDefaultDateFormat());
			downloadURL = "/folder/template/" + template.getName() + ".zip";

			rootURL = URLHelper.createStaticTemplateURL(ctx, template, "");

			ContentContext remoteCtx = ctx.getContextForAbsoluteURL();
			downloadURL = URLHelper.createStaticURL(remoteCtx, downloadURL);
			bootstrap = template.isBootstrap();
			ids = template.getHTMLIDS();
			Collections.sort(ids);
			areas = template.getAreas();
			Collections.sort(areas);
			areaMap = template.getAreasMap();
			valid = template.isValid();
			parent = template.getParentName();
			try {
				imageURL = URLHelper.createTransformStaticTemplateURL(remoteCtx, template, "template", template.getVisualFile()) + "?deployId=" + template.getDeployId();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				url = URLHelper.createStaticTemplateURL(remoteCtx, template, template.getHTMLFile(null));
			} catch (Exception e) {
				e.printStackTrace();
			}
			name = template.getName();
			authors = template.getAuthors();
			description = template.getDescription(ctx.getLanguage());
			licence = template.getLicenceFile();
			date = template.getCreationDate();
			version = template.getVersion();
			deployId = template.getDeployId();
			type = IRemoteResource.TYPE_TEMPLATE;
			category = staticConfig.getMarketServerName();
			imageFilter = template.getImageFiltersRAW();
			css = template.getCSS();
			cssByFolder = template.getCSSByFolder();
			htmls = new LinkedList<String>();
			htmls.add(template.getHTMLFile(ctx.getDevice()));
			renderers = template.getRenderers();
			mailing = template.isMailing();
			importParentComponents = template.isImportParentComponents();

		}

		public String getPreviewUrl() throws Exception {
			return previewURL;
		}

		public String getViewUrl() throws Exception {
			return viewURL;
		}

		public String getHtmlUrl() throws Exception {
			return HTMLURL;
		}

		public String getRootURL() {
			return rootURL;
		}

		public String getHtmlFile() {
			return HTMLFile;
		}

		public String getCreationDate() {
			return creationDate;
		}

		@Override
		public String getDownloadURL() {
			return downloadURL;
		}

		public Collection<String> getHTMLIDS() {
			return ids;
		}

		public Collection<String> getAreas() {
			return areas;
		}

		public Map<String, String> getAreasMap() {
			return areaMap;
		}

		public List<String> getRenderers() {
			return renderers;
		}

		public boolean isValid() {
			return valid;
		}

		public boolean isMailing() {
			return mailing;
		}

		public String getDeployId() {
			return deployId;
		};

		@Override
		public String getImageURL() {
			return imageURL;
		}

		@Override
		public String getURL() {
			return url;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getAuthors() {
			return authors;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public String getLicence() {
			return licence;
		}

		@Override
		public Date getDate() {
			return date;
		}

		@Override
		public void setDownloadURL(String url) {
			this.downloadURL = url;
		}

		@Override
		public void setImageURL(String url) {
			this.imageURL = url;
		}

		@Override
		public void setURL(String url) {
			this.url = url;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public void setAuthors(String authors) {
			this.authors = authors;
		}

		@Override
		public void setDescription(String description) {
			this.description = description;
		}

		@Override
		public void setLicence(String licence) {
			this.licence = licence;
		}

		@Override
		public void setDate(Date date) {
			this.date = date;
		}

		@Override
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public void setId(String id) {
			this.id = id;
		}

		@Override
		public String getCategory() {
			return category;
		}

		@Override
		public void setCategory(String category) {
			this.category = category;
		}

		@Override
		public String getDateAsString() {
			return StringHelper.renderDate(getDate());
		}

		@Override
		public String getVersion() {
			return version;
		}

		@Override
		public void setVersion(String version) {
			this.version = version;
		}

		public String getParent() {
			return parent;
		}

		public String getImageFilter() {
			return imageFilter;
		}

		public List<String> getCSS() {
			return css;
		}

		public Map<String, List<String>> getCSSByFolder() {
			return cssByFolder;
		}

		public List<String> getHtmls() {
			return htmls;
		}

		public Template getTemplate() {
			return template;
		}

		public void setTemplate(Template template) {
			this.template = template;
		}

		public TemplateBean getParentBean() {
			return parentBean;
		}

		public void setParentBean(TemplateBean parentBean) {
			this.parentBean = parentBean;
		}

		public boolean isBootstrap() {
			return bootstrap;
		}

		public boolean isImportParentComponents() {
			return importParentComponents;
		}

		public void setImportParentComponent(boolean importParentComponents) {
			this.importParentComponents = importParentComponents;
		}

	}

	public static class TemplateDateComparator implements Comparator<Template> {

		public static final TemplateDateComparator instance = new TemplateDateComparator();

		@Override
		public int compare(Template o1, Template o2) {
			return o2.getCreationDate().compareTo(o1.getCreationDate());
		}

	}

	private static class WEBFileFilter implements FileFilter {

		private boolean accept = false;
		private boolean jsp = true;
		private boolean copyVisualFile = false;
		private Template template = null;

		public WEBFileFilter(Template inTemplate, boolean inAccept, boolean inJSP, boolean inCopyVisualFile) {
			accept = inAccept;
			jsp = inJSP;
			copyVisualFile = inCopyVisualFile;
			template = inTemplate;
		}

		@Override
		public boolean accept(File file) {
			if (!copyVisualFile && file.getName().endsWith(template.getVisualFile())) {
				return false;
			}
			String ext = FilenameUtils.getExtension(file.getName());
			if ((ext.equalsIgnoreCase("jsp") && jsp) || ext.equalsIgnoreCase("html") || ext.equalsIgnoreCase("css") || ext.equalsIgnoreCase("scss") || ext.equalsIgnoreCase("htm") || ext.equalsIgnoreCase("js") || ext.equalsIgnoreCase("less")) {
				return accept;
			} else {
				return !accept;
			}
		}

	}

	public static final String __NO_CONTEXT = "__no_context";

	public static final TemplateComparator TEMPLATE_COMPARATOR = new TemplateComparator();

	/**
	 * create a static logger.
	 */
	protected static Logger logger = java.util.logging.Logger.getLogger(Template.class.getName());

	private static final String AREA_FORCE_DISPLAY_PREFIX = "area.forcedisplay.";

	private static final String MAIL_FOLDER = "mail";

	public static final String CONFIG_FILE = "config.properties";

	private static final String PRIVATE_CONFIG_FILE = "private-config.properties";

	private static final String LINK_EMAIL_FILE = "link_email_[lg].txt";

	private static final String DYNAMIC_COMPONENTS_PROPERTIES_FOLDER = "components";

	private static final String CONFIG_COMPONENTS_PROPERTIES_FOLDER = "components-config";

	private static final String I18N_VIEW_FILE = "view_";

	private static final String I18N_EDIT_FILE = "edit_";

	private static final String LIST_FOLDER = "list";

	private static final String MACRO_FOLDER = "macro";

	public static final String EDIT_TEMPLATE_CODE = "_edit_";

	private static final String RESOURCES_DIR = "resources";

	public static final String PLUGIN_FOLDER = "plugins";

	public static final String GZ_FILE_EXT = "httpgz";

	public static final String DEFAULT_TEMPLATE_NAME = "wktp";

	private final ConfigurationProperties properties = new StructuredConfigurationProperties();

	private final ReadOnlyPropertiesConfigurationMap configMap = new ReadOnlyPropertiesConfigurationMap(properties, false);

	private final ConfigurationProperties privateProperties = new StructuredConfigurationProperties();

	private File dir = null;

	protected StaticConfig config;

	private final String buildId = StringHelper.getRandomId();

	private boolean jsp = false;

	private static Template emptyTemplate = null;

	private String deployId = StringHelper.getRandomId();

	private List<Properties> dynamicsComponents = null;

	private boolean indexFileWaited = false;

	private final Set<String> contextWithTemplateImported = new HashSet<String>();

	private final Map<String, Map> i18n = new HashMap<String, Map>();

	private Map<String, String> freeData = null;

	private Map<String, Row> rows = null;

	private TemplateStyle style = null;

	private List<String> areas = null;

	private List<String> notAdminAreas = null;

	private String fakeName = null;

	private List<Integer> sizes = null;

	public static Template getApplicationInstance(ServletContext application, ContentContext ctx, String templateDir) throws Exception {

		Template outTemplate = null;
		if (templateDir == null) {
			// logger.severe("templateDir is null");
			return null;
		}

		outTemplate = TemplateFactory.getTemplates(application).get(templateDir);
		if (outTemplate == null) {
			return getInstance(StaticConfig.getInstance(application), ctx, templateDir);
		}

		if (!outTemplate.isTemplateInWebapp(ctx)) {
			outTemplate.importTemplateInWebapp(StaticConfig.getInstance(application), ctx);
		}

		outTemplate.parent = outTemplate.getParent(StaticConfig.getInstance(application), ctx, null);
		if (outTemplate.parent == null) { // parent must be never null
			outTemplate.parent = DefaultTemplate.INSTANCE;
		}

		if (ctx != null) {
			if (outTemplate.isAlternativeTemplate(ctx)) {
				outTemplate = outTemplate.getAlternativeTemplate(StaticConfig.getInstance(application), ctx);
			}
		}
		return outTemplate;
	}

	public static Template getFakeTemplate(String name) {
		Template outTemplate = new Template();
		outTemplate.properties.setProperty("name", name);
		outTemplate.fakeName = name;
		return outTemplate;
	}

	public static Template getInstance(StaticConfig config, ContentContext ctx, String templateDir) throws Exception {
		return getInstance(config, ctx, templateDir, true, null);
	}

	private static Template getInstance(StaticConfig config, ContentContext ctx, String templateDir, boolean alternativeTemplate, Set<String> parents) throws Exception {

		if (config == null) {
			throw new RuntimeException("StaticConfig can not be null.");
		}

		if ((templateDir == null) || templateDir.trim().length() == 0) {
			return DefaultTemplate.INSTANCE;
		}
		Template template = new Template();
		String templateFolder = URLHelper.mergePath(config.getTemplateFolder(), templateDir);

		template.dir = new File(templateFolder);
		template.config = config;

		/*
		 * if (!template.isTemplateInWebapp(ctx)) {
		 * template.importTemplateInWebapp(config, ctx); }
		 */

		File configFile = new File(URLHelper.mergePath(templateFolder, CONFIG_FILE));
		File privateConfigFile = new File(URLHelper.mergePath(templateFolder, PRIVATE_CONFIG_FILE));

		try {
			if (!configFile.exists()) {
				configFile.createNewFile();
			}

			template.properties.setFile(configFile);

			if (!privateConfigFile.exists()) {
				privateConfigFile.createNewFile();
			}
			template.privateProperties.setFile(privateConfigFile);

			template.jsp = config.isTemplateJSP();

		} catch (Throwable t) { // if default template directory not exist
			// TODO Auto-generated catch block
			// logger.warning("problem with file : " +
			// configFile.getAbsolutePath());
			// t.printStackTrace();
		}

		template.parent = template.getParent(config, ctx, parents);
		if (template.parent == null) { // parent must be never null
			template.parent = DefaultTemplate.INSTANCE;
		}

		if (alternativeTemplate && ctx != null) {
			if (template.isAlternativeTemplate(ctx)) {
				Template altTemplate = template.getAlternativeTemplate(config, ctx);
				return altTemplate;
			}
		}

		return template;
	}

	public static Template getMailingInstance(StaticConfig config, ContentContext ctx, String templateDir) throws Exception {
		return getInstance(config, ctx, templateDir, true, null);
	}

	private Template parent = null;

	private static final Map<String, File> templateErrorMap = Collections.synchronizedMap(new TimeMap<String,File>(60*60));

	/**
	 * check the structure of the template.
	 * 
	 * @return the error message, null if no error.
	 * @throws IOException
	 * @throws BadXMLException
	 */
	public List<GenericMessage> checkRenderer(GlobalContext globalContext, I18nAccess i18nAccess) throws IOException, BadXMLException {
		String templateFolder = config.getTemplateFolder();

		File HTMLFile = new File(URLHelper.mergePath(URLHelper.mergePath(templateFolder, getSourceFolderName()), getHTMLFile(null)));

		List<GenericMessage> messages = new LinkedList<GenericMessage>();
		List<String> resources = new LinkedList<String>();

		try {
			TemplatePluginFactory templatePluginFactory = TemplatePluginFactory.getInstance(globalContext.getServletContext());
			XMLManipulationHelper.convertHTMLtoTemplate(globalContext, this, i18nAccess, HTMLFile, null, getMap(), getAreas(), resources, templatePluginFactory.getAllTemplatePlugin(globalContext.getTemplatePlugin()), messages, getFontIncluding(globalContext));
		} catch (Throwable t) {
			messages.add(new GenericMessage(t.getMessage(), GenericMessage.ERROR));
		}

		if (getParentName() != null) { // parent is valid >> template is valid
										// //TODO: ameliorated this test.
			messages = Collections.EMPTY_LIST;
		}

		return messages;
	}

	public void clearRenderer(ContentContext ctx) {
		logger.info("clear template renderer : "+ctx.getGlobalContext().getContextKey());
		synchronized (ctx.getGlobalContext().getLockImportTemplate()) {
			String templateFolder = config.getTemplateFolder();
			File templateSrc = new File(URLHelper.mergePath(templateFolder, getSourceFolderName()));
			if (templateSrc.exists()) {
				try {
					FileUtils.deleteDirectory(new File(URLHelper.mergePath(getWorkTemplateFolder(), getSourceFolderName())));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				importTemplateInWebapp(config.getInstance(ctx.getRequest().getSession().getServletContext()), ctx);
			} catch (IOException e) {
				e.printStackTrace();
			}
			dynamicsComponents = null;
			contextWithTemplateImported.clear();
			i18n.clear();
		}
		for (GlobalContext subContext : ctx.getGlobalContext().getSubContexts()) {
			ContentContext subCtx = new ContentContext(ctx);
			subCtx.setForceGlobalContext(subContext);
			clearRenderer(subCtx);
		}
	}

	public void reloadConfig(ContentContext ctx) {
		synchronized (ctx.getGlobalContext().getLockImportTemplate()) {
			File templateSrc = new File(URLHelper.mergePath(config.getTemplateFolder(), getSourceFolderName()));
			File templateTgt = new File(getTemplateTargetFolder(ctx.getGlobalContext()));
			try {
				FileUtils.copyFile(new File(URLHelper.mergePath(templateSrc.getAbsolutePath(), CONFIG_FILE)), new File(URLHelper.mergePath(templateTgt.getAbsolutePath(), CONFIG_FILE)));
				File renderedFile = new File(URLHelper.mergePath(templateTgt.getAbsolutePath(), getRendererFile(null)));
				if (renderedFile.exists()) {
					renderedFile.delete();
				}
				reload();
				dynamicsComponents = null;
				contextWithTemplateImported.clear();
				i18n.clear();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void delete() {
		try {
			FileUtils.deleteDirectory(new File(getTemplateRealPath()));
			// FileUtils.deleteDirectory(new
			// File(URLHelper.mergePath(getWorkTemplateFolder(), getSourceFolderName())));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void disabledAlternativeTemplate(ContentContext ctx) {
		ctx.getRequest().getSession().removeAttribute(getAlternateTemplateSessionKey());
	}

	/*
	 * public String getDefaultHTMLFile() { return properties.getString("html",
	 * "index.html"); }
	 */

	public void enabledAlternativeTemplate(ContentContext ctx) {
		if (getAlternativeTemplateName() != null) {
			ctx.getRequest().getSession().setAttribute(getAlternateTemplateSessionKey(), new Object());
		}
	}

	public boolean exist() {
		return dir != null && dir.exists();
	}

	private String getAlternateTemplateSessionKey() {
		return "_alternate_template";
	}

	private Template getAlternativeTemplate(StaticConfig config, ContentContext ctx) throws Exception {
		Template aTemplate = this;
		String alternativeTemplate = getAlternativeTemplateName();
		if (alternativeTemplate != null) {
			aTemplate = Template.getInstance(config, ctx, alternativeTemplate, false, null);
		}
		return aTemplate;
	}

	private String getAlternativeTemplateName() {
		return properties.getString("template.alternative", null);
	}

	private Template getMobileTemplate(StaticConfig config, ContentContext ctx) throws Exception {
		Template aTemplate = this;
		String alternativeTemplate = getMobileTemplate();
		if (alternativeTemplate != null) {
			aTemplate = Template.getInstance(config, ctx, alternativeTemplate, false, null);
		}
		return aTemplate;
	}

	private String getMobileTemplate() {
		return properties.getString("template.mobile", null);
	}

	private int getAreaIndex(String html, String area) {
		int index = html.indexOf("id=\"" + area + "\"");
		if (index < 0) {
			Area areaBean = new Area();
			areaBean.setName(area);
			loadTemplatePart(areaBean, "area." + area);
			if (areaBean != null) {
				index = areaBean.getPriority();
			}
		}
		return index;
	}

	public String getAjaxAreas() {
		String areas = properties.getString("ajax.areas", null);
		if (areas == null) {
			return StringHelper.collectionToString(getAreas(), ",");
		} else {
			return areas;
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> getAreas() {
		if (areas == null) {
			Iterator<String> keys = properties.getKeys();
			String html = "";
			if (dir != null) {
				File indexFile = new File(URLHelper.mergePath(dir.getAbsolutePath(), getHTMLFile(null)));
				Template parent = getParent();
				while (!indexFile.exists() && parent != null && parent.dir != null) {
					indexFile = new File(URLHelper.mergePath(parent.dir.getAbsolutePath(), parent.getHTMLFile(null)));
					parent = parent.getParent();
				}
				if (indexFile.exists()) {
					try {
						html = ResourceHelper.loadStringFromFile(indexFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			List<SortBean<String>> workAreas = new LinkedList<SortBean<String>>();
			while (keys.hasNext()) {
				String key = keys.next();
				if (key.startsWith(XMLManipulationHelper.AREA_PREFIX) && StringUtils.countMatches(key, ".") < 2) {
					String area = key.substring(XMLManipulationHelper.AREA_PREFIX.length());
					int index = getAreaIndex(html, area);
					workAreas.add(new SortBean<String>(area, index));
				}
			}
			if (workAreas.size() == 0) {
				if (getParent() == null) {
					workAreas.add(new SortBean<String>(ComponentBean.DEFAULT_AREA, getAreaIndex(html, ComponentBean.DEFAULT_AREA)));
				} else {
					return getParent().getAreas();
				}
			}
			Collections.sort(workAreas);
			areas = SortBean.transformList(workAreas);
		}
		return areas;
	}

	public List<String> getAreas(boolean admin) {
		if (admin) {
			return getAreas();
		} else {
			if (notAdminAreas == null) {
				notAdminAreas = new LinkedList<String>();
				for (String area : getAreas()) {
					if (!isAdminArea(area)) {
						notAdminAreas.add(area);
					}
				}
				if (notAdminAreas.size() == 0) {
					if (getParent() == null) {
						notAdminAreas.add(ComponentBean.DEFAULT_AREA);
					} else {
						return getParent().getAreas(admin);
					}
				}
			}
			return notAdminAreas;
		}
	}

	public boolean isAdminArea(String area) {
		String key = XMLManipulationHelper.AREA_PREFIX + area + ".admin";
		if (properties.getProperty(key) != null) {
			return properties.getBoolean(key);
		} else {
			return false;
		}
	}

	public Set<String> getComponentsExclude() {
		String typeRAW = properties.getString("components.exclude");
		if (typeRAW == null) {
			return Collections.EMPTY_SET;
		} else {
			return new HashSet(StringHelper.stringToCollection(typeRAW, ";"));
		}
	}

	public Set<String> getComponentsIncludeForArea(String area) {
		String key = XMLManipulationHelper.AREA_PREFIX + area + ".components.include";
		String typeRAW = properties.getString(key);
		if (typeRAW == null) {
			return null;
		} else {
			return new HashSet(StringHelper.stringToCollection(typeRAW, ";"));
		}
	}

	public Set<String> getComponentsExcludeForArea(String area) {
		String key = XMLManipulationHelper.AREA_PREFIX + '.' + "components.exclude";
		String typeRAW = properties.getString(key);
		if (typeRAW == null) {
			return null;
		} else {
			return new HashSet(StringHelper.stringToCollection(typeRAW, ";"));
		}
	}

	public Map<String, String> getAreasMap() {
		Map<String, String> areas = new HashMap<String, String>();
		Iterator<String> keys = properties.getKeys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key.startsWith(XMLManipulationHelper.AREA_PREFIX) && !key.substring(XMLManipulationHelper.AREA_PREFIX.length()).contains(".")) {
				areas.put(key.substring(XMLManipulationHelper.AREA_PREFIX.length()), properties.getString(key));
			}
		}
		if (areas.size() == 0) {
			if (getParent() != null) {
				return getParent().getAreasMap();
			}
		}
		return areas;
	}

	public Map<String, String> getQuietableAreaMap() {
		String areasRaw = properties.getProperty("area-quietable");
		if (areasRaw == null) {
			return getParent().getQuietableAreaMap();
		}
		if (StringHelper.isEmpty(areasRaw)) {
			return Collections.EMPTY_MAP;
		} else {
			return new ListMapValueValue(StringHelper.stringToCollection(areasRaw, ","));
		}
	}

	public void setArea(String area, String id) {
		properties.setProperty(XMLManipulationHelper.AREA_PREFIX + area, id);
		storeProperties();
	}

	private void storeProperties() {
		try {
			properties.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void storePrivateProperties() {
		try {
			if (privateProperties != null) {
				privateProperties.save();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteArea(String area) {
		properties.clearProperty(XMLManipulationHelper.AREA_PREFIX + area);
		storeProperties();
		resetRows();
	}

	protected String getNewAreaName() {
		String prefixName = "zone";
		int areaNumber = 0;
		String areaName = prefixName + StringHelper.getNumberAsAlphabetic(areaNumber);
		while (properties.getProperty("area." + areaName) != null) {
			areaNumber++;
			areaName = prefixName + StringHelper.getNumberAsAlphabetic(areaNumber);
		}
		return areaName;
	}

	public void addArea(String rowName) {
		if (rowName.indexOf("-") >= 0) {
			String areaName = getNewAreaName();
			properties.setProperty("area." + areaName, areaName);
			properties.setProperty("area." + areaName + ".row", rowName);
			storeProperties();
			resetRows();
		}
	}

	public Row getRow(String name) {
		for (Row row : getRows()) {
			if (row.getName().equals(name)) {
				return row;
			}
		}
		return null;
	}

	public void addRow() {
		String rowName = null;
		String newRowName = null;
		for (int i = 1; i < 9999 && newRowName == null; i++) {
			rowName = "row-" + StringHelper.renderNumber(i, 4);
			if (getRow(rowName) == null) {
				newRowName = rowName;
			}
		}
		List<Row> rows = getRows();
		Row row = new Row(this);
		rows.add(row);
		row.setName(newRowName);
		Area area = new Area();
		area.setName(getNewAreaName());
		area.setRow(row);
		row.addArea(area);
		storeRows(rows);
		resetRows();
	}

	/**
	 * this area is display if specialrendere is defined
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> getAreasForceDisplay() {
		List<String> areas = new LinkedList<String>();
		Iterator<String> keys = properties.getKeys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key.startsWith(AREA_FORCE_DISPLAY_PREFIX)) {
				String area = key.substring(AREA_FORCE_DISPLAY_PREFIX.length());
				if (StringHelper.isTrue(properties.getString(key))) {
					areas.add(area);
				}
			}
		}
		if (areas.size() == 0) {
			if (getParent() != null) {
				return getParent().getAreasForceDisplay();
			}
		}
		return areas;
	}

	public String getAuthors() {
		return properties.getString("authors", getParent().getAuthors());
	}

	protected List<File> getComponentFile(GlobalContext globalContext) throws IOException {
		synchronized (globalContext.getLockImportTemplate()) {
			String templateFolder = getWorkTemplateFolder();

			String path = URLHelper.mergePath(URLHelper.mergePath(templateFolder, getFolder(globalContext)), DYNAMIC_COMPONENTS_PROPERTIES_FOLDER);
			File dynCompDir = new File(path);
			if (!dynCompDir.exists()) {
				return Collections.emptyList();
			}
			List<File> files = new LinkedList<File>();
			files.addAll(Arrays.asList(dynCompDir.listFiles(new PropertiesFilter())));
			files.addAll(Arrays.asList(dynCompDir.listFiles(new HTMLFileFilter())));
			return files;
		}
	}

	public Map<String, List<String>> getCSSByFolder(String filter) throws IOException {
		if (dir == null) {
			return Collections.EMPTY_MAP;
		}
		Map<String, List<String>> outCSS = new LinkedHashMap<String, List<String>>();
		config.getTemplateFolder();
		Collection<File> file = ResourceHelper.getAllFiles(dir, null);
		for (File cssFile : file) {
			String ext = StringHelper.neverNull(StringHelper.getFileExtension(cssFile.getName())).toLowerCase();
			if (ext.equals("css") || ext.equals("less") || ext.equals("scss")) {
				String cssFileName = cssFile.getAbsolutePath().replace(cssFile.getParentFile().getAbsolutePath(), "");
				boolean add = true;
				if (!StringHelper.isEmpty(filter)) {
					String content = ResourceHelper.loadStringFromFile(cssFile);
					if (!content.toLowerCase().contains(filter.toLowerCase())) {
						add = false;
					}
				}
				if (add) {
					String cssFolder = cssFile.getParentFile().getAbsolutePath().replace(dir.getAbsolutePath(), "");
					List<String> css = outCSS.get(cssFolder);
					if (css == null) {
						css = new LinkedList<String>();
						outCSS.put(cssFolder, css);
					}
					cssFileName = cssFileName.replace("\\", "/");
					if (cssFileName.startsWith("/")) {
						cssFileName = cssFileName.substring(1);
					}
					css.add(cssFileName);
				}
			}
		}
		for (List<String> files : outCSS.values()) {
			Collections.sort(files);
		}
		return outCSS;
	}

	protected Map<String, List<String>> getCSSByFolder() throws IOException {
		return getCSSByFolder(null);
	}

	protected List<String> getCSS() throws IOException {
		if (dir == null) {
			return Collections.EMPTY_LIST;
		}
		config.getTemplateFolder();
		Collection<File> file = ResourceHelper.getAllFiles(dir, null);
		List<String> css = new LinkedList<String>();
		for (File cssFile : file) {
			String ext = StringHelper.neverNull(StringHelper.getFileExtension(cssFile.getName())).toLowerCase();
			if (ext.equals("css") || ext.equals("less") || ext.equals("scss")) {
				String cssFileName = cssFile.getAbsolutePath().replace(dir.getAbsolutePath(), "");
				cssFileName = cssFileName.replace("\\", "/");
				if (cssFileName.startsWith("/")) {
					cssFileName = cssFileName.substring(1);
				}
				css.add(cssFileName);
			}
		}
		Collections.sort(css);
		return css;
	}

	/** return the css for wysiwyg layout **/
	public String getWysiwygCss() {
		return properties.getString("wysiwyg.css", getParent().getWysiwygCss());
	}

	public Properties getConfigComponentFile(GlobalContext globalContext, String type) throws IOException {
		String templateFolder = getWorkTemplateFolder();
		String path = URLHelper.mergePath(URLHelper.mergePath(templateFolder, getFolder(globalContext)), CONFIG_COMPONENTS_PROPERTIES_FOLDER, type + ".properties");
		File configFile = new File(path);
		if (configFile.exists()) {
			return ResourceHelper.loadProperties(configFile);
		}
		return null;
	}

	public Date getCreationDate() {
		String creationDateString = privateProperties.getString("creation-date", null);
		if (creationDateString == null) {
			setCreationDate(new Date());
			creationDateString = privateProperties.getString("creation-date", StringHelper.renderDate(new Date()));
		}
		try {
			return StringHelper.parseDate(creationDateString);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getDepth() {
		return properties.getInt("depth", getParent().getDepth());
	}

	public String getDescription(String lang) {
		if (lang != null) {
			return properties.getString("description." + lang, "");
		} else {
			return properties.getString("description", "");
		}
	}

	public String getDominantColor() {
		return properties.getString("color.dominant", getParent().getDominantColor());
	}

	public String getEscapeMenuId() {
		return properties.getString("nav.escape_id", getParent().getEscapeMenuId());
	}

	public final List<Properties> getDynamicComponentsProperties(GlobalContext globalContext) throws IOException {
		if (dynamicsComponents == null) {
			synchronized (globalContext.getLockImportTemplate()) {
				if (dynamicsComponents == null) {
					List<File> files = getComponentFile(globalContext);
					List<Properties> outProperties = new LinkedList<Properties>();
					Pattern fieldPattern = Pattern.compile("\\$\\{field.*?\\}");
					for (File file : files) {
						if (StringHelper.isProperties(file.getName())) {
							Properties prop = new Properties();
							InputStream in = new FileInputStream(file);
							Reader inReader = new InputStreamReader(in, ContentContext.CHARACTER_ENCODING);
							try {
								prop.load(inReader);
							} finally {
								ResourceHelper.closeResource(in);
								ResourceHelper.closeResource(inReader);
							}
							outProperties.add(prop);
						} else if (StringHelper.isHTMLStatic(file.getName())) {
							/* load dynamic component from HTML */
							logger.info("load dynamic component html : " + file);
							String html = ResourceHelper.loadStringFromFile(file);
							Properties properties = new Properties();
							if (html.contains(PREVIEW_EDIT_CODE)) {
								properties.setProperty("component.wrapped", "false");
							}
							properties.setProperty("component.type", StringHelper.getFileNameWithoutExtension(file.getName()));
							properties.setProperty("component.renderer", ResourceHelper.removePath(file.getAbsolutePath(), getFolder(globalContext)));
							Matcher matcher = fieldPattern.matcher(html);
							int i = 0;
							while (matcher.find()) {
								i++;
								String field = matcher.group();
								String[] data = field.substring(2, field.length() - 1).split("\\.");
								if (data.length >= 3) {
									properties.setProperty("field." + data[2] + ".order", "" + i);
									properties.setProperty("field." + data[2] + ".type", data[1]);
								}
							}
							outProperties.add(properties);
						}
					}
					dynamicsComponents = outProperties;
				}
			}
		}
		return dynamicsComponents;
	}

	public List<String> getEmailLinkFileList() {
		Locale[] locales = Locale.getAvailableLocales();
		List<String> outEmailLinkFileList = new ArrayList<String>();
		for (Locale locale : locales) {
			String file = getLinkEmailFileName(locale.getLanguage());
			if (isLinkEmail(locale.getLanguage())) {
				if (!outEmailLinkFileList.contains(file)) {
					outEmailLinkFileList.add(file);
				}
			}
		}
		outEmailLinkFileList.addAll(getParent().getEmailLinkFileList());
		return outEmailLinkFileList;
	}

	/**
	 * template can be change in some context, call this method for obtain the final
	 * Template to be use for rendering.
	 * 
	 * @param ctx
	 * @return final template for rendering.
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public Template getFinalTemplate(ContentContext ctx) throws Exception {
		if (ctx != null) {
			if (ctx.getRenderMode() == ContentContext.VIEW_MODE || ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
				if (isAlternativeTemplate(ctx)) {
					Template altTemplate = getAlternativeTemplate(StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext()), ctx);
					if (altTemplate != null) {
						return altTemplate;
					}
				}
			}
		}
		if (getMobileTemplate() != null && ctx.getDevice().isMobileDevice()) {
			Template mobileTemplate = getMobileTemplate(ctx.getGlobalContext().getStaticConfig(), ctx);
			if (mobileTemplate != null) {
				return mobileTemplate;
			}
		}
		return this;
	}

	public String getFolder(GlobalContext globalContext) {
		if (globalContext != null) {
			return getFolder(globalContext.getContextKey());
		} else {
			return getFolder((String) null);
		}
	}

	public String getFolder(String siteKey) {
		if (dir == null) {
			return null;
		} else {
			String siteFolder = __NO_CONTEXT;
			if (siteKey != null) {
				siteFolder = siteKey;
			}
			return dir.getName() + '/' + siteFolder;
		}
	}

	private File getRawCssFile(GlobalContext globalContext) {
		return new File(URLHelper.mergePath(getWorkTemplateRealPath(globalContext), RAW_CSS_FILE));
	}

	private String getRawCss(GlobalContext globalContext, File file) throws IOException {
		return ResourceHelper.loadStringFromFile(file);
	}

	private void appendRawCssFile(GlobalContext globalContext, String data, File file) throws IOException {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			out.println(data);
		} finally {
			ResourceHelper.closeResource(out);
		}
	}

	public Properties getFontReference(GlobalContext globalContext) throws IOException {
		File file = new File(URLHelper.mergePath(getWorkTemplateRealPath(globalContext), FONT_REFERENCE_FILE));
		return ResourceHelper.loadProperties(file);
	}

	private String getFontIncluding(GlobalContext globalContext) throws Exception {
		String outIncluding = "";
		Map<?, ?> fontList = getFontReference(globalContext);
		if (isAutoFontIncluding()) {
			outIncluding = "";
			String cssRaw = getRawCss(globalContext, getRawCssFile(globalContext));
			Set<Object> included = new HashSet<>();
			for (Object key : fontList.keySet()) {
				if (cssRaw != null && cssRaw.toString().contains(key.toString())) {
					if (!included.contains(key)) {
						outIncluding += fontList.get(key);
						included.add(key);
					}
				}
			}
		}
		if (globalContext.getTemplateData().getFontHeading() != null && fontList.get(globalContext.getTemplateData().getFontHeading()) != null) {
			outIncluding += fontList.get(globalContext.getTemplateData().getFontHeading());
		}
		if (globalContext.getTemplateData().getFontText() != null && fontList.get(globalContext.getTemplateData().getFontText()) != null) {
			outIncluding += fontList.get(globalContext.getTemplateData().getFontText());
		}
		return outIncluding;
	}

	private boolean isAutoFontIncluding() {
		return properties.getBoolean("font.auto-including", false);
	}

	public String getHomeRenderer(GlobalContext globalContext) {
		if (getHTMLHomeFile() == null) {
			return null;
		}
		String renderer = properties.getString("home-renderer", "home.jsp");
		File jspFile = new File(getTemplateTargetFolder(globalContext), renderer);
		if (!jspFile.exists()) {
			File HTMLFile = new File(URLHelper.mergePath(getTemplateTargetFolder(globalContext), getHTMLHomeFile()));
			logger.warning(jspFile + " not found, try to generate from " + HTMLFile);
			if (!HTMLFile.exists()) {
				logger.warning(HTMLFile + " not found.");
			}
			try {
				List<String> resources = new LinkedList<String>();
				int depth = XMLManipulationHelper.convertHTMLtoTemplate(globalContext, this, HTMLFile, jspFile, getMap(), getAreas(), resources, getTemplatePugin(globalContext), null, false, getFontIncluding(globalContext));
				setDepth(depth);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return renderer;
	}

	public String getVersion() {
		return properties.getString("version", "?");
	}

	public Integer getBootstrapVersion() {
		Integer version = properties.getInteger("bootstrap.verion", null);
		if (version == null) {
			return getParent().getBootstrapVersion();
		} else {
			return version;
		}
	}

	public boolean isColumnable() {
		return getColumnableRowTag() != null;
	}

	public List<Integer> getColumnableSizes() {
		if (sizes != null) {
			return sizes;
		}
		String sizesRaw = properties.getString("columnable.sizes", null);
		if (sizesRaw == null) {
			return getParent().getColumnableSizes();
		} else {
			List<Integer> outSizes = new LinkedList<Integer>();
			for (String strSize : StringHelper.stringToCollection(sizesRaw, ",")) {
				outSizes.add(Integer.parseInt(strSize));
			}
			sizes = outSizes;
			return outSizes;
		}
	}

	public String getColumnableRowTag() {
		String tag = properties.getString("columnable.row.tag", null);
		if (tag == null) {
			return getParent().getColumnableRowTag();
		} else {
			return tag;
		}
	}

	public String getColumnableRowTagIn() {
		String tag = properties.getString("columnable.row.in.tag", null);
		if (tag == null) {
			return getParent().getColumnableRowTagIn();
		} else {
			return tag;
		}
	}

	public String getColumnableColTagIn() {
		String tag = properties.getString("columnable.col.in.tag", null);
		if (tag == null) {
			return getParent().getColumnableColTagIn();
		} else {
			return tag;
		}
	}

	public String getColumnableStyleTagIn() {
		String tag = properties.getString("columnable.col.in.style", null);
		if (tag == null) {
			return getParent().getColumnableStyleTagIn();
		} else {
			return tag;
		}
	}

	public String getColumnableClassTagIn() {
		String tag = properties.getString("columnable.col.in.class", null);
		if (tag == null) {
			return getParent().getColumnableClassTagIn();
		} else {
			return tag;
		}
	}

	public String getColumnableColTag() {
		String tag = properties.getString("columnable.col.tag", null);
		if (tag == null) {
			return getParent().getColumnableColTag();
		} else {
			return tag;
		}
	}

	public String getColumnableRowStyle() {
		String tag = properties.getString("columnable.row.style", null);
		if (tag == null) {
			return getParent().getColumnableRowStyle();
		} else {
			return tag;
		}
	}

	public String getColumnableRowClass() {
		String cssClass = properties.getString("columnable.row.class", null);
		if (cssClass == null) {
			return getParent().getColumnableRowClass();
		} else {
			return cssClass;
		}
	}

	public String getColumnableColStyleDefault() {
		String style = properties.getString("columnable.col.style.default", null);
		if (style == null) {
			return getParent().getColumnableColStyleDefault();
		} else {
			return style;
		}
	}

	public String getColumnableColStyle(int size) {
		String style = properties.getString("columnable.col.style." + size, null);
		if (style == null) {
			return StringHelper.neverNull(getParent().getColumnableColStyle(size), getColumnableColStyleDefault());
		} else {
			return style;
		}
	}

	public String getColumnableColClass(int size, int totalSize, int leftSize) {
		String cssClass = properties.getString("columnable.col.class." + size, null);		
		if (cssClass == null) {
			return StringHelper.neverNull(getParent().getColumnableColClass(size, totalSize, leftSize), getColumnableColClassDefault());
		} else {
			cssClass = cssClass.replace("#size#", ""+size);
			cssClass = cssClass.replace("#total-size#", ""+totalSize);
			cssClass = cssClass.replace("#left-size#", ""+leftSize);
			return cssClass;
		}
	}

	public String getColumnableColClassDefault() {
		String cssClass = properties.getString("columnable.col.class.default", null);
		if (cssClass == null) {
			return getParent().getColumnableColClassDefault();
		} else {
			return cssClass;
		}
	}

	public String getHomeRendererFullName(GlobalContext globalContext) {
		if (dir == null) {
			logger.warning("no valid dir : " + dir);
			return null;
		}
		return URLHelper.mergePath(getLocalTemplateTargetFolder(globalContext), getHomeRenderer(globalContext));
	}

	protected String getHTMLFile(Device device) {
		String deviceRenderer = null;
		if (device != null) {
			deviceRenderer = properties.getString("html." + device.getCode(), getParent().getHTMLFile(device));
		}
		if (deviceRenderer != null && !deviceRenderer.equals("-1")) {
			logger.fine("device renderer found : " + deviceRenderer + " (template:" + getId() + " device:" + device + ")");
		} else {
			deviceRenderer = properties.getString("html", getParent().getHTMLFile(device));
		}
		if (!StringHelper.getFileExtension(deviceRenderer).equalsIgnoreCase("html")) {
			throw new SecurityException("template main html file must be a '.html' file.");
		}
		return deviceRenderer;
	}

	public String getHTMLFileParams(Device device) {
		String deviceRenderer = null;
		if (device != null) {
			deviceRenderer = properties.getString("html." + device.getCode() + ".params", null);
		}
		if (deviceRenderer != null) {
			logger.fine("device renderer found : " + deviceRenderer + " (template:" + getId() + " device:" + device + ")");
			return deviceRenderer;
		} else {
			String defaultRenderer = properties.getString("html.params", getParent().getHTMLFileParams(device));
			return defaultRenderer;
		}
	}

	public String getMenuRenderer(Device device) {
		String menuRenderer = null;
		if (device != null) {
			menuRenderer = properties.getString("menu." + device.getCode(), null);
		}
		if (menuRenderer != null) {
			logger.fine("device renderer found : " + menuRenderer + " (template:" + getId() + "");
		} else {
			String defaultRenderer = properties.getString("menu", getParent().getMenuRenderer(device));
			menuRenderer = defaultRenderer;
		}
		if (menuRenderer == null) {
			return null;
		} else {
			return menuRenderer;
		}
	}

	/***
	 * mail for send to user with : ${logo} : url to logo ${site} : global site
	 * title ${title} : title of the email ${text} : main text of the email
	 * ${action.link} : action button url ${action.text} : action button text
	 * ${email} : contact email ${root} : root url #cccccc : special color #555555 :
	 * title color #333333 : text color
	 * 
	 * @param globalContext
	 * @return
	 * @throws IOException
	 */
	public String getUserMailHtml(GlobalContext globalContext) throws IOException {
		File userMailFile = new File(URLHelper.mergePath(getTemplateTargetFolder(globalContext), "mail", "user.html"));
		if (userMailFile.exists()) {
			return ResourceHelper.loadStringFromFile(userMailFile);
		} else {
			return null;
		}
	}

	public String getAdminMailHtml(GlobalContext globalContext) throws IOException {
		File userMailFile = new File(URLHelper.mergePath(getTemplateTargetFolder(globalContext), "mail", "admin.html"));
		if (userMailFile.exists()) {
			return ResourceHelper.loadStringFromFile(userMailFile);
		} else {
			return null;
		}
	}

	public String getHTMLHomeFile() {
		return properties.getString("home", getParent().getHTMLHomeFile());
	}

	public String get404File() {
		return properties.getString("404", getParent().get404File());
	}

	public synchronized Map getI18nProperties(GlobalContext globalContext, Locale locale, int mode) throws IOException {
		String filePrefix = I18N_VIEW_FILE;
		if (mode == ContentContext.EDIT_MODE) {
			filePrefix = I18N_EDIT_FILE;
		}
		if (locale == null) {
			return null;
		}
		final String KEY = locale.getLanguage() + mode;
		Map propI18n = i18n.get(KEY);
		if (propI18n == null) {
			synchronized (globalContext.getLockImportTemplate()) {
				if (config != null) {
					File i18nFile = new File(URLHelper.mergePath(URLHelper.mergePath(getWorkTemplateRealPath(globalContext), filePrefix + locale.getLanguage() + ".properties")));
					if (i18nFile.exists()) {
						propI18n = new Properties();
						Reader reader = new FileReader(i18nFile);
						((Properties) propI18n).load(reader);
						reader.close();
					} else {
						i18nFile = new File(URLHelper.mergePath(URLHelper.mergePath(getWorkTemplateRealPath(globalContext), "i18n", filePrefix + locale.getLanguage() + ".properties")));
						if (i18nFile.exists()) {
							propI18n = new Properties();
							Reader reader = new FileReader(i18nFile);
							((Properties) propI18n).load(reader);
							reader.close();
						} else {
							propI18n = Collections.EMPTY_MAP;
						}
					}
					/*
					 * Map parentI18n = getParent().getI18nProperties(globalContext, locale, mode);
					 * if (parentI18n != null) { if (propI18n != null) {
					 * propI18n.putAll(parentI18n); } else { propI18n = parentI18n; } }
					 */
					i18n.put(KEY, propI18n);
				}
			}
		}
		return propI18n;
	}

	public synchronized Map<String, List<IListItem>> getAllList(GlobalContext globalContext, Locale locale) throws IOException {
		if (locale == null) {
			return null;
		}
		File listFolder = new File(URLHelper.mergePath(URLHelper.mergePath(getFolder().getAbsolutePath(), LIST_FOLDER)));
		if (!listFolder.isDirectory()) {
			return Collections.EMPTY_MAP;
		} else {
			Map<String, List<IListItem>> linkedMap = new HashMap<String, List<IListItem>>();
			for (File list : listFolder.listFiles((FilenameFilter) new FileFilterUtils().suffixFileFilter(".properties"))) {
				String lg = StringHelper.getLanguageFromFileName(list.getName());
				if (list.isFile() && (lg == null || lg.equals(locale.getLanguage()))) {
					if (locale.getLanguage().equals(lg) || linkedMap.get(StringHelper.getFileNameWithoutExtension(list.getName())) == null) {
						Properties listProp = new Properties();
						Reader reader = new FileReader(list);
						listProp.load(reader);
						reader.close();
						List<IListItem> serviceList = new LinkedList<IListItem>();

						for (Map.Entry entry : listProp.entrySet()) {
							serviceList.add(new ListService.ListItem(entry));
						}

						Collections.sort(serviceList, new ListService.OrderList());

						String listKey = StringHelper.getFileNameWithoutExtension(list.getName());
						if (lg != null) {
							listKey = listKey.substring(0, listKey.length() - 3); /*
																					 * remove language from file name
																					 */
						}
						linkedMap.put(listKey, serviceList);
					}
				}
			}
			return linkedMap;
		}
	}

	public String getId() {
		if (dir == null) {
			return null;
		} else {
			return dir.getName();
		}
	}

	public File getImageConfigFile() {
		File templateImageConfigFile = new File(URLHelper.mergePath(getTemplateRealPath(), getImageConfigFileName()));
		return templateImageConfigFile;
	}

	public ConfigurationProperties getImageConfig() {
		File templateImageConfigFile = getImageConfigFile();
		if (templateImageConfigFile.exists()) {
			try {
				ConfigurationProperties templateProperties = new ConfigurationProperties();
				templateProperties.load(templateImageConfigFile);
				if (isParent()) {
					ConfigurationProperties templatePropertiesParent = getParent().getImageConfig();
					if (templatePropertiesParent != null) {
						Iterator keys = templatePropertiesParent.getKeys();
						while (keys.hasNext()) {
							String key = (String) keys.next();
							try {
								if (templateProperties.getProperty(key) == null) {
									if (templatePropertiesParent.getProperty(key) != null && templatePropertiesParent.getProperty(key).toString().trim().length() > 0) {
										templateProperties.setProperty(key, templatePropertiesParent.getProperty(key));
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				return templateProperties;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (isParent()) {
			return getParent().getImageConfig();
		}
		return null;
	}

	public String getImageConfigFileName() {
		return properties.getString("image-config", getParent().getImageConfigFileName());
	}

	public List<String> getImageFilters() {
		String filterRaw = getImageFiltersRAW();
		if (filterRaw == null) {
			return Collections.emptyList();
		}
		List<String> outFilter = new LinkedList<String>(Arrays.asList(StringHelper.stringToArray(filterRaw, ";")));
		return outFilter;
	}

	public String getImageFiltersRAW() {
		return properties.getString("images-filter", getParent().getImageFiltersRAW());
	}

	public String getDefaultImageFilter() {
		List<String> filters = getImageFilters();
		String defaultFilter = "full";
		if (filters.size() > 0 && !filters.contains("full")) {
			defaultFilter = filters.iterator().next();
		}
		return properties.getString("images-default-filter", defaultFilter);
	}

	public List<String> getPageTypes() {
		String typesRaw = getPageTypesRAW();
		if (typesRaw == null) {
			return Collections.emptyList();
		}
		List<String> outFilter = new LinkedList<String>(Arrays.asList(StringHelper.stringToArray(typesRaw, ";")));
		return outFilter;
	}

	public String getPageTypesRAW() {
		return properties.getString("page-types", getParent().getPageTypesRAW());
	}

	public String getSpecialAreaRenderer() {
		String renderer = properties.getString("area-renderer", getParent().getSpecialAreaRenderer());
		if (StringHelper.isEmpty(renderer)) {
			return null;
		} else {
			return renderer;
		}
	}

	public void setImageFiltersRAW(String imageFilterRAW) {
		properties.setProperty("images-filter", imageFilterRAW);
	}

	public String getLastSelectedClass() {
		return properties.getString("class.selected.last", getParent().getLastSelectedClass());
	}

	public String getLicenceFile() {
		return properties.getString("licence", getParent().getLicenceFile());
	}

	public File getLinkEmail(String lg) {
		String templateFolder = config.getTemplateFolder();
		File linkEmailFile = new File(URLHelper.mergePath(URLHelper.mergePath(templateFolder, getSourceFolderName()), getLinkEmailFileName(lg)));
		return linkEmailFile;

	}

	public String getLinkEmailFileName(String lg) {
		String linkEmailFile = properties.getString("link-email", LINK_EMAIL_FILE).replace("[lg]", lg);
		return linkEmailFile;
	}

	private String getLocalTemplateTargetFolder(GlobalContext globalContext) {
		String templateTgt = URLHelper.mergePath(getLocalWorkTemplateFolder(), getFolder(globalContext));
		return templateTgt;
	}

	public String getLocalWorkMailingTemplateFolder() {
		return properties.getString("work-mailing-folder", "/work_mailing_template");
	}

	public String getLocalWorkTemplateFolder() {
		return properties.getString("work-folder", "/" + DEFAULT_TEMPLATE_NAME);
	}

	public final File getMacroFile(GlobalContext globalContext, String fileName) throws IOException {
		String templateFolder = getWorkTemplateFolder();
		String path = URLHelper.mergePath(URLHelper.mergePath(templateFolder, getFolder(globalContext)), MACRO_FOLDER, fileName);
		File macroFile = new File(path);
		return macroFile;
	}

	private final List<File> getMacroFile(GlobalContext globalContext) throws IOException {
		String templateFolder = getWorkTemplateFolder();

		String path = URLHelper.mergePath(URLHelper.mergePath(templateFolder, getFolder(globalContext)), MACRO_FOLDER);
		File dynCompDir = new File(path);
		if (!dynCompDir.exists()) {
			return Collections.emptyList();
		}
		File[] propertiesFile = dynCompDir.listFiles(new PropertiesFilter());
		return Arrays.asList(propertiesFile);
	}

	public Properties getMacroProperties(GlobalContext globalContext, String macroKey) throws IOException {
		synchronized (globalContext.getLockImportTemplate()) {
			List<File> macroFiles = getMacroFile(globalContext);
			for (File pFile : macroFiles) {
				if (pFile.getName().equals(macroKey + ".properties")) {
					Properties prop = new Properties();
					InputStream in = new FileInputStream(pFile);
					try {
						prop.load(in);
					} finally {
						ResourceHelper.closeResource(in);
					}
					return prop;
				}
			}
			return null;
		}
	}

	public Mail getMail(ContentContext ctx, String mailName, String lg) throws IOException {
		String folder = URLHelper.mergePath(getTemplateRealPath(), MAIL_FOLDER);
		File htmlFile = new File(URLHelper.mergePath(folder, mailName + '-' + lg + ".html"));

		if (!htmlFile.exists()) {
			logger.warning("html file not found : " + htmlFile);
			return null;
		} else {
			String content = FileUtils.readFileToString(htmlFile, ContentContext.CHARACTER_ENCODING);
			String subject = "";
			File subjectFile = new File(URLHelper.mergePath(folder, mailName + '-' + lg + ".txt"));
			if (subjectFile.exists()) {
				subject = FileUtils.readFileToString(subjectFile, ContentContext.CHARACTER_ENCODING);
			}
			return new Mail(subject, content);
		}
	}

	public File getMailJsp(ContentContext ctx, String mailName) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		File htmlFile = new File(URLHelper.mergePath(getFolder(globalContext), MAIL_FOLDER) + mailName + '-' + ctx.getContentLanguage() + ".html");
		File jspFile = new File(URLHelper.mergePath(getFolder(globalContext), MAIL_FOLDER) + mailName + '-' + ctx.getContentLanguage() + ".jsp");
		if (!htmlFile.exists()) {
			return null;
		} else {
			if (jspFile.exists()) {
				return jspFile;
			} else {
				XMLManipulationHelper.convertHTMLtoMail(htmlFile, this, jspFile);
				return jspFile;
			}
		}
	}

	public String getMailSubject(String lg) {
		return properties.getString("mail.subject." + lg, "");
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> getMap() {
		Map<String, String> out = new HashMap<String, String>();
		Iterator<String> keys = properties.getKeys();
		while (keys.hasNext()) {
			String key = keys.next();
			out.put(key, properties.getString(key));
		}

		if (getParent() != null) {
			for (Map.Entry<String, String> parentEntry : getParent().getMap().entrySet()) {
				if (!out.containsKey(parentEntry.getKey())) {
					out.put(parentEntry.getKey(), parentEntry.getValue());
				}
			}
		}

		return out;
	}

	public String getName() {
		if (fakeName != null) {
			return fakeName;
		}
		if (dir == null) {
			return "";
		} else {
			return properties.getString("name", dir.getName());
		}
	}

	public String getOwner() {
		return privateProperties.getString("owner", getParent().getOwner());
	}

	public Template getParent() {
		if (parent == null || parent.getName().equals(getName())) {
			return DefaultTemplate.INSTANCE;
		}
		return parent;
	}

	private Template getParent(StaticConfig config, ContentContext ctx, Set<String> parents) throws Exception {
		if (parents == null) {
			parents = new HashSet<String>();
		}
		Template parent = null;
		String parentId = getParentName();

		if (parents.contains(parentId)) {
			logger.warning("template " + getName() + " create cylce with parent:" + parentId);
			return null;
		}
		parents.add(parentId);

		if (parentId != null && !parentId.equals(getName())) {

			if (ctx == null) {
				parent = Template.getInstance(config, ctx, parentId, false, parents);
			} else {
				parent = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(parentId);
			}
			/*
			 * if (parent == null) { throw new ConfigurationException("parent not found : "
			 * + parent); }
			 */
		}
		return parent;
	}

	public String getParentName() {
		return properties.getString("parent", null);
	}

	public void setParentName(String parent) {
		synchronized (properties) {
			properties.setProperty("parent", parent);
			storeProperties();
		}
	}

	public String getMessageContainerId() {
		String htmlID = properties.getProperty("renderer.message.htmlid");
		if (htmlID == null) {
			return getParent().getMessageContainerId();
		} else {
			return htmlID;
		}
	}

	public String getMessageTemplate(ContentContext ctx) {
		String renderer = properties.getProperty("renderer.message");
		if (renderer == null) {
			return getParent().getMessageTemplate(ctx);
		} else {
			return URLHelper.mergePath(getTemplateTargetFolder(ctx.getGlobalContext()), renderer);
		}
	}

	public synchronized String getRenderer(ContentContext ctx) throws Exception {
		synchronized (ctx.getGlobalContext().getLockImportTemplate()) {
			String renderer = getRendererFile(ctx.getDevice());
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String jspPath = URLHelper.mergePath(getTemplateTargetFolder(globalContext), renderer);
			File jspFile = new File(jspPath);
			if (!jspFile.exists()) {
				importTemplateInWebapp(globalContext.getStaticConfig(), ctx);
				File HTMLFile = new File(URLHelper.mergePath(getTemplateTargetFolder(globalContext), getHTMLFile(ctx.getDevice())));
				logger.info(jspFile + " not found, try to generate from " + HTMLFile);
				if (!HTMLFile.exists()) {
					logger.warning(HTMLFile + " not found.");
				}
				List<String> resources = new LinkedList<String>();
				List<String> ids = new LinkedList<String>();
				if (!HTMLFile.exists()) {
					contextWithTemplateImported.remove(ctx.getGlobalContext().getContextKey());
					FileUtils.deleteDirectory(HTMLFile.getParentFile());
					importTemplateInWebapp(config, ctx);
				}
				int depth = XMLManipulationHelper.convertHTMLtoTemplate(globalContext, this, HTMLFile, jspFile, getMap(), getAreas(), resources, getTemplatePugin(globalContext), ids, isMailing(), getFontIncluding(globalContext));
				setHTMLIDS(ids);
				setDepth(depth);
			}
			return renderer;
		}
	}

	private List<String> getAllPluginsName(GlobalContext globalContext) {
		TemplatePluginFactory templatePluginFactory = TemplatePluginFactory.getInstance(globalContext.getServletContext());
		List<String> plugins = new LinkedList<String>(globalContext.getTemplatePlugin());
		plugins.addAll(getPlugins());
		return plugins;
	}

	private List<TemplatePlugin> getTemplatePugin(GlobalContext globalContext) throws IOException {
		TemplatePluginFactory templatePluginFactory = TemplatePluginFactory.getInstance(globalContext.getServletContext());
		return templatePluginFactory.getAllTemplatePlugin(getAllPluginsName(globalContext));
	}

	public synchronized String getRenderer(ContentContext ctx, String file) throws Exception {
		String renderer = properties.getString("renderer." + file, getParent().getRenderer(ctx, file));
		if (renderer == null) {
			return null;
		}
		return URLHelper.createStaticTemplateURLWithoutContext(ctx, this, renderer);
	}

	protected String getRSSRendererFile() {
		String renderer = properties.getString("renderer.rss", getParent().getRSSRendererFile());
		return renderer;
	}

	public String getRSSRendererFullName(ContentContext ctx) throws ServiceException {
		GlobalContext globalContext = null;
		if (ctx != null) {
			globalContext = GlobalContext.getInstance(ctx.getRequest());
		}
		if (dir == null) {
			logger.warning("no valid dir : " + dir);
			return null;
		}
		String renderer = null;
		try {
			renderer = getRSSRendererFile();
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		if (renderer == null) {
			return null;
		}
		return URLHelper.mergePath(getLocalTemplateTargetFolder(globalContext), renderer);
	}

	protected String getRendererFile(Device device) {
		String renderer = properties.getString("renderer", null);
		if (renderer == null) {
			renderer = getParent().getRendererFile(device);
		} else {
			if (device != null && !device.isDefault()) {
				renderer = StringHelper.addSufixToFileName(renderer, '-' + device.getCode());
			}
		}
		return renderer;
	}

	protected boolean isDefaultRenderer() {
		return StringHelper.isTrue(properties.getProperty("renderer.default"), getParent().isDefaultRenderer());
	}

	/**
	 * return all renderer defined in the template.
	 * 
	 * @return
	 */
	public List<String> getRenderers() {
		List<String> outRenderes = new LinkedList<String>();
		Iterator keys = properties.getKeys();
		if (isDefaultRenderer()) {
			outRenderes.add("");
		}
		boolean unvalidatedRenderer = false;
		boolean rendererDefined = false;
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (key.startsWith("html.")) {
				String renderer = key.replaceFirst("html.", "");
				if (!renderer.contains(".") && !renderer.equals("params")) {
					if (properties.getString(key).equals("-1")) {
						unvalidatedRenderer = true;
					} else {
						outRenderes.add(renderer);
						rendererDefined = true;
					}
				}
			}
		}
		if (!rendererDefined && !unvalidatedRenderer) {
			return getParent().getRenderers();
		}
		return outRenderes;
	}

	/**
	 * return all renderer defined in the template.
	 * 
	 * @return
	 */
	public List<String> getLayouts() {
		String rawLayouts = properties.getProperty("layouts");
		if (rawLayouts == null) {
			return getParent().getLayouts();
		} else {
			return StringHelper.stringToCollection(rawLayouts, ",");
		}
	}

	/**
	 * return all renderer defined in the template.
	 * 
	 * @return
	 */
	public List<String> getStructures() {
		String rawStructures = properties.getProperty("structures");
		if (rawStructures == null) {
			return getParent().getStructures();
		} else {
			return StringHelper.stringToCollection(rawStructures, ",");
		}
	}

	public String getRendererFullName(ContentContext ctx) throws ServiceException {
		GlobalContext globalContext = null;
		if (ctx != null) {
			globalContext = GlobalContext.getInstance(ctx.getRequest());
		}
		if (dir == null) {
			logger.warning("no valid dir : " + dir);
			return null;
		}
		String renderer = null;
		try {
			renderer = getRenderer(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (renderer == null) {
			logger.warning("renderer not found on template : " + getName() + " (parent:" + getParent() + " device:" + ctx.getDevice() + ")");
			return null;
		}
		String params = "?_menufix=" + ctx.getGlobalContext().getTemplateData().isFixMenu();
		params += "&_menularge=" + ctx.getGlobalContext().getTemplateData().isLargeMenu();
		params += "&_menusearch=" + ctx.getGlobalContext().getTemplateData().isSearchMenu();
		params += "&_menujssearch=" + ctx.getGlobalContext().getTemplateData().isJssearchMenu();
		params += "&_menudropdown=" + ctx.getGlobalContext().getTemplateData().isDropdownMenu();
		params += "&_menulogin=" + ctx.getGlobalContext().getTemplateData().isLoginMenu();
		String templateParams = getHTMLFileParams(ctx.getDevice());
		if (templateParams != null) {
			params = params + '&' + templateParams;
		}
		return URLHelper.mergePath(getLocalTemplateTargetFolder(globalContext), renderer) + params;
	}

	public List<String> getResources() {
		List<String> outResources = new ArrayList<String>();
		Collection<File> allFiles = FileUtils.listFiles(dir, null, true);
		for (File file : allFiles) {
			if (!file.getName().equals(PRIVATE_CONFIG_FILE) && !file.getName().endsWith("~")) {
				outResources.add(file.getAbsolutePath().replace(dir.getAbsolutePath(), ""));
			}
		}
		return outResources;
	}

	/**
	 * get resources define in the template.
	 * 
	 * @return
	 */
	public Collection<File> getResources(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		File resourcesDir = new File(URLHelper.mergePath(URLHelper.mergePath(getWorkTemplateFolder(), getFolder(globalContext)), RESOURCES_DIR));
		if (!resourcesDir.exists()) {
			return Collections.EMPTY_LIST;
		} else {
			return ResourceHelper.getAllFilesList(resourcesDir);
		}
	}

	public String getRssCSS() {
		return properties.getString("rss.css", null);
	}

	public String getSearchFormID() {
		return properties.getString("tagid.form.search", getParent().getSearchFormID());
	}

	public String getSearchRenderer(ContentContext ctx) throws Exception {
		String renderer = properties.getString("renderer.search", null);
		Template parent = getParent();
		if (renderer == null && parent != null) {
			return parent.getSearchRenderer(ctx);
		}
		return URLHelper.createStaticTemplateURLWithoutContext(ctx, ctx.getCurrentTemplate(), renderer);
	}

	public String getSelectedClass() {
		return properties.getString("class.selected", getParent().getSelectedClass());
	}

	public String getSource() {
		return properties.getString("source", getParent().getSource());
	}

	/**
	 * mode of the edit template, can be used in template renderer for include
	 * special css or js. preview css is : edit_preview_[mode].css
	 * 
	 * @return
	 */
	public String getEditTemplateMode() {
		return properties.getString("edit-template.mode", "dark");
	}

	public File getSourceFolder() {
		return dir;
	}

	public String getSourceFolderName() {
		if (dir == null) {
			return null;
		} else {
			return dir.getName();
		}
	}

	public File getFolder() {
		return new File(URLHelper.mergePath(config.getTemplateFolder(), getSourceFolderName()));
	}

	public String getSpecialRendererTemplate() {
		return properties.getString("special-renderer.template", null);
	}

	public TemplateData getTemplateData() {
		TemplateData templateData = new TemplateData();

		boolean templateDataDefined = false;
		Iterator<String> keys = properties.getKeys();
		while (keys.hasNext()) {
			if (keys.next().startsWith("data.")) {
				templateDataDefined = true;
			}
		}
		if (!templateDataDefined) {
			return getParent().getTemplateData();
		} else {
			String background = properties.getString("data.color.background", null);
			if (background != null) {
				Color backgroundColor = Color.decode('#' + background);
				templateData.setBackground(backgroundColor);
			}
			for (int i = 0; i < 6; i++) {
				String color = properties.getString("data.color.colorList" + i, null);
				if (!StringHelper.isEmpty(color)) {
					templateData.setColorList(Color.decode('#' + color), i);
				}
			}
			String foreground = properties.getString("data.color.foreground", null);
			if (foreground != null) {
				Color foregroundColor = Color.decode('#' + foreground);
				templateData.setForeground(foregroundColor);
			}
			String text = properties.getString("data.color.text", null);
			if (text != null) {
				Color color = Color.decode('#' + text);
				templateData.setText(color);
			}
			String border = properties.getString("data.color.border", null);
			if (border != null) {
				Color color = Color.decode('#' + border);
				templateData.setBorder(color);
			}
			String textMenu = properties.getString("data.color.menu.text", null);
			if (textMenu != null) {
				Color color = Color.decode('#' + textMenu);
				templateData.setTextMenu(color);
			}
			String backgroundMenu = properties.getString("data.color.menu.background", null);
			if (backgroundMenu != null) {
				Color color = Color.decode('#' + backgroundMenu);
				templateData.setBackgroundMenu(color);
			}
			String backgroundActive = properties.getString("data.color.menu.active", null);
			if (backgroundActive != null) {
				Color color = Color.decode('#' + backgroundActive);
				templateData.setBackgroundActive(color);
			}
			String title = properties.getString("data.color.title", null);
			if (title != null) {
				Color color = Color.decode('#' + title);
				templateData.setTitle(color);
			}
			String special = properties.getString("data.color.special", null);
			if (special != null) {
				Color color = Color.decode('#' + special);
				templateData.setSpecial(color);
			}
			String link = properties.getString("data.color.link", null);
			if (link != null) {
				Color color = Color.decode('#' + link);
				templateData.setLink(color);
			}
			String toolsURL = properties.getString("data.server.tools", null);
			if (toolsURL != null) {
				templateData.setToolsServer(toolsURL);
			}
			String logo = properties.getString("data.logo", null);
			if (logo != null && !logo.equals("null")) {
				templateData.setLogo(logo);
			}
			String fontText = properties.getString("data.font.text", null);
			if (fontText != null && !fontText.equals("null")) {
				templateData.setFontText(fontText);
			}
			String fontHeading = properties.getString("data.font.heading", null);
			if (fontHeading != null && !fontHeading.equals("null")) {
				templateData.setFontHeading(fontHeading);
			}

			/** messages **/
			String messagePrimary = properties.getString("data.color.message.primary", null);
			if (messagePrimary != null) {
				Color color = Color.decode('#' + messagePrimary);
				templateData.setMessagePrimary(color);
			}
			String messageSecondary = properties.getString("data.color.message.secondary", null);
			if (messageSecondary != null) {
				Color color = Color.decode('#' + messageSecondary);
				templateData.setMessageSecondary(color);
			}
			String messageSuccess = properties.getString("data.color.message.success", null);
			if (messageSuccess != null) {
				Color color = Color.decode('#' + messageSuccess);
				templateData.setMessageSuccess(color);
			}
			String messageWarning = properties.getString("data.color.message.warning", null);
			if (messageWarning != null) {
				Color color = Color.decode('#' + messageWarning);
				templateData.setMessageWarning(color);
			}
			String messageDanger = properties.getString("data.color.message.danger", null);
			if (messageDanger != null) {
				Color color = Color.decode('#' + messageDanger);
				templateData.setMessageDanger(color);
			}
			String messageInfo = properties.getString("data.color.message.info", null);
			if (messageInfo != null) {
				Color color = Color.decode('#' + messageInfo);
				templateData.setMessageInfo(color);
			}
			String componentBackground = properties.getString("data.color.component-background", null);
			if (componentBackground != null) {
				Color color = Color.decode('#' + componentBackground);
				templateData.setComponentBackground(color);
			}

			return templateData;
		}
	}

	private Map<String, String> getTemplateDataMap(GlobalContext globalContext) {
		if (globalContext == null) {
			return Collections.EMPTY_MAP;
		}
		TemplateData templateDataUser = globalContext.getTemplateData();
		Map<String, String> templateDataMap = new HashMap<String, String>();
		templateDataMap.putAll(getFreeData());
		TemplateData templateData = getTemplateData();
		if (templateData.getBackground() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getBackground()), StringHelper.colorToHexStringNotNull(templateDataUser.getBackground()));
		}

		for (int i = 0; i < 6; i++) {
			if (templateData.getColorList()[i] != null) {
				templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getColorList()[i]), StringHelper.colorToHexStringNotNull(templateDataUser.getColorList()[i]));
			}
		}

		if (templateData.getForeground() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getForeground()), StringHelper.colorToHexStringNotNull(templateDataUser.getForeground()));
		}
		if (templateData.getText() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getText()), StringHelper.colorToHexStringNotNull(templateDataUser.getText()));
		}
		if (templateData.getBackgroundMenu() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getBackgroundMenu()), StringHelper.colorToHexStringNotNull(templateDataUser.getBackgroundMenu()));
		}

		if (templateData.getBackgroundActive() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getBackgroundActive()), StringHelper.colorToHexStringNotNull(templateDataUser.getBackgroundActive()));
		}
		if (templateData.getTextMenu() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getTextMenu()), StringHelper.colorToHexStringNotNull(templateDataUser.getTextMenu()));
		}
		if (templateData.getLink() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getLink()), StringHelper.colorToHexStringNotNull(templateDataUser.getLink()));
		}
		if (templateData.getBorder() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getBorder()), StringHelper.colorToHexStringNotNull(templateDataUser.getBorder()));
		}
		if (templateData.getComponentBackground() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getComponentBackground()), StringHelper.colorToHexStringNotNull(templateDataUser.getComponentBackground()));
		}
		if (templateData.getTitle() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getTitle()), StringHelper.colorToHexStringNotNull(templateDataUser.getTitle()));
		}
		if (templateData.getSpecial() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getSpecial()), StringHelper.colorToHexStringNotNull(templateDataUser.getSpecial()));
		}
		if (templateData.getToolsServer() != null) {
			templateDataMap.put(templateData.getToolsServer(), templateDataUser.getToolsServer());
		}
		if (templateData.getFontText() != null) {
			templateDataMap.put(templateData.getFontText(), templateDataUser.getFontText());
		}
		if (templateData.getFontHeading() != null) {
			templateDataMap.put(templateData.getFontHeading(), templateDataUser.getFontHeading());
		}
		/** messages **/
		if (templateData.getMessagePrimary() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getMessagePrimary()), StringHelper.colorToHexStringNotNull(templateDataUser.getMessagePrimary()));
		}
		if (templateData.getMessageSecondary() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getMessageSecondary()), StringHelper.colorToHexStringNotNull(templateDataUser.getMessageSecondary()));
		}
		if (templateData.getMessageDanger() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getMessageDanger()), StringHelper.colorToHexStringNotNull(templateDataUser.getMessageDanger()));
		}
		if (templateData.getMessageSuccess() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getMessageSuccess()), StringHelper.colorToHexStringNotNull(templateDataUser.getMessageSuccess()));
		}
		if (templateData.getMessageInfo() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getMessageInfo()), StringHelper.colorToHexStringNotNull(templateDataUser.getMessageInfo()));
		}
		if (templateData.getMessageWarning() != null) {
			templateDataMap.put(StringHelper.colorToHexStringNotNull(templateData.getMessageWarning()), StringHelper.colorToHexStringNotNull(templateDataUser.getMessageWarning()));
		}
		return templateDataMap;
	}

	public String getTemplateRealPath() {
		if (dir == null) {
			return null;
		}
		String templateFolder = config.getTemplateFolder();
		return URLHelper.mergePath(templateFolder, getSourceFolderName());
	}

	public String getWorkTemplateRealPath(GlobalContext globalContext) {
		return URLHelper.mergePath(getWorkTemplateFolder(), getFolder(globalContext));
	}

	public boolean isDeleted() {
		if (dir == null || !dir.exists()) {
			return true;
		} else {
			return false;
		}
	}

	private String getTemplateTargetFolder(GlobalContext globalContext) {
		String templateTgt = URLHelper.mergePath(getWorkTemplateFolder(), getFolder(globalContext));
		return templateTgt;
	}

	public String getUnSelectedClass() {
		return properties.getString("class.unselected", getParent().getUnSelectedClass());
	}

	public String getVisualFile() {
		return properties.getString("file.visual", getParent().getVisualFile());
	}

	public File getVisualAbsoluteFile() {
		return new File(dir.getAbsolutePath(), getVisualFile());
	}

	public String getVisualPDFile() {
		return properties.getString("file.pdf", getParent().getVisualPDFile());
	}

	public String getWorkMailingTemplateFolder() {
		return config.getRealPath(getLocalWorkMailingTemplateFolder());
	}

	public String getWorkTemplateFolder() {
		return config.getRealPath(getLocalWorkTemplateFolder());
	}

	private Object getLockImport(GlobalContext globalContext) {
		Object lockImport = this;
		if (globalContext != null) {
			lockImport = globalContext.getLockImportTemplate();
		}
		return lockImport;
	}
	
	public int rebuildTemplate(ContentContext ctx, boolean css) {
		File target = new File(getTemplateTargetFolder(ctx.getGlobalContext()));
		File indexFile = new File(URLHelper.mergePath(target.getAbsolutePath(), "index.jsp"));
		int outCount=0;
		if (indexFile.exists()) {
			indexFile.delete();
			outCount++;
		}
		if (css) {
			outCount = outCount + rebuildTemplateCss(ctx, target);
		}		
		return outCount;
	}
	
	protected int rebuildTemplateCss(ContentContext ctx, File folder) {
		int outCount=0;
		for (File f : folder.listFiles()) {
			if (f.isDirectory()) {
				outCount = outCount+rebuildTemplateCss(ctx, f);
			} else {
				if (f.getName().toLowerCase().endsWith(".css")) {
					File scss = new File(StringHelper.replaceFileExtension(f.getAbsolutePath(), ".scss"));
					File less = new File(StringHelper.replaceFileExtension(f.getAbsolutePath(), ".less"));
					if (less.exists() || scss.exists()) {
						System.out.println(">>>>>>>>> Template.rebuildTemplateCss : DELETE : "+f); //TODO: remove debug trace
						f.delete();
						outCount++;
					}
				}
			}
		}
		return outCount;
	}

	public void importTemplateInWebapp(StaticConfig config, ContentContext ctx) throws IOException {
		if (templateErrorMap.containsKey(getName())) {
			return;
		}
		GlobalContext globalContext = null;
		if (ctx != null) {
			globalContext = GlobalContext.getInstance(ctx.getRequest());
		}
		synchronized (getLockImport(globalContext)) {
			if (!isTemplateInWebapp(ctx)) {
				String templateFolder = config.getTemplateFolder();
				File templateSrc = new File(URLHelper.mergePath(templateFolder, getSourceFolderName()));
				if (templateSrc.exists()) {
					File templateTgt = new File(getTemplateTargetFolder(globalContext));
					long startTime = System.currentTimeMillis();
					logger.info("copy template from '" + templateSrc + "' to '" + templateTgt + "'");
					FileUtils.deleteDirectory(templateTgt);
					importTemplateInWebapp(config, ctx, globalContext, templateTgt, null, true, false, getRawCssFile(globalContext), null);
					logger.info("import template : " + getName() + " in " + StringHelper.renderTimeInSecond(System.currentTimeMillis() - startTime));
				} else {
					logger.severe("folder not found : " + templateSrc+"   templateImportationError="+templateErrorMap.containsKey(getName()));
					templateErrorMap.put(getName(), getFolder());
				}
			}
		}
	}

	protected void importTemplateInWebapp(StaticConfig config, ContentContext ctx, GlobalContext globalContext, File templateTarget, Map<String, String> childrenData, boolean compressResource, boolean parent, File inRawCssFile, String logoUrl) throws IOException {

		String templateFolder = config.getTemplateFolder();
		File templateSrc = new File(URLHelper.mergePath(templateFolder, getSourceFolderName()));
		if (templateSrc.exists()) {
			logger.info("copy parent template from '" + templateSrc + "' to '" + templateTarget + "'");
			// FileUtils.copyDirectory(templateSrc, templateTarget, new
			// WEBFileFilter(this, false, jsp, true), false);
			//ResourceHelper.copyDir(templateSrc, templateTarget, false, new WEBFileFilter(this, false, jsp, true));
			/** filter html and css **/			
			Collection<File> allFiles = ResourceHelper.getAllFilesList(templateSrc);

			/** plugins **/
			if (globalContext != null && !parent) {
				Collection<String> currentPlugin = getAllPluginsName(globalContext);
				if (currentPlugin.size() > 0) {
					TemplatePluginFactory templatePluginFactory = TemplatePluginFactory.getInstance(ctx.getRequest().getSession().getServletContext());
					for (String pluginId : currentPlugin) {
						TemplatePlugin plugin = templatePluginFactory.getTemplatePlugin(pluginId);
						if (plugin != null) {
							plugin.importInTemplate(ctx, templateTarget);
						}
					}
				}
			}

			Map<String, String> map = getTemplateDataMap(globalContext);
			map.put("##BUILD_ID##", getBuildId());
			if (childrenData != null) {
				map.putAll(childrenData);
			}
			if (globalContext != null && globalContext.getTemplateData() != null) {
				String newLogo = globalContext.getTemplateData().getLogo();
				if (newLogo != null) {
					StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
					ContentContext absoluteURLCtx = new ContentContext(ctx);
					absoluteURLCtx.setAbsoluteURL(true);					
					try {
						if (logoUrl == null) {
							//logoUrl = URLHelper.createTransformURL(absoluteURLCtx, null, URLHelper.mergePath(staticConfig.getStaticFolder(), newLogo), "logo", getName());
							logoUrl = "${info.logoURL}";
						}
					} catch (Exception e) {
						throw new IOException(e);
					}
					String srcLogo = getTemplateData().getLogo();
					if (srcLogo != null) {
						map.put(srcLogo, logoUrl);
					}
				}
			} else {
				logger.warning("no template data for : " + this);
			}
			
			Iterator<File> files = allFiles.iterator();
			
			Set<File> createdFolder = new HashSet<File>();
			
			while (files.hasNext()) {
				File file = files.next();				
				String path = ResourceHelper.removePath(file.getAbsolutePath(), templateSrc.getAbsolutePath());	
				
				if (!file.getAbsolutePath().contains("/.") && !file.getAbsolutePath().contains("\\.")) { // no copy hidden file and folder					
					if (!parent || isImportParentComponents() || !path.startsWith('/'+DYNAMIC_COMPONENTS_PROPERTIES_FOLDER)) {						
						File targetFile = new File(file.getAbsolutePath().replace(templateSrc.getAbsolutePath(), templateTarget.getAbsolutePath()));
						
						File folder = targetFile.getParentFile();
						if (!createdFolder.contains(folder)) {
							if (!folder.exists()) {
								folder.mkdirs();
							}
							createdFolder.add(folder);							
						}
						
						if (ctx != null) {
							try {
								String fileExt = FilenameUtils.getExtension(file.getName());
								if (FILTER_FILE_EXTENSION.contains(fileExt)) {
									if (fileExt.equalsIgnoreCase("css") || fileExt.equalsIgnoreCase("scss") || fileExt.equalsIgnoreCase("less") || fileExt.equalsIgnoreCase("sass")) {
										appendRawCssFile(globalContext, ResourceHelper.loadStringFromFile(file), inRawCssFile);
									}
									if (fileExt.equalsIgnoreCase("jsp") || fileExt.equalsIgnoreCase("html")) {
										ResourceHelper.filteredFileCopyEscapeScriplet(file, targetFile, map, ctx.getGlobalContext().getStaticConfig().isCompressJsp());
									} else {
										ResourceHelper.filteredFileCopy(file, targetFile, map);
									}
								} else {
									if (file.isFile()) {
										ResourceHelper.copyFile(file, targetFile, false);
									}
								}
							} catch (Exception e) {
								logger.warning("error on copy file : " + file + " err:" + e.getMessage());
							}
						}					
					}
				}
			}

			/** import context **/
			File componentFolderTarget = new File(URLHelper.mergePath(templateTarget.getAbsolutePath(), DYNAMIC_COMPONENTS_PROPERTIES_FOLDER));
			File srcComp = new File(globalContext.getExternComponentFolder());
			if (!srcComp.exists()) {
				srcComp.mkdirs();
			}
			//FileUtils.copyDirectory(srcComp, componentFolderTarget);

			/** prepare merging of all sass component class **/
			if (componentFolderTarget.exists() && componentFolderTarget.isDirectory()) {
				Iterator<File> cssFiles = FileUtils.iterateFiles(componentFolderTarget, new String[] { "css", "scss" }, true);
				Collection<File> fixedFiles = new LinkedList<>();
				while (cssFiles.hasNext()) {
					File file = cssFiles.next();
					fixedFiles.add(file);
				}
				File componentMergeFile = new File(URLHelper.mergePath(templateTarget.getAbsolutePath(), "_components.scss"));
				List<File> componentsList = new LinkedList<File>();
				BufferedWriter outSassImport = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(componentMergeFile)));
				try {
					for (File cssFile : fixedFiles) {
						if (StringHelper.getFileExtension(cssFile.getName()).equalsIgnoreCase("css")) {
							File sassFile = new File(StringHelper.getFileNameWithoutExtension(cssFile.getAbsolutePath()) + ".scss");
							if (sassFile.exists()) {
								sassFile.delete();
							}
							cssFile.renameTo(sassFile);
							cssFile = sassFile;
						}
						if (!componentsList.contains(cssFile)) {
							String line = "@import '" + DYNAMIC_COMPONENTS_PROPERTIES_FOLDER + "/" + cssFile.getName() + "';";
							outSassImport.write(line);
							outSassImport.newLine();
							componentsList.add(cssFile);
						}
					}
				} finally {
					ResourceHelper.closeResource(outSassImport);
				}
			}

		} else {
			logger.warning("template not found : " + templateSrc);
		}
		// if (globalContext != null) {
		// if (compressResource && isCompressResources() &&
		// globalContext.getStaticConfig().isProd()) {
		// Iterator<File> targetFiles = FileUtils.iterateFiles(templateTarget, new
		// String[] { "js", "css", "less", "scss" }, true);
		// while (targetFiles.hasNext()) {
		// File targetFile = targetFiles.next();
		// String targetFileNameLowerCase = targetFile.getName().toLowerCase();
		// boolean isCss = targetFileNameLowerCase.endsWith(".css");
		// boolean isJs = targetFileNameLowerCase.endsWith(".js");
		// try {
		// if (isCss) {
		//// XHTMLHelper.expandCSSImports(targetFile);
		// XHTMLHelper.compressCSS(targetFile);
		// }
		// if (isJs) {
		// XHTMLHelper.compressJS(targetFile);
		// }
		// } catch (Exception e) {
		// logger.warning("error on copy file : " + targetFile + " err:" +
		// e.getMessage());
		// }
		// }
		// }
		// }

		/** clean file **/
		Iterator<File> targetFiles = FileUtils.iterateFiles(templateTarget, new String[] { "scss" }, true);
		while (targetFiles.hasNext()) {
			File targetFile = targetFiles.next();
			try {
				XHTMLHelper.removeComment(targetFile);
			} catch (Exception e) {
				logger.warning("error on copy file : " + targetFile + " err:" + e.getMessage());
			}
		}

		deployId = StringHelper.getRandomId();
		if (config != null) {
			TemplateFactory.clearTemplate(config.getServletContext());
		}
		if (isParent()) {
			if (childrenData == null) {
				childrenData = new HashMap<String, String>();
			}
			LangHelper.putAllIfNotExist(childrenData, getTemplateDataMap(globalContext));
			if (getParent().exist()) {
				getParent().importTemplateInWebapp(config, ctx, globalContext, templateTarget, childrenData, false, true, inRawCssFile, logoUrl);
			} else {
				logger.warning("parent template not found : " + getParent().getName());
				File indexFile = new File(URLHelper.mergePath(templateTarget.getAbsolutePath(), "index.jsp"));
				File source = new File(ctx.getRequest().getSession().getServletContext().getRealPath("/jsp/index_error.html"));
				String content = ResourceHelper.loadStringFromFile(source);
				content = content.replace("#template#", getName());
				content = content.replace("#parent-template#", getParent().getName());
				ResourceHelper.writeStringToFile(indexFile, content);
			}
		}
	}

	public boolean isAlternativeTemplate(ContentContext ctx) {
		HttpSession session = ctx.getRequest().getSession(false);
		if (session == null) {
			return false;
		}
		boolean out = session.getAttribute(getAlternateTemplateSessionKey()) != null;
		return out;
	}

	public boolean isHTML() {
		String htmlFile = getHTMLFile(null);

		String templateFolder = config.getTemplateFolder();
		File indexFile = new File(URLHelper.mergePath(URLHelper.mergePath(templateFolder, getSourceFolderName()), htmlFile));
		return indexFile.exists();
	}

	public boolean isLinkEmail(String lg) {
		return getLinkEmail(lg).exists();
	}

	public boolean isMailing() {
		return properties.getBoolean("mailing", getParent().isMailing());
	}
	
	public boolean isShortkeyToEdit() {
		return properties.getBoolean("key.edit", true);
	}

	public boolean isOnePage() {
		return properties.getBoolean("onepage", getParent().isOnePage());
	}

	public boolean isForStatic() {
		return properties.getBoolean("static", false);
	}

	public boolean isNavigationArea(String area) {
		if (area == null) {
			return false;
		}
		String key = XMLManipulationHelper.AREA_PREFIX + area + ".navigation";
		return properties.getBoolean(key, false);
	}

	public boolean isNosecureArea(String area) {
		if (area == null) {
			return false;
		}
		String key = XMLManipulationHelper.AREA_PREFIX + area + ".no-secure";
		return properties.getBoolean(key, false);
	}

	protected boolean isParent() {
		return getParent() != null && !getParent().getName().equals(DefaultTemplate.NAME);
	}

	public boolean isChildOf(String templateId) {
		Set<String> parents = new HashSet<String>();
		Template parent = getParent();
		while (parent != null && !parents.contains(parent.getName())) {
			if (parent.getName().equals(templateId)) {
				return true;
			} else {
				parents.add(parent.getName());
				parent = getParent();
			}
		}
		return false;
	}

	public boolean isPDFFile() {
		String pdfFilStr = getVisualPDFile();

		String templateFolder = config.getTemplateFolder();
		File pdfFile = new File(URLHelper.mergePath(URLHelper.mergePath(templateFolder, getSourceFolderName()), pdfFilStr));
		return pdfFile.exists();
	}

	public boolean isReady() {
		return privateProperties.getBoolean("ready", false);
	}

	public boolean isRenderer(GlobalContext globalContext) {
		String renderer = getRendererFile(null);
		File jspFile = new File(getTemplateTargetFolder(globalContext), renderer);
		return jspFile.exists();
	}

	public boolean isSubjectLocked() {
		return StringHelper.isTrue(properties.getString("mail.subject.locked", "false"));
	}

	public List<InternetAddress> getSenders() {
		String senders = properties.getString("mail.senders");
		if (senders == null || senders.trim().length() == 0) {
			return getParent().getSenders();
		} else {
			List<InternetAddress> outList = new LinkedList<InternetAddress>();
			for (String sender : StringUtils.split(senders, ',')) {
				sender = sender.trim();
				try {
					InternetAddress iAdd = new InternetAddress(sender);
					outList.add(iAdd);
				} catch (AddressException e) {
					e.printStackTrace();
				}
			}
			return outList;
		}
	}

	public boolean isTemplateInWebapp(ContentContext ctx) throws IOException {
		synchronized (getLockImport(ctx.getGlobalContext())) {
			GlobalContext globalContext = null;
			globalContext = GlobalContext.getInstance(ctx.getRequest());
			if (contextWithTemplateImported.contains(globalContext.getContextKey())) {
				return true;
			}
			File templateTgt = new File(URLHelper.mergePath(getWorkTemplateFolder(), getFolder(globalContext)));
			if (templateTgt.exists()) {
				/*
				 * File indexFile = new File(URLHelper.mergePath(templateTgt.getAbsolutePath(),
				 * getHTMLFile(null))); if (!indexFile.exists() && !indexFileWaited) {
				 * logger.info("index not found wait... ("+indexFile+") site:" +
				 * ctx.getGlobalContext().getContextKey() + " - tpl:" + getName()); // TODO: //
				 * remove // debug // trace try { Thread.sleep(1000 * 10); indexFileWaited =
				 * true; } catch (InterruptedException e) { e.printStackTrace(); } // wait 10
				 * sec } if (!indexFile.exists()) { logger.warning("template not found. site:" +
				 * ctx.getGlobalContext().getContextKey() + " - tpl:" +
				 * getName()+" - folder:"+templateTgt+ " - index:"+indexFile); return false; }
				 */
				contextWithTemplateImported.add(globalContext.getContextKey());
			}
			boolean outExist = templateTgt.exists();
			return outExist;
		}
	}

	public boolean isValid() {
		return privateProperties.getBoolean("valid", false);
	}

	public void reload() {
		getParent().reload();
		synchronized (properties) {
			properties.clear();
			privateProperties.clear();
			try {
				properties.load();
				privateProperties.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		dynamicsComponents = null;
		contextWithTemplateImported.clear();
		freeData = null;
		resetRows();
	}

	public void setAuthors(String name) {
		synchronized (properties) {
			properties.setProperty("authors", name);
			storeProperties();
		}
	}

	public void setProperty(String name, String value) {
		synchronized (properties) {
			if (value == null) {
				properties.clearProperty(name);
			} else {
				properties.setProperty(name, value);
			}
			storeProperties();
		}
	}

	public void setCreationDate(Date date) {
		if (date != null) {
			synchronized (properties) {
				privateProperties.setProperty("creation-date", StringHelper.renderDate(date));
				storePrivateProperties();
			}
		}
	}

	public void setDepth(int depth) {
		privateProperties.setProperty("depth", depth);
		storePrivateProperties();
	}

	public void setDominantColor(String color) {
		synchronized (properties) {
			properties.setProperty("color.dominant", color);
			storeProperties();
		}
	}

	public void setOwner(String owner) {
		privateProperties.setProperty("owner", owner);
		storePrivateProperties();
	}

	public void setReady(boolean ready) {
		synchronized (properties) {
			privateProperties.setProperty("ready", ready);
			storePrivateProperties();
		}
	}

	public void setSource(String name) {
		synchronized (properties) {
			properties.setProperty("source", name);
			storeProperties();
		}
	}

	public void setValid(boolean inValid) {
		synchronized (properties) {
			privateProperties.setProperty("valid", inValid);
			storePrivateProperties();
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	public boolean visibleForRoles(Collection<String> inRoles) {
		String rolesRaw = properties.getString("roles");
		if (rolesRaw == null) { // if no role defined -> visible for everybody
			return true;
		}
		Collection<String> roles = StringHelper.stringToCollection(rolesRaw, ",");
		for (String inRole : inRoles) {
			if (roles.contains(inRole.trim())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * return a code of build. This code change if some element will modifified.
	 * 
	 * @return
	 */
	public String getBuildId() {
		return buildId;
	}

	@Override
	public int compareTo(Template template) {
		return getName().compareTo(template.getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Template) {
			return getName().equals(((Template) obj).getName());
		} else {
			return super.equals(obj);
		}
	}

	public void setHTMLIDS(Collection<String> ids) {
		privateProperties.setProperty("html.ids", StringHelper.collectionToString(ids, ","));
		storeProperties();
	}

	public List<String> getHTMLIDS() {
		String htmlIds = privateProperties.getString("html.ids");
		if (htmlIds == null) {
			return Collections.EMPTY_LIST;
		}
		List<String> ids = StringHelper.stringToCollection(htmlIds, ",");
		return ids;
	}

	protected String getRAWPlugins() {
		return properties.getString("plugins", getParent().getRAWPlugins());
	}

	public List<String> getPlugins() {
		String plugins = getRAWPlugins();
		if (plugins == null) {
			return Collections.EMPTY_LIST;
		}
		List<String> ids = StringHelper.stringToCollection(plugins, ",");
		return ids;
	}

	public String getDeployId() {
		return deployId;
	}

	/**
	 * return true if this template contains a renderer for PDF.
	 * 
	 * @return true if PDF renderer defined.
	 */
	public boolean isPDFRenderer() {
		return getRenderers().contains("pdf");
	}

	/**
	 * return true if resources can be compressed by CMS (css, js).
	 * 
	 * @return true by default or the value of "resources.compress" property.
	 */
	public boolean isCompressResources() {
		return properties.getBoolean("resources.compress", false);
	}

	public String getRSSImageURL() {
		return properties.getString("rss.image-url", null);
	}

	public String getConfigItem(String key, String defaultValue) {
		return StringHelper.neverNull(properties.getString(key), defaultValue);
	}

	/**
	 * true if all area can be signifiant for read content, false if only main area
	 * is signifiant.
	 * 
	 * @return
	 */
	public boolean isRealContentFromAnyArea() {
		if (properties.getProperty("real-content-from-any") == null) {
			return getParent().isRealContentFromAnyArea();
		} else {
			return properties.getBoolean("real-content-from-any", false);
		}
	}

	public int getPDFHeigth() {
		return properties.getInt("pdf.height", getParent().getPDFHeigth());
	}

	public int getQRCodeSize() {
		return properties.getInt("qrcode.width", getParent().getQRCodeSize());
	}

	public Map<String, String> getFreeData() {
		if (freeData == null) {
			String freeDataPrefix = "data.free.";
			Iterator keys = properties.getKeys();
			Map<String, String> newFreeData = new HashMap<String, String>();
			while (keys.hasNext()) {
				String key = "" + keys.next();
				if (key.startsWith(freeDataPrefix)) {
					String value = "" + properties.getProperty(key);
					key = key.substring(freeDataPrefix.length());
					if (!newFreeData.containsKey(key)) {
						newFreeData.put(key, value);
					}
				}
			}
			freeData = newFreeData;
		}
		return freeData;
	}

	protected void loadTemplatePart(TemplatePart part, String prefix) {
		part.setBackgroundColor(properties.getString(prefix + ".background-color"));
		part.setOuterBackgroundColor(properties.getString(prefix + ".outer-background-color"));
		part.setBorderColor(properties.getString(prefix + ".border-color"));
		part.setBorderWidth(properties.getString(prefix + ".border-width"));
		part.setFont(properties.getString(prefix + ".font", part.getDefaultFont()));
		part.setHeight(properties.getString(prefix + ".height"));
		part.setMargin(properties.getString(prefix + ".margin"));
		part.setPadding(properties.getString(prefix + ".padding", part.getDefaultPadding()));
		part.setTextColor(properties.getString(prefix + ".color", part.getDefaultTextColor()));
		part.setLinkColor(properties.getString(prefix + ".link-color"));
		part.setTextSize(properties.getString(prefix + ".text-size", part.getDefaultTextSize()));
		part.setWidth(properties.getString(prefix + ".width", part.getDefaultWidth()));
		part.setTitleColor(properties.getString(prefix + ".title-color"));
		part.setResponsive(properties.getString(prefix + ".responsive", "false"));

		part.setH1Size(properties.getString(prefix + ".h1.size", part.getDefaultH1Size()));
		part.setH2Size(properties.getString(prefix + ".h2.size", part.getDefaultH2Size()));
		part.setH3Size(properties.getString(prefix + ".h3.size", part.getDefaultH3Size()));
		part.setH4Size(properties.getString(prefix + ".h4.size", part.getDefaultH4Size()));
		part.setH5Size(properties.getString(prefix + ".h5.size", part.getDefaultH5Size()));
		part.setH6Size(properties.getString(prefix + ".h6.size", part.getDefaultH6Size()));

		part.setPriority(properties.getInt(prefix + ".priority", 10));
	}

	protected void saveTemplatePart(TemplatePart part, String prefix) {
		properties.setProperty(prefix + ".outer-background-color", part.getOuterBackgroundColor());
		properties.setProperty(prefix + ".background-color", part.getBackgroundColor());
		properties.setProperty(prefix + ".border-color", part.getBorderColor());
		properties.setProperty(prefix + ".border-width", part.getBorderWidth());
		properties.setProperty(prefix + ".font", part.getFont());
		properties.setProperty(prefix + ".height", part.getHeight());
		properties.setProperty(prefix + ".margin", part.getMargin());
		properties.setProperty(prefix + ".padding", part.getPadding());
		properties.setProperty(prefix + ".color", part.getTextColor());
		properties.setProperty(prefix + ".link-color", part.getLinkColor());
		properties.setProperty(prefix + ".text-size", part.getTextSize());
		properties.setProperty(prefix + ".width", part.getWidth());
		properties.setProperty(prefix + ".title-color", part.getTitleColor());
		properties.setProperty(prefix + ".responsive", part.getResponsive());

		properties.setProperty(prefix + ".h1.size", part.getH1Size());
		properties.setProperty(prefix + ".h2.size", part.getH2Size());
		properties.setProperty(prefix + ".h3.size", part.getH3Size());
		properties.setProperty(prefix + ".h4.size", part.getH4Size());
		properties.setProperty(prefix + ".h5.size", part.getH5Size());
		properties.setProperty(prefix + ".h6.size", part.getH6Size());

		properties.setProperty(prefix + ".priority", part.getPriority());
	}

	public synchronized TemplateStyle getStyle() {
		if (style == null) {
			style = new TemplateStyle();
			style.setName(getName());
			style.setConfig(configMap);
			loadTemplatePart(style, "style");
		}
		return style;
	}

	public void storeStyle(TemplateStyle style) {
		saveTemplatePart(style, "style");
		style = null;
		storeProperties();
	}

	public synchronized List<Row> getRows() {
		if (rows == null) {
			rows = new HashMap<String, Row>();
			for (String area : getAreas()) {
				String rowName = properties.getString("area." + area + ".row", "");
				if (rowName.trim().length() > 0) {
					Row row = rows.get(rowName);
					if (row == null) {
						row = new Row(this);
						row.setName(rowName);
						loadTemplatePart(row, "row." + rowName);
						rows.put(rowName, row);
					}
					Area newArea = new Area();
					newArea.setRow(row);
					newArea.setName(area);
					loadTemplatePart(newArea, "area." + area);
					row.addArea(newArea);
				}
			}
		}
		List<Row> outRows = new LinkedList(rows.values());
		Collections.sort(outRows, new TemplatePart.SortByName());
		if (outRows.size() > 0) {
			for (int i = 0; i < outRows.size(); i++) {
				outRows.get(i).setFirst(false);
				outRows.get(i).setLast(false);
			}
			outRows.get(0).setFirst(true);
			outRows.get(outRows.size() - 1).setLast(true);
		}
		return outRows;
	}

	public synchronized void resetRows() {
		style = null;
		rows = null;
		areas = null;
	}

	public static Area getArea(Collection<Row> rows, String name) {
		for (Row row : rows) {
			for (Area area : row.getAreas()) {
				if (area.getName().equals(name)) {
					return area;
				}
			}
		}
		return null;
	}

	public void deleteRow(String name) {
		Collection<Row> newRows = new LinkedList<Row>();
		for (Row row : getRows()) {
			if (!row.getName().equals(name)) {
				newRows.add(row);
			}
		}
		storeRows(newRows);
		resetRows();
	}

	public synchronized void storeRows(Collection<Row> rows) {
		Iterator keys = properties.getKeys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (key.startsWith("row.") || key.startsWith("area.")) {
				keys.remove();
				// properties.clearProperty(key);
			}
		}
		for (Row row : rows) {
			saveTemplatePart(row, "row." + row.getName());
			for (Area area : row.getAreas()) {
				properties.setProperty("area." + area.getName(), area.getName());
				properties.setProperty("area." + area.getName() + ".row", row.getName());
				saveTemplatePart(area, "area." + area.getName());
			}
		}
		storeProperties();
		resetRows();
	}

	public boolean isEditable() {
		if (getParent() != null && getParent().isEditable()) {
			return true;
		} else {
			return getName().contains("editable");
		}
	}
	
	public boolean isImportParentComponents() {
		if (getConfig().get("import.parent-components") != null) {
			return StringHelper.isTrue(getConfig().get("import.parent-components"));
		} else {
			return getParent().isImportParentComponents();
		}
	}

	public String getMailingTemplate() {
		String mailingTemplate = properties.getProperty("mailing.template");
		if (mailingTemplate == null) {
			return getParent().getMailingTemplate();
		} else {
			if (mailingTemplate.trim().equals("-1")) {
				return null;
			}
			return mailingTemplate;
		}
	}

	public boolean isBootstrap() {
		if (properties.getProperty("bootstrap") == null) {
			return getParent().isBootstrap();
		} else {
			return StringHelper.isTrue(properties.getProperty("bootstrap"));
		}
	}

	/**
	 * if area empty remove from html (default true)
	 * 
	 * @return
	 */
	public boolean isRemoveEmptyArea() {
		if (properties.getProperty("area-remove-empty") == null) {
			return getParent().isRemoveEmptyArea();
		} else {
			return StringHelper.isTrue(properties.getProperty("area-remove-empty"), true);
		}
	}

	public Map<String, String> getTemplateExcludeProperties() {
		String rawProp = getExcludeProperties("template");
		if (rawProp == null) {
			return Collections.EMPTY_MAP;
		} else {
			Map<String, String> outMaps = new HashMap<String, String>();
			for (String prop : StringHelper.stringToCollection(rawProp, ",")) {
				outMaps.put(prop, prop);
			}
			return outMaps;
		}
	}

	public Map<String, String> getAreaExcludeProperties() {
		String rawProp = getExcludeProperties("area");
		if (rawProp == null) {
			return Collections.EMPTY_MAP;
		} else {
			Map<String, String> outMaps = new HashMap<String, String>();
			for (String prop : StringHelper.stringToCollection(rawProp, ",")) {
				outMaps.put(prop, prop);
			}
			return outMaps;
		}
	}

	protected String getExcludeProperties(String zone) {
		return properties.getString("exclude-properties." + zone, getParent().getExcludeProperties(zone));
	}

	public Map<String, String> getRowExcludeProperties() {
		String rawProp = getExcludeProperties("row");
		if (rawProp == null) {
			return Collections.EMPTY_MAP;
		} else {
			Map<String, String> outMaps = new HashMap<String, String>();
			for (String prop : StringHelper.stringToCollection(rawProp, ",")) {
				outMaps.put(prop, prop);
			}
			return outMaps;
		}
	}

	public String getCookiesMessageName() {
		return properties.getString("acceptcookies.name", "acceptcookies");
	}

	public String getCookiesMessagePath() {
		return properties.getString("acceptcookies.path", null);
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = hash + 31 * getName().hashCode();
		hash = hash + (isValid() ? 1 : 0);
		if (isEditable()) {
			for (Row row : getRows()) {
				hash = hash + row.hashCode();
			}
			hash = hash + 31 * getStyle().hashCode();
		} else {
			for (String area : getAreas()) {
				hash = hash + area.hashCode();
			}
		}
		hash = hash + 31 * getTemplateData().hashCode();
		return hash;
	}

	private String getMimeTypesFolder() {
		return properties.getString("mimetypes.folder", "mimetypes");
	}

	public String getMimeTypeImage(GlobalContext globalContext, String fileExtension) {
		Pattern VALUE_SPLITTER = Pattern.compile("\\s*,\\s*");
		File mappingFile = new File(URLHelper.mergePath(getWorkTemplateRealPath(globalContext), getMimeTypesFolder(), "mapping.properties"));
		if (!mappingFile.exists()) {
			return null;
		}
		FileInputStream in = null;
		String out = null;
		String defaultImage = null;
		try {
			in = new FileInputStream(mappingFile);
			Properties p = new Properties();
			p.load(in);
			for (Entry<Object, Object> prop : p.entrySet()) {
				String value = (String) prop.getValue();
				if (value != null) {
					String[] extensions = VALUE_SPLITTER.split(value);
					for (String extention : extensions) {
						if (extention.equalsIgnoreCase(fileExtension)) {
							out = (String) prop.getKey();
						} else if (extention.equals("*")) {
							defaultImage = (String) prop.getKey();
						}
					}
				}
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "Exception when parsing mapping.properties of template " + getName(), e);
		} finally {
			ResourceHelper.safeClose(in);
		}
		if (out == null) {
			out = defaultImage;
		}
		if (out != null) {
			out = URLHelper.mergePath(getMimeTypesFolder(), out);
		}
		return out;
	}

	public boolean isLanguageLinkKeepGetParams() {
		return StringHelper.isTrue(properties.getString("language.keepParams"));
	}

	public boolean isEndAreaTag() {
		return StringHelper.isTrue(properties.getString("area.end-tag"), true);
	}

	public Map<String, String> getConfig() {
		return configMap;
	}

	public boolean isColorPalette() {
		return getColorList() != null && getColorList().size() > 0;
	}

	public List<ExtendedColor> getColorList() {
		String rawList = properties.getProperty("charter.colors");
		if (rawList == null) {
			return getParent().getColorList();
		} else {
			List<ExtendedColor> colors = new LinkedList<ExtendedColor>();
			for (String col : StringHelper.stringToCollection(rawList, ",")) {
				try {
					colors.add(ExtendedColor.decode(col));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return colors;
		}
	}

	public Map<Integer, ExtendedColor> getColorsMap() {
		return new ListAsMap<ExtendedColor>(getColorList());
	}

	public List<String> getFonts() {
		String rawFonts = properties.getProperty("fonts");
		if (rawFonts == null) {
			return getParent().getFonts();
		} else {
			return StringHelper.stringToCollection(rawFonts, ";");
		}
	}

	public List<String> getWebFonts(GlobalContext globalContext) {
		List<String> outFonts = new LinkedList<String>();
		outFonts.addAll(XHTMLHelper.WEB_FONTS);
		Properties fprop;
		try {
			fprop = getFontReference(globalContext);
			if (fprop != null) {
				if (fprop != null) {
					outFonts.addAll(getFontReference(globalContext).stringPropertyNames());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outFonts;
	}

	public boolean isFake() {
		return fakeName != null;
	}

	public static void main(String[] args) throws IOException {
		File file = new File("c:/trans/fonts_reference_src.properties");

		Properties prop = ResourceHelper.loadProperties(file);
		for (Object key : prop.keySet()) {
			prop.setProperty((String) key, "<link href=\"https://fonts.googleapis.com/css?family=" + (key.toString().replace(" ", "+") + "\" rel=\"stylesheet\">"));
		}
		file = new File("c:/trans/fonts_reference.properties");
		prop.store(new FileOutputStream(file), "");
	}

}
