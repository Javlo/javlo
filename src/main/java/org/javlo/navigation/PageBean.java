package org.javlo.navigation;

import com.beust.jcommander.ParameterException;
import org.javlo.component.container.ISection;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.image.ImageBean;
import org.javlo.component.meta.ContactBean;
import org.javlo.context.ContentContext;
import org.javlo.data.taxonomy.TaxonomyDisplayBean;
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.image.ExtendedColor;
import org.javlo.module.content.Edit;
import org.javlo.service.ContentService;
import org.javlo.service.exception.ServiceException;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.tracking.Tracker;
import org.javlo.user.AdminUserSecurity;
import org.javlo.utils.CollectionAsMap;
import org.javlo.utils.HtmlPart;
import org.javlo.utils.TimeRange;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;

/**
 * bean for the page, can be use in JSTL.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class PageBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/*
	 * private PageDescription info; private String url; private String path;
	 * private boolean selected = false; private boolean lastSelected = false;
	 * private final List<PageBean> children = new LinkedList<PageBean>(); private
	 * final List<PageBean> realChildren = new LinkedList<PageBean>(); private
	 * String name = null; private String id = null; private String latestEditor;
	 * private String creationDate; private String modificationDate; private String
	 * templateId = null; private boolean realContent = false; private Map<String,
	 * String> roles = new HashMap<String, String>(); private Map<String, String>
	 * adminRoles = new HashMap<String, String>();
	 */
	
	public PageBean(ContentContext ctx, MenuElement page) {
		if (page == null) {
			throw new ParameterException("page can not be null");
		}
		this.ctx = ctx;
		this.page = page;
		this.portail = ctx.getGlobalContext().isPortail();
	}

	private final MenuElement page;
	private ContentContext ctx;
	private boolean portail = false;
	private Map<String,String> types = null;

	public PageDescription getInfo() {
		try {
			return page.getSmartPageDescription(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public PageBean getParent() {
		MenuElement parent = page.getParent();
		if (parent == null) {
			return null;
		} else {
			return new PageBean(ctx, parent);
		}
	}
	
	public PageBean getParentWidthContent() throws Exception {
		MenuElement parent = page.getParent();
		while (parent != null) {
			if (parent.isRealContent(ctx)) {
				return new PageBean(ctx, parent);
			}
			parent = parent.getParent();
		}
		return null;
	}

	public List<PageBean> getParents() {
		List<PageBean> outParents = new LinkedList<PageBean>();
		MenuElement parent = page.getParent();
		while (parent != null) {
			outParents.add(0, new PageBean(ctx, parent));
			parent = parent.getParent();
		}
		return outParents;
	}

	public String getUrl() {
		return URLHelper.createURL(ctx, page);
	}

	private String getRealContentUrl() throws Exception {
		return getRealContentUrl(this);
	}

	/**
	 * search child with real content and create URL.
	 * @return
	 */
	private String getRealContentUrl(PageBean inPage) throws Exception {
		if (inPage.getPage().isRealContent(ctx)) {
			return inPage.getUrl();
		} else {
			for (PageBean child : inPage.getChildren()) {
				String url =  getRealContentUrl(child);
				if (url != null) {
					return url;
				}
			}
		}
		return null;
	}

	public String getPreviewUrl() {		
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), page);
	}
	
	public String getViewUrl() {		
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), page);
	}

	public String getPdfUrl() {
		ContentContext pdfCtx = ctx.getFreeContentContext();
		pdfCtx.setFormat("pdf");
		return URLHelper.createURL(pdfCtx);
	}

	public String getPdfAbsoluteUrl() {
		ContentContext pdfCtx = ctx.getFreeContentContext();
		pdfCtx.setFormat("pdf");
		pdfCtx.setAbsoluteURL(true);
		return URLHelper.createURL(pdfCtx, page.getPath());
	}

	public String getAbsoluteUrl() {
		return URLHelper.createURL(ctx.getContextForAbsoluteURL(), page.getPath());
	}

	public String getAbsoluteUrlEncoded() throws UnsupportedEncodingException {
		return java.net.URLEncoder.encode(getAbsoluteUrl(), "UTF-8");
	}

	public String getAbsolutePreviewUrl() {
		return URLHelper.createAbsoluteURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), page.getPath());
	}

	public String getEditUrl() {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE), page);
	}

	public String getCreator() {
		return page.getCreator();
	}

	public boolean isSelected() {
		try {
			return page.isSelected(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public String getLayout() throws Exception {
		if (page.getLayouts(ctx) == null || page.getLayouts(ctx).size() == 0) {
			return null;
		} else {
			return page.getLayouts(ctx).get(0);
		}
	}
	
	
	public List<String> getLayouts() throws Exception {
		return page.getLayouts(ctx);
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
	 * get the list of children with isActive() true if no edition.
	 * 
	 * @return
	 */
	public List<PageBean> getChildren() {
		List<PageBean> childrenBean = new LinkedList<PageBean>();
		List<MenuElement> children = page.getChildMenuElementsList();
		for (MenuElement child : children) {
			try {
				if (child.isActive() || ctx.isEdition()) {
					childrenBean.add(child.getPageBean(ctx));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return childrenBean;
	}

	public List<PageBean> getVisibleChildrenList() {
		List<PageBean> childrenBean = new LinkedList<PageBean>();
		List<MenuElement> children = page.getChildMenuElementsList();
		for (MenuElement child : children) {
			try {
				if (child.isActive() || ctx.isEdition()) {
					if (child.isVisible()) {
						childrenBean.add(child.getPageBean(ctx));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return childrenBean;
	}

	public String getName() {
		return page.getName();
	}

	public ImageBean getImageBackground() throws Exception {
		IImageTitle imageTitle = page.getImageBackground(ctx);
		if (imageTitle != null) {
			return new ImageBean(ctx, imageTitle, "main-background");
		} else {
			return null;
		}
	}
	
	public ImageBean getImage() throws Exception {
		IImageTitle imageTitle = page.getImage(ctx);
		if (imageTitle != null) {
			return new ImageBean(ctx, imageTitle, "standard");
		} else {
			return null;
		}
	}


	public ExtendedColor getColor() {
		try {
			return page.getColor(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ExtendedColor getBackgroundColor() {
		try {
			return page.getBackgroundColor(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getHumanName() {
		return page.getHumanName();
	}

	public String getPath() {
		return page.getPath();
	}

	public String getLatestEditor() {
		return page.getLatestEditor();
	}

	public String getModificationDate() {
		try {
			return StringHelper.renderShortDate(ctx, page.getModificationDate(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Date getModificationDateSource() throws ParseException, Exception {
		return page.getModificationDate(ctx);
	}

	public String getContentDateValue() {
		try {
			return StringHelper.renderShortDate(ctx, page.getContentDate(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getContentDateNeverNullValue() {
		try {
			return StringHelper.renderShortDate(ctx, page.getContentDateNeverNull(ctx));
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

	public Template getTemplate() throws Exception {
		return TemplateFactory.getTemplate(ctx, getPage());
	}

	public boolean isLastSelected() {
		return page.isLastSelected(ctx);
	}

	public String getId() {
		return page.getId();
	}
	
	public boolean isRealContentAuto() {
		try {
			return page.isRealContentAuto(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isRealContent() {
		try {
			return page.isRealContent(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isRealContentAnyLanguage() {
		try {
			return page.isRealContentAnyLanguage(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getRealContentLanguage() {
		try {
			return page.getRealContentLanguage(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, String> getRoles() {
		return new CollectionAsMap<String>(page.getUserRoles());
	}

	public boolean isUserRolesInherited() {
		return page.isUserRolesInherited();
	}

	public Map<String, String> getAdminRoles() {
		return new CollectionAsMap<String>(page.getEditorRoles());
	}

	public Map<String, String> getAdminRolesAndParent() {
		return new CollectionAsMap<String>(page.getEditorRolesAndParent());
	}

	public String getFirstRoles() {
		Map<String, String> roles = getRoles();
		if (roles != null && roles.size() > 0) {
			return roles.keySet().iterator().next();
		} else {
			return null;
		}
	}

	public String getFirstAdminRole() {
		Map<String, String> roles = getAdminRoles();
		if (roles != null && roles.size() > 0) {
			return roles.keySet().iterator().next();
		} else {
			return null;
		}
	}

	public String getFirstAdminRoleAndParent() {
		Map<String, String> roles = getAdminRolesAndParent();
		if (roles != null && roles.size() > 0) {
			return roles.keySet().iterator().next();
		} else {
			return null;
		}
	}

	public String getShortURL() {
		try {
			return page.getShortURL(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getShortLanguageURL() {
		try {
			return page.getShortLanguageURL(ctx);
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

	public ContactBean getContact() {
		try {
			return page.getContact(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	public String getTechnicalTitle() {
		return page.getTechnicalTitle(ctx);
	}
	
	public String getSubTitle() throws Exception {
		return page.getSubTitle(ctx);
	}
	
	public String getLabel() throws Exception {
		return page.getLabel(ctx);
	}

	public String getTitleOrSubtitle() {
		try {
			String title = page.getContentTitle(ctx);

			if (title == null || title.trim().length() == 0) {
				title = page.getSubTitle(ctx);
				if (title == null || title.trim().length() == 0) {
					title = page.getName();
				}
			}
			return title;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public HtmlPart getDescription() throws Exception {
		return page.getDescription(ctx);
	}

	public String getSlogan() {
		try {
			return page.getSlogan(ctx);
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

	public double getPageRank() {
		try {
			return page.getPageRank(ctx);
		} catch (Exception e) {
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

	public TimeRange getTimeRange() throws Exception {
		return page.getTimeRange(ctx);
	}

	public boolean isVisible() {
		try {
			return page.isVisible();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isActive() {
		try {
			return page.isActive();
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isModel() {
		try {
			return page.isModel();
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean isAdmin() {
		try {
			return page.isAdmin();
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isVisibleForContext() {
		try {
			return page.isVisible(ctx);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isChildrenAssociation() {
		return page.isChildrenAssociation();
	}

	public boolean isChildrenOfAssociation() {
		return page.isChildrenOfAssociation();
	}

	public boolean isVisibleChildren() throws Exception {
		for (MenuElement child : page.getChildMenuElements()) {
			if (child.isVisible() && child.isRealContent(ctx)) {
				return true;
			}
		}
		return false;
	}

	public boolean isRealContentChildren() throws Exception {
		for (MenuElement child : page.getChildMenuElements()) {
			if (child.isRealContent(ctx)) {
				return true;
			}
		}
		return false;
	}

	public boolean isHaveChildren() throws Exception {
		return page.getChildMenuElements().size() > 0;
	}

	public PageAssociationBean getRootOfChildrenAssociation() throws Exception {
		if (page.getRootOfChildrenAssociation() != null) {
			return new PageAssociationBean(ctx, page.getRootOfChildrenAssociation());
		} else {
			return null;
		}
	}

	public MenuElement getPage() {
		return page;
	}
	
	public int getDepth() {
		return page.getDepth();
	}

	public boolean isRoot() {
		return page.isRoot();
	}

	/**
	 * is page editable by current User ?
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean isEditable() throws Exception {
		if (ctx.getCurrentEditUser() == null) {
			return false;
		} else {
			return Edit.checkPageSecurity(ctx, getPage());
		}
	}

	public boolean isCacheable() throws Exception {
		return page.isCacheable(ctx);
	}

	public String getNotCacheableComponent() throws Exception {
		IContentVisualComponent comp = page.getNotCacheableComponent(ctx);
		if (comp != null) {
			return comp.getType();
		} else {
			return "";
		}
	}

	public String getRealContentComponent() throws Exception {
		IContentVisualComponent comp = page.getRealContentComponent(ctx);
		if (comp != null) {
			return comp.getType();
		} else {
			return "";
		}
	}

	public boolean isPageEmpty() throws Exception {
		return page.isEmpty(ctx, null);
	}

	public boolean isPageLocalEmpty() throws Exception {
		return page.isLocalEmpty(ctx, null);
	}

	public boolean isTrash() {
		return ContentService.TRASH_PAGE_NAME.equals(getName());
	}
	
	public boolean isPopup() throws Exception {
		if (getUrl() == null) {
			return false;
		}
		return getUrl().equals(getLinkOn());
	}

	/**
	 * get the link on the page, if page do not contains real content, the link can be a external link or a link to a ressources.
	 * @return
	 * @throws Exception
	 */
	public String getLink() throws Exception {
		if (page.isRealContent(ctx)) {
			return getUrl();
		} else {
			String linkOn = page.getLinkOn(ctx);
			if (linkOn != null && linkOn.trim().length() > 0) {
				return linkOn;
			} else {
				return getUrl();
			}
		}
	}

	/**
	 * get the link on the page, if page do not contains real content, the link can be a external link or a link to a ressources.
	 * @return
	 * @throws Exception
	 */
	public String getRealContentLink() throws Exception {
		if (page.isRealContent(ctx)) {
			return getUrl();
		} else {
			String linkOn = page.getLinkOn(ctx);
			if (linkOn != null && linkOn.trim().length() > 0) {
				return linkOn;
			} else {
				return getRealContentUrl();
			}
		}
	}

	/**
	 * returns the first link found in a component (external link, link to a file, etc.)
	 * @return
	 * @throws Exception
	 */
	public String getLinkOn() throws Exception {
		return page.getLinkOn(ctx);
	}

	public int getSeoWeight() {
		return page.getFinalSeoWeight();
	}

	public void setContentContext(ContentContext ctx) {
		this.ctx = ctx;
	}

	public boolean isLinkRealContent() throws Exception {
		return page.isLinkRealContent(ctx);
	}

	public boolean isValid() {
		if (!portail) {
			return true;
		} else {
			return page.isValid();
		}
	}
	public boolean isNeedValidation() {
		return page.isNeedValidation();
	}
	public boolean isNoValidation() {
		return page.isNoValidation();
	}

	/**
	 * get the next page in the navigation.
	 * 
	 * @return null if current page is the last page
	 * @throws Exception
	 */
	public PageBean getNextPage() throws Exception {
		MenuElement parent = page.getParent();
		if (parent != null) {
			boolean pageFound = false;
			for (MenuElement page : parent.getAllChildrenList()) {
				if (pageFound) {
					return page.getPageBean(ctx);
				}
				if (page.getId().equals(getId())) {
					pageFound = true;
				}
			}
		}

		return null;
	}

	public int getSiblingsSize() {
		if (getParent() != null) {
			return getParent().getChildren().size();
		} else {
			return 1;
		}
	}

	public int getSiblingsPosition() {
		if (getParent() != null) {
			if (page != null) {
				int pos = 0;
				for (MenuElement p : getParent().page.getChildMenuElementsList()) {
					pos++;
					if (p.getId().equals(getId())) {
						return pos;
					}
				}
			}
		} else {
			return 1;
		}
		return -1;
	}

	/**
	 * get the next page in the navigation.
	 * 
	 * @return null if current page is the last page
	 */
	public PageBean getPreviousPage() {
		if (getParent() != null) {
			PageBean previousPage = null;
			for (PageBean page : getParent().getChildren()) {
				if (page.getId().equals(getId())) {
					return previousPage;
				}
				previousPage = page;
			}
		}
		return null;
	}

	/***
	 * return the flow number of the page
	 * 
	 * @return 1: modified not ready, 2:ready for validation, 3:valided, 4:publish
	 */
	public int getFlowIndex() {
		if (!portail || isNoValidation()) {
			return 4;
		}
		if (!isValid() && !isNeedValidation()) {
			return 1;
		} else if (!isValid() && isNeedValidation()) {
			return 2;
		} else if (isValid() && isNeedValidation()) {
			return 3;
		} else {
			return 4;
		}
	}

	public int getChildrenFlowIndex() {
		int minIndex = 4;
		for(PageBean child : getChildren()) {
			if (child.getFlowIndex() < minIndex) {
				minIndex = child.getFlowIndex();
			}
			if (!child.getId().equals(getId())) {
				int childFlowIndex = child.getChildrenFlowIndex();
				if (childFlowIndex < minIndex) {
					minIndex = childFlowIndex;
				}
			}
		}
		return minIndex;
	}

	/**
	 * current user can validated this page ?
	 * 
	 * @return
	 */
	public boolean isValidable() {
		if (!portail) {
			return true;
		} else {
			AdminUserSecurity userSecurity = AdminUserSecurity.getInstance();
			return userSecurity.canRole(ctx.getCurrentEditUser(), AdminUserSecurity.VALIDATION_ROLE);
		}
	}

	public boolean isReadAccess() {
		return page.isReadAccess(ctx, ctx.getCurrentUser());
	}

	public String getHtmlId() {
		return page.getHtmlId(ctx);
	}

	public int getIndex() {
		return page.getIndex();
	}
	
	public String getFont() throws Exception {
		return page.getFont(ctx);
	}
	
	public String getContentLanguage() {
		return ctx.getRequestContentLanguage();
	}
	
	public String getContentLanguageName() {
		Locale locale;
		if (ctx.isAsModifyMode()) {
			locale = new Locale(ctx.getGlobalContext().getEditLanguage(ctx.getRequest().getSession()));
		} else {
			locale = ctx.getLocale();
		}
		Locale lg = ctx.getLocale();
		return lg.getDisplayName(locale);
	}
	
	public Map<String,String> getTypes() {
		if (types == null) {
			types = new HashMap<String, String>();
			for (ComponentBean bean : page.getContent()) {
				if (!types.containsKey(bean.getType())) {
					types.put(bean.getType(), bean.getType());
				}
			}
		}
		return types;
	}
	
	public String getScreenshotUrl() throws IOException {
		File file = ctx.getGlobalContext().getPageScreenshotFile(page.getName());
		if (file != null && file.exists()) {
			return URLHelper.createFileURL(ctx, file);
		} else {
			return null;
		}
	}
	
	public int getLastDayVisit() throws ServiceException, IOException {
		Tracker tracker = Tracker.getTracker(ctx.getGlobalContext(), ctx.getRequest().getSession());
		String path = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), ctx.getPath());
		return tracker.getLastDayPathReading(path);
	}
	
	public int getLastMountVisit() throws ServiceException, IOException {
		Tracker tracker = Tracker.getTracker(ctx.getGlobalContext(), ctx.getRequest().getSession());
		String path = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), ctx.getPath());
		return tracker.getLastMountPathReading(path);
	}
	
	public int getLastYearVisit() throws ServiceException, IOException {
		Tracker tracker = Tracker.getTracker(ctx.getGlobalContext(), ctx.getRequest().getSession());
		String path = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), ctx.getPath());
		return tracker.getLastYearPathReading(path);
	}
	
	public Map<String, Object> getContentAsMap() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, Exception {
		return page.getContentAsMap(ctx);
	}
	
	public Set<String> getTaxonomy() {
		return page.getTaxonomy();
	}

	public List<TaxonomyDisplayBean> getTaxonomyBean() throws IOException {
		TaxonomyService ts = TaxonomyService.getInstance(ctx);
		return TaxonomyDisplayBean.convert(ctx, ctx.getGlobalContext().getAllTaxonomy(ctx).convert(page.getTaxonomy()));
	}

	public List<ISection> getSection() throws Exception {
		return page.getSection(ctx);
	}
	
}
