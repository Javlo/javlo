package org.javlo.component.web2;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class ChooseBaseContext extends AbstractPropertiesComponent implements IAction {
	
	private static final String PREFIX = "prefix";
	private static final String SELECT = "select";
	private static final String PREVIEW = "preview";
	
	public static final String TYPE = "choose-base-context";
	
	private static final List<String> FIELDS = new LinkedList(Arrays.asList(new String[] { PREFIX, SELECT, PREVIEW }));
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		String prefix = getFieldValue(PREFIX);
		Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(ctx.getRequest().getSession().getServletContext());
		out.println("<div class=\"row\">");
		ContentContext ctxGl = new ContentContext(ctx);
		ctxGl.setRenderMode(ContentContext.VIEW_MODE);
		for (GlobalContext glCtx : allContext) {
			if (StringHelper.isEmpty(prefix) || glCtx.getContextKey().startsWith(prefix)) {
				ctxGl.setForceGlobalContext(glCtx);
				out.println("<div class=\"col-xl-3 col-12 col-md-4\">");
				out.println("<div class=\"card\">");
				if (glCtx.isScreenshot(ctxGl)) {
					out.println("<img class=\"card-img-top\" src=\""+glCtx.getScreenshortUrl(ctxGl)+"\" alt=\"screenshot\" />");
				}
				out.println("<div class=\"card-body\">");
				out.println("<h5 class=\"card-title\">"+glCtx.getGlobalTitle()+"</h5>");
				String previewURL = URLHelper.createURL(ctxGl, "/");
				out.println("<a href=\""+previewURL+"\" target=\"_blank\" class=\"btn btn-secondary\">"+getFieldValue(PREVIEW)+"</a>");
				out.println("<a href=\"#\" class=\"btn btn-primary\">"+getFieldValue(SELECT)+"</a>");
				out.println("</div>");
				out.println("</div>");
				out.println("</div>");
			}
		}
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String getActionGroupName() {
		return TYPE;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_ADMIN;
	}

	
}

