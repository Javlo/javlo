package org.javlo.fields;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Locale;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;

public class FieldAge extends FieldDate {

	@Override
	public String getType() {
		return "age";
	}
	
	public String getFromName(ContentContext ctx) throws Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("global.from", "from");		
	}	
	
	public String getToName(ContentContext ctx) throws Exception {
		return i18nAccess.getText("global.to", "to");
	}

	@Override
	public String getSearchEditXHTMLCode(ContentContext ctx) throws Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"row\"><div class=\"col-sm-4 align-middle\">");		
		out.println("	<label class=\"col-form-label\" for=\"" + getInputName() + "\">" + getSearchLabel(ctx, ctx.getLocale()) + " : </label>");
		out.println("</div><div class=\"col-sm-8\"><input type=\"hidden\" name=\""+getInputName()+"\" value=\"1\" />");
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		out.println("<div class=\"form-group form-inline-2 text-right\">");
		out.println("<label for=\""+getFromName(ctx)+"\" class=\"form-label text-left\">"+i18nAccess.getViewText("global.from")+"</label>");		
		out.println("<input type=\"number\" min=\"0\" max=\"99\" class=\"form-control\" id=\""+getFromName(ctx)+"\" name=\""+getFromName(ctx)+"\" value=\""+rs.getParameter(getFromName(ctx), "0")+"\">");
		out.println("<label for=\""+getToName(ctx)+"\" class=\"col-sm-4 col-form-label text-left\">"+i18nAccess.getViewText("global.to")+"</label>");		
		out.println("<input type=\"number\" min=\"0\" max=\"99\" class=\"form-control\" id=\""+getToName(ctx)+"\" name=\""+getToName(ctx)+"\" value=\""+rs.getParameter(getToName(ctx), "99")+"\">");
		out.println("</div>");		
		out.println("</div></div>");
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
			RequestService rs = RequestService.getInstance(ctx.getRequest());			
			int age = TimeHelper.getAge(date);
			try {
				if (age >= Integer.parseInt(rs.getParameter(getFromName(ctx),"0")) && age <= Integer.parseInt(rs.getParameter(getToName(ctx),"99"))) {
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
}
