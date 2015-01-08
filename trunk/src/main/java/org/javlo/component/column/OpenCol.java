package org.javlo.component.column;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;

public class OpenCol extends AbstractVisualComponent {

	public static final String TYPE = "open-col";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return true;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	public String getRowCssClass(ContentContext ctx) {
		return getConfig(ctx).getProperty("row.class", "container_12");
	}

	public String getColCssClass(ContentContext ctx, int width) throws Exception {
		String outCol = getConfig(ctx).getProperty("col.class", "grid_${width}");
		return outCol.replace("${width}", "" + width);
	}

	protected ColContext getContext(ContentContext ctx) throws Exception {
		return ColContext.getInstance(ctx, this);
	}

	protected int getWidth(ContentContext ctx) throws Exception {
		int width = Integer.parseInt(getValue());
		if (width == 0 || width > ColContext.MAX_WIDTH) {
			width = ColContext.MAX_WIDTH - ColContext.getInstance(ctx, this).getRowWidth();
		}
		return width;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<label class=\"radio-inline\">");
		String checkedAttr = "";
		if (getValue().equals("") || getValue().equals("0")) {
			checkedAttr = " checked=\"checked\"";
		}
		out.println("<input type=\"radio\" name=\"" + getContentName() + "\" value=\"0\"" + checkedAttr + "> auto");
		out.println("</label>");
		for (int i = 1; i <= 12; i++) {
			out.println("<label class=\"radio-inline\">");
			checkedAttr = "";
			if (getValue().equals("" + i)) {
				checkedAttr = " checked=\"checked\"";
			}
			out.println("<input type=\"radio\" name=\"" + getContentName() + "\" value=\"" + i + "\"" + checkedAttr + "> " + i);
			out.println("</label>");

		}
		out.close();
		return new String(outStream.toByteArray());

	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		ColContext colContext = getContext(ctx);

		String positionCSS = getType() + ' ';
		if (colContext.isFirst(this)) {
			positionCSS = "first ";
		}
		if (colContext.isLast(this)) {
			positionCSS = positionCSS + "last ";
		}		
		if (isColEmpty(ctx)) {
			positionCSS = positionCSS + "empty";
		}
		int width = getWidth(ctx);
		if (colContext.isOpen()) {
			out.println("</div></div><div class=\"" + getColCssClass(ctx, width) + ' ' + positionCSS + "\"><div class=\"cell-wrapper\">");
			if (isColEmpty(ctx) && ctx.isAsPreviewMode() && EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession()).isEditPreview()) {
				out.print("<span class=\"cell-name\">col-" + width + "</span>");
			}
		} else {
			colContext.setOpen(true);
			out.println("<div class=\"" + getRowCssClass(ctx) + "\"><div class=\"" + getColCssClass(ctx, width) + ' ' + positionCSS + "\"><div class=\"cell-wrapper\">");
			if (isColEmpty(ctx) && ctx.isAsPreviewMode() && EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession()).isEditPreview()) {
				out.print("<span class=\"cell-name\">col-" + width + "</span>");
			}
		}
		colContext.setRowWidth(colContext.getRowWidth() + width);
		out.close();
		return new String(outStream.toByteArray());
	}

	protected boolean isColEmpty(ContentContext ctx) throws Exception {
		ContentContext ctxCompArea = ctx.getContextWithArea(getArea());
		ContentElementList content = getPage().getContent(ctxCompArea);
		while (content.hasNext(ctxCompArea)) {
			IContentVisualComponent comp = content.next(ctxCompArea);
			if (comp.getId().equals(getId())) {
				IContentVisualComponent nextComp = content.next(ctxCompArea);
				if (nextComp == null || (nextComp instanceof OpenCol || nextComp instanceof CloseCol)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected String getEmptyCode(ContentContext ctx) throws Exception {
		return getViewXHTMLCode(ctx) + super.getPrefixViewXHTMLCode(ctx) + "<div class=\"table-component-preview " + getType() + "\">" + getType() + "</div>" + super.getSuffixViewXHTMLCode(ctx);
	}

	public static String closeRow(ContentContext ctx, ColContext colContext) {
		colContext.reset();
		return "</div></div></div>";
	}

}
