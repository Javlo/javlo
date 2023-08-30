package org.javlo.fields;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.module.file.FileAction;
import org.javlo.service.google.translation.ITranslator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FieldWysiwyg extends Field {

	protected String getEditorComplexity(ContentContext ctx) throws Exception {
		return getMetaData("editor-complexity", "high");
	}

	protected int getMaxParagraph() {
		String maxP = getMetaData("max-paragraph", null);
		if (StringHelper.isDigit(maxP)) {
			return Integer.parseInt(maxP);
		} else {
			return Integer.MAX_VALUE;
		}
	}

	@Override
	public String getValue() {
		String value = super.getValue();
		if (value == null) {
			return null;
		} else {
			while (value.contains("\\,")) {
				value = value.replace("\\,", ",");
			}
		}
		setValue(value);
		return value;
	}
	
	public String getText() {
		return getValue();
	}

	@Override
	public boolean validate() {
		if (getMaxParagraph() < Integer.MAX_VALUE) {
			if (StringUtils.countMatches(StringHelper.neverNull(getText()).toLowerCase(), "</p") > getMaxParagraph()) {
				setMessage(i18nAccess.getText("content.dynamic-component.error.max-paragraph") + getMaxParagraph());				
				setMessageType(Field.MESSAGE_ERROR);
				return false;				
			}
		}
		return super.validate();
	}
	
	public String getSearchEditXHTMLCode(ContentContext ctx) throws Exception {
		return super.getEditXHTMLCode(ctx, true);
	}

	@Override
	public String getEditXHTMLCode(ContentContext ctx, boolean search) {
		try {
			String refCode = referenceEditCode(ctx);
			if (refCode != null) {
				return refCode;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<div class=\"form-group " + getType() + "\">");
		out.println("<label for=\"" + getInputName() + "\"><strong>" + getLabel(ctx, new Locale(ctx.getGlobalContext().getEditLanguage(ctx.getRequest().getSession()))) + "</strong></label>");
		if (getMessage() != null && getMessage().trim().length() > 0) {
			out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
		}
		out.print("<textarea class=\"tinymce-light wysiwyg form-control\" id=\"" + getInputName() + "\" name=\"" + getInputName() + "\"");
		out.print(" rows=\"" + 10 + "\">");

		String hostPrefix;
		try {
			hostPrefix = InfoBean.getCurrentInfoBean(ctx).getAbsoluteURLPrefix();
			out.print(StringHelper.neverNull(getValue()).replace("${info.hostURLPrefix}", hostPrefix));
			out.println("</textarea>");
			out.println("</div>");
			Map<String, String> filesParams = new HashMap<String, String>();
			String path = FileAction.getPathPrefix(ctx);
			filesParams.put("path", path);
			filesParams.put("webaction", "changeRenderer");
			filesParams.put("page", "meta");
			filesParams.put("select", "_TYPE_");
			filesParams.put(ContentContext.PREVIEW_EDIT_PARAM, "true");
			String chooseImageURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);
			out.println("<script type=\"text/javascript\">jQuery(document).ready(loadWysiwyg('#" + getInputName() + "','" + getEditorComplexity(ctx) + "','" + chooseImageURL + "'));</script>");			
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return writer.toString();
	}

	/**
	 * return the value "displayable"
	 * 
	 * @param locale
	 * @return
	 */
	@Override
	public String getDisplayValue(ContentContext ctx, Locale locale) throws Exception {
		String refCode = referenceViewCode(ctx);
		if (refCode != null) {
			return refCode;
		}
		return super.getDisplayValue(ctx, locale);
	}
	
	@Override
	public String getXHTMLValue() {
		return getValue();
	}

	@Override
	public String getType() {
		return "wysiwyg-text";
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
