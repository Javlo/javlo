/*
 * Created on 08-Sep-2004
 */
package org.javlo.helper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.component.column.TableBreak;
import org.javlo.component.container.IContainer;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.component.text.DynamicParagraph;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.fields.Field;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.utils.Cell;

/**
 * @author pvandermaesen some method for help create component.
 */
public class ComponentHelper {
	
	private static Logger logger = Logger.getLogger(ComponentHelper.class.getName());

	/**
	 * create a dynamic paragraph with a specific content on a page with a
	 * DynamicParagraph
	 * 
	 * @throws Exception
	 */
	public static String createDynamicPage(ContentContext ctx, String path, String content) throws Exception {
		MenuElement dynPage = MacroHelper.createPathIfNotExist(ctx, path);
		List<IContentVisualComponent> comps = dynPage.getContentByType(ctx, DynamicParagraph.TYPE);
		if (comps.size() == 0) {
			MacroHelper.addContent(ctx.getRequestContentLanguage(), dynPage, "0", DynamicParagraph.TYPE, "", ctx.getCurrentEditUser());
			comps = dynPage.getContentByType(ctx, DynamicParagraph.TYPE);
		}
		DynamicParagraph dynParagraph = (DynamicParagraph) comps.iterator().next();
		String contentId = dynParagraph.addMessage(ctx, content);

		Map<String, String> params = new HashMap<String, String>();
		params.put(DynamicParagraph.MESSAGE_ID_PARAM_NAME, contentId);

		ContentContext pageCtx = new ContentContext(ctx);
		pageCtx.setRenderMode(ContentContext.PAGE_MODE);
		String outURL = URLHelper.createURL(pageCtx, path, params);

		return outURL;
	}

	/*
	 * public static final boolean DisplayTitle(IContentVisualComponent[] comps,
	 * int i) { if (i >= comps.length) { return false; } for (int j = i + 1; (j
	 * < comps.length) && !(comps[j] instanceof SpecialTitle); j++) { if
	 * (comps[j].isVisible()) { return true; } } return false; }
	 */

	public static IContentVisualComponent getComponentFromRequest(ContentContext ctx) throws Exception {
		return getComponentFromRequest(ctx, IContentVisualComponent.COMP_ID_REQUEST_PARAM);
	}

