package org.javlo.component.title;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentLayout;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.LoremIpsumGenerator;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.service.ReverseLinkService;
import org.javlo.template.Area;

/**
 * @author pvandermaesen
 */
public class SubTitle extends AbstractVisualComponent {

	public static final String TYPE = "subtitle";

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		String cssClass = "subtitle-wrapper subtitle-wrapper-"+getStyle();
		if (isBackgroundColored()) {
			cssClass = cssClass + " colored-wrapper";
		}
		return "<div " + getSpecialPreviewCssClass(ctx, cssClass) + getSpecialPreviewCssId(ctx) + " "+getInlineStyle(ctx)+">";
	}
	
	@Override
	protected void init() throws ResourceNotFoundException {
		super.init();
		if (getLayout() == null) {
			getComponentBean().setLayout(new ComponentLayout(""));
		}
	}
	
	public String getXHTMLId(ContentContext ctx) {
		if (ctx.getRequest().getAttribute("__subtitle__" + getId()) != null) {
			return (String) ctx.getRequest().getAttribute("__subtitle__" + getId());
		}
		String htmlID = StringHelper.createFileName(getValue()).replace('-', '_');
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
			if (getStyle() != null) {
				level = getStyle();
			}
			String value = getValue();
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			
			String colored = "";
			if (isColored()) {
				colored = " colored";
			}
			
			if (level.equals("7") || level.equals("8") || level.equals("9")) {				
				res.append("<div id=\"" + getXHTMLId(ctx) + "\" class=\"subtitle-" + level + colored + "\">");
				ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
				value = reverserLinkService.replaceLink(ctx, this, value);
				res.append(XHTMLHelper.textToXHTML(value));
				res.append("</div>");
			} else {
				String style="";
				if (getTextColor() != null && getTextColor().length() > 3) {
					style=" style=\"color:"+getTextColor()+';'+getLayout().getStyle()+'"';
				} else {
					String areaColor = "";
					Area areaStyle = null;
					if (ctx.getCurrentTemplate().isEditable()) {
						String area = (String)ctx.getRequest().getAttribute("area");
						areaStyle = ctx.getCurrentTemplate().getArea(ctx.getCurrentTemplate().getRows(), area);
					}
					
					if (areaStyle != null && areaStyle.getFinalTitleColor() != null && areaStyle.getFinalTitleColor().trim().length() > 0) {
						areaColor = " color:"+areaStyle.getFinalTitleColor()+';';
					}
					String cssStyle = getLayout().getStyle()+areaColor;
					if (cssStyle.trim().length() > 0) {
						style=" style=\""+cssStyle+'"';
					}
				}
				res.append("<h" + level + " id=\"" + getXHTMLId(ctx) + "\" class=\"subtitle"+colored+"\""+style+">");
				ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
				value = reverserLinkService.replaceLink(ctx, this, value);
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
	
	/**
	 * get the max level of the subtitle.
	 * @param ctx
	 * @return
	 */
	protected int getMaxLevel(ContentContext ctx) {
		return Integer.parseInt(getConfig(ctx).getProperty("max-level", "7"));
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		int maxLevel = getMaxLevel(ctx);
		String[] outStyleList = new String[maxLevel-1];
		for (int i=2; i<=maxLevel; i++) {
			outStyleList[i-2]  = ""+i;
		}
		return outStyleList;
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		int maxLevel = getMaxLevel(ctx);
		String[] outStyleList = new String[maxLevel-1];
		for (int i=2; i<=maxLevel; i++) {
			outStyleList[i-2]  = "level-"+i;
		}
		return outStyleList;		
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
