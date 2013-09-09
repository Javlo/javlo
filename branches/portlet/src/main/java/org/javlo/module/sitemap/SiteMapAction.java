package org.javlo.module.sitemap;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

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
		
		public int getDepth()  {
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

	}
	
	private static String renderMenuElementBean(MenuElementBean item) {
		//return "<a href=\""+item.getUrl()+"\">"+item.getName()+ " - " + item.getTitle()+"</a> - "+item.getCountAllChildren();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);		
		out.println("<td style=\"padding-left:"+(item.getDepth()+1)*5+"px\"><a href=\""+item.getUrl()+"\">"+item.getName()+"</a></td>");
		out.println("<td>"+item.getTitle()+"</td>");
		out.println("<td>"+item.getCountAllChildren()+"</td>");
		out.println("<td>"+item.getCreator()+"</td>");
		out.close();
		return new String(outStream.toByteArray());
	}
	
	private static String renderChildrenNavigation(MenuElementBean item) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		
		out.println("<tr>");		
		for (MenuElementBean child : item.getChildren()) {
			out.println(renderMenuElementBean(child));
			out.println(renderChildrenNavigation(child));			
		}
		out.println("</tr>");
		
		
		out.close();
		return new String(outStream.toByteArray());
		
	}
	
	private static String renderNavigation(MenuElementBean item) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<table class=\"sTable3\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">");
		out.println("<thead><tr>");
		out.println("<td>name</td><td>title</td><td>#children</td><td>creator</td>");
		out.println("</tr></thead>");
		out.println(renderMenuElementBean(item));
		out.println(renderChildrenNavigation(item));
		out.println("</table>");
		out.close();
		return new String(outStream.toByteArray());
	}
	
	
	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		ctx.getRequest().setAttribute("navigation", renderNavigation(new MenuElementBean(ctx, content.getNavigation(ctx))) );
		return super.prepare(ctx, modulesContext);
	}

	@Override
	public String getActionGroupName() {
		return "sitemap";
	}

}
