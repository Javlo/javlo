package org.javlo.component.links;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.javlo.actions.IEventRegistration;
import org.javlo.bean.DateBean;
import org.javlo.bean.Link;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.links.PageReferenceComponent.PageEvent;
import org.javlo.component.meta.Tags;
import org.javlo.context.ContentContext;
import org.javlo.data.taxonomy.TaxonomyDisplayBean;
import org.javlo.ecom.Product.ProductBean;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.ExtendedColor;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.data.PageContentMap;
import org.javlo.service.event.Event;
import org.javlo.service.exception.ServiceException;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.utils.SmartTimeRange;
import org.javlo.utils.TimeRange;
import org.owasp.encoder.Encode;

public class SmartPageBean {

	private static Logger logger = Logger.getLogger(SmartPageBean.class.getName());

	public static class Image {
		private String url;
		private String viewURL;
		private String linkURL;
		private String description;
		private String path;
		private String cssClass;

		public Image(String url, String viewURL, String linkURL, String cssClass, String description, String path) {
			super();
			this.url = url;
			this.viewURL = viewURL;
			this.linkURL = linkURL;
			this.setCssClass(cssClass);
			this.description = description;
			this.path = path;
		}

		public String getCssClass() {
			return cssClass;
		}
		
		public String getDescription() {
			return description;
		}

		public String getLinkURL() {
			return linkURL;
		}

		public String getPath() {
			return path;
		}

		public String getUrl() {
			return url;
		}

		public String getViewURL() {
			return viewURL;
		}

