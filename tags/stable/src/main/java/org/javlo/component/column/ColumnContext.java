package org.javlo.component.column;

import javax.servlet.http.HttpServletRequest;

public class ColumnContext {
	
	private static final String KEY = ColumnContext.class.getName();
	
	private boolean open = false;
	private int count = 0;
	private boolean withTable = true;
	
	public static ColumnContext getInstance(HttpServletRequest request) {
		ColumnContext outCtx = (ColumnContext) request.getAttribute(KEY);
		if (outCtx == null) {
			outCtx = new ColumnContext();
			request.setAttribute(KEY, outCtx);
		}
		return outCtx;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public boolean isWithTable() {
		return withTable;
	}

	public void setWithTable(boolean withTable) {
		this.withTable = withTable;
	}

}
