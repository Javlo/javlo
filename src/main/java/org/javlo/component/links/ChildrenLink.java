/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.links;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.image.ImageBean;
import org.javlo.context.ContentContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.User;

/**
 * @author pvandermaesen
 */
public class ChildrenLink extends AbstractVisualComponent implements IImageTitle {

	protected static final char DATA_SEPARATOR = ',';
	
	private static String RECURSIVE = "recursive";
	
	public static final String TYPE = "children-link";

	public class ChildLinkBean {
		private final ContentContext ctx;
		private final MenuElement child;
		private final MenuElement currentPage;

		public ChildLinkBean(ContentContext ctx, MenuElement child, MenuElement currentPage) {
			this.ctx = ctx;
			this.child = child;
			this.currentPage = currentPage;
		}
		
		public boolean isSelected() {
			try {
				return child.isSelected(ctx);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		public boolean isLastSelected() {
			return child.equals(currentPage);
		}

		public Date getContentDate() throws Exception {
			return child.getContentDate(ctx);
		}

		public String getDisplayContentDate() throws Exception {
			Date date = getContentDate();
			return date == null ? null : StringHelper.renderUserFriendlyDate(ctx, date);
		}

		public String getUrl() {
			return URLHelper.createURL(ctx, child);
		}

		public String getDescription() throws Exception {
			return child.getDescription(ctx);
		}

		public String getFullLabel() throws Exception {
			return child.getFullLabel(ctx);
		}

		public String getChildSubTitle() throws Exception {
			return child.getSubTitle(ctx);
		}

		public String getSubTitle() throws Exception {
			return currentPage.getSubTitle(ctx);
		}

		public boolean isRealContent() throws Exception {
			return child.isRealContent(ctx);
		}
		
		public int getReactionSize() throws Exception {
			return child.getReactionSize(ctx);
		}
		
		public String getCreationDateString() throws FileNotFoundException, IOException {
			return StringHelper.renderFullDate(ctx, child.getCreationDate());
		}
		
		public String getCreationTimeString() throws FileNotFoundException, IOException {
			return StringHelper.renderTimeOnly(child.getCreationDate());
		}
		
		public String getModificationDateString() throws FileNotFoundException, IOException {
			return StringHelper.renderFullDate(ctx, child.getModificationDate());
		}
		
		public String getModificationTimeString() throws FileNotFoundException, IOException {
			return StringHelper.renderTimeOnly(child.getModificationDate());
		}
		
		public String getContentDateString() throws Exception {
			return StringHelper.renderFullDate(ctx, child.getContentDate(ctx));
		}
		
		public boolean isCurrentPageRealContent() throws Exception {
			return currentPage.isRealContent(ctx);
		}
		
		public String getCreator() {			
			return child.getCreator();
		}
		
		public String getCreatorAvatarURL() {
			AdminUserFactory userFactory = AdminUserFactory.createAdminUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
			User user = userFactory.getUser(getCreator());
			if (user != null) {
				return URLHelper.createAvatarUrl(ctx, userFactory.getUser(user.getName()).getUserInfo());
			} else {
				return null;
			}
		}
		
		public MenuElement getPage() {
			return child;
		}		
		public boolean isVisible() {
			try {
				return currentPage.isVisible(ctx);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		public ImageBean getImage() throws Exception {
			IImageTitle image = child.getImage(ctx);
			if (image != null) {
				String imageURL = URLHelper.createTransformURL(ctx, image.getResourceURL(ctx), getConfig(ctx).getProperty("filter", "list"));
				return new ImageBean(URLHelper.createResourceURL(ctx, image.getResourceURL(ctx)), imageURL, image.getImageDescription(ctx), image.getImageLinkURL(ctx));
			} else {
				return null;
			}
		}
		
		public List<ChildLinkBean> getChildren() {
			List<ChildLinkBean> outChildren = new LinkedList<ChildLinkBean>();
			for (MenuElement subchild : child.getChildMenuElements()) {
				outChildren.add(new ChildLinkBean(ctx, subchild, child));
			}
			return outChildren;	
		}

	}

	private static final String LOCK_PARENT_PAGE = "lock-parent-page";
	private static final String COMBO = "__combo__";
	private static final String IMAGE = "__image__";
	private static final String DESCRIPTION = "__description__";
	private static final String LABEL = "__label__";

	private IImageTitle getChildImageComponent(ContentContext ctx) {
		final String KEY = "image-title-comp-" + getId();
		return (IImageTitle) ctx.getRequest().getSession().getAttribute(KEY);
	}

	@Override
	protected String getRendererTitle() {
		String[] values = getValue().split("" + DATA_SEPARATOR);		
		if (values.length >= 1) {
			return values[0];
		} else {
			return "";
		}
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		// out.println(getSelectRendererXHTML(ctx));

		I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
		out.println("<input type=\"hidden\" name=\"comp-" + getId() + "\" value=\"true\" />");
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputNameRendererTitle() + "\">" + i18n.getText("global.title") + "</label>");
		out.println("<input class=\"form-control\" type=\"text\" id=\"" + getInputNameRendererTitle() + "\" name=\"" + getInputNameRendererTitle() + "\" value=\"" + getRendererTitle() + "\"/>");
		out.println("</div><div class=\"line\">");
		if (getRenderes(ctx).size() < 1) {
			out.println("</div><div class=\"line\">");
			out.println(XHTMLHelper.getCheckbox(getInputNameLabel(), isLabelListed()) + " <label for=\"" + getInputNameLabel() + "\">" + i18n.getText("content.children-list.label") + "</label>");
			out.println("</div><div class=\"line\">");
			out.println(XHTMLHelper.getCheckbox(getInputNameImage(), isImage()) + " <label for=\"" + getInputNameImage() + "\">" + i18n.getText("content.children-list.image") + "</label>");
			out.println("</div><div class=\"line\">");
			out.println(XHTMLHelper.getCheckbox(getInputNameDescription(), isDescription()) + " <label for=\"" + getInputNameDescription() + "\">" + i18n.getText("content.children-list.description") + "</label>");
			out.println("</div><div class=\"line\">");
		}
		out.println(XHTMLHelper.getCheckbox(getInputLockParentPage(), isLockParentPage()) + " <label for=\"" + getInputLockParentPage() + "\">" + i18n.getText("content.children-list.linked") + "</label>");
		out.println("</div>");

		out.close();

		return new String(outStream.toByteArray());
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	@Override
	public String getImageDescription(ContentContext ctx) {
		try {
			return getImageTitleChild(ctx).getImageDescription(ctx);
		} catch (Exception e) {
			return null;
		}
	}

	public IImageTitle getImageTitleChild(ContentContext ctx) throws Exception {
		if (getChildImageComponent(ctx) != null) {
			return getChildImageComponent(ctx);
		}

		MenuElement currentPage = getPage();

		boolean showAll = false;
		boolean showOnlyNotVisible = false;
		if (getStyle(ctx) != null) {
			showAll = getStyle(ctx).equalsIgnoreCase("all");
			showOnlyNotVisible = getStyle(ctx).equalsIgnoreCase("not-visible");
			if (showOnlyNotVisible) {
				showAll = true;
			}
		}

		Collection<MenuElement> children = currentPage.getChildMenuElementsWithVirtual(ctx, !showAll, false);
		for (MenuElement page : children) {
			if (!page.isVisible(ctx) || !showOnlyNotVisible) {
				if (page.getImage(ctx) != null) {
					setChildImageComponent(ctx, page.getImage(ctx));
					if (getChildImageComponent(ctx).isImageValid(ctx)) {
						return getChildImageComponent(ctx);
					}
				}
			}
		}
		return getChildImageComponent(ctx);
	}

	@Override
	public String getResourceURL(ContentContext ctx) {
		try {
			return getImageTitleChild(ctx).getResourceURL(ctx);
		} catch (Exception e) {
			return null;
		}
	}

	public String getInputLockParentPage() {
		return "_lock_parent_page" + getId();
	}

	public String getInputNameDescription() {
		return "description_" + getId();
	}

	public String getInputNameImage() {
		return "image_" + getId();
	}

	public String getInputNameLabel() {
		return "label_" + getId();
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			return new String[] { i18n.getText("content.web-map.only-visible"), i18n.getText("content.web-map.only-not-visible"), i18n.getText("content.web-map.all"), i18n.getText("content.children-link.recursive") };
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new String[] { "visible", "not-visible", "all", RECURSIVE };
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "visible", "not-visible", "all", RECURSIVE };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		I18nAccess i18n = null;
		try {
			i18n = I18nAccess.getInstance(ctx.getRequest());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return i18n.getText("content.children-list.style-title");
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		MenuElement currentPage = ctx.getCurrentPage();
		MenuElement parentPage = currentPage;
		if (isLockParentPage()) {
			parentPage = getPage();
		}

		boolean showAll = false;
		boolean showOnlyNotVisible = false;
		
		if (ctx.getGlobalContext().isCollaborativeMode()) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			ctx.getRequest().setAttribute("createChildButton", MacroHelper.getLaunchMacroXHTML(ctx, "create-content-children", i18nAccess.getViewText("macro.add-child.label", "Create new message.")));
		}

		if (getStyle() != null) {
			showAll = getStyle().equalsIgnoreCase("all") || getStyle().equalsIgnoreCase(RECURSIVE);
			showOnlyNotVisible = getStyle().equalsIgnoreCase("not-visible");
		}
		Collection<MenuElement> children;		
		if (getStyle().equals(RECURSIVE)) {			
			children = Arrays.asList(parentPage.getAllChildren());
		} else {
			children = parentPage.getChildMenuElementsWithVirtual(ctx, false, false);
		}		
		String renderer = getRenderer(ctx);
		if (renderer != null) {
			List<ChildLinkBean> childrenList = new LinkedList<ChildLinkBean>();
			for (MenuElement element : children) {				
				if ((element.isVisible(ctx) ^ showOnlyNotVisible) || showAll) {
					ChildLinkBean bean = new ChildLinkBean(ctx, element, currentPage);
					childrenList.add(bean);
				}
			}
			ctx.getRequest().setAttribute("title", getRendererTitle());
			ctx.getRequest().setAttribute("children", childrenList);
			ctx.getRequest().setAttribute("currentPageUrl", URLHelper.createURL(ctx));
		}
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		MenuElement currentPage = ctx.getCurrentPage();
		MenuElement parentPage = currentPage;
		if (isLockParentPage()) {
			parentPage = getPage();
		}

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		boolean showAll = false;
		boolean showOnlyNotVisible = false;
		boolean displayChildren = false;
		if (getStyle(ctx) != null) {
			showAll = getStyle(ctx).equalsIgnoreCase("all");
			showOnlyNotVisible = getStyle(ctx).equalsIgnoreCase("not-visible");
			if (showOnlyNotVisible) {
				showAll = true;
			}
		}
		Collection<MenuElement> children = parentPage.getChildMenuElementsWithVirtual(ctx, !showAll, false);
		for (MenuElement menuElement : children) {
			if (!menuElement.isVisible(ctx) || !showOnlyNotVisible) {
				displayChildren = true;
			}
		}

		if (displayChildren) {
			
			String title = I18nAccess.getInstance(ctx).getViewText("global.gotopage","");
			if (title.trim().length() > 0) {
				title = " title=\""+StringHelper.toXMLAttribute(title)+"\"";
			}
			
			String select = "";
			if (isCombo()) {
				select = " select";
			}
			out.print("<div " + getSpecialPreviewCssClass(ctx, getStyle(ctx) + " " + getType() + select) + getSpecialPreviewCssId(ctx) + " >");
			if (isCombo()) {
				out.println("<form id=\"select_page\" action=\"" + URLHelper.createURL(ctx) + "\" method=\"get\">");
				out.println("<select name=\"" + ContentContext.FORWARD_PATH_REQUEST_KEY + "\">");
				for (MenuElement page : children) {
					if (!page.isVisible(ctx) || !showOnlyNotVisible) {
						String selected = "";
						MenuElement selectedPage = ctx.getCurrentPage();
						while (selectedPage != null) {
							if (selectedPage.getId().equals(page.getId())) {
								selected = " selected=\"selected\"";
							}
							selectedPage = selectedPage.getParent();
						}
						out.print("<option value=\"" + URLHelper.createURL(ctx, page.getPath()) + "\"" + selected + " >");
						out.print(page.getFullLabel(ctx));
						out.println("</option>");
					}
				}
				out.println("</select>");
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				out.println("<input type=\"submit\" name=\"ok\" value=\"" + i18nAccess.getViewText("global.ok") + "\">");
				out.println("</form>");
			} else {
				out.println("<ul>");
				for (MenuElement page : children) {
					if ((!page.isVisible(ctx) || !showOnlyNotVisible) && page.isRealContent(ctx)) {
						if (page.equals(currentPage)) {
							out.print("<li class=\"current-page " + ctx.getCurrentTemplate().getSelectedClass() + "\">");
						} else {
							if (page.isSelected(ctx)) {
								out.print("<li class=\"" + ctx.getCurrentTemplate().getSelectedClass() + "\">");
							} else {
								out.print("<li>");
							}
						}
						if (page.getContentDate(ctx) != null) {
							out.print("<span class=\"date\">" + StringHelper.renderUserFriendlyDate(ctx, page.getContentDate(ctx)) + "</span>");
						}
						out.print("<a"+title+" href=\"" + URLHelper.createURL(ctx, page.getVirtualPath(ctx)) + "\">");
						if (isLabelListed()) {
							String sep = "";
							if (isDescription() && page.getDescription(ctx).trim().length() > 0) {
								sep = " : ";
							}
							out.print("<span class=\"label\">" + page.getFullLabel(ctx) + sep + "</span>");
						}
						if ((isImage() && (page.getImage(ctx) != null)) || (isDescription() && page.getDescription(ctx).trim().length() > 0)) {
							out.println("<span class=\"body\">");
							if (isImage() && (page.getImage(ctx) != null)) {
								String imgURL = page.getImage(ctx).getResourceURL(ctx);
								String imgDesc = page.getImage(ctx).getImageDescription(ctx);
								out.print("<span class=\"image\">");
								out.print("<img src=\"" + URLHelper.createTransformURL(ctx, page, imgURL, "list") + "\" alt=\"" + imgDesc + "\" />");
								out.print("</span>");
							}

							if (isDescription() && page.getDescription(ctx).trim().length() > 0) {
								out.print("<span class=\"description\">" + page.getDescription(ctx) + "</span>");
							}
							out.println("<span class=\"end-body\"></span></span>");
						}
						out.println("</a>");
						out.println("</li>");
					}
				}
				out.println("</ul>");
			}
			out.println("<div class=\"clear\">&nbsp;</div></div>");
		}

		return new String(outStream.toByteArray());
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		return 0;
	}

	@Override
	protected void init() throws ResourceNotFoundException {
		if (getValue() == null) {
			setValue(DATA_SEPARATOR+LABEL);
		}
		super.init();
	}

	public boolean isCombo() {
		return getValue().contains(COMBO);
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}

	public boolean isDescription() {
		return getValue().contains(DESCRIPTION);
	}

	public boolean isImage() {
		return getValue().contains(IMAGE);
	}

	@Override
	public boolean isImageValid(ContentContext ctx) {
		try {
			return getImageTitleChild(ctx) != null;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isLabelListed() {
		return getValue().contains(LABEL);
	}

	public boolean isLockParentPage() {
		return getValue().contains(LOCK_PARENT_PAGE);
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		if (requestService.getParameter("comp-" + getId(), null) == null) { // this
																			// component
																			// is
																			// not
																			// in
																			// edit
																			// form
			return null;
		}
		String newValue = "";
		if (requestService.getParameter(getInputNameDescription(), null) != null) {
			newValue = DESCRIPTION;
		}
		if (requestService.getParameter(getInputNameLabel(), null) != null) {
			newValue = newValue + DATA_SEPARATOR + LABEL;
		}
		if (requestService.getParameter(getInputNameImage(), null) != null) {
			newValue = newValue + DATA_SEPARATOR + IMAGE;
		}
		if (requestService.getParameter(getInputLockParentPage(), null) != null) {
			newValue = newValue + DATA_SEPARATOR + LOCK_PARENT_PAGE;
		}
		String rendererTitle = requestService.getParameter(getInputNameRendererTitle(), "");
		
		newValue = rendererTitle + DATA_SEPARATOR + newValue;

		if (!newValue.equals(getValue())) {
			setValue(newValue);
			setModify();
		}
		
		return null;
	}

	private void setChildImageComponent(ContentContext ctx, IImageTitle imageTitle) {
		final String KEY = "image-title-comp-" + getId();
		ctx.getRequest().getSession().setAttribute(KEY, imageTitle);
	}

	@Override
	public String getImageLinkURL(ContentContext ctx) {
		try {
			return getImageTitleChild(ctx).getImageLinkURL(ctx);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		try {
			/*MenuElement parentPage = ctx.getCurrentPage();
			if (isLockParentPage()) {
				parentPage = getPage();
			}*/			
			boolean realContent = getPage().getChildMenuElementsWithVirtual(ctx, false, false).size() > 0;
			return realContent;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public int getPriority(ContentContext ctx) {
		if (getConfig(ctx).getProperty("image.priority", null) == null) {
			return 5;
		} else {
			return Integer.parseInt(getConfig(ctx).getProperty("image.priority", null));
		}
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}
}
