/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.text;

import org.apache.commons.lang3.StringEscapeUtils;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.IImageTitle;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.*;
import org.javlo.module.file.FileAction;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.service.google.translation.ITranslator;
import org.javlo.utils.SuffixPrefix;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pvandermaesen
 */
public class WysiwygParagraph extends AbstractVisualComponent implements IImageTitle {

	public static final String TYPE = "wysiwyg-paragraph";

	private String title = null;

	private String subtitle = null;

	private String imageDescription;

	private String imageUrl;

	@Override
	protected String getEditorComplexity(ContentContext ctx) {
		return getConfig(ctx).getProperty("editor-complexity", "light");
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getDebugHeader(ctx));
		finalCode.append(getSpecialInputTag());
		finalCode.append("<textarea class=\"tinymce-light\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\"");
		finalCode.append(" rows=\"25\">");

		String hostPrefix = InfoBean.getCurrentInfoBean(ctx).getAbsoluteURLPrefix();
		finalCode.append(getValue().replace("${info.hostURLPrefix}", hostPrefix));
		finalCode.append("</textarea>");

		Map<String, String> filesParams = new HashMap<String, String>();
		String path = FileAction.getPathPrefix(ctx);
		filesParams.put("path", path);
		filesParams.put("webaction", "changeRenderer");
		filesParams.put("page", "meta");
		filesParams.put("select", "_TYPE_");
		filesParams.put(ContentContext.PREVIEW_EDIT_PARAM, "true");

		String chooseImageURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);

		String wysiwygCss = getConfig(ctx).getProperty("wysiwyg-css", null);
		String jsWysiwygCss = "var wysiwygCss=null;";
		if (wysiwygCss != null) {
			jsWysiwygCss = "var wysiwygCss='" + URLHelper.createStaticTemplateURL(ctx, wysiwygCss) + "';";
		}

		String fontsize = getConfig(ctx).getProperty("fontsize", null);
		String jsFontsize = "var fontsize=null;";
		if (fontsize != null) {
			jsFontsize = "var fontsize=" + fontsize + ";";
		}

		String format = getConfig(ctx).getProperty("format", null);
		String jsFormat = "var format=null;";
		if (format != null) {
			jsFormat = "var format=" + format + ";";
		}

		finalCode.append("<script type=\"text/javascript\">" + jsFormat + jsFontsize + jsWysiwygCss + "jQuery(document).ready(loadWysiwyg('#" + getContentName() + "','" + getEditorComplexity(ctx) + "','" + chooseImageURL + "', format, fontsize, wysiwygCss));loadWysiwyg('#" + getContentName() + "','" + getEditorComplexity(ctx) + "','" + chooseImageURL + "', format, fontsize, wysiwygCss)</script>");
		return finalCode.toString();
	}

	@Override
	public List<SuffixPrefix> getMarkerList(ContentContext ctx) {
		return null;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String text = XHTMLHelper.autoLink(XHTMLHelper.replaceLinks(ctx, XHTMLHelper.replaceJSTLData(ctx, getValue())), globalContext);
		ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
		text = reverserLinkService.replaceLink(ctx, this, text);
		ctx.getRequest().setAttribute("text", text);
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);
		return (String) ctx.getRequest().getAttribute("text");
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return getValue().trim().length() > 0;
	}

	@Override
	public boolean initContent(ContentContext ctx) {
		if (isEditOnCreate(ctx)) {
			return false;
		}
		setValue("<p>" + LoremIpsumGenerator.getParagraph(120, true, true) + "</p>");
		setModify();
		return true;
	}

	@Override
	protected void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		updateCache(ctx);
	}

	@Override
	public String getXHTMLTitle(ContentContext ctx) {
		return title;
	}

	public void updateCache(ContentContext ctx) {
		String xhtml = getValue();
		Document doc = Jsoup.parse(xhtml);
		Elements titles = doc.getElementsByTag("h1");
		this.title = null;
		if (titles.size() > 0) {
			this.title = titles.get(0).text();
		}
		this.subtitle = null;
		for (int i = 2; i <= 6 && this.subtitle == null; i++) {
			titles = doc.getElementsByTag("h" + i);
			if (titles.size() > 0) {
				this.subtitle = titles.get(0).text();
			}
		}

		Elements images = doc.getElementsByTag("img");
		if (images.size() > 0) {
			this.imageDescription = images.get(0).attr("alt");
			this.imageUrl = ResourceHelper.extractResourcePathFromURL(ctx, images.get(0).attr("src"));
		}
	}

	@Override
	public String getImageDescription(ContentContext ctx) {
		return imageDescription;
	}

	@Override
	public String getResourceURL(ContentContext ctx) {
		return imageUrl;
	}

	@Override
	public String getImageLinkURL(ContentContext ctx) {
		return null;
	}

	@Override
	public boolean isImageValid(ContentContext ctx) {
		return imageUrl != null;
	}

	@Override
	public int getPriority(ContentContext ctx) {
		return imageUrl != null ? 1 : -1;
	}

	@Override
	public int getLabelLevel(ContentContext ctx) {
		if (title != null) {
			return HIGH_LABEL_LEVEL;
		} else {
			return -1;
		}
	}

	@Override
	public String getTextTitle(ContentContext ctx) {
		return title;
	}

	/*
	 * @Override public String getPrefixViewXHTMLCode(ContentContext ctx) { if
	 * (ctx.isAsViewMode() && getValue().contains("</p>")) { return ""; } else {
	 * return super.getPrefixViewXHTMLCode(ctx); } }
	 * 
	 * @Override public String getSuffixViewXHTMLCode(ContentContext ctx) { if
	 * (ctx.isAsViewMode() && getValue().contains("</p>")) { return ""; } else {
	 * return super.getSuffixViewXHTMLCode(ctx); } }
	 */

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return !getValue().contains("${");
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		performColumnable(ctx);
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String newContent = requestService.getParameter(getContentName(), null);
		if (newContent != null) {
			if (!StringHelper.isTrue(getConfig(ctx).getProperty("html", null), false)) {
				newContent = XHTMLHelper.removeEscapeTag(newContent);
			}
			if (!getValue().equals(newContent)) {
				if (StringHelper.isTrue(getConfig(ctx).getProperty("clean-html", "false"))) {
					newContent = XHTMLHelper.cleanHTML(newContent);
				}
				newContent = XHTMLHelper.replaceAbsoluteLinks(ctx, newContent);
				setValue(newContent);
				setModify();
			}
		}
		updateCache(ctx);
		return null;
	}

	@Override
	protected boolean isXML() {
		return true;
	}

	/*@Override
	public String getFontAwesome() {
		return "align-left";
	}*/
	
	@Override
	public String getIcon() {
		return  "bi bi-body-text";
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
			String value = StringEscapeUtils.unescapeHtml4(getValue());
			String newValue = translator.translate(ctx, value, lang, ctx.getRequestContentLanguage());
			if (newValue == null) {
				translated = false;
				newValue = ITranslator.ERROR_PREFIX + getValue();
			}
			setValue(XHTMLHelper.removeEscapeTag(newValue));
			return translated;
		}
	}

	@Override
	protected boolean getColumnableDefaultValue() {
		return true;
	}

	@Override
	public boolean isMobileOnly(ContentContext ctx) {
		return false;
	}

}
