package org.javlo.component.column;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class TableBreak extends TableComponent {
	
	private static final List<String> tableFields = Arrays.asList(new String[] {"padding","width","valign","align","border","grid","spacing"});
	
	private String TYPE = "table-break";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return tableFields;
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {		
		return "</td></tr></table>";
	}
	
	public static String closeTable(ContentContext ctx, TableContext tableContext) {
		tableContext.resetTable(ctx); 
		return "</td></tr></table>";
	}
	
	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		try {
			getContext(ctx).resetTable(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.getSuffixViewXHTMLCode(ctx);
	}
	
	public String getOpenTableStyle(ContentContext ctx) {
		StringBuffer style = new StringBuffer();
		if (isBorder(ctx)) {
			style.append(" border: 1px #333333 solid;");
		} 
		if (getSpacing(ctx) != null && getSpacing(ctx).trim().length() > 0) {
			style.append("border-spacing:"+getSpacing(ctx)+"; border-collapse: separate;");
		}
		return style.toString();
	}
	
	protected String getBorderInputString() {
		return createKeyWithField("border");
	}
	
	protected String getGridInputString() {
		return createKeyWithField("grid");
	}

	protected String getSpacingInputString() {
		return createKeyWithField("spacing");
	}
	
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"line\">");
		out.println("<label for=\""+getBorderInputString()+"\">border: </label>");
		String checked = "";
		if (isBorder(ctx)) {
			checked = " checked=\"checked\"";
		}
		out.println("<input type=\"checkbox\" name=\""+getBorderInputString()+"\" "+checked+" />");
		out.println("</div>");
		out.println("<div class=\"line\">");
		out.println("<label for=\""+getGridInputString()+"\">grid: </label>");
		checked = "";
		if (isGrid(ctx)) {
			checked = " checked=\"checked\"";
		}
		out.println("<input type=\"checkbox\" name=\""+getGridInputString()+"\" "+checked+" />");
		out.println("</div>");
		out.println("<div class=\"line\">");
		out.println("<label for=\""+getSpacingInputString()+"\">spacing : </label>");
		out.println("<input name=\""+getSpacingInputString()+"\" value=\""+getSpacing(ctx)+"\" />");
		out.println("</div>");
		
		out.println("<fieldset>");
		out.println("<legend>default</legend>");
		out.print(super.getEditXHTMLCode(ctx));
		out.println("</fieldset>");

		out.close();
		return new String(outStream.toByteArray());
	}

	public boolean isBorder(ContentContext ctx) {
		return StringHelper.isTrue(getFieldValue("border"));
	}
	
	public boolean isGrid(ContentContext ctx) {
		return StringHelper.isTrue(getFieldValue("grid"));
	}
	
	public String getSpacing(ContentContext ctx) {
		return getFieldValue("spacing");
	}
	
	@Override
	public boolean isRowBreak() {	
		return true;
	}

}
