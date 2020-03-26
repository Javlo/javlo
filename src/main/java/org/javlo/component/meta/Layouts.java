/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.meta;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;

/**
 * list of tags of the current page.
 * <h4>JSTL variable :</h4>
 * <ul>
 * <li>inherited from {@link AbstractVisualComponent}</li>
 * <li>{@link String} tags : list of tags. See {@link MenuElement#getTags}</li>
 * *
 * </ul>
 * 
 * @author pvandermaesen
 */
public class Layouts extends AbstractVisualComponent {

	public static final String TYPE = "layouts";
	
	private static String EMPTY_LAYOUT = "___emptylayout";

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		List<String> layouts = ctx.getCurrentTemplate().getLayouts();
		if (layouts != null) {
			Collections.sort(layouts);
			int i = 0;
			List<String> currentlayout = getLayouts();
			out.print("<input type=\"hidden\" name=\"layout-" + getId() + "\" value=\"layout\"/>");
			out.print("<input type=\"hidden\" name=\"" + getContentName() + "\" value=\""+EMPTY_LAYOUT+"\"/>");
			out.println("<ul>");			
			for (String layout : layouts) {
				String checked = "";
				if (currentlayout.contains(layout)) {
					checked = " checked=\"checked\"";
				}				
				out.print("<li class=\"line\"><input type=\"checkbox\"" + checked + " id=\"" + getContentName() + i + "\" name=\"" + getContentName() + "\" value=\"" + layout + "\"/>");
				out.print("<label for=\"" + getContentName() + i + "\" >" + layout + "</label></li>");
				i++;
			}
			out.println("</ul>");
		} else {
			return super.getEditXHTMLCode(ctx);
		}
		return writer.toString();
	}

	@Override
	public String getHexColor() {
		return WEB2_COLOR;
	}
	
	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return super.getPrefixViewXHTMLCode(ctx) + "<div" + getPrefixCssClass(ctx, getComponentCssClass(ctx) + " list count" + getLayouts().size()) + getSpecialPreviewCssId(ctx) + ">";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "</div>" + super.getSuffixViewXHTMLCode(ctx);
	}

	public List<String> getLayouts() {
		String[] tags = StringHelper.stringToArray(getValue(), ",");
		List<String> tagsList = new LinkedList<String>();		
		for (String tag : tags) {			
			if (tag.trim().length() > 0 && !tag.equals(EMPTY_LAYOUT)) {
				tagsList.add(tag);
			}
		}
		return tagsList;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		if (ctx.getCurrentPage() != null) {
			ctx.getRequest().setAttribute("layouts", ctx.getCurrentPage().getTags(ctx));
		}
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "<span class=\"layout-inside "+StringHelper.collectionToString(getLayouts(), " ")+"\"></span>";
	}

	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
	}
	
	@Override
	public String getEmptyXHTMLCode(ContentContext ctx) throws Exception {	
		return "<span class=\""+EDIT_CLASS + "\">" + getType()+" : "+StringHelper.collectionToString(getLayouts()) + "</span>";
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}
	
	@Override
	public String getFontAwesome() {
		return "columns";
	}

}
