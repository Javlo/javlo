package org.javlo.fields;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Locale;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.service.RequestService;

public class FieldAge extends FieldDate {

	@Override
	public String getType() {
		return "age";
	}

	@Override
	public String getSearchEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"row\">");
		out.println("<div class=\"" + LABEL_SIZE + "\">");
		out.println("	<label for=\"" + getInputName() + "\">" + getLabel(ctx, new Locale(ctx.getContextRequestLanguage())) + " : </label>");
		out.println("</div><div class=\"" + VALUE_SIZE + "\">");
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		String currentValue = rs.getParameter(getInputName(), "");
		out.println("<select class=\"form-control\" name=\"" + getInputName() + "\" id=\"" + getInputName() + "\">");	
		out.println("<option></option>");
		for (int y = -1; y < 80; y += 5) {
			String select = "";
			if (currentValue.equals(""+(y+1)+","+(y+5))) {
				select = " selected=\"selected\"";
			}
			out.println("<option value=\""+(y+1)+","+(y+5)+"\""+select+">"+(y+1)+" > "+(y+5)+"</option>");
		}
		out.println("</select>");
		out.println("</div>");
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public boolean search(ContentContext ctx, String query) {		
		if (StringHelper.isEmpty(query)) {
			return true;
		} else {
			Date date = getDate(ctx);
			if (date == null) {
				return false;
			}
			String[] values = query.split(",");
			int age = TimeHelper.getAge(date);
			if (age>=Integer.parseInt(values[0])&&age<=Integer.parseInt(values[1])) {
				return true;
			} else {
				return false;
			}
		}
		
	}

}
