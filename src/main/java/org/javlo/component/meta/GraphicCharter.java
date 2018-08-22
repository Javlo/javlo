/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextCreationBean;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.admin.AdminAction;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class GraphicCharter extends AbstractVisualComponent implements IAction {

	public static final String TYPE = "graphic-charter";

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_NONE;
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return new String[] { i18nAccess.getText("global.no", "no"), i18nAccess.getText("global.yes", "yes"), "hidden" };
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[0];
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		List<String> fonts = ctx.getCurrentTemplate().getWebFonts(ctx.getGlobalContext());
		Collections.sort(fonts);
		ctx.getRequest().setAttribute("currentContext", ctx.getGlobalContext());
		ctx.getRequest().setAttribute("fonts", fonts);
		ctx.getRequest().setAttribute("fontsMap", ctx.getCurrentTemplate().getFontReference(ctx.getGlobalContext()));
		String jsp = "/modules/admin/jsp/graphic_charter.jsp";
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<form action=\""+URLHelper.createURL(ctx)+"\" method=\"post\">");
		out.println("<input type=\"hidden\" name=\"webaction\" value=\""+TYPE+".create\" />");
		out.println("<p>" + XHTMLHelper.autoLink(getValue()) + "</p>");
		out.println(ServletHelper.executeJSP(ctx, jsp));
		out.println("<div class=\"action mb-3\">");
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		out.println("<button type=\"submit\" class=\"btn btn-primary btn-block\">"+i18nAccess.getViewText("admin.graphic-charter.button.create")+"</button>");
		out.println("</div></form>");
		out.close();
		return new String(outStream.toByteArray());
	}
	
	public static String performCreate(ContentContext ctx, RequestService rs) throws Exception {
		GlobalContextCreationBean createBean = GlobalContextCreationBean.getInstance(ctx.getRequest().getSession());
		GlobalContext newGlobalContext = createBean.create(ctx);
		AdminAction.updateGraphicCharter(ctx, newGlobalContext);
		ContentContext newContext = new ContentContext(ctx);
		newContext.setForceGlobalContext(newGlobalContext);
		newContext.setAbsoluteURL(true);
		newContext.setRenderMode(ContentContext.PREVIEW_MODE);
		newGlobalContext.setDefinedByHost(false);
		System.out.println(">>>>>>>>> GraphicCharter.performCreate : newContext.contextKey = "+newGlobalContext.getContextKey()); //TODO: remove debug trace
		String newURL = URLHelper.createURL(newContext, "/");
		System.out.println(">>>>>>>>> GraphicCharter.performCreate : newURL = "+newURL); //TODO: remove debug trace
		NetHelper.sendRedirectTemporarily(ctx.getResponse(), newURL);
		return null;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_ADMIN);
	}

	@Override
	public String getFontAwesome() {
		return "adjust";
	}

	@Override
	public String getActionGroupName() {
		return TYPE;
	}

}
