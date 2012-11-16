package org.javlo.helper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.links.RSSLink;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.template.Template;

public class XHTMLNavigationHelper {

	public static String getBreadcrumb(ContentContext ctx) throws Exception {
		return getBreadcrumb(ctx, false);
	}

	public static String getBreadcrumb(ContentContext ctx, boolean displayRoot) throws Exception {

		String outBreadcrumb = new String();

		MenuElement root = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx);
		MenuElement currentPage = root.searchChild(ctx);
		if (currentPage == null) {
			currentPage = root;
		}
		String sep = "";

		boolean endWhile = false;
		if (displayRoot) {
			endWhile = currentPage == null;
		} else {
			endWhile = currentPage.equals(root);
		}

		while (!endWhile) {
			String url = URLHelper.createURL(ctx, currentPage);
			String label = currentPage.getLabel(ctx);

			currentPage = currentPage.getParent();

			if (displayRoot) {
				endWhile = currentPage == null;
			} else {
				endWhile = currentPage.equals(root);
			}

			String cssClass = "";
			String title = "";
			if (sep.length() == 0) {
				cssClass = " class=\"first";
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				title = " title=\"" + i18nAccess.getContentViewText("global.you-are-here", "you are here") + "\"";
			}
			if (endWhile) {
				if (cssClass.length() > 0) {
					cssClass = cssClass + " last";
				} else {
					cssClass = " class=\"last";
				}
			}
			if (cssClass.length() > 0) {
				cssClass = cssClass + "\"";
			}

			outBreadcrumb = "<a href=\"" + url + "\"" + cssClass + "" + title + ">" + label + "</a>" + sep + outBreadcrumb;
			sep = " &gt; ";
		}

