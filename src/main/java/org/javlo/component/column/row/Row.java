package org.javlo.component.column.row;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.javlo.component.column.CloseCol;
import org.javlo.component.column.ColContext;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;

public class Row extends AbstractRowComponent {

	public static final String TYPE = "row";

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
		return getConfig(ctx).getProperty("row.class", "row");
	}

	public String getColCssClass(ContentContext ctx, String width) throws Exception {
		String outCol = getConfig(ctx).getProperty("col.class", "col-"+width);
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
	
	public RowBean getRow() {
		return new RowBean(getValue());
	}
	
	private String getInputNameCell(int c) {
		return getInputName("cell-"+c);
	}
	
	public List<String> getSizes(ContentContext ctx) {
		return StringHelper.stringToCollection(getConfig(ctx).getProperty("sizes", "1,2,3,4,5,6,7,8,9,10,11,12"), ",");
	}

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		RowBean row = new RowBean(null);
		row.addCell();
		row.addCell();
		setValue(row.storeToString());
		return super.initContent(ctx);
	}
	
	public String getOpenRowXHTML(ContentContext ctx) {
		String cssClass = "";
		if (!StringHelper.isEmpty(getStyle())) {
			cssClass= " "+getStyle();			
		}
		return "<div class=\"row"+cssClass+"\">";
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
		
		out.println(closeRow(ctx));
		
		EditContext editCtx = EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession());
		if (editCtx.isPreviewEditionMode() && !ctx.isAsViewMode()) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
			if (editCtx.isPreviewEditionMode()) {
				out.println("<div "+getPreviewAttributes(ctx)+"><div class=\"_component_title _component_row\">"+i18nAccess.getText("content."+getType(), getType())+"</div></div>");
			}
			
		}
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
				if (nextComp == null || (nextComp instanceof Row || nextComp instanceof CloseCol)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected String getEmptyCode(ContentContext ctx) throws Exception {
		return getViewXHTMLCode(ctx) ;
	}

	public static String closeRow(ContentContext ctx) throws Exception {
		RowContext rowContext = RowContext.getInstance(ctx);
		if (!rowContext.isFirst()) {
			RowContext.reset(ctx);
			return "</div></div></div>";	
		} else { 
			RowContext.reset(ctx);
			return "";
		}
		
	}
	
	@Override
	public String getFontAwesome() {
		return "columns";
	}
	
	@Override
	public boolean isDisplayable(ContentContext ctx) throws Exception {
		return true;
	}	

}
