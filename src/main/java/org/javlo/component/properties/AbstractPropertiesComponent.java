package org.javlo.component.properties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;

public abstract class AbstractPropertiesComponent extends AbstractVisualComponent {

	protected Properties properties = new Properties();
	
	protected String createKeyWithField(String inField) {
		return getInputName(inField);
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		Map<String,String> fields = new HashMap<String, String>();
		for (String field : getFields(ctx)) {
			fields.put(field, getFieldValue(field));
		}
		ctx.getRequest().setAttribute("fields", fields);
	}
	
	protected static String getFieldName(String field) {
		if (!field.contains("#")) {
			return field;
		} else {
			return field.substring(0, field.indexOf('#'));
		}
	}
	
	protected static String getFieldType(String field) {
		if (!field.contains("#")) {
			return "text";
		} else {
			return field.substring(field.indexOf('#')+1);
		}
	}
	
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		List<String> fields = getFields(ctx);
		out.println("<div class=\"row\">");
		for (String field : fields) {
			String fieldName = getFieldName(field);
			String fieldType = getFieldType(field);
			out.println("<div class=\"col-md-4 col-xs-6\">");
			out.println("<div class=\"form-group\">");
			if (fieldType.equals("text")) {
				out.println("<label for=\"" + createKeyWithField(fieldName) + "\">");						
				out.println(i18nAccess.getText("field." + fieldName, fieldName));
				out.println("</label>");
				out.print("<textarea class=\"form-control\" rows=\"" + getRowSize(fieldName) + "\" id=\"");
				out.print(createKeyWithField(field));
				out.print("\" name=\"");
				out.print(createKeyWithField(fieldName));
				out.print("\">");
				out.print(getFieldValue(fieldName));
				out.println("</textarea>");				
			} else if (fieldType.equals("checkbox")) {
				out.println("<div class=\"checkbox\"><label>");				
				String checked = "";
				if (getFieldValue(fieldName).length()>0) {
					checked=" checked=\"checked\"";
				}
				out.print("<input type=\"checkbox\" id=\"");
				out.print(createKeyWithField(field));
				out.print("\" name=\"");
				out.print(createKeyWithField(fieldName));
				out.print("\" "+checked+" />");
				out.println(i18nAccess.getText("field." + fieldName, fieldName));
				out.println("</label>");				
				out.println("</div>");
			} else {
				out.println("type not found : "+fieldType);
			}
			out.println("</div></div>");
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
		try {
			return Long.parseLong(properties.getProperty(inField, "0"));
		} catch (NumberFormatException e) {			
			logger.warning(e.getMessage());
			return 0;
		}		
	}

	public abstract List<String> getFields(ContentContext ctx) throws Exception;

	protected String getFieldValue(String inField) {		
		return properties.getProperty(getFieldName(inField), "");
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

	protected String getListSeparator() {
		return ",";
	}
	
	public String validateField(ContentContext ctx, String fieldName, String fieldValue) throws Exception {
		return null;
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		List<String> fields = getFields(ctx);
		String msg = null;
		for (String fieldKey : fields) {
			String field = getFieldName(fieldKey);
			String fieldValue = requestService.getParameter(createKeyWithField(field), null);
			
			String newMsg = validateField(ctx, field, fieldValue);
			if (newMsg != null) {
				msg = newMsg;
			}
			
			String[] fieldValues = requestService.getParameterValues(createKeyWithField(field), null);
			if (fieldValues != null && fieldValues.length > 1) {
				fieldValue = StringHelper.arrayToString(fieldValues, getListSeparator());
			}

			if (fieldValue != null) {
				if (!fieldValue.equals(getFieldValue(field))) {
					setModify();
					properties.put(field, fieldValue);
				}
			} else {
				if (StringHelper.isTrue(properties.get(field))) {
					setModify();
				}
				properties.remove(field);
			}

		}

		if (isModify()) {
			storeProperties();
		}
		
		return msg;
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
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		try {
			for (String field : getFields(ctx)) {
				String fieldValue = getFieldValue(field);
				if (fieldValue != null && fieldValue.trim().length() > 0) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
