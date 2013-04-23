/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.text;

import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.LoremIpsumGenerator;
import org.javlo.helper.XHTMLHelper;
import org.javlo.service.ReverseLinkService;
import org.javlo.utils.SuffixPrefix;

/**
 * @author pvandermaesen
 */
public class Paragraph extends AbstractVisualComponent {

	public static final String TYPE = "paragraph";

	@Override
	public boolean isInline() {
		return true;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		String style = getStyle(ctx);
		if (style != null) {
			style = style + " ";
		} else {
			style = "";
		}
		return "<p" + getSpecialPreviewCssClass(ctx, style + getType()) + getSpecialPreviewCssId(ctx) + ">";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "</p>";
	}

	protected boolean isPrefixed() {
		return false;
	}

	protected String getContent(ContentContext ctx) {
		return getValue();
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		String content = getContent(ctx);
		content = applyReplacement(content);

		if (isPrefixed()) {
			int sepIndex = content.indexOf(":");
			if ((sepIndex >= 0) && (sepIndex < content.length())) {
				content = "<span class=\"prefix\">" + content.substring(0, sepIndex + 1) + "</span>" + content.substring(sepIndex + 1, content.length());
			}
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String value = XHTMLHelper.textToXHTML(content, getType(), globalContext);
		ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
		value = reverserLinkService.replaceLink(ctx, value);

		return value;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getHexColor() {
		return TEXT_COLOR;
	}

	@Override
	public List<SuffixPrefix> getMarkerList(ContentContext ctx) {
		return getItalicAndStrongLanguageMarkerList(ctx);
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return !isEmpty(ctx);
	}

	@Override
	public void initContent(ContentContext ctx) {
		setValue(LoremIpsumGenerator.getParagraph(80, false, true));
		setModify();
	}

}
