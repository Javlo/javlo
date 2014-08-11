package org.javlo.component.column;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.context.ContentContext;

public class RowBreak extends TableComponent {
	
	private static final String TYPE = "row-break";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getSpecificClass(ContentContext ctx) {
		try {
			if (!getContext(ctx).isTableOpen()) {
				return " error";
			} else {
				return super.getSpecificClass(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return super.getSpecificClass(ctx);
		}
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("</td></tr><tr><td"+getColSpanHTML(ctx)+" style=\""+getTDStyle(ctx)+"\">");
		out.close();
		return new String(outStream.toByteArray());
	}

}
