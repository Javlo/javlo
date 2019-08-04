package org.javlo.component.text;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.service.RequestService;

public class SecureText extends AbstractPropertiesComponent implements IAction {
	
	public static final String TYPE = "secure-text";
	
	public static final List<String> FIELDS = Arrays.asList(new String[] { "code", "text" });

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}
	
	
	public String getSessionPassword(ContentContext ctx) {
		return (String)ctx.getRequest().getSession().getAttribute(TYPE+'-'+getId());
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		String pwd = getSessionPassword(ctx);
		if (getFieldValue("code", "").equals(pwd)) {
			return getFieldValue("text");
		} else {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("<form action\""+URLHelper.createURL(ctx)+"\" method=\"post\"><input type=\"hidden\" name=\"webaction\" value=\""+TYPE+".code\" /><input type=\"hidden\" name=\"id\" value=\""+getId()+"\" />"
					+ "<div class=\"row\"><div class=\"col-auto\">" + 
					"<div class=\"input-group mb-2\">" + 
					"  <div class=\"input-group-prepend\">" + 
					"    <div class=\"input-group-text\"><i class=\"fas fa-key\"></i></div>" + 
					"  </div>" + 
					"  <input type=\"text\" class=\"form-control\" name=\"code\" placeholder=\"secret key\" />" + 
					"</div>" + 
					"</div>" +
					"<div class=\"col-auto\">\r\n" + 
					"  <button type=\"submit\" class=\"btn btn-primary mb-2\"><i class=\"fas fa-lock-open\"></i></button>\r\n" + 
					"</div></div></form>");
			out.close();
			return new String(outStream.toByteArray());
			
		}
	}
	
	@Override
	public String getFontAwesome() {
		return "lock";
	}
	
	public static String performCode(ContentContext ctx, RequestService rs) throws Exception {
		ctx.getRequest().getSession().setAttribute(TYPE+'-'+rs.getParameter("id"), rs.getParameter("code"));
		return null;
	}

	@Override
	public String getActionGroupName() {
		return TYPE;
	}

}
