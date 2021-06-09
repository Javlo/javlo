package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import org.javlo.bean.Link;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class SectionLinks extends AbstractVisualComponent {
	
	public static final String TYPE = "section-links";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		List<Link> links = new LinkedList<>();
		IContentVisualComponent comp = getNextComponent();
		while (comp != null) {
			if (comp instanceof Section) {
				Section section = (Section)comp;
				if (!StringHelper.isEmpty(section.getTitle())) {
					links.add(new Link("#"+section.getHtmlId(), section.getTitle()));
				}
			}
			comp = comp.getNextComponent();
		}
		ctx.getRequest().setAttribute("links", links);
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		List<Link> links = (List<Link>)ctx.getRequest().getAttribute("links");
		out.println("<nav class=\"nav\">");
		for (Link link : links) {
			out.println("<a class=\"nav-links\" href=\""+link.getUrl()+"\">"+link.getTitle()+"</a>");
		}
		out.println("</nav>");
		out.close();
		return new String(outStream.toByteArray());
	}

}
