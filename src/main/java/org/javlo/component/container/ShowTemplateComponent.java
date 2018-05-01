package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.template.Template;
import org.javlo.template.Template.TemplateBean;
import org.javlo.template.TemplateFactory;

public class ShowTemplateComponent extends AbstractVisualComponent {

	private static final String TYPE = "show-template";

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		Collection<TemplateBean> templates = new LinkedList<TemplateBean>();
		for (String id : getTemplateIds(ctx)) {
			Template template = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(id);
			if (template != null) {
				template.importTemplateInWebapp(StaticConfig.getInstance(ctx.getRequest().getSession()), ctx);
				templates.add(new TemplateBean(ctx, template));
			}
		}
		ctx.getRequest().setAttribute("templates", templates);
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);
		Collection<TemplateBean> templates = (Collection<TemplateBean>)ctx.getRequest().getAttribute("templates");
		if (templates.size()>0) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("<ul>");
			for (TemplateBean template : templates) {
				out.println("<li><a href=\""+template.getDownloadURL()+"\">"+template.getName()+"</a></li>");
			}
			out.println("</ul>");
			out.close();
			return new String(outStream.toByteArray());
		} else {
			return "";
		}
	}
	
	@Override
	public String getFontAwesome() {
		return "clone";
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_ADMIN;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return getTemplateIds(ctx).size()>0;
	}
	
	public List<String> getTemplateIds(ContentContext ctx) {
		return StringHelper.stringToCollection(getValue(), ",");
	}
	
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		Collection<String> templates = getTemplateIds(ctx);
		Collection<Template> allTemplates = TemplateFactory.getAllTemplates(ctx.getRequest().getSession().getServletContext());
		out.println("<div class=\"row\">");
		for (Template template : allTemplates) {
			String checked = "";
			if (templates.contains(template.getId())) {
				checked = "checked=\"checked\"";
			}
			out.println("<div class=\"col-lg-2 col-xs-6 col-sm-3\"><label class=\"checkbox-inline\"><input type=\"checkbox\" name=\""+getContentName()+"\" value=\""+template.getId()+"\" "+checked+" />"+template.getId()+"</div>");
		}
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String getType() { 
		return TYPE;
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

}
