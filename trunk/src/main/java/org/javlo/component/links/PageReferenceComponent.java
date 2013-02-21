/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.javlo.actions.IAction;
import org.javlo.bean.Link;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComplexPropertiesLink;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.meta.Tags;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.PaginationContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.XHTMLNavigationHelper;
import org.javlo.helper.Comparator.MenuElementCreationDateComparator;
import org.javlo.helper.Comparator.MenuElementGlobalDateComparator;
import org.javlo.helper.Comparator.MenuElementModificationDateComparator;
import org.javlo.helper.Comparator.MenuElementPopularityComparator;
import org.javlo.helper.Comparator.MenuElementVisitComparator;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.content.Edit;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.RequestService;

/**
 * list of links to a subset of pages. <h4>exposed variable :</h4>
 * <ul>
 * <li>inherited from {@link AbstractVisualComponent}</li>
 * <li>{@link PageStatus} pagesStatus : root page of menu. See {@link #getRootPage}.</li>
 * <li>{@link PageBean} pages : list of pages selected to display.</li>
 * <li>{@link String} title : title of the page list. See {@link #getContentTitle}</li>
 * <li>{@link PageReferenceComponent} comp : current component.</li>
 * <li>{@link String} firstPage : first page rendered in xHTML.</li>
 * </ul>
 * 
 * @author pvandermaesen
 */
public class PageReferenceComponent extends ComplexPropertiesLink implements IAction {

	public static final String MOUNT_FORMAT = "MMMM yyyy";

	public static class PageBean {

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

		private static PageBean getInstance(ContentContext ctx, MenuElement page, PageReferenceComponent comp) throws Exception {

			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

			Iterator<String> defaultLg = globalContext.getDefaultLanguages().iterator();

			defaultLg = globalContext.getContentLanguages().iterator();
			ContentContext tagCtx = new ContentContext(ctx);
			while (page.getContentByType(tagCtx, Tags.TYPE).size() == 0 && defaultLg.hasNext()) {
				String lg = defaultLg.next();
				tagCtx.setContentLanguage(lg);
				tagCtx.setRequestContentLanguage(lg);
			}

			PageBean bean = new PageBean();
			bean.language = ctx.getRequestContentLanguage();
			bean.title = page.getTitle(ctx);
			bean.subTitle = page.getSubTitle(ctx);
			bean.realContent = page.isRealContent(ctx);
			bean.attTitle = XHTMLHelper.stringToAttribute(page.getTitle(ctx));
			bean.description = page.getDescription(ctx);
			bean.location = page.getLocation(ctx);
			bean.category = page.getCategory(ctx);
			bean.visible = page.isVisible();
			bean.path = page.getPath();
			bean.setCategoryKey("category." + StringHelper.neverNull(page.getCategory(ctx)).toLowerCase().replaceAll(" ", ""));

			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			ContentContext realContentCtx = new ContentContext(ctx);
			realContentCtx.setLanguage(realContentCtx.getRequestContentLanguage());
			i18nAccess.changeViewLanguage(realContentCtx);
			bean.categoryLabel = i18nAccess.getViewText(bean.getCategoryKey());

			for (String tag : page.getTags(tagCtx)) {
				bean.addTagLabel(i18nAccess.getViewText("tag." + tag));
			}

			i18nAccess.changeViewLanguage(ctx);

			bean.id = page.getId();
			bean.name = page.getName();
			bean.selected = page.isSelected(ctx);
			bean.linkOn = page.getLinkOn(ctx);
			if (page.getContentDate(ctx) != null) {
				bean.date = StringHelper.renderShortDate(ctx, page.getContentDate(ctx));
			} else {
				bean.date = StringHelper.renderShortDate(ctx, page.getModificationDate());
			}
			if (page.getTimeRange(ctx) != null) {
				bean.startDate = StringHelper.renderShortDate(ctx, page.getTimeRange(ctx).getStartDate());
				bean.endDate = StringHelper.renderShortDate(ctx, page.getTimeRange(ctx).getEndDate());
			} else {
				bean.startDate = bean.date;
				bean.endDate = bean.date;
			}
			bean.url = URLHelper.createURL(ctx, page.getPath());

			String filter = comp.getConfig(ctx).getProperty("filter-image", "reference-list");

			IImageTitle image = page.getImage(ctx);
			if (image != null) {
				bean.imagePath = image.getResourceURL(ctx);
				bean.imageURL = URLHelper.createTransformURL(ctx, page, image.getResourceURL(ctx), filter);
				bean.viewImageURL = URLHelper.createTransformURL(ctx, page, image.getResourceURL(ctx), "thumb-view");
				bean.imageDescription = XHTMLHelper.stringToAttribute(image.getImageDescription(ctx));
			}
			Collection<IImageTitle> images = page.getImages(ctx);

			for (IImageTitle imageItem : images) {
				String imagePath = imageItem.getResourceURL(ctx);
				String imageURL = URLHelper.createTransformURL(ctx, page, imageItem.getResourceURL(ctx), filter);
				String viewImageURL = URLHelper.createTransformURL(ctx, page, imageItem.getResourceURL(ctx), "thumb-view");
				String imageDescription = XHTMLHelper.stringToAttribute(imageItem.getImageDescription(ctx));
				String cssClass = "";
				String linkURL = imageItem.getImageLinkURL(ctx);
				if (linkURL != null) {
					if (linkURL.equals(IImageTitle.NO_LINK)) {
						cssClass = "no-link";
						viewImageURL = null;
						linkURL = null;
					} else {
						cssClass = "link " + StringHelper.getPathType(linkURL, "");
					}
				}
				PageBean.Image imageBean = new PageBean.Image(imageURL, viewImageURL, linkURL, cssClass, imageDescription, imagePath);
				bean.getImages().add(imageBean);
			}

			bean.staticResources = page.getStaticResources(realContentCtx);

			Collection<String> lgs = globalContext.getContentLanguages();
			for (String lg : lgs) {
				ContentContext lgCtx = new ContentContext(ctx);
				lgCtx.setRequestContentLanguage(lg);
				lgCtx.setContentLanguage(lg);
				if (!page.isEmpty(lgCtx)) {
					Link link = new Link(URLHelper.createURL(lgCtx, page.getPath()), lg);
					bean.links.add(link);
				}
			}
			if (bean.links.size() == 0) {
				bean.links = null;
			}
			bean.setTags(page.getTags(tagCtx));
			return bean;
		}

