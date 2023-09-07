package org.javlo.navigation;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.javlo.actions.IEventRegistration;
import org.javlo.bean.Link;
import org.javlo.cache.ICache;
import org.javlo.cache.MapCache;
import org.javlo.comparator.MenuElementPriorityComparator;
import org.javlo.component.container.IContainer;
import org.javlo.component.core.*;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.component.ecom.IProductContainer;
import org.javlo.component.image.*;
import org.javlo.component.layout.PDFLayoutComponent;
import org.javlo.component.links.ChildrenLink;
import org.javlo.component.links.PageMirrorComponent;
import org.javlo.component.links.PageReferenceComponent;
import org.javlo.component.meta.Font;
import org.javlo.component.meta.*;
import org.javlo.component.title.GroupTitle;
import org.javlo.component.title.WebSiteTitle;
import org.javlo.component.web2.ReactionComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.ContentManager;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.data.rest.IRestItem;
import org.javlo.data.taxonomy.ITaxonomyContainer;
import org.javlo.ecom.Product.ProductBean;
import org.javlo.helper.*;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.EmptyColor;
import org.javlo.image.ExtendedColor;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.content.Edit;
import org.javlo.module.core.IPrintInfo;
import org.javlo.service.ClipBoard;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.event.Event;
import org.javlo.service.exception.ServiceException;
import org.javlo.service.resource.Resource;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.utils.HtmlPart;
import org.javlo.utils.NeverEmptyMap;
import org.javlo.utils.TimeMap;
import org.javlo.utils.TimeRange;
import org.javlo.xml.NodeXML;
import org.javlo.xml.XMLFactory;
import org.javlo.ztatic.IStaticContainer;

import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author pvanderm
 */
public class MenuElement implements Serializable, IPrintInfo, IRestItem, ITaxonomyContainer {

	public static MenuElement NOT_FOUND_PAGE = new MenuElement();

	public static final String NULL_STRING = "NS";

	public static int INSTANCE = 0;

	public static final String PAGE_TYPE_DEFAULT = "default";

	public static final double VOTES_MULTIPLY = 100000;

	private static final long serialVersionUID = 1L;

	public static final int SEO_HEIGHT_INHERITED = -1;
	public static final int SEO_HEIGHT_NULL = 0;
	public static final int SEO_HEIGHT_LOW = 1;
	public static final int SEO_HEIGHT_NORMAL = 2;
	public static final int SEO_HEIGHT_HIGHT = 3;

	public class SmartPageDescription extends PageDescription {

		ContentContext ctx;
		MenuElement page;

		private SmartPageDescription(ContentContext ctx, MenuElement page) {
			this.ctx = ctx;
			this.page = page;
		}

