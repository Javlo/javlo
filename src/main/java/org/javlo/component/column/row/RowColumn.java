package org.javlo.component.column.row;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;

public class RowColumn extends AbstractRowComponent {

	public static final String TYPE = "row-column";

	@Override
	public String getType() {
		return TYPE;
	}

	protected boolean isCellEmpty(ContentContext ctx) throws Exception {
		ContentContext ctxCompArea = ctx.getContextWithArea(getArea());
		ContentElementList content = getPage().getContent(ctxCompArea);
		while (content.hasNext(ctxCompArea)) {
			IContentVisualComponent comp = content.next(ctxCompArea);
			if (comp.getId().equals(getId())) {
				IContentVisualComponent nextComp = content.next(ctxCompArea);
				if (nextComp == null || nextComp instanceof AbstractRowComponent) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isDisplayable(ContentContext ctx) throws Exception {
		return true;
	}

	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return false;
	}

	protected String getColClass(ContentContext ctx, String w) {
		String breakpoint = getColBreakpoint(ctx);
		if (breakpoint.length() != 2) {
			return getConfig(ctx).getProperty("class.prefix", "col-" + w);
		} else {
			return getConfig(ctx).getProperty("class.prefix", "col-"+breakpoint+"-" + w);
		}
	}

	public List<String> getWidths(ContentContext ctx) {
		return StringHelper.stringToCollection(getConfig(ctx).getProperty("widths", "1,2,3,4,5,6,7,8,9,10,11,12"), ",");
	}
	
	public String getColBreakpoint(ContentContext ctx) {
		return getConfig(ctx).getProperty("breakpoint", "").trim();
	}
	
	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "";
	}
	
	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);		
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);		
		out.println("<div class\"form-group\">");
		out.println("<div class\"row\"><div class\"col-sm-1\">");
		
		out.println("<label for=\"" + getContentName() + "\">" + i18nAccess.getText("global.width")
				+ "</label></div><div class\\\"col-sm-11\\\">");
		out.println(XHTMLHelper.getInputOneSelect(getContentName(), getWidths(ctx), getValue(), "form-control", false));		
		out.println("</div></div></div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String getValue() {
		String value = super.getValue();
		if (StringHelper.isEmpty(value)) {
			return "3";
		} else {
			return value;
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		RowContext rowContext = getContext(ctx);
		String positionCSS = "open-col " + getColClass(ctx, getValue()) + ' ';
		Row row = rowContext.getRowComponent(ctx, getPage(), getArea());
		if (rowContext.isFirst() && row != null) {
			out.println(row.getOpenRowXHTML(ctx));
			positionCSS += "first ";
		} else {
			out.println("</div></div>"); // close latest cell
		}
		if (isCellEmpty(ctx)) {
			positionCSS += " empty";
		}
		if (!StringHelper.isEmpty(getStyle())) {
			positionCSS += " "+getStyle();
		}
		// if (tableContext.isLast(this)) {
		// positionCSS = positionCSS + "last ";
		// }
		// if (isCellEmpty(ctx)) {
		// positionCSS = positionCSS + "empty";
		// }
		if (positionCSS.length() > 0) {
			positionCSS = " class=\"" + positionCSS.trim() + "\" ";
		}
		EditContext editCtx = EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession());
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);	
		
		out.println("<div" + positionCSS + "><div class=\"cell-wrapper\">");
		if (editCtx.isPreviewEditionMode() && !ctx.isAsViewMode()) {
			out.println("<div "+getPreviewAttributes(ctx)+">");
			if (!isCellEmpty(ctx)) {
				out.println("<div class=\"_component_title\">"+i18nAccess.getText("content."+getType(), getType())+" ["+getValue()+"]</div>");	
			}
			if (isCellEmpty(ctx) && ctx.isAsPreviewMode() && EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession()).isPreviewEditionMode()) {
				out.print("<span class=\"cell-name\">" + rowContext.getColNumber() +" ["+getValue()+"] </span>");
			}
			out.println("</div>");
		}
		out.close();
		rowContext.nextCol();
		return new String(outStream.toByteArray());
	}

	private RowContext getContext(ContentContext ctx) throws Exception {
		return RowContext.getInstance(ctx);
	}
	
	@Override
	public String getFontAwesome() {
		return "window-maximize";
	}

}
