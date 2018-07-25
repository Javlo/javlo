package org.javlo.helper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.owasp.encoder.Encode;

public class XHTMLFormBuilder {
	
	private static final String renderSimpleField(String type, String label, String name, String value, String placeholder) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"form-group\">");
		if (!StringHelper.isEmpty(label)) {
			out.println("<label for=\""+name+"\">"+label+"</label>");
		}
		out.println("<input type=\""+type+"\" class=\"form-control\" name=\""+name+"\" value=\""+Encode.forHtmlAttribute(value)+"\" id=\""+name+"\" "+(!StringHelper.isEmpty(placeholder)?"placeholder=\""+placeholder+"\"":"")+" />");
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}
	
	public static final String renderSimpleText(String label, String name, String value, String placeholder) {
		return renderSimpleField("text", label, name, value, placeholder);
	}
	
	public static final String renderSelect(String label, String name, List<String> values, String currentValue) {
		Collection<Map.Entry<String, String>> valuesMap = new LinkedList<Map.Entry<String,String>>();
		for (String val : values) {
			valuesMap.add(new AbstractMap.SimpleEntry(val,val));
		}
		return renderSelect(label, name, valuesMap, currentValue, false);
	}
	
	public static final String renderSelect(String label, String name, Collection<Map.Entry<String, String>> values, String currentValue, boolean submitOnChange) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"form-group\">");
		if (!StringHelper.isEmpty(label)) {
			out.println("<label for=\""+name+"\">"+label+"</label>");
		}
		String js = "";
		if (submitOnChange) {
			js=" onchange=\"this.form.submit()\"";
		}
		out.println("<select"+js+" class=\"form-control\" name=\""+name+"\" id=\""+name+"\">");
		for (Map.Entry<String, String> value : values) {			
			out.println("<option "+(value.getKey().equals(currentValue)?"selected=\"selected\" ":"")+"value=\""+value.getKey()+"\">"+value.getValue()+"</option>");
		}
		out.println("</select></div>");
		out.close();
		return new String(outStream.toByteArray());
	}

}
