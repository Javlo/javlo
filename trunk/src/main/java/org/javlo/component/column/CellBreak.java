package org.javlo.component.column;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.context.ContentContext;

public class CellBreak extends TableComponent {
	
	private static final String TYPE = "cell-break";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		TableContext tableContext = getContext(ctx);
		
		String positionCSS = "";		
		if (tableContext.isFirst(this)) {
			positionCSS = "first ";
		}
		if (tableContext.isLast(this)) {
			positionCSS = positionCSS + "last";
		}
		if (positionCSS.length() > 0) {
			positionCSS = "class=\""+positionCSS+"\" ";
		}		
		if (tableContext.isTableOpen()) {
			out.println("</td><td"+getColSpanHTML(ctx)+' '+positionCSS+"style=\""+getTDStyle(ctx)+"\">");
		} else {
			tableContext.openTable();
			String tableStyle = "";
			String border = "border=\"0\" ";			
			
			TableBreak tableBreak = tableContext.getTableBreak();
			tableStyle = (tableStyle + ' ' + tableBreak.getOpenTableStyle(ctx)).trim();
			if (tableBreak.isGrid(ctx)) {
				border = "border=\"1\" ";
			}
			
			out.println("<table "+border+"style=\""+tableStyle+"\" class=\"component-table\"><tr><td"+getColSpanHTML(ctx)+' '+positionCSS+"style=\""+getTDStyle(ctx)+"\">");
		}
		out.close();
		return new String(outStream.toByteArray());		
	}
	
	@Override
	protected String getPadding(ContentContext ctx) {
		String padding = getFieldValue("padding");
		if (padding == null || padding.trim().length() == 0) {
			try {
				padding = getContext(ctx).getTableBreak().getPadding(ctx);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return padding;
	}
	
	@Override
	protected String getWidth(ContentContext ctx) {
		String width = getFieldValue("width");
		if (width == null || width.trim().length() == 0) {
			try {
				width = getContext(ctx).getTableBreak().getWidth(ctx);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return width;
	}
	
	@Override
	protected String getVAlign(ContentContext ctx) {
		String valign = getFieldValue("valign");
		if (valign == null || valign.trim().length() == 0) {
			try {
				valign = getContext(ctx).getTableBreak().getVAlign(ctx);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return valign;
	}

}

