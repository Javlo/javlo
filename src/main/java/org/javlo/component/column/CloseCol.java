package org.javlo.component.column;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

public class CloseCol extends AbstractVisualComponent{

	private static final String TYPE = "close-col";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		ColContext colContext;
		try {
			colContext = ColContext.getInstance(ctx, this);
			if (colContext.getRowWidth() >= 12) {
				out.println(OpenCol.closeRow(ctx, colContext));
				colContext.reset();			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		out.close();
		return new String(outStream.toByteArray());
	}
	
	@Override
	public String getEmptyXHTMLCode(ContentContext ctx) throws Exception {
		return getViewXHTMLCode(ctx);
	}

	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return false;
	}
	

}
