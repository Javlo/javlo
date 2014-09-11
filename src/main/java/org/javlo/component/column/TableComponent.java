package org.javlo.component.column;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;

public abstract class TableComponent extends AbstractPropertiesComponent {

	private static final List<String> fields = Arrays.asList(new String[] { "padding", "width", "valign", "align", "colspan" });

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
			if ((tableContext.isFirst(this) || tableContext.isLast(this)) && (!tableContext.getTableBreak().isGrid(ctx) && !tableContext.getTableBreak().isBorder(ctx))) {
				if (tableContext.isFirst(this)) {
					outStyle.append("padding: " + padding + " " + padding + " " + padding + " 0;");
				}
				if (tableContext.isLast(this)) {
					outStyle.append("padding: " + padding + " 0 " + padding + " " + padding + ";");
				}
			} else {
				outStyle.append("padding:" + padding + ';');
			}
		}
		
		String width = getWidth(ctx);
		if (width != null && width.trim().length() > 0) {
			outStyle.append("width:" + getWidth(ctx) + "%;");
		} else if (!tableContext.isCellWidth()) {
			if (tableContext.getRowSize(this) == tableContext.getMaxRowSize()) {
				String widthStr = StringHelper.renderDouble(((double) 100 / (double) tableContext.getMaxRowSize()), 2, '.');
				outStyle.append("width:" + widthStr + "%;");
			}
		}
		if (getVAlign(ctx).trim().length() > 0) {
			outStyle.append("vertical-align:" + getVAlign(ctx) + ';');
		}
		if (getAlign(ctx).trim().length() > 0) {
			outStyle.append("text-align:" + getAlign(ctx) + ';');
		}
		return outStyle.toString();
	}

	protected TableContext getContext(ContentContext ctx) throws Exception {
		return TableContext.getInstance(ctx, this);
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

	protected boolean isWidth(ContentContext ctx) {
		String width = getFieldValue("width");
		if (width == null || width.trim().length() == 0) {
			return false;
		} else {
			return true;
		}
	}

	protected String getVAlign(ContentContext ctx) {
		return getFieldValue("valign", "");
	}

	protected String getAlign(ContentContext ctx) {
		return getFieldValue("align");
	}

	protected int getColspan() {
		return Integer.parseInt(getFieldValue("colspan", "1"));
	}

	@Override
	protected String getEmptyCode(ContentContext ctx) throws Exception {
		return getViewXHTMLCode(ctx) + super.getPrefixViewXHTMLCode(ctx) + "<div class=\"table-component-preview " + getType() + "\">" + getType() + "</div>" + super.getSuffixViewXHTMLCode(ctx);
	}

	protected String getColSpanHTML(ContentContext ctx) throws Exception {
		TableContext tableContext = getContext(ctx);
		int colsSpan = (tableContext.getMaxRowSize() - tableContext.getRowSize(this)) + 1;
		String colsSpanHTML = "";
		if (colsSpan > 1 && tableContext.isFirst(this)) {
			colsSpanHTML = " colspan=\"" + colsSpan + "\"";
		} else if (getColspan() > 1) {
			colsSpanHTML = " colspan=\"" + getColspan() + "\"";
		}
		return colsSpanHTML;
	}

	protected String getPaddingInputName() {
		return createKeyWithField("padding");
	}

	protected String getVAlignInputName() {
		return createKeyWithField("valign");
	}

	protected String getAlignInputName() {
		return createKeyWithField("align");
	}

	protected String getWidthInputName() {
		return createKeyWithField("width");
	}

	protected String getColspanInputName() {
		return createKeyWithField("colspan");
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getPaddingInputName() + "\">padding : </label>");
		out.println("<input name=\"" + getPaddingInputName() + "\" value=\"" + getFieldValue("padding") + "\" />");
		out.println("</div>");
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getWidthInputName() + "\">width : </label>");
		out.println("<input name=\"" + getWidthInputName() + "\" value=\"" + getFieldValue("width") + "\" /> %");
		out.println("</div>");
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getAlignInputName() + "\">align : </label>");
		out.println(XHTMLHelper.getRadioInput(getAlignInputName(), new String[][] { { "", "inherited" }, { "left", "left" }, { "right", "right" }, { "center", "center" }, { "justify", "justify" } }, getFieldValue("align", ""), null));
		out.println("</div>");
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getVAlignInputName() + "\">vertical-align : </label>");
		out.println(XHTMLHelper.getRadioInput(getVAlignInputName(), new String[][] { { "", "inherited" }, { "top", "top" }, { "middle", "middle" }, { "bottom", "bottom" } }, getFieldValue("valign", ""), null));
		out.println("</div>");
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getColspanInputName() + "\">colspan : </label>");
		TableContext tableContext = getContext(ctx);
		String[][] colspanChoice;
		if (tableContext.getMaxRowSize() > 0) {
			colspanChoice = new String[tableContext.getMaxRowSize() - 1][];
			for (int i = 1; i < tableContext.getMaxRowSize(); i++) {
				colspanChoice[i - 1] = new String[2];
				colspanChoice[i - 1][0] = "" + i;
				colspanChoice[i - 1][1] = "" + i;
			}
		} else {
			colspanChoice = new String[0][0];
		}
		out.println(XHTMLHelper.getRadioInput(getColspanInputName(), colspanChoice, "" + getColspan(), null));
		out.println("</div>");

		out.close();
		return new String(outStream.toByteArray());
	}

	protected boolean isCellEmpty(ContentContext ctx) throws Exception {
		ContentContext ctxCompArea = ctx.getContextWithArea(getArea());
		ContentElementList content = getPage().getContent(ctxCompArea);
		while (content.hasNext(ctxCompArea)) {
			IContentVisualComponent comp = content.next(ctxCompArea);
			if (comp.getId().equals(getId())) {
				IContentVisualComponent nextComp = content.next(ctxCompArea);
				if (nextComp == null || nextComp instanceof TableComponent) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * return true if the component break the row.
	 * 
	 * @return
	 */
	public boolean isRowBreak() {
		return false;
	}
}
