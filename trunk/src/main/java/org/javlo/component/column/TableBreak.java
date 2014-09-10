package org.javlo.component.column;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.service.ContentService;

public class TableBreak extends TableComponent {
	
	private static final List<String> tableFields = Arrays.asList(new String[] {"padding","width","valign","align","border","grid","spacing","col","row"});
	
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
		return "</div></td></tr></table>";
	}
	
	public static String closeTable(ContentContext ctx, TableContext tableContext) {
		tableContext.resetTable(ctx); 
		return "</div></td></tr></table>";
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
	
	protected void countSize(ContentContext ctx) throws Exception {
		TableContext tableContext = TableContext.getInstance(ctx, this);		
		setFieldValue("col", ""+tableContext.getMaxRowSize());
		int countRow = 1;
		for (TableComponent comp : tableContext.getComponents()) {
			if (comp instanceof RowBreak) {
				countRow++;
			}
		}		
		setFieldValue("row", ""+countRow);
	}
	
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		countSize(ctx);
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
		
		out.println("<div class=\"line\">");
		out.println("<label for=\""+createKeyWithField("col")+"\">number of colums : </label>");
		out.println("<input name=\""+createKeyWithField("col")+"\" id=\""+createKeyWithField("col")+"\" value=\""+getFieldValue("col")+"\" />");
		out.println("</div>");

		out.println("<div class=\"line\">");
		out.println("<label for=\""+createKeyWithField("row")+"\">number of rows : </label>");
		out.println("<input name=\""+createKeyWithField("row")+"\" id=\""+createKeyWithField("row")+"\" value=\""+getFieldValue("row")+"\" />");
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
	
	@Override
	public void performEdit(ContentContext ctx) throws Exception {		
		super.performEdit(ctx);
		int newCol = Integer.parseInt(getFieldValue("col"));
		int newRow = Integer.parseInt(getFieldValue("row"));
		countSize(ctx);
		boolean modifContent = false;
		int col = Integer.parseInt(getFieldValue("col"));
		int row = Integer.parseInt(getFieldValue("row"));
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		TableContext tableContext = TableContext.getInstance(ctx, this);
		/** add row **/
		for (int i=row;i<newRow;i++) {
			modifContent = true;
			MacroHelper.addContent(ctx.getRequestContentLanguage(), getPage(), ComponentHelper.getPreviousComponent(this,ctx).getId(), RowBreak.TYPE, "", ctx.getCurrentEditUser());
		}
		for (int i=newRow;i<row;i++) {
			modifContent = true;
			IContentVisualComponent prvComp = ComponentHelper.getPreviousComponent(this,ctx);			
			while (!prvComp.getType().equals(RowBreak.TYPE)) {
				String id = prvComp.getId();
				prvComp = ComponentHelper.getPreviousComponent(this,ctx);
				getPage().removeContent(ctx, id);
			}
			getPage().removeContent(ctx, prvComp.getId());
		}
		for (int i=col;i<newCol;i++) {			
			IContentVisualComponent prvComp = ComponentHelper.getPreviousComponent(this,ctx);
			while (!prvComp.getId().equals(tableContext.getFirstComponent().getId())) {
				if (prvComp.getType().equals(RowBreak.TYPE)) {
					MacroHelper.addContent(ctx.getRequestContentLanguage(), getPage(), ComponentHelper.getPreviousComponent(prvComp,ctx).getId(), CellBreak.TYPE, "", ctx.getCurrentEditUser());
				}
				prvComp = ComponentHelper.getPreviousComponent(prvComp,ctx);
			}
			MacroHelper.addContent(ctx.getRequestContentLanguage(), getPage(), ComponentHelper.getPreviousComponent(this,ctx).getId(), CellBreak.TYPE, "", ctx.getCurrentEditUser());
		}		
	}

}
