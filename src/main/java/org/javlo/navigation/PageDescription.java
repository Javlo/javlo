package org.javlo.navigation;

import org.javlo.bean.Link;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.image.ImageTitleBean;
import org.javlo.component.meta.ContactBean;
import org.javlo.ecom.Product.ProductBean;
import org.javlo.helper.StringHelper;
import org.javlo.image.ExtendedColor;
import org.javlo.service.event.Event;
import org.javlo.utils.HtmlPart;
import org.javlo.utils.TimeRange;

import java.io.Serializable;
import java.util.*;

/**
 * the description bean of the page, use for cache and JSTL.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class PageDescription implements Serializable {

	private static final long serialVersionUID = 1L;

	String title = null;
	String contentTitle = null;
	String localTitle = null;
	String subTitle = null;
	List<String> subTitles = null;
	int subTitleLevel = -1;
	String pageTitle = null;
	String forcedPageTitle = null;
	String linkOn = null;
	List<IImageTitle> images = null;
	Collection<Link> staticResources = null;
	HtmlPart description = null;
	String xhtmlDescription = null;
	String metaDescription = null;
	String keywords = null;
	String globalTitle = null;
	Date contentDate = null;
	Integer toTheTop = null;
	Boolean empty = null;
	Boolean realContent = null;
	String label = null;
	String location = null;
	String category = null;
	Double pageRank = null;
	List<String> tags = null;
	List<String> layouts = null;
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
	Boolean likeRoot = null;
	Boolean cacheable = null;
	int priority;
	String type = MenuElement.PAGE_TYPE_DEFAULT;
	String sharedName = null;
	Event event = null;
	List<Event> events = null;
	String slogan;
	ExtendedColor color;

	ExtendedColor backgroundColor;
	String linkLabel = null;
	Map<String, String> i18n = null;
	Map<String, String> genericData = null;
	ContactBean contactBean = null;
	String font = null;
	String metaHead = null;
	Map<String, Object> contentAsMap;
	Map<String, String> areaAsMap = new HashMap();
	ProductBean product = null;
	Map<String, List<String>> componentTypes;
	String css = null;

	String forward = null;

	Double weight = null;

	public String forcedUrl = null;

	public ImageTitleBean imageLink;

	public ImageTitleBean imageBackground;

	public ImageTitleBean imageBackgroundMobile;

	public Map<String, ImageTitleBean> imageAreaBackground;

	public Map<String, ImageTitleBean> imageAreaBackgroundMobile;

	public Collection<String> needdedResources = null;
	
	public Collection<String> needdedModules = null;

	private Map<String, Boolean> emptyArea = Collections.EMPTY_MAP;

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

	public String getFont() {
		return font;
	}

	public void setFont(String font) {
		this.font = font;
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

	public HtmlPart getDescription() {
		return description;
	}

	public void setDescription(HtmlPart description) {
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

	public Boolean isEmpty(String area) {
		return emptyArea.get(area);
	}

	public void setEmpty(String area, boolean empty) {
		if (emptyArea == Collections.EMPTY_MAP) {
			emptyArea = new HashMap<String, Boolean>();
		}
		emptyArea.put(area, empty);
	}

	public Map<String, Boolean> getEmptyArea() {
		return emptyArea;
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

	public String getFirstTag() {
		List<String> tags = getTags();
		if (tags == null || tags.size() == 0) {
			return null;
		} else {
			return tags.get(0);
		}
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSharedName() {
		return StringHelper.neverNull(sharedName);
	}

	public void setSharedName(String sharedName) {
		this.sharedName = sharedName;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public void setSlogan(String slogan) {
		this.slogan = slogan;
	}

	public String getSlogan() {
		return slogan;
	}

	public String getLinkLabel() {
		return linkLabel;
	}

	public void setLinkLabel(String linkLabel) {
		this.linkLabel = linkLabel;
	}

	public Map<String, String> getI18n() {
		return i18n;
	}

	public void setI18n(Map<String, String> i18n) {
		this.i18n = i18n;
	}

	public Boolean getLikeRoot() {
		return likeRoot;
	}

	public void setLikeRoot(Boolean likeRoot) {
		this.likeRoot = likeRoot;
	}

	public String getForward() {
		return forward;
	}

	public void setForward(String forward) {
		this.forward = forward;
	}

	public Map<String, Object> getContentAsMap() {
		return contentAsMap;
	}

	public void setContentAsMap(Map<String, Object> contentAsMap) {
		this.contentAsMap = contentAsMap;
	}
	
	public ProductBean getProduct() {
		return product;
	}
	
	public void setProduct(ProductBean product) {
		this.product = product;
	}
	
	public void setComponentTypes(Map<String, List<String>> componentTypes) {
		this.componentTypes = componentTypes;
	}
	
	public Map<String, List<String>> getComponentTypes() {
		return componentTypes;
	}

}