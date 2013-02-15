package org.javlo.navigation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.javlo.bean.Link;
import org.javlo.cache.ICache;
import org.javlo.comparator.MenuElementPriorityComparator;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentComponentsList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IDate;
import org.javlo.component.core.ILink;
import org.javlo.component.core.IPageRank;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.image.ImageTitleBean;
import org.javlo.component.meta.Category;
import org.javlo.component.meta.DateComponent;
import org.javlo.component.meta.Keywords;
import org.javlo.component.meta.LocationComponent;
import org.javlo.component.meta.MetaDescription;
import org.javlo.component.meta.NotSearchPage;
import org.javlo.component.meta.Tags;
import org.javlo.component.meta.TimeRangeComponent;
import org.javlo.component.text.Description;
import org.javlo.component.title.GroupTitle;
import org.javlo.component.title.WebSiteTitle;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.ContentManager;
import org.javlo.context.GlobalContext;
import org.javlo.helper.Logger;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.XMLManipulationHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.PersistenceService;
import org.javlo.service.exception.ServiceException;
import org.javlo.service.resource.Resource;
import org.javlo.utils.CollectionAsMap;
import org.javlo.utils.TimeRange;
import org.javlo.xml.NodeXML;
import org.javlo.xml.XMLFactory;
import org.javlo.ztatic.IStaticContainer;
import org.javlo.ztatic.StaticInfo;

/**
 * @author pvanderm
 */
public class MenuElement implements Serializable {

	private static final String NO_LANG = "no-lang";

	private static final long serialVersionUID = 1L;

	/**
	 * bean for the page, can be use in JSTL.
	 * 
	 * @author Patrick Vandermaesen
	 * 
	 */
	public static class PageBean implements Serializable {
		private static final long serialVersionUID = 1L;

		/*
		 * private PageDescription info; private String url; private String path; private boolean selected = false; private boolean lastSelected = false; private final List<PageBean> children = new LinkedList<PageBean>(); private final List<PageBean> realChildren = new LinkedList<PageBean>(); private String name = null; private String id = null; private String latestEditor; private String creationDate; private String modificationDate; private String templateId = null; private boolean realContent = false; private Map<String, String> roles = new HashMap<String, String>(); private Map<String, String> adminRoles = new HashMap<String, String>();
		 */

		public PageBean(ContentContext ctx, MenuElement page) {
			this.ctx = ctx;
			this.page = page;
		}

		private final MenuElement page;
		private final ContentContext ctx;

