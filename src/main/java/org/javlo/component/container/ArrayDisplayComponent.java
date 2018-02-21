package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.utils.CSVFactory;
import org.javlo.utils.Cell;
import org.javlo.utils.XLSTools;

public class ArrayDisplayComponent extends AbstractVisualComponent implements IAction {

	public static final String TYPE = "array-display-component";

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
		return "table";
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return !StringHelper.isEmpty(getValue());
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		File sourceFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), getValue()));
		if (sourceFile.exists()) {
			Cell[][] data = null;
			String ext = StringHelper.getFileExtension(sourceFile.getName()).toLowerCase();
			if (ext.equals("csv")) {
				CSVFactory newCSV = new CSVFactory(sourceFile);
				data = XLSTools.getCellArray(newCSV.getArray());
			} else if (ext.equals("xslx")) {
				data = XLSTools.getArray(ctx, sourceFile);
			}
			ctx.getRequest().setAttribute("data", data);
			ctx.getRequest().setAttribute("items", XLSTools.getItems(ctx, data));
		}		
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);
		Cell[][] data = (Cell[][])ctx.getRequest().getAttribute("data");
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<table>");
		for (Cell[] cells : data) {
			out.println("<tr>");
			for (Cell cell : cells) {
				out.print("<td>"+cell.getValue()+"</td>");
			}
			out.println("</tr>");
		}
		out.println("</table>");
		out.close();
		return new String(outStream.toByteArray());
	}
}
