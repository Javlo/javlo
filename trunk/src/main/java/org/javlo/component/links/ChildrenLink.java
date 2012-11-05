/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.links;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.image.IImageTitle;
import org.javlo.context.ContentContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class ChildrenLink extends AbstractVisualComponent implements IImageTitle {

	protected static final char DATA_SEPARATOR = ',';

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
			return URLHelper.createURL(ctx, child.getVirtualPath(ctx));
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

		// public IImageTitle getImage() throws Exception {
		// return child.getImage(ctx);
		// }

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
	public int getComplexityLevel() {
		return COMPLEXITY_STANDARD;
	}

	@Override
	protected String getRendererTitle() {
		String[] values = getValue().split("" + DATA_SEPARATOR);
		if (values.length > 1) {
			return values[1];
		} else {
			return "";
		}
	}

	@Override
	protected String getCurrentRenderer(ContentContext ctx) {
		if (super.getCurrentRenderer(ctx) != null) {
			return super.getCurrentRenderer(ctx);
		} else {
			String[] values = getValue().split("" + DATA_SEPARATOR);
			if (values.length > 0) {
				setRenderer(ctx, values[0]);
				return values[0];
			} else {
				return "";
			}
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
		out.println(XHTMLHelper.getCheckbox(getInputNameLabel(), isLabelListed()) + " <label for=\"" + getInputNameLabel() + "\">" + i18n.getText("content.children-list.label") + "</label>");
		out.println("</div><div class=\"line\">");
		out.println(XHTMLHelper.getCheckbox(getInputNameImage(), isImage()) + " <label for=\"" + getInputNameImage() + "\">" + i18n.getText("content.children-list.image") + "</label>");
		out.println("</div><div class=\"line\">");
		out.println(XHTMLHelper.getCheckbox(getInputNameDescription(), isDescription()) + " <label for=\"" + getInputNameDescription() + "\">" + i18n.getText("content.children-list.description") + "</label>");
		out.println("</div><div class=\"line\">");
		out.println(XHTMLHelper.getCheckbox(getInputNameCombo(), isCombo()) + " <label for=\"" + getInputNameCombo() + "\">" + i18n.getText("content.children-list.combo") + "</label>");
		out.println("</div><div class=\"line\">");
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

		MenuElement[] children = currentPage.getChildMenuElementsWithVirtual(ctx, !showAll, false);
		for (int i = 0; i < children.length; i++) {
			if (!children[i].isVisible(ctx) || !showOnlyNotVisible) {
				if (children[i].getImage(ctx) != null) {
					setChildImageComponent(ctx, children[i].getImage(ctx));
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

	public String getInputNameCombo() {
		return "combo_" + getId();
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
			return new String[] { i18n.getText("content.web-map.only-visible"), i18n.getText("content.web-map.only-not-visible"), i18n.getText("content.web-map.all") };
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new String[] { "visible", "not-visible", "all" };
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "visible", "not-visible", "all" };
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
		return "children-link";
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

		if (getStyle() != null) {
			showAll = getStyle().equalsIgnoreCase("all");
			showOnlyNotVisible = getStyle().equalsIgnoreCase("not-visible");
		}
		MenuElement[] children = parentPage.getChildMenuElementsWithVirtual(ctx, false, false);
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
		MenuElement[] children = parentPage.getChildMenuElementsWithVirtual(ctx, !showAll, false);
		for (MenuElement menuElement : children) {
			if (!menuElement.isVisible(ctx) || !showOnlyNotVisible) {
				displayChildren = true;
			}
		}

		if (displayChildren) {
			out.print("<div " + getSpecialPreviewCssClass(ctx, getStyle(ctx) + " " + getType()) + getSpecialPreviewCssId(ctx) + " >");
			if (isCombo()) {
				out.println("<form id=\"select_page\" action=\"" + URLHelper.createURL(ctx) + "\" method=\"get\">");
				out.println("<select name=\"" + ContentContext.FORWARD_PATH_REQUEST_KEY + "\">");
				for (int i = 0; i < children.length; i++) {
					if (!children[i].isVisible(ctx) || !showOnlyNotVisible) {
						out.print("<option value=\"" + URLHelper.createURL(ctx, children[i].getPath()) + "\">");
						out.print(children[i].getFullLabel(ctx));
						out.println("</option>");
					}
				}
				out.println("</select>");
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				out.println("<input type=\"submit\" name=\"ok\" value=\"" + i18nAccess.getViewText("global.ok") + "\">");
				out.println("</form>");
			} else {
				out.println("<ul>");
				for (int i = 0; i < children.length; i++) {
					if ((!children[i].isVisible(ctx) || !showOnlyNotVisible) && children[i].isRealContent(ctx)) {
						if (children[i].equals(currentPage)) {
							out.print("<li class=\"current-page " + ctx.getCurrentTemplate().getSelectedClass() + "\">");
						} else {
							if (children[i].isSelected(ctx)) {
								out.print("<li class=\"" + ctx.getCurrentTemplate().getSelectedClass() + "\">");
							} else {
								out.print("<li>");
							}
						}
						if (children[i].getContentDate(ctx) != null) {
							out.print("<span class=\"date\">" + StringHelper.renderUserFriendlyDate(ctx, children[i].getContentDate(ctx)) + "</span>");
						}
						out.print("<a href=\"" + URLHelper.createURL(ctx, children[i].getVirtualPath(ctx)) + "\">");
						if (isLabelListed()) {
							String sep = "";
							if (isDescription() && children[i].getDescription(ctx).trim().length() > 0) {
								sep = " : ";
							}
							out.print("<span class=\"label\">" + children[i].getFullLabel(ctx) + sep + "</span>");
						}
						if ((isImage() && (children[i].getImage(ctx) != null)) || (isDescription() && children[i].getDescription(ctx).trim().length() > 0)) {
							out.println("<span class=\"body\">");
							if (isImage() && (children[i].getImage(ctx) != null)) {
								String imgURL = children[i].getImage(ctx).getResourceURL(ctx);
								String imgDesc = children[i].getImage(ctx).getImageDescription(ctx);
								out.print("<span class=\"image\">");
								out.print("<img src=\"" + URLHelper.createTransformURL(ctx, children[i], imgURL, "list") + "\" alt=\"" + imgDesc + "\" />");
								out.print("</span>");
							}

							if (isDescription() && children[i].getDescription(ctx).trim().length() > 0) {
								out.print("<span class=\"description\">" + children[i].getDescription(ctx) + "</span>");
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
			setValue(LABEL);
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
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		if (requestService.getParameter("comp-" + getId(), null) == null) { // this component is not in edit form
			return;
		}
		String newValue = "";
		boolean modify = false;
		if (requestService.getParameter(getInputNameDescription(), null) != null) {
			newValue = DESCRIPTION;
			if (!isDescription()) {
				modify = true;
			}
		} else if (isDescription()) {
			modify = true;
		}
		if (requestService.getParameter(getInputNameLabel(), null) != null) {
			newValue = newValue + DATA_SEPARATOR + LABEL;
			if (!isLabelListed()) {
				modify = true;
			}
		} else if (isLabelListed()) {
			modify = true;
		}
		if (requestService.getParameter(getInputNameImage(), null) != null) {
			newValue = newValue + DATA_SEPARATOR + IMAGE;
			if (!isImage()) {
				modify = true;
			}
		} else if (isImage()) {
			modify = true;
		}
		boolean lockParentPage = false;
		if (requestService.getParameter(getInputLockParentPage(), null) != null) {
			newValue = newValue + DATA_SEPARATOR + LOCK_PARENT_PAGE;
			lockParentPage = true;
			if (!isLockParentPage()) {
				modify = true;
			}
		} else if (isLockParentPage()) {
			modify = true;
		}
		if (requestService.getParameter(getInputNameCombo(), null) != null) {
			if (!lockParentPage) {
				newValue = LABEL + DATA_SEPARATOR + COMBO;
			} else {
				newValue = LABEL + DATA_SEPARATOR + COMBO + DATA_SEPARATOR + LOCK_PARENT_PAGE;
			}
			if (!isCombo() || isDescription() || isImage() || !isLabel()) {
				modify = true;
			}
		} else if (isCombo()) {
			modify = true;
		}
		String rendererTitle = requestService.getParameter(getInputNameRendererTitle(), "");
		newValue = rendererTitle + DATA_SEPARATOR + newValue;
		if (!getRendererTitle().equals(rendererTitle)) {
			modify = true;
			setModify();
		}
		String renderer = requestService.getParameter(getInputNameRenderer(), "");
		newValue = renderer + DATA_SEPARATOR + newValue;
		if (getRenderer(ctx) != null) {
			if (!getRenderer(ctx).equals(renderer)) {
				modify = true;
				setModify();
			}
		}
		if (modify) {
			setValue(newValue);
			setModify();
		}
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

}
