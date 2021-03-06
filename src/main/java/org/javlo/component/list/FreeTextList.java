/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.list;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.LoremIpsumGenerator;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.text.PrefixedText;
import org.javlo.utils.SuffixPrefix;

/**
 * @author pvandermaesen
 */
public class FreeTextList extends AbstractVisualComponent {
	
	public static final char SUBLIST_CHAR = '-';

	public static final String NUMBER_LIST = "ol-ol";

	public static final String TYPE = "text-list";

	@Override
	public String getStyleTitle(ContentContext ctx) {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return i18nAccess.getText("content.text-list.style-title");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "style";
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "ul-ul", NUMBER_LIST, "ul-ol", "ol-ul" };
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {

		String ulul = "unsorted list";
		String olol = "cols title";
		String lilo = "rows title";
		String olli = "no title";

		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			ulul = i18nAccess.getText("content.text-list.ulul");
			olol = i18nAccess.getText("content.text-list.olol");
			lilo = i18nAccess.getText("content.text-list.lilo");
			olli = i18nAccess.getText("content.text-list.olli");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new String[] { ulul, olol, lilo, olli };
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		if (getRenderer(ctx) != null) {
			return super.getPrefixViewXHTMLCode(ctx);
		} else {
			return "";
		}
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		if (getRenderer(ctx) != null) {
			return super.getSuffixViewXHTMLCode(ctx);
		} else {
			return "";
		}
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		if (getRenderer(ctx) != null) {
			ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(ctx.getGlobalContext());
			String value = XHTMLHelper.autoLink(getValue());
			value = reverserLinkService.replaceLink(ctx, this, value);
			String sep = getSeparatorValue();
			if (value.length() > 3) {
				if (value.startsWith("{")) {
					sep = "" + value.charAt(1);
					value = value.substring(3);
				}
			}			
			BufferedReader read = new BufferedReader(new StringReader(value));
			String line = read.readLine();
			List<PrefixedText> lines = new LinkedList<PrefixedText>();
			while (line != null) {				
				if ((sep != null) && sep.length() > 0) {
					int sepIndex = line.indexOf(sep);
					if (sepIndex >= 0) {
						String prefix = line.substring(0, sepIndex + 1);
						prefix = prefix.replace(sep, "<span class=\"sep\">"+sep+"</span>");
						lines.add(new PrefixedText(prefix, line.substring(sepIndex + 1), null,SUBLIST_CHAR));
					} else {
						lines.add(new PrefixedText(null, line, null,SUBLIST_CHAR));
					}
				} else {
					lines.add(new PrefixedText(null, line, null,SUBLIST_CHAR));	
				}				
				line = read.readLine();
			}
			ctx.getRequest().setAttribute("lines", lines);
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
		String value = reverserLinkService.replaceLink(ctx, this, getValue());
		String sep = "";
		if (value.length() > 3) {
			if (value.startsWith("{")) {
				sep = "" + value.charAt(1);
				value = value.substring(3);
			}
		}
		value = applyReplacement(value);
		value = StringHelper.textToList(globalContext, value, sep, getComponentCssClass(ctx), true);

		return "<div " + getPrefixCssClass(ctx, getComponentCssClass(ctx) + ' ' + getType()) + getSpecialPreviewCssId(ctx) + ">" + value + "</div>";
	}

	@Override
	public boolean isInline() {
		return false;
	}

	public String getSeparatorInputName() {
		return "separator-" + getId();
	}

	public String getSeparatorValue() {
		String value = getValue();
		if (value != null) {
			if (value.length() >= 3) {
				if (value.startsWith("{")) {
					return "" + value.charAt(1);
				}
			}
		}
		return "";
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		finalCode.append(getSpecialInputTag());
		finalCode.append("<div class=\"form-group\"><div class=\"row\"><div class=\"col-sm-2\">");
		finalCode.append("<label for=\"" + getSeparatorInputName() + "\">" + i18nAccess.getText("content.text-list.separator") + " :</label></div><div class=\"col-sm-3\">");
		finalCode.append("<input class=\"form-control\" id=\"" + getSeparatorInputName() + "\" name=\"" + getSeparatorInputName() + "\" value=\"" + getSeparatorValue() + "\" />");
		finalCode.append("</div></div></div>");
		finalCode.append("<textarea class=\"form-control\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\"");
		finalCode.append(" rows=\"" + (Math.max(countLine(),4) + 1) + "\">");
		String value = getValue();
		if (getSeparatorValue().length() > 0) {
			value = value.substring(3);
		}
		finalCode.append(value);
		finalCode.append("</textarea>");

		return finalCode.toString();
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String newContent = requestService.getParameter(getContentName(), null);
		String sepValue = requestService.getParameter(getSeparatorInputName(), "");
		if (newContent != null) {
			if (sepValue.length() > 0) {
				newContent = '{' + sepValue + '}' + newContent;
			}
			if (!getComponentBean().getValue().equals(newContent)) {
				getComponentBean().setValue(newContent);
				setModify();
			}
		}
		return null;
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
	public boolean initContent(ContentContext ctx) {		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println(LoremIpsumGenerator.getParagraph(8, true, false) + '.');
		for (int i = 0; i < 4; i++) {
			out.println(LoremIpsumGenerator.getParagraph(8, false, false) + '.');
		}
		out.close();
		setValue(new String(outStream.toByteArray()));
		return true;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return getValue() != null && getValue().length() > 0;
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}
	
	@Override
	public String getFontAwesome() {	
		return "list-ul";
	}

}