		private String id = null;
		private String name = null;
		private boolean selected = false;
		private String title = null;
		private String subTitle = null;
		private String attTitle = null;
		private String description = null;
		private String location = null;
		private String category = null;
		private String categoryLabel = null;
		private String categoryKey = null;
		private String imageURL = null;
		private String imagePath = null;
		private String imageDescription = null;
		private String date = null;
		private String startDate = null;
		private String endDate = null;
		private String url = null;
		private String language;
		private String viewImageURL = null;
		private String linkOn = null;
		private String rawTags = null;
		private String path = null;
		private boolean realContent = false;
		private boolean visible = false;
		private Collection<Link> links = new LinkedList<Link>();
		private Collection<Link> staticResources = new LinkedList<Link>();
		private final Collection<Image> images = new LinkedList<Image>();

		private Collection<String> tags = new LinkedList<String>();
		private final Collection<String> tagsLabel = new LinkedList<String>();

		public String getAttTitle() {
			return attTitle;
		}

		public String getCategory() {
			return category;
		}

		public String getDate() {
			return date;
		}

		public String getDescription() {
			return description;
		}

		public String getEndDate() {
			return endDate;
		}

		public String getId() {
			return id;
		}

		public String getImageDescription() {
			return imageDescription;
		}

		public String getImagePath() {
			return imagePath;
		}

		public Collection<Image> getImages() {
			return images;
		}

		public String getImageURL() {
			return imageURL;
		}

		public String getLanguage() {
			return language;
		}

		public String getForceLinkOn() {
			return linkOn;
		}

		public String getLinkOn() {
			if (linkOn != null && !isRealContent()) {
				return linkOn;
			} else {
				return getUrl();
			}
		}

		public Collection<Link> getLinks() {
			return links;
		}

		public String getLocation() {
			return location;
		}

		public String getName() {
			return name;
		}

		public String getRawTags() {
			return rawTags;
		}

		public String getStartDate() {
			return startDate;
		}

		public String getSubTitle() {
			return subTitle;
		}

		public Collection<String> getTags() {
			return tags;
		}

		public Collection<String> getTagsLabel() {
			return tagsLabel;
		}

		public String getTitle() {
			return title;
		}

		public String getUrl() {
			return url;
		}

		public String getViewImageURL() {
			return viewImageURL;
		}

		public boolean isRealContent() {
			return realContent;
		}

		public boolean isSelected() {
			return selected;
		}

