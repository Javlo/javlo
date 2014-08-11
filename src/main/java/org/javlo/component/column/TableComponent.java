package org.javlo.component.column;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.XHTMLHelper;

public abstract class TableComponent extends AbstractPropertiesComponent {
	
	private static final List<String> fields = Arrays.asList(new String[] {"padding","width","valign"});

	public TableComponent() {		
	}
	
	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return fields;
	}
		
	protected String getTDStyle(ContentContext ctx) throws Exception {				
		StringBuffer outStyle = new StringBuffer();
		TableContext tableContext = getContext(ctx);
		
		String padding = getPadding(ctx);
		if (padding != null && padding.trim().length() > 0) {
			if ((tableContext.isFirst(this) || tableContext.isLast(this)) && (tableContext.getTableBreak().isGrid(ctx) || tableContext.getTableBreak().isBorder(ctx))) {
				if (tableContext.isFirst(this)) {
					outStyle.append("padding: "+padding+" "+padding+" "+padding+" 0;");
				}
				if (tableContext.isLast(this)) {
					outStyle.append("padding: "+padding+" 0 "+padding+" "+padding+";");
				}
			} else {
				outStyle.append("padding:"+padding+';');
			}			
		}
		String width = getWidth(ctx);
		if (width != null && width.trim().length() > 0) {
			outStyle.append("width:"+getWidth(ctx)+"%;");
		}
		outStyle.append("vertical-align:"+getVAlign(ctx)+';');
		return outStyle.toString(); 
	}
	
	protected TableContext getContext(ContentContext ctx) throws Exception {
		return TableContext.getInstance(ctx,this);
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
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return getEmptyCode(ctx);
	}
	
	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return true;
	}
	
	protected String getPadding(ContentContext ctx) {
		return getFieldValue("padding");
	}
	
	protected String getWidth(ContentContext ctx) {
		return getFieldValue("width");
	}
	
	protected String getVAlign(ContentContext ctx) {
		return getFieldValue("valign","top");
	}
	
	@Override
	protected String getEmptyCode(ContentContext ctx) throws Exception {	
		return getViewXHTMLCode(ctx)+super.getPrefixViewXHTMLCode(ctx)+"<div class=\"table-component-preview "+getType()+"\">"+getType()+"</div>"+super.getSuffixViewXHTMLCode(ctx);
	}
	
	protected String getColSpanHTML(ContentContext ctx) throws Exception {
		TableContext tableContext = getContext(ctx);
		int colsSpan = (tableContext.getMaxRowSize()-tableContext.getRowSize(this))+1;
		String colsSpanHTML = "";
		if (colsSpan > 1) {
			colsSpanHTML=" colspan=\""+colsSpan+"\"";
		}
		return colsSpanHTML;
	}
	
	protected String getPaddingInputName() {
		return createKeyWithField("padding");
	}
	
	protected String  getVAlignInputName() {
		return createKeyWithField("valign");
	}
	
	protected String  getWidthInputName() {
		return createKeyWithField("width");
	}
	
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"line\">");
		out.println("<label for=\""+getPaddingInputName()+"\">padding : </label>");
		out.println("<input name=\""+getPaddingInputName()+"\" value=\""+getFieldValue("padding")+"\" />");
		out.println("</div>");
		out.println("<div class=\"line\">");
		out.println("<label for=\""+getWidthInputName()+"\">width : </label>");
		out.println("<input name=\""+getWidthInputName()+"\" value=\""+getFieldValue("width")+"\" /> %");
		out.println("</div>");
		out.println("<div class=\"line\">");
		out.println("<label for=\""+getVAlignInputName()+"\">vertical-align : </label>");				
		out.println(XHTMLHelper.getInputOneSelect(getVAlignInputName(), Arrays.asList(new String[] {"top", "middle", "bottom"}), getVAlign(ctx), false));		
		out.println("</div>");

		out.close();
		return new String(outStream.toByteArray());
	}
}
