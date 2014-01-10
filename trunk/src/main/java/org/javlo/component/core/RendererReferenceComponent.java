package org.javlo.component.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Properties;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.RequestService;

public class RendererReferenceComponent extends AbstractVisualComponent {
	
	public static final String TYPE = "renderer-reference-component";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_ADMIN;
	}
	
	protected String getRendererInputName() {
		return getInputName("renderer");
	}
	
	protected String getConfigInputName() {
		return getInputName("config");
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		Properties prop = new Properties();
		prop.load(new StringReader(getValue()));
		ctx.getRequest().setAttribute("config", prop);		
	}
	
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		
		out.println("<div class=\"line\">");
		out.println("<label for=\""+getRendererInputName()+"\">renderer</label>");
		out.println("<input type=\"text\" id=\""+getRendererInputName()+"\" name=\""+getRendererInputName()+"\" value=\""+StringHelper.neverNull(getComponentBean().getRenderer())+"\" />");
		out.println("</div>");
		
		out.println("<div class=\"line\">");
		out.println("<label for=\""+getConfigInputName()+"\">config</label>");
		out.println("<textarea id=\""+getConfigInputName()+"\" name=\""+getConfigInputName()+"\" cols=\"70\" rows=\"10\">"+getValue(ctx)+"</textarea>");
		out.println("</div>");

		
		out.close();
		return new String(outStream.toByteArray());
	}
	
	@Override
	public String getRenderer(ContentContext ctx) {
		ContentContext notAbsCtx = new ContentContext(ctx);
		notAbsCtx.setAbsoluteURL(false);
		try {
			return URLHelper.createStaticTemplateURLWithoutContext(notAbsCtx , ctx.getCurrentTemplate(), getComponentBean().getRenderer());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}				
	}
	
	protected void setLocalRenderer(String renderer) {
		getComponentBean().setRenderer(renderer);
	}
	
	@Override
	public void performEdit(ContentContext ctx) throws Exception {		
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		
		String renderer = rs.getParameter(getRendererInputName(), "");
		String config = rs.getParameter(getConfigInputName(), "");
		
		if (!renderer.equals(getComponentBean().getRenderer())) {
			setModify();
			setLocalRenderer(renderer);
		}
		
		if (!config.equals(getValue())) {
			setModify();
			setValue(config);
		}		
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}
	
	@Override
	public boolean isContentCachableByQuery(ContentContext ctx) {
		return false;
	}

}
