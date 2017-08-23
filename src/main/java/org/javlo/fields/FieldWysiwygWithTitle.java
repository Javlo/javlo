package org.javlo.fields;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.javlo.context.ContentContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.module.file.FileAction;
import org.javlo.service.RequestService;
import org.owasp.encoder.Encode;

public class FieldWysiwygWithTitle extends FieldWysiwyg {
	
	protected int getTitleDepth(ContentContext ctx) throws Exception {	
		return Integer.parseInt(getMetaData("title-depth", "3"));
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
	
	public String getInputNameText() {
		if (getCurrentLocale() != null) {
			return getName() + "-text-" + getId() + '-' + getCurrentLocale().getLanguage();
		} else {
			return getName() + "-text-" + getId();
		}
	}
	
	public String getInputNameTitle() {
		if (getCurrentLocale() != null) {
			return getName() + "-title-" + getId() + '-' + getCurrentLocale().getLanguage();
		} else {
			return getName() + "-title-" + getId();
		}
	}
	
	public String getTitle() {
		Collection<String> values = StringHelper.stringToCollection(getValue());	
		if (values != null && values.size()>1) {
			return values.iterator().next();
		} else {
			return "";
		}
	}
	
	@Override
	public String getText() {
		List<String> values = StringHelper.stringToCollection(getValue());
		if (values != null && values.size()>1) {
			return values.get(1);
		} else {
			return "";
		}
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
		out.println("<label for=\""+getInputNameText()+"\"><strong>"+getLabel(ctx, new Locale(ctx.getGlobalContext().getEditLanguage(ctx.getRequest().getSession())))+"</strong></label>");
		out.println("<div class=\"form-group " + getType() + "\">");
		out.println("<label for=\""+getInputNameTitle()+"\">title</label>");
		out.print("<input class=\"form-control\" type=\"text\" id=\"" + getInputNameTitle() + "\" name=\"" + getInputNameTitle() + "\" value=\"" + Encode.forHtmlAttribute(getTitle()) + "\" />");
		out.println("</div>");
		out.println("<div class=\"form-group " + getType() + "\">");		
		out.print("<textarea class=\"tinymce-light wysiwyg form-control\" id=\"" + getInputNameText() + "\" name=\"" + getInputNameText() + "\"");
		out.print(" rows=\"" + 10 + "\">");

		String hostPrefix;
		try {
			hostPrefix = InfoBean.getCurrentInfoBean(ctx).getAbsoluteURLPrefix();
			out.print(StringHelper.neverNull(getText()).replace("${info.hostURLPrefix}", hostPrefix));
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
			out.println("<script type=\"text/javascript\">jQuery(document).ready(loadWysiwyg('#" + getInputNameText() + "','" + getEditorComplexity(ctx) + "','" + chooseImageURL + "'));</script>");
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return writer.toString();
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		String title = getTitle();
		if (!StringHelper.isEmpty(title)) {
			out.println("<h"+(getTitleDepth(ctx)+1)+">"+Encode.forHtml(title)+"</h"+(getTitleDepth(ctx)+1)+">");
		}
		out.println(getText());
		out.close();
		return new String(outStream.toByteArray());		
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
	public String getType() {
		return "wysiwyg-text-title";
	}
	
	public boolean process(HttpServletRequest request) {
		RequestService requestService = RequestService.getInstance(request);		
		String title = requestService.getParameter(getInputNameTitle(), null);
		String text = requestService.getParameter(getInputNameText(), null);
		String value = StringHelper.collectionToString(title,text);
		boolean modify = false;		
		if (value != null) {			
			if (!value.equals(getValue())) {
				setValue(value);
				if (!validate()) {
					setNeedRefresh(true);
				}
				modify = true;
			}
		} else {
			setValue("");
		}
		return modify;
	}

}
