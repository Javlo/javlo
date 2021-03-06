package org.javlo.component.column;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;

public class TableContext {
	
	private static final String KEY = TableContext.class.getName();
	
	private LinkedList<TableComponent> components = new LinkedList<TableComponent>();	
	private Collection<TableComponent> first = new HashSet<TableComponent>();
	private Collection<TableComponent> last = new HashSet<TableComponent>();
	
	private int rowSize = 0;
	
	private boolean tableOpen = false;
	private boolean cellWidth = false;
	
	public static TableContext getInstance(ContentContext ctx, IContentVisualComponent currentComponent) throws Exception {
		TableContext outCtx = (TableContext) ctx.getRequest().getAttribute(KEY);
		if (outCtx == null) {			
			outCtx = new TableContext();
			outCtx.refresh(ctx,currentComponent);
			ctx.getRequest().setAttribute(KEY, outCtx);
		}
		return outCtx;
	}
	
	public void refresh(ContentContext ctx , IContentVisualComponent currentComponent) throws Exception {
		
		components = new LinkedList<TableComponent>();	
		first = new HashSet<TableComponent>();
		last = new HashSet<TableComponent>();		
		rowSize = 0;		
		tableOpen = false;
		cellWidth = false; 
		
		MenuElement page = currentComponent.getPage();
		if (page == null) {
			throw new Exception("page not found on component : "+currentComponent.getType()+" id:"+currentComponent.getId());
		}
		ContentElementList content = page.getContent(ctx);			
		IContentVisualComponent comp = content.next(ctx);
		IContentVisualComponent firstComp = null;
		
		/* search first component of the table contains 'currentComponent' */
		while (content.hasNext(ctx) && !comp.getId().equals(currentComponent.getId())) {
			if (firstComp == null && comp instanceof TableComponent) {
				firstComp = comp;
			}
			if (comp instanceof TableBreak) {
				firstComp = null;
			}
			comp = content.next(ctx);				
		}
		if (firstComp == null) {
			firstComp = comp;
		}
		/* position content iterator on the firstComp */
		content.initialize(ctx);
		comp = content.next(ctx);
		while (content.hasNext(ctx) && !comp.getId().equals(firstComp.getId())) {
			comp = content.next(ctx);
		}
		
		addTableComponent((TableComponent)comp);
		first.add((TableComponent)comp);
		int maxRowSize = 0;			
		while (content.hasNext(ctx) && !(comp instanceof TableBreak)) {
			comp = content.next(ctx);
			if (comp instanceof TableComponent) {
				if (((TableComponent) comp).isWidth(ctx)) {
					cellWidth = true;
				}
				maxRowSize = maxRowSize+((TableComponent) comp).getColspan();						
				if (((TableComponent) comp).isRowBreak()) {
					if (maxRowSize > rowSize) {
						rowSize = maxRowSize;
					}
					maxRowSize = 0;
					if (components.size() > 0) {
						last.add(components.getLast());
					}
				}					
				if (((TableComponent)comp).isRowBreak()) {
					first.add((TableComponent)comp);
				}
				addTableComponent((TableComponent)comp);
			}
		}
		last.add(components.getLast());
		if (maxRowSize >  rowSize) {
			rowSize = maxRowSize;
		}	
	}
	
	public boolean isFirst(TableComponent comp) {
		return first.contains(comp);
	}
	
	public boolean isLast(TableComponent comp) {
		return last.contains(comp);
	}
	
	public String getName(TableComponent inComp) {
		int row = -1;
		int cell = 0;
		for (TableComponent comp : components) {
			if (comp instanceof OpenRow) {
				cell=1;
				row++;
			} else if (comp instanceof OpenCell) {
				cell++;
				if (row < 0) {
					row = 0;
				}
			}
			if (comp == inComp) {
				return StringHelper.getNumberAsAlphabetic(row).toUpperCase()+cell;		
			}
		}
		return "?";
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
		int size = comp.getColspan();
		while (comps.hasNext() && !comp.getId().equals(currentComp.getId())) {
			comp = comps.next();			
			if (comp.isRowBreak()) {
				size = comp.getColspan();
			} else {
				size=size+comp.getColspan();
			}
		}
		if (!comps.hasNext()) {
			return -1;
		} else {
			comp = comps.next();
			while (comps.hasNext() && !(comp.isRowBreak())) {
				size=size+comp.getColspan();
				comp = comps.next();	
			}
			if (!comps.hasNext() && !(comp.isRowBreak())) {
				size=size+comp.getColspan();
			}
		}
		return size; 
	}
	
	public boolean isTableOpen() {
		return tableOpen;
	}
	
	public void openTable() {
		tableOpen = true;
	}

	public void resetTable(ContentContext ctx) {		
		ctx.getRequest().removeAttribute(KEY);				
	}
	
	private void addTableComponent (TableComponent comp) {
		components.add(comp);
	}
	
	public List<TableComponent> getComponents() {
		return components;
	}
	
	public TableComponent getFirstComponent() {
		return components.getFirst();
	}
	
	public String getFirstComponentId() {
		TableComponent first = getFirstComponent();
		if (first != null) {
			return first.getId();
		} else {
			return null;
		}
	}
	
	public boolean isFirstComponent(TableComponent comp) {
		return comp.getId().equals(getFirstComponentId());
	}
	
	public TableComponent getLastComponent() {
		return components.getLast();
	}
	
	public TableBreak getTableBreak() {
		TableComponent last = getLastComponent();
		if (last instanceof TableBreak) {
			return (TableBreak)last;
		} else {
			return DefaultTableBreak.instance;
		}
		
	}
	
	public int getMaxRowSize() {
		return rowSize;
	}
	
	/**
	 * true if there are at least one cell with "width" manually defined.
	 * @return
	 */
	public boolean isCellWidth() {
		return cellWidth;
	}

}
