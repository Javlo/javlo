/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.title;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentLayout;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.LoremIpsumGenerator;
import org.javlo.service.ReverseLinkService;

/**
 * @author pvandermaesen
 */
public class Title extends AbstractVisualComponent {

	public static final String TYPE = "title";

	private static final String[] STYLES = new String[] { "important", "standard", HIDDEN };

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_HIGH;
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return STYLES;
	}
	
	@Override
	protected void init() throws ResourceNotFoundException {	
		super.init();
		if (getLayout() == null) {
			getComponentBean().setLayout(new ComponentLayout(""));
		}
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		/* editable if hidden */
		if (HIDDEN.equals(getStyle())) {
			if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && EditContext.getInstance(GlobalContext.getInstance(ctx.getRequest()), ctx.getRequest().getSession()).isEditPreview()) {				
				return super.getPrefixViewXHTMLCode(ctx);
			}
		}
		return "";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		if (HIDDEN.equals(getStyle())) {
			if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && EditContext.getInstance(GlobalContext.getInstance(ctx.getRequest()), ctx.getRequest().getSession()).isEditPreview()) {
				return super.getSuffixViewXHTMLCode(ctx);
			}
		}
		return "";
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		String value = getValue();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
		value = reverserLinkService.replaceLink(ctx, this, value);
		ctx.getRequest().setAttribute("value", value);
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (getStyle().equals(HIDDEN)) {
			return "";
		}
		StringBuffer res = new StringBuffer();
		String style="";
		if (getLayout() != null && getLayout().getLayout().length() > 0) {
			style = " style=\""+getLayout().getStyle()+'"';
		}
		res.append("<h1"+style+" " + getSpecialPreviewCssClass(ctx, getStyle(ctx)) + getSpecialPreviewCssId(ctx) + "><span>");

		String value = getValue();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
		value = reverserLinkService.replaceLink(ctx, this, value);

		res.append(value);
		res.append("</span></h1>");
		return res.toString();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isLabel(ContentContext ctx) {
		return true;
	}

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	public String getHelpURI(ContentContext ctx) {
		return "/components/title.html";
	}

	@Override
	public int getTitleLevel(ContentContext ctx) {
		return 1;
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return true; /* page with only a title is never pertinent */
	}

	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return super.isEmpty(ctx); // this component is never not empty -> use
									// empty parent method
	}

	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	public boolean initContent(ContentContext ctx) {
		setValue(LoremIpsumGenerator.getParagraph(4, true, true));
		setModify();
		return true;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

}
