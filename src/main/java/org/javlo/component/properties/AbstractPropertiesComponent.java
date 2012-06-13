package org.javlo.component.properties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;

public abstract class AbstractPropertiesComponent extends AbstractVisualComponent {

	protected Properties properties = new Properties();

	protected String createKeyWithField(String inField) {
		return "field_" + inField + "_" + getId();
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		List<String> fields = getFields(ctx);

		out.println("<div class=\"edit\" style=\"padding: 3px;\">");
		for (String field : fields) {
			out.println("<div class=\"field-label\">");
			out.println("<label for=\"" + createKeyWithField(field) + "\">");
			out.println(i18nAccess.getText("field." + field));
			out.println("</label>");
			out.println("</div>");
			out.println("<div class=\"field-input\">");
			out.print("<textarea rows=\"" + getRowSize(field) + "\" id=\"");
			out.print(createKeyWithField(field));
			out.print("\" name=\"");
			out.print(createKeyWithField(field));
			out.print("\">");
			out.print(getFieldValue(field));
			out.println("</textarea>");
			out.println("</div>");
		}
		out.println("</div>");

		out.flush();
		out.close();
		return writer.toString();
	}

	protected double getFieldDoubleValue(String inField) {
		return Double.parseDouble(properties.getProperty(inField, "0"));
	}

	protected long getFieldLongValue(String inField) {
		return Long.parseLong(properties.getProperty(inField, "0"));
	}

	public abstract List<String> getFields(ContentContext ctx) throws Exception;

	protected String getFieldValue(String inField) {
		return properties.getProperty(inField, "");
	}

	protected String getFieldValue(String inField, String defaultValue) {
		return properties.getProperty(inField, defaultValue);
	}

	public String getHeader() {
		return getType();
	}

	public int getRowSize(String field) {
		return 1;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		List<String> fields = getFields(ctx);
		if (getRenderer(ctx) != null) {		
			for (String field : fields) {
				ctx.getRequest().setAttribute(field, getFieldValue(field));
			}
			return executeJSP(ctx, getRenderer(ctx));
		} else {
			StringBuffer out = new StringBuffer();
			out.append("<div class=\"");
			out.append(getType());
			out.append("\">");
			for (String field : fields) {
				out.append("<div class=\"");
				out.append(field);
				out.append("\">");
				out.append(getFieldValue(field));
				out.append("</div>");
			}
			out.append("</div>");
			return out.toString();
		}
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		Collection<Object> values = properties.values();
		int wordCount = 0;
		for (Object value : values) {
			if (value != null) {
				wordCount = wordCount + value.toString().split(" ").length;
			}
		}
		return wordCount;
	}

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
		properties.load(stringToStream(getValue()));
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		List<String> fields = getFields(ctx);
		for (String field : fields) {
			String fieldValue = requestService.getParameter(createKeyWithField(field), null);
			if (fieldValue != null) {
				if (!fieldValue.equals(getFieldValue(field))) {
					setModify();
					properties.put(field, fieldValue);
				}
			}
		}

		if (isModify()) {
			storeProperties();
		}

	}

	protected void setFieldValue(String inField, String value) {
		properties.setProperty(inField, value);
	}

	public void storeProperties() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String res = "";
		try {
			properties.store(out, getHeader());
			out.flush();
			res = new String(out.toByteArray());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setValue(res);
	}

}