		public void setEndDate(String endDate) {
			this.endDate = endDate;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setImagePath(String imagePath) {
			this.imagePath = imagePath;
		}

		public void setRawTags(String rawTags) {
			this.rawTags = rawTags;
		}

		public void setStartDate(String startDate) {
			this.startDate = startDate;
		}

		public void setSubTitle(String subTitle) {
			this.subTitle = subTitle;
		}

		public void addTagLabel(String tagLabel) {
			tagsLabel.add(tagLabel);
		}

		public void setTags(Collection<String> tags) {
			String sep = "";
			rawTags = "";
			for (String tag : tags) {
				rawTags = rawTags + sep + tag.toLowerCase().replace(' ', '-');
				sep = " ";
			}
			this.tags = tags;
		}

		public String getCategoryKey() {
			return categoryKey;
		}

		public void setCategoryKey(String categoryKey) {
			this.categoryKey = categoryKey;
		}

		public String getCategoryLabel() {
			return categoryLabel;
		}

		public void setCategoryLabel(String categoryLabel) {
			this.categoryLabel = categoryLabel;
		}

		public boolean isVisible() {
			return visible;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

		public Collection<Link> getStaticResources() {
			return staticResources;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

	}

	public static class PagesStatus {
		private int totalSize = 0;
		private int realContentSize = 0;

		public PagesStatus(int totalSize, int realContentSize) {
			super();
			this.totalSize = totalSize;
			this.realContentSize = realContentSize;
		}

		public int getRealContentSize() {
			return realContentSize;
		}

		public int getTotalSize() {
			return totalSize;
		}

		public void setRealContentSize(int realContentSize) {
			this.realContentSize = realContentSize;
		}

		public void setTotalSize(int totalSize) {
			this.totalSize = totalSize;
		}
	}

	public static final String TYPE = "page-reference";

	private static final String PAGE_REF_PROP_KEY = "page-ref";

	private static final String PAGE_START_PROP_KEY = "page-start";

	private static final String PAGE_END_PROP_KEY = "page-end";

	private static final String ORDER_KEY = "order";

	private static final String PARENT_NODE_PROP_KEY = "parent-node";

	private static final String TAG_KEY = "tag";

	private static final String DEFAULT_SELECTED_PROP_KEY = "is-def-selected";

	private static Logger logger = Logger.getLogger(PageReferenceComponent.class.getName());

	private static final String ID_SEPARATOR = ";";

	private static final String PAGE_SEPARATOR = ";";

	private static final String ALWAYS = "ALWAYS";

	private static final String STAY_1D = "S1D";

	private static final String STAY_3D = "S3D";

	private static final String STAY_1W = "S1W";

	private static final String STAY_1M = "S1M";

	private static final String STAY_1Y = "S1Y";

	private static final String STAY_3N = "S3N";

	private static final String STAY_6N = "S6N";

	private static final String STAY_10N = "S10N";

	private static final String STAY_24N = "S24N";

	private static final String STAY_72N = "S72N";

	private static final List<String> TIME_SELECTION_OPTIONS = Arrays.asList(new String[] { "before", "inside", "after" });

	private static final String TIME_SELECTION_KEY = "time-selection";

	private static final String DISPLAY_FIRST_PAGE_KEY = "display-first-page";

	private static final String CHANGE_ORDER_KEY = "reverse-order";

	private static final String WIDTH_EMPTY_PAGE_PROP_KEY = "width_empty";

	public static final Integer getCurrentMonth(HttpSession session) {
		return (Integer) session.getAttribute("___current_month");
	}

	/************/
	/** ACTION **/
	/************/

	public static final Integer getCurrentYear(HttpSession session) {
		return (Integer) session.getAttribute("___current-year");
	}

	public static final String performCalendar(HttpServletRequest request, HttpServletResponse response) {
		RequestService requestService = RequestService.getInstance(request);

		String newYear = requestService.getParameter("year", null);
		if (newYear != null) {
			setCurrentYear(request.getSession(), Integer.parseInt(newYear));
		}
		String newMonth = requestService.getParameter("month", null);
		if (newMonth != null) {
			setCurrentMonth(request.getSession(), Integer.parseInt(newMonth));
		}

		return null;
	}

	public static final void setCurrentMonth(HttpSession session, int currentMonth) {
		session.setAttribute("___current_month", currentMonth);
	}

	public static final void setCurrentYear(HttpSession session, int currentYear) {
		session.setAttribute("___current-year", currentYear);
	}

	/**
	 * filter the page
	 * 
	 * @param ctx
	 *            current contentcontext
	 * @param page
	 *            a page
	 * @return true if page is accepted
	 * @throws Exception
	 */
	protected boolean filterPage(ContentContext ctx, MenuElement page) throws Exception {

		ContentContext lgDefaultCtx = new ContentContext(ctx);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Iterator<String> contentLg = globalContext.getContentLanguages().iterator();

		if (getTimeSelection() != null) {
			Date today = new Date();
			boolean timeAccept = false;
			if (getTimeSelection().contains(TIME_SELECTION_OPTIONS.get(0))) {
				if (page.getTimeRange(ctx).isAfter(today)) {
					timeAccept = true;
				}
			}
			if (getTimeSelection().contains(TIME_SELECTION_OPTIONS.get(1))) {
				if (page.getTimeRange(ctx).isInside(today)) {
					timeAccept = true;
				}
			}
			if (getTimeSelection().contains(TIME_SELECTION_OPTIONS.get(2))) {
				if (page.getTimeRange(ctx).isBefore(today)) {
					timeAccept = true;
				}
			}
			if (!timeAccept) {
				return false;
			}
		}

		while (page.getContentByType(lgDefaultCtx, Tags.TYPE).size() == 0 && contentLg.hasNext()) {
			String lg = contentLg.next();
			lgDefaultCtx.setContentLanguage(lg);
			lgDefaultCtx.setRequestContentLanguage(lg);
		}

		if (getTag().length() == 0) {
			return true;
		}
		if (page.getTags(lgDefaultCtx).contains(getTag())) {
			return true;
		}
		return false;
	}

	protected Calendar getBackDate(ContentContext ctx) {
		Calendar backDate = Calendar.getInstance();
		int backDay = 9999; /*
							 * infinity back if no back day defined (all news included)
							 */
		String style = getStyle(ctx);
		if (style.equals(STAY_1D)) {
			backDay = 1;
		} else if (style.equals(STAY_3D)) {
			backDay = 3;
		} else if (style.equals(STAY_1W)) {
			backDay = 7;
		} else if (style.equals(STAY_1M)) {
			backDay = 30;
		} else if (style.equals(STAY_1Y)) {
			backDay = 365;
		}
		while (backDay > 365) {
			backDate.roll(Calendar.YEAR, false);
			backDay = backDay - 365;
		}
		if (backDate.get(Calendar.DAY_OF_YEAR) <= backDay) {
			backDate.roll(Calendar.YEAR, false);
			backDay = backDay + 365;
		}
		backDate.roll(Calendar.DAY_OF_YEAR, -backDay);
		return backDate;
	}

	protected String getCompInputName() {
		return "comp_" + getId();
	}

	@Override
	public int getComplexityLevel() {
		return COMPLEXITY_ADMIN;
	}

	private String getContentTitle() {
		return properties.getProperty("content-title", "");
	}

	public String getCSSClassName(ContentContext ctx) {
		return getStyle(ctx);
	}

	protected String getDefaultSelectedInputName() {
		return "default-selected-" + getId();
	}

	@Override
	protected String getDisplayAsInputName() {
		return "display-as-" + getId();
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getEditXHTMLCode(ContentContext ctx) throws Exception {

		Calendar backDate = getBackDate(ctx);

		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement menu = content.getNavigation(ctx);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		out.println("<input type=\"hidden\" name=\"" + getCompInputName() + "\" value=\"true\" />");

		out.println("<fieldset class=\"config\">");
		out.println("<legend>" + i18nAccess.getText("global.config") + "</legend>");
		/* by default selected */
		out.println("<div class=\"line\">");
		out.println(XHTMLHelper.getCheckbox(getDefaultSelectedInputName(), isDefaultSelected()));
		out.println("<label for=\"" + getDefaultSelectedInputName() + "\">" + i18nAccess.getText("content.page-teaser.default-selected") + "</label></div>");
		/* parent node */
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getParentNodeInputName() + "\">" + i18nAccess.getText("content.page-teaser.parent-node") + " : </label>");
		out.println(XHTMLNavigationHelper.renderComboNavigation(ctx, menu, getParentNodeInputName(), getParentNode()));
		out.println("</div>");
		/* sequence of pages */
		out.println("<div class=\"line-inline\">");
		out.println("<label for=\"" + getFirstPageNumberInputName() + "\">" + i18nAccess.getText("content.page-teaser.start-page") + " : </label>");
		out.println("<input id=\"" + getFirstPageNumberInputName() + "\" name=\"" + getFirstPageNumberInputName() + "\" value=\"" + getFirstPageNumber() + "\"/>");
		out.println("<label for=\"" + getLastPageNumberInputName() + "\">" + i18nAccess.getText("content.page-teaser.end-page") + " : </label>");
		String lastValue = "" + getLastPageNumber();
		if (getLastPageNumber() == Integer.MAX_VALUE) {
			lastValue = "";
		}
		out.println("<input id=\"" + getLastPageNumberInputName() + "\" name=\"" + getLastPageNumberInputName() + "\" value=\"" + lastValue + "\"/>");
		out.println("</div>");
		/* time selection */
		out.println("<div class=\"line-inline\">");
		out.println("<label>" + i18nAccess.getText("content.page-teaser.time-selection") + " : </label>");
		// out.println(XHTMLHelper.getInputOneSelectFirstEnpty(getTimeSelectionInputName(null), getTimeSelectionOptions(), ""+getTimeSelection(), false));
		for (String option : getTimeSelectionOptions()) {
			String selected = "";
			if (getTimeSelection().contains(option)) {
				selected = " checked=\"checked\"";
			}
			out.println("<input type=\"checkbox\" name=\"" + getTimeSelectionInputName(option) + "\"" + selected + " />");
			out.println("<label for=\"" + getTimeSelectionInputName(option) + "\">" + i18nAccess.getText("content.page-teaser." + option, option) + "</label>");
		}
		out.println("</div>");
		/* first page full */
		out.println("<div class=\"line\">");
		String selected = "";
		if (isDisplayFirstPage()) {
			selected = " checked=\"checked\"";
		}
		out.println("<input type=\"checkbox\" name=\"" + getInputFirstPageFull() + "\"" + selected + " />");
		out.println("<label for=\"" + getInputFirstPageFull() + "\">" + i18nAccess.getText("content.display-first-page") + "</label>");
		out.println("</div>");

		/* tag filter */
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (globalContext.isTags() && globalContext.getTags().size() > 0) {
			out.println("<div class=\"line\">");
			out.println("<label for=\"" + getTagsInputName() + "\">" + i18nAccess.getText("content.page-teaser.tag") + " : </label>");
			out.println(XHTMLHelper.getInputOneSelectFirstEnpty(getTagsInputName(), globalContext.getTags(), getTag()));
			out.println("</div>");
		}
		out.println("<div class=\"line\">");
		out.println(XHTMLHelper.getCheckbox(getWidthEmptyPageInputName(), isWidthEmptyPage()));
		out.println("<label for=\"" + getWidthEmptyPageInputName() + "\">" + i18nAccess.getText("content.page-teaser.width-empty-page") + "</label></div>");
		out.println("</fieldset>");

		out.println("<fieldset class=\"order\">");
		out.println("<legend>" + i18nAccess.getText("global.order") + "</legend>");

		out.println("<div class=\"line\">");
		out.println(XHTMLHelper.getCheckbox(getReverseOrderInput(), isReverseOrder()));
		out.println("<label for=\"" + getReverseOrderInput() + "\">" + i18nAccess.getText("content.page-teaser.reverse-order") + "</label></div>");

		out.println("<div class=\"line\">");
		out.println(XHTMLHelper.getRadio(getOrderInputName(), "no-order", getOrder()));
		out.println("<label for=\"date\">" + i18nAccess.getText("content.page-teaser.no-order") + "</label></div>");
		out.println("<div class=\"line\">");
		out.println(XHTMLHelper.getRadio(getOrderInputName(), "date", getOrder()));
		out.println("<label for=\"date\">" + i18nAccess.getText("content.page-teaser.order-date") + "</label></div>");
		out.println("<div class=\"line\">");
		out.println(XHTMLHelper.getRadio(getOrderInputName(), "creation", getOrder()));
		out.println("<label for=\"date\">" + i18nAccess.getText("content.page-teaser.order-creation") + "</label></div>");
		out.println("<div class=\"line\">");
		out.println(XHTMLHelper.getRadio(getOrderInputName(), "visit", getOrder()));
		out.println("<label for=\"visit\">" + i18nAccess.getText("content.page-teaser.order-visit") + "</label></div>");
		out.println("<div class=\"line\">");
		out.println(XHTMLHelper.getRadio(getOrderInputName(), "popularity", getOrder()));
		out.println("<label for=\"popularity\">" + i18nAccess.getText("content.page-teaser.order-popularity") + "</label></div>");

		out.println("</fieldset>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputNameTitle() + "\">" + i18nAccess.getText("global.title") + " : </label>");
		out.println("<input type=\"text\" id=\"" + getInputNameTitle() + "\" name=\"" + getInputNameTitle() + "\" value=\"" + getContentTitle() + "\"  />");
		out.println("</div>");

		/* array filter */
		String filterID = "filter-" + getId();
		String tableID = "table-" + getId();
		out.println("<div class=\"line\" style=\"margin-left: 5px;\">");
		out.println("<label for=\"" + filterID + "\">" + i18nAccess.getText("global.filter") + " : </label>");
		out.println("<input type=\"text\" onkeyup=\"filter(this.value, '." + tableID + " .filtered');\"/>");
		out.println("</div>");

		out.print("<div class=\"page-list-container\"><table class=\"");
		out.print("page-list" + ' ' + tableID);
		out.println("\"><tr><th>" + i18nAccess.getText("global.label") + "</th><th>" + i18nAccess.getText("global.date") + "</th><th>" + i18nAccess.getText("global.modification") + "</th><th>" + i18nAccess.getText("content.page-teaser.language") + "</th><th>" + i18nAccess.getText("global.select") + "</th></tr>");

		MenuElement basePage = null;
		if (getParentNode().length() > 1) { // if parent node is not root node
			basePage = menu.searchChild(ctx, getParentNode());
		}
		if (basePage != null) {
			menu = basePage;
		}
		MenuElement[] allChildren = menu.getAllChildren();
		Arrays.sort(allChildren, new MenuElementModificationDateComparator(true));
		Set<String> currentSelection = getPagesId(ctx, allChildren);

		int numberOfPage = 16384;
		if (allChildren.length < numberOfPage) {
			numberOfPage = allChildren.length;
		}

		for (int i = 0; i < numberOfPage; i++) {
			ContentContext newCtx = new ContentContext(ctx);
			newCtx.setArea(null);
			ContentContext lgCtx = ctx;
			if (GlobalContext.getInstance(ctx.getRequest()).isAutoSwitchToDefaultLanguage()) {
				lgCtx = allChildren[i].getContentContextWithContent(ctx);
			}
			if (filterPage(lgCtx, allChildren[i]) && (allChildren[i].getContentDateNeverNull(ctx).after(backDate.getTime()))) {
				String editPageURL = URLHelper.createEditURL(allChildren[i].getPath(), ctx);
				out.print("<tr class=\"filtered\"><td><a href=\"" + editPageURL + "\">" + allChildren[i].getFullLabel(lgCtx) + "</a></td>");
				out.print("<td>" + StringHelper.neverNull(StringHelper.renderLightDate(allChildren[i].getContentDate(lgCtx))) + "</td>");
				out.println("<td>" + StringHelper.renderLightDate(allChildren[i].getModificationDate()) + "</td><td>" + lgCtx.getRequestContentLanguage() + "</td>");

				String checked;

				checked = "";
				if (currentSelection.contains(allChildren[i].getId())) {
					checked = " checked=\"checked\"";
				}

				out.print("<td><input type=\"checkbox\" name=\"" + getPageId(allChildren[i]) + "\" value=\"" + allChildren[i].getId() + "\"" + checked + "/></td></tr>");
			}
		}

		out.println("</tr></table></div>");
		return new String(outStream.toByteArray());
	}

	private int getFirstPageNumber() {
		return Integer.parseInt(properties.getProperty(PAGE_START_PROP_KEY, "1"));
	}

	private String getFirstPageNumberInputName() {
		return "first_page_number_" + getId();
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	private String getInputNameTitle() {
		return "title_" + getId();
	}

	private int getLastPageNumber() {
		if (properties.getProperty(PAGE_END_PROP_KEY, "").trim().length() == 0) {
			return Integer.MAX_VALUE;
		} else {
			return Integer.parseInt(properties.getProperty(PAGE_END_PROP_KEY, "" + Integer.MAX_VALUE));
		}
	}

	private String getLastPageNumberInputName() {
		return "last_page_number_" + getId();
	}

	protected int getMaxNews(ContentContext ctx) {
		String style = getStyle(ctx);
		if (style.equals(STAY_3N)) {
			return 3;
		} else if (style.equals(STAY_6N)) {
			return 6;
		} else if (style.equals(STAY_10N)) {
			return 10;
		} else if (style.equals(STAY_24N)) {
			return 24;
		} else if (style.equals(STAY_72N)) {
			return 72;
		}
		return 99999; /* infinity news if no limit defined (all news included) */
	}

	protected String getOrder() {
		return properties.getProperty(ORDER_KEY, "date");
	}

	protected String getOrderInputName() {
		return "orde-" + getId();
	}

	protected String getPageId(MenuElement page) {
		return "page_" + getId() + "_" + page.getId();
	}

	protected Set<String> getPagesId(ContentContext ctx, MenuElement[] children) throws Exception {
		String value = properties.getProperty(PAGE_REF_PROP_KEY, "");
		Set<String> out = new TreeSet<String>();
		String[] deserializedId = StringHelper.split(value, PAGE_SEPARATOR);

		out.addAll(Arrays.asList(deserializedId));

		if (isDefaultSelected()) {
			Set<String> selectedPage = new TreeSet<String>();
			MenuElement parentNode = null;
			if (children.length > 0) {
				parentNode = children[0].getRoot().searchChild(ctx, getParentNode());
			}
			for (int i = 0; i < children.length; i++) {
				if (!out.contains(children[i].getId())) {
					if (parentNode == null || children[i].isChildOf(parentNode)) {
						selectedPage.add(children[i].getId());
					}
				}
			}
			out = selectedPage;
		}
		return out;
	}

	protected String getParentNode() {
		return properties.getProperty(PARENT_NODE_PROP_KEY, "/");
	}

	protected String getParentNodeInputName() {
		return "parent-node-" + getId();
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {

		if (getConfig(ctx).getProperty("prefix", null) != null) {
			String prefix = "";
			if (ctx.isPreview()) {
				prefix = "<div " + getSpecialPreviewCssClass(ctx, "") + getSpecialPreviewCssId(ctx) + ">";
			}
			return prefix + getConfig(ctx).getProperty("prefix", null);
		}

		String specialClass = "";
		if (isDateOrder()) {
			specialClass = " date-order" + specialClass;
		} else if (isCreationOrder()) {
			specialClass = " creation-order" + specialClass;
		} else if (isVisitOrder()) {
			specialClass = " visit-order" + specialClass;
		} else if (isPopularityOrder()) {
			specialClass = " popularity-order" + specialClass;
		}
		return "<div " + getSpecialPreviewCssClass(ctx, "page-reference" + specialClass) + getSpecialPreviewCssId(ctx) + ">";
	}

	protected String getReverseOrderInput() {
		return "reserve-order-" + getId();
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String[] styles = getStyleList(ctx);
			String[] res = new String[styles.length];
			for (int i = 0; i < styles.length; i++) {
				res[i] = i18nAccess.getText("page-teaser.rules." + styles[i]);
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getStyleList(ctx);
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { ALWAYS, STAY_1D, STAY_3D, STAY_1W, STAY_1M, STAY_1Y, STAY_3N, STAY_6N, STAY_10N, STAY_24N, STAY_72N };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return i18nAccess.getText("page-teaser.rules");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "page-teaser-rules";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		if (getConfig(ctx).getProperty("suffix", null) != null) {
			String suffix = "";
			if (ctx.isPreview()) {
				suffix = "</div>";
			}
			return getConfig(ctx).getProperty("suffix", null) + suffix;
		}
		return "</div>";
	}

	protected String getTag() {
		return properties.getProperty(TAG_KEY, "");
	}

	protected String getTagsInputName() {
		return "tag-" + getId();
	}

	private Collection<String> getTimeSelection() {
		if (properties.getProperty(TIME_SELECTION_KEY, null) == null) {
			return getTimeSelectionOptions();
		}
		return StringHelper.stringToCollection(properties.getProperty(TIME_SELECTION_KEY, null));
	}

	protected boolean isDisplayFirstPage() {
		return StringHelper.isTrue(properties.getProperty(DISPLAY_FIRST_PAGE_KEY, "false"));
	}

	protected void setDisplayFirstPage(boolean value) {
		properties.setProperty(DISPLAY_FIRST_PAGE_KEY, "" + value);
	}

	protected String getInputFirstPageFull() {
		return "first-page-full-" + getId();
	}

	protected String getTimeSelectionInputName(String option) {
		return "time-selection-" + getId() + '-' + option;
	}

	private Collection<String> getTimeSelectionOptions() {
		return TIME_SELECTION_OPTIONS;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	protected String getWidthEmptyPageInputName() {
		return "width-empty-page-" + getId();
	}

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
		if (getValue().trim().length() > 0) {
			properties.load(stringToStream(getValue()));
			if (properties.getProperty("type") != null) {
				setRenderer(newContext, properties.getProperty("type"));
				properties.remove("type");
			}
		}
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return StringHelper.isTrue(getConfig(ctx).getProperty("config.cache", null), false);
	}

	@Override
	public boolean isContentCachableByQuery(ContentContext ctx) {
		return StringHelper.isTrue(getConfig(ctx).getProperty("config.cache-query", null), true);
	}

	@Override
	public boolean isContentTimeCachable(ContentContext ctx) {
		return StringHelper.isTrue(getConfig(ctx).getProperty("config.time-cache", null), true);
	}

	private boolean isCreationOrder() {
		return getOrder().equals("creation");
	}

	private boolean isDateOrder() {
		return getOrder().equals("date");
	}

	private boolean isNoOrder() {
		return getOrder().equals("no-order");
	}

	private boolean isDefaultSelected() {
		return StringHelper.isTrue(properties.getProperty(DEFAULT_SELECTED_PROP_KEY, "false"));
	}

	private boolean isPopularityOrder() {
		return getOrder().equals("popularity");
	}

	protected boolean isReverseOrder() {
		return StringHelper.isTrue(properties.getProperty(CHANGE_ORDER_KEY, "false"));
	}

	private boolean isVisitOrder() {
		return getOrder().equals("visit");
	}

	private boolean isWidthEmptyPage() {
		return StringHelper.isTrue(properties.getProperty(WIDTH_EMPTY_PAGE_PROP_KEY, "false"));
	}

	public int getPageSize(ContentContext ctx) {
		String size = getConfig(ctx).getProperty("page.size", null);
		if (size == null) {
			return 10; // default value
		} else {
			return Integer.parseInt(size);
		}
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		Calendar backDate = getBackDate(ctx);

		SimpleDateFormat format = new SimpleDateFormat(MOUNT_FORMAT, new Locale(ctx.getRequestContentLanguage()));

		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement menu = content.getNavigation(ctx);
		MenuElement[] allChildren = menu.getAllChildren();

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		new PrintStream(outStream);

		boolean ascending = false;
		Calendar todayCal = Calendar.getInstance();
		Calendar pageCal = Calendar.getInstance();

		Set<String> selectedPage = getPagesId(ctx, allChildren);
		List<MenuElement> pages = new LinkedList<MenuElement>();
		List<PageBean> pageBeans = new LinkedList<PageBean>();

		int firstPageNumber = getFirstPageNumber();
		int lastPageNumber = getLastPageNumber();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NavigationService navigationService = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
		for (String pageId : selectedPage) {
			MenuElement page = navigationService.getPage(ctx, pageId);
			ContentContext lgCtx = page.getContentContextWithContent(ctx);
			if (page != null) {
				Date pageDate = page.getModificationDate();
				Date contentDate;
				contentDate = page.getContentDate(lgCtx);
				if (contentDate != null) {
					boolean futurPage = page.getCreationDate().getTime() - page.getContentDate(lgCtx).getTime() < 0;
					if (!futurPage) {
						ascending = true;
					}
					pageDate = page.getContentDate(lgCtx);
				}
				pageCal.setTime(pageDate);
				if (todayCal.after(pageCal)) {
					ascending = true;
				}
				pages.add(page);
			} else {
				logger.warning("page not found : " + pageId);
			}
		}

		if (isReverseOrder()) {
			ascending = !ascending;
		}

		if (!isNoOrder()) {
			if (isDateOrder()) {
				Collections.sort(pages, new MenuElementGlobalDateComparator(ctx, ascending));
			} else if (isCreationOrder()) {
				Collections.sort(pages, new MenuElementCreationDateComparator(ascending));
			} else if (isVisitOrder()) {
				if (getMaxNews(ctx) > 100) {
					Collections.sort(pages, new MenuElementVisitComparator(ctx, true));
				} else {
					visitSorting(ctx, pages, getMaxNews(ctx));
				}
			} else if (isPopularityOrder()) {
				if (getMaxNews(ctx) > 100) {
					Collections.sort(pages, new MenuElementPopularityComparator(ctx, true));
				} else {
					popularitySorting(ctx, pages, getMaxNews(ctx));
				}
			}
		}

		int countPage = 0;
		int realContentSize = 0;
		MenuElement firstPage = null;

		String tagFilter = null;
		Calendar startDate = null;
		Calendar endDate = null;
		String catFilter = null;
		String monthFilter = null;

		if (ctx.getRequest().getParameter("comp_id") == null || ctx.getRequest().getParameter("comp_id").equals(getId())) {
			tagFilter = ctx.getRequest().getParameter("tag");
			catFilter = ctx.getRequest().getParameter("category");
			monthFilter = ctx.getRequest().getParameter("month");
			if (monthFilter != null && monthFilter.trim().length() > 0) {
				startDate = Calendar.getInstance();
				endDate = Calendar.getInstance();
				Date mount = format.parse(monthFilter);
				startDate.setTime(mount);
				endDate.setTime(mount);
				startDate = TimeHelper.convertRemoveAfterMonth(startDate);
				endDate = TimeHelper.convertRemoveAfterMonth(endDate);
				endDate.add(Calendar.MONTH, 1);
				endDate.add(Calendar.MILLISECOND, -1);
			} else {
				monthFilter = null;
			}
		}

		Collection<Calendar> allMonths = new LinkedList<Calendar>();
		Collection<String> allMonthsKeys = new HashSet<String>();

		for (MenuElement page : pages) {
			ContentContext lgCtx = ctx;
			if (GlobalContext.getInstance(ctx.getRequest()).isAutoSwitchToDefaultLanguage()) {
				lgCtx = page.getContentContextWithContent(ctx);
			}
			if (filterPage(lgCtx, page)) {
				if (countPage < getMaxNews(lgCtx)) {
					if ((page.isRealContentAnyLanguage(lgCtx) || isWidthEmptyPage()) && page.getContentDateNeverNull(lgCtx).after(backDate.getTime())) {

						if (firstPage == null) {
							firstPage = page;
						}

						if (page.isRealContent(lgCtx)) {
							realContentSize++;
						}
						if (page.isRealContent(lgCtx) || isWidthEmptyPage()) {
							if (tagFilter == null || tagFilter.trim().length() == 0 || page.getTags(lgCtx).contains(tagFilter)) {
								if (catFilter == null || catFilter.trim().length() == 0 || page.getCategory(lgCtx).equals(catFilter)) {
									Calendar cal = Calendar.getInstance();
									cal.setTime(page.getContentDateNeverNull(lgCtx));
									cal = TimeHelper.convertRemoveAfterMonth(cal);
									String key = ("" + cal.get(Calendar.YEAR)) + '-' + cal.get(Calendar.MONTH);
									if (!allMonthsKeys.contains(key)) {
										allMonths.add(cal);
										allMonthsKeys.add(key);
									}

									if (monthFilter == null || TimeHelper.betweenInDay(page.getContentDateNeverNull(lgCtx), startDate.getTime(), endDate.getTime())) {
										countPage++;
										if (countPage >= firstPageNumber && countPage <= lastPageNumber) {
											pageBeans.add(PageBean.getInstance(lgCtx, page, this));
										}
									}
								}
							}
						}
					}
				}
			}
		}

		if (isDisplayFirstPage() && firstPage != null && ctx.getRequest().getParameter("_wcms_content_path") == null) {
			String path = firstPage.getPath();
			String pageRendered = executeJSP(ctx, Edit.CONTENT_RENDERER + "?_wcms_content_path=" + path);
			ctx.getRequest().setAttribute("firstPage", pageRendered);
		} else {
			ctx.getRequest().removeAttribute("firstPage");
		}

		PagesStatus pagesStatus = new PagesStatus(countPage, realContentSize);
		PaginationContext pagination = PaginationContext.getInstance(ctx.getRequest().getSession(), getId(), pageBeans.size(), getPageSize(ctx));

		List<String> months = new LinkedList<String>();
		for (Calendar calendar : allMonths) {
			months.add(format.format(calendar.getTime()));
		}

		ctx.getRequest().setAttribute("pagination", pagination);
		ctx.getRequest().setAttribute("pagesStatus", pagesStatus);
		ctx.getRequest().setAttribute("pages", pageBeans);
		ctx.getRequest().setAttribute("title", getContentTitle());
		ctx.getRequest().setAttribute("comp", this);
		ctx.getRequest().setAttribute("months", months);
		ctx.getRequest().setAttribute("tags", globalContext.getTags());

	}

	public static void main(String[] args) {
		Date mount = new Date();
		Calendar endDate = Calendar.getInstance();
		endDate.setTime(mount);
		System.out.println("***** PageReferenceComponent.main : 0 = " + StringHelper.renderTime(endDate.getTime())); // TODO: remove debug trace
		endDate = TimeHelper.convertRemoveAfterMonth(endDate);
		System.out.println("***** PageReferenceComponent.main : 1 = " + StringHelper.renderTime(endDate.getTime())); // TODO: remove debug trace
		endDate.add(Calendar.MONTH, 1);
		endDate.add(Calendar.SECOND, -1);
		System.out.println("***** PageReferenceComponent.main : 2 = " + StringHelper.renderTime(endDate.getTime())); // TODO: remove debug trace
	}

	private void popularitySorting(ContentContext ctx, List<MenuElement> pages, int pertinentPageToBeSort) throws Exception {
		double minMaxPageRank = 0;
		TreeSet<MenuElement> maxElement = new TreeSet<MenuElement>(new MenuElementPopularityComparator(ctx, false));
		for (MenuElement page : pages) {
			double pageRank = page.getPageRank(ctx);
			if (pageRank >= minMaxPageRank) {
				if (maxElement.size() > pertinentPageToBeSort) {
					maxElement.pollFirst();
					minMaxPageRank = maxElement.first().getPageRank(ctx);
				}
				maxElement.add(page);
			}
		}
		for (MenuElement page : maxElement) {
			pages.remove(page);
		}
		for (MenuElement page : maxElement) {
			pages.add(0, page);
		}
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		if (requestService.getParameter(getCompInputName(), null) != null) {
			ContentService content = ContentService.getInstance(ctx.getRequest());
			MenuElement menu = content.getNavigation(ctx);
			MenuElement[] allChildren = menu.getAllChildren();
			String pagesSelected = "";
			String separation = "";
			for (MenuElement element : allChildren) {
				String selectedPage = requestService.getParameter(getPageId(element), null);
				if (isDefaultSelected() ^ (selectedPage != null) && filterPage(ctx, element)) {
					pagesSelected = pagesSelected + separation + element.getId();
					separation = ID_SEPARATOR;
				}
			}

			String order = requestService.getParameter(getOrderInputName(), "date");
			if (!getOrder().equals(order)) {
				setOrder(order);
				storeProperties();
				setModify();
			}

			String title = requestService.getParameter(getInputNameTitle(), "");
			if (!getContentTitle().equals(title)) {
				setContentTitle(title);
				storeProperties();
				setModify();
			}

			String tag = requestService.getParameter(getTagsInputName(), "");
			if (!getTag().equals(tag)) {
				setTag(tag);
				setModify();
				setNeedRefresh(true);
			}

			String reverseOrder = requestService.getParameter(getReverseOrderInput(), "false");
			boolean newReserveOrder = StringHelper.isTrue(reverseOrder);
			if (isReverseOrder() != newReserveOrder) {
				setReverseOrder(newReserveOrder);
				setModify();
			}

			String firstPageNumber = requestService.getParameter(getFirstPageNumberInputName(), "1");
			if (!firstPageNumber.equals("" + getFirstPageNumber())) {
				setFirstPageNumber(firstPageNumber);
				setModify();
			}

			if (requestService.getParameter(getOrderInputName(), null) != null) {
				Collection<String> timeSelectionList = new LinkedList<String>();
				for (String option : getTimeSelectionOptions()) {
					String timeSelection = requestService.getParameter(getTimeSelectionInputName(option), null);
					if (timeSelection != null) {
						timeSelectionList.add(option);
					}
				}
				if (!getTimeSelection().equals(timeSelectionList)) {
					setTimeSelection(timeSelectionList);
					setModify();
				}
			}

			boolean displayFirstPage = requestService.getParameter(getInputFirstPageFull(), null) != null;
			if (displayFirstPage != isDisplayFirstPage()) {
				setDisplayFirstPage(displayFirstPage);
				setModify();
			}

			String lastPageNumber = requestService.getParameter(getLastPageNumberInputName(), "");
			if (!lastPageNumber.equals(getLastPageNumber())) {
				setLastPageNumber(lastPageNumber);
				setModify();
			}

			setPageSelected(pagesSelected);
			boolean defaultSelected = requestService.getParameter(getDefaultSelectedInputName(), null) != null;
			if (defaultSelected != isDefaultSelected()) {
				setModify();
				setNeedRefresh(true);
				setPageSelected("");
			}
			setDefaultSelected(defaultSelected);

			boolean withEmptyPage = requestService.getParameter(getWidthEmptyPageInputName(), null) != null;
			if (withEmptyPage != isWidthEmptyPage()) {
				setModify();
			}
			setWidthPageEmpty(withEmptyPage);

			String basePage = requestService.getParameter(getParentNodeInputName(), "/");
			if (!basePage.equals(getParentNode())) {
				setNeedRefresh(true);
				setModify();
			}
			setParentNode(basePage);

			storeProperties();

		}
	}

	private void setContentTitle(String title) {
		properties.setProperty("content-title", title);
	}

	private void setDefaultSelected(boolean selected) {
		properties.setProperty(DEFAULT_SELECTED_PROP_KEY, "" + selected);
	}

	private void setFirstPageNumber(String firstPage) {
		properties.setProperty(PAGE_START_PROP_KEY, firstPage);
	}

	private void setLastPageNumber(String last) {
		properties.setProperty(PAGE_END_PROP_KEY, last);
	}

	protected void setOrder(String order) {
		properties.setProperty(ORDER_KEY, order);
	}

	private void setPageSelected(String pagesSelected) {
		if (!properties.getProperty(PAGE_REF_PROP_KEY, "").equals(pagesSelected)) {
			setModify();
		}
		properties.setProperty(PAGE_REF_PROP_KEY, pagesSelected);
	}

	protected void setParentNode(String node) {
		properties.setProperty(PARENT_NODE_PROP_KEY, node);
	}

	protected void setReverseOrder(boolean reverseOrder) {
		properties.setProperty(CHANGE_ORDER_KEY, "" + reverseOrder);
	}

	protected void setTag(String tag) {
		properties.setProperty(TAG_KEY, tag);
	}

	private void setTimeSelection(Collection<String> timeSelection) {
		properties.setProperty(TIME_SELECTION_KEY, StringHelper.collectionToString(timeSelection));
	}

	private void setWidthPageEmpty(boolean selected) {
		properties.setProperty(WIDTH_EMPTY_PAGE_PROP_KEY, "" + selected);
	}

	private void visitSorting(ContentContext ctx, List<MenuElement> pages, int pertinentPageToBeSort) throws Exception {
		int minMaxVisit = 0;
		TreeSet<MenuElement> maxElement = new TreeSet<MenuElement>(new MenuElementVisitComparator(ctx, false));

		for (MenuElement page : pages) {
			if (page.isRealContent(ctx)) {
				int access = page.getLastAccess(ctx);
				if (access >= minMaxVisit) {
					if (maxElement.size() >= pertinentPageToBeSort) {
						maxElement.pollFirst();
						minMaxVisit = maxElement.first().getLastAccess(ctx);
					}
					maxElement.add(page);
				}
			}
		}

		for (MenuElement page : maxElement) {
			pages.remove(page);
		}
		for (MenuElement page : maxElement) {
			pages.add(0, page);
		}
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		try {
			MenuElement menu = content.getNavigation(ctx);
			MenuElement[] allChildren = menu.getAllChildren();
			return getPagesId(ctx, allChildren).size() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String getActionGroupName() {
		return "page-links";
	}

	@Override
	public int getSearchLevel() {
		return 0;
	}

}
