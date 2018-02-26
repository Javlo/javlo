package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class ComponentList extends AbstractPropertiesComponent implements IAction {
	
	private static List<String> types = new LinkedList<String>(Arrays.asList(new String[] {"page", "type", "size"}));

	public static final String TYPE = "component-list";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}

	@Override
	public String getHexColor() {
		return CONTAINER_COLOR;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}
	
	@Override
	public String getFontAwesome() {
		return "bars";
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return !StringHelper.isEmpty(getValue());
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement root = content.getNavigation(ctx);
		MenuElement page = root.searchChildFromName(getFieldValue("page", null));
		int size = Integer.MAX_VALUE;
		if (StringHelper.isDigit(getFieldValue("size"))) {
			size = Integer.parseInt(getFieldValue("size"));
		};
		int c=0;
		if (page != null) {
			List<IContentVisualComponent> data = page.getContentByType(ctx, getFieldValue("type", null));
			if (data != null && data.size() > 0) {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				PrintStream out = new PrintStream(outStream);
				out.println("<ul>");
				String cssClass = " class=\"first\"";
				for (IContentVisualComponent comp : data) {					
					if (c<size) {
						out.println("<li"+cssClass+">"+comp.getPrefixViewXHTMLCode(ctx)+comp.getXHTMLCode(ctx)+comp.getSuffixViewXHTMLCode(ctx)+"</li>");
						cssClass="";
					}
					c++;
				}
				out.println("</ul>");
				out.close();
				return new String(outStream.toByteArray());
			}
		}
		return "";
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return types;
	}
}
