package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.module.file.FileAction;

public class FieldWysiwyg extends Field {

	protected String getEditorComplexity(ContentContext ctx) throws Exception {
		return properties.getProperty("editor-complexity", "light");
	}
	
	@Override
	public String getValue() {	
		String value = super.getValue();
		while (value.contains("\\,")) {
			value = value.replace("\\,", ",");
		}
		setValue(value);
		return value;
	}
	
	@Override
	public String getEditXHTMLCode(ContentContext ctx) {

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
		out.print("<textarea class=\"tinymce-light wysiwyg\" id=\"" + getInputName() + "\" name=\"" + getInputName() + "\"");
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
			// TODO Auto-generated catch block
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
		return XHTMLHelper.textToXHTML(super.getDisplayValue(ctx, locale));
	}

	@Override
	public String getType() {
		return "wysiwyg-text";
	}

}
