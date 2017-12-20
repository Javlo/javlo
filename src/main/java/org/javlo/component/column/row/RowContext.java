package org.javlo.component.column.row;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

public class RowContext {
	
	private static final String KEY = RowContext.class.getName();
	
	private int colNumber = 1;
	
	private Row rowComponent;
	
	public static RowContext getInstance(ContentContext ctx) throws Exception {
		RowContext outCtx = (RowContext) ctx.getRequest().getAttribute(KEY);
		if (outCtx == null) {			
			outCtx = new RowContext();			
			ctx.getRequest().setAttribute(KEY, outCtx);
		}
		return outCtx;
	}

	public int getColNumber() {
		return colNumber;
	}

	public void setColNumner(int colNumber) {
		this.colNumber = colNumber;
	}

	public Row getRowComponent(ContentContext ctx, MenuElement page, String area) throws Exception {
		if (rowComponent == null) {
			ContentContext ctxCompArea = ctx.getContextWithArea(area);
			ContentElementList content = page.getContent(ctxCompArea);
			while (content.hasNext(ctxCompArea)) {
				IContentVisualComponent comp = content.next(ctxCompArea);
				if (comp.getType().equals(Row.TYPE)) {
					rowComponent = (Row) comp;
					return rowComponent;
				}
			}
		}
		return rowComponent;
	}
	
	public boolean isFirst() {
		return getColNumber()==1;
	}

	public static void reset(ContentContext ctx) {
		ctx.getRequest().removeAttribute(KEY);
	}
	
	public void nextCol() {
		colNumber++;
	}
	
	
}
