package org.javlo.component.column;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;

public class TableBreak extends TableComponent {

	private static final List<String> tableFields = Arrays.asList(new String[] { "padding", "width", "valign", "align", "border", "grid", "spacing", "col", "row", "backgroundcolor", "bordersize", "bordercolor" });

	protected static final Set<String> FIELD_NUMBER_ONLY = new HashSet<String>(Arrays.asList(new String[] { "width", "colspan", "col", "row" }));

	public static String TYPE = "table-break";

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
	}

	@Override
	protected Set<String> getFieldNumberOnly() {
		return FIELD_NUMBER_ONLY;
	}

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
		TableContext tableContext = TableContext.getInstance(ctx, this);
		if (!tableContext.getFirstComponent().getId().equals(getId())) {
			return "</div></td></tr></table>";
		} else {
			return "";
		}

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
			String spacing = getSpacing(ctx);
			if (StringHelper.isDigit(spacing)) {
				spacing = spacing + "px";
			}
			style.append("border-spacing:" + spacing + "; border-collapse: separate;");
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
		setFieldValue("col", "" + tableContext.getMaxRowSize());
		if (tableContext.getMaxRowSize() == 0) {
			setFieldValue("row", "0");
		} else {
			int countRow = 1;
			for (TableComponent comp : tableContext.getComponents()) {
				if (comp instanceof OpenRow && !tableContext.isFirstComponent(comp)) {
					countRow++;
				}
			}
			setFieldValue("row", "" + countRow);
		}
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		countSize(ctx);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getBorderInputString() + "\">border: </label>");
		String checked = "";
		if (isBorder(ctx)) {
			checked = " checked=\"checked\"";
		}
		out.println("<input type=\"checkbox\" name=\"" + getBorderInputString() + "\" " + checked + " />");
		out.println("</div>");
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getGridInputString() + "\">grid: </label>");
		checked = "";
		if (isGrid(ctx)) {
			checked = " checked=\"checked\"";
		}
		out.println("<input type=\"checkbox\" name=\"" + getGridInputString() + "\" " + checked + " />");
		out.println("</div>");
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getSpacingInputString() + "\">spacing : </label>");
		out.println("<input name=\"" + getSpacingInputString() + "\" value=\"" + getSpacing(ctx) + "\" />");
		out.println("</div>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + createKeyWithField("col") + "\">number of colums : </label>");
		out.println("<input name=\"" + createKeyWithField("col") + "\" id=\"" + createKeyWithField("col") + "\" value=\"" + getFieldValue("col") + "\" />");
		out.println("</div>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + createKeyWithField("row") + "\">number of rows : </label>");
		out.println("<input name=\"" + createKeyWithField("row") + "\" id=\"" + createKeyWithField("row") + "\" value=\"" + getFieldValue("row") + "\" />");
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
	public boolean initContent(ContentContext ctx) throws Exception {
		super.initContent(ctx);
		if (isEditOnCreate(ctx)) {
			return false;
		}

		IContentVisualComponent previous = ComponentHelper.getPreviousComponent(this, ctx);
		while (previous != null && !(previous instanceof TableComponent)) {
			previous = ComponentHelper.getPreviousComponent(previous, ctx);
		}

		if (previous == null || previous instanceof TableBreak) {
			String previousId = ComponentHelper.getPreviousComponentId(this, ctx);
			MacroHelper.addContent(ctx.getRequestContentLanguage(), getPage(), previousId, OpenCell.TYPE, "", getArea(), "", ctx.getCurrentEditUser());
			MacroHelper.addContent(ctx.getRequestContentLanguage(), getPage(), previousId, OpenRow.TYPE, "", getArea(), "", ctx.getCurrentEditUser());
			MacroHelper.addContent(ctx.getRequestContentLanguage(), getPage(), previousId, OpenCell.TYPE, "", getArea(), "", ctx.getCurrentEditUser());
			MacroHelper.addContent(ctx.getRequestContentLanguage(), getPage(), previousId, OpenCell.TYPE, "", getArea(), "", ctx.getCurrentEditUser());
		}
		return true;
	}

	public TableComponent getOpenTableComponent(ContentContext ctx) throws Exception {
		ContentContext compAreaContext = ctx.getContextWithArea(getArea());
		ContentElementList content = getPage().getContent(compAreaContext);
		boolean inTable = false;
		IContentVisualComponent firstComp = null;
		while (content.hasNext(compAreaContext)) {
			IContentVisualComponent comp = content.next(compAreaContext);
			if (firstComp == null) {
				firstComp = comp;
			}
			if (!inTable && comp instanceof TableComponent) {
				inTable = true;
				firstComp = comp;
			}
			if (comp.getId().equals(getId())) {
				return (TableComponent) firstComp;
			} else if (comp instanceof TableBreak) {
				inTable = false;
			}
		}
		return null;
	}

	protected boolean updateTable(ContentContext ctx, int newCol, int newRow) throws Exception {
		if (newCol < 0 && newRow < 0) {
			return false;
		}
		if (newCol == 0) {
			newRow = 0;
		}

		countSize(ctx);
		int col = Integer.parseInt(getFieldValue("col"));
		int row = Integer.parseInt(getFieldValue("row"));

		boolean modifContent = false;
		TableContext tableContext = TableContext.getInstance(ctx, this);
		while (tableContext.getMaxRowSize() > newCol) {
			TableComponent previewComp = null;
			List<String> needDel = new LinkedList<String>();
			for (TableComponent comp : tableContext.getComponents()) {
				if (previewComp != null && (comp instanceof OpenRow || comp instanceof TableBreak)) {
					if (tableContext.getRowSize(previewComp) > newCol) {
						IContentVisualComponent currentComp = previewComp;
						IContentVisualComponent nextComp = ComponentHelper.getNextComponent(currentComp, ctx);
						needDel.add(currentComp.getId());
						while (!(nextComp instanceof TableComponent) && nextComp != null) {
							currentComp = nextComp;
							nextComp = ComponentHelper.getNextComponent(currentComp, ctx);
							needDel.add(currentComp.getId());
						}
					}
				}
				previewComp = comp;
			}
			for (String id : needDel) {
				getPage().removeContent(ctx, id);
			}
			tableContext.refresh(ctx, this);
		}
		for (int i = col; i < newCol; i++) {
			IContentVisualComponent prvComp = this;
			while (prvComp != null && !prvComp.getId().equals(tableContext.getFirstComponentId())) {
				if (prvComp.getType().equals(OpenRow.TYPE)) {
					MacroHelper.addContent(ctx.getRequestContentLanguage(), getPage(), ComponentHelper.getPreviousComponentId(prvComp, ctx), OpenCell.TYPE, null, getArea(), "", ctx.getCurrentEditUser());
				}
				prvComp = ComponentHelper.getPreviousComponent(prvComp, ctx);
			}
			MacroHelper.addContent(ctx.getRequestContentLanguage(), getPage(), ComponentHelper.getPreviousComponentId(this, ctx), OpenCell.TYPE, null, getArea(), "", ctx.getCurrentEditUser());
		}
		if (row == 0) {
			row++;
		}
		for (int i = row; i < newRow; i++) {
			modifContent = true;
			String rowId = MacroHelper.addContent(ctx.getRequestContentLanguage(), getPage(), ComponentHelper.getPreviousComponentId(this, ctx), OpenRow.TYPE, null, getArea(), "", ctx.getCurrentEditUser());
			for (int c = 1; c < Math.max(tableContext.getMaxRowSize(), newCol); c++) {
				MacroHelper.addContent(ctx.getRequestContentLanguage(), getPage(), rowId, OpenCell.TYPE, null, getArea(), "", ctx.getCurrentEditUser());
			}
		}
		if (newRow > 0) {
			for (int i = newRow; i < row; i++) {
				modifContent = true;
				IContentVisualComponent prvComp = ComponentHelper.getPreviousComponent(this, ctx);
				while (prvComp != null && !prvComp.getType().equals(OpenRow.TYPE)) {
					prvComp = ComponentHelper.getPreviousComponent(prvComp, ctx);
				}
				if (prvComp != null) { // check if there are openrow before
										// table break
					prvComp = ComponentHelper.getPreviousComponent(this, ctx);
					while (prvComp != null && !prvComp.getType().equals(OpenRow.TYPE)) {
						String id = prvComp.getId();
						if (prvComp.getId().equals(tableContext.getFirstComponent().getId())) {
							prvComp = null;
						} else {
							prvComp = ComponentHelper.getPreviousComponent(this, ctx);
						}
						getPage().removeContent(ctx, id);
					}
					if (prvComp != null) {
						getPage().removeContent(ctx, prvComp.getId());
					}
				}
			}
		}

		return modifContent;
	}

	@Override
	public void delete(ContentContext ctx) {
		try {
			setFieldValue("col", "0");
			setFieldValue("row", "0");
			Collection<String> toDel = new LinkedList<String>();
			Collection<String> prepareDel = new LinkedList<String>();
			IContentVisualComponent comp = ComponentHelper.getPreviousComponent(this, ctx);
			while (comp != null && !comp.getType().equals(TYPE)) {
				prepareDel.add(comp.getId());
				if (comp instanceof OpenRow || comp instanceof OpenCell) {
					toDel.addAll(prepareDel);
					prepareDel.clear();
				}
				comp = ComponentHelper.getPreviousComponent(comp, ctx);
			}
			for (String toDelComp : toDel) {
				getPage().removeContent(ctx, toDelComp, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.delete(ctx);
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		String msg = super.performEdit(ctx);
		try {
			int newCol = Integer.parseInt(getFieldValue("col"));
			int newRow = Integer.parseInt(getFieldValue("row"));
			updateTable(ctx, newCol, newRow);
		} catch (Throwable t) {
			logger.warning(t.getMessage());
		}
		return msg;
	}

}
