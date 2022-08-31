package org.javlo.fields;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;

public class FieldNumber extends Field {

	private static Logger logger = Logger.getLogger(FieldNumber.class.getName());

	@Override
	public String getType() {
		return "number";
	}

	public int getMin(ContentContext ctx) {
		return Integer.parseInt(properties.getProperty("field." + getUnicName() + ".min", "" + Integer.MIN_VALUE));
	}

	public int getMax(ContentContext ctx) {
		return Integer.parseInt(properties.getProperty("field." + getUnicName() + ".max", "" + Integer.MAX_VALUE));
	}

	@Override
	public boolean validate() {
		boolean superValidation = super.validate();
		if (superValidation) {
			if (!StringHelper.isEmpty(getValue()) && !StringHelper.isFloat(getValue())) {
				setMessage(i18nAccess.getText("global.error"));
				setMessageType(Field.MESSAGE_ERROR);
				return false;
			}
		}
		return superValidation;
	}

	public String getFromName(ContentContext ctx) throws Exception {
		return "from_" + getUnicName();
	}

	public String getToName(ContentContext ctx) throws Exception {
		return "to_" + getUnicName();
	}

	public boolean isSearchAsRange() {
		String key = createKey("search.range");
		return StringHelper.isTrue(properties.getProperty(key));
	}

	@Override
	public String getSearchEditXHTMLCode(ContentContext ctx) throws Exception {
		if (isSearchAsRange()) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("<div class=\"row\"><div class=\"col-sm-4 align-middle\">");
			String unity = "";
			if (!StringHelper.isEmpty(getUnity(ctx))) {
				unity = "<span class=\"\">(" + getUnity(ctx) + ")</span>";
			}
			out.println("	<label class=\"col-form-label\" for=\"" + getInputName() + "\">" + getSearchLabel(ctx, new Locale(ctx.getContextRequestLanguage())) + " " + unity + " : </label>");
			out.println("</div><div class=\"col-sm-8\"><input type=\"hidden\" name=\"" + getInputName() + "\" value=\"1\" />");
			RequestService rs = RequestService.getInstance(ctx.getRequest());
			out.println("<div class=\"form-group form-inline-2 text-right\">");
			out.println("<label for=\"" + getFromName(ctx) + "\" class=\"form-label text-left\">" + i18nAccess.getViewText("global.from") + "</label>");
			out.println("<input type=\"number\" step=\""+getStep(ctx)+"\" min=\"" + getMin(ctx) + "\" max=\"" + getMax(ctx) + "\" class=\"form-control\" id=\"" + getFromName(ctx) + "\" name=\"" + getFromName(ctx) + "\" value=\"" + rs.getParameter(getFromName(ctx), "" + getMin(ctx)) + "\">");
			out.println("<label for=\"" + getToName(ctx) + "\" class=\"col-sm-4 col-form-label text-left\">" + i18nAccess.getViewText("global.to") + "</label>");
			out.println("<input type=\"number\" step=\""+getStep(ctx)+"\" min=\"" + getMin(ctx) + "\" max=\"" + getMax(ctx) + "\" class=\"form-control\" id=\"" + getToName(ctx) + "\" name=\"" + getToName(ctx) + "\" value=\"" + rs.getParameter(getToName(ctx), "" + getMax(ctx)) + "\">");
			out.println("</div>");
			out.println("</div></div>");
			out.close();
			return new String(outStream.toByteArray());
		} else {
			return super.getSearchEditXHTMLCode(ctx);
		}
	}

	public boolean searchAsRange(ContentContext ctx, String query) {
		if (StringHelper.isEmpty(query) || StringHelper.isEmpty(getValue())) {
			return true;
		} else {
			RequestService rs = RequestService.getInstance(ctx.getRequest());
			if (!StringHelper.isDigit(getValue())) {
				return false;
			}
			int val = Integer.parseInt(getValue());
			try {
				if (val >= Integer.parseInt(rs.getParameter(getFromName(ctx), "" + getMin(ctx))) && val <= Integer.parseInt(rs.getParameter(getToName(ctx), "" + getMax(ctx)))) {
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	
	public String getStep(ContentContext ctx) {
		return properties.getProperty("field." + getUnicName() + ".step", "1");
	}


	public String getEditXHTMLCode(ContentContext ctx, boolean search) throws Exception {
		String refCode = referenceEditCode(ctx);
		if (refCode != null) {
			return refCode;
		}
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<div class=\"row\"><div class=\"" + LABEL_CSS + "\">");
		out.println(getEditLabelCode());
		boolean isUnity = !StringHelper.isEmpty(getUnity(ctx));
		out.println("<label for=\"" + getInputName() + "\">" + getLabel(ctx, new Locale(ctx.getContextRequestLanguage())) + " : </label></div><div class=\"" + (isUnity ? SMALL_VALUE_SIZE : VALUE_SIZE) + "\">");
		String readOnlyHTML = "";
		if (isReadOnly()) {
			readOnlyHTML = " readonly=\"readonly\"";
		}
		out.println("	<input class=\"form-control\" type=\"number\" step=\"" + getStep(ctx) + "\" min=\"" + getMin(ctx) + "\" max=\"" + getMax(ctx) + "\"" + readOnlyHTML + " id=\"" + getInputName() + "\" name=\"" + getInputName() + "\" value=\"" + StringHelper.neverNull(getValue()) + "\"/></div>");
		if (isUnity) {
			out.println("<div class=\"" + SMALL_PART_SIZE + " unity col-form-label\">" + getUnity(ctx) + "</div>");
		}
		out.println("</div>");
		if (getMessage() != null && getMessage().trim().length() > 0) {
			out.println("<div class=\"row form-group\"><div class=\"" + LABEL_CSS + "\"></div><div class=\"" + VALUE_SIZE + "\">");
			out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
			out.println("</div></div>");
		}

		out.close();
		return writer.toString();
	}

	@Override
	public boolean search(ContentContext ctx, String query) {
		if (isSearchAsRange()) {
			return searchAsRange(ctx, query);
		}
		if (!StringHelper.isDigit(getValue())) {
			return false;
		}
		if (getSearchType().equals(DEFAULT_SEARCH_TYPE)) {
			return super.search(ctx, query);
		} else if (getSearchType().equals("<=")) {
			return Integer.parseInt(getValue()) <= Integer.parseInt(query);
		} else if (getSearchType().equals("<")) {
			return Integer.parseInt(getValue()) < Integer.parseInt(query);
		} else if (getSearchType().equals(">=")) {
			return Integer.parseInt(getValue()) >= Integer.parseInt(query);
		} else if (getSearchType().equals(">")) {
			return Integer.parseInt(getValue()) > Integer.parseInt(query);
		} else {
			logger.warning("bad search type : " + getSearchType());
			return false;
		}
	}

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		if (StringHelper.isDigit(getInitValue())) {
			setValue(getInitValue());
		} else {
			int min = getMin(ctx);
			if (min < 0) {
				min = 0;
			}
			setValue("" + min);
		}
		return true;
	}
	
	@Override
	public String getFormatedValue(ContentContext ctx) {
		String value = getValue();
		if (StringHelper.isDigit(value)) {
			double number = Double.parseDouble(value);			
			DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(ctx.getLocale());
			return df.format(number);
		} else {
			return value;
		}
	}

}
