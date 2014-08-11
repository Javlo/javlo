package org.javlo.component.column;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;

public class TableContext {
	
	private static final String KEY = TableContext.class.getName();
	
	private Stack<TableComponent> components = new Stack<TableComponent>();
	
	private int rowSize = 0;
	
	private boolean tableOpen = false;
	
	public static TableContext getInstance(ContentContext ctx, IContentVisualComponent currentComponent) throws Exception {
		TableContext outCtx = (TableContext) ctx.getRequest().getAttribute(KEY);
		if (outCtx == null || outCtx.components.size() == 0 && currentComponent != null) {
			outCtx = new TableContext();
			ContentElementList content = ctx.getCurrentPage().getContent(ctx);			
			IContentVisualComponent comp = content.next(ctx);
			while (content.hasNext(ctx) && !comp.getId().equals(currentComponent.getId())) {
				comp = content.next(ctx);				
			}
			outCtx.addTableComponent((TableComponent)comp);
			int maxRowSize = 1;
			while (content.hasNext(ctx) && !(comp instanceof TableBreak)) {
				comp = content.next(ctx);
				if (comp instanceof TableComponent) {
					if (comp instanceof CellBreak) {
						maxRowSize++;						
					} else if (comp instanceof RowBreak) {
						if (maxRowSize > outCtx.rowSize) {
							outCtx.rowSize = maxRowSize;
						}
						maxRowSize = 1;
					}
					outCtx.addTableComponent((TableComponent)comp);
				}
			}
			if (maxRowSize > outCtx.rowSize) {
				outCtx.rowSize = maxRowSize;
			}			
			ctx.getRequest().setAttribute(KEY, outCtx);
			
			/*System.out.println("");
			System.out.println("components structure : ");
			for (TableComponent tComp : outCtx.components) {
				System.out.println("     "+tComp.getType());
			}
			System.out.println("");*/
		}
		return outCtx;
	}
	
	public static boolean isInstance(ContentContext ctx) throws Exception {
		return ctx.getRequest().getAttribute(KEY) != null;
	}
	
	/**
	 * reutrn the size of the row (the number of cellBreak in the row +1).
	 * @param comp
	 * @return -1 if currentComp not found in table context or found as last element.
	 */
	public int getRowSize(TableComponent currentComp) {
		Iterator<TableComponent> comps = components.iterator();
		TableComponent comp = comps.next();
		int size = 1;
		while (comps.hasNext() && !comp.getId().equals(currentComp.getId())) {
			comp = comps.next();
		}
		if (!comps.hasNext()) {
			return -1;
		} else {
			comp = comps.next();
			while (comps.hasNext() && !(comp instanceof RowBreak)) {
				size++;
				comp = comps.next();				
			}
			if (!comps.hasNext() && !(comp instanceof RowBreak)) {
				size++;
			}
		}
		return size; 
	}
	
	public boolean isTableOpen() {
		return tableOpen;
	}

	public void setTableOpen(boolean tableOpen) {		
		if (!tableOpen) {
			components.clear();
		}
		this.tableOpen = tableOpen;
	}
	
	public void addTableComponent (TableComponent comp) {
		components.add(comp);
	}
	
	public List<TableComponent> getComponents() {
		return components;
	}
	
	public TableComponent getLastComponent() {
		return components.lastElement();
	}
	
	public int getMaxRowSize() {
		return rowSize;
	}

}
