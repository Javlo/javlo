package org.javlo.component.links;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.javlo.bean.DateBean;
import org.javlo.bean.Link;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.links.PageReferenceComponent.PageBean;
import org.javlo.component.links.PageReferenceComponent.PageEvent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.image.ExtendedColor;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.data.PageContentMap;
import org.javlo.service.event.Event;

public class SmartPageBean extends PageBean {

	private static Logger logger = Logger.getLogger(SmartPageBean.class.getName());

	ContentContext ctx;
	ContentContext lgCtx;
	MenuElement page;
	PageReferenceComponent comp;

	public SmartPageBean(ContentContext ctx, ContentContext lgCtx, MenuElement page, PageReferenceComponent comp) {
		this.ctx = ctx;
		this.page = page;
		this.lgCtx = lgCtx;
		this.comp = comp;
	}

	@Override
	public String getAttTitle() {
		try {
			return XHTMLHelper.stringToAttribute(page.getTitle(lgCtx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getCategory() {
		try {
			return page.getCategory(lgCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public DateBean getDate() {
		try {
			return new DateBean(lgCtx, page.getContentDate(lgCtx));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getDescription() {
		try {
			ContentContext newPageCtx = new ContentContext(ctx);
			newPageCtx.setCurrentPageCached(page);
			return XHTMLHelper.replaceJSTLData(newPageCtx, page.getDescription(lgCtx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
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

	@Override
	public DateBean getEndDate() {
		try {
			return new DateBean(lgCtx, page.getTimeRange(lgCtx).getEndDate());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getId() {
		return page.getId();
	}

	@Override
	public String getImageDescription() {
		try {
			IImageTitle image = page.getImage(lgCtx);
			return XHTMLHelper.stringToAttribute(image.getImageDescription(lgCtx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getImagePath() {
		try {
			IImageTitle image = page.getImage(lgCtx);
			return image.getResourceURL(lgCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected String getImageFilter() {
		return comp.getConfig(lgCtx).getProperty("filter-image", "reference-list");
	}

	@Override
	public Collection<Image> getImages() {
		try {
			Collection<IImageTitle> images = page.getImages(lgCtx);
			Collection<Image> imagesBean = new LinkedList<PageReferenceComponent.PageBean.Image>();
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
				PageBean.Image imageBean = new PageBean.Image(imageURL, viewImageURL, linkURL, cssClass, imageDescription, imagePath);
				imagesBean.add(imageBean);
			}
			return imagesBean;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Image getImage() {
		return new PageBean.Image(getImageURL(), getViewImageURL(), "", "", getImageDescription(), getImagePath());
	}

	@Override
	public String getImageURL() {
		try {
			IImageTitle image = page.getImage(lgCtx);
			return URLHelper.createTransformURL(lgCtx, page, image.getResourceURL(lgCtx), getImageFilter());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getLanguage() {
		return lgCtx.getRequestContentLanguage();
	}

	@Override
	public String getForceLinkOn() {
		try {
			return page.getLinkOn(lgCtx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getLinkOn() {
		String linkOn = getForceLinkOn();
		if (!StringHelper.isEmpty(linkOn) && !isRealContent()) {
			return linkOn;
		} else {
			return getUrl();
		}
	}

	@Override
	public boolean isLink() {
		try {
			return isRealContent() || !StringHelper.isEmpty(page.getLinkOn(lgCtx));
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	@Override
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

	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return super.getLocation();
	}

	@Override
	public String getName() {
		return page.getName();
	}

	@Override
	public String getRawTags() {
		String rawTags = "";
		String sep = "";
		for (String tag : getTags()) {
			rawTags = rawTags + sep + tag.toLowerCase().replace(' ', '-');
			sep = " ";
		}
		return rawTags;
	}

	@Override
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

	@Override
	public List<DateBean> getDates() {
		List<DateBean> dates;
		try {
			ContentContext realContentCtx = new ContentContext(lgCtx);
			dates = new LinkedList<DateBean>();
			realContentCtx.setLanguage(realContentCtx.getRequestContentLanguage());
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

	@Override
	public String getSubTitle() {
		return super.getSubTitle();
	}

	@Override
	public List<String> getSubTitles() {
		try {
			return page.getSubTitles(lgCtx, 2);
		} catch (Exception e) {		
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Collection<String> getTags() {		
		try {
			return page.getTags(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}

	@Override
	public Collection<String> getTagsLabel() {
		// TODO Auto-generated method stub
		return super.getTagsLabel();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return super.getTitle();
	}

	@Override
	public String getContentTitle() {
		// TODO Auto-generated method stub
		return super.getContentTitle();
	}

	@Override
	public String getUrl() {
		// TODO Auto-generated method stub
		return super.getUrl();
	}

	@Override
	public String getViewImageURL() {
		// TODO Auto-generated method stub
		return super.getViewImageURL();
	}

	@Override
	public boolean isRealContent() {
		// TODO Auto-generated method stub
		return super.isRealContent();
	}

	@Override
	public boolean isSelected() {
		// TODO Auto-generated method stub
		return super.isSelected();
	}

	@Override
	public void setId(String id) {
		// TODO Auto-generated method stub
		super.setId(id);
	}

	@Override
	public void setImagePath(String imagePath) {
		// TODO Auto-generated method stub
		super.setImagePath(imagePath);
	}

	@Override
	public void setRawTags(String rawTags) {
		// TODO Auto-generated method stub
		super.setRawTags(rawTags);
	}

	@Override
	public void setSubTitle(String subTitle) {
		// TODO Auto-generated method stub
		super.setSubTitle(subTitle);
	}

	@Override
	public void addTagLabel(String tagLabel) {
		// TODO Auto-generated method stub
		super.addTagLabel(tagLabel);
	}

	@Override
	public void setTags(Collection<String> tags) {
		// TODO Auto-generated method stub
		super.setTags(tags);
	}

	@Override
	public String getCategoryKey() {
		// TODO Auto-generated method stub
		return super.getCategoryKey();
	}

	@Override
	public void setCategoryKey(String categoryKey) {
		// TODO Auto-generated method stub
		super.setCategoryKey(categoryKey);
	}

	@Override
	public String getCategoryLabel() {
		// TODO Auto-generated method stub
		return super.getCategoryLabel();
	}

	@Override
	public void setCategoryLabel(String categoryLabel) {
		// TODO Auto-generated method stub
		super.setCategoryLabel(categoryLabel);
	}

	@Override
	public boolean isVisible() {
		// TODO Auto-generated method stub
		return super.isVisible();
	}

	@Override
	public void setVisible(boolean visible) {
		// TODO Auto-generated method stub
		super.setVisible(visible);
	}

	@Override
	public Collection<Link> getStaticResources() {
		// TODO Auto-generated method stub
		return super.getStaticResources();
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return super.getPath();
	}

	@Override
	public void setPath(String path) {
		// TODO Auto-generated method stub
		super.setPath(path);
	}

	@Override
	public String getCreator() {
		// TODO Auto-generated method stub
		return super.getCreator();
	}

	@Override
	public void setCreator(String creator) {
		// TODO Auto-generated method stub
		super.setCreator(creator);
	}

	@Override
	public boolean isContentDate() {
		// TODO Auto-generated method stub
		return super.isContentDate();
	}

	@Override
	public void setContentDate(boolean contentDate) {
		// TODO Auto-generated method stub
		super.setContentDate(contentDate);
	}

	@Override
	public String getPublishURL() {
		// TODO Auto-generated method stub
		return super.getPublishURL();
	}

	@Override
	public void setPublishURL(String publishURL) {
		// TODO Auto-generated method stub
		super.setPublishURL(publishURL);
	}

	@Override
	public boolean isChildrenOfAssociation() {
		// TODO Auto-generated method stub
		return super.isChildrenOfAssociation();
	}

	@Override
	public void setChildrenOfAssociation(boolean childrenOfAssociation) {
		// TODO Auto-generated method stub
		super.setChildrenOfAssociation(childrenOfAssociation);
	}

	@Override
	public MenuElement getRootOfChildrenAssociation() {
		// TODO Auto-generated method stub
		return super.getRootOfChildrenAssociation();
	}

	@Override
	public void setRootOfChildrenAssociation(MenuElement rootOfChildrenAssociation) {
		// TODO Auto-generated method stub
		super.setRootOfChildrenAssociation(rootOfChildrenAssociation);
	}

	@Override
	public String getCreationDate() {
		// TODO Auto-generated method stub
		return super.getCreationDate();
	}

	@Override
	public void setCreationDate(String creationDate) {
		// TODO Auto-generated method stub
		super.setCreationDate(creationDate);
	}

	@Override
	public String getSortableDate() {
		// TODO Auto-generated method stub
		return super.getSortableDate();
	}

	@Override
	public void setSortableDate(String sortableDate) {
		// TODO Auto-generated method stub
		super.setSortableDate(sortableDate);
	}

	@Override
	public String getSortableCreationDate() {
		// TODO Auto-generated method stub
		return super.getSortableCreationDate();
	}

	@Override
	public void setSortableCreationDate(String sortableCreationDate) {
		// TODO Auto-generated method stub
		super.setSortableCreationDate(sortableCreationDate);
	}

	@Override
	public boolean isMailing() {
		// TODO Auto-generated method stub
		return super.isMailing();
	}

	@Override
	public void setMailing(boolean mailing) {
		// TODO Auto-generated method stub
		super.setMailing(mailing);
	}

	@Override
	public boolean isChildrenAssociation() {
		// TODO Auto-generated method stub
		return super.isChildrenAssociation();
	}

	@Override
	public void setChildrenAssociation(boolean childrenAssociation) {
		// TODO Auto-generated method stub
		super.setChildrenAssociation(childrenAssociation);
	}

	@Override
	public String getHumanName() {
		// TODO Auto-generated method stub
		return super.getHumanName();
	}

	@Override
	public void setHumanName(String humanName) {
		// TODO Auto-generated method stub
		super.setHumanName(humanName);
	}

	@Override
	public int getReactionSize() {
		// TODO Auto-generated method stub
		return super.getReactionSize();
	}

	@Override
	public void setReactionSize(int reactionSize) {
		// TODO Auto-generated method stub
		super.setReactionSize(reactionSize);
	}

	@Override
	public String getModificationDate() {
		// TODO Auto-generated method stub
		return super.getModificationDate();
	}

	@Override
	public String getContentDateValue() {
		// TODO Auto-generated method stub
		return super.getContentDateValue();
	}

	@Override
	public void setModificationDate(String modificationDate) {
		// TODO Auto-generated method stub
		super.setModificationDate(modificationDate);
	}

	@Override
	public String getSortableModificationDate() {
		// TODO Auto-generated method stub
		return super.getSortableModificationDate();
	}

	@Override
	public void setSortableModificationDate(String sortableModificationDate) {
		// TODO Auto-generated method stub
		super.setSortableModificationDate(sortableModificationDate);
	}

	@Override
	public List<PageBean> getChildren() throws Exception {
		// TODO Auto-generated method stub
		return super.getChildren();
	}

	@Override
	public String getTechnicalTitle() {
		// TODO Auto-generated method stub
		return super.getTechnicalTitle();
	}

	@Override
	public void setImage(Image image) {
		// TODO Auto-generated method stub
		super.setImage(image);
	}

	@Override
	public PageEvent getEvent() {
		// TODO Auto-generated method stub
		return super.getEvent();
	}

	@Override
	public void setEvent(PageEvent pageEvent) {
		// TODO Auto-generated method stub
		super.setEvent(pageEvent);
	}

	@Override
	public String getSortableCreationTime() {
		// TODO Auto-generated method stub
		return super.getSortableCreationTime();
	}

	@Override
	public void setSortableCreationTime(String sortableCreationTime) {
		// TODO Auto-generated method stub
		super.setSortableCreationTime(sortableCreationTime);
	}

	@Override
	public String getSortableModificationTime() {
		// TODO Auto-generated method stub
		return super.getSortableModificationTime();
	}

	@Override
	public void setSortableModificationTime(String sortableModificationTime) {
		// TODO Auto-generated method stub
		super.setSortableModificationTime(sortableModificationTime);
	}

	@Override
	public String getModificationTime() {
		// TODO Auto-generated method stub
		return super.getModificationTime();
	}

	@Override
	public void setModificationTime(String modificationTime) {
		// TODO Auto-generated method stub
		super.setModificationTime(modificationTime);
	}

	@Override
	public String getCreationTime() {
		// TODO Auto-generated method stub
		return super.getCreationTime();
	}

	@Override
	public void setCreationTime(String creationTime) {
		// TODO Auto-generated method stub
		super.setCreationTime(creationTime);
	}

	@Override
	public PageBean getParent() {
		// TODO Auto-generated method stub
		return super.getParent();
	}

	@Override
	public String getLinkLabel() {
		// TODO Auto-generated method stub
		return super.getLinkLabel();
	}

	@Override
	public void setLinkLabel(String linkLabel) {
		// TODO Auto-generated method stub
		super.setLinkLabel(linkLabel);
	}

	@Override
	public boolean isCurrentUserAsRight() {
		// TODO Auto-generated method stub
		return super.isCurrentUserAsRight();
	}

	@Override
	public void setCurrentUserAsRight(boolean currentUserAsRight) {
		// TODO Auto-generated method stub
		super.setCurrentUserAsRight(currentUserAsRight);
	}

	@Override
	public boolean isEditable() {
		// TODO Auto-generated method stub
		return super.isEditable();
	}

	@Override
	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
		super.setEditable(editable);
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return super.isActive();
	}

	@Override
	public int getSeoWeight() {
		// TODO Auto-generated method stub
		return super.getSeoWeight();
	}

	@Override
	public void setSeoWeight(int seoWeight) {
		// TODO Auto-generated method stub
		super.setSeoWeight(seoWeight);
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return super.getLabel();
	}

	@Override
	public void setLabel(String label) {
		// TODO Auto-generated method stub
		super.setLabel(label);
	}

	@Override
	public List<Event> getEvents() {
		// TODO Auto-generated method stub
		return super.getEvents();
	}

	@Override
	public boolean isModel() {
		// TODO Auto-generated method stub
		return super.isModel();
	}

	@Override
	public void setModel(boolean model) {
		// TODO Auto-generated method stub
		super.setModel(model);
	}

	@Override
	public PageContentMap getData() {
		// TODO Auto-generated method stub
		return super.getData();
	}

	@Override
	public int getToTheTopLevel() {
		// TODO Auto-generated method stub
		return super.getToTheTopLevel();
	}

	@Override
	public void setToTheTopLevel(int toTheTopLevel) {
		// TODO Auto-generated method stub
		super.setToTheTopLevel(toTheTopLevel);
	}

	@Override
	public ExtendedColor getColor() {
		// TODO Auto-generated method stub
		return super.getColor();
	}

	@Override
	public void setColor(ExtendedColor color) {
		// TODO Auto-generated method stub
		super.setColor(color);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}

}
