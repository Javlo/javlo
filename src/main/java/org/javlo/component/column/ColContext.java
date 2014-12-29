package org.javlo.component.column;

import java.util.LinkedList;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

public class ColContext {

	public static final int MAX_WIDTH = 12;

	private static final String KEY = ColContext.class.getName();

	private LinkedList<OpenCol> components = new LinkedList<OpenCol>();

	private int rowWidth = 0;

	private boolean open = false;

	public static ColContext getInstance(ContentContext ctx, IContentVisualComponent currentComponent) throws Exception {
		ColContext outCtx = (ColContext) ctx.getRequest().getAttribute(KEY);
		if (outCtx == null) {
			outCtx = new ColContext();
			outCtx.refresh(ctx, currentComponent);
			ctx.getRequest().setAttribute(KEY, outCtx);
		}
		return outCtx;
	}

	public void reset() {
		rowWidth = 0;
		open = false;
		components.clear();
	}

	public void refresh(ContentContext ctx, IContentVisualComponent currentComponent) throws Exception {
		components = new LinkedList<OpenCol>();
		MenuElement page = currentComponent.getPage();
		ContentElementList content = page.getContent(ctx);
		IContentVisualComponent comp = content.next(ctx);
		IContentVisualComponent firstComp = null;
		int width = 0;
		while (content.hasNext(ctx) && !comp.getId().equals(currentComponent.getId())) {
			if (comp instanceof OpenCol) {
				if (firstComp == null) {
					firstComp = comp;
				}
				width = width + ((OpenCol) comp).getWidth(ctx);
				if (width > MAX_WIDTH) {
					firstComp = null;
				}
				components.add((OpenCol) comp);
			}
			comp = content.next(ctx);
		}	
	}

	public static boolean isInstance(ContentContext ctx) throws Exception {
		return ctx.getRequest().getAttribute(KEY) != null;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public boolean isFirst(IContentVisualComponent comp) {
		if (components.isEmpty()) {
			return false;
		} else {
			return components.getFirst().getId().equals(comp.getId());
		}
	}

	public boolean isLast(IContentVisualComponent comp) {
		if (components.isEmpty()) {
			return false;
		} else {
			return components.getLast().getId().equals(comp.getId());
		}
	}

	public int getRowWidth() {
		return rowWidth;
	}

	public void setRowWidth(int rowWidth) {
		this.rowWidth = rowWidth;
	}
	
	

}