		public String getId() {
			return page.getId();
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

		public boolean isActive() {
			try {
				return page.isActive(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return true;
			}
		}

		public boolean isPageActive() {
			try {
				return page.isPageActive();
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return true;
			}
		}

		@Override
		public Map<String, Boolean> getEmptyArea() {
			Map<String, Boolean> outMaps = new HashMap<String, Boolean>();
			try {
				if (ctx.getCurrentTemplate() != null) {
					for (String area : ctx.getCurrentTemplate().getAreas()) {
						outMaps.put(area, page.isEmpty(ctx, area));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return outMaps;
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

		public boolean isLinkRealContent() {
			try {
				return page.isLinkRealContent(ctx);
			} catch (Exception e) {
				logger.warning(e.getMessage());
				return false;
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

		public Collection<ImageBean> getImagesBean() {
			Collection<ImageBean> outImages = new LinkedList<ImageBean>();
			for (IImageTitle image : getImages()) {
				try {
					ContentContext absCtx = this.ctx.getContextForAbsoluteURL();
					outImages.add(new ImageBean(URLHelper.createResourceURL(absCtx, image.getResourceURL(absCtx)), URLHelper.createTransformURL(absCtx, image.getResourceURL(absCtx), "preview"), image.getImageDescription(absCtx), image.getImageLinkURL(absCtx)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return outImages;
		}

		public ImageBean getImageBean() throws Exception {
			IImageTitle image = page.getImage(ctx);
			if (image == null) {
				return null;
			}
			ContentContext absCtx = this.ctx.getContextForAbsoluteURL();
			return new ImageBean(URLHelper.createResourceURL(absCtx, image.getResourceURL(absCtx)), URLHelper.createTransformURL(absCtx, image.getResourceURL(absCtx), "preview"), image.getImageDescription(absCtx), image.getImageLinkURL(absCtx));
		}

		@Override
		public HtmlPart getDescription() {
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

		public String getMainContentLabel() {
			try {
				return page.getMainContentLabel(ctx);
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

		@Override
		public String getSharedName() {
			return page.getSharedName();
		}

		@Override
		public String getType() {
			return page.getType();
		}

		public boolean isChildrenAssociation() {
			return page.isChildrenAssociation();
		}

		public boolean isChildrenOfAssociation() {
			return page.isChildrenOfAssociation();
		}

		public Collection<PageBean> getPreviousBrothers() {
			LinkedList<PageBean> outBean = new LinkedList<PageBean>();
			MenuElement previous = page.getPreviousBrother();
			while (previous != null) {
				outBean.add(0, new PageBean(ctx, previous));
				previous = previous.getPreviousBrother();
			}
			return outBean;
		}

		public Collection<PageBean> getNextBrothers() {
			Collection<PageBean> outBean = new LinkedList<PageBean>();
			MenuElement next = page.getNextBrother();
			while (next != null) {
				outBean.add(new PageBean(ctx, next));
				next = next.getNextBrother();
			}
			return outBean;
		}

		public String getSmallDate() {
			Date contentDate = getContentDate();
			if (contentDate == null) {
				return null;
			} else {
				return StringHelper.renderDate(contentDate, ctx.getGlobalContext().getShortDateFormat());
			}
		}

		public String getMediumDate() {
			Date contentDate = getContentDate();
			if (contentDate == null) {
				return null;
			} else {
				return StringHelper.renderDate(contentDate, ctx.getGlobalContext().getMediumDateFormat());
			}
		}

		public String getFullDate() {
			Date contentDate = getContentDate();
			if (contentDate == null) {
				return null;
			} else {
				return StringHelper.renderDate(contentDate, ctx.getGlobalContext().getFullDateFormat());
			}
		}

		public String getSlogan() {
			try {
				return page.getSlogan(ctx);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public boolean isEditable() {
			try {
				return page.isEditabled(ctx);
			} catch (Exception e) {
				e.printStackTrace();
				return true;
			}
		}

		public int getSeoWeight() {
			return page.getSeoWeight();
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
			pageDescription.layouts = getLayouts(ctx);
			pageDescription.title = getTitle(ctx);
			pageDescription.localTitle = getLocalTitle(ctx);
			pageDescription.depth = getDepth();
			pageDescription.visible = isVisible();
			pageDescription.breakRepeat = isBreakRepeat();
			pageDescription.referenceLanguage = getReferenceLanguage();
			pageDescription.priority = getPriority();
			pageDescription.childrenCategories = getChildrenCategories(ctx);
			pageDescription.type = getType();
			pageDescription.sharedName = getSharedName();
		}
		return pageDescription;
	}

	public String getMainContentLabel(ContentContext ctx) {
		try {
			ContentContext mainContextLg = new ContentContext(ctx);
			mainContextLg.setRequestContentLanguage(ctx.getLanguage());
			mainContextLg.setContentLanguage(ctx.getLanguage());
			return getLabel(mainContextLg);
		} catch (Exception e) {
			logger.warning(e.getMessage());
			return null;
		}
	}

	public boolean isActive(ContentContext ctx) {
		if (isAdmin() && ctx.getCurrentEditUser() == null) {
			return false;
		}
		return isActive();
	}

	public PageDescription getSmartPageDescription(ContentContext ctx) {
		return new SmartPageDescription(ctx, this);
	}

	public static MenuElement getInstance(ContentContext ctx) {
		MenuElement outMenuElement = new MenuElement();
		outMenuElement.releaseCache = true;
		outMenuElement.lock = ctx.getGlobalContext().getLockLoadContent();
		if (!ctx.isAsViewMode()) {
			outMenuElement.useCache = false;
		}
		return outMenuElement;
	}

	public static MenuElement searchChild(MenuElement elem, ContentContext ctx, String path, Collection<MenuElement> pastNode) throws Exception {
		if (elem == null) {
			return null;
		}
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

	static MenuElement searchChildFromName(MenuElement elem, String... names) {
		List<MenuElement> children = elem.getChildMenuElements();
		for (int i = 0; i < children.size(); i++) {
			for (String name : names) {
				if (children.get(i).getName().equals(name)) {
					return children.get(i);
				}
			}
			MenuElement res = searchChildFromName(children.get(i), names);
			if (res != null) {
				return res;
			}
		}
		return null;
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

	private static final MenuElement NO_PAGE = new MenuElement();

	public static final String LAYOUTS_PREFIX = "layouts-";

	int priority = 10;

	String name = null;

	String nameKey = null;

	boolean childrenAssociation = false;

	// String path = null;
	String id = StringHelper.getRandomId();

	Set<String> userRoles = new HashSet<String>();

	private String templateId;

	private boolean model;

	private boolean admin;

	private String savedParent;

	boolean visible = true;

	boolean active = true;

	String contentLanguage = null;

	private int seoWeight = SEO_HEIGHT_INHERITED;

	List<MenuElement> virtualParent = new LinkedList<MenuElement>();

	// ContentElementList contentElementList = null;

	// ContentElementList localContentElementList = null;

	List<MenuElement> virtualChild = Collections.EMPTY_LIST;

	List<MenuElement> childMenuElements = Collections.EMPTY_LIST;

	/* date and user */

	private ComponentBean[] componentBean = new ComponentBean[0];

	MenuElement parent = null;

	transient Map<String, ContentElementList> contentElementListMap = null;

	transient Map<String, ContentElementList> localContentElementListMap = null;

	private Date creationDate = new Date();

	private String creator = null;

	private Date modificationDate = new Date();

	private Date manualModificationDate = null;

	private String latestEditor = "";

	private boolean valid = false;

	private boolean needValidation = false;

	private boolean noValidation = false;

	private boolean blocked = false;

	private String blocker = "";

	private String validater = "";

	private String reversedLink = "";

	private Date validationDate = null;

	private String linkedURL = "";

	private String sharedName = null;

	private boolean https = false;

	private String referenceLanguage = null;

	private String type = PAGE_TYPE_DEFAULT;

	private int urlNumber = 0;

	private boolean restWidthChildren = false;

	private String ipSecurityErrorPageName = null;

	private Set<String> taxonomy = null;

	/**
	 * protect page localy if there are linked with other website.
	 */
	private boolean remote = false;

	private boolean breakRepeat = false;

	// private final Map<String, PageDescription> pageInfinityCache = new
	// HashMap<String, PageDescription>();

	protected boolean releaseCache = false;

	// private final TimeMap<String, Object> pageTimeCache = new TimeMap<String,
	// Object>(60 * (int) Math.round(((Math.random() + 1) * 60))); // cache
	// between 1u and 2u, all cache can not be updated at the same time

	private Map<String, String> replacement = Collections.EMPTY_MAP;

	private final Collection<String> compToBeDeleted = new LinkedList<String>();

	private Map<String, ComponentBean> contentToBeAdded = Collections.EMPTY_MAP;

	private Set<String> editGroups = new HashSet<String>();

	private Date latestUpdateLinkedData = null;

	public static Logger logger = Logger.getLogger(MenuElement.class.getName());

	private String shortURL = null;

	private Date startPublishDate = null;

	private Date endPublishDate = null;

	private transient ICache localCache = null;

	private MenuElement root = null;

	private Object lock = null;

	private Map<String, MenuElement> pageCache = null;

	private boolean useCache = true;

	protected MenuElement getPageCached(String key) {
		if (!useCache) {
			return null;
		}
		if (pageCache == null) {
			return null;
		} else {
			return pageCache.get(key);
		}
	}

	protected void setPageCached(String key, MenuElement page) {
		if (!useCache) {
			return;
		}
		if (pageCache == null) {
			pageCache = new TimeMap<String, MenuElement>(5 * 60, 2048); // cache 5 minutes
		}
		pageCache.put(key, page);
	}

	protected MenuElement() {
		super();
		INSTANCE++;
	}

	@Override
	protected void finalize() throws Throwable {
		INSTANCE--;
	}

	public void addAccess(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (globalContext.getSpecialConfig().isTrackingAccess()) {
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
			if (menuElement.getUserRoles().size() == 0) {
				menuElement.setUserRoles(getUserRoles());
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
			menuElement.setUserRoles(getUserRoles());
			menuElement.addEditorRoles(getEditorRoles());
			childMenuElements.add(menuElement);
			sortChild();
		}
	}

	public void addChildMenuElementOnTop(MenuElement menuElement) {
		synchronized (getLock()) {
			NavigationHelper.changeStepPriority(getChildMenuElements(), 10);
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
			menuElement.setUserRoles(getUserRoles());
			childMenuElements.add(menuElement);
			sortChild();
		}
	}

	public void addChildMenuElementOnBottom(MenuElement menuElement) {
		menuElement.setPriority(Integer.MAX_VALUE);
		addChildMenuElement(menuElement);
		NavigationHelper.changeStepPriority(getChildMenuElements(), 10);
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
						if (newBean[j].getArea() != null) {
							bean.setArea(newBean[j].getArea());
						}
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

	public void addEditorRole(String group) {
		if (isChildrenOfAssociation()) {
			getRootOfChildrenAssociation().addEditorRole(group);
		} else {
			editGroups.add(group);
		}
	}

	public void addEditorRoles(Collection<String> groups) {
		editGroups.addAll(groups);
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
						// NetHelper.sendMailToAdministrator(ctx,
						// "bad structure in contentToBeAdded : more that one
						// parent id not found",
						// writer.toString());
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
						// NetHelper.sendMailToAdministrator(ctx,
						// "error null bean found.", writer.toString());
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
				if (virtualChild == Collections.EMPTY_LIST) {
					virtualChild = new LinkedList<MenuElement>();
				}
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
		if (isChildrenOfAssociation() && getRootOfChildrenAssociation() != null) {
			getRootOfChildrenAssociation().clearEditorGroups();
		} else {
			editGroups.clear();
		}
	}

	public Map<String, ContentElementList> getContentElementListMap() {
		if (contentElementListMap == null) {
			contentElementListMap = Collections.synchronizedMap(new HashMap<String, ContentElementList>());
		}
		return contentElementListMap;
	}

	private Map<String, ContentElementList> getLocalContentElementListMap() {
		if (localContentElementListMap == null) {
			localContentElementListMap = new HashMap<String, ContentElementList>();
		}
		return localContentElementListMap;
	}

	/**
	 * clear content of the page, and delete all children.
	 */
	private void clearPage() {
		getContentElementListMap().clear();
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
					if (componentBean[i] != null) {
						if (!compToBeDeleted.contains(componentBean[i].getId())) {
							outList.add(componentBean[i]);
						} else {
							IContentVisualComponent comp = ComponentFactory.getComponentWithType(ctx, componentBean[i].getType());
							if (comp != null) {
								((AbstractVisualComponent) comp).setPage(this);
								comp.delete(ctx);
							}
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
		if (obj == this) {
			return true;
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
		return "clk__" + getPath() + "__" + StringHelper.renderDate(date, GlobalContext.ACCESS_DATE_FORMAT);
	}

	public MenuElement[] _getAllChildren() throws Exception {
		ArrayList<MenuElement> list = getChildElementRecursive(this, 0, new ArrayList<MenuElement>());
		MenuElement[] res = new MenuElement[list.size()];
		list.toArray(res);
		return res;
	}

	public List<MenuElement> getAllChildrenList() throws Exception {
		return getChildElementRecursive(this, 0, new ArrayList<MenuElement>());
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
	private ContentElementList getAllLocalContent(ContentContext ctx) throws Exception {
		ContentElementList localContentElementList = getLocalContentElementListMap().get(ctx.getRequestContentLanguage());
		if (localContentElementList == null) {
			logger.fine("update all local content on (ctx:" + ctx + ")");
			localContentElementList = new ContentElementList(componentBean, ctx, this, true);
			getLocalContentElementListMap().put(ctx.getRequestContentLanguage(), localContentElementList);
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
		ContentElementList contentList = getAllContent(ctx);
		Collection<Resource> outList = new LinkedList<Resource>();
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent comp = contentList.next(ctx);
			if (comp instanceof IStaticContainer) {
				Collection<Resource> resources = ((IStaticContainer) comp).getAllResources(ctx);
				if (resources != null) {
					outList.addAll(resources);
				} else {
					logger.warning("ressources list null on a " + comp.getType() + " id:" + comp.getId());
				}
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
	 * get the content in current language and in default languages order if not
	 * exist.
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

	protected String getCacheKey(ContentContext ctx, String subkey) {

		String deviceStr = "laptop";
		if (ctx.getDevice() != null) {
			deviceStr = "dv-" + ctx.getDevice().isMobileDevice();
		}

		String key = this.getClass().getName() + '_' + getId() + '_' + subkey + '_' + deviceStr;
		if (ctx.getGlobalContext().isCollaborativeMode() && ctx.getCurrentEditUser() != null) {
			key = key + '_' + ctx.getCurrentEditUser().getLogin();
		}
		return key;
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
			noAreaCtx.setRenderMode(ContentContext.PREVIEW_MODE);
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

	/**
	 * get the font of the page (category component)
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String getFont(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.font != null) {
			return desc.font;
		}
		String res = "";
		ContentContext noAreaCtx = ctx.getContextWithoutArea();

		if (noAreaCtx.getRenderMode() == ContentContext.EDIT_MODE) {
			noAreaCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		}

		IContentComponentsList contentList = getContent(noAreaCtx);
		while (contentList.hasNext(noAreaCtx)) {
			IContentVisualComponent elem = contentList.next(noAreaCtx);
			if (elem.getType().equals(Font.TYPE)) {
				res = elem.getValue(noAreaCtx);
			}
		}
		desc.font = res;

		return desc.font;
	}

	/**
	 * get the category of the page (category component)
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String getForward(ContentContext ctx) throws Exception {
		if (!ctx.isAsViewMode()) {
			return null;
		}

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.forward != null) {
			return desc.forward;
		}
		String res = "";
		ContentContext noAreaCtx = ctx.getContextWithoutArea();

		IContentComponentsList contentList = getContent(noAreaCtx);
		while (contentList.hasNext(noAreaCtx)) {
			IContentVisualComponent elem = contentList.next(noAreaCtx);
			if (elem.getType().equals(Forward.TYPE)) {
				res = elem.getValue(noAreaCtx);
			}
		}
		if (!StringHelper.isEmpty(res) && !res.contains("/")) {
			ContentService content = ContentService.getInstance(ctx.getRequest());
			MenuElement page = content.getNavigation(noAreaCtx).searchChildFromName(res);
			if (page != null) {
				res = URLHelper.createURL(ctx, page);
			} else {
				logger.warning("page not found : " + res);
			}
		}
		desc.forward = res;
		return desc.forward;
	}

	/**
	 * get the slogan of the page (slogan component)
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String getSlogan(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.slogan != null) {
			return desc.slogan;
		}
		String res = "";
		ContentContext noAreaCtx = ctx.getContextWithoutArea();

		if (noAreaCtx.getRenderMode() == ContentContext.EDIT_MODE) {
			noAreaCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		}

		IContentComponentsList contentList = getContent(noAreaCtx);
		while (contentList.hasNext(noAreaCtx)) {
			IContentVisualComponent elem = contentList.next(noAreaCtx);
			if (elem.getType().equals(Slogan.TYPE)) {
				res = elem.getValue(noAreaCtx);
			}
		}
		desc.slogan = StringUtils.replace(res, "\"", "&quot;");

		return desc.slogan;
	}

	/**
	 * get the color of the page
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public ExtendedColor getColor(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.color != null) {
			return desc.color;
		}
		ExtendedColor res = EmptyColor.instance;
		ContentContext noAreaCtx = ctx.getContextWithoutArea();

		if (noAreaCtx.getRenderMode() == ContentContext.EDIT_MODE) {
			noAreaCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		}

		IContentComponentsList contentList = getContent(noAreaCtx);
		while (contentList.hasNext(noAreaCtx)) {
			IContentVisualComponent elem = contentList.next(noAreaCtx);
			if (elem.getType().equals(ColorComponent.TYPE) && !StringHelper.isEmpty(elem.getValue(noAreaCtx)) && !ColorComponent.BACKGROUND_COLOR.equals(elem.getStyle())) {
				try {
					res = new ExtendedColor(Color.decode(elem.getValue(noAreaCtx)));
				} catch (Exception e) {
					res = EmptyColor.instance;
				}
			}
		}
		desc.color = res;
		return desc.color;
	}

	/**
	 * get the background color
	 *
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public ExtendedColor getBackgroundColor(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.backgroundColor != null) {
			return desc.backgroundColor;
		}
		ExtendedColor res = EmptyColor.instance;
		ContentContext noAreaCtx = ctx.getContextWithoutArea();

		if (noAreaCtx.getRenderMode() == ContentContext.EDIT_MODE) {
			noAreaCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		}

		IContentComponentsList contentList = getContent(noAreaCtx);
		while (contentList.hasNext(noAreaCtx)) {
			IContentVisualComponent elem = contentList.next(noAreaCtx);
			if (elem.getType().equals(ColorComponent.TYPE) && !StringHelper.isEmpty(elem.getValue(noAreaCtx)) && ColorComponent.BACKGROUND_COLOR.equals(elem.getStyle())) {
				try {
					res = new ExtendedColor(Color.decode(elem.getValue(noAreaCtx)));
				} catch (Exception e) {
					res = EmptyColor.instance;
				}
			}
		}
		desc.backgroundColor = res;
		return desc.backgroundColor;
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

	ArrayList<MenuElement> getChildElementRecursive(MenuElement elem, int deph, ArrayList<MenuElement> result) throws Exception {
		result.add(elem);
		Collection<MenuElement> children = elem.getChildMenuElements();
		for (MenuElement child : children) {
			getChildElementRecursive(child, deph + 1, result);
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
	 * static MenuElement searchChild(MenuElement elem, ContentContext ctx, String
	 * path, int depth) { if (depth > MAX_SEARCH_DEPTH) { return null; } MenuElement
	 * res = null; List<MenuElement> children =
	 * elem.getChildMenuElementsWithVirtualList(false, false); for (MenuElement
	 * menuElement : children) { List<String> paths =
	 * menuElement.getAllVirtualPath(ctx); if (paths.contains(path)) { return
	 * menuElement; } else { res = searchChild(menuElement, ctx, path, depth+1); if
	 * (res != null) { return res; } } }
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

	public int getChildPosition(MenuElement child) {
		int pos = childMenuElements.indexOf(child);
		if (pos < 0) {
			return -1;
		} else {
			return pos + 1;
		}
	}

	/**
	 * return the position on the page in the children list of her parent
	 * 
	 * @return
	 */
	public int getPosition() {
		if (parent == null) {
			return 1;
		} else {
			return parent.getChildPosition(this);
		}
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

	public String getContentAsText(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		ContentElementList content = getContent(ctx);
		while (content.hasNext(ctx)) {
			out.println(content.next(ctx).getContentAsText(ctx));
		}
		out.close();
		return new String(outStream.toByteArray());
	}

	public List<ComponentBean> getContentAsList() {
		return Arrays.asList(componentBean);
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
		ContentElementList elemList = getLocalContent(pageCtx);

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
		return getContentByType(ctx, type, true);
	}

	private List<IContentVisualComponent> getContentByType(ContentContext ctx, String type, boolean withRepeat) throws Exception {

		List<IContentVisualComponent> outComp = new LinkedList<IContentVisualComponent>();

		ContentElementList content;
		if (withRepeat) {
			content = getAllContent(ctx);
		} else {
			content = getContent(ctx);
		}
		while (content.hasNext(ctx)) {
			IContentVisualComponent comp = content.next(ctx);
			if (comp.getType().equals(type)) {
				outComp.add(comp);
			}
		}
		content.initialize(ctx);

		return outComp;
	}

	public List<IContentVisualComponent> getAllLanguageContent(ContentContext ctx) throws Exception {

		List<IContentVisualComponent> outComp = new LinkedList<IContentVisualComponent>();

		for (ComponentBean bean : getContent()) {
			IContentVisualComponent comp = ComponentFactory.createComponent(ctx, bean, this, null, null);
			outComp.add(comp);
		}

		return outComp;
	}

	public List<IContentVisualComponent> getContentByImplementation(ContentContext ctx, Class clazz) throws Exception {

		List<IContentVisualComponent> outComp = new LinkedList<IContentVisualComponent>();

		ContentElementList content = getAllContent(ctx);
		while (content.hasNext(ctx)) {
			IContentVisualComponent comp = content.next(ctx);
			if (clazz.isInstance(comp)) {
				outComp.add(comp);
			} else if (comp instanceof PageMirrorComponent) {
				PageMirrorComponent pageMirror = (PageMirrorComponent) comp;
				if (pageMirror.getArea().equals(ctx.getArea()) && !pageMirror.getPage().equals(this)) {
					List<IContentVisualComponent> mirrorContent = pageMirror.getMirrorPage(ctx).getContentByImplementation(ctx, clazz);
					for (IContentVisualComponent mComp : mirrorContent) {
						mComp.setContainerPage(ctx, pageMirror.getPage());
						outComp.add(mComp);
					}
				}
			}
		}
		content.initialize(ctx);

		return outComp;
	}

	/**
	 * return a language with content. If there are content in current language, it
	 * is returned.
	 * 
	 * @return a ContentContext with content or current context if there are no
	 *         content in any language.
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

			Collection<String> defaultLgs = globalContext.getDefaultLanguages();
			for (String lg : defaultLgs) {
				lgCtx.setAllLanguage(lg);
				if (isRealContent(lgCtx)) {
					return lgCtx;
				}
			}

			Collection<String> languages = globalContext.getContentLanguages();
			for (String lg : languages) {
				/*
				 * if (globalContext.getLanguages().contains(lg)) { // if content lg exist as
				 * lgCtx.setLanguage(lg); }
				 */
				lgCtx.setContentLanguage(lg);
				lgCtx.setRequestContentLanguage(lg);
				if (isRealContent(lgCtx)) {
					return lgCtx;
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

	public int getToTheTopLevel(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		// no cache to read toTheTop defined
		if (desc.toTheTop != null && desc.toTheTop == 0) {
			return desc.toTheTop;
		}
		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setArea(null);

		if (newCtx.getRenderMode() == ContentContext.EDIT_MODE) {
			newCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		}
		IContentComponentsList contentList = getAllContent(newCtx);
		desc.toTheTop = 0;
		while (contentList.hasNext(newCtx)) {
			IContentVisualComponent elem = contentList.next(newCtx);
			if (elem instanceof ToTheTopComponent) {
				desc.toTheTop = ((ToTheTopComponent) elem).getPower();
				return desc.toTheTop;
			}

		}
		return desc.toTheTop;
	}

	public Date getContentDateComponent(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Iterator<String> defaultLgs = globalContext.getDefaultLanguages().iterator();
		ContentContext localContext = new ContentContext(ctx);
		localContext.setArea(null);
		while (isEmpty(localContext) && defaultLgs.hasNext()) {
			localContext.setRequestContentLanguage(defaultLgs.next());
		}
		if (isChildrenAssociation()) {
			for (MenuElement child : getChildMenuElements()) {
				Date contentDate = child.getContentDate(ctx);
				if (contentDate != null) {
					return contentDate;
				}
			}
		} else {
			ContentElementList contentList = getAllContent(localContext);
			Date bestDate = null;
			while (contentList.hasNext(ctx)) {
				IContentVisualComponent comp = contentList.next(ctx);
				if (comp instanceof IDate && ((IDate) comp).isValidDate(ctx)) {
					IDate dateComp = ((IDate) comp);
					if (dateComp.getDate(ctx) != null) {
						if (bestDate == null || bestDate.getTime() < dateComp.getDate(ctx).getTime()) {
							bestDate = dateComp.getDate(ctx);
						}
					}
				}
			}
			return bestDate;
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
			return getTimeRange(ctx).getStartDate();
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
	public HtmlPart getDescription(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.description != null) {
			return desc.description;
		}

		String res = "";
		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setArea(null);

		if (newCtx.getRenderMode() == ContentContext.EDIT_MODE) {
			newCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		}

		IContentComponentsList contentList = getAllContent(newCtx);
		PageMirrorComponent pageMirror = null;
		while (contentList.hasNext(newCtx)) {
			IContentVisualComponent elem = ComponentHelper.getRealComponent(newCtx, contentList.next(newCtx));
			if (elem != null) {
				if (elem instanceof PageMirrorComponent) {
					pageMirror = (PageMirrorComponent) elem;
				} else {
					String description = elem.getPageDescription(ctx);
					if (!StringHelper.isEmpty(description)) {
						if (!elem.isRepeat()) {
							desc.description = new HtmlPart(StringHelper.removeTag(StringUtils.replace(description, "\"", "&quot;")), "p", elem.getComponentCssClass(newCtx));
							;
							return desc.description;
						} else {
							res = description;
						}
					}
				}
			}
		}
		if (StringHelper.isEmpty(res) && pageMirror != null && pageMirror.getPage() != null && pageMirror.getMirrorPage(newCtx) != null && !pageMirror.getMirrorPage(newCtx).getId().equals(getId())) {
			desc.description = pageMirror.getMirrorPage(newCtx).getDescription(newCtx);
		} else {
			desc.description = new HtmlPart(StringHelper.removeTag(StringUtils.replace(res, "\"", "&quot;")));
		}
		return desc.description;
	}

	/**
	 * get description of the page (description component)
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String getDescriptionAsText(ContentContext ctx) throws Exception {
		HtmlPart description = getDescription(ctx);
		if (description != null) {
			return description.getText();
		} else {
			return "";
		}
	}

	/**
	 * get description of the page (description component)
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String getXHTMLDescription(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.xhtmlDescription != null) {
			return desc.xhtmlDescription;
		}
		String res = "";
		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setArea(null);

		if (newCtx.getRenderMode() == ContentContext.EDIT_MODE) {
			newCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		}

		IContentComponentsList contentList = getAllContent(newCtx);
		while (contentList.hasNext(newCtx)) {
			IContentVisualComponent elem = contentList.next(newCtx);
			if (elem.getPageDescription(ctx) != null) {
				res = res + elem.getPageDescription(ctx);
			}
		}
		desc.xhtmlDescription = res;
		return XHTMLHelper.textToXHTML(desc.xhtmlDescription);
	}

	/**
	 * get number of reactions of the page (description component)
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public int getReactionSize(ContentContext ctx) throws Exception {
		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setArea(null);
		newCtx.setCurrentPageCached(this);

		IContentComponentsList contentList = getAllContent(newCtx);
		while (contentList.hasNext(newCtx)) {
			IContentVisualComponent elem = contentList.next(newCtx);
			if (elem.getType().equals(ReactionComponent.TYPE)) {
				return ((ReactionComponent) elem).getReactionSize(newCtx);
			}
		}
		return 0;
	}

	public Set<String> getEditorRoles() {
		if (isChildrenOfAssociation() && getRootOfChildrenAssociation() != null) {
			return getRootOfChildrenAssociation().getEditorRoles();
		} else {
			return editGroups;
		}
	}

	public void setEditRoles(MenuElement page) {
		editGroups = new HashSet<String>(page.editGroups);
	}

	public Set<String> getEditorRolesAndParent() {
		Set<String> roles = new HashSet<String>(editGroups);
		if (getParent() != null) {
			roles.addAll(getParent().getEditorRolesAndParent());
		}
		return roles;
	}

	public List<String> getFollowers(ContentContext ctx) {
		List<String> outFollowers = getLocalFollowers(ctx);
		if (getParent() != null) {
			List<String> outFollowersParent = getParent().getFollowers(ctx);
			if (outFollowersParent.size() > 0) {
				outFollowers = LangHelper.getModifiableList(outFollowers);
				outFollowers.addAll(outFollowersParent);
			}
		}
		return outFollowers;
	}

	private String getFollowersKey() {
		return "_folowers_" + getId();
	}

	public void addFollowers(ContentContext ctx, String userName) {
		List<String> followers = LangHelper.getModifiableList(getFollowers(ctx));
		if (!followers.contains(userName)) {
			followers.add(userName);
			ctx.getGlobalContext().setData(getFollowersKey(), StringHelper.collectionToString(followers, ","));
		}
	}

	public List<String> getLocalFollowers(ContentContext ctx) {
		String followers = ctx.getGlobalContext().getData(getFollowersKey());
		List<String> outFollowers;
		if (StringHelper.isEmpty(followers)) {
			outFollowers = Collections.emptyList();
		} else {
			outFollowers = StringHelper.stringToCollection(followers, ",");
		}
		return outFollowers;
	}

	public void removeFollowers(ContentContext ctx, String userName) {

		MenuElement parent = this;
		while (parent != null) {
			List<String> followers = parent.getLocalFollowers(ctx);
			if (followers.size() > 0 && followers.contains(userName)) {
				followers.remove(userName);
				ctx.getGlobalContext().setData(parent.getFollowersKey(), StringHelper.collectionToString(followers, ","));
			}
			parent = parent.getParent();
		}

	}

	public Collection<String> getExternalResources(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.needdedResources == null) {
			Collection<String> outResources = new LinkedList<String>();
			ContentElementList content = getAllContent(ctx);
			while (content.hasNext(ctx)) {
				IContentVisualComponent comp = content.next(ctx);
				Collection<String> resources = comp.getExternalResources(ctx);
				if (resources != null) {
					for (String res : resources) {
						if (!outResources.contains(res)) {
							outResources.add(res);
						}
					}
				}
				// outResources.addAll(comp.getExternalResources());
			}
			desc.needdedResources = outResources;
		}
		return desc.needdedResources;
	}

	public Collection<String> getExternalModules(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.needdedModules == null) {
			Collection<String> outModules = new LinkedList<String>();
			ContentElementList content = getAllContent(ctx);
			while (content.hasNext(ctx)) {
				IContentVisualComponent comp = content.next(ctx);
				Collection<String> modules = comp.getExternalModules(ctx);
				if (modules != null) {
					for (String res : modules) {
						if (!outModules.contains(res)) {
							outModules.add(res);
						}
					}
				}
				// outResources.addAll(comp.getExternalResources());
			}
			desc.needdedModules = outModules;
		}
		return desc.needdedModules;
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

		PageDescription desc = getPageDescriptionCached(ctx, newCtx.getRequestContentLanguage());

		if (desc.label != null) {
			return desc.label;
		}

		newCtx.setRequestContentLanguage(ctx.getRequestContentLanguage()); // label
																			// is
																			// from
		// navigation
		// language
		desc.label = getLocalContent(newCtx).getLabel(newCtx);

		if (desc.label != null) {
			if ((desc.label.trim().length() == 0) && (name != null)) {
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
				if (globalContext.isAutoSwitchToDefaultLanguage()) {
					ContentContext defaultLgCtx = newCtx.getContextWithContent(this);
					if (defaultLgCtx != null) {
						desc.label = getContent(defaultLgCtx).getLabel(ctx);
						if ((desc.label.trim().length() == 0) && (name != null)) {
							desc.label = getSubTitle(defaultLgCtx);
						}
					}
				}
			}
			desc.label = StringHelper.removeTag(desc.label);
			if (StringHelper.isEmpty(desc.label)) {
				desc.label = name;
			}
			desc.label = XHTMLHelper.replaceJSTLData(ctx, desc.label);
		}
		
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

		for (MenuElement child : getAllChildrenList()) {
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
	 * public ContentElementList getViewContent(ContentContext ctx, int format)
	 * throws Exception { ContentElementList contentElementList =
	 * contentElementListMap.get(ctx.getRequestContentLanguage()); if
	 * (contentElementList == null) { Content content =
	 * Content.createContent(ctx.getRequest()); ContentContext localContext = new
	 * ContentContext(ctx); if (!content.contentExistForContext(ctx)) {
	 * GlobalContext globalCotext = GlobalContext.getInstance(ctx.getRequest());
	 * Set<String> lgs = globalCotext.getContentLanguages(); for (String lg : lgs) {
	 * localContext.setLanguage(lg); if
	 * (content.contentExistForContext(localContext)) { break; } } }
	 * contentElementList = new ContentElementList(componentBean, localContext,
	 * this); contentElementListMap.put(ctx.getRequestContentLanguage(),
	 * contentElementList); }
	 * 
	 * return contentElementList; }
	 */

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	public IImageTitle getImageBackground(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.imageHeader != null) {
			if (desc.imageHeader == ImageTitleBean.EMPTY_BEAN) {
				return null;
			} else {
				return desc.imageHeader;
			}
		}
		ContentContext specialCtx = ctx.getContextWithArea(ComponentBean.DEFAULT_AREA);
		IContentComponentsList contentList = getAllContent(specialCtx);
		int bestPriority = Integer.MIN_VALUE;
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent elem = contentList.next(specialCtx);
			if ((elem instanceof ImageBackground) && elem.isRealContent(specialCtx)) {
				if (((ImageBackground) elem).isForGlobal()) {
					IImageTitle imageComp = (IImageTitle) elem;
					if (imageComp.isImageValid(specialCtx)) {
						int priority = imageComp.getPriority(specialCtx);
						if (priority == 9) {
							desc.imageHeader = new ImageTitleBean(specialCtx, imageComp);
							return imageComp;
						} else if (priority > bestPriority) {
							desc.imageHeader = new ImageTitleBean(specialCtx, imageComp);
							bestPriority = priority;
						}
					}
				}
			}
		}
		if (desc.imageHeader == null) {
			desc.imageHeader = ImageTitleBean.EMPTY_BEAN;
			return null;
		} else {
			return desc.imageHeader;
		}
	}

	public Map<String, ImageTitleBean> getImageBackgroundForArea(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.imageAreaBackground != null) {
			return desc.imageAreaBackground;
		}
		Map<String, ImageTitleBean> outImageBackground = new HashMap<String, ImageTitleBean>();
		ContentContext specialCtx = ctx.getContextWithArea(ComponentBean.DEFAULT_AREA);
		IContentComponentsList contentList = getAllContent(specialCtx);
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent elem = contentList.next(specialCtx);
			if ((elem instanceof ImageBackground) && elem.isRealContent(specialCtx)) {
				if (((ImageBackground) elem).isForArea()) {
					IImageTitle imageComp = (IImageTitle) elem;
					if (imageComp.isImageValid(specialCtx)) {
						if (outImageBackground.get(elem.getArea()) == null || !elem.isRepeat()) {
							outImageBackground.put(elem.getArea(), new ImageTitleBean(specialCtx, imageComp));
						}
					}
				}
			}
		}
		if (outImageBackground.size() > 0) {
			desc.imageAreaBackground = outImageBackground;
		} else {
			desc.imageAreaBackground = Collections.EMPTY_MAP;
		}
		return desc.imageAreaBackground;
	}

	public ImageBean getImageBean(ContentContext ctx) throws Exception {
		return new ImageBean(ctx, getImage(ctx), "standard");
	}

	public IImageTitle getImage(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.imageLink != null) {
			return desc.imageLink;
		}
		String defaultArea = ComponentBean.DEFAULT_AREA;
		Template template = TemplateFactory.getTemplate(ctx, this);
		if (template != null) {
			defaultArea = template.getDefaultArea();
		}

		boolean mobileDevice = false;
		if (ctx.getDevice() != null) {
			mobileDevice = ctx.getDevice().isMobileDevice();
		}

		ContentContext specialCtx = ctx.getContextWithArea(defaultArea);
		IContentComponentsList contentList = getAllContent(specialCtx);
		contentList.setAllArea(false);
		IImageTitle bestImageTitle = null;
		int bestPriority = Integer.MIN_VALUE;
		while (contentList.hasNext(specialCtx)) {
			IContentVisualComponent elem = contentList.next(specialCtx);
			if ((elem instanceof IImageTitle) && !(elem instanceof ImageBackground) && (!elem.isRepeat())) {
				IImageTitle imageComp = (IImageTitle) elem;

				if (mobileDevice && imageComp.isMobileOnly(specialCtx)) {
					desc.imageLink = new ImageTitleBean(specialCtx, imageComp);
					return imageComp;
				}

				if (imageComp.isImageValid(specialCtx)) {
					int priority = imageComp.getPriority(specialCtx);
					if (priority > bestPriority) {
						bestPriority = priority;
						bestImageTitle = imageComp;
					}
				}
			}
		}

		//if (bestImageTitle == null) {
			/** search on all area **/
			specialCtx = ctx.getContextWithArea(null);
			contentList = getAllContent(specialCtx);
			bestPriority = Integer.MIN_VALUE;
			while (contentList.hasNext(ctx)) {
				IContentVisualComponent elem = contentList.next(specialCtx);
				if ((elem instanceof IImageTitle) && (!elem.isRepeat())) {
					IImageTitle imageComp = (IImageTitle) elem;
					if (imageComp.isImageValid(specialCtx)) {
						int priority = imageComp.getPriority(specialCtx);
						if (elem.getArea().equals(defaultArea)) {
							priority = priority*2;
						}
						if (priority > bestPriority) {
							bestPriority = priority;
							bestImageTitle = imageComp;
						}
					}
				}
			}
		//}
		if (bestImageTitle != null) {
			desc.imageLink = new ImageTitleBean(specialCtx, bestImageTitle);
		}
		return desc.imageLink;
	}

	public List<IImageTitle> getImages(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		List<IImageTitle> res = null;
		if (desc.images != null) {
			res = desc.images;
		}
		if (res != null) {
			return res;
		}

		String defaultArea = ComponentBean.DEFAULT_AREA;
		Template template = TemplateFactory.getTemplate(ctx, this);
		if (template != null) {
			defaultArea = template.getDefaultArea();
		}

		res = new LinkedList<IImageTitle>();
		IContentComponentsList contentList = getAllContent(ctx);
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent elem = contentList.next(ctx);
			if ((elem instanceof IImageTitle)) {
				IImageTitle imageComp = (IImageTitle) elem;
				if (imageComp.isImageValid(ctx)) {
					int w = 0;
					if (!elem.isRepeat()) {
						if (elem.getArea().equals(defaultArea)) {
							w = 6;
						} else {
							w = 4;
						}
					} else {
						if (elem.getPage().equals(this)) {
							if (elem.getArea().equals(defaultArea)) {
								w = 3;
							} else {
								w = 1;
							}
						}
					}
					if (w > 0) {
						res.add(new ImageTitleBean(imageComp.getImageDescription(ctx), imageComp.getResourceURL(ctx), imageComp.getImageLinkURL(ctx), w, imageComp.isMobileOnly(ctx)));
					}
				}
			}
		}
		if (res.size() == 0 && isChildrenAssociation()) {
			for (MenuElement child : getAllChildrenList()) {
				contentList = child.getAllContent(ctx);
				while (contentList.hasNext(ctx)) {
					IContentVisualComponent elem = contentList.next(ctx);
					if ((elem instanceof IImageTitle) && (!elem.isRepeat())) {
						IImageTitle imageComp = (IImageTitle) elem;
						if (imageComp.isImageValid(ctx)) {
							res.add(new ImageTitleBean(imageComp.getImageDescription(ctx), imageComp.getResourceURL(ctx), imageComp.getImageLinkURL(ctx), imageComp.isMobileOnly(ctx)));
						}
					}
				}
			}
		}

		if (res.size() > 1) {
			Collections.sort(res, new SortImageTitleByPriority(ctx));
		} else if (res.size() == 0) {
			res = Collections.emptyList();
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
			if (!(elem instanceof IImageTitle) && (elem instanceof IStaticContainer) && (elem.isRealContent(ctx)) && (!elem.isRepeat())) {
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
		IContentComponentsList contentList = getAllContent(ctx);
		Set<String> keywordsSet = new HashSet<String>();
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent elem = contentList.next(ctx);
			if (elem.getType().equals(Keywords.TYPE)) {
				if (res.length() > 0) {
					res = res + ',';
				}
				if (!keywordsSet.contains(elem.getValue(ctx))) {
					res = res + elem.getValue(ctx);
					keywordsSet.add(elem.getValue(ctx));
				}
				Keywords keywords = (Keywords) elem;
				if (keywords.getComponentCssClass(ctx).equals(Keywords.BOLD_IN_CONTENT)) {
					String[] keys = keywords.getValue().split(",");
					for (String key : keys) {
						if (!keywordsSet.contains(key)) {
							keywordsSet.add(key);
						}
					}
				}
			}
		}
		/*
		 * List<String> tags = getTags(ctx); for (String tag : tags) { if (res.length()
		 * > 0) { res = res + ','; } if (!keywordsSet.contains(tag)) { res = res + tag;
		 * keywordsSet.add(res); } }
		 */
		desc.keywords = res;
		return desc.keywords;
	}

	public String getLabel(ContentContext ctx) throws Exception {
		if (isTrash()) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return i18nAccess.getText("preview.label.trash");
		}
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
			while (contentList.hasNext(newCtx) && desc.linkOn == null) {
				IContentVisualComponent elem = contentList.next(newCtx);
				if (elem instanceof ILink && !elem.isRepeat()) {
					res = ((ILink) elem).getURL(newCtx);
					if (((ILink) elem).isLinkValid(newCtx)) {
						desc.linkOn = res;
					}
				}
			}
		}
		if (desc.linkOn == null) {
			desc.linkOn = "";
		}
		if (desc.linkOn.length() == 0) {
			return null;
		}
		return desc.linkOn;
	}

	public boolean isLinkRealContent(ContentContext ctx) throws Exception {
		if (isRealContent(ctx)) {
			return true;
		} else if (!StringHelper.isEmpty(getLinkOn(ctx))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * get content of the current area
	 * 
	 * @param ctx
	 *            current context
	 * @return
	 * @throws Exception
	 */
	private ContentElementList getLocalContent(ContentContext ctx) throws Exception {
		ContentElementList localContentElementList = getContentElementListMap().get(ctx.getRequestContentLanguage());
		if (!ctx.isComponentCache()) {
			localContentElementList = new ContentElementList(componentBean, ctx, this, false);
		} else if (localContentElementList == null || !ctx.isAsViewMode()) {
			localContentElementList = new ContentElementList(componentBean, ctx, this, false);
			if (!ctx.isFree()) { // no reference to template >>> some component can be absent
				getContentElementListMap().put(ctx.getRequestContentLanguage(), localContentElementList);
			}

			logger.fine("update local content  - # component : " + localContentElementList.size(ctx) + " (ctx:" + ctx + ")");
		}
		localContentElementList = new ContentElementList(localContentElementList);
		localContentElementList.initialize(ctx);

		return localContentElementList;
	}

	public ContentElementList getLocalContentCopy(ContentContext ctx) throws Exception {
		return getLocalContent(ctx);
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
	private Object getLock() {
		return lock;
	}

	public Date getManualModificationDate() {
		return manualModificationDate;
	}

	/**
	 * get the metaHead for meta tag (if no meta description defined return the
	 * description)
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String getMetaHead(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.metaHead != null) {
			return desc.metaHead;
		}
		String res = "";
		IContentComponentsList contentList = getAllContent(ctx);
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent elem = contentList.next(ctx);
			if (elem.getType().equals(HeadMeta.TYPE)) {
				res = res + elem.getValue(ctx);
			}
		}
		desc.metaHead = res;
		return desc.metaHead;
	}

	/**
	 * get the description for meta tag (if no meta description defined return the
	 * description)
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
			if (getDescription(ctx) != null) {
				res = getDescription(ctx).getText();
			}
		}
		desc.metaDescription = StringUtils.replace(res, "\"", "&quot;");
		return desc.metaDescription;
	}

	public Date getModificationDate() throws ParseException, Exception {
		return getModificationDate(null);
	}

	public Date getModificationDate(ContentContext ctx) throws ParseException, Exception {
		Date pageDate;
		if (getManualModificationDate() != null) {
			pageDate = getManualModificationDate();
		} else {
			pageDate = getRealModificationDate(ctx);
		}
		if (isRootChildrenAssociation()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(pageDate);
			for (MenuElement child : getChildMenuElements()) {
				Calendar childCat = Calendar.getInstance();
				childCat.setTime(child.getModificationDate(ctx));
				if (childCat.after(cal)) {
					cal = childCat;
				}
			}
			pageDate = cal.getTime();
		}
		return pageDate;
	}

	/**
	 * reutrn the name, formated as "file name", without space and special char.
	 * 
	 * @return
	 */
	public String getName() {
		if (nameKey == null) {
			nameKey = StringHelper.createFileName(StringHelper.neverNull(name, "_NULL_")).trim();
		}
		return nameKey;
	}

	/**
	 * return the name, as create by user.
	 * 
	 * @return
	 */
	public String getHumanName() {
		return name;
	}

	/**
	 * get the next menu element in the child list
	 * 
	 * @return a MenuElement with the same depth, null if current is the first
	 *         element.
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
		/*
		 * GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		 * ICache cache = globalContext.getCache("navigation"); if (releaseCache) {
		 * String prefix = getCacheKey(""); for (String key : cache.getKeys()) { if
		 * (key.startsWith(prefix)) { cache.removeItem(key); } } releaseCache = false; }
		 * return cache;
		 */

		if (localCache == null) {
			localCache = new MapCache(new HashMap(), "navigation");
		}
		if (releaseCache) {
			localCache.removeAll();
			releaseCache = false;
		}
		return localCache;
	}

	PageDescription getPageDescriptionCached(ContentContext ctx, String lg) {
		String key = getCacheKey(ctx, lg);
		PageDescription outDesc = (PageDescription) getCache(ctx).get(key);
		if (outDesc == null) {
			outDesc = new PageDescription();
			getCache(ctx).put(key, outDesc);
		}
		return outDesc;
	}

	PageDescription getPageBeanCached(ContentContext ctx, String lg) {
		String key = getCacheKey(ctx, "bean-" + lg);
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
	 * get index of the page in the children list of the her parent page
	 * 
	 * @return
	 */
	public int getIndex() {
		MenuElement parent = getParent();
		if (parent != null) {
			return parent.getChildMenuElements().indexOf(this) + 1;
		} else {
			return 1;
		}
	}

	/**
	 * get the page rank (define with content)
	 * 
	 * @return a page rank between 0 and 1
	 * @throws Exception
	 */
	public double getPageRank(ContentContext ctx) throws Exception {

		final double defaultValue = 0.5;

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.pageRank != null) {
			return desc.pageRank;
		}

		ContentElementList contentList = getAllContent(ctx); // search date in
		// all area
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent comp = contentList.next(ctx);
			if (comp instanceof IPageRank) {
				int votes = ((IPageRank) comp).getVotes(ctx, getPath());
				if (votes == 0) {
					return defaultValue;
				}
				if (votes > 0) {
					desc.pageRank = (((IPageRank) comp).getRankValue(ctx, getPath())) / (double) votes;
					desc.pageRank = desc.pageRank + (double) (votes) / VOTES_MULTIPLY;
					return desc.pageRank;
				}
			}
		}
		desc.pageRank = defaultValue;
		return desc.pageRank;
	}

	/**
	 * get the title of the page define in the content, empty string if no title
	 * defined.
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

		desc.forcedPageTitle = getContent(newCtx).getPageTitle(ctx);
		if (desc.forcedPageTitle == null) {
			desc.forcedPageTitle = "";
		}

		return desc.forcedPageTitle;
	}

	public String getPageTitle(ContentContext ctx) throws Exception {
		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setArea(null); // warning : check if the method is needed.
		newCtx.setFree(true);

		PageDescription desc = getPageDescriptionCached(ctx, newCtx.getRequestContentLanguage());

		if (desc.pageTitle != null) {
			// return desc.pageTitle;
		}

		desc.pageTitle = getForcedPageTitle(newCtx);

		if (desc.pageTitle == null || desc.pageTitle.length() == 0) {
			desc.pageTitle = getTitle(newCtx);
			if (desc.pageTitle != null && desc.pageTitle.equals(getName())) {
				desc.pageTitle = getLabel(newCtx);
			}
		}
		return desc.pageTitle;
	}

	/**
	 * @return Returns the parent.
	 */
	public MenuElement getParent() {
		return parent;
	}

	public boolean isRoot() {
		return getParent() == null;
	}

	public boolean isLikeRoot(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.likeRoot != null) {
			return desc.likeRoot;
		}
		if (isRoot()) {
			desc.likeRoot = true;
			return desc.likeRoot;
		}
		MenuElement parent = getParent();
		MenuElement child = this;
		while (parent != null && parent.getFirstChild() != null && parent.getFirstChild().getId().equals(child.getId()) && !parent.isRealContent(ctx)) {
			child = parent;
			parent = parent.getParent();
		}
		desc.likeRoot = (parent == null);
		return desc.likeRoot;
	}

	/**
	 * check if the current page is a child of a page with id or name give in
	 * parameter.
	 * 
	 * @param page
	 *            name, id of a page or path of the page.
	 * @return
	 */
	public boolean isChildOf(String page) {
		if (page == null) {
			return false;
		} else {
			if (page.equals("/")) {
				return true;
			}
			MenuElement parent = getParent();
			while (parent != null) {
				if (parent.getId().equals(page) || parent.getName().equals(page) || parent.getPath().equals(page)) {
					return true;
				}
				parent = parent.getParent();
			}
		}
		return false;
	}

	/**
	 * return the page of this page
	 * 
	 * @return a path.
	 */
	public String _getPath() {
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
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * return the page of this page
	 * 
	 * @return a path.
	 */
	public String getPath() {
		try {
			MenuElement workParent = parent;
			MenuElement previousParent = this;
			String path = "";
			while (workParent != null) {
				path = previousParent.getName() + '/' + path;
				if (workParent == this) {
					throw new Exception("recursive reference : " + getName());
				}
				previousParent = workParent;
				workParent = workParent.parent;
			}
			// if (parent != null) {
			// return parent.getPath() + '/' + getName();
			// } else {
			// return "";
			// }
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			path = '/' + path;
			return path;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * get the previous menu element in the child list
	 * 
	 * @return a MenuElement with the same depth, null if current is the first
	 *         element.
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
		if (isTrash()) {
			return Integer.MAX_VALUE;
		} else {
			return priority;
		}
	}

	public Date getRealModificationDate(ContentContext ctx) throws ParseException, Exception {
		return modificationDate;
	}

	public Map<String, String> getReplacement() {
		return replacement;
	}

	public Map<String, String> getReplacementEditable() {
		if (replacement == Collections.EMPTY_MAP) {
			replacement = new HashMap<String, String>();
		}
		return replacement;
	}

	public String getReversedLink() {
		return reversedLink;
	}

	public MenuElement getRoot() {
		if (root == null) {
			MenuElement rootNode = this;
			MenuElement parent = getParent();
			while (parent != null) {
				rootNode = parent;
				parent = parent.getParent();
			}
			root = rootNode;
		}
		return root;
	}

	/**
	 * return the depth of the selection. sample: if the first selected element have
	 * children and sedond not the depth is 2.
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
				switch (getSeoWeight()) {
				case SEO_HEIGHT_NULL:
					return 0;
				case SEO_HEIGHT_LOW:
					return 1 / 4;
				case SEO_HEIGHT_NORMAL:
					return 1 / 2;
				case SEO_HEIGHT_HIGHT:
					return 1;
				default:
					break;
				}
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

		newCtx.setArea(null);
		desc.subTitle = getLocalContent(newCtx).getSubTitle(newCtx);
		if (desc.subTitle == null) {
			desc.subTitle = getContent(newCtx).getSubTitle(newCtx);
		}

		return desc.subTitle;
	}

	public List<String> getSubTitles(ContentContext ctx, int level) throws Exception {
		ContentContext newCtx = new ContentContext(ctx);
		PageDescription desc = getPageDescriptionCached(ctx, newCtx.getRequestContentLanguage());
		if (desc.subTitles != null) {
			return desc.subTitles;
		}
		newCtx.setArea(null);
		desc.subTitles = getLocalContent(newCtx).getSubTitles(newCtx, level);
		if (desc.subTitles == null) {
			desc.subTitles = getContent(newCtx).getSubTitles(newCtx, level);
		}
		return desc.subTitles;
	}

	/**
	 * label of the link to page.
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String getLinkLabel(ContentContext ctx) throws Exception {

		ContentContext newCtx = new ContentContext(ctx);

		PageDescription desc = getPageDescriptionCached(ctx, newCtx.getRequestContentLanguage());

		if (desc.linkLabel != null) {
			return desc.linkLabel;
		}

		newCtx.setArea(null);
		desc.linkLabel = getLocalContent(newCtx).getLinkLabel(newCtx);
		if (desc.linkLabel == null) {
			desc.linkLabel = getContent(newCtx).getLinkLabel(newCtx);
		}

		return desc.linkLabel;
	}

	public int getSubTitleLevel(ContentContext ctx) throws Exception {

		ContentContext newCtx = new ContentContext(ctx);

		PageDescription desc = getPageDescriptionCached(ctx, newCtx.getRequestContentLanguage());

		if (desc.subTitleLevel >= 0) {
			return desc.subTitleLevel;
		}

		newCtx.setArea(null);
		desc.subTitleLevel = getLocalContent(newCtx).getSubTitleLevel(newCtx);
		if (desc.subTitleLevel < 0) {
			desc.subTitleLevel = getContent(newCtx).getSubTitleLevel(newCtx);
		}

		return desc.subTitleLevel;
	}

	public List<String> getTags(ContentContext ctx) throws Exception {

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.tags != null) {
			return desc.tags;
		}

		ContentContext lgDefaultCtx = new ContentContext(ctx);
		lgDefaultCtx.setArea(null);

		if (lgDefaultCtx.getRenderMode() == ContentContext.EDIT_MODE) {
			lgDefaultCtx.setRenderMode(ContentContext.PREVIEW_MODE);
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
		if (outTags.size() == 0) {
			desc.tags = Collections.emptyList();
		} else {
			desc.tags = outTags;
		}
		return desc.tags;
	}

	public List<String> getLayouts(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.layouts != null) {
			return desc.layouts;
		}
		ContentContext lgDefaultCtx = new ContentContext(ctx);
		lgDefaultCtx.setArea(null);
		if (lgDefaultCtx.getRenderMode() == ContentContext.EDIT_MODE) {
			lgDefaultCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		}
		List<String> outLayouts = new LinkedList<String>();
		Collection<IContentVisualComponent> layoutComps = getContentByType(lgDefaultCtx, Layouts.TYPE);
		for (IContentVisualComponent comp : layoutComps) {
			Layouts layouts = (Layouts) comp;
			outLayouts.addAll(layouts.getLayouts());
		}
		if (outLayouts.size() == 0) {
			desc.layouts = Collections.emptyList();
		} else {
			desc.layouts = outLayouts;
		}
		return desc.layouts;
	}

	public ProductBean getProduct(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.product != null) {
			return desc.product;
		}
		ContentContext lgDefaultCtx = new ContentContext(ctx);
		lgDefaultCtx.setArea(null);
		if (lgDefaultCtx.getRenderMode() == ContentContext.EDIT_MODE) {
			lgDefaultCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		}
		Collection<IContentVisualComponent> productComps = getContentByImplementation(lgDefaultCtx, IProductContainer.class);
		for (IContentVisualComponent comp : productComps) {
			desc.product = ((IProductContainer) comp).getProductBean(lgDefaultCtx);
			return desc.product;
		}
		return null;
	}

	public String getTemplateId() {
		return templateId;
	}

	public String getTemplateIdOnInherited(ContentContext ctx) {
		String outTemplate = this.templateId;
		MenuElement parent = getParent();
		while (parent != null && outTemplate == null) {
			outTemplate = parent.getTemplateId();
			parent = parent.getParent();
		}
		if (outTemplate == null) {
			return ctx.getGlobalContext().getDefaultTemplate();
		}
		return outTemplate;
	}

	public Collection<MenuElement> getTemplates() {
		for (MenuElement child : getChildMenuElements()) {
			if (child.getName().equals(LAYOUTS_PREFIX + getName())) {
				return child.getChildMenuElements();
			}
		}
		return null;
	}

	private TimeRangeComponent getTimeRangeComponent(ContentContext ctx) throws Exception {
		ContentContext localCtx = ctx.getContextWithArea(null);
		ContentElementList contentList = getAllContent(localCtx);
		while (contentList.hasNext(localCtx)) {
			IContentVisualComponent comp = contentList.next(localCtx);
			if (comp instanceof TimeRangeComponent) {
				return (TimeRangeComponent) comp;
			}
		}
		return null;
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

		TimeRangeComponent comp = getTimeRangeComponent(ctx);
		if (comp != null) {
			Date startDate = comp.getStartDate(ctx);
			if (startDate == null) {
				startDate = getContentDate(ctx);
				if (startDate == null) {
					startDate = getCreationDate();
				}
			}
			Date endDate = comp.getEndDate(ctx);
			if (endDate == null) {
				endDate = startDate;
			}
			desc.timeRange = new TimeRange(startDate, endDate);
		} else {

			ContentContext ctxNoArea = ctx.getContextWithArea(null);
			ContentElementList content = getContent(ctxNoArea);
			while (content.hasNext(ctxNoArea)) {
				IContentVisualComponent c = content.next(ctxNoArea);
				if (c instanceof ITimeRange) {
					ITimeRange tr = (ITimeRange)c;
					if (tr.isTimeRangeValid(ctx)) {
						desc.timeRange = new TimeRange(ctx, tr);
						return desc.timeRange;
					}
				}
			}

			Date contentDate = getContentDate(ctx);
			if (contentDate == null) {
				contentDate = getCreationDate();
			}
			desc.timeRange = new TimeRange(contentDate, contentDate);
		}
		return desc.timeRange;
	}

	/**
	 * get the real content title.
	 * 
	 * @param ctx
	 * @return null if no title found in content.
	 * @throws Exception
	 */
	public String getContentTitle(ContentContext ctx) throws Exception {
		if (isTrash()) {
			return getLabel(ctx);
		}
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.contentTitle != null) {
			return desc.contentTitle;
		}

		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setArea(null);
		desc.contentTitle = getContent(newCtx).getTitle(ctx);

		return desc.contentTitle;
	}

	public boolean isTitle(ContentContext ctx) throws Exception {
		return !name.equals(getTitle(ctx));
	}

	public String getTitle(ContentContext ctx) throws Exception {
		if (isTrash()) {
			return getLabel(ctx);
		}
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.title != null) {
			return desc.title;
		}

		ContentContext newCtx = new ContentContext(ctx);

		newCtx.setArea(null);
		desc.title = getContent(newCtx).getTitle(newCtx);

		if (desc.title != null) {
			if ((desc.title.trim().length() == 0) && (name != null)) {
				desc.title = name;
			}
		}
		return desc.title;
	}

	/**
	 * create a title without space and special character with the title of the page
	 * in the default language.
	 * 
	 * @param ctx
	 * @return
	 */
	public String getTechnicalTitle(ContentContext ctx) {
		ContentContext defaultLangCtx = ctx.getContextForDefaultLanguage();
		String title;
		try {
			title = getPageTitle(defaultLangCtx);
		} catch (Exception e) {
			title = getName();
			e.printStackTrace();
		}
		return StringHelper.createFileName(title).toLowerCase();
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
		desc.localTitle = getLocalContent(newCtx).getLocalTitle(newCtx);
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

	public boolean isReadAccess(ContentContext ctx, User user) {
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		String token = rs.getParameter(GlobalContext.PAGE_TOKEN_PARAM, null);
		if (token != null) {
			if (getName().equals(ctx.getGlobalContext().getPageToken(token))) {
				return true;
			} else {
				logger.warning("unvalid token : " + token);
			}
		}
		if (userRoles.size() > 0) {
			if (user == null) {
				return false;
			}
			Set<String> roles = new HashSet<String>(user.getRoles());
			roles.retainAll(getUserRoles());
			if (roles.size() == 0 && !(ctx.getCurrentUser().isEditor() && AdminUserSecurity.getInstance().haveRight(ctx.getCurrentUser(), AdminUserSecurity.CONTENT_ROLE))) {
				return false;
			}
		}
		return true;
	}

	public boolean isEditAccess(ContentContext ctx) throws Exception {
		return AdminUserSecurity.canModifyPage(ctx, this, false);
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
			if (parent == this && c > 25) {
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
				logger.severe("recursive vpath in :" + getPath() + "  context : " + globalContext.getContextKey());
			} else {
				MenuElement realParent = parent;
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
					 * defaultLgCtx.setContentLanguage(globalContext. getDefaultLanguage ()); if
					 * (element.isVisible(defaultLgCtx)) { res.add(element); }
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
		return res;
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
		if (getParent() == null || parent == null) {
			return false;
		} else if (getParent().getId().equals(parent.getId())) {
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

	// TODO: change this method with a method in the component, it return is
	// date if visible of not.
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
			ContentElementList contentList = getAllContent(localContext); // search
																			// date
																			// in
																			// all
																			// area
			while (contentList.hasNext(ctx)) {
				IContentVisualComponent comp = contentList.next(ctx);
				if (comp.getType() == DateComponent.TYPE) {
					if (((DateComponent) comp).getComponentCssClass(ctx).equals(DateComponent.NOT_VISIBLE_TYPE)) {
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

	public boolean isNoComponent(ContentContext ctx) throws Exception {
		return getContent(ctx).size(ctx) == 0;
	}

	public boolean isNoComponent(ContentContext ctx, String area) throws Exception {
		ContentContext langCtx = ctx.getContextWithArea(area);
		return isNoComponent(langCtx);
	}

	public boolean isEmpty(ContentContext ctx, String area) throws Exception {
		return isEmpty(ctx, area, true);
	}

	public boolean isLocalEmpty(ContentContext ctx, String area) throws Exception {
		return isEmpty(ctx, area, false);
	}

	public boolean isEmpty(ContentContext ctx, String area, boolean withRepeat) throws Exception {
		if (ctx.getRequestContentLanguage() == null) {
			return true;
		}
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		ContentContext ctxForceArea = new ContentContext(ctx);
		ctxForceArea.setArea(area);

		boolean empty = true;

		if (isChildrenAssociation()) {
			for (MenuElement child : getChildMenuElements()) {
				if (!child.isEmpty(ctxForceArea)) {
					desc.setEmpty(area, false);
					return false;
				}
			}
		} else {
			if (withRepeat) {
				empty = !getContent(ctxForceArea).hasNext(ctxForceArea);
			} else {
				empty = !getLocalContent(ctxForceArea).hasNext(ctxForceArea);
			}
		}

		desc.setEmpty(area, empty);
		return empty;
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
			if (getName().equals(pathElems[pathElems.length - 1])) {
				return true;
			}
		}
		return false;
	}

	public boolean isMetadataEquals(MenuElement elem) {
		if (elem == null) {
			return false;
		}
		return getName().equals(elem.getName()) && visible == elem.visible && userRoles.equals(elem.userRoles) && priority == elem.priority;
	}

	public boolean isRealContentAuto(ContentContext ctx) throws Exception {
		if (ctx.getGlobalContext().isAutoSwitchToDefaultLanguage()) {
			return isRealContentAnyLanguage(ctx);
		} else {
			return isRealContent(ctx);
		}
	}

	public boolean isRealContent(ContentContext ctx) throws Exception {
		// TODO: warning test deadlock on this
		synchronized (ctx.getGlobalContext().getLockLoadContent()) {

			if (!isInsideTimeRange()) {
				return false;
			}

			if (!isActive()) {
				return false;
			}

			if (isChildrenAssociation()) {
				return true;
			}

			Template template = TemplateFactory.getTemplate(ctx, this);

			String lang = ctx.getRequestContentLanguage();
			if (template != null && template.isNavigationArea(ctx.getArea())) {
				lang = ctx.getLanguage();
			}

			PageDescription desc = getPageDescriptionCached(ctx, lang);

			if (!desc.isRealContentNull()) {
				return desc.isRealContent();
			}

			if (isEmpty(ctx)) {
				desc.realContent = false;
				return false;
			}

			ContentContext contentAreaCtx = new ContentContext(ctx);

			if (template == null || !template.isRealContentFromAnyArea()) {
				contentAreaCtx.setArea(ComponentBean.DEFAULT_AREA);
			} else {
				contentAreaCtx.setArea(null);
			}

			if (template == null || !template.isRealContentFromAnyArea()) {
				contentAreaCtx.setArea(ComponentBean.DEFAULT_AREA);
			} else {
				contentAreaCtx.setArea(null);
			}

			ContentElementList comps = getContent(contentAreaCtx);
			while (comps.hasNext(contentAreaCtx)) {
				IContentVisualComponent comp = comps.next(contentAreaCtx);
				if (comp instanceof ForceRealContent) {
					desc.realContent = !StringHelper.isTrue(comp.getValue(contentAreaCtx));
					return desc.realContent;
				}
				if (comp.isRealContent(contentAreaCtx) && !comp.isRepeat()) {
					desc.realContent = true;
					return true;
				}
			}

			// search force real content
			if (!template.isRealContentFromAnyArea()) {
				contentAreaCtx.setArea(null);
				comps = getContent(contentAreaCtx);
				while (comps.hasNext(contentAreaCtx)) {
					IContentVisualComponent comp = comps.next(contentAreaCtx);
					if (comp instanceof ForceRealContent) {
						desc.realContent = !StringHelper.isTrue(comp.getValue(contentAreaCtx));
						return desc.realContent;
					}
				}
			}

			if (isChildrenAssociation()) {
				desc.realContent = true; // added: 12/01/2017
				return true;
			}

			desc.realContent = false;

			return false;
		}
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

	public String getRealContentLanguage(ContentContext ctx) throws Exception {
		if (isRealContent(ctx)) {
			return ctx.getRequestContentLanguage();
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> lgs = globalContext.getContentLanguages();
		ContentContext lgContext = new ContentContext(ctx);
		lgContext.setArea(null); // remove the area
		for (String lg : lgs) {
			lgContext.setRequestContentLanguage(lg);
			if (isRealContent(lgContext)) {
				return lg;
			}
		}
		return null;
	}

	/*
	 * public boolean isSelected(ContentContext ctx) { String[] pathElems =
	 * ctx.getPath().split("\\/"); for (String pathElem : pathElems) { if
	 * (name.equals(pathElem)) { return true; } } return false; }
	 */

	public boolean isRemote() {
		if (getParent() != null) {
			if (getParent().getId().equals(getId())) {
				logger.severe("page '" + getName() + "' (" + getPath() + ") as him self like parent.");
			} else if (getParent().isRemote()) {
				return true;
			}
		}
		return remote;
	}

	public boolean isSelected(ContentContext ctx) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		if (currentPage == null) {
			return false;
		}
		if (this.getId().equals(currentPage.getId())) {
			return true;
		}
		while (!this.getId().equals(currentPage.getId()) && currentPage.getParent() != null) {
			currentPage = currentPage.getParent();
			if (this.getId().equals(currentPage.getId())) {
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

	public boolean isActive() {
		if (!isInsideTimeRange()) {
			return false;
		}
		if (isInTrash() || isTrash()) {
			return false;
		} else {
			MenuElement parent = getParent();
			if (parent != null && !parent.isActive()) {
				return false;
			}
			return active;
		}
	}

	public boolean isPageActive() {
		return this.active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public boolean isVisible(ContentContext ctx) throws Exception {
		if (!visible) {
			return false;
		} else {
			if (!isActive()) {
				return false;
			}
			if (isAdmin() && ctx.getCurrentEditUser() == null) {
				return false;
			}

			if (ctx.getGlobalContext().isCollaborativeMode() && ctx.getCurrentEditUser() != null) {
				if (getEditorRoles().size() > 0 && !ctx.getCurrentEditUser().validForRoles(getEditorRoles())) {
					return false;
				}
			}

			/*ContentContext contentAreaCtx = new ContentContext(ctx);
			contentAreaCtx.setArea(ComponentBean.DEFAULT_AREA);
			ContentElementList content = this.getContent(contentAreaCtx);
			while (content.hasNext(contentAreaCtx)) {
				if (content.next(contentAreaCtx).isRealContent(contentAreaCtx)) {
					return isInsideTimeRange();
				}
			}*/

			if (isRealContent(ctx)) {
				return isInsideTimeRange();
			}

			if (ctx.getGlobalContext().isAutoSwitchToDefaultLanguage()) {
				for (String lg : ctx.getGlobalContext().getDefaultLanguages()) {
					ContentContext lgCtx = ctx.getContextWidthOtherRequestLanguage(lg);
					ContentElementList contentList = this.getContent(lgCtx);
					while (contentList.hasNext(lgCtx)) {
						if (contentList.next(lgCtx).isRealContent(lgCtx)) {
							return isInsideTimeRange();
						}
					}
				}
			}

			ContentContext contentAreaCtx = new ContentContext(ctx);
			contentAreaCtx.setArea(ComponentBean.DEFAULT_AREA);
			for (MenuElement child : getAllChildrenList()) {
				ContentElementList content = child.getContent(ctx);
				while (content.hasNext(contentAreaCtx)) {
					if (content.next(contentAreaCtx).isRealContent(contentAreaCtx)) {
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
	public final String prepareAddContent(String lg, String parentCompId, String contentType, String style, String value, User authors) throws Exception {
		ComponentBean comp = new ComponentBean(StringHelper.getRandomId(), contentType, value, lg, false, authors);
		if (style != null) {
			comp.setStyle(style);
		}
		if (contentToBeAdded.keySet().contains(parentCompId)) {
			logger.warning("parent id : '" + parentCompId + "' all ready found is parent id list.");
		} else {
			if (contentToBeAdded == Collections.EMPTY_MAP) {
				contentToBeAdded = new HashMap<String, ComponentBean>();
			}
			contentToBeAdded.put(parentCompId, comp);
		}
		return comp.getId();
	}

	public void releaseCache() {
		releaseCache = true;
		getContentElementListMap().clear();
		getLocalContentElementListMap().clear();
		for (MenuElement child : getChildMenuElements()) {
			child.releaseCache();
		}
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
		ClipBoard clipBoard = ClipBoard.getInstance(ctx.getRequest());
		if (id.equals(clipBoard.getCopied())) {
			clipBoard.clear();
		}
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
				if (componentBean[i].getId().equals(id)) {
					IContentVisualComponent comp = ComponentFactory.getComponentWithType(ctx, componentBean[i].getType());
					if (comp != null) {
						((AbstractVisualComponent) comp).setComponentBean(componentBean[i]);
						((AbstractVisualComponent) comp).setPage(this);
						comp.delete(ctx);
						type = comp.getType();
					} else {
						logger.warning("comp type not found : " + componentBean[i].getType());
					}
				}
			}
			for (int i = 0; i < componentBean.length; i++) {
				if (!componentBean[i].getId().equals(id)) {
					if (!delete) {
						outList.add(componentBean[i]);
					}
				}
			}
			componentBean = new ComponentBean[outList.size()];
			outList.toArray(componentBean);
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
	 * public boolean needJavaScript(ContentContext ctx) throws Exception {
	 * PageDescription desc =
	 * getPageDescriptionCached(ctx,ctx.getRequestContentLanguage()); if
	 * (desc.needJavaScript != null) { return desc.needJavaScript; }
	 * 
	 * ContentElementList content = getAllContent(ctx); while (content.hasNext()) {
	 * IContentVisualComponent comp = content.next(); if (comp.needJavaScript()) {
	 * desc.needJavaScript = true; return true; } } desc.needJavaScript = false;
	 * return false; }
	 */

	public MenuElement searchChild(ContentContext ctx) throws Exception {
		return searchChild(ctx, ctx.getPath());
	}

	public MenuElement searchChild(ContentContext ctx, String path) throws Exception {
		if (getParent() == null) {
			if (path.equals("/")) {
				return this;
			}
		}
		// if (path.equals("/")) {
		// return this;
		// } else {
		// Collection<MenuElement> pastNode = new LinkedList<MenuElement>();
		// return searchChild(this, ctx, path, pastNode);
		// }
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		MenuElement page = globalContext.getPageIfExist(ctx, path, false);
		return page;
	}

	/**
	 * @param id
	 * @return
	 * @Deprecated use NavigationService.getPage(ContentContext, pageKey)
	 */
	public MenuElement searchChildFromId(String id) {
		if ((id == null) || (id.equals("0"))) {
			// return this;
			return null;
		} else {
			try {
				for (MenuElement child : getAllChildrenList()) {
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
	 * @param names
	 *            list of possible name
	 * @return
	 * @Deprecated use NavigationService.getPage(ContentContext, pageKey)
	 */
	public MenuElement searchChildFromName(String... names) {
		if (names == null || names.length == 0) {
			return null;
		}
		for (int i = 0; i < names.length; i++) {
			names[i] = StringHelper.createFileName(names[i]);
			MenuElement cachedPage = getPageCached("name-" + names[i]);
			if (cachedPage != null) {
				if (cachedPage == NO_PAGE) {
					return null;
				} else {
					return cachedPage;
				}
			}
		}
		for (String name : names) {
			if (name.equals(this.getName())) {
				return this;
			}
		}
		MenuElement page = searchChildFromName(this, names);
		if (page != null) {
			setPageCached("name-" + page.getName(), page);
		} else {
			if (useCache) {
				for (String name : names) {
					setPageCached("name-" + name, NO_PAGE);
				}
			}
		}
		return page;
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
			if (StringHelper.isEmpty(latestEditor)) {
				latestEditor = creator;
			}
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
		this.latestEditor = latestEditor;
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
		this.modificationDate = modificationDate;
	}

	/*
	 * public int getLastAccess(ContentContext ctx) throws ServiceException { String
	 * accessPageKey = "access-page-" + ctx.getLanguage(); Integer lastMonthAccess =
	 * (Integer) pageTimeCache.get(accessPageKey); if (lastMonthAccess == null) {
	 * synchronized (pageTimeCache) { Tracker tracker =
	 * Tracker.getTracker(ctx.getRequest().getSession()); StaticConfig staticConfig
	 * = StaticConfig.getInstance(ctx.getRequest().getSession()); int
	 * outLastMonthAccess =
	 * tracker.getPathCountAccess(staticConfig.getLastAccessPage(),
	 * URLHelper.mergePath(ctx.getLanguage(), getPath()));
	 * pageTimeCache.put(accessPageKey, new Integer(outLastMonthAccess)); return
	 * outLastMonthAccess; } } else { return lastMonthAccess; } }
	 */

	public void setName(String name) {
		this.name = name;
		nameKey = null;
		releaseCache();
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

	public void setTemplateId(String inTemplate) {
		synchronized (getLock()) {
			templateId = inTemplate;
		}
	}

	/**
	 * @param strings
	 */
	public void setUserRoles(Set<String> roles) {
		userRoles = roles;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
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
		synchronized (getLock()) {
			releaseCache();
			visible = b;
		}
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

	public boolean notInSearch(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.notInSearch != null) {
			return desc.notInSearch;
		}

		desc.notInSearch = false;
		ContentContext noAreaCtx = new ContentContext(ctx);
		noAreaCtx.setArea(null);
		if (getContentByType(noAreaCtx, NotSearchPage.TYPE).size() > 0 || !isActive(ctx)) {
			desc.notInSearch = true;
		}

		return desc.notInSearch;
	}

	/**
	 * get local i18n data
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> getI18n(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.i18n == null) {
			ContentContext noAreaCtx = new ContentContext(ctx);
			noAreaCtx.setArea(null);
			List<IContentVisualComponent> content = getContentByType(noAreaCtx, I18nComponent.TYPE);
			for (IContentVisualComponent i18nComp : content) {
				Properties prop = new Properties();
				prop.load(new StringReader(i18nComp.getValue(noAreaCtx)));
				desc.i18n = new HashedMap(prop);
			}
			if (desc.i18n == null) {
				desc.i18n = Collections.EMPTY_MAP;
			}
		}

		return desc.i18n;
	}

	/**
	 * get local i18n data
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public ContactBean getContact(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.contactBean == null) {
			ContentContext noAreaCtx = new ContentContext(ctx);
			noAreaCtx.setArea(null);
			List<IContentVisualComponent> content = getContentByType(noAreaCtx, ContactInformation.TYPE);
			desc.contactBean = ContactBean.EMPTY_CONTACT_BEAN;
			for (IContentVisualComponent comp : content) {
				ContactInformation contactComp = (ContactInformation) comp;
				desc.contactBean = contactComp.getContactBean();
			}
		}
		if (desc.contactBean == ContactBean.EMPTY_CONTACT_BEAN) {
			return null;
		} else {
			return desc.contactBean;
		}
	}

	/**
	 * check if the page is cacheable (static content)
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public boolean isCacheable(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.cacheable != null) {
			return desc.cacheable;
		}
		ContentContext noAreaCtx = new ContentContext(ctx);
		noAreaCtx.setArea(null);
		ContentElementList content = getContent(noAreaCtx);
		while (content.hasNext(noAreaCtx)) {
			IContentVisualComponent comp = content.next(noAreaCtx);
			if (!comp.isContentCachable(noAreaCtx)) {
				desc.cacheable = false;
				return false;
			}
		}
		desc.cacheable = true;
		return true;
	}

	public IContentVisualComponent getNotCacheableComponent(ContentContext ctx) throws Exception {
		ContentContext noAreaCtx = new ContentContext(ctx);
		noAreaCtx.setArea(null);
		ContentElementList content = getContent(noAreaCtx);
		while (content.hasNext(noAreaCtx)) {
			IContentVisualComponent comp = content.next(noAreaCtx);
			if (!comp.isContentCachable(noAreaCtx)) {
				return comp;
			}
		}
		return null;
	}

	public IContentVisualComponent getRealContentComponent(ContentContext ctx) throws Exception {
		Template template = TemplateFactory.getTemplate(ctx, this);

		ContentContext contentAreaCtx = new ContentContext(ctx);
		if (template == null || !template.isRealContentFromAnyArea()) {
			contentAreaCtx.setArea(ComponentBean.DEFAULT_AREA);
		} else {
			contentAreaCtx.setArea(null);
		}

		ContentElementList comps = getContent(contentAreaCtx);
		while (comps.hasNext(contentAreaCtx)) {
			IContentVisualComponent comp = comps.next(contentAreaCtx);
			if (comp instanceof ForceRealContent) {
				if (StringHelper.isTrue(comp.getValue(contentAreaCtx))) {
					return comp;
				}
				;
			}
			if (comp.isRealContent(contentAreaCtx) && !comp.isRepeat()) {
				return comp;
			}
		}

		return null;
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
		return getShortURL(ctx, true);
	}

	public String getShortURL(ContentContext ctx, boolean releaseCache) throws Exception {
		if (shortURL == null) {
			HashSet<String> shortURLs = new HashSet<String>();
			MenuElement root = getRoot();
			if (root.isShortURL()) {
				shortURLs.add(root.getShortURL(ctx));
			}
			for (MenuElement child : root.getAllChildrenList()) {
				if (child.isShortURL()) {
					shortURLs.add(child.getShortURL(ctx).substring(1));
				}
			}
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			this.shortURL = 'U' + StringHelper.createKey(globalContext.getStaticConfig().getShortURLSize(), shortURLs);
			if (releaseCache) {
				ContentService.getInstance(ctx.getGlobalContext()).releaseShortUrlMap(globalContext);
			}
			PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		}
		return shortURL;
	}

	public String getShortLanguageURL(ContentContext ctx) throws Exception {
		String shortURL = getShortURL(ctx);
		if (shortURL != null && shortURL.length() > 1) {
			shortURL = "L" + ctx.getRequestContentLanguage() + shortURL.substring(1);
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

	boolean isInsideTimeRange() {
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSharedName() {
		return sharedName;
	}

	public void setSharedName(String sharedName) {
		this.sharedName = sharedName;
	}

	public boolean isChildrenAssociation() {
		return childrenAssociation;
	}

	public boolean isRootChildrenAssociation() {
		if (getChildMenuElements().size() > 0) {
			return getChildMenuElements().iterator().next().isChildrenAssociation();
		}
		return false;
	}

	/**
	 * check if this page is a part of page association
	 * 
	 * @return
	 */
	public boolean isChildrenOfAssociation() {
		MenuElement root = getChildrenAssociationPage();
		if (root != null) {
			String rootId = root.getId();
			MenuElement parent = getParent();
			while (parent != null) {
				if (parent.getId().equals(rootId)) {
					return true;
				}
				parent = parent.getParent();
			}
		}
		return false;
	}

	/**
	 * check if this page is a direct children of page association
	 * 
	 * @return
	 */
	public boolean isDirectChildrenOfAssociation() {
		MenuElement root = getChildrenAssociationPage();
		if (root != null) {
			String rootId = root.getId();
			MenuElement parent = getParent();
			if (parent != null) {
				if (parent.getId().equals(rootId)) {
					return true;
				}
				parent = parent.getParent();
			}
		}
		return false;
	}

	/**
	 * get the children association page of the current page.
	 * 
	 * @return
	 */
	public MenuElement getChildrenAssociationPage() {
		MenuElement parent = getParent();
		while (parent != null) {
			if (parent.isChildrenAssociation()) {
				return parent;
			}
			parent = parent.getParent();
		}
		return null;
	}

	/**
	 * get the root (parent) of the children association page
	 * 
	 * @return
	 */
	public MenuElement getRootOfChildrenAssociation() {
		MenuElement parent = this;
		while (parent != null) {
			for (MenuElement child : parent.getChildMenuElements()) {
				if (child.isChildrenAssociation()) {
					return parent;
				}
			}
			parent = parent.getParent();
		}
		return null;
	}

	/**
	 * get the page marked as children association
	 * 
	 * @return
	 */
	public MenuElement getMainChildrenAssociation() {
		MenuElement parent = this;
		while (parent != null) {
			for (MenuElement child : parent.getChildMenuElements()) {
				if (child.isChildrenAssociation()) {
					return child;
				}
			}
			parent = parent.getParent();
		}
		return null;
	}

	public void setChildrenAssociation(boolean childrenAssociation) {
		this.childrenAssociation = childrenAssociation;
		releaseCache();
	}

	public MenuElement getFirstChild() {
		if (childMenuElements.size() > 0) {
			return childMenuElements.get(0);
		} else {
			return null;
		}
	}

	public boolean equals(ContentContext ctx, MenuElement page) {
		if (page == null) {
			return false;
		}
		try {
			if (isVisible() != page.isVisible()) {
				return false;
			}
			if (!getName().equals(page.getName())) {
				return false;
			}
			if (isBreakRepeat() != page.isBreakRepeat()) {
				return false;
			}
			if (isBlocked() != page.isBlocked()) {
				return false;
			}
			if (getStartPublishDate() != page.getStartPublishDate() || (getStartPublishDate() != null && !getStartPublishDate().equals(page.getStartPublishDate()))) {
				return false;
			}
			if (getEndPublishDate() != page.getEndPublishDate() || (getEndPublishDate() != null && !getEndPublishDate().equals(page.getEndPublishDate()))) {
				return false;
			}
			ContentContext noAreaCtx = ctx.getContextWithArea(null);
			noAreaCtx.setComponentCache(false);
			List<IContentVisualComponent> localComponents = new LinkedList<IContentVisualComponent>();
			for (IContentVisualComponent comp : getContent(noAreaCtx).getIterable(noAreaCtx)) {
				localComponents.add(comp);
			}
			List<IContentVisualComponent> pageComponents = new LinkedList<IContentVisualComponent>();
			for (IContentVisualComponent comp : page.getContent(noAreaCtx).getIterable(noAreaCtx)) {
				pageComponents.add(comp);
			}

			if (localComponents.size() != pageComponents.size()) {
				return false;
			}
			for (int i = 0; i < localComponents.size(); i++) {
				if (!localComponents.get(i).equals(pageComponents.get(i))) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean equals(ContentContext ctx, MenuElement page, boolean withChildren) {
		if (page == null) {
			return false;
		}
		if (!equals(ctx, page) || childMenuElements.size() != page.childMenuElements.size()) {
			return false;
		}
		if (withChildren) {
			for (int i = 0; i < childMenuElements.size(); i++) {
				if (!childMenuElements.get(i).equals(ctx, page.childMenuElements.get(i), true)) {
					return false;
				}
			}
		}
		return true;
	}

	public void copyChildren(MenuElement page) {
		childMenuElements = page.childMenuElements;
	}

	@Override
	public void printInfo(ContentContext ctx, PrintStream out) {
		out.println("****");
		out.println("**** MenuElement : " + getPath());
		out.println("****");
		out.println("**** name                        : " + getName());
		out.println("**** #componentBean              : " + componentBean.length);
		out.println("**** #contentElementListMap      : " + contentElementListMap.size());
		out.println("**** #localContentElementListMap : " + localContentElementListMap.size());
		out.println("****");
	}

	/**
	 * in case of same URL, this value in incremented. all URL factory
	 * (implementation of @IURLFactory) can use this number for create different
	 * URL.
	 * 
	 * @return
	 */
	public int getUrlNumber() {
		return urlNumber;
	}

	public void setUrlNumber(int urlNumber) {
		this.urlNumber = urlNumber;
	}

	private IEventRegistration getEventRegistration(ContentContext ctx) throws Exception {
		ContentContext noAreaCtx = ctx.getContextWithArea(null);
		ContentElementList content = getContent(noAreaCtx);
		while (content.hasNext(noAreaCtx)) {
			IContentVisualComponent comp = content.next(noAreaCtx);
			if (comp instanceof IEventRegistration) {
				return (IEventRegistration) comp;
			}
		}
		return null;
	}

	/**
	 * get event if menu element contains event info.
	 * 
	 * @param ctx
	 * @return a event, null if this page does'nt contains event information
	 * @throws Exception
	 */
	public Event getEvent(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.event != null) {
			if (desc.event == Event.NO_EVENT) {
				return null;
			} else {
				return desc.event;
			}
		} else {
			ContentContext noAreaCtx = ctx.getContextWithArea(null);
			ContentElementList content = getContent(noAreaCtx);
			IEventRegistration eventRegistration = getEventRegistration(noAreaCtx);
			while (content.hasNext(noAreaCtx)) {
				IContentVisualComponent comp = content.next(noAreaCtx);
				if (comp instanceof EventDefinitionComponent) {
					EventDefinitionComponent eventComp = (EventDefinitionComponent) comp;
					String description = null;
					if (getDescription(ctx) != null) {
						description = getDescription(ctx).getText();
					}
					Event event = new Event(ctx, getName(), eventComp.getId(), eventComp.getStartDate(ctx), eventComp.getEndDate(ctx), getTitle(ctx), description, getImage(noAreaCtx));
					event.setCategory(getCategory(ctx));
					event.setLocation(getLocation(ctx));
					event.setUrl(new URL(URLHelper.createURL(ctx.getContextForAbsoluteURL(), this)));
					event.setUser(getCreator());
					event.setEventRegistration(eventRegistration);
					desc.event = event;
					return desc.event;
				}
			}
		}
		desc.event = Event.NO_EVENT;
		return null;
	}

	public String getAreaClass(ContentContext ctx, String area) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		String clazz = desc.areaAsMap.get(area);
		if (clazz == null || ctx.isAsModifyMode()) {
			ContentContext ctxArea = ctx.getContextWithArea(area);
			ContentElementList content = getContent(ctx);
			Boolean firstContainer = null;
			boolean containContainer = false;
			while (content.hasNext(ctxArea)) {
				IContentVisualComponent comp = content.next(ctxArea);
				if (comp instanceof IContainer) {
					if (firstContainer == null) {
						firstContainer = true;
					}
					containContainer = true;
				} else if (firstContainer == null) {
					firstContainer = false;
				}
			}
			clazz = "";
			if (firstContainer != null && firstContainer) {
				clazz = "area-contain-container area-contain-container-first";
			} else if (containContainer) {
				clazz = "area-contain-container area-contain-container-not-first";
			}

			List<String> compTypes = getComponentTypes(ctx).get(area);
			if (compTypes != null) {
				clazz += ' ' + "_cp_type_size_" + compTypes.size();
				clazz += ' ' + "_cp_size_" + getContent(ctxArea).size(ctxArea);
				if (compTypes.size() > 0) {
					clazz += ' ' + "_cp_first_-" + compTypes.get(0);
				}
				for (String type : compTypes) {
					clazz += ' ' + "_cp-" + type;
				}
			}

			desc.areaAsMap.put(area, clazz);
		}
		return clazz;
	}

	/**
	 * get event if menu element contains event info.
	 * 
	 * @param ctx
	 * @return a event, null if this page does'nt contains event information
	 * @throws Exception
	 */
	public List<Event> getEvents(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.events != null) {
			if (desc.events == Collections.EMPTY_LIST) {
				return null;
			} else {
				return desc.events;
			}
		} else {
			ContentContext noAreaCtx = ctx.getContextWithArea(null);
			ContentElementList content = getContent(noAreaCtx);
			List<Event> events = new LinkedList<Event>();
			IEventRegistration eventRegistration = getEventRegistration(noAreaCtx);
			while (content.hasNext(noAreaCtx)) {
				IContentVisualComponent comp = content.next(noAreaCtx);
				if (comp instanceof EventDefinitionComponent) {
					EventDefinitionComponent eventComp = (EventDefinitionComponent) comp;
					String description = null;
					if (getDescription(ctx) != null) {
						description = getDescription(ctx).getText();
					}
					Event event = new Event(ctx, getName(), eventComp.getId(), eventComp.getStartDate(ctx), eventComp.getEndDate(ctx), getTitle(ctx), description, getImage(noAreaCtx));
					event.setCategory(getCategory(ctx));
					event.setLocation(getLocation(ctx));
					event.setUrl(new URL(URLHelper.createURL(ctx.getContextForAbsoluteURL(), this)));
					event.setUser(getCreator());
					event.setEventRegistration(eventRegistration);
					events.add(event);
				}
			}
			if (events.size() > 0) {
				return events;
			}
		}

		desc.events = Collections.EMPTY_LIST;
		return null;
	}

	public boolean isEditabled(ContentContext ctx) throws Exception {
		return Edit.checkPageSecurity(ctx, parent);
	}

	/**
	 * is a page with reference to other page, like page reference of children link
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public boolean isReference(ContentContext ctx) throws Exception {
		ContentElementList content = getLocalContent(ctx);
		while (content.hasNext(ctx)) {
			IContentVisualComponent comp = content.next(ctx);
			if (comp.getType().equals(ChildrenLink.TYPE) || comp.getType().equals(PageReferenceComponent.TYPE)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Map<String, Object> getContentAsMap(ContentContext ctx) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, Exception {

		if (!isReadAccess(ctx, ctx.getCurrentUser())) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("blocked", "no read access for : " + ctx.getCurrentUser());
			return map;
		}

		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());

		if (desc.contentAsMap != null) {
			return desc.contentAsMap;
		}

		RequestService rs = RequestService.getInstance(ctx.getRequest());
		String lgParam = rs.getParameter("lg", null);
		Collection<String> langs = ctx.getGlobalContext().getContentLanguages();
		if (lgParam != null) {
			ctx.setAllLanguage(lgParam);
			langs = new LinkedList<String>(Arrays.asList(new String[] { lgParam }));
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", getId());
		if (lgParam != null) {
			map.putAll(BeanHelper.bean2Map(getPageDescription(ctx)));
			map.put("realContent", isRealContent(ctx));
		} else {
			map.put("type", getType());
			map.put("realContentAnyLanguage", isRealContentAnyLanguage(ctx));
		}
		map.put("path", getPath());
		map.put("visible", isVisible());
		map.put("active", isActive());
		map.put("editorRoles", getEditorRoles());
		map.put("userRoles", getUserRoles());
		map.put("sharedName", getSharedName());
		if (getStartPublishDate() != null) {
			map.put("startPublishDate", StringHelper.renderSortableTime(getStartPublishDate()));
		}
		if (getEndPublishDate() != null) {
			map.put("endPublishDate", StringHelper.renderSortableTime(getEndPublishDate()));
		}
		map.put("creationDate", StringHelper.renderSortableTime(getCreationDate()));
		map.put("creator", getCreator());
		map.put("latestEditor", getLatestEditor());

		List<Map<String, Object>> contentArray = new LinkedList<Map<String, Object>>();
		Map<String, Object> dynamicComponent = new HashMap<String, Object>();
		for (String lg : langs) {
			ContentContext langCtx = ctx.getContextWidthOtherRequestLanguage(lg);
			langCtx.setArea(null);
			ContentElementList content = getLocalContent(langCtx);
			while (content.hasNext(langCtx)) {
				IContentVisualComponent comp = content.next(langCtx);
				if (comp instanceof DynamicComponent) {
					if (dynamicComponent.get(comp.getType()) == null || lg.equals(ctx.getRequestContentLanguage())) {
						DynamicComponent dynComp = (DynamicComponent) comp;
						dynamicComponent.put(dynComp.getType(), dynComp.getContentAsMap(ctx));
					}
				}
				contentArray.add(comp.getContentAsMap(langCtx));
			}
		}
		map.put("content", contentArray);
		map.put("dynamicComponent", dynamicComponent); // shortcut to access directly to data of a dynamicComponent
		if (isRestWidthChildren()) {
			Collection<MenuElement> childrenMenuElement = getChildMenuElements();
			List<Map<String, Object>> childrenArray = new LinkedList<Map<String, Object>>();
			for (MenuElement child : childrenMenuElement) {
				childrenArray.add(child.getContentAsMap(ctx));
			}
			map.put("children", childrenArray);
		}
		desc.contentAsMap = map;
		return map;
	}

	public boolean isRestWidthChildren() {
		return restWidthChildren;
	}

	public void setRestWidthChildren(boolean restWidthChildren) {
		this.restWidthChildren = restWidthChildren;
	}

	public String getSavedParent() {
		return savedParent;
	}

	public void setSavedParent(String freeData) {
		this.savedParent = freeData;
	}

	public boolean isTrash() {
		return ContentService.TRASH_PAGE_NAME.equals(getName());
	}

	public boolean isInTrash() {
		MenuElement parent = getParent();
		while (parent != null) {
			if (parent.isTrash()) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}

	public boolean isLayout() {
		/*
		 * MenuElement parent = getParent(); if (parent != null &&
		 * getName().equals(LAYOUTS_PREFIX + parent.getName())) { return true; } else {
		 * return false; }
		 */
		return isModel();
	}

	public int getFinalSeoWeight() {
		if (isLayout()) {
			return SEO_HEIGHT_NULL;
		}
		if (getSeoWeight() == MenuElement.SEO_HEIGHT_INHERITED) {
			MenuElement parent = getParent();
			if (parent == null) {
				return SEO_HEIGHT_NORMAL;
			} else {
				return parent.getFinalSeoWeight();
			}
		} else {
			return getSeoWeight();
		}
	}

	public boolean isNoIndex() {
		return getFinalSeoWeight() == SEO_HEIGHT_NULL;
	}

	public int getSeoWeight() {
		return seoWeight;
	}

	public void setSeoWeight(int searchEngineWeight) {
		this.seoWeight = searchEngineWeight;
	}

	/**
	 * check if page need a specific template.
	 * 
	 * @param templateId
	 * @return
	 * @throws Exception
	 */
	public boolean needTemplate(ContentContext ctx, String templateId) throws Exception {
		Template template = TemplateFactory.getTemplate(ctx, this);
		while (template != null) {
			if (template.getId().equals(templateId)) {
				return true;
			}
			template = template.getParent();
		}
		return false;
	}

	public boolean isModel() {
		return model;
	}

	public void setModel(boolean model) {
		this.model = model;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getIpSecurityErrorPageName() {
		return ipSecurityErrorPageName;
	}

	public void setIpSecurityErrorPageName(String ipSecurity) {
		this.ipSecurityErrorPageName = ipSecurity;
	}

	public boolean isNeedValidation() {
		return needValidation;
	}

	public void setNeedValidation(boolean needValidation) {
		this.needValidation = needValidation;
	}

	public boolean isNoValidation() {
		return noValidation;
	}

	public void setNoValidation(boolean noValidation) {
		this.noValidation = noValidation;
	}

	/**
	 * get list of component type by area
	 * 
	 * @param ctx
	 * @return map of list with area as key
	 * @throws Exception
	 */
	public Map<String, List<String>> getComponentTypes(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.componentTypes == null) {
			Map<String, List<String>> out = new NeverEmptyMap<>(LinkedList.class);
			ContentContext noAreaCtx = ctx.getContextWithArea(null);
			ContentElementList content = getContent(noAreaCtx);
			while (content.hasNext(noAreaCtx)) {
				IContentVisualComponent comp = content.next(noAreaCtx);
				if (!out.get(comp.getArea()).contains(comp.getType())) {
					out.get(comp.getArea()).add(comp.getType());
				}
			}
			desc.componentTypes = out;
		}
		return desc.componentTypes;
	}

	@Override
	public Set<String> getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(Set<String> taxonomy) {
		this.taxonomy = taxonomy;
	}

	public boolean isPublic(ContentContext ctx) {
		boolean outPublic = getName().equals("registration") || getName().startsWith("pb_");
		MenuElement parent = getParent();
		while (!outPublic && parent != null) {
			outPublic = parent.isPublic(ctx);
			parent = parent.getParent();
		}
		return outPublic;
	}

	public IContentVisualComponent getComponent(ContentContext ctx, String id) throws Exception {
		return getLocalContent(ctx).getComponent(id);
	}

	public String getHtmlId(ContentContext ctx) {
		return "page_" + getName();
	}

	public String getHtmlSectionId(ContentContext ctx) {
		return "section_" + getHtmlId(ctx);
	}

	public String getPDFLayout(ContentContext ctx) throws Exception {
		String pdfData = ctx.getGlobalContext().getStaticConfig().getDefaultPDFLayout();
		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setArea(null);
		if (newCtx.getRenderMode() == ContentContext.EDIT_MODE) {
			newCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		}
		IContentComponentsList contentList = getAllContent(newCtx);
		while (contentList.hasNext(newCtx)) {
			IContentVisualComponent comp = contentList.next(newCtx);
			if (comp.getType().equals(PDFLayoutComponent.TYPE)) {
				pdfData = comp.getValue(newCtx);
			}
		}
		InfoBean.updateInfoBean(newCtx);
		pdfData = XHTMLHelper.replaceJSTLData(newCtx, pdfData);
		return pdfData;
	}

	public String getCss(ContentContext ctx) throws Exception {
		PageDescription desc = getPageDescriptionCached(ctx, ctx.getRequestContentLanguage());
		if (desc.css == null) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			ContentContext ctxAllArea = ctx.getContextWithArea(null);
			ContentElementList content = getContent(ctx);
			while (content.hasNext(ctxAllArea)) {
				IContentVisualComponent comp = content.next(ctx);
				if (comp instanceof PageCss) {
					String scss = comp.getValue(ctx);
					if (scss == null || scss.length() == 0) {
						desc.css = "";
					} else {
						try {
							out.println(StringHelper.neverNull(XHTMLHelper.compileScss(scss)));
						} catch (Exception e) {
							e.printStackTrace();
							MessageRepository messageRepository = MessageRepository.getInstance(ctx);
							messageRepository.setGlobalMessage(new GenericMessage(e.getMessage(), GenericMessage.ERROR));
						}
					}
				}
			}
			out.close();
			desc.css = new String(outStream.toByteArray());
		}
		return desc.css;
	}

	/**
	 * the content of this page is only for this language (use for import data only)
	 * 
	 * @return a language code
	 */
	public String getContentLanguage() {
		return contentLanguage;
	}

	public void setContentLanguage(String contentLanguage) {
		this.contentLanguage = contentLanguage;
	}

	/**
	 * delete components
	 * 
	 * @param lang
	 *            the lang to be deleted (null if all lang)
	 */
	public void deleteComponent(String lang) {
		if (lang == null) {
			componentBean = new ComponentBean[0];
		} else {
			ArrayList<ComponentBean> newList = new ArrayList<>();
			for (ComponentBean bean : componentBean) {
				if (!bean.getLanguage().equalsIgnoreCase(lang)) {
					newList.add(bean);
				}
			}
			componentBean = newList.toArray(new ComponentBean[0]);
		}
		releaseCache();
	}

}
