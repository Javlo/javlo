/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.RootMenuElement;
import org.javlo.service.ContentService;

/**
 * @author pvandermaesen
 */
public class SiteMap extends AbstractVisualComponent {

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "visible", "all", "children", "children-visible", "virtual-children" };
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			return new String[] { i18n.getText("content.web-map.only-visible"), i18n.getText("content.web-map.all"), i18n.getText("content.web-map.children"), i18n.getText("content.web-map.children-visible"), i18n.getText("content.web-map.virtual-children") };
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new String[] { "visible", "all", "children", "children-visible", "virtual-children" };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "";
	}

	int depth = 1;

	private void recNav(ContentContext ctx, MenuElement menu, PrintStream out, boolean showVisible, boolean virtual, Collection<MenuElement> pastNode, int calldepth) throws Exception {

		if (calldepth > getDepth()) {
			return;
		}

		List<MenuElement> childs;

		if (virtual) {
			childs = menu.getChildMenuElementsWithVirtual(ctx, false, false);
		} else {
			childs = menu.getChildMenuElements();
		}
		out.print("<li class=\"webmap-");
		out.print(depth);
		out.println("\">");
		if (menu.isRealContent(ctx)) {
			out.print("<a href=\"");
			out.print(URLHelper.createURL(ctx, menu.getPath()));
			out.print("\"> ");
		} else {
			out.print("<span class=\"no-link\">");
		}
		if (menu instanceof RootMenuElement) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String home = i18nAccess.getContentViewText("global.home");
			out.println(home);
		} else {
			out.print(menu.getFullLabel(ctx));
		}
		if (menu.isRealContent(ctx)) {
			out.print("</a>");
		} else {
			out.print("</span>");
		}
		String description = menu.getDescription(ctx);
		if ((description != null) && (description.trim().length() > 0)) {
			out.print("<span> : ");
			out.print(description);
			out.print("</span>");
		}

		boolean showAll = getStyle(ctx).equalsIgnoreCase("all");
		pastNode.add(menu);
		if (childs.size() > 0) {
			out.println("<ul>");
		}
		for (MenuElement page : childs) {
			if ((showAll || (page.isVisible(ctx) || showVisible) && !pastNode.contains(page))) {
				depth++;
				if (depth < 50) {
					recNav(ctx, page, out, showVisible, virtual, pastNode, calldepth + 1);
				}
				depth--;
			}
		}
		if (childs.size() > 0) {
			out.println("</ul>");
		}
		pastNode.remove(menu);
		out.println("</li>");
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement menu = content.getNavigation(ctx);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		out.print("<div class=\"");
		out.print("webmap");
		out.println("\"><ul>");
		Collection<MenuElement> childs = menu.getChildMenuElements(ctx, false);
		boolean showAll = false;
		boolean showVisible = false;
		boolean virtual = false;
		if (getStyle(ctx) != null) {
			showAll = getStyle(ctx).equalsIgnoreCase("all");
			if (getStyle(ctx).equalsIgnoreCase("children")) {
				menu = ctx.getCurrentPage();
				childs = menu.getChildMenuElements(ctx, false);
				showVisible = true;
			} else if (getStyle(ctx).equalsIgnoreCase("virtual-children")) {
				menu = ctx.getCurrentPage();
				childs = menu.getChildMenuElementsWithVirtual(ctx, false, false);
				showVisible = true;
				virtual = true;
			} else if (getStyle(ctx).equalsIgnoreCase("children-visible")) {
				menu = ctx.getCurrentPage();
				childs = menu.getChildMenuElements(ctx, true);
				showVisible = false;
				virtual = true;
			}
		}
		for (MenuElement child : childs) {
			if (showAll || (child.isVisible(ctx) || showVisible)) {
				recNav(ctx, child, out, showVisible, virtual, new LinkedList<MenuElement>(), 1);
			}
		}
		out.println("</ul></div>");

		return new String(outStream.toByteArray());
	}

	@Override
	public String getType() {
		return "web-map";
	}

	public int getDepth() {
		if (getValue() == null || getValue().trim().length() == 0) {
			return Integer.MAX_VALUE;
		} else {
			return Integer.parseInt(getCorrectValue());
		}
	}

	private String getCorrectValue() {
		String outValue = "99";
		try {
			outValue = "" + Integer.parseInt(getValue());
		} catch (Throwable t) {
		}
		return outValue;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		out.print("<label for=\"" + getContentName() + "\">");
		out.print(i18nAccess.getText("content.web-map.depth"));
		out.println("</label>");

		out.println("<input type=\"text\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\" value=\"" + getCorrectValue() + "\" />");

		out.close();
		return writer.toString();
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}

	@Override
	public boolean isVisible(ContentContext ctx) {
		return true;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

}
