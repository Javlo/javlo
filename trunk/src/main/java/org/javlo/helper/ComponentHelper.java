/*
 * Created on 08-Sep-2004
 */
package org.javlo.helper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.text.DynamicParagraph;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen some method for help create component.
 */
public class ComponentHelper {

	/**
	 * create a dynamic paragraph with a specific content on a page with a DynamicParagraph
	 * 
	 * @throws Exception
	 */
	public static String createDynamicPage(ContentContext ctx, String path, String content) throws Exception {
		MenuElement dynPage = MacroHelper.createPathIfNotExist(ctx, path);
		List<IContentVisualComponent> comps = dynPage.getContentByType(ctx, DynamicParagraph.TYPE);
		if (comps.size() == 0) {
			MacroHelper.addContent(ctx.getRequestContentLanguage(), dynPage, "0", DynamicParagraph.TYPE, "");
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
	 * public static final boolean DisplayTitle(IContentVisualComponent[] comps, int i) { if (i >= comps.length) { return false; } for (int j = i + 1; (j < comps.length) && !(comps[j] instanceof SpecialTitle); j++) { if (comps[j].isVisible()) { return true; } } return false; }
	 */

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

	public static void moveComponent(ContentContext ctx, IContentVisualComponent comp, IContentVisualComponent newPrevious, String area) {
		comp.getPage().removeContent(ctx, comp.getId());
		if (newPrevious != null) {
			newPrevious.getPage().addContent(newPrevious.getId(), comp.getComponentBean());
		} else {
			comp.getPage().addContent("0", comp.getComponentBean());
		}
		comp.getComponentBean().setArea(area);
	}
}