		public PageDescription getInfo() {
			try {
				return page.getSmartPageDescription(ctx);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public String getUrl() {
			return URLHelper.createURL(ctx, page);
		}

		public boolean isSelected() {
			try {
				return page.isSelected(ctx);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		/**
		 * get the list of children with isRealContent() and isVisible() is true.
		 * 
		 * @return
		 */
		public List<PageBean> getRealChildren() {
			List<PageBean> realChildren = new LinkedList<PageBean>();
			List<MenuElement> children = page.getChildMenuElementsList();
			for (MenuElement child : children) {
				try {
					if (child.isRealContent(ctx) && child.isVisible(ctx)) {
						realChildren.add(child.getPageBean(ctx));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return realChildren;
		}

		/**
		 * get the list of children with isRealContent() and isVisible() is true.
		 * 
		 * @return
		 */
		public List<PageBean> getChildren() {
			List<PageBean> childrenBean = new LinkedList<PageBean>();
			List<MenuElement> children = page.getChildMenuElementsList();
			for (MenuElement child : children) {
				try {
					childrenBean.add(child.getPageBean(ctx));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return childrenBean;
		}

		public String getName() {
			return page.getName();
		}

		public String getPath() {
			return page.getPath();
		}

		public String getLatestEditor() {
			return page.getLatestEditor();
		}

		public String getModificationDate() {
			try {
				return StringHelper.renderShortDate(ctx, page.getModificationDate());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public String getCreationDate() {
			try {
				return StringHelper.renderShortDate(ctx, page.getCreationDate());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public String getTemplateId() {
			return page.getTemplateId();
		}

		public boolean isLastSelected() {
			return page.isLastSelected(ctx);
		}

		public String getId() {
			return page.getId();
		}

		public boolean isRealContent() {
			try {
				return page.isRealContent(ctx);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		public Map<String, String> getRoles() {
			return new CollectionAsMap<String>(page.getUserRoles());
		}

		public Map<String, String> getAdminRoles() {
			return new CollectionAsMap<String>(page.getEditorRoles());
		}

		public String getShortURL() {
			try {
				return page.getShortURL(ctx);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public String getViewURL() {
			return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), page);
		}

		public boolean isAllreadyShortURL() {
			return page.isShortURL();
		}

		public String getForcedPageTitle() {
			try {
				return page.getForcedPageTitle(ctx);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public String getPageTitle() {
			try {
				return page.getPageTitle(ctx);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public int getLastAccess() {
			try {
				return page.getLastAccess(ctx);
			} catch (ServiceException e) {
				e.printStackTrace();
				return -1;
			}
		}

		public String getStartPublish() {
			return StringHelper.renderTime(page.getStartPublishDate());
		}

		public String getEndPublish() {
			return StringHelper.renderTime(page.getEndPublishDate());
		}

		public boolean isInsideTimeRange() {
			return page.isInsideTimeRange();
		}

		public boolean isVisible() {
			try {
				return page.isVisible(ctx);
			} catch (Exception e) {
				return false;
			}
		}

	}

	/**
	 * the description bean of the page, use for cache and JSTL.
	 * 
	 * @author Patrick Vandermaesen
	 * 
	 */
	public class PageDescription implements Serializable {

		private static final long serialVersionUID = 1L;

		String title = null;
		String localTitle = null;
		String subTitle = null;
		String pageTitle = null;
		String forcedPageTitle = null;
		String linkOn = null;
		Collection<IImageTitle> images = null;
		Collection<Link> staticResources = null;
		String description = null;
		String metaDescription = null;
		String keywords = null;
		String globalTitle = null;
		Date contentDate = null;
		Boolean empty = null;
		Boolean realContent = null;
		String label = null;
		String location = null;
		String category = null;
		Double pageRank = null;
		List<String> tags = null;
		String headerContent = null;
		List<String> groupID = null;
		List<String> childrenCategories = null;
		TimeRange timeRange = null;
		Boolean contentDateVisible = null;
		Boolean notInSearch = null;
		int depth = 0;
		public boolean visible = false;
		String referenceLanguage = null;
		boolean breakRepeat;
		int priority;

		public ImageTitleBean imageLink;

		public Collection<String> needdedResources = null;

		public boolean isVisible() {
			return visible;
		}

		public Collection<Link> getStaticResources() {
			return staticResources;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

		public int getDepth() {
			return depth;
		}

		public void setDepth(int depth) {
			this.depth = depth;
		}

		public String getTitle() {
			return title;
		}

		public String getLocalTitle() {
			return localTitle;
		}

		public void setLocalTitle(String localTitle) {
			this.localTitle = localTitle;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getSubTitle() {
			return subTitle;
		}

		public int getPriority() {
			return priority;
		}

		public void setSubTitle(String subTitle) {
			this.subTitle = subTitle;
		}

		public String getPageTitle() {
			return pageTitle;
		}

		public void setPageTitle(String pageTitle) {
			this.pageTitle = pageTitle;
		}

		public String getLinkOn() {
			return linkOn;
		}

		public void setLinkOn(String linkOn) {
			this.linkOn = linkOn;
		}

		public Collection<IImageTitle> getImages() {
			return images;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getMetaDescription() {
			return metaDescription;
		}

		public void setMetaDescription(String metaDescription) {
			this.metaDescription = metaDescription;
		}

		public String getKeywords() {
			return keywords;
		}

		public void setKeywords(String keywords) {
			this.keywords = keywords;
		}

		public String getGlobalTitle() {
			return globalTitle;
		}

		public void setGlobalTitle(String globalTitle) {
			this.globalTitle = globalTitle;
		}

		public Date getContentDate() {
			return contentDate;
		}

		public void setContentDate(Date contentDate) {
			this.contentDate = contentDate;
		}

		public Boolean isEmpty() {
			return empty;
		}

		public void setEmpty(Boolean isEmpty) {
			this.empty = isEmpty;
		}

		public boolean isRealContent() {
			if (realContent == null) {
				return false;
			}
			return realContent;
		}

		public boolean isRealContentNull() {
			return realContent == null;
		}

		public void setIsRealContent(Boolean isRealContent) {
			this.realContent = isRealContent;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public Double getPageRank() {
			return pageRank;
		}

		public void setPageRank(Double pageRank) {
			this.pageRank = pageRank;
		}

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}

		public String getHeaderContent() {
			return headerContent;
		}

		public void setHeaderContent(String headerContent) {
			this.headerContent = headerContent;
		}

		public List<String> getGroupID() {
			return groupID;
		}

		public void setGroupID(List<String> groupID) {
			this.groupID = groupID;
		}

		public TimeRange getTimeRange() {
			return timeRange;
		}

		public void setTimeRange(TimeRange timeRange) {
			this.timeRange = timeRange;
		}

		public Boolean isContentDateVisible() {
			return contentDateVisible;
		}

		public void setContentDateVisible(Boolean isContentDateVisible) {
			this.contentDateVisible = isContentDateVisible;
		}

		public String getReferenceLanguage() {
			return referenceLanguage;
		}

		public void setReferenceLanguage(String referenceLangugae) {
			this.referenceLanguage = referenceLangugae;
		}

		public boolean isBreakRepeat() {
			return breakRepeat;
		}

		public void setBreakRepeat(boolean breakRepeat) {
			this.breakRepeat = breakRepeat;
		}

		public List<String> getChildrenCategories() {
			return childrenCategories;
		}

	}

	public class SmartPageDescription extends PageDescription {

		ContentContext ctx;
		MenuElement page;

		private SmartPageDescription(ContentContext ctx, MenuElement page) {
			this.ctx = ctx;
			this.page = page;
		}

		@Override
		public boolean isVisible() {
			try {
				return page.isVisible();
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return false;
			}
		}

		@Override
		public Collection<Link> getStaticResources() {
			try {
				return page.getStaticResources(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public int getDepth() {
			return page.getDepth();
		}

		@Override
		public String getTitle() {
			try {
				return page.getTitle(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public String getLocalTitle() {
			try {
				return page.getLocalTitle(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public String getSubTitle() {
			try {
				return page.getSubTitle(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public int getPriority() {
			return page.getPriority();
		}

		@Override
		public String getPageTitle() {
			try {
				return page.getPageTitle(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public String getLinkOn() {
			try {
				return page.getLinkOn(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public Collection<IImageTitle> getImages() {
			try {
				return page.getImages(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public String getDescription() {
			try {
				return page.getDescription(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public String getMetaDescription() {
			try {
				return page.getMetaDescription(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public String getKeywords() {
			try {
				return page.getKeywords(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public String getGlobalTitle() {
			try {
				return page.getGlobalTitle(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public Date getContentDate() {
			try {
				return page.getContentDate(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public Boolean isEmpty() {
			try {
				return page.isEmpty(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public boolean isRealContent() {
			try {
				return page.isRealContent(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return false;
			}
		}

		@Override
		public boolean isRealContentNull() {
			try {
				return page.isRealContent(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return false;
			}
		}

		@Override
		public String getLabel() {
			try {
				return page.getLabel(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public String getLocation() {
			try {
				return page.getLocation(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public String getCategory() {
			try {
				return page.getCategory(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public Double getPageRank() {
			try {
				return page.getPageRank(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public List<String> getTags() {
			try {
				return page.getTags(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public String getHeaderContent() {
			try {
				return page.getHeaderContent(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public List<String> getGroupID() {
			try {
				return page.getGroupID(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public TimeRange getTimeRange() {
			try {
				return page.getTimeRange(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public Boolean isContentDateVisible() {
			try {
				return page.isContentDateVisible(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

		@Override
		public String getReferenceLanguage() {
			return page.getReferenceLanguage();
		}

		@Override
		public boolean isBreakRepeat() {
			return page.isBreakRepeat();
		}

		@Override
		public List<String> getChildrenCategories() {
			try {
				return page.getChildrenCategories(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return null;
			}
		}

	}

	// private GlobalContext globalContext;;

	protected PageDescription getPageDescription(ContentContext ctx) throws Exception {
		PageDescription pageDescription = getPageBeanCached(ctx, ctx.getLanguage());
		if (pageDescription.getTitle() == null) {
			pageDescription.category = getCategory(ctx);
			pageDescription.contentDate = getContentDate(ctx);
			pageDescription.description = getDescription(ctx);
			pageDescription.globalTitle = getGlobalTitle(ctx);
			pageDescription.groupID = getGroupID(ctx);
			pageDescription.headerContent = getHeaderContent(ctx);
			pageDescription.images = getImages(ctx);
			pageDescription.staticResources = getStaticResources(ctx);
			pageDescription.contentDateVisible = isContentDateVisible(ctx);
			pageDescription.empty = isEmpty(ctx);
			pageDescription.realContent = isRealContent(ctx);
			pageDescription.keywords = getKeywords(ctx);
			pageDescription.label = getLabel(ctx);
			pageDescription.linkOn = getLinkOn(ctx);
			pageDescription.location = getLocation(ctx);
			pageDescription.metaDescription = getMetaDescription(ctx);
			pageDescription.notInSearch = notInSearch(ctx);
			pageDescription.pageRank = getPageRank(ctx);
			pageDescription.pageTitle = getPageTitle(ctx);
			pageDescription.forcedPageTitle = getForcedPageTitle(ctx);
			pageDescription.subTitle = getSubTitle(ctx);
			pageDescription.tags = getTags(ctx);
			pageDescription.title = getTitle(ctx);
			pageDescription.localTitle = getLocalTitle(ctx);
			pageDescription.depth = getDepth();
			pageDescription.visible = isVisible();
			pageDescription.breakRepeat = isBreakRepeat();
			pageDescription.referenceLanguage = getReferenceLanguage();
			pageDescription.priority = getPriority();
			pageDescription.childrenCategories = getChildrenCategories(ctx);
		}
		return pageDescription;
	}

	public PageDescription getSmartPageDescription(ContentContext ctx) {
		return new SmartPageDescription(ctx, this);
	}

	public static MenuElement getInstance(GlobalContext globalContext) {
		MenuElement outMenuElement = new MenuElement();
		outMenuElement.releaseCache = true;
		return outMenuElement;
	}

	public static MenuElement searchChild(MenuElement elem, ContentContext ctx, String path, Collection<MenuElement> pastNode) throws Exception {
		// check if this is the path to homepage
		if (elem.getParent() == null && ('/' + elem.getName()).equals(path)) {
			return elem;
		}
		if (pastNode.contains(elem)) {
			return null;
		} else {
			pastNode.add(elem);
		}
		MenuElement res = null;
		List<MenuElement> children = elem.getChildMenuElementsWithVirtualList(ctx, false, false);
		for (MenuElement menuElement : children) {
			if (menuElement.getVirtualPath(ctx).equals(path)) {
				return menuElement;
			} else {
				res = searchChild(menuElement, ctx, path, pastNode);
				pastNode.remove(menuElement);
				if (res != null) {
					return res;
				}
			}
		}
		return res;
	}

	static MenuElement searchChildFromName(MenuElement elem, String name) {
		MenuElement res = null;
		List<MenuElement> children = elem.getChildMenuElements();
		for (int i = 0; (i < children.size()) && (res == null); i++) {
			if (children.get(i).getName().equals(name)) {
				res = children.get(i);
			} else {
				res = searchChildFromName(children.get(i), name);
			}
		}
		return res;
	}

	static MenuElement searchRealChild(MenuElement elem, ContentContext ctx, String path, Collection<MenuElement> pastNode) throws Exception {
		if (pastNode.contains(elem)) {
			return null;
		} else {
			pastNode.add(elem);
		}
		MenuElement res = null;
		List<MenuElement> children = elem.getChildMenuElementsList();
		for (MenuElement menuElement : children) {
			if (menuElement.getPath().equals(path)) {
				return menuElement;
			} else {
				res = searchRealChild(menuElement, ctx, path, pastNode);
				pastNode.remove(menuElement);
				if (res != null) {
					return res;
				}
			}
		}
		return res;
	}

	int priority = 10;

	String name = null;

	// String path = null;
	String id = StringHelper.getRandomId();

	Set<String> userRoles = new HashSet<String>();

	private String templateId;

	boolean visible = true;

	List<MenuElement> virtualParent = new LinkedList<MenuElement>();

	// ContentElementList contentElementList = null;

	// ContentElementList localContentElementList = null;

	List<MenuElement> virtualChild = new LinkedList<MenuElement>();

	List<MenuElement> childMenuElements = new LinkedList<MenuElement>();

	/* date and user */

	private ComponentBean[] componentBean = new ComponentBean[0];

	MenuElement parent = null;

	transient Map<String, ContentElementList> contentElementListMap = new HashMap<String, ContentElementList>();

	transient Map<String, ContentElementList> localContentElementListMap = new HashMap<String, ContentElementList>();

	private Date creationDate = new Date();

	private String creator = null;

	private Date modificationDate = new Date();

	private Date manualModificationDate = null;

	private String latestEditor = "";

	private boolean valid = false;

	private boolean blocked = false;

	private String blocker = "";

	private String validater = "";

	private String reversedLink = "";

	private Date validationDate = null;

	private String linkedURL = "";

	private boolean https = false;

	private String referenceLanguage = null;

	/**
	 * protect page localy if there are linked with other website.
	 */
	private boolean remote = false;

	private boolean breakRepeat = false;

	// private final Map<String, PageDescription> pageInfinityCache = new HashMap<String, PageDescription>();

	protected boolean releaseCache = false;

	// private final TimeMap<String, Object> pageTimeCache = new TimeMap<String, Object>(60 * (int) Math.round(((Math.random() + 1) * 60))); // cache between 1u and 2u, all cache can not be updated at the same time

	private final Map<String, String> replacement = new HashMap<String, String>();

	private final Collection<String> compToBeDeleted = new LinkedList<String>();

	private final Map<String, ComponentBean> contentToBeAdded = new HashMap<String, ComponentBean>(); // key is parent component id

	private final Set<String> editGroups = new HashSet<String>();

	private Date latestUpdateLinkedData = null;

	public static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MenuElement.class.getName());

	private String shortURL = null;

	private Date startPublishDate = null;

	private Date endPublishDate = null;

	protected MenuElement() {
	}

	public void addAccess(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String key = getAccessKey(new Date());
		int todayAccess = 0;

		String currentAccessString = globalContext.getData(key);
		if (currentAccessString == null) {
			currentAccessString = "0";
		}

		todayAccess = Integer.parseInt(currentAccessString);
		todayAccess++;
		globalContext.setData(key, "" + todayAccess);
	}

	private void addAllRepeatContent(ContentElementList list, ContentContext ctx) throws Exception {

		if (getParent() != null) {
			getParent().addAllRepeatContent(list, ctx);
		}
		ContentElementList currentList = getAllLocalContent(ctx);
		while (currentList.hasNext(ctx)) {
			IContentVisualComponent comp = currentList.next(ctx);
			if ((comp != null) && (comp.isRepeat())) {
				list.addRepeatElement(comp);
			}
		}
	}

	public void addChildMenuElement(MenuElement menuElement) {
		synchronized (getLock()) {
			menuElement.setParent(this);
			if (childMenuElements == Collections.EMPTY_LIST) {
				childMenuElements = new LinkedList<MenuElement>();
			}
			childMenuElements.add(menuElement);
			sortChild();
		}
	}

	public void addChildMenuElementAutoPriority(MenuElement menuElement) {
		synchronized (getLock()) {
			menuElement.setParent(this);
			if (childMenuElements == Collections.EMPTY_LIST) {
				childMenuElements = new LinkedList<MenuElement>();
			}

			int priority = 0;
			for (MenuElement elem : childMenuElements) {
				if (elem.getPriority() >= priority) {
					priority = elem.getPriority();
				}
			}

			menuElement.setPriority(priority + 10);
			childMenuElements.add(menuElement);
			sortChild();
		}
	}

	public void addChildMenuElementOnTop(MenuElement menuElement) {
		NavigationHelper.changeStepPriority(getChildMenuElements(), 10);
		synchronized (getLock()) {
			menuElement.setParent(this);
			if (childMenuElements == Collections.EMPTY_LIST) {
				childMenuElements = new LinkedList<MenuElement>();
			}

			int priority = Integer.MAX_VALUE;

			for (MenuElement elem : childMenuElements) {
				if (elem.getPriority() <= priority) {
					priority = elem.getPriority();
				}
			}

			if (priority == Integer.MAX_VALUE) {
				priority = 10;
			}
			menuElement.setPriority(priority - 5);
			childMenuElements.add(menuElement);
			sortChild();
		}
	}

	public void addCompToDelete(String id) {
		synchronized (compToBeDeleted) {
			compToBeDeleted.add(id);
		}
	}

	public void addContent(String parentId, ComponentBean bean) {
		addContent(parentId, bean, true);
	}

	public void addContent(String parentId, ComponentBean bean, boolean realeaseCache) {

		assert bean != null;
		synchronized (getLock()) {
			synchronized (componentBean) {
				ComponentBean[] newBean = new ComponentBean[componentBean.length + 1];
				int j = 0;
				boolean parentFound = false;
				for (ComponentBean element : componentBean) {
					if (element.getId().equals(parentId)) {
						newBean[j] = element;
						j++;
						newBean[j] = bean;
						parentFound = true;
						bean.setArea(newBean[j].getArea());
					} else {
						newBean[j] = element;
					}
					j++;
				}
				if (!parentFound) { // component not found
					newBean[0] = bean;
					for (int i = 0; i < componentBean.length; i++) {
						newBean[i + 1] = componentBean[i];
					}
				}
				componentBean = newBean;
			}
		}
		if (realeaseCache) {
			releaseCache();
		}
	}

	public void addEditorRoles(String group) {
		editGroups.add(group);
	}

	/**
	 * add prepared component
	 * 
	 * @param ctx
	 * @return true if component is added.
	 */
	private boolean addPreparedContent(ContentContext ctx) {
		if (contentToBeAdded.size() == 0) {
			return false;
		}
		logger.info("try to add component : " + contentToBeAdded.size());
		synchronized (getLock()) {
			synchronized (contentToBeAdded) {
				ComponentBean[] newBean = new ComponentBean[componentBean.length + contentToBeAdded.size()];
				ComponentBean[] workBean = componentBean;
				boolean parentFound = true;
				while (parentFound && contentToBeAdded.size() > 0) {
					parentFound = false;
					int j = 0;
					for (ComponentBean element : workBean) {
						if (element != null) {
							if (contentToBeAdded.keySet().contains(element.getId())) {
								newBean[j] = element;
								j++;
								newBean[j] = contentToBeAdded.get(element.getId());
								contentToBeAdded.remove(element.getId());
								parentFound = true;
							} else {
								newBean[j] = element;
							}
							j++;
						}
					}
					workBean = newBean;
					newBean = new ComponentBean[workBean.length];
				}
				if (contentToBeAdded.size() > 0) { // component not found
					if (contentToBeAdded.size() > 1) { // component not found
						logger.warning("bad structure in contentToBeAdded : more that one parent id not found");
						StringWriter writer = new StringWriter();
						PrintWriter out = new PrintWriter(writer);
						out.println("error : add prepare content");
						out.println("");
						out.println("page : " + getPath());
						out.println("");
						out.close();
						// NetHelper.sendMailToAdministrator(ctx, "bad structure in contentToBeAdded : more that one parent id not found", writer.toString());
						logger.warning(writer.toString());
					} else {
						newBean = new ComponentBean[workBean.length];
						newBean[0] = contentToBeAdded.values().iterator().next();
						for (int i = 0; i < workBean.length - 1; i++) {
							newBean[i + 1] = workBean[i];
						}
						workBean = newBean;
					}
				}
				contentToBeAdded.clear();
				for (int i = 0; i < workBean.length; i++) {
					if (workBean[i] == null) {
						StringWriter writer = new StringWriter();
						PrintWriter out = new PrintWriter(writer);
						out.println("error : add prepare content");
						out.println("");
						out.println("page : " + getPath());
						out.println("error on bean : " + i);
						out.println("");
						out.close();
						// NetHelper.sendMailToAdministrator(ctx, "error null bean found.", writer.toString());
						logger.warning(writer.toString());
					}
				}
				componentBean = workBean;
			}
		}
		setModificationDate(new Date());
		releaseCache();
		return true;
	}

	private void addRepeatContent(ContentElementList list, ContentContext ctx) throws Exception {

		if (getParent() != null && !isBreakRepeat()) {
			getParent().addRepeatContent(list, ctx);
		}
		ContentElementList currentList = getLocalContent(ctx);
		while (currentList.hasNext(ctx)) {
			IContentVisualComponent comp = currentList.next(ctx);
			if ((comp != null) && (comp.isRepeat())) {
				list.addRepeatElement(comp);
			}
		}
	}

	private void addVirtualChild(MenuElement vChild) {
		if (vChild != null) {
			if (!vChild.getId().equals(getId())) {
				virtualChild.add(vChild);
			}
		}
	}

	public void addVirtualParent(String menuId) {
		if (menuId.equals(getId())) {
			return;
		}
		MenuElement root = getRoot();
		MenuElement node = root.searchChildFromId(menuId);
		if (root.getId().equals(menuId)) {
			node = root;
		}
		if (node != null) {
			virtualParent.add(node);
			node.addVirtualChild(this);
		}
	}

	public void clearEditorGroups() {
		editGroups.clear();
	}

	/**
	 * clear content of the page, and delete all children.
	 */
	private void clearPage() {
		contentElementListMap.clear();
		childMenuElements.clear();
	}

	public void clearVirtualParent() {
		for (MenuElement parent : virtualParent) {
			parent.removeVirtualChild(this);
		}
		virtualParent.clear();
	}

	/**
	 * count the component of a specific type on the current page.
	 * 
	 * @param ctx
	 *            the current context.
	 * @param inComponentType
	 *            the type of the component
	 * @return a count of component.
	 * @throws Exception
	 */
	public int countComponentInCtx(ContentContext ctx, String inComponentType) throws Exception {
		int c = 0;
		ContentElementList content = getAllContent(ctx);
		ContentContext lgCtx = ctx;
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (globalContext.isAutoSwitchToDefaultLanguage() && !this.isRealContent(ctx)) {
			lgCtx = ctx.getContextWithContent(this);
			if (lgCtx == null) {
				lgCtx = ctx;
			}
		}
		while (content.hasNext(lgCtx)) {
			IContentVisualComponent cpnt = content.next(lgCtx);
			if (cpnt.getType().equals(inComponentType)) {
				c++;
			}
		}
		return c;
	}

	public boolean deleteCompList(ContentContext ctx) {
		if (compToBeDeleted.size() == 0) {
			return false;
		}
		synchronized (compToBeDeleted) {
			synchronized (getLock()) {
				List<ComponentBean> outList = new LinkedList<ComponentBean>();
				for (int i = 0; i < componentBean.length; i++) {
					if (!compToBeDeleted.contains(componentBean[i].getId())) {
						outList.add(componentBean[i]);
					} else {
						IContentVisualComponent comp = ComponentFactory.getComponentWithType(ctx, componentBean[i].getType());
						if (comp != null) {
							comp.delete(ctx);
						}
					}
				}
				componentBean = new ComponentBean[outList.size()];
				outList.toArray(componentBean);
			}
			logger.info("deleted a group of component : " + compToBeDeleted.size());
			compToBeDeleted.clear();
			releaseCache();
		}
		return true;
	}

	public void endRendering(ContentContext ctx) {
		if (deleteCompList(ctx)) {
			setModificationDate(new Date());
		}
		if (addPreparedContent(ctx)) {
			setManualModificationDate(new Date());
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		MenuElement elem = (MenuElement) obj;
		boolean res = true;
		if (!isMetadataEquals(elem)) {
			res = false;
		} else {
			if (elem.getChildMenuElements().size() != getChildMenuElements().size()) {
				res = false;
			} else {
				if (!isContentEquals(elem)) {
					res = false;
				} else {
					res = isChildrenEquals(elem);
				}
			}
		}
		return res;
	}

	public int getAccess(ContentContext ctx, Date date) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String key = getAccessKey(date);
		if (globalContext.getData(key) == null) {
			return 0;
		} else {
			return Integer.parseInt(globalContext.getData(key));
		}
	}

	private String getAccessKey(Date date) {
		return "clk__" + getPath() + "__" + StringHelper.renderDate(date, "yyyy-MM-dd");
	}

	public MenuElement[] getAllChildren() throws Exception {
		ArrayList<MenuElement> list = getChildElementRecursive(this, 0);
		MenuElement[] res = new MenuElement[list.size()];
		list.toArray(res);
		return res;
	}

	public List<MenuElement> getAllChildrenWithComponentType(ContentContext ctx, String type) throws Exception {
		return getChildElementRecursive(ctx, this, type, 0);
	}

	public ContentElementList getAllContent(ContentContext ctx) throws Exception {

		ContentContext ctxPage = ctx.getContextOnPage(this);

		ContentElementList elemList = new ContentElementList(getAllLocalContent(ctxPage));

		if ((getParent() != null) && (ctxPage.getRenderMode() != ContentContext.EDIT_MODE)) {
			getParent().addAllRepeatContent(elemList, ctxPage);
		}

		elemList.initialize(ctxPage);

		return elemList;
	}

	/**
	 * get content for all area
	 * 
	 * @param ctx
	 *            current context
	 * @return
	 * @throws Exception
	 */
	private synchronized ContentElementList getAllLocalContent(ContentContext ctx) throws Exception {
		ContentElementList localContentElementList = localContentElementListMap.get(ctx.getRequestContentLanguage());
		if (localContentElementList == null) {
			logger.fine("update all local content on (ctx:" + ctx + ")");

			/*
			 * ComponentBean[] localComponentBean; synchronized (componentBean) { localComponentBean = new ComponentBean[componentBean.length]; System.arraycopy(componentBean, 0, localComponentBean, 0, componentBean.length); }
			 */
			localContentElementList = new ContentElementList(componentBean, ctx, this, true);

			localContentElementListMap.put(ctx.getRequestContentLanguage(), localContentElementList);
		}

		localContentElementList.initialize(ctx);
		return localContentElementList;
	}

	public ComponentBean[] getAllLocalContentBean() throws Exception {
		synchronized (componentBean) {
			return componentBean;
		}
	}

	public Collection<Resource> getAllResources(ContentContext ctx) throws Exception {
		ContentElementList contentList = getAllContent(ctx); // search date in
		// all area
		Collection<Resource> outList = new LinkedList<Resource>();
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent comp = contentList.next(ctx);
			if (comp instanceof IStaticContainer) {
				outList.addAll(((IStaticContainer) comp).getAllResources(ctx));
			}
		}
		return outList;
	}

	/**
	 * return the page of this page
	 * 
	 * @return a path.
	 */
	public List<String> getAllVirtualPath() {
		List<String> outVPath = new LinkedList<String>();
		try {
			if (parent == this) {
				throw new Exception("recursive reference !!!");
			} else {

				if (parent == null) {
					if (getName().equals("root")) {
						outVPath.add("");
					} else {
						outVPath.add(getName());
					}
				} else {
					outVPath.add(parent.getPath() + '/' + getName());
				}

				for (MenuElement vparent : getVirtualParent()) {
					outVPath.add(vparent.getPath() + '/' + getName());
				}

				return outVPath;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * get the content in current language and in default languages order if not exist.
	 */
	public ContentElementList getBestLanguageContent(ContentContext ctx) throws Exception {
		if (isRealContent(ctx)) {
			return getContent(ctx);
		} else {
			ContentContext lgCtx = new ContentContext(ctx);
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			Collection<String> defaultLgs = globalContext.getDefaultLanguages();
			for (String lg : defaultLgs) {
				lgCtx.setRequestContentLanguage(lg);
				if (isRealContent(lgCtx)) {
					return getContent(lgCtx);
				}
			}
		}
		return getContent(ctx);
	}

	public String getBlocker() {
		return blocker;
	}

	protected String getCacheKey(String subkey) {
		return this.getClass().getName() + "_" + getId() + "_" + subkey;
	}

	/**
	 * get the category of the page (category component)
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String getCategory(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.category != null) {
			return desc.category;
		}
		String res = "";
		ContentContext noAreaCtx = ctx.getContextWithoutArea();

		if (noAreaCtx.getRenderMode() == ContentContext.EDIT_MODE) {
			noAreaCtx.setRenderMode(ContentContext.PREVIEW_MODE); // get info for preview mode (with repeat elements)
		}

		IContentComponentsList contentList = getContent(noAreaCtx);
		while (contentList.hasNext(noAreaCtx)) {
			IContentVisualComponent elem = contentList.next(noAreaCtx);
			if (elem.getType().equals(Category.TYPE)) {
				res = elem.getValue(noAreaCtx);
			}
		}
		desc.category = StringUtils.replace(res, "\"", "&quot;");

		return desc.category;
	}

	ArrayList<MenuElement> getChildElementRecursive(ContentContext ctx, MenuElement elem, String type, int deph) throws Exception {
		ArrayList<MenuElement> result = new ArrayList<MenuElement>();
		if (type == null) {
			return result;
		}
		if (elem.countComponentInCtx(ctx, type) > 0) {
			result.add(elem);
		}
		Collection<MenuElement> children = elem.getChildMenuElements();
		for (MenuElement child : children) {
			result.addAll(getChildElementRecursive(ctx, child, type, deph + 1));
		}
		return result;
	}

	ArrayList<MenuElement> getChildElementRecursive(MenuElement elem, int deph) throws Exception {
		ArrayList<MenuElement> result = new ArrayList<MenuElement>();
		result.add(elem);
		Collection<MenuElement> children = elem.getChildMenuElements();
		for (MenuElement child : children) {
			result.addAll(getChildElementRecursive(child, deph + 1));
		}
		return result;
	}

	public String[] getChildList() throws Exception {
		ArrayList<String> result = getChildListRecursive(this, 0);
		String[] finalResult = new String[result.size()];
		result.toArray(finalResult);
		return finalResult;
	}

	/*
	 * static MenuElement searchChild(MenuElement elem, ContentContext ctx, String path, int depth) { if (depth > MAX_SEARCH_DEPTH) { return null; } MenuElement res = null; List<MenuElement> children = elem.getChildMenuElementsWithVirtualList(false, false); for (MenuElement menuElement : children) { List<String> paths = menuElement.getAllVirtualPath(ctx); if (paths.contains(path)) { return menuElement; } else { res = searchChild(menuElement, ctx, path, depth+1); if (res != null) { return res; } } }
	 * 
	 * return res; }
	 */

	ArrayList<String> getChildListRecursive(MenuElement elem, int deph) throws Exception {
		ArrayList<String> result = new ArrayList<String>();
		result.add(elem.getPath());
		Collection<MenuElement> children = elem.getChildMenuElements();
		for (MenuElement child : children) {
			result.addAll(getChildListRecursive(child, deph + 1));
		}
		return result;
	}

	/**
	 * get the child list of the current element.
	 * 
	 * @return a array of children.
	 */
	public List<MenuElement> getChildMenuElements() {
		return childMenuElements;
	}

	public List<MenuElement> getChildMenuElements(ContentContext ctx, boolean visible) throws Exception {
		if (visible) {
			return getVisibleChildMenuElements(ctx);
		} else {
			return getChildMenuElements();
		}
	}

	/**
	 * get the child list of the current element.
	 * 
	 * @return a list of children.
	 */
	public List<MenuElement> getChildMenuElementsList() {
		return childMenuElements;
	}

	public List<MenuElement> getChildMenuElementsList(ContentContext ctx, boolean visible) throws Exception {
		if (visible) {
			return getVisibleChildMenuElementsList(ctx);
		} else {
			return getChildMenuElementsList();
		}
	}

	public List<MenuElement> getChildMenuElementsWithVirtual(ContentContext ctx, boolean onlyVisible, boolean virtualBefore) throws Exception {
		List<MenuElement> allChild = new LinkedList<MenuElement>();
		if (virtualBefore) {
			allChild.addAll(getVirtualChild(ctx, onlyVisible));
			allChild.addAll(getChildMenuElements(ctx, onlyVisible));
		} else {
			allChild.addAll(getChildMenuElements(ctx, onlyVisible));
			allChild.addAll(getVirtualChild(ctx, onlyVisible));
		}
		return allChild;
	}

	public List<MenuElement> getChildMenuElementsWithVirtualList(ContentContext ctx, boolean visible, boolean virtualBefore) throws Exception {
		List<MenuElement> allChild = new LinkedList<MenuElement>();
		if (virtualBefore) {
			allChild.addAll(getChildMenuElementsList(ctx, visible));
			allChild.addAll(getVirtualChild(ctx, visible));
		} else {
			allChild.addAll(getVirtualChild(ctx, visible));
			allChild.addAll(getChildMenuElements(ctx, visible));
		}
		return allChild;
	}

	public ComponentBean[] getContent() {
		return componentBean;
	}

	/**
	 * get content of the current area
	 * 
	 * @param ctx
	 *            the content context
	 * @return a list of component
	 */
	public ContentElementList getContent(ContentContext ctx) throws Exception {

		ContentContext pageCtx = ctx.getContextOnPage(this);
		ContentElementList elemList = new ContentElementList(getLocalContent(pageCtx));

		if (!isBreakRepeat()) {
			if ((getParent() != null) && (ctx.getRenderMode() != ContentContext.EDIT_MODE)) {
				getParent().addRepeatContent(elemList, pageCtx);
			}
		}

		elemList.initialize(pageCtx);

		return elemList;
	}

	/**
	 * return the content separed on the date component.
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public Map<Date, List<IContentVisualComponent>> getContentByDate(ContentContext ctx) throws Exception {

		Map<Date, List<IContentVisualComponent>> outContentByDate = new HashMap<Date, List<IContentVisualComponent>>();

		Date currentDate = null;
		ContentContext noAreaCtx = new ContentContext(ctx);
		noAreaCtx.setArea(null);
		ContentElementList content = getBestLanguageContent(noAreaCtx);
		while (content.hasNext(noAreaCtx)) {
			IContentVisualComponent comp = content.next(noAreaCtx);
			if (comp instanceof IDate) {
				IDate dateComp = (IDate) comp;
				currentDate = dateComp.getDate(ctx);
				if (dateComp instanceof DynamicComponent) {
					if (currentDate != null) {
						if (outContentByDate.get(currentDate) == null) {
							outContentByDate.put(currentDate, new LinkedList<IContentVisualComponent>());
						}
						outContentByDate.get(currentDate).add(comp);
					}
					currentDate = null;
				}
			}
			if (currentDate != null) {
				if (outContentByDate.get(currentDate) == null) {
					outContentByDate.put(currentDate, new LinkedList<IContentVisualComponent>());
				}
				outContentByDate.get(currentDate).add(comp);
			}
		}

		return outContentByDate;
	}

	public List<IContentVisualComponent> getContentByType(ContentContext ctx, String type) throws Exception {

		List<IContentVisualComponent> outComp = new LinkedList<IContentVisualComponent>();

		ContentElementList content = getAllContent(ctx);
		while (content.hasNext(ctx)) {
			IContentVisualComponent comp = content.next(ctx);
			if (comp.getType().equals(type)) {
				outComp.add(comp);
			}
		}
		content.initialize(ctx);

		return outComp;
	}

	public List<IContentVisualComponent> getContentByImplementation(ContentContext ctx, Class clazz) throws Exception {

		List<IContentVisualComponent> outComp = new LinkedList<IContentVisualComponent>();

		ContentElementList content = getAllContent(ctx);
		while (content.hasNext(ctx)) {
			IContentVisualComponent comp = content.next(ctx);
			if (clazz.isInstance(comp)) {
				outComp.add(comp);
			}
		}
		content.initialize(ctx);

		return outComp;
	}

	/**
	 * return a language with content. If there are content in current language, it is returned.
	 * 
	 * @return a ContentContext with content or current context if there are no content in any language.
	 * @throws Exception
	 */
	public ContentContext getContentContextWithContent(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (!globalContext.isAutoSwitchToDefaultLanguage()) {
			return ctx;
		}
		if (isRealContent(ctx)) {
			return ctx;
		} else {
			ContentContext lgCtx = new ContentContext(ctx);
			if (globalContext.isAutoSwitchToDefaultLanguage()) {
				Collection<String> defaultLgs = globalContext.getDefaultLanguages();
				for (String lg : defaultLgs) {
					/*
					 * if (globalContext.getLanguages().contains(lg)) { // if content lg exist as lgCtx.setLanguage(lg); }
					 */
					lgCtx.setContentLanguage(lg);
					lgCtx.setRequestContentLanguage(lg);
					if (isRealContent(lgCtx)) {
						return lgCtx;
					}
				}
			}
			if (globalContext.isAutoSwitchToDefaultLanguage()) {
				Collection<String> languages = globalContext.getContentLanguages();
				for (String lg : languages) {
					/*
					 * if (globalContext.getLanguages().contains(lg)) { // if content lg exist as lgCtx.setLanguage(lg); }
					 */
					lgCtx.setContentLanguage(lg);
					lgCtx.setRequestContentLanguage(lg);
					if (isRealContent(lgCtx)) {
						return lgCtx;
					}
				}
			}
		}
		return ctx;

	}

	/**
	 * get the date found in the content.
	 * 
	 * @return
	 * @throws Exception
	 */
	public Date getContentDate(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.contentDate != null) {
			return desc.contentDate;
		}
		desc.contentDate = getContentDateComponent(ctx);
		return desc.contentDate;
	}

	public Date getContentDateComponent(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Iterator<String> defaultLgs = globalContext.getDefaultLanguages().iterator();
		ContentContext localContext = new ContentContext(ctx);
		while (isEmpty(localContext) && defaultLgs.hasNext()) {
			localContext.setRequestContentLanguage(defaultLgs.next());
		}
		if (!isEmpty(localContext)) {
			ContentElementList contentList = getAllContent(localContext); // search date in all area
			while (contentList.hasNext(ctx)) {
				IContentVisualComponent comp = contentList.next(ctx);
				if (comp.getType() == DateComponent.TYPE) {
					return ((DateComponent) comp).getDate();
				} else if (comp.getType() == TimeRangeComponent.TYPE) {
					return ((TimeRangeComponent) comp).getStartDate();
				}
			}
		}
		return null;
	}

	/**
	 * return content Date and modification data if no content date.
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public Date getContentDateNeverNull(ContentContext ctx) throws Exception {
		Date contentDate = getContentDate(ctx);
		if (contentDate != null) {
			return contentDate;
		} else {
			return getModificationDate();
		}

	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getCreator() {
		if ((creator == null) || (creator.equals("null"))) {
			return "";
		}
		return creator;
	}

	/**
	 * return the depth of the current element
	 * 
	 * @return a depth
	 */
	public int getDepth() {
		return ContentManager.getPathDepth(getPath());
	}

	/**
	 * get description of the page (description component)
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String getDescription(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.description != null) {
			return desc.description;
		}
		String res = "";
		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setArea(null);

		if (newCtx.getRenderMode() == ContentContext.EDIT_MODE) {
			newCtx.setRenderMode(ContentContext.PREVIEW_MODE); // get info for preview mode (with repeat elements)
		}

		IContentComponentsList contentList = getAllContent(newCtx);
		while (contentList.hasNext(newCtx)) {
			IContentVisualComponent elem = contentList.next(newCtx);
			if (elem.getType().equals(Description.TYPE)) {
				res = res + elem.getValue(newCtx);
			}
		}
		desc.description = StringUtils.replace(res, "\"", "&quot;");
		return desc.description;
	}

	public Set<String> getEditorRoles() {
		return editGroups;
	}

	public Collection<String> getExternalResources(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.needdedResources == null) {
			Collection<String> outResources = new LinkedList<String>();
			ContentElementList content = getAllContent(ctx);
			while (content.hasNext(ctx)) {
				IContentVisualComponent comp = content.next(ctx);
				Collection<String> resources = comp.getExternalResources(ctx);
				for (String res : resources) {
					if (outResources.contains(res)) {
						// TODO: check if this line can be removed
						// outResources.remove(res);
					}
					if (!outResources.contains(res)) {
						outResources.add(res);
					}
				}
				// outResources.addAll(comp.getExternalResources());
			}
			desc.needdedResources = outResources;
		}
		return desc.needdedResources;
	}

	/**
	 * return the field value of the first component match with the component type
	 * 
	 * @param ctx
	 * @param componentType
	 * @param fieldName
	 * @param defaultValue
	 *            value if component or field not found
	 * @return
	 * @throws Exception
	 */
	public String getFieldValue(ContentContext ctx, String componentType, String fieldName, String defaultValue) throws Exception {
		ContentElementList content = getAllContent(ctx);
		while (content.hasNext(ctx)) {
			IContentVisualComponent comp = content.next(ctx);
			if (comp.getType().equals(componentType)) {
				DynamicComponent dynComp = (DynamicComponent) comp;
				if (dynComp.getFieldValue(ctx, fieldName) != null) {
					return dynComp.getFieldValue(ctx, fieldName);
				} else {
					return defaultValue;
				}
			}
		}
		return defaultValue;
	}

	/**
	 * return the field values of all components match with the component type
	 * 
	 * @param ctx
	 * @param componentType
	 * @param fieldName
	 * @param defaultValue
	 *            value if component or field not found
	 * @return a set of values, empty set if not found.
	 * @throws Exception
	 */
	public Set<String> getFieldValues(ContentContext ctx, String componentType, String fieldName) throws Exception {
		Set<String> values = new HashSet<String>();
		ContentElementList content = getAllContent(ctx);
		while (content.hasNext(ctx)) {
			IContentVisualComponent comp = content.next(ctx);
			if (comp.getType().equals(componentType)) {
				DynamicComponent dynComp = (DynamicComponent) comp;
				if (dynComp.getFieldValue(ctx, fieldName) != null) {
					values.add(dynComp.getFieldValue(ctx, fieldName));

				}
			}
		}
		return values;
	}

	public String getFullLabel(ContentContext ctx) throws Exception {

		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setArea(null); // warning : check if the method is needed.

		PageDescription desc = getPageDescriptionCached(ctx, newCtx.getLanguage());

		if (desc.label != null) {
			return desc.label;
		}

		newCtx.setRequestContentLanguage(ctx.getLanguage()); // label is from
		// navigation
		// language
		desc.label = getContent(newCtx).getLabel();

		if (desc.label != null) {
			if ((desc.label.trim().length() == 0) && (name != null)) {
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
				if (globalContext.isAutoSwitchToDefaultLanguage()) {
					ContentContext defaultLgCtx = newCtx.getContextWithContent(this);
					if (defaultLgCtx != null) {
						desc.label = getContent(defaultLgCtx).getLabel();
					}
					if ((desc.label.trim().length() == 0) && (name != null)) {
						desc.label = name;
					}
				} else {
					desc.label = name;
				}

			}
		}
		desc.label = StringHelper.removeTag(desc.label);
		return desc.label;
	}

	public String getFullName() {
		return name;
	}

	public String getGlobalTitle(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.globalTitle != null) {
			return desc.globalTitle;
		}
		IContentComponentsList contentList = getAllContent(ctx);
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent elem = contentList.next(ctx);
			if (elem.getType().equals(WebSiteTitle.TYPE)) {
				desc.globalTitle = elem.getValue(ctx);
				return desc.globalTitle;
			}
		}
		return GlobalContext.getInstance(ctx.getRequest()).getGlobalTitle();
	}

	public List<String> getGroupID(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.groupID != null) {
			return desc.groupID;
		}

		ContentContext lgDefaultCtx = new ContentContext(ctx);
		lgDefaultCtx.setArea(null);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Iterator<String> defaultLg = globalContext.getDefaultLanguages().iterator();

		Iterator<IContentVisualComponent> groupIdList = getContentByType(lgDefaultCtx, GroupTitle.TYPE).iterator();
		GroupTitle groupIdComp = null;
		if (groupIdList.hasNext()) {
			groupIdComp = (GroupTitle) groupIdList.next();
		}
		while (groupIdComp == null && defaultLg.hasNext()) {
			String lg = defaultLg.next();
			lgDefaultCtx.setContentLanguage(lg);
			lgDefaultCtx.setRequestContentLanguage(lg);
			groupIdList = getContentByType(lgDefaultCtx, GroupTitle.TYPE).iterator();
			groupIdComp = null;
			if (groupIdList.hasNext()) {
				groupIdComp = (GroupTitle) groupIdList.next();
			}
		}
		List<String> outGroupID = new LinkedList<String>();

		IContentComponentsList contentList = getAllContent(lgDefaultCtx);
		while (contentList.hasNext(lgDefaultCtx)) {
			IContentVisualComponent elem = contentList.next(lgDefaultCtx);
			if (elem.getType().equals(GroupTitle.TYPE)) {
				outGroupID.add(elem.getId());
			}
		}
		desc.groupID = outGroupID;

		return desc.groupID;
	}

	public List<String> getChildrenCategories(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.childrenCategories != null) {
			return desc.childrenCategories;
		}

		List<String> categories = new LinkedList<String>();

		MenuElement[] children = getAllChildren();
		for (MenuElement child : children) {
			String cat = child.getCategory(ctx);
			if (cat != null && cat.trim().length() > 0 && !categories.contains(cat)) {
				categories.add(cat);
			}
		}

		desc.childrenCategories = categories;

		return desc.childrenCategories;
	}

	public String getHeaderContent(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.headerContent != null) {
			if (desc.headerContent.trim().length() == 0) {
				return null;
			} else {
				return desc.headerContent;
			}
		}
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		ContentElementList content = getAllContent(ctx);
		while (content.hasNext(ctx)) {
			IContentVisualComponent comp = content.next(ctx);
			if (comp.getHeaderContent(ctx) != null) {
				out.println(comp.getHeaderContent(ctx));
			}
		}
		out.close();
		desc.headerContent = writer.toString();
		if (desc.headerContent.trim().length() == 0) {
			return null;
		} else {
			return desc.headerContent;
		}
	}

	/**
	 * generate a list of navigation element. replace #id with the page id.
	 * 
	 * @param startTag
	 *            insert before path sample : <option value=#id>.
	 * @param endTag
	 *            insert after path : </option>
	 * @return a string with XHTML code
	 * @throws Exception
	 * @Deprecated use XHTMLHelper.getHTMLChildList
	 */
	public String getHTMLChildList(String startTag, String endTag) throws Exception {
		return XHTMLHelper.getHTMLChildList(this, null, startTag, null, endTag, true);
	}

	/*
	 * public ContentElementList getViewContent(ContentContext ctx, int format) throws Exception { ContentElementList contentElementList = contentElementListMap.get(ctx.getRequestContentLanguage()); if (contentElementList == null) { Content content = Content.createContent(ctx.getRequest()); ContentContext localContext = new ContentContext(ctx); if (!content.contentExistForContext(ctx)) { GlobalContext globalCotext = GlobalContext.getInstance(ctx.getRequest()); Set<String> lgs = globalCotext.getContentLanguages(); for (String lg : lgs) { localContext.setLanguage(lg); if (content.contentExistForContext(localContext)) { break; } } } contentElementList = new ContentElementList(componentBean, localContext, this); contentElementListMap.put(ctx.getRequestContentLanguage(), contentElementList); }
	 * 
	 * return contentElementList; }
	 */

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	public IImageTitle getImage(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		IImageTitle res = null;
		if (desc.imageLink != null) {
			res = desc.imageLink;
		}
		if (res != null) {
			return res;
		}
		IContentComponentsList contentList = getAllContent(ctx);
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent elem = contentList.next(ctx);
			if ((elem instanceof IImageTitle) && (!elem.isEmpty(ctx)) && (!elem.isRepeat())) {
				IImageTitle imageComp = (IImageTitle) elem;
				if (imageComp.isImageValid(ctx)) {
					res = imageComp;
					// desc.imageLink = new WeakReference<IImageTitle>(res);
					desc.imageLink = new ImageTitleBean(res.getImageDescription(ctx), res.getResourceURL(ctx), res.getImageLinkURL(ctx));
					return res;
				}
			}
		}
		return null;
	}

	public Collection<IImageTitle> getImages(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		Collection<IImageTitle> res = null;
		if (desc.images != null) {
			res = desc.images;
		}
		if (res != null) {
			return res;
		}
		res = new LinkedList<IImageTitle>();
		IContentComponentsList contentList = getAllContent(ctx);
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent elem = contentList.next(ctx);
			if ((elem instanceof IImageTitle) && (!elem.isEmpty(ctx)) && (!elem.isRepeat())) {
				IImageTitle imageComp = (IImageTitle) elem;
				if (imageComp.isImageValid(ctx)) {
					res.add(new ImageTitleBean(imageComp.getImageDescription(ctx), imageComp.getResourceURL(ctx), imageComp.getImageLinkURL(ctx)));
				}
			}
		}
		desc.images = res;
		return desc.images;
	}

	public Collection<Link> getStaticResources(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		Collection<Link> res = null;
		if (desc.staticResources != null) {
			res = desc.staticResources;
		}
		if (res != null) {
			return res;
		}
		res = new LinkedList<Link>();
		IContentComponentsList contentList = getAllContent(ctx);
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent elem = contentList.next(ctx);
			if (!(elem instanceof IImageTitle) && (elem instanceof IStaticContainer) && (!elem.isEmpty(ctx)) && (!elem.isRepeat())) {
				IStaticContainer resourcesContainer = (IStaticContainer) elem;
				res.addAll(resourcesContainer.getAllResourcesLinks(ctx));
			}
		}
		desc.staticResources = res;
		return desc.staticResources;
	}

	public String getKeywords(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.keywords != null) {
			return desc.keywords;
		}
		String res = "";
		replacement.clear();
		IContentComponentsList contentList = getAllContent(ctx);
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent elem = contentList.next(ctx);
			if (elem.getType().equals(Keywords.TYPE)) {
				if (res.length() > 0) {
					res = res + ',';
				}
				res = res + elem.getValue(ctx);
				Keywords keywords = (Keywords) elem;
				if (keywords.getStyle(ctx).equals(Keywords.BOLD_IN_CONTENT)) {
					String[] keys = keywords.getValue().split(",");
					for (String key : keys) {
						replacement.put(key.trim(), "<strong>" + key.trim() + "</strong>");
					}
				}
			}
			List<String> tags = getTags(ctx);
			for (String tag : tags) {
				if (res.length() > 0) {
					res = res + ',';
				}
				res = res + tag;
			}
		}
		desc.keywords = res;
		return desc.keywords;
	}

	public String getLabel(ContentContext ctx) throws Exception {
		String res = getFullLabel(ctx);

		if (res != null) {
			if ((res.trim().length() == 0) && (name != null)) {
				res = name;
			}
		}
		return res;
	}

	public int getLastAccess(ContentContext ctx) throws ServiceException {
		Calendar cal = Calendar.getInstance();
		int countDay = 0;
		int outAccess = 0;
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		while (countDay < staticConfig.getLastAccessPage()) {
			outAccess = outAccess + getAccess(ctx, cal.getTime());
			cal.roll(Calendar.DAY_OF_YEAR, false);
			countDay++;
		}
		return outAccess;
	}

	public String getLatestEditor() {
		return latestEditor;
	}

	public String getLinkedURL() {
		return linkedURL;
	}

	/**
	 * get the first link on the page.
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String getLinkOn(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.linkOn == null) {
			String res = "";
			ContentContext newCtx = new ContentContext(ctx);
			newCtx.setArea(null);
			IContentComponentsList contentList = getAllContent(newCtx);
			while (contentList.hasNext(newCtx)) {
				IContentVisualComponent elem = contentList.next(newCtx);
				if (elem instanceof ILink && !elem.isRepeat()) {
					res = ((ILink) elem).getURL(newCtx);
				}
			}
			desc.linkOn = res;
		}

		return desc.linkOn;
	}

	/**
	 * get content of the current area
	 * 
	 * @param ctx
	 *            current context
	 * @return
	 * @throws Exception
	 */
	private synchronized ContentElementList getLocalContent(ContentContext ctx) throws Exception {
		ContentElementList localContentElementList = contentElementListMap.get(ctx.getRequestContentLanguage());
		if (localContentElementList == null) {

			/*
			 * ComponentBean[] localComponentBean; synchronized (componentBean) { localComponentBean = new ComponentBean[componentBean.length]; System.arraycopy(componentBean, 0, localComponentBean, 0, componentBean.length); }
			 */
			localContentElementList = new ContentElementList(componentBean, ctx, this, false);

			if (!ctx.isFree()) { // no reference to template >>> some component can be absent
				contentElementListMap.put(ctx.getRequestContentLanguage(), localContentElementList);
			}

			logger.fine("update local content  - # component : " + localContentElementList.size(ctx) + " (ctx:" + ctx + ")");
		}

		localContentElementList.initialize(ctx);

		return localContentElementList;

	}

	public ContentElementList getLocalContentCopy(ContentContext ctx) throws Exception {
		ContentElementList outList = new ContentElementList(getLocalContent(ctx));
		outList.initialize(ctx);
		return outList;
	}

	/**
	 * get description of the page (description component)
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String getLocation(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.location != null) {
			return desc.location;
		}
		String res = "";
		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setArea(null);
		IContentComponentsList contentList = getContent(newCtx);
		while (contentList.hasNext(newCtx)) {
			IContentVisualComponent elem = contentList.next(newCtx);
			if (elem.getType().equals(LocationComponent.TYPE)) {
				res = res + elem.getValue(newCtx);
			}
		}
		desc.location = StringUtils.replace(res, "\"", "&quot;");
		return desc.location;
	}

	/**
	 * return the object for lock the navigation structure
	 * 
	 * @return
	 */
	public Object getLock() {
		return getRoot();
	}

	public Date getManualModificationDate() {
		return manualModificationDate;
	}

	/**
	 * get the description for meta tag (if no meta description defined return the description)
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String getMetaDescription(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.metaDescription != null) {
			return desc.metaDescription;
		}
		String res = "";
		IContentComponentsList contentList = getAllContent(ctx);
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent elem = contentList.next(ctx);
			if (elem.getType().equals(MetaDescription.TYPE)) {
				res = res + elem.getValue(ctx);
			}
		}
		if (res.trim().length() == 0) {
			res = getDescription(ctx);
		}
		desc.metaDescription = StringUtils.replace(res, "\"", "&quot;");
		return desc.metaDescription;
	}

	public Date getModificationDate() {
		if (getManualModificationDate() != null) {
			return getManualModificationDate();
		} else {
			return getRealModificationDate();
		}
	}

	public String getName() {
		return name;
	}

	/**
	 * get the previous menu element in the child list
	 * 
	 * @return a MenuElement with the same depth, null if current is the first element.
	 */
	public MenuElement getNextBrother() {
		if (getParent() == null) {
			return null;
		}
		Collection<MenuElement> children = getParent().getChildMenuElements();
		MenuElement elem = null;
		for (MenuElement child : children) {
			if (elem != null && elem.equals(this)) {
				return child;
			}
			elem = child;
		}
		return null;
	}

	public MenuElement getNoErrorFreeCurrentPage(ContentContext ctx) throws Exception {
		if (ctx.getPath().equals("/")) {
			return this;
		} else {

			Collection<MenuElement> pastNode = new LinkedList<MenuElement>();
			MenuElement elem = searchChild(this, ctx, ctx.getPath(), pastNode);
			if (elem == null) {
				elem = this;
			}
			// DebugHelper.checkAssert(elem == null, "current page (" +
			// ctx.getPath() + ") not found.");
			return elem;
		}
	}

	public List<IContentVisualComponent> getNoRepeatContentByType(ContentContext ctx, String type) throws Exception {

		List<IContentVisualComponent> outComp = new LinkedList<IContentVisualComponent>();

		ContentElementList content = getAllLocalContent(ctx);

		while (content.hasNext(ctx)) {
			IContentVisualComponent comp = content.next(ctx);
			if (comp.getType().equals(type)) {
				outComp.add(comp);
			}
		}
		content.initialize(ctx);

		return outComp;
	}

	protected ICache getCache(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ICache cache = globalContext.getCache("navigation");
		if (releaseCache) {
			cache.removeAll();
			releaseCache = false;
		}
		return cache;
	}

	PageDescription getPageDescriptionCached(ContentContext ctx, String lg) {
		String key = getCacheKey(lg);
		PageDescription outDesc = (PageDescription) getCache(ctx).get(key);
		if (outDesc == null) {
			outDesc = new PageDescription();
			getCache(ctx).put(key, outDesc);
		}
		return outDesc;
	}

	PageDescription getPageBeanCached(ContentContext ctx, String lg) {
		String key = getCacheKey("bean-" + lg);
		PageDescription outDesc = (PageDescription) getCache(ctx).get(key);
		if (outDesc == null) {
			outDesc = new PageDescription();
			getCache(ctx).put(key, outDesc);
		}
		return outDesc;
	}

	public void clearPageBean(ContentContext ctx) {
		String requestKey = getPath() + '_' + ctx.getRequestContentLanguage() + "_" + ctx.getLanguage();
		ctx.getRequest().removeAttribute(requestKey);
	}

	public PageBean getPageBean(ContentContext ctx) throws Exception {
		String requestKey = getPath() + '_' + ctx.getRequestContentLanguage() + "_" + ctx.getLanguage();
		PageBean pageBean = (PageBean) ctx.getRequest().getAttribute(requestKey);
		if (pageBean == null) {
			pageBean = new PageBean(ctx, this);
			ctx.getRequest().setAttribute(requestKey, pageBean);
		}
		return pageBean;
	}

	/**
	 * get the page rank (define with content)
	 * 
	 * @return a page rank between 0 and 1
	 * @throws Exception
	 */
	public double getPageRank(ContentContext ctx) throws Exception {

		final double defaultValue = 0;

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.pageRank != null) {
			return desc.pageRank;
		}

		ContentElementList contentList = getAllContent(ctx); // search date in
		// all area
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent comp = contentList.next(ctx);
			if (comp instanceof IPageRank) {
				if (((IPageRank) comp).getVotes(ctx, getPath()) == 0) {
					return defaultValue;
				}
				if (((IPageRank) comp).getVotes(ctx, getPath()) > 0) {
					desc.pageRank = (((IPageRank) comp).getRankValue(ctx, getPath())) / (double) (((IPageRank) comp).getVotes(ctx, getPath()));
					return desc.pageRank;
				}
			}
		}
		desc.pageRank = defaultValue;
		return desc.pageRank;
	}

	/**
	 * get the title of the page define in the content, empty string if no title defined.
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String getForcedPageTitle(ContentContext ctx) throws Exception {
		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setArea(null); // warning : check if the method is needed.

		PageDescription desc = getPageDescriptionCached(ctx, newCtx.getRequestContentLanguage());

		if (desc.forcedPageTitle != null) {
			return desc.forcedPageTitle;
		}

		desc.forcedPageTitle = getContent(newCtx).getPageTitle();
		if (desc.forcedPageTitle == null) {
			desc.forcedPageTitle = "";
		}

		return desc.pageTitle;
	}

	public String getPageTitle(ContentContext ctx) throws Exception {

		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setArea(null); // warning : check if the method is needed.
		newCtx.setFree(true);

		PageDescription desc = getPageDescriptionCached(ctx, newCtx.getRequestContentLanguage());

		if (desc.pageTitle != null) {
			return desc.pageTitle;
		}

		desc.pageTitle = getForcedPageTitle(newCtx);

		if (desc.pageTitle == null || desc.pageTitle.trim().length() == 0) {
			desc.pageTitle = getLabel(newCtx);
		}
		return desc.pageTitle;
	}

	/**
	 * @return Returns the parent.
	 */
	public MenuElement getParent() {
		return parent;
	}

	/**
	 * return the page of this page
	 * 
	 * @return a path.
	 */
	public String getPath() {
		try {
			if (parent == this) {
				throw new Exception("recursive reference !!!");
			} else {
				if (parent != null) {
					return parent.getPath() + '/' + getName();
				} else {
					return "";
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * get the previous menu element in the child list
	 * 
	 * @return a MenuElement with the same depth, null if current is the first element.
	 */
	public MenuElement getPreviousBrother() {
		if (getParent() == null) {
			return null;
		}
		Collection<MenuElement> children = getParent().getChildMenuElements();
		MenuElement brother = null;
		for (MenuElement child : children) {
			if (child.equals(this)) {
				return brother;
			}
			brother = child;
		}
		return null;
	}

	public int getPriority() {
		return priority;
	}

	public Date getRealModificationDate() {
		return modificationDate;
	}

	public Map<String, String> getReplacement() {
		return replacement;
	}

	public Collection<StaticInfo> getResources(ContentContext ctx) throws Exception {
		Collection<StaticInfo> pageResources = new LinkedList<StaticInfo>();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<StaticInfo> resources = globalContext.getResources(ctx);
		for (StaticInfo staticInfo : resources) {
			if (staticInfo.getLinkedPage(ctx) != null) {
				if (this.getPath().equals(staticInfo.getLinkedPage(ctx).getPath())) {
					pageResources.add(staticInfo);
				}
			}
		}
		return pageResources;
	}

	public String getReversedLink() {
		return reversedLink;
	}

	public MenuElement getRoot() {
		MenuElement rootNode = this;
		MenuElement parent = getParent();
		while (parent != null) {
			rootNode = parent;
			parent = parent.getParent();
		}
		return rootNode;
	}

	/**
	 * return the depth of the selection. sample: if the first selected element have children and sedond not the depth is 2.
	 * 
	 * @return a depth
	 * @throws Exception
	 */
	public int getSelectedDepth(ContentContext ctx) throws Exception {
		MenuElement elem = searchChild(ctx);
		int res = ContentManager.getPathDepth(ctx.getPath());
		if (elem.isHaveChild()) {
			res++;
		}
		return res;
	}

	public double getSiteMapPriority(ContentContext ctx) {
		try {
			double pageRank = getPageRank(ctx);
			if (pageRank == 0) {
				return 1 / 2;
			} else {
				return pageRank;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 1 / 2;
		}
	}

	public String getSubTitle(ContentContext ctx) throws Exception {

		ContentContext newCtx = new ContentContext(ctx);

		PageDescription desc = getPageDescriptionCached(ctx, newCtx.getRequestContentLanguage());

		if (desc.subTitle != null) {
			return desc.subTitle;
		}

		newCtx.setDefaultArea();
		desc.subTitle = getContent(newCtx).getSubTitle(ctx);
		if (desc.subTitle == null) {
			newCtx.setArea(null); // warning : check if the method is needed.
			desc.subTitle = getContent(newCtx).getSubTitle(ctx);
		}

		return desc.subTitle;
	}

	public List<String> getTags(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.tags != null) {
			return desc.tags;
		}

		ContentContext lgDefaultCtx = new ContentContext(ctx);
		lgDefaultCtx.setArea(null);

		if (lgDefaultCtx.getRenderMode() == ContentContext.EDIT_MODE) {
			lgDefaultCtx.setRenderMode(ContentContext.PREVIEW_MODE); // get info for preview mode (with repeat elements)
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Iterator<String> defaultLg = globalContext.getDefaultLanguages().iterator();

		Iterator<IContentVisualComponent> tagsList = getContentByType(lgDefaultCtx, Tags.TYPE).iterator();
		Tags tag = null;
		if (tagsList.hasNext()) {
			tag = (Tags) tagsList.next();
		}
		while ((tag == null || tag.getTags().size() == 0) && defaultLg.hasNext()) {
			String lg = defaultLg.next();
			lgDefaultCtx.setContentLanguage(lg);
			lgDefaultCtx.setRequestContentLanguage(lg);
			tagsList = getContentByType(lgDefaultCtx, Tags.TYPE).iterator();
			tag = null;
			if (tagsList.hasNext()) {
				tag = (Tags) tagsList.next();
			}
		}

		List<String> outTags = new LinkedList<String>();

		IContentComponentsList contentList = getAllContent(lgDefaultCtx);
		while (contentList.hasNext(lgDefaultCtx)) {
			IContentVisualComponent elem = contentList.next(lgDefaultCtx);
			if (elem.getType().equals(Tags.TYPE)) {
				outTags.addAll(((Tags) elem).getTags());
			}
		}
		desc.tags = outTags;
		return desc.tags;
	}

	public String getTemplateId() {
		return templateId;
	}

	/**
	 * get the time range found in the content.
	 * 
	 * @return
	 * @throws Exception
	 */
	public TimeRange getTimeRange(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.timeRange != null) {
			return desc.timeRange;
		}

		List<IContentVisualComponent> content = getContentByType(ctx, TimeRangeComponent.TYPE);
		if (content.size() > 0) {
			TimeRangeComponent comp = (TimeRangeComponent) content.iterator().next();
			desc.timeRange = new TimeRange(comp.getStartDate(), comp.getEndDate());
		} else {
			Date contentDate = getContentDateNeverNull(ctx);
			desc.timeRange = new TimeRange(contentDate, contentDate);
		}

		return desc.timeRange;
	}

	public String getTitle(ContentContext ctx) throws Exception {

		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setArea(null); // warning : check if the method is needed.

		PageDescription desc = getPageDescriptionCached(ctx, newCtx.getRequestContentLanguage());

		if (desc.title != null) {
			return desc.title;
		}

		desc.title = getContent(newCtx).getTitle();

		if (desc.title != null) {
			if ((desc.title.trim().length() == 0) && (name != null)) {
				desc.title = name;
			}
		}
		return desc.title;
	}

	/**
	 * get title withtout repeat content
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String getLocalTitle(ContentContext ctx) throws Exception {

		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setArea(null); // warning : check if the method is needed.
		newCtx.setFree(true);

		PageDescription desc = getPageDescriptionCached(ctx, newCtx.getRequestContentLanguage());

		if (desc.localTitle != null) {
			return desc.localTitle;
		}

		desc.localTitle = getContent(newCtx).getLocalTitle();

		if (desc.localTitle != null) {
			if ((desc.localTitle.trim().length() == 0) && (name != null)) {
				desc.localTitle = name;
			}
		}
		return desc.localTitle;
	}

	/**
	 * @return
	 */
	public Set<String> getUserRoles() {
		return userRoles;
	}

	public String getValidater() {
		return validater;
	}

	public Date getValidationDate() {
		return validationDate;
	}

	public List<MenuElement> getVirtualChild(ContentContext ctx, boolean onlyVisible) throws Exception {
		List<MenuElement> outList = new LinkedList<MenuElement>();
		if (onlyVisible) {
			for (MenuElement menuElement : virtualChild) {
				if (menuElement.isVisible(ctx)) {
					outList.add(menuElement);
				}
			}
		} else {
			outList.addAll(virtualChild);
		}

		return outList;
	}

	public List<MenuElement> getVirtualParent() {
		return virtualParent;
	}

	/**
	 * return the page of this page
	 * 
	 * @return a path.
	 */
	public String getVirtualPath(ContentContext ctx) {
		return getVirtualPathRec(ctx, 0);
	}

	/**
	 * return the page of this page
	 * 
	 * @return a path.
	 */
	private String getVirtualPathRec(ContentContext ctx, int c) {
		try {
			if (parent == this && c > 1000) {
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
				logger.severe("recursive vpath in :" + getPath() + "  context : " + globalContext.getContextKey());
			} else {

				MenuElement realParent = parent;
				/*
				 * if (!realParent.isSelected(ctx)) { for (MenuElement vparent : getVirtualParent()) { if (vparent.isSelected(ctx)) { realParent = vparent; } } }
				 */// TODO : warning this comment must be bad, but with it you have a recursive call to isSelected

				if (realParent != null) {
					return realParent.getVirtualPathRec(ctx, c + 1) + '/' + getName();
				} else {
					if (parent == null) {
						return "";
					} else {
						return getName();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * get the child list of the current element.
	 * 
	 * @return a array of children.
	 * @throws Exception
	 */
	public List<MenuElement> getVisibleChildMenuElements(ContentContext ctx) throws Exception {
		ArrayList<MenuElement> resObj = new ArrayList<MenuElement>();
		for (MenuElement element : childMenuElements) {
			if (element.isVisible(ctx)) {
				resObj.add(element);
			} else {
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
				if (globalContext.isAutoSwitchToDefaultLanguage() && ctx.getRenderMode() != ContentContext.EDIT_MODE) {
					ContentContext defaultLgCtx = new ContentContext(ctx);
					defaultLgCtx.setArea(null);
					Iterator<String> defaultLgs = globalContext.getDefaultLanguages().iterator();
					boolean insered = false;
					while (defaultLgs.hasNext() && !insered) {
						String lg = defaultLgs.next();
						defaultLgCtx.setRequestContentLanguage(lg);
						if (element.isVisible(defaultLgCtx)) {
							resObj.add(element);
							insered = true;
						}
					}
				}
			}
		}
		return resObj;
	}

	public List<MenuElement> getVisibleChildMenuElementsList(ContentContext ctx) throws Exception {
		List<MenuElement> res = new LinkedList<MenuElement>();
		for (MenuElement element : childMenuElements) {
			if (element.isVisible(ctx)) {
				res.add(element);
			} else {
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
				if (globalContext.isAutoSwitchToDefaultLanguage() && ctx.getRenderMode() != ContentContext.EDIT_MODE) {
					ContentContext defaultLgCtx = new ContentContext(ctx);
					/*
					 * defaultLgCtx.setContentLanguage(globalContext.getDefaultLanguage ()); if (element.isVisible(defaultLgCtx)) { res.add(element); }
					 */

					Iterator<String> defaultLgs = globalContext.getDefaultLanguages().iterator();
					boolean insered = false;
					while (defaultLgs.hasNext() && !insered) {
						String lg = defaultLgs.next();
						defaultLgCtx.setRequestContentLanguage(lg);
						if (element.isVisible(defaultLgCtx)) {
							res.add(element);
							insered = true;
						}
					}
				}
			}
		}
		return res;
	}

	public String getXHTMLTitle(ContentContext ctx) throws Exception {
		String res = getContent(ctx).getXHTMLTitle(ctx);

		if (res != null) {
			if (res.trim().length() == 0) {
				res = name;
			}
		}
		return StringHelper.removeTag(res);
	}

	public boolean isBlocked() {
		return blocked;
	}

	/**
	 * check if a page is parent or parent of a parent.
	 * 
	 * @param parent
	 *            a page of the navigation.
	 * @return true if page found in paternity
	 */
	public boolean isChildOf(MenuElement parent) {
		if (getParent() == null) {
			return false;
		} else if (getParent().equals(parent)) {
			return true;
		} else {
			return getParent().isChildOf(parent);
		}
	}

	public boolean isChildrenEquals(MenuElement elem) {
		if (elem == null) {
			return false;
		}
		return getChildMenuElements().equals(elem.getChildMenuElements());
	}

	// TODO: change this method with a method in the component, it return is date if visible of not.
	public boolean isContentDateVisible(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.contentDateVisible != null) {
			return desc.contentDateVisible;
		}
		desc.contentDateVisible = true;
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Iterator<String> defaultLgs = globalContext.getDefaultLanguages().iterator();
		ContentContext localContext = new ContentContext(ctx);
		while (isEmpty(localContext) && defaultLgs.hasNext()) {
			localContext.setRequestContentLanguage(defaultLgs.next());
		}
		if (!isEmpty(localContext)) {
			ContentElementList contentList = getAllContent(localContext); // search date in all area
			while (contentList.hasNext(ctx)) {
				IContentVisualComponent comp = contentList.next(ctx);
				if (comp.getType() == DateComponent.TYPE) {
					if (((DateComponent) comp).getStyle(ctx).equals(DateComponent.NOT_VISIBLE_TYPE)) {
						desc.contentDateVisible = false;
					}
				}
			}
		}
		return desc.contentDateVisible;
	}

	public boolean isContentEquals(MenuElement elem) {
		if (elem == null) {
			return false;
		}
		return Arrays.equals(elem.componentBean, componentBean);
	}

	public boolean isEmpty(ContentContext ctx) throws Exception {
		if (!isInsideTimeRange()) {
			return false;
		}
		return isEmpty(ctx, null);
	}

	public boolean isEmpty(ContentContext ctx, String area) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.isEmpty() != null) {
			return desc.isEmpty();
		}

		if (ctx.getRequestContentLanguage() == null) {
			return true;
		}
		ContentContext ctxNoArea = new ContentContext(ctx);
		ctxNoArea.setArea(null);
		Logger.startCount("local content");
		IContentComponentsList contentList = getAllLocalContent(ctxNoArea);
		Logger.stepCount("local content", "local content loaded");
		while ((contentList.hasNext(ctxNoArea))) {
			IContentVisualComponent component = contentList.next(ctxNoArea);
			if (component != null) {
				if (!component.isEmpty(ctxNoArea)) {
					if (!component.isRepeat()) {
						desc.empty = false;
						return false;
					}
				}
			}
		}
		Logger.endCount("local content", "content checked");
		desc.empty = true;
		return true;
	}

	/**
	 * @return
	 */
	public boolean isHaveChild() {
		return childMenuElements.size() > 0;
	}

	public boolean isHaveVisibleChild(ContentContext ctx) throws Exception {
		return getVisibleChildMenuElements(ctx).size() > 0;
	}

	public boolean isHttps() {
		return https;
	}

	public boolean isLastSelected(ContentContext ctx) {
		String[] pathElems = ctx.getPath().split("\\/");
		if (pathElems.length > 0) {
			if (name.equals(pathElems[pathElems.length - 1])) {
				return true;
			}
		}
		return false;
	}

	public boolean isMetadataEquals(MenuElement elem) {
		if (elem == null) {
			return false;
		}
		return name.equals(elem.getName()) && visible == elem.visible && userRoles.equals(elem.userRoles) && priority == elem.priority;
	}

	public boolean isRealContent(ContentContext ctx) throws Exception {

		if (!isInsideTimeRange()) {
			return false;
		}

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (!desc.isRealContentNull()) {
			return desc.isRealContent();
		}

		ContentContext contentAreaCtx = new ContentContext(ctx);
		contentAreaCtx.setArea(ComponentBean.DEFAULT_AREA);

		/*
		 * if (isEmpty(contentAreaCtx)) { return false; }
		 */

		ContentElementList comps = getContent(contentAreaCtx);
		while (comps.hasNext(contentAreaCtx)) {
			IContentVisualComponent comp = comps.next(contentAreaCtx);
			if (comp.isRealContent(contentAreaCtx) && !comp.isRepeat()) {
				desc.realContent = true;
				return true;
			}
		}

		desc.realContent = false;

		return false;
	}

	public boolean isRealContentAnyLanguage(ContentContext ctx) throws Exception {
		if (isRealContent(ctx)) {
			return true;
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> lgs = globalContext.getContentLanguages();
		ContentContext lgContext = new ContentContext(ctx);
		lgContext.setArea(null); // remove the area
		for (String lg : lgs) {
			lgContext.setRequestContentLanguage(lg);
			if (isRealContent(lgContext)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * public boolean isSelected(ContentContext ctx) { String[] pathElems = ctx.getPath().split("\\/"); for (String pathElem : pathElems) { if (name.equals(pathElem)) { return true; } } return false; }
	 */

	public boolean isRemote() {
		if (getParent() != null && getParent().isRemote()) {
			return true;
		} else {
			return remote;
		}
	}

	public boolean isSelected(ContentContext ctx) throws Exception {
		MenuElement page = ctx.getCurrentPage();
		if (page == null) {
			return false;
		}
		if (this.getId().equals(page.getId())) {
			return true;
		}
		while (!this.getId().equals(page.getId()) && page.getParent() != null) {
			page = page.getParent();
			if (this.getId().equals(page.getId())) {
				return true;
			}
		}
		return false;
	}

	public boolean isValid() {
		return valid;
	}

	public boolean isVisible() throws Exception {
		return visible;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public boolean isVisible(ContentContext ctx) throws Exception {
		if (!visible) {
			return false;
		} else {
			ContentContext contentAreaCtx = new ContentContext(ctx);
			contentAreaCtx.setArea(ComponentBean.DEFAULT_AREA);
			ContentElementList content = this.getContent(contentAreaCtx);
			while (content.hasNext(contentAreaCtx)) {
				IContentVisualComponent comp = content.next(contentAreaCtx);
				if (!comp.isEmpty(contentAreaCtx)) {
					return isInsideTimeRange();
				}
			}

			MenuElement[] children = this.getAllChildren();
			for (MenuElement child : children) {
				content = child.getContent(ctx);
				while (content.hasNext(contentAreaCtx)) {
					if (!content.next(contentAreaCtx).isEmpty(contentAreaCtx) /* && child.isVisible() */) { // TODO:
						return isInsideTimeRange();
					}
				}
			}
			return false;
		}
	}

	/**
	 * move the current page under other parent
	 */
	public void moveToParent(MenuElement parent) {
		if (parent != null) {
			if (getParent() != null) {
				getParent().removeChild(this);
			}
			parent.addChildMenuElement(this);
			setParent(parent);
		}
	}

	/**
	 * prepare a content to be added at the end of the rendering
	 * 
	 * @param ctx
	 *            the current content context
	 * @param page
	 *            the page when the content must be insered
	 * @param parentComp
	 *            the parent component
	 * @param contentType
	 *            the type of the component
	 * @param value
	 *            the value of the component
	 * @return the if of the new component
	 * @throws Exception
	 */
	public final String prepareAddContent(String lg, String parentCompId, String contentType, String style, String value) throws Exception {
		ComponentBean comp = new ComponentBean(StringHelper.getRandomId(), contentType, value, lg, false);
		if (style != null) {
			comp.setStyle(style);
		}
		if (contentToBeAdded.keySet().contains(parentCompId)) {
			logger.warning("parent id : '" + parentCompId + "' all ready found is parent id list.");
		} else {
			contentToBeAdded.put(parentCompId, comp);
		}
		return comp.getId();
	}

	public void releaseCache() {
		// pageInfinityCache.clear();
		releaseCache = true;
		contentElementListMap.clear();
		localContentElementListMap.clear();
	}

	public void removeChild(MenuElement elem) {
		synchronized (getLock()) {
			childMenuElements.remove(elem);
		}
	}

	/**
	 * remove a component on the page
	 * 
	 * @return the type of the component, null if not found
	 */
	public String removeContent(ContentContext ctx, String id) {
		return removeContent(ctx, id, true);
	}

	/**
	 * remove a component on the page
	 * 
	 * @param releaseCache
	 *            true if you want release the page cache after deleted component
	 * @return the type of the component, null if not found
	 */
	public String removeContent(ContentContext ctx, String id, boolean releaseCache) {
		String type = null;
		synchronized (getLock()) {
			List<ComponentBean> outList = new LinkedList<ComponentBean>();
			boolean delete = false;
			for (int i = 0; i < componentBean.length; i++) {
				IContentVisualComponent comp = ComponentFactory.getComponentWithType(ctx, componentBean[i].getType());
				if (!componentBean[i].getId().equals(id)) {
					if (!delete) {
						outList.add(componentBean[i]);
					}
				} else {
					if (comp != null) {
						// added by plm for portlet delete purpose
						((AbstractVisualComponent) comp).setComponentBean(componentBean[i]);

						comp.delete(ctx);
						type = comp.getType();
					}
				}
			}
			// if (type != null) {
			componentBean = new ComponentBean[outList.size()];
			outList.toArray(componentBean);
			// }
		}
		if (releaseCache) {
			releaseCache();
		}

		return type;
	}

	public void removeEditorRoles(String group) {
		editGroups.remove(group);
	}

	private void removeVirtualChild(MenuElement vChild) {
		if (vChild != null) {
			virtualChild.remove(vChild);
		}
	}

	/*
	 * public boolean needJavaScript(ContentContext ctx) throws Exception { PageDescription desc = getPageDescriptionCached(ctx,ctx.getRequestContentLanguage()); if (desc.needJavaScript != null) { return desc.needJavaScript; }
	 * 
	 * ContentElementList content = getAllContent(ctx); while (content.hasNext()) { IContentVisualComponent comp = content.next(); if (comp.needJavaScript()) { desc.needJavaScript = true; return true; } } desc.needJavaScript = false; return false; }
	 */

	public MenuElement searchChild(ContentContext ctx) throws Exception {
		return searchChild(ctx, ctx.getPath());
	}

	public MenuElement searchChild(ContentContext ctx, String path) throws Exception {
		/*
		 * if (path.equals("/")) { return this; } else { Collection<MenuElement> pastNode = new LinkedList<MenuElement>(); return searchChild(this, ctx, path, pastNode); }
		 */
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		return globalContext.getPage(ctx, path);
	}

	/**
	 * @param id
	 * @return
	 * @Deprecated use NavigationService.getPage(ContentContext, pageKey)
	 */
	public MenuElement searchChildFromId(String id) {
		if ((id == null) || (id.equals("0"))) {
			return this;
		} else {
			try {
				for (MenuElement child : getAllChildren()) {
					if (child.getId().equals(id)) {
						return child;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @param name
	 * @return
	 * @Deprecated use NavigationService.getPage(ContentContext, pageKey)
	 */
	public MenuElement searchChildFromName(String name) {
		if (name.equals(this.getName())) {
			return this;
		} else {
			return searchChildFromName(this, name);
		}
	}

	public MenuElement searchRealChild(ContentContext ctx, String path) throws Exception {
		if (path.equals("/")) {
			return this;
		} else {
			Collection<MenuElement> pastNode = new LinkedList<MenuElement>();
			return searchRealChild(this, ctx, path, pastNode);
		}
	}

	public void setBlocked(boolean blocked) {
		synchronized (getLock()) {
			this.blocked = blocked;
		}
	}

	public void setBlocker(String blocker) {
		synchronized (getLock()) {
			this.blocker = blocker;
		}
	}

	public void setChildMenuElements(Collection<MenuElement> childMenuElements) {
		synchronized (getLock()) {
			this.childMenuElements = new LinkedList<MenuElement>();
		}
	}

	public void setContent(ComponentBean[] newContent) {
		synchronized (getLock()) {
			componentBean = newContent;
			releaseCache();
		}
	}

	public void setContentStayCache(ComponentBean[] newContent) {
		synchronized (getLock()) {
			componentBean = newContent;
		}
	}

	public void setCreationDate(Date createDate) {
		creationDate = createDate;
	}

	public void setCreator(String creator) {
		synchronized (getLock()) {
			this.creator = creator;
		}
	}

	public void setHttps(boolean https) {
		this.https = https;
	}

	/**
	 * @param string
	 */
	public void setId(String string) {
		id = string;
	}

	public void setLatestEditor(String latestEditor) {
		synchronized (getLock()) {
			this.latestEditor = latestEditor;
		}
	}

	public void setLinkedURL(String linkedURL) {
		if (this.linkedURL.length() > 0 && linkedURL.trim().length() == 0) {
			clearPage();
		}
		if (linkedURL == null) { // on load XML
			this.linkedURL = "";
			return;
		}
		this.linkedURL = linkedURL;
		if (linkedURL.trim().length() == 0) {
			setRemote(false);
		} else {
			setRemote(true);
		}
	}

	public void setManualModificationDate(Date manualModificationDate) {
		synchronized (getLock()) {
			this.manualModificationDate = manualModificationDate;
		}
	}

	public void setModificationDate(Date modificationDate) {
		synchronized (getLock()) {
			this.modificationDate = modificationDate;
		}
	}

	/*
	 * public int getLastAccess(ContentContext ctx) throws ServiceException { String accessPageKey = "access-page-" + ctx.getLanguage(); Integer lastMonthAccess = (Integer) pageTimeCache.get(accessPageKey); if (lastMonthAccess == null) { synchronized (pageTimeCache) { Tracker tracker = Tracker.getTracker(ctx.getRequest().getSession()); StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession()); int outLastMonthAccess = tracker.getPathCountAccess(staticConfig.getLastAccessPage(), URLHelper.mergePath(ctx.getLanguage(), getPath())); pageTimeCache.put(accessPageKey, new Integer(outLastMonthAccess)); return outLastMonthAccess; } } else { return lastMonthAccess; } }
	 */

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param parent
	 *            The parent to set.
	 */
	public void setParent(MenuElement parent) {
		this.parent = parent;
	}

	public void setPriority(int newPriority) {
		synchronized (getLock()) {
			priority = newPriority;
			releaseCache();
			if (getParent() != null) {
				getParent().sortChild();
			}
		}
	}

	public void setPriorityNoSort(int newPriority) {
		synchronized (getLock()) {
			priority = newPriority;
		}
	}

	public void setRemote(boolean readOnly) {
		remote = readOnly;
	}

	public void setReversedLink(String reversedLink) {
		this.reversedLink = reversedLink;
	}

	public void setTemplateName(String inTemplate) {
		templateId = inTemplate;
	}

	/**
	 * @param strings
	 */
	public void setUserRoles(Set<String> roles) {
		userRoles = roles;
	}

	public void setValid(boolean valid) {
		synchronized (getLock()) {
			this.valid = valid;
		}
	}

	public void setValidater(String validater) {
		synchronized (getLock()) {
			this.validater = validater;
		}
	}

	public void setValidationDate(Date validationDate) {
		synchronized (getLock()) {
			this.validationDate = validationDate;
		}
	}

	public void setVirtualParent(List<MenuElement> newVirtualParent) {
		virtualParent = newVirtualParent;
	}

	/**
	 * @param b
	 */
	public void setVisible(boolean b) {
		releaseCache();
		visible = b;
	}

	private void sortChild() {
		Collections.sort(childMenuElements, new MenuElementPriorityComparator());
	}

	@Override
	public String toString() {
		try {
			return getName() + " [priority : " + getPriority() + ", #children : " + getChildMenuElementsList().size() + ",visible=" + isVisible() + "] " + super.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return getName() + " [priority : " + getPriority() + ", #children : " + getChildMenuElementsList().size() + ",visible=" + e.getMessage() + "] " + super.toString();
		}
	}

	public void updateLinkedData(ContentContext ctx) {
		try {
			if (getLinkedURL().trim().length() == 0) {
				if (isRemote()) {
					if (getParent() != null) {
						getParent().updateLinkedData(ctx);
					}
				}
				return;
			}
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			int msTime = staticConfig.getCacheLinkedPage() * 100;
			Date currentDate = new Date();
			if (latestUpdateLinkedData != null && latestUpdateLinkedData.getTime() + msTime > currentDate.getTime()) {
				return;
			}
			logger.info("update page : " + getPath() + " on : " + getLinkedURL());
			latestUpdateLinkedData = new Date();

			String XMLURL = StringHelper.changeFileExtension(getLinkedURL(), "xml");

			URL url = new URL(XMLURL);

			String xml = NetHelper.readPage(url);

			if (xml != null) {

				try {

					xml = xml.trim();
					if (xml.length() > 5 && xml.substring(0, 5).toLowerCase().startsWith("<?xml")) {

						XMLManipulationHelper.searchAllTag(xml, true);

						InputStream inStr = new ByteArrayInputStream(xml.getBytes());
						NodeXML node = XMLFactory.getFirstNode(inStr);
						inStr.close();

						NodeXML pageNode = node.getChild("page");
						if (pageNode != null) {
							clearPage();
							GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
							PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
							NavigationHelper.importPage(ctx, persistenceService, pageNode, this, ctx.getLanguage(), true);
						}

					} else {
						logger.severe("bad external link (xml don't start with '<?xml') : " + XMLURL + "   page:" + getPath());
					}

				} catch (Exception e) {
					logger.severe(e.getMessage());
					logger.severe("bad external link : " + XMLURL + "   page:" + getPath());
				}

			}

		} catch (Exception e) {
			setRemote(false);
			logger.warning("can not update page : " + getPath() + " with linked url : " + getLinkedURL());
			e.printStackTrace();
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(e.getMessage(), GenericMessage.ERROR));
		}
	}

	/**
	 * valid all children
	 * 
	 * @return the number of valided element
	 * @throws Exception
	 */
	public int validAllChildren() throws Exception {
		MenuElement[] children = getAllChildren();
		int outValided = 0;
		for (int i = 0; i < children.length; i++) {
			if (!children[i].isValid()) {
				children[i].setValid(true);
				outValided++;
			}
		}
		return outValided;
	}

	public boolean notInSearch(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.notInSearch != null) {
			return desc.notInSearch;
		}

		desc.notInSearch = false;
		ContentContext noAreaCtx = new ContentContext(ctx);
		noAreaCtx.setArea(null);
		if (getContentByType(noAreaCtx, NotSearchPage.TYPE).size() > 0) {
			desc.notInSearch = true;
		}

		return desc.notInSearch;
	}

	public String getReferenceLanguage() {
		return referenceLanguage;
	}

	public void setReferenceLanguage(String referenceLanguage) {
		this.referenceLanguage = referenceLanguage;
	}

	public boolean isBreakRepeat() {
		return breakRepeat;
	}

	public void setBreakRepeat(boolean breakRepeat) {
		releaseCache();
		this.breakRepeat = breakRepeat;
	}

	public boolean isShortURL() {
		return shortURL != null;
	}

	public String getShortURL() {
		return shortURL;
	}

	public String getShortURL(ContentContext ctx) throws Exception {
		if (shortURL == null) {
			HashSet<String> shortURLs = new HashSet<String>();
			MenuElement root = getRoot();
			if (root.isShortURL()) {
				shortURLs.add(root.getShortURL(ctx));
			}
			MenuElement[] children = root.getAllChildren();
			for (MenuElement child : children) {
				if (child.isShortURL()) {
					shortURLs.add(child.getShortURL(ctx).substring(1));
				}
			}
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			this.shortURL = 'U' + StringHelper.createKey(globalContext.getStaticConfig().getShortURLSize(), shortURLs);
		}
		return shortURL;
	}

	public void setShortURL(String shortURL) {
		this.shortURL = shortURL;
	}

	public Date getStartPublishDate() {
		return startPublishDate;
	}

	public void setStartPublishDate(Date startPublishDate) {
		this.startPublishDate = startPublishDate;
	}

	public Date getEndPublishDate() {
		return endPublishDate;
	}

	public void setEndPublishDate(Date endPublishDate) {
		this.endPublishDate = endPublishDate;
	}

	/**
	 * return true if time range can modify the status of the page in the future.
	 * 
	 * @return
	 */
	public boolean isTimeRange() {
		if (getEndPublishDate() != null) {
			Calendar now = Calendar.getInstance();
			Calendar cal = Calendar.getInstance();
			cal.setTime(getEndPublishDate());
			if (now.after(cal)) {
				return false;
			} else {
				return true;
			}
		} else {
			Calendar now = Calendar.getInstance();
			Calendar cal = Calendar.getInstance();
			cal.setTime(getStartPublishDate());
			if (now.after(cal)) {
				return false;
			} else {
				return true;
			}
		}
	}

	private boolean isInsideTimeRange() {
		if (getStartPublishDate() == null && getEndPublishDate() == null) {
			return true;
		} else {
			boolean inside = true;

			Calendar now = Calendar.getInstance();
			Calendar cal = Calendar.getInstance();

			if (getStartPublishDate() != null) {
				cal.setTime(getStartPublishDate());
				if (now.before(cal)) {
					inside = false;
				}
			}

			if (getEndPublishDate() != null) {
				cal.setTime(getEndPublishDate());
				if (now.after(cal)) {
					inside = false;
				}
			}

			return inside;
		}
	}
}
