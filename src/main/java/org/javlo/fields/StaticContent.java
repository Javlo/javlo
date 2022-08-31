package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;

public class StaticContent extends Field {

	public String TYPE = "static";

	@Override
	public String getViewXHTMLCode(ContentContext ctx) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<" + getTag() + " class=\"" + getType() + ' ' + getName() + ' ' + getCSSClass() + "\">");
		out.println(XHTMLHelper.textToXHTML(StringHelper.neverNull(getLabel(ctx, ctx.getLocale()))));
		out.println("</" + getTag() + ">");

		out.close();
		return writer.toString();
	}
	
	@Override
	public String getEditXHTMLCode(ContentContext ctx, boolean search) throws Exception {
		return getViewXHTMLCode(ctx);
	}

	public String getTag() {
		return properties.getProperty("field." + getUnicName() + ".tag", "p");
	}

	public String getType() {
		return TYPE;
	}

	public boolean isTitle() {
		return getTag().equalsIgnoreCase("h1");
	}

}
