package org.javlo.component.title;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringEscapeUtils;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentLayout;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.ISubTitle;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.LoremIpsumGenerator;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.ReverseLinkService;
import org.javlo.service.exception.ServiceException;
import org.owasp.encoder.Encode;

public class Heading extends AbstractPropertiesComponent implements ISubTitle {

	public static final String TYPE = "heading";

	private static final String DEPTH = "depth";
	public static final String TEXT = "text";
	public static final String SMALL_TEXT = "smtext";
	private static final String LINK = "link";
	private static final List<String> FIELDS = new LinkedList<String>(Arrays.asList(new String[] { DEPTH, TEXT, SMALL_TEXT, LINK }));

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("title", getTitle(ctx));
		ctx.setTitleDepth(getDepth(ctx));
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		
		String depthHTML = "<div class=\"form-group\"><label>" + i18nAccess.getText("content.heading.depth", "depth") + "</label><div>";
		int depth = getDepth(ctx);
		for (int i = 1; i < 7; i++) {
			depthHTML = depthHTML+"<label class=\"radio-inline\">";
			depthHTML = depthHTML+"<input type=\"radio\" value=\"" + i + "\" name=\"" + getInputName(DEPTH) + "\"" + (depth == i ? " checked=\"checked\"" : "") + "/>" + i + "</label>";
		}
		depthHTML = depthHTML+"</select></div></div>";
		
		if (!ctx.getGlobalContext().isMailingPlatform()) {
			out.println("<div class=\"row\"><div class=\"col-sm-8\">");
		} else {
			out.println(depthHTML);
		}
		out.println("<div class=\"form-group\">");
		out.println("<label for=\"" + getInputName(TEXT) + "\">" + i18nAccess.getText("content.header.text", "text") + "</label>");
		if (ctx.getGlobalContext().isMailingPlatform()) {
			out.println("<input class=\"form-control\" type=\"text\" id=\"" + createKeyWithField(TEXT) + "\" name=\"" + createKeyWithField(TEXT) + "\" value=\"" + Encode.forHtmlAttribute(getFieldValue(TEXT)) + "\" >");
		} else {
			out.println("<textarea rows=\"1\" class=\"form-control\" id=\"" + createKeyWithField(TEXT) + "\" name=\"" + createKeyWithField(TEXT) + "\">"+Encode.forHtmlContent(getFieldValue(TEXT))+"</textarea>");
			out.println("<script type=\"text/javascript\">jQuery(document).ready(loadWysiwyg('#" + createKeyWithField(TEXT) + "','soft',''));</script>");
		}
		out.println("</div>");

		if (!ctx.getGlobalContext().isMailingPlatform()) {
			out.println("</div><div class=\"col-sm-4\"><div class=\"form-group\">");
			out.println(depthHTML);
			out.println("<label for=\"" + getInputName(SMALL_TEXT) + "\">" + i18nAccess.getText("content.header.smtext", "small text") + "</label>");
			out.println("<input class=\"form-control\" type=\"text\" id=\"" + createKeyWithField(SMALL_TEXT) + "\" name=\"" + createKeyWithField(SMALL_TEXT) + "\" value=\"" + getFieldValue(SMALL_TEXT) + "\" >");
			out.println("</div>");
		}
		
		out.println("<div class=\"form-group\">");
		out.println("<label for=\"" + getInputName(LINK) + "\">" + i18nAccess.getText("content.header.link", "link") + "</label>");
		out.println("<input class=\"form-control\" type=\"text\" id=\"" + createKeyWithField(LINK) + "\" name=\"" + createKeyWithField(LINK) + "\" value=\"" + getFieldValue(LINK) + "\" >");		
		out.println("</div>");
		if (!ctx.getGlobalContext().isMailingPlatform()) {
			out.println("</div></div>");
		}