		public void setCssClass(String cssClass) {
			this.cssClass = cssClass;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public void setLinkURL(String linkURL) {
			this.linkURL = linkURL;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public void setViewURL(String viewURL) {
			this.viewURL = viewURL;
		}
	}

	private ContentContext ctx;
	private ContentContext lgCtx;
	private ContentContext realContentCtx;
	private MenuElement page;
	private PageReferenceComponent comp;

	private List<SmartPageBean> children = null;
	private int toTheTopLevel = -1;

	private SmartPageBean(ContentContext ctx, ContentContext lgCtx, MenuElement page, PageReferenceComponent comp) {
		this.ctx = ctx;
		this.page = page;
		this.lgCtx = lgCtx;
		this.comp = comp;
		realContentCtx = new ContentContext(lgCtx);
		realContentCtx.setLanguage(realContentCtx.getRequestContentLanguage());
	}

	public static SmartPageBean getInstance(ContentContext ctx, ContentContext lgCtx, MenuElement page, PageReferenceComponent comp) {
		return new SmartPageBean(ctx, lgCtx, page, comp);
	}
	
	public boolean isLinkRealContent() throws Exception {
		return page.isLinkRealContent(ctx);
	}
	
	public boolean isPopup() throws Exception {
		if (getUrl() == null) {
			return false;
		}
		return !getUrl().equals(getLinkOn());
	}

	public String getAttTitle() {
		try {
			return XHTMLHelper.stringToAttribute(page.getTitle(lgCtx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getCategory() {
		try {
			return page.getCategory(lgCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getAuthors() {
		User user = AdminUserFactory.createAdminUserFactory(ctx.getGlobalContext(), ctx.getSession()).getUser(page.getCreator());
		if (user != null) {
			return user.getUserInfo().getFirstName() + user.getUserInfo().getLastName();
		} else {
			return page.getCreator();
		}
	}

	public DateBean getDate() {
		try {
			return new DateBean(lgCtx, page.getContentDate(lgCtx));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public SmartTimeRange getTimeRange() throws Exception {
		TimeRange tr = page.getTimeRange(ctx);
		if (tr == null) {
			return null;
		} else {
			return new SmartTimeRange(ctx, tr);
		}
	}

	public String getDescription() {
		try {
			ContentContext newPageCtx = new ContentContext(ctx);
			newPageCtx.setCurrentPageCached(page);
			return XHTMLHelper.replaceJSTLData(newPageCtx, page.getDescriptionAsText(lgCtx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getXhtmlTitle() throws Exception {
		return page.getXHTMLTitle(ctx);
	}

	public String getXhtmlDescription() {
		try {
			ContentContext newPageCtx = new ContentContext(ctx);
			newPageCtx.setCurrentPageCached(page);
			return XHTMLHelper.replaceJSTLData(newPageCtx, page.getXHTMLDescription(lgCtx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public DateBean getEndDate() {
		try {
			return new DateBean(lgCtx, page.getTimeRange(lgCtx).getEndDate());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getId() {
		return page.getId();
	}

	public String getImageDescription() {
		try {
			IImageTitle image = page.getImage(lgCtx);
			if (image != null) {
				return XHTMLHelper.stringToAttribute(image.getImageDescription(lgCtx));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getImagePath() {
		try {
			IImageTitle image = page.getImage(lgCtx);
			if (image != null) {
				return image.getResourceURL(lgCtx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected String getImageFilter() {
		if (comp == null) {
			return "reference-list";
		} else {
			return comp.getConfig(lgCtx).getProperty("filter-image", "reference-list");
		}
	}

	public Collection<Image> getImages() {
		try {
			Collection<IImageTitle> images = page.getImages(lgCtx);
			Collection<Image> imagesBean = new LinkedList<SmartPageBean.Image>();
			for (IImageTitle imageItem : images) {
				String imagePath = imageItem.getResourceURL(lgCtx);
				String imageURL = URLHelper.createTransformURL(lgCtx, page, imageItem.getResourceURL(lgCtx), getImageFilter());
				String viewImageURL = URLHelper.createTransformURL(lgCtx, page, imageItem.getResourceURL(lgCtx), "thumb-view");
				String imageDescription = XHTMLHelper.stringToAttribute(imageItem.getImageDescription(lgCtx));
				String cssClass = "";
				String linkURL = imageItem.getImageLinkURL(lgCtx);
				if (linkURL != null) {
					if (linkURL.equals(IImageTitle.NO_LINK)) {
						cssClass = "no-link";
						linkURL = null;
					} else {
						cssClass = "link " + StringHelper.getPathType(linkURL, "");
					}
				}
				SmartPageBean.Image imageBean = new SmartPageBean.Image(imageURL, viewImageURL, linkURL, cssClass, imageDescription, imagePath);
				imagesBean.add(imageBean);
			}
			return imagesBean;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Image getImage() {
		return new SmartPageBean.Image(getImageURL(), getViewImageURL(), "", "", getImageDescription(), getImagePath());
	}

	public String getImageURL() {
		try {
			IImageTitle image = page.getImage(lgCtx);
			if (image != null) {
				return URLHelper.createTransformURL(lgCtx, page, image.getResourceURL(lgCtx), getImageFilter());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getLanguage() {
		return lgCtx.getRequestContentLanguage();
	}

	public String getForceLinkOn() {
		try {
			return page.getLinkOn(lgCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getLinkOn() {
		String linkOn = getForceLinkOn();
		if (!StringHelper.isEmpty(linkOn) && !isRealContent()) {
			return linkOn;
		} else {
			if (page.isDirectChildrenOfAssociation()) {
				try {
					if (getParent().getId().equals(ctx.getCurrentPage().getId())) {
						return "#" + page.getHtmlSectionId(ctx);
					} else {
						return getParent().getUrl() + "#" + page.getHtmlSectionId(ctx);
					}
				} catch (Exception e) {
					e.printStackTrace();
					return "error:" + e.getMessage();
				}

			} else {
				return getUrl();
			}
		}
	}

	public boolean isLink() {
		try {
			return isRealContent() || !StringHelper.isEmpty(page.getLinkOn(lgCtx));
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	public Collection<Link> getLinks() {
		try {
			Collection<String> lgs = ctx.getGlobalContext().getContentLanguages();
			Collection<Link> links = new LinkedList<Link>();
			for (String lg : lgs) {
				ContentContext localLGCtx = new ContentContext(lgCtx);
				localLGCtx.setRequestContentLanguage(lg);
				localLGCtx.setContentLanguage(lg);
				if (page.isRealContent(localLGCtx)) {
					Locale locale = new Locale(lg);
					Link link = new Link(URLHelper.createURL(localLGCtx, page.getPath()), lg, lg + " - " + locale.getDisplayLanguage(locale));
					links.add(link);
				}
			}
			if (links.size() == 0) {
				links = null;
			}
			return links;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getLocation() {
		try {
			return page.getLocation(lgCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getName() {
		return page.getName();
	}

	public String getRawTags() {
		String rawTags = "";
		String sep = "";
		for (String tag : getTags()) {
			rawTags = rawTags + sep + tag.toLowerCase().replace(' ', '-');
			sep = " ";
		}
		return rawTags;
	}

	public DateBean getStartDate() {
		try {
			if (page.getTimeRange(lgCtx) != null) {
				return new DateBean(lgCtx, page.getTimeRange(lgCtx).getStartDate());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<DateBean> getDates() {
		List<DateBean> dates;
		try {
			dates = new LinkedList<DateBean>();
			if (page.getEvents(realContentCtx) != null) {
				dates = new LinkedList<DateBean>();
				for (Event pageEvent : page.getEvents(realContentCtx)) {
					Calendar startDate = Calendar.getInstance();
					startDate.setTime(pageEvent.getStart());
					Calendar endDate = Calendar.getInstance();
					endDate.setTime(pageEvent.getEnd());
					dates.add(new DateBean(ctx, startDate.getTime()));
					final int MAX_DAYS_OF_EVENTS = 400;
					int i = 0;
					while (TimeHelper.isBeforeForDay(startDate.getTime(), endDate.getTime()) && i < MAX_DAYS_OF_EVENTS) {
						i++;
						startDate.add(Calendar.DAY_OF_YEAR, 1);
						dates.add(new DateBean(ctx, startDate.getTime()));
					}
					if (i == MAX_DAYS_OF_EVENTS) {
						logger.warning("to much days in event (max:" + MAX_DAYS_OF_EVENTS + ") : " + page.getPath() + " [" + ctx.getGlobalContext().getContextKey() + ']');
					}
				}
			} else {
				dates = new LinkedList<DateBean>();
				Calendar startDate = Calendar.getInstance();
				startDate.setTime(getStartDate().getDate());
				Calendar endDate = Calendar.getInstance();
				endDate.setTime(getEndDate().getDate());

				dates.add(new DateBean(ctx, startDate.getTime()));

				final int MAX_DAYS_OF_EVENTS = 1000;
				int i = 0;

				/// while (startDate.before(endDate) && i<MAX_DAYS_OF_EVENTS) {
				while (TimeHelper.isBeforeForDay(startDate.getTime(), endDate.getTime()) && i < MAX_DAYS_OF_EVENTS) {
					startDate.add(Calendar.DAY_OF_YEAR, 1);
					dates.add(new DateBean(ctx, startDate.getTime()));
					i++;
				}
				if (i == MAX_DAYS_OF_EVENTS) {
					logger.warning("to much days in page (max:" + MAX_DAYS_OF_EVENTS + ") : " + page.getPath() + " [" + ctx.getGlobalContext().getContextKey() + ']');
				}
			}
			return dates;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getSubTitle() {
		display();
		try {
			return page.getSubTitle(lgCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<String> getSubTitles() {
		try {
			return page.getSubTitles(lgCtx, 2);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Collection<String> getTags() {
		try {
			return page.getTags(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getFirstTag() {
		try {
			Collection<String> tags = getTags();
			if (tags.size() > 0) {
				return tags.iterator().next();
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getFirstTagLabel() {
		try {
			Collection<String> tags = getTagsLabel();
			if (tags.size() > 0) {
				return tags.iterator().next();
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Collection<String> getTagsLabel() {
		ContentContext tagCtx = new ContentContext(lgCtx);
		Iterator<String> defaultLg = ctx.getGlobalContext().getDefaultLanguages().iterator();
		Collection<String> tags = new LinkedList<String>();
		try {
			while (page.getContentByType(tagCtx, Tags.TYPE).size() == 0 && defaultLg.hasNext()) {
				String lg = defaultLg.next();
				tagCtx.setContentLanguage(lg);
				tagCtx.setRequestContentLanguage(lg);
			}
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			for (String tag : page.getTags(tagCtx)) {
				tags.add(i18nAccess.getViewText("tag." + tag, tag));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tags;
	}

	public String getTitle() {
		try {
			display();
			return page.getTitle(lgCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getHtmlTitle() {
		try {
			display();
			return page.getXHTMLTitle(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getTitleForAttribute() {
		display();
		try {
			return Encode.forHtmlAttribute(page.getTitle(lgCtx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getContentTitle() {
		display();
		try {
			return page.getContentTitle(lgCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getUrl() {
		try {
			MenuElement firstChild = page.getFirstChild();
			if (firstChild != null && firstChild.isChildrenAssociation()) {
//				ContentContext ctxLg = ctx;
//				if (ctx.getGlobalContext().isAutoSwitchToDefaultLanguage()) {
//					ctxLg = ctx.getContextWithContent(firstChild);
//				}
				return URLHelper.createURL(ctx, firstChild.getPath());
			} else {
//				ContentContext ctxLg = ctx;
//				if (ctx.getGlobalContext().isAutoSwitchToDefaultLanguage()) {
//					ctxLg = ctx.getContextWithContent(page);
//					if (ctxLg == null) {
//						ctxLg = ctx;
//					}
//				}
				return URLHelper.createURL(ctx, page.getPath());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getViewImageURL() {
		try {
			IImageTitle imageTitle = page.getImage(lgCtx);
			if (imageTitle != null) {
				return URLHelper.createTransformURL(lgCtx, page, imageTitle.getResourceURL(lgCtx), "thumb-view");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isRealContent() {
		try {
			return page.isRealContent(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isSelected() {
		try {
			return page.isSelected(lgCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getCategoryKey() {
		try {
			return "category." + StringHelper.neverNull(page.getCategory(lgCtx)).toLowerCase().replaceAll(" ", "");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getCategoryLabel() {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return i18nAccess.getViewText(getCategoryKey());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean isVisible() {
		try {
			return page.isVisible();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Collection<Link> getStaticResources() {
		try {
			return page.getStaticResources(realContentCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getPath() {
		return page.getPath();
	}

	public String getCreator() {
		return page.getCreator();
	}

	public boolean isContentDate() {
		try {
			if (page.getContentDate(lgCtx) != null) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getPublishURL() {
		try {
			MenuElement firstChild = page.getFirstChild();
			if (firstChild != null && firstChild.isChildrenAssociation()) {
				return URLHelper.createAbsoluteViewURL(lgCtx, firstChild.getPath());
			} else {
				ContentContext ctxLg = ctx;
				if (ctx.getGlobalContext().isAutoSwitchToDefaultLanguage()) {
					ctxLg = ctx.getContextWithContent(page);
					if (ctxLg == null) {
						ctxLg = ctx;
					}
				}
				return URLHelper.createAbsoluteViewURL(lgCtx, page.getPath());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean isChildrenOfAssociation() {
		return page.isChildrenOfAssociation();
	}

	public MenuElement getRootOfChildrenAssociation() {
		return page.getRootOfChildrenAssociation();
	}

	public String getCreationDate() {
		try {
			return StringHelper.renderShortDate(lgCtx, page.getCreationDate());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getSortableDate() {
		try {
			if (page.getContentDate(lgCtx) != null) {
				return StringHelper.renderSortableDate(page.getContentDate(lgCtx));
			} else {
				return StringHelper.renderSortableDate(page.getModificationDate(ctx));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getFont() {
		try {
			return page.getFont(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getSortableCreationDate() {
		return StringHelper.renderSortableDate(page.getCreationDate());
	}

	public boolean isMailing() {
		try {
			Template pageTemplate = TemplateFactory.getTemplate(lgCtx, page);
			if (pageTemplate != null) {
				return pageTemplate.isMailing();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isChildrenAssociation() {
		return page.isChildrenAssociation();
	}

	public String getHumanName() {
		return page.getHumanName();
	}

	public int getReactionSize() {
		try {
			return page.getReactionSize(lgCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public String getModificationDate() {
		try {
			return StringHelper.renderShortDate(lgCtx, page.getModificationDate(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getContentDateValue() {
		try {
			return StringHelper.renderShortDate(lgCtx, page.getContentDate(lgCtx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getRangeOrDate() throws ServiceException, Exception {
		I18nAccess i18n = I18nAccess.getInstance(ctx);
		TimeRange tr = page.getTimeRange(ctx);
		if (tr != null && !tr.getStartDate().equals(tr.getEndDate())) {
			return i18n.getViewText("global.from-date")+' '
					+StringHelper.renderShortDate(ctx, tr.getStartDate())+' '
					+i18n.getViewText("global.to-date")+' '
					+StringHelper.renderShortDate(ctx, tr.getEndDate());
		} else if (page.getContentDate(ctx) != null) {
				return StringHelper.renderShortDate(ctx, page.getContentDate(ctx));
		} else {
			return "";
		}
	}

	public String getSortableModificationDate() {
		try {
			return StringHelper.renderShortDate(lgCtx, page.getModificationDate(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<SmartPageBean> getChildren() throws Exception {
		if (children == null) {
			List<SmartPageBean> workChildren = new LinkedList<SmartPageBean>();
			if (page != null) {
				for (MenuElement child : page.getChildMenuElementsList()) {
					workChildren.add(SmartPageBean.getInstance(ctx, lgCtx, child, comp));
				}
			}
			children = workChildren;
		}
		return children;
	}

	public String getTechnicalTitle() {
		ContentContext defaultLangCtx = ctx.getContextForDefaultLanguage();
		String title;
		try {
			title = page.getTitle(defaultLangCtx);
		} catch (Exception e) {
			title = page.getName();
			e.printStackTrace();
		}
		return StringHelper.createFileName(title).toLowerCase();
	}

	public PageEvent getEvent() {
		try {
			PageEvent outEvent = new PageEvent();
			Event event = page.getEvent(realContentCtx);
			if (event != null) {
				outEvent.setStart(event.getStart());
				outEvent.setEnd(event.getEnd());
			} else {
				outEvent.setStart(page.getContentDate(realContentCtx));
			}
			return outEvent;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getSortableCreationTime() {
		return StringHelper.renderSortableTime(page.getCreationDate());
	}

	public String getSortableModificationTime() {
		try {
			return StringHelper.renderShortTime(lgCtx, page.getModificationDate(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getModificationTime() {
		try {
			return StringHelper.renderShortTime(lgCtx, page.getModificationDate(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getCreationTime() {
		try {
			return StringHelper.renderShortTime(lgCtx, page.getCreationDate());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public SmartPageBean getParent() {
		if (page.getParent() == null) {
			return null;
		} else {
			try {
				return SmartPageBean.getInstance(ctx, lgCtx, page.getParent(), comp);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public String getLinkLabel() {
		try {
			return page.getLinkLabel(lgCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean isCurrentUserAsRight() {
		Set<String> roles = page.getEditorRoles();
		if (roles.size() == 0 && ctx.getCurrentUser() != null) {
			return true;
		} else {
			if (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser())) {
				return true;
			} else if (ctx.getCurrentUser() != null && !Collections.disjoint(roles, ctx.getCurrentUser().getRoles())) {
				return true;
			} else {
				return false;
			}
		}
	}

	public boolean isEditable() {
		try {
			return page.isEditabled(realContentCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isActive() {
		return page.isActive(ctx);
	}

	public int getSeoWeight() {
		return page.getSeoWeight();
	}

	public String getLabel() {
		try {
			return page.getLabel(lgCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<Event> getEvents() {
		try {
			return page.getEvents(realContentCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean isModel() {
		return page.isModel();
	}
	
	public boolean isAdmin() {
		return page.isAdmin();
	}

	public PageContentMap getData() {
		return new PageContentMap(ctx, page);
	}

	public int getToTheTopLevel() {
		if (toTheTopLevel == -1) {
			try {
				toTheTopLevel = page.getToTheTopLevel(realContentCtx);
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		}
		return toTheTopLevel;
	}

	public ExtendedColor getColor() {
		try {
			return page.getColor(realContentCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getPriority() {
		return page.getPriority();
	}

	public MenuElement getPage() {
		return page;
	}

	public String getHtmlId() {
		return page.getHtmlId(ctx);
	}

	public String getHtmlSectionId() {
		return page.getHtmlSectionId(ctx);
	}

	public List<TaxonomyDisplayBean> getTaxonomy() {
		try {
			return TaxonomyDisplayBean.convert(ctx, ctx.getGlobalContext().getAllTaxonomy(ctx).convert(page.getTaxonomy()));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private List<Map<String, String>> outData = null;

	public List<Map<String, String>> getUserData() throws Exception {
		if (outData == null) {
			String userId = ctx.getCurrentUserId();
			if (userId == null) {
				outData = Collections.EMPTY_LIST;
			} else {
				outData = new LinkedList<Map<String, String>>();
				ContentElementList content = page.getContent(ctx);
				while (content.hasNext(ctx)) {
					IContentVisualComponent comp = content.next(ctx);
					if (comp instanceof IEventRegistration) {
						outData.addAll(((IEventRegistration) comp).getData(ctx, userId));
					}
				}
			}
		}
		return outData;
	}
	
	public void display() {
		final String KEY = "comp-displayed-"+getId();
		ctx.getRequest().setAttribute(KEY, true);
	}
	
	public boolean isAlreadyDisplayed() {
		final String KEY = "comp-displayed-"+getId();
		return ctx.getRequest().getAttribute(KEY) != null;
	}
	
	public String getScreenshotUrl() throws IOException {
		File file = ctx.getGlobalContext().getPageScreenshotFile(page.getName());
		if (file.exists()) {
			return URLHelper.createFileURL(ctx, file);
		} else {
			return null;
		}
	}
	
	public String getContentDateNeverNull() throws FileNotFoundException, IOException, Exception {
		return StringHelper.renderShortDate(ctx, page.getContentDateNeverNull(ctx));
	}
	
	public ProductBean getProduct() throws Exception {
		return page.getProduct(ctx);
	}
}
