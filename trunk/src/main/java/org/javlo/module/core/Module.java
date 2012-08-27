package org.javlo.module.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.javlo.actions.IModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.AjaxHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.Comparator.FileComparator;
import org.javlo.user.User;
import org.javlo.utils.ReadOnlyPropertiesMap;

/**
 * A module is a application inside javlo.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class Module {

	private static final String CSS_FOLDER = "css";

	private static final String JS_FOLDER = "js";

	public static final class EmptyAction implements IModuleAction {

		@Override
		public String getActionGroupName() {
			return "empty";
		}

		@Override
		public String prepare(ContentContext ctx, ModulesContext moduleContext) {
			return null;
		}

		@Override
		public String performSearch(ContentContext ctx, ModulesContext moduleContext, String searchText) throws Exception {
			return null;
		}

		@Override
		public Boolean haveRight(HttpSession session, User user) {
			return null;
		}

	}

	private static final EmptyAction emptyAction = new EmptyAction();

	public static class HtmlLink {
		private String url;
		private String legend;
		private String title;

		public HtmlLink(String url, String legend, String title) {
			this.url = url;
			this.legend = legend;
			this.title = title;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getLegend() {
			return legend;
		}

		public void setLegend(String legend) {
			this.legend = legend;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		@Override
		public boolean equals(Object obj) {
			HtmlLink otherLink = (HtmlLink) obj;
			return otherLink.getUrl().equals(getUrl()) && otherLink.getLegend().equals(getLegend());
		}
	}

	public class Link {
		public Link(String params, String label, String style) {
			this.params = params;
			this.label = label;
			this.style = style;
		}

		private String params;
		private String label;
		private String style;
		private boolean active;

		public String getParams() {
			return params;
		}

		public String getLabel() {
			return label;
		}

		public String getStyle() {
			return style;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}
	}

	public class Box {

		private Box(String title, String renderer, boolean action) {
			this(title, renderer, action, null);
		}

		private Box(String title, String renderer, boolean action, List<BoxStep> stepList) {
			this.steps = stepList;
			this.title = StringHelper.neverNull(title);
			this.defaultTitle = this.title;
			this.renderer = URLHelper.mergePath(path, renderer);
			this.action = action;
			id = "box-" + StringHelper.getRandomId();
		}

		protected String title;
		protected String defaultTitle;
		protected String renderer;
		protected String id;
		protected boolean action;
		protected List<BoxStep> steps;

		public String getTitle() {
			return title;
		}

		public void restoreTitle() {
			title = defaultTitle;
		}

		public String getRenderer() {
			return renderer;
		}

		public boolean isAction() {
			return action;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void setRenderer(String renderer) {
			if (renderer != null) {
				this.renderer = URLHelper.mergePath(path, renderer);
			} else {
				this.renderer = null;
			}
		}

		public void setAction(boolean action) {
			this.action = action;
		}

		public String getId() {
			return id;
		}

		/**
		 * this method is called when the box must be updated. This method is used only in ajax context.
		 * 
		 * @param ctx
		 * @throws IOException
		 * @throws ServletException
		 */
		public void update(ContentContext ctx) throws ServletException, IOException {
			AjaxHelper.updateBox(ctx, this);
		}

		public Module getModule() {
			return Module.this;
		}
		
		public List<BoxStep> getSteps() {
			return steps;
		}
		
	}

	public class BoxStep {

		private BoxStep(String title, String renderer) {
			this.title = StringHelper.neverNull(title);
			this.renderer = renderer;
		}

		protected String title;
		protected String renderer;

		public String getTitle() {
			return title;
		}

		public String getRenderer() {
			return renderer;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void setRenderer(String renderer) {
			if (renderer != null) {
				this.renderer = URLHelper.mergePath(path, renderer);
			} else {
				this.renderer = null;
			}
		}

	}

	private class NavigationBox extends Box {
		private NavigationBox(String title, boolean action) {
			super(title, "/", action);
			this.renderer = "/jsp/edit/modules/navigation.jsp";
		}
	}

	private String name;
	private String title;
	private String path;
	private File moduleRoot;
	private boolean sidebar = false;
	private boolean breadcrumb = false;
	private String breadcrumbTitle = null;
	Collection<Box> mainBoxes = new LinkedList<Box>();
	Collection<Box> defaultMainBoxes = null;
	Collection<Box> sideBoxes = new LinkedList<Box>();
	Collection<Box> defaultSideBoxes = null;
	String navigationTitle = null;
	Collection<Box> navigation = new LinkedList<Box>();
	private String helpTitle = null;
	private String helpText = null;
	private String toolsTitle = null;
	private String toolsRenderer = null;
	private String defaultToolsRenderer = null;
	private IModuleAction action = emptyAction;
	private Collection<String> cssURI = new LinkedList<String>();
	private Collection<String> jsURI = new LinkedList<String>();
	private String renderer;
	private String defaultRenderer;
	private Map<String, Box> boxes = new HashMap<String, Box>();
	private Map<String, Box> defaultBoxes;
	private String backUrl = null;
	private Stack<HtmlLink> breadcrumbLinks;
	private boolean search;
	private String parent;
	private List<Module> children = new LinkedList<Module>();
	private int order;
	private Set<String> roles;
	private Set<String> excludeRoles;
	private Map<String, String> config;

	private File configFile;
	private Locale locale;
	private String modulePath;
	private String URIPrefix;

	private String description = "?";

	public Module(File configFile, Locale locale, String modulePath, String URIPrefix) throws IOException {

		this.configFile = configFile;
		this.locale = locale;
		this.modulePath = modulePath;
		this.URIPrefix = URIPrefix;

		loadModule();

	}

	private void loadModule() throws IOException {

		cssURI.clear();
		jsURI.clear();
		boxes.clear();
		mainBoxes.clear();
		sideBoxes.clear();
		navigation.clear();

		FileReader fileReader = null;
		try {
			fileReader = new FileReader(configFile);
			Properties properties = new Properties();
			properties.load(fileReader);
			config = new ReadOnlyPropertiesMap(properties);
		} finally {
			if (fileReader != null) {
				fileReader.close();
			}
		}
		path = modulePath.replace('\\', '/');
		name = config.get("name");
		breadcrumb = StringHelper.isTrue(config.get("breadcrumb"));
		search = StringHelper.isTrue(config.get("search"));
		setParent(config.get("parent"));

		order = Integer.parseInt(StringHelper.neverNull(config.get("order"), "100"));

		/** security **/
		String rolesRaw = config.get("security.roles");
		if (rolesRaw != null) {
			roles = new HashSet<String>();
			roles.addAll(StringHelper.stringToCollection(rolesRaw, ";"));
		}
		rolesRaw = config.get("security.exclude-roles");
		if (rolesRaw != null) {
			excludeRoles = new HashSet<String>();
			excludeRoles.addAll(StringHelper.stringToCollection(rolesRaw, ";"));
		}


		title = config.get("title." + locale.getLanguage());
		if (title == null) {
			title = config.get("title");
		}

		description = config.get("description." + locale.getLanguage());
		if (description == null) {
			description = config.get("description");
		}

		moduleRoot = configFile.getParentFile();
		
		/* css */
		File cssFolder = new File(URLHelper.mergePath(moduleRoot.getAbsolutePath(), CSS_FOLDER));
		if (cssFolder.isDirectory()) {
			File[] cssFiles = cssFolder.listFiles();
			Arrays.sort(cssFiles, new FileComparator(FileComparator.NAME, true));
			for (File file : cssFiles) {
				if (file.isFile() && StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("css")) {					
					cssURI.add(URLHelper.mergePath("/", URIPrefix, ModulesContext.MODULES_FOLDER + '/' + getName() + '/' + CSS_FOLDER + '/' + file.getName()));
				}
			}
		}

		/* js */
		File jsFolder = new File(URLHelper.mergePath(moduleRoot.getAbsolutePath(), JS_FOLDER));
		if (jsFolder.isDirectory()) {
			File[] jspFiles = jsFolder.listFiles();
			Arrays.sort(jspFiles, new FileComparator(FileComparator.NAME, true));
			for (File file : jspFiles) {
				if (file.isFile() && StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("js")) {
					jsURI.add(URLHelper.mergePath("/",URIPrefix,ModulesContext.MODULES_FOLDER + '/' + getName() + '/' + JS_FOLDER + '/' + file.getName()));
				}
			}
			for (int i = 0; i < 100; i++) {
				if (config.get("js.import." + i) != null) {
					jsURI.add(URLHelper.mergePath("/",URIPrefix,ModulesContext.MODULES_FOLDER + '/' + getName() + config.get("js.import." + i)));
				}
			}
		}

		/* main renderer */
		renderer = config.get("renderer");
		if (renderer != null) {
			renderer = URLHelper.mergePath(path, renderer);
			defaultRenderer = renderer;
		}

		/* tools */
		toolsTitle = config.get("tools.title." + locale.getLanguage());
		if (toolsTitle == null) {
			toolsTitle = config.get("tools.title");
		}
		toolsRenderer = config.get("tools.renderer");
		if (toolsRenderer != null) {
			toolsRenderer = URLHelper.mergePath(path, toolsRenderer);
			defaultToolsRenderer = toolsRenderer;
		}

		/* help text */
		helpTitle = config.get("help.title." + locale.getLanguage());
		if (helpTitle == null) {
			helpTitle = config.get("help.title");
		}
		helpText = config.get("help.text." + locale.getLanguage());
		if (helpText == null) {
			helpText = config.get("help.text");
		}

		/* navigation */
		navigationTitle = config.get("navigation.title" + locale.getLanguage());
		if (navigationTitle == null) {
			navigationTitle = config.get("navigation.title");
		}
		for (int i = 1; i < 100; i++) {
			String navigationBaseKey = "navigation." + i;

			if (config.get(navigationBaseKey + ".name") != null) {
				String renderer = config.get(navigationBaseKey + ".renderer");

				String boxTitle = config.get(navigationBaseKey + ".title." + locale.getLanguage());
				if (boxTitle == null) {
					boxTitle = config.get(navigationBaseKey + ".title");
				}
				Box box;
				if (renderer == null) {
					box = new NavigationBox(boxTitle, StringHelper.isTrue(config.get(navigationBaseKey + ".action")));
				} else {
					box = new Box(boxTitle, renderer, StringHelper.isTrue(config.get(navigationBaseKey + ".action")));
				}
				navigation.add(box);
				if (config.get(navigationBaseKey + ".name") != null) {
					boxes.put(config.get(navigationBaseKey + ".name"), box);
				}
			} else {
				break;
			}
		}

		/* box */
		loadBoxes("box.main.", mainBoxes);
		defaultMainBoxes = new LinkedList<Module.Box>(mainBoxes);
		loadBoxes("box.side.", sideBoxes);
		sidebar = sideBoxes.size() > 0;
		defaultSideBoxes = new LinkedList<Module.Box>(sideBoxes);
		defaultBoxes = new HashMap<String, Box>(boxes);

		/* action */
		String actionName = config.get("class.action");
		if (actionName != null) {
			try {
				action = (IModuleAction) Class.forName(actionName).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void loadBoxes(String prefix, Collection<Box> boxList) {
		for (int i = 1; i < 100; i++) {
			String boxBaseKey = prefix + i;
			String renderer = config.get(boxBaseKey + ".renderer");
			String stepRenderer = config.get(boxBaseKey + ".step1.renderer");

			if (renderer != null || stepRenderer != null) {
				List<BoxStep> stepList = new LinkedList<BoxStep>();
				String boxTitle;
				if (renderer != null) {
					boxTitle = config.get(boxBaseKey + ".title." + locale.getLanguage());
					if (boxTitle == null) {
						boxTitle = config.get(boxBaseKey + ".title");
					}
				} else {
					for (int j = 1; i < 100; j++) {
						String stepBaseKey = boxBaseKey + ".step" + j;
						stepRenderer = config.get(stepBaseKey + ".renderer");

						if (stepRenderer != null) {
							String stepTitle = config.get(stepBaseKey + ".title." + locale.getLanguage());
							if (stepTitle == null) {
								stepTitle = config.get(stepBaseKey + ".title");
							}
							stepList.add(new BoxStep(stepTitle, stepRenderer));
						} else {
							break;
						}
					}
					BoxStep first = stepList.get(0);
					renderer = first.getRenderer();
					boxTitle = first.getTitle();
				}
				Box box = new Box(boxTitle, renderer, StringHelper.isTrue(config.get(boxBaseKey + ".action")), stepList);
				boxList.add(box);
				if (config.get(boxBaseKey + ".name") != null) {
					boxes.put(config.get(boxBaseKey + ".name"), box);
				}
			} else {
				break;
			}
		}
	}

	public Collection<Box> getMainBoxes() {
		return mainBoxes;
	}

	public Collection<Box> getSideBoxes() {
		return sideBoxes;
	}

	public String getName() {
		return name;
	}

	public String getHelpTitle() {
		return helpTitle;
	}

	public String getHelpText() {
		return helpText;
	}

	public String getRenderer() {
		return renderer;
	}

	/**
	 * set a renderer. Root folder is the root of the module.
	 * 
	 * @param renderer
	 */
	public void setRenderer(String renderer) {
		if (renderer != null) {
			this.renderer = URLHelper.mergePath(path, renderer);
		} else {
			this.renderer = null;
		}
	}

	/**
	 * set a renderer. Root folder is the root of webapps.
	 * 
	 * @param renderer
	 */
	public void setAbsoluteRenderer(String renderer) {
		this.renderer = renderer;
	}

	/**
	 * restore the renderer of the config file.
	 */
	public void restoreRenderer() {
		renderer = defaultRenderer;
	}

	/**
	 * restore all element to value defined in the config.
	 * 
	 * @throws IOException
	 */
	public void restoreAll() throws IOException {
		loadModule();
	}

	public void restoreTitle() {
		title = config.get("title");
	}

	public synchronized void restoreBoxes() {
		mainBoxes = new LinkedList<Module.Box>(defaultMainBoxes);
		sideBoxes = new LinkedList<Module.Box>(defaultSideBoxes);
		boxes = new HashMap<String, Module.Box>(defaultBoxes);
	}

	public String getTitle() {
		if (title != null) {
			return title;
		} else {
			return getName();
		}
	}

	public boolean isSidebar() {
		return sidebar;
	}

	public void setSidebar(boolean sidebar) {
		this.sidebar = sidebar;
	}

	public String getNavigationTitle() {
		return navigationTitle;
	}

	public Collection<Box> getNavigation() {
		return navigation;
	}
	
	public boolean removeNavigation(String name) {
		Box box = getBox(name);
		if (box != null) {
			boxes.remove(name);
			return navigation.remove(box);
		}
		return false;
	}

	
	public IModuleAction getAction() {
		return action;
	}

	public boolean isBreadcrumb() {
		return breadcrumb;
	}

	public void setBreadcrumb(boolean breadcrumb) {
		this.breadcrumb = breadcrumb;
	}

	/**
	 * return the list of URL to CSS files of the module.
	 * 
	 * @return
	 */
	public Collection<String> getCSS() {
		return cssURI;
	}

	/**
	 * return the list of URL to JS files of the module.
	 * 
	 * @return
	 */
	public Collection<String> getJS() {
		return jsURI;
	}

	public Properties loadEditI18n(GlobalContext globalContext) throws IOException {
		File file = new File(moduleRoot.getAbsolutePath(), "edit_" + globalContext.getEditLanguage());
		if (!file.exists()) {
			file = new File(URLHelper.mergePath(moduleRoot.getAbsolutePath(), "/i18n/edit_" + globalContext.getDefaultEditLanguage() + ".properties"));
		}
		Properties prop = null;
		if (file.exists()) {
			prop = new Properties();
			FileReader reader = new FileReader(file);
			prop.load(reader);
			reader.close();
		}
		return prop;
	}

	public String getToolsTitle() {
		return toolsTitle;
	}

	public void setToolsTitle(String toolsTitle) {
		this.toolsTitle = toolsTitle;
	}

	public String getToolsRenderer() {
		return toolsRenderer;
	}

	public void setToolsRenderer(String toolsRenderer) {
		if (toolsRenderer != null) {
			this.toolsRenderer = URLHelper.mergePath(path, toolsRenderer);
		} else {
			this.toolsRenderer = null;
		}
	}

	/**
	 * restore the tools renderer of the config file.
	 */
	public void restoreToolsRenderer() {
		toolsRenderer = defaultToolsRenderer;
	}

	public synchronized Box getBox(String name) {
		return boxes.get(name);
	}

	public synchronized Box createMainBox(String name, String title, String renderer, boolean action) {
		Box box = new Box(title, renderer, action);
		mainBoxes.add(box);
		if (name != null) {
			boxes.put(name, box);
		}
		return box;
	}

	/**
	 * create a a new side box.
	 * 
	 * @param name
	 * @param title
	 * @param renderer
	 * @param action
	 * @return null if box with the same name allready exist.
	 */
	public synchronized Box createSideBox(String name, String title, String renderer, boolean action) {
		if (getBox(name) != null) {
			return null;
		}
		Box box = new Box(title, renderer, action);
		sideBoxes.add(box);
		if (name != null) {
			boxes.put(name, box);
			setSidebar(true);
		}
		return box;
	}

	public synchronized void clearAllBoxes() {
		boxes.clear();
		sideBoxes.clear();
		mainBoxes.clear();
		setSidebar(false);
	}

	public String getBackUrl() {
		return backUrl;
	}

	public void setBackUrl(String backUrl) {
		this.backUrl = backUrl;
	}

	public String getPath() {
		return path;
	}

	public Collection<HtmlLink> getBreadcrumbList() {
		return breadcrumbLinks;
	}

	public void pushBreadcrumb(HtmlLink link) {
		if (breadcrumbLinks == null) {
			breadcrumbLinks = new Stack<HtmlLink>();
		}
		if (breadcrumbLinks.size() == 0 || !breadcrumbLinks.peek().equals(link)) {
			breadcrumbLinks.push(link);
		}
	}

	public HtmlLink popBreadcrumb() {
		return breadcrumbLinks.pop();
	}

	public void clearBreadcrump() {
		breadcrumbLinks = null;
	}

	public String getBreadcrumbTitle() {
		return breadcrumbTitle;
	}

	public void setBreadcrumbTitle(String breadcrumbTitle) {
		this.breadcrumbTitle = breadcrumbTitle;
	}

	public boolean isSearch() {
		return search;
	}

	public void setSearch(boolean search) {
		this.search = search;
	}

	public int getOrder() {
		return order;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public Set<String> getExcludeRoles() {
		return excludeRoles;
	}
	
	public String getVersion() {
		return StringHelper.neverNull(config.get("version"), "?");
	}

	public String getDescription() {
		return description;
	}

	public boolean haveRight(HttpSession session, User user) throws ModuleException {
		Boolean haveRight = getAction().haveRight(session, user);
		if (haveRight != null) {
			return haveRight;
		} else {
			if (user == null) {
				return false;
			}
			if (getRoles() == null) {
				haveRight = true;
			} else {
				haveRight =  user.validForRoles(getRoles());
			}
		}
		if (haveRight) {
			if (getExcludeRoles() == null) {
				haveRight = true;
			} else {
				Set<String> workingRoles = new HashSet<String>();
				workingRoles.addAll(getExcludeRoles());		
				workingRoles.retainAll(user.getRoles());
				haveRight = workingRoles.size() == 0;
			}			
		}
		return haveRight;
	}

	@Override
	public String toString() {
		return getClass().getName() + ' ' + getName();
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public List<Module> getChildren() {
		return children;
	}

	public void addChild(Module module) {
		this.children.add(module);
	}
}

