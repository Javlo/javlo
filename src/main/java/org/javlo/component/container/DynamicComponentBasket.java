package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class DynamicComponentBasket extends AbstractVisualComponent implements IAction {

	public static final String TYPE = "dynamic-component-basket";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "long", "short" };
	}

	public boolean isLong() {
		return "long".equals(getStyle());
	}

	@Override
	public boolean isAjaxWrapper(ContentContext ctx) {
		return true;
	}

	public String getLargeViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		String pdfURL = URLHelper.createPDFURL(ctx);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		pdfURL = URLHelper.addParam(pdfURL, ComponentBasket.SESSION_KEY, ctx.getGlobalContext().addSharedObject(ComponentBasket.getComponentBasket(ctx)));
		out.println("<div class=\"btn-group pdf-link\"><a class=\"btn btn-secondary\" href=\"" +  pdfURL + "\"><i class=\"fa fa-file-pdf-o\" aria-hidden=\"true\"></i>&nbsp;"+i18nAccess.getViewText("global.download")+" PDF</a></div>");
		int index=0;
		ContentService contentService = ContentService.getInstance(ctx.getRequest());
		ctx.getRequest().setAttribute("first", true);
		ctx.getRequest().setAttribute("last", false);
		ctx.getRequest().setAttribute("share", true);
		ctx.getRequest().setAttribute("previousSame", false);
		ctx.getRequest().setAttribute("nextSame", true);
		int size = ComponentBasket.getComponentBasket(ctx).getComponents().size();
		for (String selctId : ComponentBasket.getComponentBasket(ctx).getComponents()) {
			index++;
			if (index == size) {
				ctx.getRequest().setAttribute("nextSame", false);
			}
			ctx.getRequest().setAttribute("componentIndex", index);			
			DynamicComponent container = (DynamicComponent)contentService.getComponent(ctx, selctId);
			if (container != null) {
				out.println(container.getViewListXHTMLCode(ctx));
				ctx.getRequest().setAttribute("first", false);
				ctx.getRequest().setAttribute("previousSame", true);
			}
			
		}
		out.close();
		return new String(outStream.toByteArray());
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (isLong()) {
			return getLargeViewXHTMLCode(ctx);
		} else {
			return getSmallViewXHTMLCode(ctx);
		}
	}

	public String getSmallViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);
		ComponentBasket basket = ComponentBasket.getComponentBasket(ctx);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"card ajax-group\"><div class=\"card-header\">");
		out.println(ComponentBasket.getComponentBasket(ctx).size() + " " + getValue());
		Map<String, String> params = new HashMap<String, String>();
		params.put("selection", StringHelper.collectionToString(basket.getComponents()));
		String basketURL = URLHelper.createURLFromPageName(ctx, "basket", params);		
		out.println("<a class=\"share-link pull-right\" href=\"" + basketURL + "\"><i class=\"fa fa-share\" aria-hidden=\"true\"></i></a>");				
		out.println("</div>");
		if (basket.components.size() > 0) {
			out.println("<ul class=\"list-group list-group-flush\">");
			ContentService contentService = ContentService.getInstance(ctx.getRequest());
			for (String compid : basket.components) {
				DynamicComponent comp = (DynamicComponent) contentService.getComponent(ctx, compid);
				if (comp != null) {
					params.clear();
					params.put("webaction", getActionGroupName() + ".delete");
					params.put("comp", comp.getId());
					String delURL = URLHelper.createAjaxURL(ctx, params);
					out.println("<li class=\"list-group-item\">" + comp.getTextTitle(ctx) + "<a id=\"dc-" + comp.getId() + "\" class=\"pull-right ajax\" href=\"" + delURL + "\"><i class=\"fa fa-times\" aria-hidden=\"true\"></i></a></li>");
				}
			}
			out.println("</ul>");
		}
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return ComponentBasket.getComponentBasket(ctx).size() > 0;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}

	@Override
	public String getHexColor() {
		return DEFAULT_COLOR;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}

	public static String performAddcomp(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		if (ComponentBasket.getComponentBasket(ctx).addComponent(rs.getParameter("comp", null))) {
			for (IContentVisualComponent comp : ctx.getCurrentPage().getContentByType(ctx.getContextWithArea(null), DynamicComponentBasket.TYPE)) {
				if (comp != null) {
					ctx.getAjaxInsideZone().put("cp-" + comp.getId(), ((AbstractVisualComponent) comp).getViewXHTMLCode(ctx));
				}
			}
		}
		return null;
	}

	public static String performDelete(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		if (ComponentBasket.getComponentBasket(ctx).removeComponent(rs.getParameter("comp", null))) {
			for (IContentVisualComponent comp : ctx.getCurrentPage().getContentByType(ctx.getContextWithArea(null), DynamicComponentBasket.TYPE)) {
				if (comp != null) {
					ctx.getAjaxInsideZone().put("cp-" + comp.getId(), ((AbstractVisualComponent) comp).getViewXHTMLCode(ctx));
				}
			}
		}
		return null;
	}

}