		return outBreadcrumb;
	}

	public static String getBreadcrumbList(ContentContext ctx) throws Exception {
		return getBreadcrumbList(ctx, true);
	}

	public static String getBreadcrumbList(ContentContext ctx, boolean withRoot) throws Exception {

		String outBreadcrumb = new String();

		MenuElement root = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx);
		MenuElement currentPage = root.searchChild(ctx);
		if (currentPage == null) {
			return "";
		}
		boolean firstBcl = true;
		String cssClass = "";
		while (!currentPage.equals(root)) {
			String url = URLHelper.createURL(ctx, currentPage);
			String label = currentPage.getLabel(ctx);

			currentPage = currentPage.getParent();

			cssClass = "";
			String title = "";
			if (firstBcl) {
				firstBcl = false;
				cssClass = " class=\"first";
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				title = " title=\"" + i18nAccess.getContentViewText("global.you-are-here", "you are here") + "\"";
			}
			if (currentPage.equals(root) && !withRoot) {
				if (cssClass.length() > 0) {
					cssClass = cssClass + " last";
				} else {
					cssClass = " class=\"last";
				}
			}
			if (cssClass.length() > 0) {
				cssClass = cssClass + "\"";
			}

			outBreadcrumb = "<li" + cssClass + "><a href=\"" + url + "\"" + cssClass + "" + title + ">" + label + "</a></li>" + outBreadcrumb;

		}

		if (withRoot) {
			cssClass = "";
			if (cssClass.length() > 0) {
				cssClass = cssClass + " last";
			} else {
				cssClass = " class=\"last";
			}
			if (cssClass.length() > 0) {
				cssClass = cssClass + "\"";
			}
			String url = URLHelper.createURL(ctx, "/");
			String label = currentPage.getLabel(ctx);
			outBreadcrumb = "<li" + cssClass + "><a href=\"" + url + "\"" + cssClass + ">" + label + "</a></li>" + outBreadcrumb;
		}

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		if (outBreadcrumb.trim().length() > 0) {
			return "<span class=\"title\">" + i18nAccess.getViewText("global.you-are-here") + " : </span><ul>" + outBreadcrumb + "</ul>";
		} else {
			return "";
		}
	}

	public static final String getRSSHeader(ContentContext ctx, MenuElement page) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		List<IContentVisualComponent> rssComp = page.getContentByType(ctx, RSSLink.TYPE);
		for (IContentVisualComponent comp : rssComp) {
			RSSLink rssLink = (RSSLink) comp;
			out.println("<link rel=\"alternate\" title=\"" + rssLink.getChannel() + "\" href=\"" + rssLink.getRSSURL(ctx) + "\" type=\"application/rss+xml\" />");
		}
		out.close();
		return writer.toString();
	}

	public static boolean menuExist(ContentContext ctx, int level) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		if (currentPage.getDepth() < level - 1) {
			return false;
		} else if (currentPage.getDepth() < level) {
			if (currentPage.getChildMenuElements(ctx, true).size() > 0) {
				return true;
			} else {
				return false;
			}
		} else if (currentPage.getDepth() == level && currentPage.getParent() != null) {
			if (currentPage.getParent().getChildMenuElements(ctx, true).size() == 0) {
				return false;
			}
		}
		return true;
	}

	public static String renderComboNavigation(ContentContext ctx, Collection<MenuElement> pages) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		return renderComboNavigation(ctx, pages, ContentContext.FORWARD_PATH_REQUEST_KEY, currentPage.getPath());
	}

	public static String renderComboNavigation(ContentContext ctx, Collection<MenuElement> pages, String id, String currentPath) throws Exception {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);
		out.println("<select name=\"" + id + "\" id=\"" + id + "\">");
		for (MenuElement page : pages) {
			String path = URLHelper.createURL(ctx, page);
			if ((currentPath != null) && (currentPath.equals(page.getPath()))) {
				out.println("<option value=\"" + path + "\" selected=\"true\">");
			} else {
				out.print("<option value=\"" + path + "\">");
			}
			out.print(page.getLabel(ctx));
			out.println("</option>");
		}
		out.println("</select>");
		out.close();
		return res.toString();
	}

	public static String renderComboNavigation(ContentContext ctx, MenuElement rootPage, String id, String currentValue) throws Exception {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);
		out.println("<select name=\"" + id + "\" id=\"" + id + "\">");
		MenuElement elem = rootPage;
		String[] values = elem.getChildList();
		for (String value : values) {
			if ((currentValue != null) && (currentValue.equals(value))) {
				out.println("<option value=\"" + value + "\" selected=\"true\">");
			} else {
				out.print("<option value=\"" + value + "\">");
			}
			out.print(value);
			out.println("</option>");
		}
		out.println("</select>");
		out.close();
		return res.toString();
	}

	public static String renderDefinedMenu(ContentContext ctx, boolean onlyVisible, boolean moveIcon) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		return renderDefinedMenu(ctx, currentPage.getId(), onlyVisible, moveIcon);
	}

	public static String renderDefinedMenu(ContentContext ctx, String parentId, boolean onlyVisible) throws Exception {
		return renderDefinedMenu(ctx, parentId, onlyVisible, false);
	}

	/**
	 * render a sub element of the navigation tree.
	 * 
	 * @param parentId
	 *            the id of parent.
	 * @return a html list with the defined sub menu.
	 * @throws Exception
	 */
	public static String renderDefinedMenu(ContentContext ctx, String parentId, boolean onlyVisible, boolean moveIcon) throws Exception {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NavigationService navigationService = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
		MenuElement menu = navigationService.getPage(ctx, parentId);

		if (menu == null) {
			out.println("ERROR: " + parentId + " not found in navigation structure.");
		} else {
			Collection<MenuElement> elems;
			if (onlyVisible) {
				elems = menu.getVisibleChildMenuElements(ctx);
			} else {
				elems = menu.getChildMenuElements();
			}

			String firstClass = "first ";
			out.println("<ul>");
			if (moveIcon && elems.size() > 0) {
				out.println("<li class=\"insert-here page-not-selected first\"><input type=\"submit\" name=\"page_0\" value=\"insert-here\" /></li>");
			}
			for (MenuElement elem : elems) {
				String selected = "";
				if (elem.isSelected(ctx)) {
					selected = "class=\"selected\"";
				}
				String moveIconHTML = "";
				if (moveIcon) {
					moveIconHTML = "<input type=\"radio\" onclick=\"selectPageToMove();\" name=\"select-page\" value=\"" + elem.getId() + "\" />";
				}

				String liClass = firstClass;

				out.println("<li class=\"" + liClass.trim() + "\">" + moveIconHTML + "<a " + selected + " href=\"" + URLHelper.createURL(ctx, elem.getPath()) + "\">" + elem.getLabel(ctx) + "</a></li>");
				if (moveIcon) {
					out.println("<li class=\"insert-here page-not-selected\"><input type=\"submit\" name=\"page_" + elem.getId() + "\" value=\"insert-here\" /></li>");
				}
				firstClass = "";
			}

			out.println("</ul>");
		}

		out.close();
		return res.toString();
	}

	public static String renderImageMenu(ContentContext ctx, int fromDepth, int toDepth) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		return renderMenu(ctx, content.getNavigation(ctx), fromDepth, toDepth, true, 0, false, true, true, false, false, null, null);
	}

	public static String renderMenu(ContentContext ctx, int fromDepth, int toDepth) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		return renderMenu(ctx, content.getNavigation(ctx), fromDepth, toDepth, true, 0, false, false, true, false, false, null, null);
	}

	public static String renderMenu(ContentContext ctx, int fromDepth, int toDepth, boolean extended) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		return renderMenu(ctx, content.getNavigation(ctx), fromDepth, toDepth, true, 0, extended, false, true, false, false, null, null);
	}

	public static String renderMenu(ContentContext ctx, MenuElement fromPage, int fromDepth, int toDepth) throws Exception {
		return renderMenu(ctx, fromPage, fromDepth, toDepth, false, 0, false, false, true, false, false, null, null);
	}

	public static String renderMenu(ContentContext ctx, MenuElement fromPage, int fromDepth, int toDepth, boolean extended) throws Exception {
		return renderMenu(ctx, fromPage, fromDepth, toDepth, true, fromDepth, extended, false, true, false, false, null, null);
	}

	private static String renderMenu(ContentContext ctx, MenuElement menu, int fromDepth, int toDepth, boolean onlyVisible, int depth, boolean extended, boolean image, boolean withVirtual, boolean selecteableItem, Boolean selectableBetween, List<MenuElement> values, MenuElement value) throws Exception {

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		if (menu == null) {
			return "";
		} else {
			Collection<MenuElement> elems;
			if (withVirtual) {
				elems = menu.getChildMenuElementsWithVirtual(ctx, onlyVisible, false);
			} else {
				elems = menu.getChildMenuElements(ctx, onlyVisible);
			}

			if (elems.size() == 0 && !selectableBetween) {
				return "";
			}

			boolean print = (depth >= fromDepth) && (depth <= toDepth);

			if (print) {
				out.println("<ul>");
			}

			MenuElement currentPage = ctx.getCurrentPage();
			Template currentTemplate = ctx.getCurrentTemplate();

			if (print && selectableBetween) {
				out.print("<li id=\"page_" + menu.getName() + "\"><div class=\"selection\"><input type=\"submit\" value=\"" + i18nAccess.getText("global.move-here", new String[][] { { "item", currentPage.getLabel(ctx) } }) + "\" name=\"P_" + menu.getId() + "\"/></div></li>");
			}

			int i = 0;
			for (MenuElement page : elems) {
				String selected = "";
				String cssClass = page.getName().toLowerCase();
				if (page.isSelected(ctx)) {
					String cssClassSelected = currentTemplate.getSelectedClass();
					if (page.isLastSelected(ctx)) {
						cssClassSelected = (cssClassSelected + " " + currentTemplate.getLastSelectedClass()).trim();
					}
					selected = "class=\"" + cssClassSelected + "\"";
					cssClass = cssClass + ' ' + cssClassSelected;
				}

				if (i == 0) {
					cssClass = cssClass + " first";
				} else if (i == elems.size() - 1) {
					cssClass = cssClass + " last";
				}
				String att = "";
				if (print) {
					String visualCode = page.getLabel(ctx);
					if (image && (page.getImage(ctx) != null)) {
						String imageURL = URLHelper.createTransformURL(ctx, page.getImage(ctx).getResourceURL(ctx), "menu");
						String imageDescription = page.getImage(ctx).getImageDescription(ctx);
						String imageUnselectedURL = URLHelper.createTransformURL(ctx, page.getImage(ctx).getResourceURL(ctx), currentTemplate.getUnSelectedClass());

						String url = imageUnselectedURL;
						if (page.isSelected(ctx)) {
							url = imageURL;
						}

						String imgName = "img_" + StringHelper.getRandomId();
						String startJS = "document.images['" + imgName + "'].src=";

						att = "onMouseover=\"" + startJS + "'" + imageURL + "'\" onMouseout=\"" + startJS + "'" + url + "'\"";

						visualCode = "<img src=\"" + url + "\" alt=\"" + imageDescription + "\" name=\"" + imgName + "\" class=\"autoMouseOver\" /><span class=\"text\">" + page.getLabel(ctx) + "</span>";
					}
					String selectedStrIn = "";
					String selectedStrBetween = "";
					String checked = "";
					String inputDisabled = "";
					String type = null;
					if (selecteableItem) {
						if (values != null) {
							if (values.contains(page)) {
								checked = " checked=\"checked\"";
							}
							type = "checkbox";
						} else if (value != null) {
							if (value.equals(page)) {
								checked = " checked=\"checked\"";
							}
							type = "radio";
						}
						selectedStrIn = "<input type=\"" + type + "\" name=\"parent\" value=\"" + page.getId() + "\"" + checked + "/> ";
					}
					if (selectableBetween) {
						type = "radio";
						if (values != null) {
							if (values.contains(page)) {
								checked = " checked=\"checked\"";
							}
							type = "checkbox";
						} else if (value != null) {
							if (value.equals(page)) {
								checked = " checked=\"checked\"";
								inputDisabled = " disabled=\"disabled\"";
							}
						}
						String disabled = "";
						if (page.equals(currentPage)) {
							disabled = " disabled=\"disabled\"";
						}
						selectedStrBetween = "<input" + disabled + " type=\"submit\" name=\"N_" + page.getId() + "\"" + checked + inputDisabled + " value=\"" + i18nAccess.getText("global.move-here", new String[][] { { "item", currentPage.getLabel(ctx) } }) + "\"/>";
					}

					if (page.getChildMenuElements(ctx, true).size() > 0) {
						cssClass = cssClass + " have-children";
					}

					out.println("<li class=\"" + cssClass + "\">");

					String title = page.getTitle(ctx);
					title = StringHelper.removeTag(title);
					String fullTitleHTML = "";
					if (!title.equals(visualCode)) {
						fullTitleHTML = " title=\"" + title + "\"";
					}
					out.print(selectedStrIn + "<a " + selected + " href=\"" + URLHelper.createURL(ctx, page) + "\" " + fullTitleHTML + " " + att + "><span><span>" + visualCode + "</span></span></a>");
					if (selectableBetween) {
						out.print("<div class=\"selection\">" + selectedStrBetween + "</div>");
					}

				}

				if (elems.size() > 0) {
					if (depth < toDepth) {
						/*
						 * if (print) { out.println("<li>"); }
						 */
						if (page.isSelected(ctx) || (extended && print)) {
							out.print(renderMenu(ctx, page, fromDepth, toDepth, onlyVisible, depth + 1, extended, image, withVirtual, selecteableItem, selectableBetween, values, value));
						}
						/*
						 * if (print) { out.println("</li>"); }
						 */
					}
				}
				if (print) {
					out.print("</li>");
				}
				i++;
			}
			if (print) {
				out.println("</ul>");
			}
		}
		out.close();
		return res.toString();
	}

	public static String renderPageStructure(ContentContext ctx, MenuElement page) throws Exception {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		ContentElementList content = page.getAllContent(ctx);
		int currentLevel = 0;
		String closeLi = "";
		while (content.hasNext(ctx)) {
			IContentVisualComponent comp = content.next(ctx);
			if (comp.getTitleLevel(ctx) > 0) {
				while (currentLevel < comp.getTitleLevel(ctx)) {
					out.println("<ul>");
					currentLevel++;
				}
				while (currentLevel > comp.getTitleLevel(ctx)) {
					out.println("</ul>");
					currentLevel--;
				}
				out.println(closeLi);
				closeLi = "</li>";
				out.print("<li><a href=\"#cp_" + comp.getId() + "\">" + comp.getTextLabel() + "</a>");
			}
		}
		while (currentLevel > 0) {
			out.println("</ul>");
			currentLevel--;
		}

		out.close();
		return res.toString();
	}

	public static String renderSelectableBetweenMenu(ContentContext ctx, MenuElement menu, MenuElement value) throws Exception {
		return renderMenu(ctx, menu, 0, 999, false, 0, true, false, false, false, true, null, value);
	}

	public static String renderSelectableMenu(ContentContext ctx, MenuElement menu, List<MenuElement> values) throws Exception {
		return renderMenu(ctx, menu, 0, 999, false, 0, true, false, false, true, false, values, null);
	}

	public static String renderSelectableMenu(ContentContext ctx, MenuElement menu, MenuElement value) throws Exception {
		return renderMenu(ctx, menu, 0, 999, false, 0, true, false, false, true, false, null, value);
	}

}
