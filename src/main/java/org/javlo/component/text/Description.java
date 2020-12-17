/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.text;

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.LoremIpsumGenerator;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.ReverseLinkService;
import org.javlo.service.google.translation.ITranslator;
import org.javlo.utils.SuffixPrefix;

/**
 * @author pvandermaesen
 */
public class Description extends AbstractVisualComponent {

	public static final String TYPE = "description";

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_MIDDLE;
	}

	@Override
	public List<SuffixPrefix> getMarkerList(ContentContext ctx) {
		return getItalicAndStrongLanguageMarkerList(ctx);
	}

	@Override
	public String getTag(ContentContext ctx) {
		if (!isNotDisplayHTML(ctx)) {
			return "p";
		} else {
			return "span";
		}
	}

	@Override
	protected String getEditorComplexity(ContentContext ctx) {
		return getConfig(ctx).getProperty("editor-complexity", "soft");
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		String[] styles = super.getStyleLabelList(ctx);
		if (styles != null && styles.length > 0) {
			return styles;
		} else {
			String visible = "visible";
			String hidden = "hidden";
			try {
				I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
				visible = i18n.getText("global.visible");
				hidden = i18n.getText("global.hidden");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new String[] { visible, hidden };
		}
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		String[] styles = super.getStyleList(ctx);	
		if (styles != null && styles.length > 0) {
			return styles;
		} else {
			return new String[] { "visible", "hidden" };
		}
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "position";
	}

	/*
	 * @Override public boolean isUnique() { return true; }
	 */

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		StringBuffer finalCode = new StringBuffer();
		String content = applyReplacement(getValue());
		if (!isNotDisplayHTML(ctx)) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			content = XHTMLHelper.replaceJSTLData(ctx, content);
			content = XHTMLHelper.textToXHTML(content, globalContext);
			ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
			content = reverserLinkService.replaceLink(ctx, this, content);
			finalCode.append(content);
		}
		ctx.getRequest().setAttribute("xhtml", finalCode.toString());
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);
		return "" + ctx.getRequest().getAttribute("xhtml");
	}

	private boolean isNotDisplayHTML(ContentContext ctx) {
		return StringHelper.neverNull(getStyle()).equals("hidden");
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return !isNotDisplayHTML(ctx) && getValue().length() > 0;
	}

	@Override
	public boolean initContent(ContentContext ctx) {
		setValue(LoremIpsumGenerator.getParagraph(24, true, true));
		setModify();
		return true;
	}

	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public String getPageDescription(ContentContext ctx) {
		return getValue(ctx);
		/*
		 * try {
		 * ctx.getRequest().setAttribute(MenuElement.FAKE_DESCRIPTION+getPage().getId(),
		 * getValue()); return XHTMLHelper.replaceJSTLData(ctx, getValue(ctx)); } catch
		 * (Exception e) { e.printStackTrace(); return getValue(); }
		 */
	}

	@Override
	public String getFontAwesome() {
		return "sticky-note";
	}
	
	@Override
	protected boolean isValueTranslatable() {
		return true;
	}
	
	@Override
	public boolean transflateFrom(ContentContext ctx, ITranslator translator, String lang) {
		if (!isValueTranslatable()) {
			return false;
		} else {
			boolean translated = true;
			String value =  StringEscapeUtils.unescapeHtml4(getValue());
			String newValue = translator.translate(ctx, value, lang, ctx.getRequestContentLanguage());
			if (newValue == null) {
				translated=false;
				newValue = ITranslator.ERROR_PREFIX+getValue();
			}
			setValue(XHTMLHelper.removeEscapeTag(newValue));
			return translated;
		}
	}

}
