package org.javlo.navigation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.module.content.Edit;
import org.javlo.navigation.MenuElement.PageDescription;
import org.javlo.service.exception.ServiceException;
import org.javlo.utils.CollectionAsMap;

import com.beust.jcommander.ParameterException;

/**
 * bean for the page, can be use in JSTL.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class PageBean implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/*
	 * private PageDescription info; private String url; private String path; private boolean selected = false; private boolean lastSelected = false; private final List<PageBean> children = new LinkedList<PageBean>(); private final List<PageBean> realChildren = new LinkedList<PageBean>(); private String name = null; private String id = null; private String latestEditor; private String creationDate; private String modificationDate; private String templateId = null; private boolean realContent = false; private Map<String, String> roles = new HashMap<String, String>(); private Map<String, String> adminRoles = new HashMap<String, String>();
	 */

	public PageBean(ContentContext ctx, MenuElement page) {
		if (page == null) {
			throw new ParameterException("page can not be null");
		}
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
	
	public PageBean getParent() {
		MenuElement parent = page.getParent();
		if (parent == null) {
			return null;
		} else {
			return new PageBean(ctx,parent);
		}
	}
	
	public List<PageBean> getParents() {
		List<PageBean> outParents = new LinkedList<PageBean>();
		MenuElement parent = page.getParent();
		while (parent != null) {
			outParents.add(0,new PageBean(ctx, parent));
			parent = parent.getParent();
		}
		return outParents;
	}

	public String getUrl() {
		return URLHelper.createURL(ctx, page);
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
		return URLHelper.createURL(pdfCtx,page.getPath());		
	}
	
	public String getAbsoluteUrl() {
		return URLHelper.createAbsoluteURL(ctx, page.getPath());
	}
	
	public String getAbsolutePreviewUrl() {
		return URLHelper.createAbsoluteURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), page.getPath());
	}
	
	public String getEditUrl() {
		return URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE), page);
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
	
	public Map<String, String> getAdminRolesAndParent() {
		return new CollectionAsMap<String>(page.getEditorRolesAndParent());
	}
	
	public String getFirstRoles() {
		Map<String,String> roles = getRoles();
		if (roles != null && roles.size() > 0) {
			return roles.keySet().iterator().next();
		} else {
			return null;
		}
	}
	
	public String getFirstAdminRole() {
		Map<String,String> roles = getAdminRoles();
		if (roles != null && roles.size() > 0) {
			return roles.keySet().iterator().next();
		} else {
			return null;
		}
	}
	
	public String getFirstAdminRoleAndParent() {
		Map<String,String> roles = getAdminRolesAndParent();
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
	
	public String getTechnicalTitle() {
		return page.getTechnicalTitle(ctx);
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

	public boolean isVisible() {
		try {
			return page.isVisible();
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
	 * @return
	 * @throws Exception 
	 */
	public boolean isEditable() throws Exception {
		if (ctx.getCurrentEditUser() == null) {
			return false;
		} else {
			return Edit.checkPageSecurity(ctx,getPage());
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
	
	public boolean isPageEmpty() throws Exception {
		return page.isEmpty(ctx);
	}

}

