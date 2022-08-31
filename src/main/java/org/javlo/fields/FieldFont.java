package org.javlo.fields;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.service.IListItem;
import org.javlo.service.ListService;
import org.owasp.encoder.Encode;

public class FieldFont extends Field {
	
	@Override
	public String getDisplayValue(ContentContext ctx, Locale locale) throws Exception {
		return "<i class=\"fab fa-"+getValue()+"\"></i>";
	}
	
	protected String getPreviewCode(ContentContext ctx, boolean title) throws Exception {			
		return getDisplayValue(ctx, ctx.getLocale());
	}
	
	@Override
	public String getEditXHTMLCode(ContentContext ctx, boolean search) throws Exception {
		List<IListItem> fontList = ListService.getInstance(ctx).getList(ctx, "fonts");
		if (fontList == null) {
			return super.getEditXHTMLCode(ctx, search);
		} else {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("<link rel=\"stylesheet\" href=\"https://use.fontawesome.com/releases/v5.2.0/css/all.css\" integrity=\"sha384-hWVjflwFxL6sNzntih27bfxkr27PmbbK/iSvJ+a4+0owXq79v+lsFkW54bOGbiDQ\" crossorigin=\"anonymous\">");
			out.println("<div class=\"row field-"+getName()+"\"><div class=\""+LABEL_CSS+"\">");
			out.println(getEditLabelCode());	
			String label=null;;
			if (search) {
				label = getSearchLabel(ctx, ctx.getLocale());
			}
			if (StringHelper.isEmpty(label)) {
				label = getLabel(ctx, ctx.getLocale());
			}
			out.println("<label class=\"col-form-label\" for=\"" + getInputName() + "\">" + label + " : </label>");
			String readOnlyHTML = "";
			if (isReadOnly()) {
				readOnlyHTML = " readonly=\"readonly\"";
			}
			String value = Encode.forHtmlAttribute(StringHelper.neverNull(getValue()));
			out.println("</div><div class=\""+VALUE_SIZE+"\"><div class=\"row\"><div class=\"col-sm-8\">");
			String js = "var e = this; e.parentElement.parentElement.querySelector('.font-preview i').className=e.options[e.selectedIndex].value;";
			out.println("<select" + readOnlyHTML + " id=\"" + getInputName() + "\" class=\"form-control"+getSpecialClass()+"\" name=\"" + getInputName() + "\" value=\"" + value + "\" onchange=\""+js+"\" >");
			
			for (Iterator iterator = fontList.iterator(); iterator.hasNext();) {
				IListItem iListItem = (IListItem) iterator.next();
				out.println("<option value=\""+iListItem.getKey()+"\" "+(iListItem.getKey().equals(getValue())?"selected='selected'":"")+">"+iListItem.getValue()+"</option>");
			}
			out.println("</select>");
			out.println("</div><div class=\"col-sm-4\"><div class=\"font-preview\"><i class=\""+getValue()+"\"></i></div></div>");
			if (getMessage() != null && getMessage().trim().length() > 0) {
				out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
			}
			out.println("</div></div></div>");
			out.close();
			return new String(outStream.toByteArray());
		}
	}
	
	@Override
	public String getType() {
		return "font";
	}
	
	public static void main(String[] args) {
		File file = new File("c:/trans/font.properties");
		Properties p;
		try {
			p = ResourceHelper.loadProperties(file);
			for (Object key : p.keySet()) {
				System.out.println(key+ "  >>>  "+p.getProperty(key.toString()));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
