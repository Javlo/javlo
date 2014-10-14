package org.javlo.module.sitemap;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;

public class SiteMapAction extends AbstractModuleAction {

	public static class MenuElementBean {

		private ContentContext ctx;
		private MenuElement menuElement;
		private List<MenuElementBean> children = null;
		private Integer countAllChildren = null;

		public MenuElementBean(ContentContext ctx, MenuElement menuElement) {
			this.ctx = ctx;
			this.menuElement = menuElement;
		}

		public List<MenuElementBean> getChildren() {
			if (children == null) {
				List<MenuElement> menuChildren = menuElement.getChildMenuElements();
				if (menuChildren.size() == 0) {
					children = Collections.EMPTY_LIST;
				} else {
					children = new LinkedList<SiteMapAction.MenuElementBean>();
					for (MenuElement child : menuChildren) {
						children.add(new MenuElementBean(ctx, child));
					}
				}
			}
			return children;
		}

		public String getId() {
			return menuElement.getId();
		}

		public String getName() {
			return menuElement.getName();
		}

		public String getTitle() {
			try {
				return menuElement.getTitle(ctx);
			} catch (Exception e) {
				e.printStackTrace();
				return "error : " + e.getMessage();
			}
		}

		public String getCreator() {
			try {
				return menuElement.getCreator();
			} catch (Exception e) {
				e.printStackTrace();
				return "error : " + e.getMessage();
			}
		}

		public int getDepth() {
			int depth = 0;
			MenuElement parent = menuElement;
			while (parent.getParent() != null) {
				depth++;
				parent = parent.getParent();
			}
			return depth;
		}

		public String getUrl() {
			return URLHelper.createURL(ctx, menuElement);
		}

		public int getCountAllChildren() {
			if (countAllChildren == null) {
				int count = getChildren().size();
				for (MenuElementBean child : getChildren()) {
					count = count + child.getCountAllChildren();
				}
				countAllChildren = count;
			}
			return countAllChildren;
		}
		
		public boolean isRoot() {
			return menuElement.getParent() == null;
		}

	}

	private static String renderMenuElementBean(ContentContext ctx, MenuElementBean item) {
		// return "<a href=\""+item.getUrl()+"\">"+item.getName()+ " - " +
		// item.getTitle()+"</a> - "+item.getCountAllChildren();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		String pasteAction = "";
		pasteAction = "<input type=\"submit\" name=\"paste-child-" + item.getId() + "\" value=\"move as child\" />";
		if (!item.isRoot()) {
			pasteAction += "<input type=\"submit\" name=\"paste-brother-" + item.getId() + "\" value=\"move as brother\" />";
		}

		out.println("<td class=\"action check\"><input type=\"checkbox\" name=\"selection\" value=\"" + item.getId() + "\" /></td>");
		out.println("<td class=\"action button" + "\">" + pasteAction + "</td>");
		out.println("<td class=\"link\"><a href=\"" + item.getUrl() + "\">" + item.getName() + "</a></td>");
		out.println("<td>" + item.getTitle() + "</td>");
		out.println("<td>" + item.getCountAllChildren() + "</td>");
		out.println("<td>" + item.getCreator() + "</td>");
		out.close();
		return new String(outStream.toByteArray());
	}

	private static String renderChildrenNavigation(ContentContext ctx, MenuElementBean item) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		for (MenuElementBean child : item.getChildren()) {
			out.println("<tr class=\"depth-" + item.getDepth() + "\">");
			out.println(renderMenuElementBean(ctx, child));
			out.println("</tr>");
			out.println(renderChildrenNavigation(ctx, child));
		}

		out.close();
		return new String(outStream.toByteArray());

	}

	private static String renderNavigation(ContentContext ctx, MenuElementBean item) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<form action=\"" + URLHelper.createURL(ctx) + "\" method=\"post\"><input type=\"hidden\" name=\"webaction\" value=\"move\" />");
		out.println("<table class=\"sTable3\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">");
		out.println("<thead><tr>");
		out.println("<td>&nbsp;</td><td>&nbsp;</td><td>name</td><td>title</td><td>#children</td><td>creator</td>");
		out.println("</tr></thead>");
		out.println("<tr class=\"depth-root\">");
		out.println(renderMenuElementBean(ctx, item));
		out.println("</tr>");
		out.println(renderChildrenNavigation(ctx, item));
		out.println("</table></form>");
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		ctx.getRequest().setAttribute("navigation", renderNavigation(ctx, new MenuElementBean(ctx, content.getNavigation(ctx))));
		return super.prepare(ctx, modulesContext);
	}

	@Override
	public String getActionGroupName() {
		return "sitemap";
	}

	public static String performMove(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		MenuElement targetPage = null;
		boolean asChildren = true;
		List<MenuElement> pageToMove = new LinkedList<MenuElement>();
		Set<String> selection = new HashSet<String>(Arrays.asList(rs.getParameterValues("selection", new String[0])));

		ContentService content = ContentService.getInstance(ctx.getRequest());
		if (rs.getParameter("paste-brother-0", null) != null) {
			asChildren = false;
			targetPage = content.getNavigation(ctx);
		} else if (rs.getParameter("paste-child-0", null) != null) {
			targetPage = content.getNavigation(ctx);
		}
		for (MenuElement page : content.getNavigation(ctx).getAllChildren()) {
			if (selection.contains(page.getId())) {
				pageToMove.add(page);
			}
			if (targetPage == null) {
				if (rs.getParameter("paste-brother-" + page.getId(), null) != null) {
					asChildren = false;
					targetPage = content.getNavigation(ctx).searchChildFromId(page.getId());
				} else if (rs.getParameter("paste-child-" + page.getId(), null) != null) {
					targetPage = content.getNavigation(ctx).searchChildFromId(page.getId());
				}
			}
		}
		
		MenuElement page = targetPage;
		while (page != null) {
			if (selection.contains(page.getId())) {
				return i18nAccess.getText("sitemap.error.movetmyself", "error, you can'nt move a parent of the target.");
			}
			page = page.getParent();
		}		
		
		for (MenuElement pageMov : pageToMove) {
			if (asChildren) {
				pageMov.moveToParent(targetPage);
				pageMov.setPriority(1);
				NavigationHelper.changeStepPriority(targetPage.getChildMenuElements(), 10);
			} else {
				pageMov.moveToParent(targetPage.getParent());
				pageMov.setPriority(targetPage.getPriority()+1);
				NavigationHelper.changeStepPriority(targetPage.getParent().getChildMenuElements(), 10);
			}			
		}
		
		PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		
		return null;
	}
	
	
}