	/**
	 * get a component with the id in the request.
	 * 
	 * @param ctx
	 * @param paramName
	 *            the name of the parameter it contains the id.
	 * @return
	 * @throws Exception
	 */
	public static IContentVisualComponent getComponentFromRequest(ContentContext ctx, String paramName) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String compId = requestService.getParameter(paramName, null);
		IContentVisualComponent comp = null;
		if (compId != null) {
			ContentService content = ContentService.getInstance(ctx.getRequest());
			comp = content.getComponent(ctx, compId);
		}
		return comp;
	}

	static public String getInternalLinkEdition(ContentContext ctx, String linkName, String linkIdStr) {
		ContentService content = ContentService.getInstance(ctx.getRequest());

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		try {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());
			String linkTitle = i18nAccess.getText("component.link.link");

			out.println("<table class=\"edit\"><tr><td style=\"text-align: center;\" width=\"50%\">");
			out.println(linkTitle + " : ");
			out.println("<select name=\"" + linkName + "\">");
			MenuElement elem = content.getNavigation(ctx);
			MenuElement[] values = elem.getAllChildren();
			String currentLink = null;
			for (MenuElement value : values) {
				if (linkIdStr.equals(value.getId())) {
					currentLink = value.getName();
					out.println("<option selected=\"true\" value=\"" + value.getId() + "\">");
				} else {
					out.println("<option value=\"" + value.getId() + "\">");
				}
				out.println(value.getPath());
				out.println("</option>");
			}
			out.println("</select>");
			if (currentLink != null) {
				out.print("<a href=\"");
				out.print(URLHelper.createURL(currentLink, ctx));
				out.println("\">go</a>");
			}
			out.println("</td></tr></table>");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return writer.toString();
	}

	/**
	 * create a XHTML link with a id of a MenuElement
	 * 
	 * @param ctx
	 *            the current dc context
	 * @param linkId
	 *            the unic identifier of the page
	 * @param label
	 *            the set label of the link
	 * @return a XHTML code
	 * @throws Exception
	 */
	static public String getInternalLinkFromId(ContentContext ctx, String linkId, String label) throws Exception {

		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement nav = content.getNavigation(ctx);

		MenuElement child = nav.searchChildFromId(linkId);
		String link = child.getPath();
		if (label.trim().length() == 0) {
			label = child.getLabel(ctx);
		}
		StringBuffer res = new StringBuffer();
		String url = URLHelper.createURL(link, ctx);
		res.append("<a href=\" ");
		res.append(url);
		res.append("\">");
		res.append(label);
		res.append("</a>");
		return res.toString();
	}

	public static void moveComponent(ContentContext ctx, IContentVisualComponent comp, IContentVisualComponent newPrevious, MenuElement targetPage, String area) throws Exception {
		comp.getPage().removeContent(ctx, comp.getId());
		comp.getComponentBean().setArea(area);
		ComponentBean newComp = comp.getComponentBean();
		newComp.setLanguage(ctx.getRequestContentLanguage()); // component could be move between two different browser
		if (newPrevious != null) {
			newPrevious.getPage().addContent(newPrevious.getId(), newComp);
			comp.setPage(newPrevious.getPage());
		} else {
			targetPage.addContent("0", newComp);
			comp.setPage(targetPage);
		}
		ContentContext areaCtx = ctx.getContextWithArea(comp.getArea());
		updateNextAndPreviouv(areaCtx, comp.getPage().getContent(areaCtx).getIterable(areaCtx));
	}

	public static void smartMoveComponent(ContentContext ctx, IContentVisualComponent comp, IContentVisualComponent newPrevious, MenuElement targetPage, String area) throws Exception {
		if (comp != null && newPrevious != null && comp.getId().equals(newPrevious.getId())) {
			return;
		}
		if (!(comp instanceof TableBreak) && !(comp instanceof IContainer)) {
			moveComponent(ctx, comp, newPrevious, targetPage, area);
		} else if (comp instanceof IContainer) {
			if (!((IContainer) comp).isOpen(ctx)) {
				moveComponent(ctx, comp, newPrevious, targetPage, area);
			} else {
				String openType = comp.getType();
				IContentVisualComponent nextComp = comp;
				List<IContentVisualComponent> componentToMove = new LinkedList<IContentVisualComponent>();
				componentToMove.add(nextComp);
				nextComp = ComponentHelper.getNextComponent(nextComp, ctx);
				boolean closeFound = nextComp == null;
				int depth = 0;
				while (!closeFound) {
					if (newPrevious != null && nextComp.getId().equals(newPrevious.getId())) { /* if target inside the container */
						moveComponent(ctx, comp, newPrevious, targetPage, area);
						return;
					}
					componentToMove.add(nextComp);
					nextComp = ComponentHelper.getNextComponent(nextComp, ctx);
					if (nextComp != null) {
						if (nextComp.getType().equals(openType)) {
						if (((IContainer) nextComp).isOpen(ctx)) {
							depth++;
						} else {
							if (depth == 0) {
								closeFound = true;
							} else {
								depth--;
							}
						}
						}
					} else {
						closeFound = true;
					}
				}
				if (nextComp != null) {
					componentToMove.add(nextComp);
				}
				for (IContentVisualComponent moveComp : componentToMove) {
					moveComponent(ctx, moveComp, newPrevious, targetPage, area);
					newPrevious = moveComp;
				}
			}
		} else if (comp instanceof TableBreak) {
			IContentVisualComponent openTable = ((TableBreak) comp).getOpenTableComponent(ctx);
			if (openTable == null) {
				logger.warning("table not open : "+comp.getId());
				return;
			} else {
				ContentContext compCtx = ctx.getContextWithArea(comp.getArea());
				ContentElementList tableContent = comp.getPage().getContent(compCtx);
				boolean inTable = false;
				while (tableContent.hasNext(compCtx)) {
					IContentVisualComponent nextComp = tableContent.next(compCtx);
					if (nextComp != null) {
						if (nextComp.getId().equals(openTable.getId())) {
							inTable = true;
						}
						if (inTable) {
							moveComponent(ctx, nextComp, newPrevious, targetPage, area);
							newPrevious = nextComp;
						}
					}
				}
			}
		}
	}

	/**
	 * change all area of a componentBean list.
	 * 
	 * @param beans
	 *            list of content.
	 * @param newArea
	 *            new area, can be null.
	 */
	public static void changeAllArea(Iterable<ComponentBean> beans, String newArea) {
		for (ComponentBean bean : beans) {
			bean.setArea(newArea);
		}
	}

	public static String getPreviousComponentId(IContentVisualComponent inComp, ContentContext ctx) throws Exception {
		IContentVisualComponent previous = getPreviousComponent(inComp, ctx);
		if (previous != null) {
			return previous.getId();
		} else {
			return "0";
		}
	}

	public static IContentVisualComponent getPreviousComponent(IContentVisualComponent inComp, ContentContext ctx) throws Exception {
		if (inComp.getPage() == null) {
			return null;
		}
		ContentContext ctxCompArea = ctx.getContextWithArea(inComp.getArea());
		ContentElementList content = inComp.getPage().getContent(ctxCompArea);
		IContentVisualComponent previousComp = null;
		IContentVisualComponent comp = null;
		while (content.hasNext(ctxCompArea)) {
			previousComp = comp;
			comp = content.next(ctxCompArea);
			if (comp.getId().equals(inComp.getId())) {
				return previousComp;
			}
		}
		return null;
	}

	public static IContentVisualComponent getNextComponent(IContentVisualComponent inComp, ContentContext ctx) throws Exception {
		if (inComp == null || inComp.getPage() == null) {
			return null;
		}
		ContentContext ctxCompArea = ctx.getContextWithArea(inComp.getArea());
		ContentElementList content = inComp.getPage().getContent(ctxCompArea);
		IContentVisualComponent previousComp = null;
		IContentVisualComponent comp = null;
		while (content.hasNext(ctxCompArea)) {
			previousComp = comp;
			comp = content.next(ctxCompArea);
			if (previousComp != null && previousComp.getId().equals(inComp.getId())) {
				return comp;
			}
		}
		return null;
	}

	/**
	 * get the position of the component in the list of component with same type
	 * with current ContentContext return -1 if component is not found.
	 * 
	 * @throws Exception
	 */
	public static int getComponentPosition(ContentContext ctx, IContentVisualComponent comp) throws Exception {
		int componentPosition = 1;
		ContentContext ctxCompArea = ctx.getContextWithArea(comp.getArea());
		ContentElementList content = comp.getPage().getContent(ctxCompArea);
		while (content.hasNext(ctxCompArea)) {
			IContentVisualComponent nextComp = content.next(ctxCompArea);
			if (nextComp.getId().equals(comp.getId())) {
				return componentPosition;
			}
			if (nextComp.getType().equals(comp.getType())) {
				componentPosition++;
			}
		}
		return -1;
	}

	/**
	 * get the the component with the position in the list of component with
	 * same type with current ContentContext return null if position is to big
	 * 
	 * @throws Exception
	 */
	public static IContentVisualComponent getComponentWidthPosition(ContentContext ctx, MenuElement page, String area, String type, int position) throws Exception {
		int componentPosition = 0;
		ContentContext ctxCompArea = ctx.getContextWithArea(area);
		ContentElementList content = page.getContent(ctxCompArea);
		while (content.hasNext(ctxCompArea)) {
			IContentVisualComponent nextComp = content.next(ctxCompArea);
			if (nextComp.getType().equals(type)) {
				componentPosition++;
			}
			if (componentPosition == position) {
				return nextComp;
			}
		}
		return null;
	}

	public static void updateNextAndPreviouv(ContentContext ctx, Iterable<IContentVisualComponent> comps) throws Exception {
		for (IContentVisualComponent comp : comps) {
			comp.setPreviousComponent(getPreviousComponent(comp, ctx));
			comp.setNextComponent(getNextComponent(comp, ctx));
		}
	}

	public static Cell[][] componentsToArray(ContentContext ctx, Collection<IContentVisualComponent> components, String type) throws Exception {
		boolean firstLine = true;
		List<String[]> cols = new LinkedList<String[]>();
		for (IContentVisualComponent comp : components) {
			if (type == null || comp.getType().equals(type)) {
				if (comp instanceof DynamicComponent) {
					DynamicComponent dcomp = (DynamicComponent) comp;
					if (firstLine) {
						firstLine = false;
						List<String> values = new LinkedList<String>();
						values.add("page");
						values.add("authors");
						values.add("style");
						values.add("area");
						List<Field> fields = dcomp.getFields(ctx);
						for (Field field : fields) {
							values.add(field.getName());
						}
						String[] row = new String[values.size()];
						int i = 0;
						for (String val : values) {
							row[i] = val;
							i++;
						}
						cols.add(row);
					}

					List<String> values = new LinkedList<String>();
					values.add(comp.getPage().getName());
					values.add(comp.getAuthors());
					values.add(comp.getStyle(ctx));
					values.add(comp.getArea());
					List<Field> fields = dcomp.getFields(ctx);
					for (Field field : fields) {
						values.add(field.getValue(new Locale(ctx.getRequestContentLanguage())));
					}
					String[] row = new String[values.size()];
					int i = 0;
					for (String val : values) {
						row[i] = val;
						i++;
					}
					cols.add(row);
				} else {
					if (firstLine) {
						firstLine = false;
						List<String> values = new LinkedList<String>();
						values.add("page");
						values.add("authors");
						values.add("value");
						values.add("style");
						values.add("area");
					}
					List<String> values = new LinkedList<String>();
					values.add(comp.getPage().getName());
					values.add(comp.getAuthors());
					values.add(comp.getValue(ctx));
					values.add(comp.getStyle(ctx));
					values.add(comp.getArea());
					String[] row = new String[values.size()];
					int i = 0;
					for (String val : values) {
						row[i] = val;
						i++;
					}
					cols.add(row);
				}
			}
		}
		Cell[][] cells = new Cell[cols.size()][];
		int i = 0;
		for (String[] row : cols) {
			cells[i] = new Cell[row.length];
			int j = 0;
			for (String cell : row) {
				cells[i][j] = new Cell(cell, null, cells, i, j);
				j++;
			}
			i++;
		}
		return cells;
	}

}