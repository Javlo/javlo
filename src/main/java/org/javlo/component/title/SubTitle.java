package org.javlo.component.title;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.LoremIpsumGenerator;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.service.ReverseLinkService;

/**
 * @author pvandermaesen
 */
public class SubTitle extends AbstractVisualComponent {

	public static final String TYPE = "subtitle";

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "<div " + getSpecialPreviewCssClass(ctx, "") + getSpecialPreviewCssId(ctx) + " >";
	}

	public String getXHTMLId(ContentContext ctx) {
		if (ctx.getRequest().getAttribute("__subtitle__" + getId()) != null) {
			return (String) ctx.getRequest().getAttribute("__subtitle__" + getId());
		}
		String htmlID = StringHelper.createFileName(getValue());
		if (htmlID.trim().length() == 0) {
			htmlID = "empty";
		}
		htmlID = "H_" + htmlID;
		while (ctx.getRequest().getAttribute("__subtitle__" + htmlID) != null) {
			htmlID = htmlID + "_bis";
		}
		ctx.getRequest().setAttribute("__subtitle__" + htmlID, "");
		ctx.getRequest().setAttribute("__subtitle__" + getId(), htmlID);
		return htmlID;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (getRenderer(ctx) != null) {
			return executeJSP(ctx, getRenderer(ctx));
		} else {
			StringBuffer res = new StringBuffer();
			String level = "2";
			if (getStyle(ctx) != null) {
				level = getStyle(ctx);
			}
			String value = getValue();
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			if (level.equals("7") || level.equals("8") || level.equals("9")) {
				res.append("<div id=\"" + getXHTMLId(ctx) + "\" class=\"subtitle-" + level + "\">");
				ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
				value = reverserLinkService.replaceLink(ctx, value);
				res.append(XHTMLHelper.textToXHTML(value));
				res.append("</div>");
			} else {
				res.append("<h" + level + " id=\"" + getXHTMLId(ctx) + "\" class=\"subtitle\">");
				ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
				value = reverserLinkService.replaceLink(ctx, value);
				res.append(XHTMLHelper.textToXHTML(value));
				res.append("</h" + level + ">");
			}
			return res.toString();
		}
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_HIGH;
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "2", "3", "4", "5", "6", "7", "8", "9" };
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		return new String[] { "level-2", "level-3", "level-4", "level-5", "level-6", "level-7", "level-8", "level-9" };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "level";
	}

	public String getCSSClassName(ContentContext ctx) {
		return getStyle(ctx);
	}

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	public int getTitleLevel(ContentContext ctx) {
		try {
			return Integer.parseInt(getStyle());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean initContent(ContentContext ctx) {
		setValue(LoremIpsumGenerator.getParagraph(6, true, true));
		setModify();
		return true;
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

}