		out.close();
		return new String(outStream.toByteArray());
	}

	public int getDepth(ContentContext ctx) {
		String depthValue = getFieldValue(DEPTH);
		if (depthValue == null || depthValue.length() != 1) {
			if (ctx != null) {
				try {
					Collection<IContentVisualComponent> comps = null;
					if (ctx.getCurrentPage() != null) {
						comps = ctx.getCurrentPage().getContentByType(ctx, getType());
					}
					if (comps != null) {
						for (IContentVisualComponent comp : comps) {
							if (((Heading) comp).getFieldValue(DEPTH).equals("1")) {
								setFieldValue(DEPTH, "2");
								return 2;
							}
						}
					}
					setFieldValue(DEPTH, "1");
					return 1;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return 0;
		} else {
			switch (depthValue.charAt(0)) {
			case '1':
				return 1;
			case '2':
				return 2;
			case '3':
				return 3;
			case '4':
				return 4;
			case '5':
				return 5;
			case '6':
				return 6;
			default:
				return 0;
			}
		}
	}

	@Override
	public String getTextTitle(ContentContext ctx) {
		try {
			// return XHTMLHelper.replaceJSTLData(ctx,StringHelper.removeTag(getFieldValue(TEXT))); // warning recursive call of title
			return StringHelper.removeTag(getFieldValue(TEXT));
		} catch (Exception e) {
			e.printStackTrace();
			return StringHelper.removeTag(getFieldValue(TEXT));
		}
	}
	
	@Override
	public String getTextLabel(ContentContext ctx) {
		String smText = getFieldValue(SMALL_TEXT);
		smText = StringEscapeUtils.unescapeHtml4(smText);		
		if (StringHelper.isEmpty(smText)) {	
			String lgText = getFieldValue(TEXT);
			lgText = StringEscapeUtils.unescapeHtml4(lgText);
			return StringHelper.removeTag(lgText);
		} else {
			return smText;
		}
	}	

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		String link = getFieldValue(LINK);
		String target = "";
		if (link != null && link.trim().length() > 0) {
			if (ctx.getGlobalContext().isOpenExternalLinkAsPopup(link)) {
				target = " target=\"_blank\"";
			}
			if (!link.contains("://")) {
				if (link.contains(".")) {
					link = "http://" + link;
				} else {
					ContentService content = ContentService.getInstance(ctx.getRequest());
					MenuElement targetPage = content.getNavigation(ctx).searchChildFromName(link);
					if (targetPage != null) {
						link = URLHelper.createURL(ctx, targetPage);
					}
				}
			}
		} else {
			link = null;
		}

		String style = "";
		if (link != null) {
			if (getBackgroundColor() != null && getBackgroundColor().length() > 2 && ctx.getGlobalContext().isMailing()) {
				style = "border: 1px " + getBackgroundColor() + " solid; ";
			}
			if (getTextColor() != null && getTextColor().length() > 2) {
				style = style + "color:" + getTextColor() + ";";
			}
			if (style.length() > 0) {
				style = " style=\"" + style + "\"";
			}
			return "<a" + style + " href=\"" + link + "\"" + target + ">" + getTitle(ctx) + "</a>";
		} else {
			if (getBackgroundColor() != null && getBackgroundColor().length() > 2 && ctx.getGlobalContext().isMailing()) {
				style = "border: 1px " + getBackgroundColor() + " solid; ";
			}
			if (getTextColor() != null && getTextColor().length() > 2) {
				style = style + "color:" + getTextColor() + ";";
			}
			if (getBackgroundColor() != null && getBackgroundColor().length() > 2) {
				if (ctx.getGlobalContext().isMailing()) {
					style = " style=\"border: 1px " + getBackgroundColor() + " solid; "+style+"\"";
				}
			} else if (style.trim().length() > 0) {
				style=" style=\""+style+"\"";
			}
			String tag = "span";
			if (ctx.getGlobalContext().isMailingPlatform()) {
				tag = "div";
			}
			return "<" + tag + " class=\"inside-wrapper\"" + style + ">" + XHTMLHelper.replaceJSTLData(ctx, cleanValue(ctx, getTitle(ctx))) + "</" + tag + ">";
		}
	}
	
	protected String getTitle(ContentContext ctx) throws ServiceException, Exception {
		String html = getFieldValue(TEXT);
		if (!isNolink()) {			
			html = XHTMLHelper.autoLink(html, ctx.getGlobalContext());
			html = ReverseLinkService.getInstance(ctx.getGlobalContext()).replaceLink(ctx, this, html);
		}
		return html;
	}

	@Override
	protected String getTag(ContentContext ctx) {
		int depth = getDepth(ctx);
		if (depth == 0) {
			return "div";
		} else {
			return "h" + depth;
		}
	}

	@Override
	public boolean isListable() {
		return false;
	}

	@Override
	public int getSearchLevel() {
		if (getDepth(null) == 1) {
			return SEARCH_LEVEL_HIGH;
		} else {
			return SEARCH_LEVEL_MIDDLE;
		}
	}

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	protected void init() throws ResourceNotFoundException {
		super.init();
		if (getLayout() == null) {
			getComponentBean().setLayout(new ComponentLayout(""));
		}
	}
	
	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
//		if ((StringHelper.isEmpty(getFieldValue(TEXT))) && !StringHelper.isEmpty(getFieldValue(SMALL_TEXT))) {			
//			setFieldValue(TEXT, getFieldValue(SMALL_TEXT));
//			setFieldValue(SMALL_TEXT, "");			
//		}
	}

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		setFieldValue(TEXT, LoremIpsumGenerator.getParagraph(4, false, true));		
		setModify();
		storeProperties();
		return true;
	}

	@Override
	public String getSubTitle(ContentContext ctx) {
		if (getDepth(ctx) != 1) {
			return getTextTitle(ctx);
		}
		return null;
	}

	@Override
	public int getSubTitleLevel(ContentContext ctx) {
		return getDepth(ctx);
	}

	@Override
	public int getLabelLevel(ContentContext ctx) {
		if (getDepth(ctx) == 1) {
			return HIGH_LABEL_LEVEL;
		} else if (getDepth(ctx) > 1) {
			return MIDDLE_LABEL_LEVEL - getDepth(ctx);
		} else {
			return 0;
		}
	}

	public String getXHTMLId(ContentContext ctx) {
		final String suffix = "_st_";
		if (ctx.getRequest().getAttribute(suffix + getId()) != null) {
			return (String) ctx.getRequest().getAttribute(suffix + getId());
		}
		String htmlID = StringHelper.createFileName(getTextTitle(ctx)).replace('-', '_').replace('.', '_');
		if (htmlID.trim().length() == 0) {
			htmlID = "empty";
		}
		htmlID = "H_" + htmlID;
		while (ctx.getRequest().getAttribute(suffix + htmlID) != null) {
			htmlID = htmlID + "_bis";
		}
		ctx.getRequest().setAttribute(suffix + htmlID, "");
		ctx.getRequest().setAttribute(suffix + getId(), htmlID);
		return htmlID;
	}

	@Override
	protected String getInlineStyle(ContentContext ctx) {
		String inlineStyle = "";
		if (getBackgroundColor() != null && getBackgroundColor().length() > 2) {
			if (ctx.getGlobalContext().isMailing()) {
				inlineStyle = " overflow: hidden; border: 1px " + getBackgroundColor() + " solid; background-color: " + getBackgroundColor() + ';';
			} else {
				inlineStyle = " overflow: hidden; background-color: " + getBackgroundColor() + ';';
			}
		}
		if (getTextColor() != null && getTextColor().length() > 2) {
			inlineStyle = inlineStyle + " color: " + getTextColor() + ';';
		}
		if (getLayout() != null) {
			inlineStyle = inlineStyle + ' ' + getLayout().getStyle();
		}
		inlineStyle = inlineStyle.trim();
		if (inlineStyle.length() > 0) {
			inlineStyle = " style=\"" + inlineStyle + "\"";
		}
		return inlineStyle;
	}
	
	@Override
	public String getXHTMLConfig(ContentContext ctx) throws Exception {
		String xhtml = super.getXHTMLConfig(ctx);
		return xhtml;
	}
	
	public String getSpecialPreviewCssId(ContentContext ctx) {
		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			return " id=\"cp_" + getForcedId(ctx) + "\"";
		} else {
			return " id=\"" + getXHTMLId(ctx) + "\"";
		}
	}
	
	@Override
	protected boolean isHiddable() {
		return true;
	}
	
	protected String getForcedPrefixViewXHTMLCode(ContentContext ctx) {
		if (isDisplayHidden() && ctx.isAsViewMode()) {
			return "";
		}
		if (getConfig(ctx).getProperty("prefix", null) != null) {
			return getConfig(ctx).getProperty("prefix", null);
		}
		String style = contructViewStyle(ctx);		
		return "<" + getTag(ctx) + " " + getPrefixCssClass(ctx, style) + getSpecialPreviewCssId(ctx) + " " + getInlineStyle(ctx) + ">";		
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		String colSuffix = getColomnableSuffix(ctx);
		if (isDisplayHidden() && ctx.isAsViewMode()) {
			return "";
		}
		if (isWrapped(ctx)) {
			return "</" + getTag(ctx) + '>'+colSuffix;
		} else {
			return colSuffix;
		}
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
	}
	
	@Override
	public boolean isNoLinkable() {
		return true;
	}	
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return !getValue().contains("${");	
	}
	
	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		performColumnable(ctx);
		String outPerform = super.performEdit(ctx);
		if (!ctx.getGlobalContext().isMailingPlatform()) {
			setFieldValue(TEXT, XHTMLHelper.removeTag(getFieldValue(TEXT, "" ),"p"));
		}
		return outPerform;
		
	}
	
	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return getFieldValue(TEXT, "" ).trim().length() == 0;
	}
	
	@Override
	public String getFontAwesome() {	
		return "header";
	}
	
	public static void main(String[] args) {
		Properties p = new Properties();
		String value = "mes articles d'été";
		p.setProperty("text", value);
		System.out.println("value = p.get ? "+value.equals(p.getProperty("text")));
	}
	
	@Override
	protected boolean isValueTranslatable() {
		return true;
	}
	
	@Override
	protected boolean getColumnableDefaultValue() {
		return true;
	}

}

